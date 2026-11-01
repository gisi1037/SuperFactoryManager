package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.RegistryNamespaced;
import net.minecraft.util.RegistrySimple;
import net.minecraftforge.common.util.ForgeDirection;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.blueprint.BlueprintCompiler;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Type;
import ca.teamdman.sfm.client.blueprint.StudioDocument;
import ca.teamdman.sfm.client.blueprint.StudioExpressionSyntax;
import ca.teamdman.sfm.client.blueprint.StudioFieldDraft;
import ca.teamdman.sfm.client.blueprint.StudioFieldFeedback;
import ca.teamdman.sfm.client.blueprint.StudioFieldPicker;
import ca.teamdman.sfm.client.blueprint.StudioNodeProperties;
import ca.teamdman.sfm.client.blueprint.StudioTransferArguments;
import ca.teamdman.sfm.client.blueprint.StudioTransferArguments.Role;
import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class StudioTransferArgumentsTest {

    @Test
    void amountOnlyThirdArgumentKeepsItsSpanAndRoundTripsWithoutASelector() {
        String clause = "tRaNsFeR( LABEL(\"source\") ,\tLABEL(\"dest\"), 2 + 3 )";
        Node node = node(clause);
        StudioFieldDraft draft = draft(node);
        assertEquals(clause, apply(draft, node));
        UUID third = row(draft, Role.ITEM);
        draft.setTransferArgument(third, "7");
        String changed = apply(draft, node);
        assertEquals(clause.replace("2 + 3", "7"), changed);
        assertEquals("7", new StudioFieldDraft(draft).transferArgumentValue(third));
        assertNull(StudioFieldPicker.transferRowTarget(draft, third));
        ProgramBuildResult built = new ProgramBuilder(program(changed)).buildWithSyntax();
        assertTrue(built.isSuccess(), built.getErrorMessage());
        String visual = BlueprintCompiler.generate(BlueprintCompiler.importText(program(changed)));
        assertTrue(
            new ProgramBuilder(visual).build()
                .isSuccess());
        assertEquals(clause, node.clause());
    }

    private static final UUID PROJECT = new UUID(20, 1037);
    private static final String TWO = "tRaNsFeR( LABEL(\"source\") ,\t LABEL(\"dest\")  )";
    private static final String UNICODE = "\u03c0\uD83C\uDFED";
    private static final Item ITEM = new Item(), OTHER = new Item();

    static {
        try {
            Field namesField = RegistrySimple.class.getDeclaredField("registryObjects");
            namesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Object> names = (Map<String, Object>) namesField.get(Item.itemRegistry);
            names.put("sfm_studio_transfer:item", ITEM);
            names.put("sfm_studio_transfer:other", OTHER);
            Field idsField = RegistryNamespaced.class.getDeclaredField("underlyingIntegerMap");
            idsField.setAccessible(true);
            ObjectIntIdentityMap ids = (ObjectIntIdentityMap) idsField.get(Item.itemRegistry);
            ids.func_148746_a(ITEM, 32035);
            ids.func_148746_a(OTHER, 32036);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError(failure);
        }
    }

    private static String program(String clause) {
        return "every 20 ticks do " + clause + " end";
    }

    private static Node node(String clause) {
        return new Node(UUID.randomUUID(), Type.TRANSFER, 0, 0, clause);
    }

    private static StudioFieldDraft draft(Node node) {
        return new StudioFieldDraft(
            PROJECT,
            node,
            program(node.clause()),
            StudioNodeProperties.inspect(node.type, node.clause()));
    }

    private static String apply(StudioFieldDraft draft, Node node) {
        return draft.appliedClause(PROJECT, node, draft.source);
    }

    private static String repeat(String text, int count) {
        return String.join("", Collections.nCopies(count, text));
    }

    private static UUID row(StudioFieldDraft draft, Role role) {
        return draft.transferArgumentRows()
            .stream()
            .filter(r -> r.role == role)
            .findFirst()
            .get().id;
    }

    @Test
    void parsedRolesKeepNestedQuotedCommasEscapesUnicodeCaseAndSpacing() {
        String clause = "tRaNsFeR( LaBeL(  \"" + UNICODE
            + ",\\\"x\" ).slots(0,2) ,\t LABEL(\"dest\"), ITEM(\"*\"), (2+3)  )";
        StudioTransferArguments model = new StudioTransferArguments(clause);
        assertEquals(4, model.size());
        assertEquals(
            Role.SOURCE,
            model.rows()
                .get(0).role);
        assertEquals(
            "(2+3)",
            model.rows()
                .get(3).text);
        assertEquals(clause, model.applyTo(clause));
        Node node = node(clause);
        assertEquals(clause, apply(draft(node), node));
    }

    @Test
    void suffixTransitionsPreserveEveryUntouchedSpanAndReturnToExactOriginal() {
        StudioTransferArguments model = new StudioTransferArguments(TWO);
        UUID source = model.alias(Role.SOURCE), dest = model.alias(Role.DESTINATION);
        UUID item = model.append(" ITEM(\"*\") ");
        String three = TWO.replace("LABEL(\"dest\")  )", "LABEL(\"dest\"), ITEM(\"*\")  )");
        assertEquals(three, model.applyTo(TWO));
        UUID amount = model.append(" 0 ");
        assertEquals(three.replace("ITEM(\"*\")  )", "ITEM(\"*\"), 0  )"), model.applyTo(TWO));
        model.remove(amount);
        assertEquals(three, model.applyTo(TWO));
        model.remove(item);
        assertEquals(TWO, model.applyTo(TWO));
        assertEquals(
            source,
            model.rows()
                .get(0).id);
        assertEquals(
            dest,
            model.rows()
                .get(1).id);
        assertTrue(model.aliasesRetired());
    }

    @Test
    void onlyOptionalTailCanBeRemovedAndRefusalsKeepIdentityRevisionAndAliases() {
        StudioTransferArguments model = new StudioTransferArguments(TWO);
        UUID revision = model.revision();
        for (StudioTransferArguments.Row row : model.rows())
            assertThrows(IllegalArgumentException.class, () -> model.remove(row.id));
        assertEquals(revision, model.revision());
        assertFalse(model.aliasesRetired());
        UUID item = model.append("ITEM(\"*\")"), amount = model.append("1");
        revision = model.revision();
        assertThrows(IllegalArgumentException.class, () -> model.remove(item));
        assertThrows(IllegalArgumentException.class, () -> model.append("2"));
        assertEquals(revision, model.revision());
        assertEquals(4, model.size());
        model.remove(amount);
        assertEquals(
            Role.ITEM,
            model.rows()
                .get(2).role);
    }

    @Test
    void textEditsCopiesAndAggregateBoundsKeepRawPendingValues() {
        StudioTransferArguments model = new StudioTransferArguments(TWO);
        UUID source = model.alias(Role.SOURCE), revision = model.revision();
        StudioTransferArguments copy = new StudioTransferArguments(model);
        assertTrue(copy.set(source, " LABEL(\"new\") "));
        assertEquals(revision, copy.revision());
        assertEquals(" LABEL(\"new\") ", copy.value(source));
        assertEquals("LABEL(\"source\")", model.value(source));
        assertFalse(copy.set(source, " LABEL(\"new\") "));
        assertEquals(TWO.replace("LABEL(\"source\")", "LABEL(\"new\")"), copy.applyTo(TWO));
        assertThrows(IllegalArgumentException.class, () -> copy.set(source, repeat("1", 4096)));
        assertThrows(IllegalArgumentException.class, () -> copy.append(repeat("1", 4096)));
        assertThrows(
            UnsupportedOperationException.class,
            () -> copy.rows()
                .clear());
        assertFalse(copy.aliasesRetired());
    }

    @Test
    void newRolesAreBlankAndAtomicApplyRequiresAllRolesToBeRepaired() {
        Node node = node(TWO);
        StudioFieldDraft draft = draft(node);
        UUID item = draft.addTransferArgument("");
        assertThrows(IllegalArgumentException.class, () -> apply(draft, node));
        draft.setTransferArgument(item, "ITEM(\"*\")");
        UUID amount = draft.addTransferArgument("");
        assertThrows(IllegalArgumentException.class, () -> apply(draft, node));
        StudioFieldDraft copy = new StudioFieldDraft(draft);
        assertEquals("", copy.transferArgumentValue(amount));
        assertEquals(TWO, node.clause());
        copy.setTransferArgument(amount, "0");
        assertTrue(apply(copy, node).contains(", 0"));
        assertEquals("", draft.transferArgumentValue(amount));
    }

    @Test
    void initialAliasesDelegateThenAllRetirePermanentlyAcrossAbaAndCopies() {
        Node node = node(TWO);
        StudioFieldDraft draft = draft(node);
        UUID source = row(draft, Role.SOURCE);
        draft.set("source", " LABEL(\"new\") ");
        assertEquals(" LABEL(\"new\") ", draft.transferArgumentValue(source));
        assertEquals(StudioNodeProperties.update(Type.TRANSFER, TWO, "source", " LABEL(\"new\") "), apply(draft, node));
        UUID item = draft.addTransferArgument("ITEM(\"*\")");
        draft.removeTransferArgument(item);
        for (String key : Arrays.asList("source", "destination", "resource", "amount")) {
            assertTrue(draft.retiredTransferField(key));
            assertThrows(IllegalArgumentException.class, () -> draft.value(key));
            assertThrows(IllegalArgumentException.class, () -> draft.set(key, "1"));
        }
        StudioFieldDraft copy = new StudioFieldDraft(draft);
        assertTrue(copy.retiredTransferField("source"));
        copy.setTransferArgument(source, "LABEL(\"latest\")");
        assertTrue(apply(copy, node).contains("latest"));
        assertFalse(apply(draft, node).contains("latest"));
        UUID newItem = draft.addTransferArgument("ITEM(\"*\")");
        assertNotEquals(item, newItem);
        assertThrows(IllegalArgumentException.class, () -> draft.setTransferArgument(item, "ITEM(\"other\")"));
    }

    @Test
    void pendingInvalidTypesCommentsControlsAndInjectionNeverAlterOriginal() {
        for (String invalid : Arrays.asList(
            "",
            " ",
            "1,2",
            "LABEL(\"x\")) FORGET TRANSFER(",
            "1 -- comment",
            "1\n",
            "1\r",
            "1\0",
            "unknown()",
            "1+TRUE")) {
            Node node = node(TWO);
            StudioFieldDraft draft = draft(node);
            UUID item = draft.addTransferArgument(invalid);
            assertThrows(IllegalArgumentException.class, () -> apply(draft, node), invalid);
            assertEquals(invalid, draft.transferArgumentValue(item));
            assertEquals(TWO, node.clause());
        }
        Node node = node(TWO);
        StudioFieldDraft draft = draft(node);
        draft.setTransferArgument(row(draft, Role.SOURCE), "ITEM(\"*\")");
        assertThrows(IllegalArgumentException.class, () -> apply(draft, node));
        draft.setTransferArgument(row(draft, Role.SOURCE), "LABEL(\"source\")");
        draft.addTransferArgument("1");
        assertTrue(
            new ProgramBuilder(program(apply(draft, node))).build()
                .isSuccess());
        // With four arguments the third must still be a selector, not another amount.
        draft.addTransferArgument("2");
        assertThrows(IllegalArgumentException.class, () -> apply(draft, node));
    }

    @Test
    void variableRowsUseWholeProgramScopeValidation() {
        Node node = node(TWO);
        StudioFieldDraft draft = draft(node);
        UUID item = draft.addTransferArgument("$unknown");
        String clause = apply(draft, node);
        assertEquals("$unknown", draft.transferArgumentValue(item));
        assertFalse(
            new ProgramBuilder(program(clause)).build()
                .isSuccess());
        assertTrue(
            new ProgramBuilder(program("LET $unknown = ITEM(\"*\") " + clause)).build()
                .isSuccess());
        assertEquals(TWO, node.clause());
    }

    @Test
    void unsupportedInitialFormsAndResultCaptureKeepSourceManual() {
        for (String clause : Arrays.asList(
            "TRANSFER()",
            "TRANSFER(LABEL(\"source\"))",
            TWO + " INTO $result",
            TWO.replace("  )", ",ITEM(\"*\"),1,2)"),
            "TRANSFER(LABEL(\"source\"),LABEL(\"dest\"),)",
            "TRANSFER(unknown(),LABEL(\"dest\"))")) {
            Node node = node(clause);
            StudioFieldDraft draft = draft(node);
            assertFalse(draft.hasTransferArguments(), clause);
            assertEquals(clause, draft.original);
            assertEquals(clause, node.clause());
        }
    }

    @Test
    void lexerBudgetsProtectOriginalRowAggregateAndLegacyUpdaterWithoutChangingPrint() {
        String nested = "1";
        for (int i = 0; i < 31; i++) nested = "(" + nested + repeat("+1", 60) + ")";
        String deep = repeat("(", 33) + "1" + repeat(")", 33);
        for (String expression : Arrays.asList(
            nested,
            deep,
            repeat("!", 33) + "TRUE",
            "1" + repeat(" * 1", 65),
            "LABEL(\"x\")" + repeat(".available()", 33))) {
            assertThrows(IllegalArgumentException.class, () -> StudioExpressionSyntax.transferExpression(expression));
            assertThrows(IllegalArgumentException.class, () -> StudioTransferArguments.validateRow(expression));
            String original = "TRANSFER(LABEL(\"source\"),LABEL(\"dest\"),ITEM(\"*\")," + expression + ")";
            assertThrows(
                IllegalArgumentException.class,
                () -> BlueprintCompiler.validateClause(Type.TRANSFER, original));
            assertTrue(
                StudioNodeProperties.inspect(Type.TRANSFER, original)
                    .isEmpty());
            assertThrows(IllegalArgumentException.class, () -> new StudioTransferArguments(original));
            String four = "TRANSFER(LABEL(\"source\"),LABEL(\"dest\"),ITEM(\"*\"),1)";
            assertThrows(
                IllegalArgumentException.class,
                () -> StudioNodeProperties.update(Type.TRANSFER, four, "amount", expression));
            Node node = node(four);
            StudioFieldDraft draft = draft(node);
            draft.set("amount", expression);
            assertThrows(IllegalArgumentException.class, () -> apply(draft, node));
            assertEquals(four, node.clause());
        }
        String atBoundary = repeat("(", 32) + "1" + repeat(")", 32);
        StudioExpressionSyntax.transferExpression(atBoundary);
        StudioExpressionSyntax.expression(atBoundary);
        StudioExpressionSyntax.transfer("TRANSFER(" + atBoundary + "," + atBoundary + ")");
        assertTrue(
            assertThrows(IllegalArgumentException.class, () -> StudioExpressionSyntax.transferExpression("\n"))
                .getMessage()
                .contains("Transfer"));
        assertTrue(
            assertThrows(IllegalArgumentException.class, () -> StudioExpressionSyntax.expression("\n")).getMessage()
                .contains("Print"));
    }

    @Test
    void guardKeepsWideSiblingBudgetsLocalAndCompilerRetainsItsLeafBoundary() {
        String arithmetic = "(1" + repeat("+1", 29) + ")";
        StudioExpressionSyntax.transferExpression("[" + String.join(",", Collections.nCopies(32, arithmetic)) + "]");
        String label = "LABEL(\"source\")", dest = "LABEL(\"dest\")";
        String original = "TRANSFER(" + label + "," + dest + ",ITEM(\"*\")," + arithmetic + ")";
        Node node = node(original);
        assertEquals(original, apply(draft(node), node));
        Node four = node("TRANSFER(" + label + "," + dest + ",ITEM(\"*\"),1)");
        StudioFieldDraft draft = draft(four);
        draft.set("amount", repeat("(", 32) + "1" + repeat(")", 32));
        assertThrows(IllegalArgumentException.class, () -> apply(draft, four));
        draft.set("amount", repeat("(", 31) + "1" + repeat(")", 31));
        assertTrue(apply(draft, four).contains(repeat("(", 31)));
    }

    private static StudioFieldPicker.Candidate candidate(StudioFieldPicker.Snapshot snapshot, String value) {
        return snapshot.search(value).entries.stream()
            .filter(c -> c.value.equals(value))
            .findFirst()
            .get();
    }

    private static StudioFieldPicker.Session itemPicker(StudioFieldDraft draft, UUID item) {
        StudioFieldPicker.Target target = StudioFieldPicker.transferRowTarget(draft, item);
        assertNotNull(target);
        StudioFieldPicker.Snapshot snapshot = StudioFieldPicker
            .items(Arrays.asList("minecraft:coal", "minecraft:iron_ingot"), target);
        StudioFieldPicker.Session session = new StudioFieldPicker.Session(draft, target, snapshot);
        session.select(candidate(snapshot, "minecraft:iron_ingot"));
        return session;
    }

    @Test
    void newRolePickerWorksAfterAliasRetirementWithoutValidatingOtherPendingRoles() {
        Node node = node(TWO);
        StudioFieldDraft draft = draft(node);
        UUID item = draft.addTransferArgument("iTeM(  \"*\"  )");
        UUID amount = draft.addTransferArgument("");
        StudioFieldPicker.Session session = itemPicker(draft, item);
        assertTrue(session.choose(draft, PROJECT, node, draft.source));
        assertEquals("iTeM(  \"minecraft:iron_ingot\"  )", draft.transferArgumentValue(item));
        assertEquals("", draft.transferArgumentValue(amount));
        assertThrows(IllegalArgumentException.class, () -> apply(draft, node));
        draft.setTransferArgument(amount, "0");
        assertTrue(apply(draft, node).contains(", 0"));
        assertEquals(TWO, node.clause());
        assertFalse(itemPicker(draft, item).choose(draft, PROJECT, node, draft.source));
    }

    @Test
    void rolePickersRejectRemovedReaddedCopiedOrForeignOwnersEvenWithIdenticalText() {
        Node node = node(TWO);
        StudioFieldDraft draft = draft(node);
        UUID item = draft.addTransferArgument("ITEM(\"*\")");
        StudioFieldPicker.Session stale = itemPicker(draft, item);
        StudioFieldDraft copy = new StudioFieldDraft(draft);
        assertThrows(IllegalArgumentException.class, () -> stale.choose(copy, PROJECT, node, copy.source));
        StudioFieldPicker.Target target = StudioFieldPicker.transferRowTarget(draft, item);
        StudioFieldPicker.Snapshot snapshot = StudioFieldPicker
            .items(Collections.singletonList("minecraft:coal"), target);
        assertThrows(IllegalArgumentException.class, () -> new StudioFieldPicker.Session(copy, target, snapshot));
        draft.removeTransferArgument(item);
        UUID replacement = draft.addTransferArgument("ITEM(\"*\")");
        assertNotEquals(item, replacement);
        assertThrows(IllegalArgumentException.class, () -> stale.choose(draft, PROJECT, node, draft.source));
        assertTrue(itemPicker(draft, replacement).choose(draft, PROJECT, node, draft.source));
        StudioFieldPicker.Session current = itemPicker(draft, replacement);
        assertThrows(
            IllegalArgumentException.class,
            () -> current.choose(draft, UUID.randomUUID(), node, draft.source));
        assertThrows(IllegalArgumentException.class, () -> current.choose(draft, PROJECT, node, draft.source + " "));
        assertThrows(IllegalArgumentException.class, () -> current.choose(draft, PROJECT, node(TWO), draft.source));
    }

    @Test
    void rolePickerEligibilityStaysBareAndLabelsKeepTypedEscapingAfterStructureEdits() {
        Node node = node(TWO);
        StudioFieldDraft draft = draft(node);
        draft.addTransferArgument("ITEM(\"*\")");
        UUID source = row(draft, Role.SOURCE);
        draft.setTransferArgument(source, "LaBeL(  \"old\"  )");
        StudioFieldPicker.Target target = StudioFieldPicker.transferRowTarget(draft, source);
        assertNotNull(target);
        String name = "box, " + UNICODE + " \\\"";
        String literal = ca.teamdman.sfml.data.DataValue.string(name)
            .toString();
        StudioFieldPicker.Snapshot snapshot = StudioFieldPicker
            .labels(program("PRINT(LABEL(" + literal + "))"), target);
        StudioFieldPicker.Session session = new StudioFieldPicker.Session(draft, target, snapshot);
        session.select(candidate(snapshot, name));
        assertTrue(session.choose(draft, PROJECT, node, draft.source));
        assertEquals("LaBeL(  " + literal + "  )", draft.transferArgumentValue(source));
        draft.setTransferArgument(source, "LABEL(\"source\").slots(0,0)");
        assertNull(StudioFieldPicker.transferRowTarget(draft, source));
        UUID item = row(draft, Role.ITEM);
        draft.setTransferArgument(item, "");
        assertNull(StudioFieldPicker.transferRowTarget(draft, item));
        UUID amount = draft.addTransferArgument("1");
        assertNull(StudioFieldPicker.transferRowTarget(draft, amount));
    }

    @Test
    void legacyPickerCannotBypassRetiredAliasesAndNewPickerNoopKeepsFeedback() {
        Node node = node(TWO);
        StudioFieldDraft draft = draft(node);
        StudioNodeProperties.Field field = StudioNodeProperties.inspect(Type.TRANSFER, TWO)
            .get(0);
        StudioFieldPicker.Target target = StudioFieldPicker.target(Type.TRANSFER, field, field.getValue());
        StudioFieldPicker.Snapshot snapshot = StudioFieldPicker.labels(draft.source, target);
        StudioFieldPicker.Session old = new StudioFieldPicker.Session(draft, target, snapshot);
        old.select(candidate(snapshot, "dest"));
        draft.addTransferArgument("ITEM(\"minecraft:iron_ingot\")");
        assertThrows(IllegalArgumentException.class, () -> old.choose(draft, PROJECT, node, draft.source));
        StudioFieldFeedback feedback = new StudioFieldFeedback();
        feedback.open(draft, "");
        String footer = feedback.failedApply(draft, "old", "");
        StudioFieldFeedback.Snapshot previous = feedback.snapshot(draft);
        UUID item = row(draft, Role.ITEM);
        assertFalse(itemPicker(draft, item).choose(draft, PROJECT, node, draft.source));
        assertTrue(feedback.current(previous, draft));
        UUID amount = draft.addTransferArgument("");
        footer = feedback.changed(draft, footer);
        assertFalse(feedback.current(previous, draft));
        footer = feedback.failedApply(draft, "blank", footer);
        draft.setTransferArgument(amount, "0");
        feedback.changed(draft, footer);
        assertNull(feedback.snapshot(draft));
        feedback.failedApply(draft, "last", footer);
        draft.removeTransferArgument(amount);
        feedback.changed(draft, footer);
        assertNull(feedback.snapshot(draft));
    }

    @Test
    void projectSourceTypeAndOriginalClauseFencesRejectAtomicUpdater() {
        Node node = node(TWO);
        StudioFieldDraft draft = draft(node);
        draft.addTransferArgument("ITEM(\"*\")");
        assertThrows(IllegalArgumentException.class, () -> draft.appliedClause(UUID.randomUUID(), node, draft.source));
        assertThrows(IllegalArgumentException.class, () -> draft.appliedClause(PROJECT, node, draft.source + " "));
        assertThrows(IllegalArgumentException.class, () -> draft.appliedClause(PROJECT, node(TWO), draft.source));
        assertThrows(
            IllegalArgumentException.class,
            () -> draft.appliedClause(PROJECT, new Node(node.id, Type.PRINT, 0, 0, TWO), draft.source));
        node.setClause("TRANSFER(LABEL(\"new\"),LABEL(\"dest\"))");
        assertThrows(IllegalArgumentException.class, () -> apply(draft, node));
        assertThrows(
            IllegalArgumentException.class,
            () -> StudioNodeProperties.updateTransferArguments(Type.PRINT, TWO, new StudioTransferArguments(TWO)));
        assertThrows(IllegalArgumentException.class, () -> new StudioTransferArguments(TWO).applyTo(TWO + " "));
    }

    private static List<String> identities(BlueprintGraph graph) {
        List<String> result = new ArrayList<>();
        for (Node node : graph.nodes()) result.add(node.id + ":" + node.type + ":" + node.hasElse);
        for (BlueprintGraph.Edge edge : graph.edges()) result.add(edge.from + ":" + edge.pin + ":" + edge.to);
        return result;
    }

    @Test
    void allPendingRolesApplyAsOneHistoryActionWithExactUndoAndBranchIds() {
        String source = "-- " + UNICODE
            + "\r\nevery 20 ticks do if TRUE then "
            + TWO
            + " else PRINT(\"other\") end end";
        StudioDocument document = new StudioDocument();
        document.load(source);
        document.openVisual();
        Node node = document.graph()
            .nodes()
            .stream()
            .filter(n -> n.type == Type.TRANSFER)
            .findFirst()
            .get();
        StudioFieldDraft draft = new StudioFieldDraft(
            PROJECT,
            node,
            document.code(),
            StudioNodeProperties.inspect(node.type, node.clause()));
        List<String> ids = identities(document.graph());
        draft.addTransferArgument("ITEM(\"*\")");
        draft.addTransferArgument("3");
        assertEquals(source, document.source());
        assertFalse(document.undo());
        String clause = draft.appliedClause(PROJECT, node, document.code());
        document.editGraph(
            g -> g.node(node.id)
                .setClause(clause));
        String after = document.source();
        assertEquals(ids, identities(document.graph()));
        assertTrue(document.undo());
        assertEquals(source, document.source());
        assertFalse(document.undo());
        assertTrue(document.redo());
        assertEquals(after, document.source());
        assertEquals(ids, identities(document.graph()));
    }

    private static ItemStack stack(Item item, int count, int metadata, String tag) {
        ItemStack stack = new ItemStack(item, count, metadata);
        if (tag != null) {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("name", tag);
            stack.setTagCompound(nbt);
        }
        return stack;
    }

    private static AuditRegressionTest.Fixture stock() throws Exception {
        AuditRegressionTest.Fixture fixture = new AuditRegressionTest.Fixture();
        fixture.source.setInventorySlotContents(0, stack(ITEM, 7, 0, null));
        fixture.source.setInventorySlotContents(1, stack(OTHER, 5, 0, null));
        return fixture;
    }

    private static ProgramContext tick(AuditRegressionTest.Fixture fixture, String source) throws Exception {
        TileEntityManager manager = RecoveryFixtures.manager(fixture);
        ProgramContext context = new ProgramContext(
            fixture.world,
            manager,
            new CableNetwork(fixture.world, AuditRegressionTest.MANAGER),
            fixture.labels,
            20,
            false);
        ProgramBuildResult built = new ProgramBuilder(source).buildWithSyntax();
        assertTrue(built.isSuccess(), built.getErrorMessage());
        built.getProgram()
            .tick(context);
        assertFalse(manager.hasPendingRecovery());
        return context;
    }

    @Test
    void actualProgramTickDistinguishesAllItemsFilteredAutomaticQuotaAndZero() throws Exception {
        String[] optional = { "", ",ITEM(\"sfm_studio_transfer:item\")", ",ITEM(\"sfm_studio_transfer:item\"),3",
            ",ITEM(\"*\"),0" };
        int[] moved = { 12, 7, 3, 0 };
        for (int i = 0; i < optional.length; i++) {
            AuditRegressionTest.Fixture fixture = stock();
            String clause = "TRANSFER(LABEL(\"source\"),LABEL(\"dest\")" + optional[i] + ")";
            Node node = node(clause);
            tick(fixture, program(apply(draft(node), node)));
            assertEquals(moved[i], AuditRegressionTest.count(fixture.dest));
            assertEquals(12 - moved[i], AuditRegressionTest.count(fixture.source));
        }
    }

    @Test
    void noTransferEffectsBeforeApplyAndUndoRedoRestoreActualRuntimeBehavior() throws Exception {
        StudioDocument document = new StudioDocument();
        document.load(program(TWO));
        document.openVisual();
        Node node = document.graph()
            .nodes()
            .stream()
            .filter(n -> n.type == Type.TRANSFER)
            .findFirst()
            .get();
        StudioFieldDraft draft = new StudioFieldDraft(
            PROJECT,
            node,
            document.code(),
            StudioNodeProperties.inspect(node.type, node.clause()));
        UUID item = draft.addTransferArgument("");
        draft.addTransferArgument("0");
        AuditRegressionTest.Fixture original = stock();
        assertThrows(IllegalArgumentException.class, () -> draft.appliedClause(PROJECT, node, document.code()));
        assertEquals(12, AuditRegressionTest.count(original.source));
        assertEquals(0, AuditRegressionTest.count(original.dest));
        tick(original, document.source());
        assertEquals(12, AuditRegressionTest.count(original.dest));
        draft.setTransferArgument(item, "ITEM(\"*\")");
        String clause = draft.appliedClause(PROJECT, node, document.code());
        document.editGraph(
            g -> g.node(node.id)
                .setClause(clause));
        AuditRegressionTest.Fixture zero = stock();
        tick(zero, document.source());
        assertEquals(0, AuditRegressionTest.count(zero.dest));
        assertTrue(document.undo());
        AuditRegressionTest.Fixture undo = stock();
        tick(undo, document.source());
        assertEquals(12, AuditRegressionTest.count(undo.dest));
        assertTrue(document.redo());
        AuditRegressionTest.Fixture redo = stock();
        tick(redo, document.source());
        assertEquals(0, AuditRegressionTest.count(redo.dest));
    }

    @Test
    void actualProgramTickMatchesMetadataAndNbtFromAnItemKeyExpression() throws Exception {
        AuditRegressionTest.Fixture fixture = new AuditRegressionTest.Fixture();
        fixture.source.setInventorySlotContents(0, stack(ITEM, 2, 7, "different"));
        fixture.source.setInventorySlotContents(1, stack(ITEM, 3, 8, "same"));
        fixture.source.setInventorySlotContents(2, stack(ITEM, 7, 7, "same"));
        AuditRegressionTest.InventoryTile template = new AuditRegressionTest.InventoryTile(1);
        template.setInventorySlotContents(0, stack(ITEM, 1, 7, "same"));
        fixture.add("template", AuditRegressionTest.EXTRA, template);
        Node node = node("TRANSFER(LABEL(\"source\"),LABEL(\"dest\"))");
        StudioFieldDraft draft = draft(node);
        draft.addTransferArgument("LABEL(\"template\").contents()[0].item");
        draft.addTransferArgument("5");
        tick(fixture, program(apply(draft, node)));
        assertEquals(5, AuditRegressionTest.count(fixture.dest));
        assertEquals(7, AuditRegressionTest.count(fixture.source));
        assertEquals(
            7,
            fixture.dest.getStackInSlot(0)
                .getItemDamage());
        assertEquals(
            "same",
            fixture.dest.getStackInSlot(0)
                .getTagCompound()
                .getString("name"));
        assertEquals(2, fixture.source.getStackInSlot(0).stackSize);
        assertEquals(3, fixture.source.getStackInSlot(1).stackSize);
        assertEquals(2, fixture.source.getStackInSlot(2).stackSize);
    }

    private static final class Sided extends AuditRegressionTest.InventoryTile implements ISidedInventory {

        final int side;
        int checks;

        Sided(int side) {
            super(2);
            this.side = side;
        }

        public int[] getAccessibleSlotsFromSide(int side) {
            return new int[] { 0, 1 };
        }

        public boolean canExtractItem(int slot, ItemStack stack, int side) {
            checks++;
            return side == this.side;
        }

        public boolean canInsertItem(int slot, ItemStack stack, int side) {
            checks++;
            return side == this.side;
        }
    }

    @Test
    void actualProgramTickHonorsSidedAndSlotRestrictionsAfterSuffixEdits() throws Exception {
        AuditRegressionTest.Fixture fixture = new AuditRegressionTest.Fixture();
        Sided source = new Sided(ForgeDirection.UP.ordinal()), dest = new Sided(ForgeDirection.DOWN.ordinal());
        source.setInventorySlotContents(0, stack(ITEM, 9, 0, null));
        source.setInventorySlotContents(1, stack(ITEM, 8, 0, null));
        fixture.world.tiles.put(AuditRegressionTest.SRC, source);
        fixture.world.tiles.put(AuditRegressionTest.DST, dest);
        Node node = node(
            "TRANSFER(LABEL(\"source\").side(\"up\").slots(1,1),LABEL(\"dest\").side(\"down\").slots(1,1))");
        StudioFieldDraft draft = draft(node);
        draft.addTransferArgument("ITEM(\"*\")");
        draft.addTransferArgument("3");
        tick(fixture, program(apply(draft, node)));
        assertEquals(14, AuditRegressionTest.count(source));
        assertEquals(3, AuditRegressionTest.count(dest));
        assertEquals(9, source.getStackInSlot(0).stackSize);
        assertNull(dest.getStackInSlot(0));
        assertTrue(source.checks > 0 && dest.checks > 0);
    }
}
