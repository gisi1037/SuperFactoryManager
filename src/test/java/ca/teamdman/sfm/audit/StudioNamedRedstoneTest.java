package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

import net.minecraftforge.common.util.ForgeDirection;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.junit.jupiter.api.Test;

import ca.teamdman.langs.SFMLLexer;
import ca.teamdman.langs.SFMLParser;
import ca.teamdman.sfm.client.blueprint.BlueprintCompiler;
import ca.teamdman.sfm.client.blueprint.BlueprintDocument;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Pin;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Type;
import ca.teamdman.sfm.client.blueprint.StudioDocument;
import ca.teamdman.sfm.client.blueprint.StudioFieldDraft;
import ca.teamdman.sfm.client.blueprint.StudioFieldFeedback;
import ca.teamdman.sfm.client.blueprint.StudioFieldPicker;
import ca.teamdman.sfm.client.blueprint.StudioNamedRedstoneHeader;
import ca.teamdman.sfm.client.blueprint.StudioNodeProperties;
import ca.teamdman.sfm.client.blueprint.StudioSourceMap;
import ca.teamdman.sfm.client.gui.mui2.StudioLayout;
import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.program.RedstoneTriggerTracker;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfm.common.tileentity.TileEntityRedstonePort;
import ca.teamdman.sfml.ast.NamedRedstoneTrigger;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;
import ca.teamdman.sfml.program_builder.VisualCapabilityReport.Kind;

class StudioNamedRedstoneTest {

    private static List<String> tokens(String source) {
        List<String> tokens = new ArrayList<>();
        for (Token token : new SFMLLexer(CharStreams.fromString(source)).getAllTokens())
            if (token.getChannel() == Token.DEFAULT_CHANNEL) tokens.add(token.getType() + ":" + token.getText());
        return tokens;
    }

    private static final UUID PROJECT = new UUID(21, 1037);
    private static final String HEADER = "eVeRy ReDsToNe PuLsE FROM \"signal\" North SIDE";
    private static final String UNICODE = "\u03c0\uD83C\uDFED";

    private static String body(String header, String body) {
        return header + " DO " + body + " END";
    }

    private static String manualHeader() {
        return "eVeRy ReDsToNe PuLsE FROM " + String.join(", ", Collections.nCopies(33, "signal")) + " North SIDE";
    }

    private static Node node(String header) {
        return new Node(UUID.randomUUID(), Type.NAMED_REDSTONE, 0, 0, header);
    }

    private static StudioFieldDraft draft(Node node) {
        return new StudioFieldDraft(
            PROJECT,
            node,
            body(node.clause(), "PRINT(\"body\")"),
            StudioNodeProperties.inspect(node.type, node.clause()));
    }

    private static ProgramBuildResult build(String source) {
        ProgramBuildResult result = new ProgramBuilder(source).buildWithSyntax();
        assertTrue(result.isSuccess(), result.getErrorMessage());
        return result;
    }

    @Test
    void newTypeAppendsWithoutChangingOldTypeNamesOrderDefaultsOrPulseSemantics() {
        String[] old = { "TIMER", "PULSE", "INPUT", "OUTPUT", "IF", "FORGET", "REDSTONE", "TRANSFER", "PRINT" };
        for (int i = 0; i < old.length; i++) assertEquals(old[i], Type.values()[i].name());
        assertEquals(Type.NAMED_REDSTONE, Type.values()[9]);
        assertEquals("Port edge", Type.NAMED_REDSTONE.title);
        assertTrue(Type.NAMED_REDSTONE.isTrigger());
        assertTrue(Type.PULSE.isTrigger());
        assertFalse(Type.REDSTONE.isTrigger());
        assertEquals("EVERY REDSTONE PULSE", Type.PULSE.defaultClause);
        for (Pin pin : Pin.values()) assertEquals(pin == Pin.NEXT, Type.NAMED_REDSTONE.hasPin(pin));
        for (Type type : Type.values()) BlueprintCompiler.validateClause(type, type.defaultClause);
    }

    @Test
    void sharedHelpersClassifyEveryActualTriggerAndRejectUnknownTypes() {
        ProgramBuildResult built = build("EVERY 20 TICKS DO END EVERY REDSTONE PULSE DO END " + body(HEADER, "FORGET"));
        Type[] expected = { Type.TIMER, Type.PULSE, Type.NAMED_REDSTONE };
        for (int i = 0; i < 3; i++) {
            SFMLParser.TriggerContext trigger = built.getSyntax()
                .trigger(i);
            assertEquals(expected[i], BlueprintCompiler.triggerType(trigger));
            assertEquals(
                "DO",
                BlueprintCompiler.triggerDo(trigger)
                    .getText());
            assertEquals(
                i == 2 ? 1 : 0,
                BlueprintCompiler.triggerBlock(trigger)
                    .statement()
                    .size());
        }
        assertThrows(IllegalArgumentException.class, () -> BlueprintCompiler.triggerType(null));
        assertThrows(
            IllegalArgumentException.class,
            () -> BlueprintCompiler.triggerDo(new SFMLParser.TriggerContext(null, 0)));
        assertThrows(IllegalArgumentException.class, () -> BlueprintCompiler.triggerBlock(null));
    }

    @Test
    void namedHeadersImportGenerateAndValidateOnlyTheirOwnTriggerKind() {
        for (String edge : Arrays.asList("PULSE", "RISING", "FALLING"))
            for (String sides : Arrays.asList("", " North, East SIDE", " EACH SIDE", " TOP, BOTTOM SIDE")) {
                String header = "every redstone " + edge + " from \"port, " + UNICODE + "\",spare" + sides;
                BlueprintCompiler.validateClause(Type.NAMED_REDSTONE, header);
                String source = body(header, "PRINT(\"ok\")");
                BlueprintGraph graph = BlueprintCompiler.importText(source);
                assertEquals(
                    Type.NAMED_REDSTONE,
                    graph.triggers()
                        .get(0).type);
                assertEquals(tokens(source), tokens(BlueprintCompiler.generate(graph)));
                assertThrows(
                    IllegalArgumentException.class,
                    () -> BlueprintCompiler.validateClause(Type.PULSE, header));
                assertThrows(
                    IllegalArgumentException.class,
                    () -> BlueprintCompiler.validateClause(Type.TIMER, header));
            }
        assertThrows(
            IllegalArgumentException.class,
            () -> BlueprintCompiler.validateClause(Type.NAMED_REDSTONE, "EVERY REDSTONE PULSE"));
        assertThrows(
            IllegalArgumentException.class,
            () -> BlueprintCompiler.validateClause(Type.NAMED_REDSTONE, "EVERY 20 TICKS"));
    }

