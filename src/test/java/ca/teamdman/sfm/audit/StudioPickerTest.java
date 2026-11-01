package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.blueprint.BlueprintGraph;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Type;
import ca.teamdman.sfm.client.blueprint.StudioDocument;
import ca.teamdman.sfm.client.blueprint.StudioFieldDraft;
import ca.teamdman.sfm.client.blueprint.StudioFieldPicker;
import ca.teamdman.sfm.client.blueprint.StudioFieldPicker.Candidate;
import ca.teamdman.sfm.client.blueprint.StudioFieldPicker.Session;
import ca.teamdman.sfm.client.blueprint.StudioFieldPicker.Snapshot;
import ca.teamdman.sfm.client.blueprint.StudioFieldPicker.Target;
import ca.teamdman.sfm.client.blueprint.StudioNodeProperties;
import ca.teamdman.sfm.client.blueprint.StudioNodeProperties.Field;
import ca.teamdman.sfm.client.gui.ProgrammingStationDraftState;
import ca.teamdman.sfm.common.net.PacketStationRequest.Action;
import ca.teamdman.sfm.common.program.project.ProjectDraft;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class StudioPickerTest {

    @Test
    void multipleConditionFieldsApplyTogetherAndInvalidPendingValuesLeaveHistoryUntouched() {
        StudioDocument document = new StudioDocument();
        document
            .load(program("IF (\"source\" HAS > 0 WITH #forge:ingots) THEN OUTPUT 9 REDSTONE LEVEL TO \"signal\" END"));
        document.openVisual();
        Node node = document.graph()
            .nodes()
            .stream()
            .filter(n -> n.type == Type.IF)
            .findFirst()
            .get();
        UUID project = UUID.randomUUID(), id = node.id;
        String before = document.source(), code = document.code();
        StudioFieldDraft draft = new StudioFieldDraft(
            project,
            node,
            code,
            StudioNodeProperties.inspect(node.type, node.clause()));
        draft.set("label", "\"Other 🏭\"");
        draft.set("amount", "0002");
        draft.set("comparator", "<=");
        draft.set("resource", "item:a OR item:b");
        assertThrows(IllegalArgumentException.class, () -> draft.appliedClause(project, node, code));
        assertEquals(before, document.source());
        assertFalse(document.undo());
        draft.set("resource", "item:minecraft:iron_ingot");
        String applied = draft.appliedClause(project, node, code);
        assertEquals(
            node.clause()
                .replace("\"source\"", "\"Other 🏭\"")
                .replace("HAS > 0", "HAS <= 0002 item:minecraft:iron_ingot "),
            applied);
        document.editGraph(
            graph -> graph.node(id)
                .setClause(applied));
        references(document.code(), "Other 🏭");
        assertTrue(document.undo());
        assertEquals(before, document.source());
        assertEquals(
            id,
            document.graph()
                .node(id).id);
        assertFalse(document.undo());
    }

    @Test
    void cancelingPendingFieldsDoesNotRewindTheLateLoadEditFence() {
        ProgrammingStationDraftState state = new ProgrammingStationDraftState();
        long request = state.begin(Action.LOAD);
        // Pending field changes do not change serialized source/dirty state, but count as local editing.
        state.noteLocalEdit();
        assertFalse(state.dirty());
        assertTrue(
            state.receive(
                request,
                Action.LOAD,
                true,
                new ProjectDraft(UUID.randomUUID(), UUID.randomUUID(), 0, "Other", program("PRINT(1)")),
                null));
        assertTrue(state.ignoredLateLoad);
        assertNull(state.saved);
        assertEquals("", state.source);
    }

    private static Field field(Type type, String clause, String key) {
        return StudioNodeProperties.inspect(type, clause)
            .stream()
            .filter(
                f -> f.getKey()
                    .equals(key))
            .findFirst()
            .get();
    }

    private static Target target(Type type, String clause, String key) {
        Field field = field(type, clause, key);
        return StudioFieldPicker.target(type, field, field.getValue());
    }

    private static Candidate candidate(Snapshot snapshot, String value) {
        return snapshot.search(value).entries.stream()
            .filter(c -> c.value.equals(value))
            .findFirst()
            .get();
    }

    private static String program(String clause) {
        return "EVERY 20 TICKS DO " + clause + " END";
    }

    private static void references(String source, String value) {
        ProgramBuildResult built = new ProgramBuilder(source).build();
        assertTrue(built.isSuccess(), built.getErrorMessage());
        assertTrue(
            built.getProgram()
                .getReferencedLabels()
                .contains(value),
            value);
    }

    @Test
    void quotedAndTypedLabelPickersShareStandardStringEscapeSemantics() {
        String[] names = { "Back\\slash", "Escaped\\\"quote", "Plain\"quote", "Trailing\\", "Unicode 🏭" };
        StringBuilder source = new StringBuilder("EVERY 20 TICKS DO INPUT FROM \"Legacy\\\\path\" ");
        for (String name : names) source.append("PRINT(LABEL(")
            .append(DataValue.string(name))
            .append(")) ");
        source.append("END");
        Target legacy = target(Type.INPUT, "INPUT FROM \"source\"", "label");
        Target typed = target(Type.TRANSFER, "TRANSFER(LaBeL(  \"old\"  ), LABEL(\"dest\"))", "source");
        Snapshot old = StudioFieldPicker.labels(source.toString(), legacy);
        Snapshot modern = StudioFieldPicker.labels(source.toString(), typed);
        for (String name : names) {
            Candidate selected = candidate(modern, name);
            assertEquals("LaBeL(  " + DataValue.string(name) + "  )", selected.replacement);
            references(program("PRINT(" + selected.replacement + ")"), name);
        }
        for (String name : new String[] { names[0], names[1], names[2], names[3], names[4], "Legacy\\path" }) {
            Candidate selected = candidate(old, name);
            assertEquals(
                DataValue.string(name)
                    .toString(),
                selected.replacement);
            references(program("INPUT FROM " + selected.replacement), name);
        }
        assertEquals(0, old.omitted);
        assertTrue(
            old.search("Plain").entries.stream()
                .anyMatch(c -> c.value.equals(names[2])));
        assertTrue(old.origin.contains("not live connectivity"));
        assertTrue(modern.origin.contains("Draft references"));
    }

    @Test
    void onlyBareConstructorsAndSingleLegacyFieldsArePickable() {
        String clause = "TRANSFER(LABEL(\"s\").slots(0, 1), LABEL(\"d\"), ITEM(\"*\"), 2)";
        assertNull(target(Type.TRANSFER, clause, "source"));
        assertNull(target(Type.TRANSFER, clause, "amount"));
        assertNotNull(target(Type.TRANSFER, clause, "destination"));
        assertNotNull(target(Type.TRANSFER, clause, "resource"));
        Field source = field(Type.TRANSFER, clause, "source");
        for (String value : new String[] { "LABEL(\"s\").side(\"north\")", "LABEL(\"s\" + \"x\")",
            "LABEL(\"s\") -- comment", "LABEL(\"s\")\n", "LABEL(\"s\"), LABEL(\"d\")", "1", "$x", "LABEL(\"s\")@" })
            assertNull(StudioFieldPicker.target(Type.TRANSFER, source, value), value);
        assertNull(target(Type.PRINT, "PRINT(LABEL(\"s\"))", "expression0"));
        assertNotNull(target(Type.IF, "\"source\" HAS > 0", "resource"));
    }

    @Test
    void itemSnapshotsCountInvalidKeysBoundIterationAndNeverRescanOnSearch() {
        Target item = target(Type.TRANSFER, "TRANSFER(LABEL(\"s\"), LABEL(\"d\"), ItEm( \"*\" ))", "resource");
        AtomicInteger reads = new AtomicInteger();
        Iterable<String> registry = () -> new Iterator<String>() {

            private int index;

            public boolean hasNext() {
                return index < StudioFieldPicker.MAX_REGISTRY_VISITS + 20;
            }

            public String next() {
                reads.incrementAndGet();
                int current = index++;
                if (current == 0) return null;
                if (current == 1) return new String(new char[257]).replace('\0', 'x');
                if (current == 2) return "bad key\n";
                return "test:item_" + current;
            }
        };
        Snapshot snapshot = StudioFieldPicker.items(registry, item);
        assertEquals(8192, snapshot.visited);
        assertEquals(8192, reads.get());
        assertTrue(snapshot.truncated);
        assertEquals(3, snapshot.omitted);
        assertEquals(64, snapshot.search("").entries.size());
        assertEquals(8190, snapshot.search("").matches); // 8189 valid IDs plus wildcard.
        assertEquals("ItEm( \"test:item_30\" )", candidate(snapshot, "test:item_30").replacement);
        for (int i = 0; i < 10; i++) snapshot.search("ITEM_3");
        assertEquals(8192, reads.get());
        assertThrows(IllegalArgumentException.class, () -> snapshot.search(new String(new char[97])));
        assertThrows(UnsupportedOperationException.class, () -> snapshot.search("").entries.clear());
        Target legacy = target(Type.IF, "\"source\" HAS > 0", "resource");
        Snapshot old = StudioFieldPicker.items(Arrays.asList("Mod:Item", "minecraft:iron_ingot"), legacy);
        assertEquals("\"item:Mod:Item\"", candidate(old, "Mod:Item").replacement);
        StudioNodeProperties.update(Type.IF, "\"source\" HAS > 0", "resource", candidate(old, "Mod:Item").replacement);
        assertTrue(old.origin.contains("metadata/NBT stay manual"));
    }

    @Test
    void draftLabelsAreBoundedAndInvalidSourcesDoNotProvidePartialSuggestions() {
        Target target = target(Type.INPUT, "INPUT FROM \"source\"", "label");
        StringBuilder source = new StringBuilder("EVERY 20 TICKS DO ");
        for (int i = 0; i < 300; i++) source.append("INPUT FROM \"label_")
            .append(i)
            .append("\" ");
        source.append("END");
        Snapshot snapshot = StudioFieldPicker.labels(source.toString(), target);
        assertTrue(snapshot.truncated);
        assertEquals(300, snapshot.visited);
        assertEquals(256, snapshot.search("").matches);
        assertTrue(
            StudioFieldPicker.labels("EVERY invalid", target)
                .search("").entries.isEmpty());
        assertTrue(StudioFieldPicker.labels(new String(new char[32768]), target).truncated);
    }

    @Test
    void selectionAndChooseStayPendingUntilOneApplyAndUndoPreservesIdsAndExactSource() {
        StudioDocument document = new StudioDocument();
        document.load(program("INPUT FROM \"source\" OUTPUT TO \"dest\""));
        document.openVisual();
        Node node = document.graph()
            .nodes()
            .stream()
            .filter(n -> n.type == Type.INPUT)
            .findFirst()
            .get();
        UUID project = UUID.randomUUID(), id = node.id;
        String original = document.source(), code = document.code();
        StudioFieldDraft draft = new StudioFieldDraft(
            project,
            node,
            code,
            StudioNodeProperties.inspect(node.type, node.clause()));
        Target target = target(node.type, node.clause(), "label");
        Snapshot snapshot = StudioFieldPicker.labels(code, target);
        Session canceled = new Session(draft, target, snapshot);
        canceled.select(candidate(snapshot, "dest"));
        assertFalse(draft.dirty());
        assertEquals(original, document.source());
        assertFalse(document.undo());
        Session selected = new Session(draft, target, snapshot);
        selected.select(candidate(snapshot, "dest"));
        assertTrue(selected.choose(draft, project, node, code));
        assertTrue(draft.dirty());
        assertEquals(original, document.source());
        assertEquals("INPUT FROM \"source\"", node.clause());
        assertFalse(document.undo());
        StudioFieldDraft retained = new StudioFieldDraft(draft);
        assertEquals(draft.value("label"), retained.value("label"));
        // A second picker can be canceled without clearing the prior pending choice.
        Target pending = StudioFieldPicker
            .target(node.type, field(node.type, node.clause(), "label"), draft.value("label"));
        Snapshot choices = StudioFieldPicker.labels(code, pending);
        Session canceledAgain = new Session(draft, pending, choices);
        canceledAgain.select(candidate(choices, "source"));
        assertEquals("\"dest\"", draft.value("label"));
        String applied = draft.appliedClause(project, node, code);
        document.editGraph(
            graph -> graph.node(id)
                .setClause(applied));
        assertEquals(
            id,
            document.graph()
                .node(id).id);
        assertTrue(
            document.code()
                .contains("INPUT FROM \"dest\""));
        assertTrue(document.undo());
        assertEquals(original, document.source());
        assertEquals(code, document.code());
        assertEquals(
            id,
            document.graph()
                .node(id).id);
        assertFalse(document.undo());
    }

    @Test
    void staleOwnersProjectsNodesClausesWholeSourcesAndPendingValuesFailAtomically() {
        BlueprintGraph graph = new BlueprintGraph();
        Node node = new Node(Type.INPUT, 0, 0);
        graph.add(node);
        node.setClause("INPUT FROM \"source\"");
        String source = program("INPUT FROM \"source\" OUTPUT TO \"dest\"");
        UUID project = UUID.randomUUID();
        StudioFieldDraft draft = new StudioFieldDraft(
            project,
            node,
            source,
            StudioNodeProperties.inspect(node.type, node.clause()));
        Target target = target(node.type, node.clause(), "label");
        Snapshot snapshot = StudioFieldPicker.labels(source, target);
        Session session = new Session(draft, target, snapshot);
        session.select(candidate(snapshot, "dest"));
        assertThrows(
            IllegalArgumentException.class,
            () -> session.choose(new StudioFieldDraft(draft), project, node, source));
        assertThrows(IllegalArgumentException.class, () -> session.choose(draft, UUID.randomUUID(), node, source));
        assertThrows(
            IllegalArgumentException.class,
            () -> session.choose(draft, project, new Node(Type.INPUT, 0, 0), source));
        assertThrows(IllegalArgumentException.class, () -> session.choose(draft, project, null, source));
        assertThrows(
            IllegalArgumentException.class,
            () -> session.choose(draft, project, node, source + " -- unrelated edit"));
        node.setClause("INPUT FROM \"other\"");
        assertThrows(IllegalArgumentException.class, () -> session.choose(draft, project, node, source));
        node.setClause("INPUT FROM \"source\"");
        Target foreign = target(node.type, node.clause(), "label");
        Snapshot other = StudioFieldPicker.labels(source, foreign);
        assertThrows(IllegalArgumentException.class, () -> new Session(draft, target, other));
        assertThrows(IllegalArgumentException.class, () -> session.select(candidate(other, "dest")));
        assertFalse(draft.dirty());
        draft.set("label", "\"manual\"");
        assertThrows(IllegalArgumentException.class, () -> session.choose(draft, project, node, source));
        assertEquals("\"manual\"", draft.value("label"));
        assertEquals("INPUT FROM \"source\"", node.clause());
        assertThrows(IllegalArgumentException.class, () -> draft.appliedClause(project, node, source + " "));
    }
}