    @Test
    void headerGuardRefusesBlocksStatementsExpressionsCommentsControlsAndWrongKindsBeforeCompilation() {
        String huge = String.join("", Collections.nCopies(4097, "x"));
        for (String header : Arrays.asList(
            "",
            " ",
            HEADER + " DO END",
            HEADER + " DO IF " + String.join("", Collections.nCopies(2000, "NOT ")) + "TRUE THEN END END",
            HEADER + " END EVERY 20 TICKS",
            HEADER + " --hidden",
            HEADER + "\n",
            HEADER + "\r",
            HEADER + "\0",
            "EVERY 20 TICKS",
            "EVERY REDSTONE RISING FROM LABEL(\"signal\")",
            "EVERY REDSTONE RISING FROM [signal]",
            "EVERY REDSTONE RISING FROM {signal}",
            huge)) {
            assertThrows(IllegalArgumentException.class, () -> StudioNamedRedstoneHeader.parse(header));
            assertThrows(
                IllegalArgumentException.class,
                () -> BlueprintCompiler.validateClause(Type.NAMED_REDSTONE, header));
        }
        BlueprintCompiler
            .validateClause(Type.NAMED_REDSTONE, "EVERY REDSTONE FALLING FROM \"DO END ([]) -- " + UNICODE + "\"");
    }

    @Test
    void compilerStillRejectsRoundRobinSlotsAndRelativeOrUnknownSides() {
        for (String suffix : Arrays
            .asList(" ROUND ROBIN BY LABEL", " SLOTS 0-1", " FRONT SIDE", " LEFT SIDE", " NULL SIDE")) {
            String header = "EVERY REDSTONE RISING FROM signal" + suffix;
            assertThrows(
                IllegalArgumentException.class,
                () -> BlueprintCompiler.validateClause(Type.NAMED_REDSTONE, header));
            Node node = node(header);
            StudioFieldDraft draft = draft(node);
            assertEquals(header, draft.original);
            assertTrue(
                StudioNodeProperties.inspect(Type.NAMED_REDSTONE, header)
                    .isEmpty());
        }
    }

    @Test
    void supportedWrapperDoesNotHideUnsupportedBodiesAndNeverExposesPartialGraphs() {
        String[] bodies = { "LET $x = 1 PRINT($x)", "TRANSFER(LABEL(\"source\"),LABEL(\"dest\")) INTO $result",
            "FOR $i = 1 TO 2 DO PRINT($i) END", "WHILE FALSE DO FORGET END",
            "IF TRUE THEN FORGET ELSE IF FALSE THEN FORGET END", "PRINT(LEN([1,2]))" };
        Kind[] kinds = { Kind.LOCAL_DECLARATION, Kind.TRANSFER_STATEMENT, Kind.LOOP_STATEMENT, Kind.LOOP_STATEMENT,
            Kind.ELSE_IF, Kind.FUNCTION_CALL };
        for (int i = 0; i < bodies.length; i++) {
            String source = body(HEADER, bodies[i]);
            build(source);
            BlueprintCompiler.ImportResult result = BlueprintCompiler.inspectText(source);
            assertNull(result.graph);
            final Kind kind = kinds[i];
            assertTrue(
                result.report.getBlockers()
                    .stream()
                    .anyMatch(b -> b.kind == kind));
            StudioDocument document = new StudioDocument();
            document.load(source);
            assertThrows(IllegalArgumentException.class, document::openVisual);
            assertEquals(source, document.source());
            assertNull(document.graph());
        }
        String functions = "FUNCTION f() RETURNS INTEGER DO RETURN 1 END " + body(HEADER, "PRINT(f())");
        assertFalse(BlueprintCompiler.inspectText(functions).report.isAvailable());
    }

    @Test
    void manualHeaderFieldPreservesNoopAliasCaseAndAtomicInvalidPendingText() {
        String header = manualHeader();
        Node node = node(header);
        StudioFieldDraft draft = draft(node);
        assertEquals(
            1,
            StudioNodeProperties.inspect(Type.NAMED_REDSTONE, header)
                .size());
        assertEquals(
            "header",
            StudioNodeProperties.inspect(Type.NAMED_REDSTONE, header)
                .get(0)
                .getKey());
        assertEquals(
            StudioNodeProperties.FieldType.TRIGGER_HEADER,
            StudioNodeProperties.inspect(Type.NAMED_REDSTONE, header)
                .get(0)
                .getType());
        assertFalse(draft.set("header", header));
        assertEquals(header, draft.appliedClause(PROJECT, node, draft.source));
        draft.set("header", header + " DO FORGET END");
        assertThrows(IllegalArgumentException.class, () -> draft.appliedClause(PROJECT, node, draft.source));
        StudioFieldDraft copy = new StudioFieldDraft(draft);
        assertEquals(header + " DO FORGET END", copy.value("header"));
        String edited = "every redstone falling from \"new, " + UNICODE + "\" south side";
        copy.set("header", edited);
        assertEquals(edited, copy.appliedClause(PROJECT, node, copy.source));
        assertEquals(header, node.clause());
        assertEquals(header + " DO FORGET END", draft.value("header"));
        assertThrows(IllegalArgumentException.class, () -> copy.appliedClause(UUID.randomUUID(), node, copy.source));
        assertThrows(IllegalArgumentException.class, () -> copy.appliedClause(PROJECT, node, copy.source + " "));
        assertThrows(IllegalArgumentException.class, () -> copy.appliedClause(PROJECT, node(header), copy.source));
    }

    @Test
    void manualHeaderEditsUseExistingFeedbackAndDoNotValidateWhileTyping() {
        String header = manualHeader();
        Node node = node(header);
        StudioFieldDraft draft = draft(node);
        StudioFieldFeedback feedback = new StudioFieldFeedback();
        feedback.open(draft, "");
        String footer = feedback.failedApply(draft, "last", "");
        StudioFieldFeedback.Snapshot previous = feedback.snapshot(draft);
        assertFalse(draft.set("header", header));
        assertTrue(feedback.current(previous, draft));
        assertTrue(draft.set("header", "invalid pending header"));
        feedback.changed(draft, footer);
        assertFalse(feedback.current(previous, draft));
        assertEquals("invalid pending header", draft.value("header"));
        assertEquals(header, node.clause());
    }

    private static List<String> identities(BlueprintGraph graph) {
        List<String> ids = new ArrayList<>();
        for (Node node : graph.nodes()) ids.add(node.id + ":" + node.type + ":" + node.hasElse);
        for (BlueprintGraph.Edge edge : graph.edges()) ids.add(edge.from + ":" + edge.pin + ":" + edge.to);
        return ids;
    }

    @Test
    void headerEditIsOneHistoryActionAndNoopVisualOpenKeepsExactSourceAndLayoutIds() {
        String source = "-- " + UNICODE
            + "\r\n"
            + body(HEADER, "IF TRUE THEN PRINT(\"yes\") ELSE PRINT(\"no\") END")
            + "\r\nEVERY 20 TICKS DO FORGET END";
        StudioDocument document = new StudioDocument();
        document.load(source);
        document.openVisual();
        assertEquals(source, document.source());
        Node root = document.graph()
            .triggers()
            .get(0);
        List<String> ids = identities(document.graph());
        StudioFieldDraft draft = new StudioFieldDraft(
            PROJECT,
            root,
            document.code(),
            StudioNodeProperties.inspect(root.type, root.clause()));
        draft.set("edge", "FALLING");
        draft.set("label", "spare");
        assertFalse(document.undo());
        String clause = draft.appliedClause(PROJECT, root, document.code());
        document.editGraph(
            g -> g.node(root.id)
                .setClause(clause));
        String after = document.source();
        assertEquals(ids, identities(document.graph()));
        assertTrue(document.undo());
        assertEquals(source, document.source());
        assertFalse(document.undo());
        assertTrue(document.redo());
        assertEquals(after, document.source());
        assertEquals(ids, identities(document.graph()));
        BlueprintDocument reopened = BlueprintDocument.open(after);
        assertTrue(reopened.restoredLayout);
        assertEquals(ids, identities(reopened.graph));
    }

    @Test
    void duplicateNamedHeadersAndBodiesMapByIdentityAcrossUnicodeAndCrLf() {
        String header = "EVERY REDSTONE RISING FROM \"" + UNICODE + "\" NORTH SIDE";
        String source = "-- " + UNICODE
            + "\r\n"
            + body(header, "PRINT(\"same\")")
            + "\r\n"
            + body(header, "PRINT(\"same\")");
        BlueprintGraph graph = BlueprintCompiler.importText(source);
        String display = StudioSourceMap.normalize(source);
        StudioSourceMap map = StudioSourceMap.build(PROJECT, source, display, graph);
        Node first = graph.triggers()
            .get(0),
            second = graph.triggers()
                .get(1);
        assertEquals(first.id, map.atCaret(display.indexOf("EVERY")).node);
        assertEquals(second.id, map.atCaret(display.lastIndexOf("EVERY")).node);
        StudioSourceMap.Span span = map.atCaret(display.lastIndexOf("EVERY"));
        assertEquals(header + " DO", source.substring(span.sourceStart, span.sourceEnd));
        assertEquals(header + " DO", display.substring(span.displayStart, span.displayEnd));
        assertEquals(graph.next(first.id, Pin.NEXT).id, map.atCaret(display.indexOf("PRINT")).node);
        assertEquals(graph.next(second.id, Pin.NEXT).id, map.atCaret(display.lastIndexOf("PRINT")).node);
        assertTrue(map.matches(PROJECT, source, display, graph));
        assertFalse(map.matches(UUID.randomUUID(), source, display, graph));
        graph.node(first.id)
            .setClause("EVERY REDSTONE FALLING FROM changed");
        assertFalse(map.matches(PROJECT, source, display, graph));
    }

    @Test
    void sourceReentryMatchesOnlyCurrentHeaderAndOldPulseTypeCannotMasqueradeAsNamedRoot() {
        String source = body(HEADER, "PRINT(1)");
        BlueprintDocument imported = BlueprintDocument.open(source);
        Node root = imported.graph.triggers()
            .get(0);
        root.setPosition(317, 219);
        String saved = imported.saveEdited(imported.graph);
        String changed = BlueprintDocument.prepareTextSave(
            BlueprintDocument.withoutMetadata(saved)
                .replace("PuLsE", "FALLING"),
            saved).source;
        BlueprintDocument current = BlueprintDocument.open(changed);
        assertEquals(
            Type.NAMED_REDSTONE,
            current.graph.triggers()
                .get(0).type);
        assertTrue(
            current.graph.triggers()
                .get(0)
                .clause()
                .contains("FALLING"));
        BlueprintGraph wrong = new BlueprintGraph();
        Node pulse = new Node(Type.PULSE, 0, 0);
        wrong.add(pulse);
        pulse.setClause(HEADER);
        assertThrows(IllegalArgumentException.class, () -> BlueprintCompiler.generate(wrong));
    }

    private static void uuid(DataOutputStream data, UUID id) throws Exception {
        data.writeLong(id.getMostSignificantBits());
        data.writeLong(id.getLeastSignificantBits());
    }

    @Test
    void existingV1AndV2LayoutsRetainOldTimerAndPulseTypeIdentities() throws Exception {
        BlueprintGraph graph = BlueprintCompiler
            .importText("EVERY 20 TICKS DO FORGET END EVERY REDSTONE PULSE DO PRINT(1) END");
        List<String> ids = identities(graph);
        String code = BlueprintCompiler.generate(graph);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream data = new DataOutputStream(bytes)) {
            data.writeInt(1);
            data.write(ca.teamdman.sfm.common.util.ProgramRevision.digest(code));
            data.writeUTF(graph.nameClause());
            data.writeUTF(graph.comments());
            data.writeInt(
                graph.nodes()
                    .size());
            for (Node node : graph.nodes()) {
                uuid(data, node.id);
                data.writeUTF(node.type.name());
                data.writeDouble(node.x());
                data.writeDouble(node.y());
                data.writeUTF(node.clause());
                data.writeBoolean(node.hasElse);
            }
            data.writeInt(
                graph.edges()
                    .size());
            for (BlueprintGraph.Edge edge : graph.edges()) {
                uuid(data, edge.from);
                data.writeUTF(edge.pin.name());
                uuid(data, edge.to);
            }
        }
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        try (GZIPOutputStream zip = new GZIPOutputStream(compressed)) {
            zip.write(bytes.toByteArray());
        }
        BlueprintDocument old = BlueprintDocument.open(
            code + "\n"
                + BlueprintDocument.LEGACY_MARKER
                + Base64.getEncoder()
                    .encodeToString(compressed.toByteArray()));
        assertTrue(old.restoredLayout);
        assertEquals(ids, identities(old.graph));
        BlueprintDocument current = BlueprintDocument.open(BlueprintDocument.save(graph));
        assertTrue(current.restoredLayout);
        assertEquals(ids, identities(current.graph));
    }

    @Test
    void namedRootsUseExistingConnectionLimitsAndPaletteGeometry() {
        BlueprintGraph graph = new BlueprintGraph();
        Node root = new Node(Type.NAMED_REDSTONE, 0, 0), other = new Node(Type.PULSE, 200, 0),
            print = new Node(Type.PRINT, 0, 80);
        graph.add(root);
        graph.add(other);
        graph.add(print);
        graph.connect(root.id, Pin.NEXT, print.id);
        assertThrows(IllegalArgumentException.class, () -> graph.connect(print.id, Pin.NEXT, root.id));
        assertThrows(IllegalArgumentException.class, () -> graph.connect(root.id, Pin.TRUE, print.id));
        assertTrue(
            BlueprintCompiler.generate(graph)
                .contains("EVERY REDSTONE RISING FROM signal"));
        StudioLayout.NodePalette small = new StudioLayout.NodePalette(320, 240),
            independent = new StudioLayout.NodePalette(1280, 960);
        for (StudioLayout.NodePalette layout : Arrays.asList(small, independent)) {
            assertTrue(26 + layout.listHeight < layout.footerY);
            assertTrue(layout.footerY + 20 <= layout.height - 8);
            assertTrue(Type.values().length * 21 > layout.listHeight);
        }
    }

    private static final class CounterPort extends TileEntityRedstonePort {

        final long[] rising = new long[6], falling = new long[6];
        final int[] levels = new int[6];

        public boolean isInputInitialized() {
            return true;
        }

        public long getSignalEpoch() {
            return 1;
        }

        public long getRisingCount(int side) {
            return rising[side];
        }

        public long getFallingCount(int side) {
            return falling[side];
        }

        public boolean isInputAvailable(int side) {
            return true;
        }

        public int getInputLevel(int side) {
            return levels[side];
        }

        public void refreshInputs(Runnable before) {
            before.run();
        }

        public boolean setOutput(List<ForgeDirection> sides, int level) {
            boolean changed = false;
            for (ForgeDirection side : sides) {
                int i = side.ordinal();
                if (levels[i] == level) continue;
                if (levels[i] == 0 && level > 0) rising[i]++;
                if (levels[i] > 0 && level == 0) falling[i]++;
                levels[i] = level;
                changed = true;
            }
            return changed;
        }
    }

    private static final class Rig {

        final AuditRegressionTest.Fixture fixture = new AuditRegressionTest.Fixture();
        final CableNetwork network = new CableNetwork(fixture.world, AuditRegressionTest.MANAGER);
        final RedstoneTriggerTracker tracker = new RedstoneTriggerTracker();
        final TileEntityManager manager = new TileEntityManager();
        final CounterPort first = new CounterPort(), second = new CounterPort();
        final Program program;

        Rig(String source) throws Exception {
            program = build(source).getProgram();
            fixture.add("signal", AuditRegressionTest.SRC, first);
            fixture.add("spare", AuditRegressionTest.DST, second);
            manager.getDebugConsole()
                .setEnabled(true);
        }

        List<String> tick(boolean pulse) {
            int before = manager.getDebugConsole()
                .snapshot()
                .size();
            ProgramContext context = new ProgramContext(fixture.world, manager, network, fixture.labels, 20, pulse);
            context.setRedstoneTracker(tracker);
            program.tick(context);
            return new ArrayList<>(
                manager.getDebugConsole()
                    .snapshot()
                    .subList(
                        before,
                        manager.getDebugConsole()
                            .snapshot()
                            .size()));
        }
    }

    @Test
    void actualTickTracePreservesBaselineRisingAliasFallingAndShortPulsesAfterRoundTrip() throws Exception {
        String source = body("EVERY REDSTONE PULSE FROM signal NORTH SIDE", "PRINT(\"up\")") + " "
            + body("EVERY REDSTONE FALLING FROM signal NORTH SIDE", "PRINT(\"down\")");
        for (String candidate : Arrays
            .asList(source, BlueprintCompiler.generate(BlueprintCompiler.importText(source)))) {
            Rig rig = new Rig(candidate);
            rig.first.rising[2] = 8;
            rig.first.levels[2] = 15;
            assertTrue(
                rig.tick(false)
                    .isEmpty());
            rig.first.rising[2] += 3;
            rig.first.falling[2] += 3;
            assertEquals(Arrays.asList("up", "down"), rig.tick(false));
            assertTrue(
                rig.tick(false)
                    .isEmpty());
        }
    }

    @Test
    void actualGroupedPortsCoalesceEdgesOnceAndOnlySelectedFacesCount() throws Exception {
        String source = body("EVERY REDSTONE RISING FROM signal,signal,spare NORTH SIDE", "PRINT(\"group\")");
        for (String candidate : Arrays
            .asList(source, BlueprintCompiler.generate(BlueprintCompiler.importText(source)))) {
            Rig rig = new Rig(candidate);
            assertTrue(
                rig.tick(false)
                    .isEmpty());
            rig.first.rising[5]++;
            assertTrue(
                rig.tick(false)
                    .isEmpty());
            rig.first.rising[2] += 3;
            rig.second.rising[2] += 5;
            assertEquals(Collections.singletonList("group"), rig.tick(false));
            assertTrue(
                rig.tick(false)
                    .isEmpty());
        }
    }

    @Test
    void actualMixedTriggerOrderAndOldManagerPulseRemainDistinct() throws Exception {
        String source = body("EVERY REDSTONE RISING FROM signal NORTH SIDE", "PRINT(\"r\")")
            + " EVERY 20 TICKS DO PRINT(\"timer\") END EVERY REDSTONE PULSE DO PRINT(\"manager\") END "
            + body("EVERY REDSTONE FALLING FROM signal NORTH SIDE", "PRINT(\"f\")");
        for (String candidate : Arrays
            .asList(source, BlueprintCompiler.generate(BlueprintCompiler.importText(source)))) {
            Rig rig = new Rig(candidate);
            assertEquals(Collections.singletonList("timer"), rig.tick(false));
            rig.first.rising[2]++;
            assertEquals(Arrays.asList("r", "timer", "manager"), rig.tick(true));
            rig.first.falling[2]++;
            assertEquals(Arrays.asList("timer", "f"), rig.tick(false));
        }
    }

    @Test
    void actualEligibilitySnapshotPrecedesBodiesIncludingSignalOutputs() throws Exception {
        String source = "EVERY 20 TICKS DO OUTPUT 15 REDSTONE LEVEL TO signal NORTH SIDE END "
            + body("EVERY REDSTONE RISING FROM signal NORTH SIDE", "PRINT(\"next\")");
        for (String candidate : Arrays
            .asList(source, BlueprintCompiler.generate(BlueprintCompiler.importText(source)))) {
            Rig rig = new Rig(candidate);
            assertTrue(
                rig.tick(false)
                    .isEmpty());
            assertEquals(Collections.singletonList("next"), rig.tick(false));
            assertTrue(
                rig.tick(false)
                    .isEmpty());
        }
    }

    @Test
    void pendingHeaderAndUndoRedoProduceOnlyTheirCurrentActualRuntimeTrace() throws Exception {
        StudioDocument document = new StudioDocument();
        String source = body("EVERY REDSTONE RISING FROM signal NORTH SIDE", "PRINT(\"edge\")");
        document.load(source);
        document.openVisual();
        Node root = document.graph()
            .triggers()
            .get(0);
        StudioFieldDraft draft = new StudioFieldDraft(
            PROJECT,
            root,
            document.code(),
            StudioNodeProperties.inspect(root.type, root.clause()));
        draft.set("edge", "FALLING");
        assertEquals(source, document.source());
        assertFalse(document.undo());
        Rig pending = new Rig(document.source());
        pending.tick(false);
        pending.first.rising[2]++;
        assertEquals(Collections.singletonList("edge"), pending.tick(false));
        String header = draft.appliedClause(PROJECT, root, document.code());
        document.editGraph(
            g -> g.node(root.id)
                .setClause(header));
        Rig applied = new Rig(document.source());
        applied.tick(false);
        applied.first.rising[2]++;
        assertTrue(
            applied.tick(false)
                .isEmpty());
        applied.first.falling[2]++;
        assertEquals(Collections.singletonList("edge"), applied.tick(false));
        assertTrue(document.undo());
        Rig undo = new Rig(document.source());
        undo.tick(false);
        undo.first.rising[2]++;
        assertEquals(Collections.singletonList("edge"), undo.tick(false));
        assertTrue(document.redo());
        Rig redo = new Rig(document.source());
        redo.tick(false);
        redo.first.falling[2]++;
        assertEquals(Collections.singletonList("edge"), redo.tick(false));
    }

    @Test
    void structuredEdgeAndLabelsPreserveExactLexemesSuffixAndNoop() {
        for (String edge : Arrays.asList("rIsInG", "PuLsE", "FaLlInG")) {
            String header = "  eVeRy\tReDsToNe " + edge
                + " FrOm \"Room "
                + UNICODE
                + ", A\" ,\tspare,spare  NoRtH , EaSt SiDe  ";
            Node node = node(header);
            StudioFieldDraft fields = draft(node);
            assertEquals(
                3,
                StudioNodeProperties.inspect(node.type, header)
                    .size());
            assertEquals(edge, fields.value("edge"));
            assertEquals(3, fields.labelCount());
            assertFalse(fields.dirty());
            assertEquals(header, fields.appliedClause(PROJECT, node, fields.source));
            assertEquals("  NoRtH , EaSt SiDe  ", StudioNamedRedstoneHeader.physicalSideSuffix(header));
            assertTrue(
                fields.setLabel(
                    fields.labelRows()
                        .get(1).id,
                    "\"changed\""));
            assertEquals(
                header.replace("spare,spare", "\"changed\",spare"),
                fields.appliedClause(PROJECT, node, fields.source));
            assertTrue(fields.set("edge", "FALLING"));
            assertTrue(
                fields.appliedClause(PROJECT, node, fields.source)
                    .endsWith("  NoRtH , EaSt SiDe  "));
            assertEquals(header, node.clause());
        }
    }

    @Test
    void rowCapsUseCompleteManualFallbackAndNoConflictingHeaderOwner() {
        String atCap = "EVERY REDSTONE PULSE FROM " + String.join(",", Collections.nCopies(32, "signal"));
        StudioFieldDraft fields = draft(node(atCap));
        assertEquals(32, fields.labelCount());
        assertThrows(IllegalArgumentException.class, () -> fields.addLabel("spare"));
        fields.setLabel(
            fields.labelRows()
                .get(0).id,
            "spare");
        assertThrows(IllegalArgumentException.class, () -> fields.set("header", HEADER));
        assertThrows(IllegalArgumentException.class, () -> fields.value("header"));
        assertEquals(
            "spare",
            fields.labelRows()
                .get(0).text);
        StudioFieldDraft manual = draft(node(manualHeader()));
        assertFalse(manual.hasLabelRows());
        assertEquals(manualHeader(), manual.value("header"));
        assertThrows(IllegalArgumentException.class, () -> manual.set("edge", "FALLING"));
        assertEquals(manualHeader(), manual.appliedClause(PROJECT, nodeFor(manual), manual.source));
    }

    private static Node nodeFor(StudioFieldDraft fields) {
        return new Node(fields.node, fields.type, 0, 0, fields.original);
    }

    @Test
    void rawHeaderStatelessCompatibilityKeepsItsGuardAndTrailingGap() {
        assertEquals(HEADER, StudioNodeProperties.update(Type.NAMED_REDSTONE, HEADER, "header", HEADER));
        String replacement = "every redstone falling from spare south side";
        assertEquals(
            replacement + "  ",
            StudioNodeProperties.update(Type.NAMED_REDSTONE, HEADER + "  ", "header", replacement));
        for (String invalid : Arrays.asList(
            "",
            "every 20 ticks",
            HEADER + " DO END",
            HEADER + " -- comment",
            "EVERY REDSTONE PULSE FROM signal FRONT SIDE"))
            assertThrows(
                IllegalArgumentException.class,
                () -> StudioNodeProperties.update(Type.NAMED_REDSTONE, HEADER, "header", invalid));
    }

    @Test
    void invalidRowsOrModeBlockTheWholeTransactionAndCopyRetainsIndependentRows() {
        Node node = node("EVERY REDSTONE PULSE FROM signal,spare NORTH SIDE");
        for (String invalid : Arrays.asList("", "\"unfinished", "a,b", "x -- comment", "x\n", "x\0")) {
            StudioFieldDraft fields = draft(node);
            fields.set("edge", "FALLING");
            UUID first = fields.labelRows()
                .get(0).id;
            fields.setLabel(first, invalid);
            StudioFieldDraft copy = new StudioFieldDraft(fields);
            assertEquals(
                first,
                copy.labelRows()
                    .get(0).id);
            assertEquals(fields.labelRevision(), copy.labelRevision());
            assertThrows(IllegalArgumentException.class, () -> fields.appliedClause(PROJECT, node, fields.source));
            assertThrows(IllegalArgumentException.class, () -> copy.appliedClause(PROJECT, node, copy.source));
            copy.setLabel(first, "signal");
            assertEquals(
                "EVERY REDSTONE FALLING FROM signal,spare NORTH SIDE",
                copy.appliedClause(PROJECT, node, copy.source));
            assertEquals(invalid, fields.labelValue(first));
        }
        for (String invalid : Arrays.asList("", "TRUE", "RISING FROM spare", "FALLING DO END", "PULSE -- x")) {
            StudioFieldDraft fields = draft(node);
            fields.set("edge", invalid);
            fields.setLabel(
                fields.labelRows()
                    .get(0).id,
                "spare");
            assertThrows(IllegalArgumentException.class, () -> fields.appliedClause(PROJECT, node, fields.source));
            assertEquals("EVERY REDSTONE PULSE FROM signal,spare NORTH SIDE", node.clause());
        }
    }

    @Test
    void labelReplacementProtectsKeywordBoundariesAndAllPhysicalSuffixForms() {
        for (String suffix : Arrays.asList("", "\t", " NORTH SIDE", " EACH SIDE", " TOP , BOTTOM SIDE")) {
            String original = "every redstone pulse from\"signal\"" + suffix;
            Node node = node(original);
            StudioFieldDraft fields = draft(node);
            fields.set("label", "spare");
            String changed = fields.appliedClause(PROJECT, node, fields.source);
            assertTrue(changed.endsWith(suffix));
            assertEquals("every redstone pulse from spare" + suffix, changed);
            BlueprintCompiler.validateClause(Type.NAMED_REDSTONE, changed);
        }
        String original = "every redstone pulse from\"signal\"NORTH SIDE";
        assertEquals(
            "every redstone pulse from spare NORTH SIDE",
            StudioNodeProperties.update(Type.NAMED_REDSTONE, original, "label", "spare"));
    }

    private static StudioFieldPicker.Session labelPicker(StudioFieldDraft fields, UUID row) {
        StudioFieldPicker.Target target = StudioFieldPicker.labelRowTarget(fields, row);
        assertNotNull(target);
        StudioFieldPicker.Snapshot snapshot = StudioFieldPicker.labels(fields.source, target);
        StudioFieldPicker.Session session = new StudioFieldPicker.Session(fields, target, snapshot);
        session.select(
            snapshot.search("spare").entries.stream()
                .filter(c -> c.value.equals("spare"))
                .findFirst()
                .get());
        return session;
    }

    @Test
    void existingBoundedPickerRepairsRowsAndFencesCopiesOwnersAndStructuralAba() {
        Node node = node("EVERY REDSTONE PULSE FROM signal,spare NORTH SIDE");
        StudioFieldDraft fields = draft(node);
        UUID row = fields.labelRows()
            .get(0).id;
        fields.setLabel(row, "\"unfinished");
        StudioFieldPicker.Session repair = labelPicker(fields, row);
        assertThrows(
            IllegalArgumentException.class,
            () -> repair.choose(new StudioFieldDraft(fields), PROJECT, node, fields.source));
        assertThrows(
            IllegalArgumentException.class,
            () -> repair.choose(fields, UUID.randomUUID(), node, fields.source));
        assertThrows(IllegalArgumentException.class, () -> repair.choose(fields, PROJECT, node, fields.source + " "));
        assertTrue(repair.choose(fields, PROJECT, node, fields.source));
        assertEquals("\"spare\"", fields.labelValue(row));
        StudioFieldPicker.Session stale = labelPicker(fields, row);
        UUID added = fields.addLabel("signal");
        fields.removeLabel(added);
        assertThrows(IllegalArgumentException.class, () -> stale.choose(fields, PROJECT, node, fields.source));
        assertFalse(fields.setLabel(row, "\"spare\""));
        fields.removeLabel(
            fields.labelRows()
                .get(1).id);
        assertThrows(IllegalArgumentException.class, () -> fields.removeLabel(row));
        assertEquals(
            "EVERY REDSTONE PULSE FROM \"spare\" NORTH SIDE",
            fields.appliedClause(PROJECT, node, fields.source));
    }

    @Test
    void physicalSideFieldIsLastAndManualHeadersNeverExposePartialSides() {
        List<StudioNodeProperties.Field> fields = StudioNodeProperties.inspect(Type.NAMED_REDSTONE, HEADER);
        assertEquals(3, fields.size());
        assertEquals(
            "edge",
            fields.get(0)
                .getKey());
        assertEquals(
            "label",
            fields.get(1)
                .getKey());
        assertEquals(
            "sides",
            fields.get(2)
                .getKey());
        assertEquals(
            StudioNodeProperties.FieldType.PHYSICAL_SIDES,
            fields.get(2)
                .getType());
        assertEquals(
            "North",
            fields.get(2)
                .getValue());
        assertNull(StudioFieldPicker.target(Type.NAMED_REDSTONE, fields.get(2), "North"));
        StudioFieldDraft manual = draft(node(manualHeader()));
        assertThrows(IllegalArgumentException.class, () -> manual.value("sides"));
        assertThrows(IllegalArgumentException.class, () -> manual.set("sides", "SOUTH"));
        StudioFieldDraft cap = draft(
            node("EVERY REDSTONE PULSE FROM " + String.join(",", Collections.nCopies(32, "signal"))));
        assertEquals("", cap.value("sides"));
        cap.set("sides", "EACH");
        assertTrue(
            cap.appliedClause(PROJECT, nodeFor(cap), cap.source)
                .endsWith(" EACH SIDE"));
    }

    @Test
    void physicalSideEditsOwnOnlyValueOrWholeQualifierAndPreserveBoundaryGaps() {
        String prefix = "  eVeRy ReDsToNe PuLsE FrOm \"" + UNICODE + ", room\",spare,spare";
        String header = prefix + "\t NoRtH , EaSt \tSiDe  \t";
        assertEquals("NoRtH , EaSt", StudioNamedRedstoneHeader.physicalSideValue(header));
        assertEquals(header, StudioNodeProperties.update(Type.NAMED_REDSTONE, header, "sides", "north , east"));
        assertEquals(
            prefix + "\t south, TOP \tSiDe  \t",
            StudioNodeProperties.update(Type.NAMED_REDSTONE, header, "sides", " south, TOP "));
        assertEquals(
            prefix + "\t EACH \tSiDe  \t",
            StudioNodeProperties.update(Type.NAMED_REDSTONE, header, "sides", "EACH"));
        assertEquals(prefix + "\t   \t", StudioNodeProperties.update(Type.NAMED_REDSTONE, header, "sides", ""));
        String each = prefix + " EACH\tSiDe  ";
        assertEquals(prefix + " WEST\tSiDe  ", StudioNodeProperties.update(Type.NAMED_REDSTONE, each, "sides", "WEST"));
        assertEquals(prefix + "   ", StudioNodeProperties.update(Type.NAMED_REDSTONE, each, "sides", ""));
    }

    @Test
    void absentAndTouchingPhysicalSidesPreserveUnicodeOffsetsAndTrailingWhitespace() {
        String prefix = "every redstone pulse from\"" + UNICODE + "\"";
        String absent = prefix + " \t ";
        assertEquals(absent, StudioNodeProperties.update(Type.NAMED_REDSTONE, absent, "sides", " \t"));
        assertEquals(
            prefix + " NORTH, BOTTOM SIDE \t ",
            StudioNodeProperties.update(Type.NAMED_REDSTONE, absent, "sides", "NORTH, BOTTOM"));
        String touching = prefix + "NORTH SiDe\t";
        assertEquals(
            prefix + "SOUTH SiDe\t",
            StudioNodeProperties.update(Type.NAMED_REDSTONE, touching, "sides", "SOUTH"));
        assertEquals(prefix + "\t", StudioNodeProperties.update(Type.NAMED_REDSTONE, touching, "sides", ""));
        assertEquals(
            prefix + " EACH SIDE\t",
            StudioNodeProperties.update(Type.NAMED_REDSTONE, prefix + "\t", "sides", "EACH"));
    }

    @Test
    void physicalSideInputRefusesAllNonPhysicalOrInjectedTextBeforeTrimming() {
        List<String> invalid = Arrays.asList(
            "UP",
            "DOWN",
            "FRONT",
            "BACK",
            "LEFT",
            "RIGHT",
            "NULL",
            "SIDE",
            "NORTH SIDE",
            "EACH SIDE",
            "NORTH,",
            ",NORTH",
            "NORTH EAST",
            "EACH,NORTH",
            "NORTH,EACH",
            "NORTH -- comment",
            "-- comment",
            "NORTH DO END",
            "SLOTS 0",
            "ROUND ROBIN BY LABEL",
            "\"NORTH\"",
            "LABEL(\"signal\")",
            "[NORTH]",
            "{NORTH}",
            "$face",
            "NORTH;",
            "\n",
            "\r",
            "\0",
            "NORTH\n",
            "NORTH\r",
            "NORTH\0",
            String.join("", Collections.nCopies(BlueprintGraph.MAX_CLAUSE + 1, " ")));
        for (String value : invalid) {
            assertThrows(
                IllegalArgumentException.class,
                () -> StudioNodeProperties.update(Type.NAMED_REDSTONE, HEADER, "sides", value),
                value);
            assertThrows(
                IllegalArgumentException.class,
                () -> StudioNamedRedstoneHeader.updatePhysicalSides(HEADER, value),
                value);
        }
        assertThrows(IllegalArgumentException.class, () -> StudioNamedRedstoneHeader.updatePhysicalSides(HEADER, null));
    }

    @Test
    void invalidSideBlocksCombinedFieldsAndCopiedRepairRetainsOwnershipAndPickerFences() {
        Node node = node("EVERY REDSTONE PULSE FROM signal,spare NORTH SIDE");
        StudioFieldDraft fields = draft(node);
        fields.set("edge", "FALLING");
        fields.setLabel(
            fields.labelRows()
                .get(0).id,
            "\"" + UNICODE + "\"");
        fields.set("sides", "FRONT");
        StudioFieldDraft copy = new StudioFieldDraft(fields);
        assertThrows(IllegalArgumentException.class, () -> fields.appliedClause(PROJECT, node, fields.source));
        assertThrows(IllegalArgumentException.class, () -> copy.appliedClause(PROJECT, node, copy.source));
        copy.set("sides", "SOUTH, BOTTOM");
        String expected = "EVERY REDSTONE FALLING FROM \"" + UNICODE + "\",spare SOUTH, BOTTOM SIDE";
        assertEquals(expected, copy.appliedClause(PROJECT, node, copy.source));
        assertEquals("FRONT", fields.value("sides"));
        assertEquals("EVERY REDSTONE PULSE FROM signal,spare NORTH SIDE", node.clause());
        assertThrows(IllegalArgumentException.class, () -> copy.appliedClause(UUID.randomUUID(), node, copy.source));
        assertThrows(IllegalArgumentException.class, () -> copy.appliedClause(PROJECT, node, copy.source + " "));
        assertThrows(
            IllegalArgumentException.class,
            () -> copy.appliedClause(PROJECT, node(node.clause()), copy.source));
        UUID row = fields.labelRows()
            .get(0).id;
        StudioFieldPicker.Session stale = labelPicker(fields, row);
        UUID added = fields.addLabel("signal");
        fields.removeLabel(added);
        assertThrows(IllegalArgumentException.class, () -> stale.choose(fields, PROJECT, node, fields.source));
    }

    @Test
    void clearedAbsentSideAndRestoredExistingSideDoNotLeaveFalsePendingChanges() {
        StudioFieldDraft absent = draft(node("EVERY REDSTONE PULSE FROM signal"));
        assertFalse(absent.set("sides", ""));
        absent.set("sides", "EACH");
        assertTrue(absent.dirty());
        absent.set("sides", "");
        assertFalse(absent.dirty());
        assertEquals(absent.original, absent.appliedClause(PROJECT, nodeFor(absent), absent.source));
        StudioFieldDraft present = draft(node(HEADER));
        present.set("sides", "");
        present.set("sides", "North");
        assertFalse(present.dirty());
        assertEquals(HEADER, present.appliedClause(PROJECT, nodeFor(present), present.source));
    }

    @Test
    void combinedSideEditIsOneHistoryStepAndKeepsUnrelatedUnicodeSourceMappings() {
        String source = "-- " + UNICODE
            + "\r\n"
            + body(HEADER, "PRINT(\"one\")")
            + "\r\n"
            + body(HEADER, "PRINT(\"two\")");
        StudioDocument document = new StudioDocument();
        document.load(source);
        document.openVisual();
        Node root = document.graph()
            .triggers()
            .get(0),
            other = document.graph()
                .triggers()
                .get(1);
        String otherClause = other.clause();
        List<String> ids = identities(document.graph());
        StudioSourceMap oldMap = StudioSourceMap
            .build(PROJECT, document.code(), StudioSourceMap.normalize(document.code()), document.graph());
        StudioFieldDraft fields = new StudioFieldDraft(
            PROJECT,
            root,
            document.code(),
            StudioNodeProperties.inspect(root.type, root.clause()));
        fields.set("edge", "FALLING");
        fields.set("label", "spare");
        fields.set("sides", "SOUTH");
        String changed = fields.appliedClause(PROJECT, root, document.code());
        assertEquals(source, document.source());
        assertFalse(document.undo());
        document.editGraph(
            g -> g.node(root.id)
                .setClause(changed));
        String after = document.source();
        assertEquals(ids, identities(document.graph()));
        assertEquals(otherClause, other.clause());
        assertFalse(
            oldMap.matches(PROJECT, document.code(), StudioSourceMap.normalize(document.code()), document.graph()));
        StudioSourceMap current = StudioSourceMap
            .build(PROJECT, document.code(), StudioSourceMap.normalize(document.code()), document.graph());
        assertEquals(
            other.id,
            current.atCaret(
                StudioSourceMap.normalize(document.code())
                    .lastIndexOf("eVeRy")).node);
        assertTrue(document.undo());
        assertEquals(source, document.source());
        assertFalse(document.undo());
        assertTrue(document.redo());
        assertEquals(after, document.source());
        assertEquals(ids, identities(document.graph()));
    }

    @Test
    void actualEditedPhysicalMasksMatchFixedProgramsForAllFacesAliasesAndIdleTicks() throws Exception {
        String[] values = { "BOTTOM", "TOP", "NORTH", "SOUTH", "WEST", "EAST", "EACH", "", "NORTH,NORTH,TOP" };
        int[] masks = { 1, 2, 4, 8, 16, 32, 63, 63, 6 };
        for (String edge : Arrays.asList("PULSE", "RISING", "FALLING")) for (int i = 0; i < values.length; i++) {
            StudioFieldDraft fields = draft(node("EVERY REDSTONE PULSE FROM signal NORTH SIDE"));
            fields.set("edge", edge);
            fields.set("sides", values[i]);
            String edited = fields.appliedClause(PROJECT, nodeFor(fields), fields.source);
            String fixed = "EVERY REDSTONE " + edge
                + " FROM signal"
                + (values[i].isEmpty() ? "" : " " + values[i] + " SIDE");
            for (String header : Arrays.asList(edited, fixed)) {
                Rig rig = new Rig(body(header, "PRINT(\"edge\")"));
                assertEquals(
                    masks[i],
                    ((NamedRedstoneTrigger) rig.program.getTriggers()
                        .get(0)).getSideMask());
                assertTrue(
                    rig.tick(false)
                        .isEmpty());
                int selected = Integer.numberOfTrailingZeros(masks[i]);
                if (masks[i] != 63) {
                    int wrong = Integer.numberOfTrailingZeros((~masks[i]) & 63);
                    rig.first.rising[wrong]++;
                    rig.first.falling[wrong]++;
                    assertTrue(
                        rig.tick(false)
                            .isEmpty());
                }
                if (edge.equals("FALLING")) rig.first.rising[selected]++;
                else rig.first.falling[selected]++;
                assertTrue(
                    rig.tick(false)
                        .isEmpty());
                if (edge.equals("FALLING")) rig.first.falling[selected]++;
                else rig.first.rising[selected]++;
                assertEquals(Collections.singletonList("edge"), rig.tick(false));
                assertTrue(
                    rig.tick(false)
                        .isEmpty());
            }
        }
    }

    @Test
    void actualStructuredFieldsKeepAliasGroupAndPhysicalSideTickOutcomes() throws Exception {
        for (String edge : Arrays.asList("PULSE", "RISING", "FALLING")) {
            Node node = node("EVERY REDSTONE PULSE FROM signal NORTH SIDE");
            StudioFieldDraft fields = draft(node);
            fields.set("edge", edge);
            fields.addLabel("spare");
            fields.addLabel("signal");
            String changed = fields.appliedClause(PROJECT, node, fields.source);
            for (String candidate : Arrays.asList(
                body(changed, "PRINT(\"edge\")"),
                BlueprintCompiler.generate(BlueprintCompiler.importText(body(changed, "PRINT(\"edge\")"))))) {
                Rig rig = new Rig(candidate);
                assertTrue(
                    rig.tick(false)
                        .isEmpty());
                rig.first.rising[5]++;
                rig.first.falling[5]++;
                assertTrue(
                    rig.tick(false)
                        .isEmpty());
                if (edge.equals("FALLING")) {
                    rig.first.rising[2]++;
                    rig.second.rising[2]++;
                } else {
                    rig.first.falling[2]++;
                    rig.second.falling[2]++;
                }
                assertTrue(
                    rig.tick(false)
                        .isEmpty());
                if (edge.equals("FALLING")) {
                    rig.first.falling[2] += 3;
                    rig.second.falling[2] += 5;
                } else {
                    rig.first.rising[2] += 3;
                    rig.second.rising[2] += 5;
                }
                assertEquals(Collections.singletonList("edge"), rig.tick(false));
                assertTrue(
                    rig.tick(false)
                        .isEmpty());
            }
        }
    }
}
