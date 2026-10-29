package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.blueprint.BlueprintCompiler;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Pin;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Type;
import ca.teamdman.sfm.client.blueprint.BlueprintLayoutMatcher;
import ca.teamdman.sfm.client.blueprint.BlueprintLayoutMatcher.Result;

class BlueprintLayoutMatcherTest {

    @Test
    void ordinaryQuantityItemLabelAndConditionEditsKeepIdsAndPositionsWithoutChangingCurrentSource() {
        BlueprintGraph previous = graph(
            "NAME \"Before\" EVERY 20 TICKS DO "
                + "IF source HAS >= 3 minecraft:stone THEN INPUT 64 minecraft:stone FROM source "
                + "OUTPUT 8 TO dest ELSE FORGET source END OUTPUT 1 TO final END");
        BlueprintGraph current = graph(
            "NAME \"After\" EVERY 40 TICKS DO "
                + "IF \"Source B\" HAS >= 5 minecraft:iron_ingot THEN INPUT 32 minecraft:iron_ingot FROM \"Source B\" "
                + "OUTPUT 16 TO \"Destination B\" ELSE FORGET \"Source B\" END OUTPUT 2 TO final END");
        place(previous);
        String originalCurrent = BlueprintCompiler.generate(current),
            originalPrevious = BlueprintCompiler.generate(previous);
        Result result = BlueprintLayoutMatcher.match(previous, current);
        assertEquals(
            previous.nodes()
                .size(),
            result.reusedNodes);
        assertEquals(0, result.freshNodes);
        assertEquals(originalCurrent, BlueprintCompiler.generate(result.graph));
        assertEquals(originalCurrent, BlueprintCompiler.generate(current));
        assertEquals(originalPrevious, BlueprintCompiler.generate(previous));
        assertPositions(previous, result);
        assertThrows(UnsupportedOperationException.class, () -> result.retainedIds.clear());
    }

    @Test
    void insertionAndDeletionKeepOnlyConfidentSurvivingNodes() {
        BlueprintGraph previous = graph("EVERY 20 TICKS DO INPUT 3 FROM source OUTPUT 4 TO dest FORGET source END");
        BlueprintGraph inserted = graph(
            "EVERY 20 TICKS DO INPUT 3 FROM source OUTPUT 15 REDSTONE LEVEL TO signal "
                + "OUTPUT 4 TO dest FORGET source END");
        place(previous);
        Result added = BlueprintLayoutMatcher.match(previous, inserted);
        assertEquals(4, added.reusedNodes);
        assertEquals(1, added.freshNodes);
        assertCurrent(inserted, added);
        Node signal = only(added.graph, Type.REDSTONE);
        assertFalse(ids(previous).contains(signal.id));
        Node originalSignal = only(inserted, Type.REDSTONE);
        assertEquals(originalSignal.x(), signal.x());
        assertEquals(originalSignal.y(), signal.y());
        BlueprintGraph deleted = graph("EVERY 20 TICKS DO INPUT 3 FROM source FORGET source END");
        Result removed = BlueprintLayoutMatcher.match(previous, deleted);
        assertEquals(3, removed.reusedNodes);
        assertNull(removed.graph.node(only(previous, Type.OUTPUT).id));
        assertCurrent(deleted, removed);
    }

    @Test
    void uniqueStatementReorderPreservesIdentityButUsesTheNewExecutionOrder() {
        BlueprintGraph previous = graph("EVERY 20 TICKS DO INPUT 3 FROM source OUTPUT 4 TO dest FORGET source END");
        BlueprintGraph current = graph("EVERY 20 TICKS DO FORGET source INPUT 3 FROM source OUTPUT 4 TO dest END");
        place(previous);
        Result result = BlueprintLayoutMatcher.match(previous, current);
        assertEquals(4, result.reusedNodes);
        assertCurrent(current, result);
        assertEquals(
            Type.FORGET,
            result.graph.next(
                result.graph.triggers()
                    .get(0).id,
                Pin.NEXT).type);
        assertPositions(previous, result);
    }

    @Test
    void identicalBranchDuplicatesResetInsteadOfGuessingByPosition() {
        String statements = "IF TRUE THEN INPUT FROM source END IF TRUE THEN INPUT FROM source END";
        BlueprintGraph previous = graph("EVERY 20 TICKS DO " + statements + " END");
        BlueprintGraph current = graph("EVERY 40 TICKS DO " + statements + " END");
        place(previous);
        Result result = BlueprintLayoutMatcher.match(previous, current);
        assertEquals(1, result.reusedNodes);
        assertEquals(4, result.freshNodes);
        assertEquals(2, result.ambiguousNodes);
        for (Node node : previous.nodes()) if (!node.type.isTrigger()) assertNull(result.graph.node(node.id));
        assertCurrent(current, result);
    }

    @Test
    void deletingOneOfTwoIdenticalStatementsCannotAttachTheDeletedStatementsLayout() {
        BlueprintGraph previous = graph("EVERY 20 TICKS DO INPUT FROM source INPUT FROM source END");
        BlueprintGraph current = graph("EVERY 20 TICKS DO INPUT FROM source END");
        Result result = BlueprintLayoutMatcher.match(previous, current);
        assertEquals(1, result.reusedNodes);
        assertEquals(1, result.ambiguousNodes);
        assertFalse(ids(previous).contains(only(result.graph, Type.INPUT).id));
        assertCurrent(current, result);
    }

    @Test
    void matchingStaysWithinBranchRolesAndDoesNotReuseCrossBranchMoves() {
        BlueprintGraph previous = graph("EVERY 20 TICKS DO IF TRUE THEN INPUT FROM source ELSE OUTPUT TO dest END END");
        BlueprintGraph current = graph("EVERY 20 TICKS DO IF TRUE THEN OUTPUT TO dest ELSE INPUT FROM source END END");
        Result result = BlueprintLayoutMatcher.matchExact(previous, current);
        assertEquals(2, result.reusedNodes, "An incorrect exact-mode request must fall back to conservative matching");
        assertNull(result.graph.node(only(previous, Type.INPUT).id));
        assertNull(result.graph.node(only(previous, Type.OUTPUT).id));
        assertCurrent(current, result);
    }

    @Test
    void exactModePreservesDuplicateNodesOnlyAfterCheckingCompleteOrderedStructure() {
        String source = "EVERY 20 TICKS DO IF TRUE THEN INPUT FROM source END IF TRUE THEN INPUT FROM source END END";
        BlueprintGraph previous = graph(source), current = graph(source);
        place(previous);
        Result exact = BlueprintLayoutMatcher.matchExact(previous, current);
        assertEquals(
            previous.nodes()
                .size(),
            exact.reusedNodes);
        assertEquals(0, exact.ambiguousNodes);
        assertPositions(previous, exact);
        assertCurrent(current, exact);
        BlueprintGraph edited = graph(source.replace("INPUT FROM source", "INPUT 2 FROM source"));
        Result fallback = BlueprintLayoutMatcher.matchExact(previous, edited);
        assertEquals(1, fallback.reusedNodes);
        assertEquals(4, fallback.freshNodes);
        assertCurrent(edited, fallback);
    }

    @Test
    void triggerReorderCanUseDistinctBodyEvidenceEvenWhenHeadersAreIdentical() {
        BlueprintGraph previous = graph("EVERY 20 TICKS DO INPUT FROM source END EVERY 20 TICKS DO OUTPUT TO dest END");
        BlueprintGraph current = graph("EVERY 20 TICKS DO OUTPUT TO dest END EVERY 20 TICKS DO INPUT FROM source END");
        Result result = BlueprintLayoutMatcher.match(previous, current);
        assertEquals(4, result.reusedNodes);
        assertEquals(
            previous.triggers()
                .get(1).id,
            result.graph.triggers()
                .get(0).id);
        assertCurrent(current, result);
    }

    @Test
    void adversarialCurrentIdsCannotShadowReusedIdsAndFallbackIdsAreDeterministic() {
        BlueprintGraph previous = graph("EVERY 20 TICKS DO INPUT FROM source END");
        BlueprintGraph current = graph("EVERY 20 TICKS DO INPUT FROM source END");
        Node forget = new Node(only(previous, Type.INPUT).id, Type.FORGET, 71, 93, "FORGET");
        current.add(forget);
        current.connect(only(current, Type.INPUT).id, Pin.NEXT, forget.id);
        Result first = BlueprintLayoutMatcher.match(previous, current),
            second = BlueprintLayoutMatcher.match(previous, current);
        assertEquals(2, first.reusedNodes);
        assertEquals(3, ids(first.graph).size());
        assertFalse(ids(previous).contains(only(first.graph, Type.FORGET).id));
        assertEquals(only(first.graph, Type.FORGET).id, only(second.graph, Type.FORGET).id);
        assertEquals(71, only(first.graph, Type.FORGET).x());
        assertEquals(93, only(first.graph, Type.FORGET).y());
        assertCurrent(current, first);
    }

    @Test
    void boundedMaximumGraphMatchesAndDisconnectedCandidatesAreRejectedWithoutMutatingCurrent() {
        StringBuilder source = new StringBuilder("EVERY 20 TICKS DO ");
        for (int i = 0; i < 255; i++) source.append("INPUT ")
            .append(i + 1)
            .append(" FROM source ");
        source.append("END");
        BlueprintGraph previous = graph(source.toString()), current = graph(source.toString());
        Result result = BlueprintLayoutMatcher.matchExact(previous, current);
        assertEquals(BlueprintGraph.MAX_NODES, result.reusedNodes);
        assertCurrent(current, result);
        BlueprintGraph invalid = previous.copy();
        Node disconnected = new ArrayList<>(invalid.nodes()).get(1);
        invalid.disconnectInput(disconnected.id);
        String before = BlueprintCompiler.generate(current);
        assertThrows(IllegalArgumentException.class, () -> BlueprintLayoutMatcher.match(invalid, current));
        assertEquals(before, BlueprintCompiler.generate(current));
    }

    private static BlueprintGraph graph(String source) {
        return BlueprintCompiler.importText(source);
    }

    private static Node only(BlueprintGraph graph, Type type) {
        List<Node> nodes = graph.nodes()
            .stream()
            .filter(node -> node.type == type)
            .collect(Collectors.toList());
        assertEquals(1, nodes.size());
        return nodes.get(0);
    }

    private static Set<UUID> ids(BlueprintGraph graph) {
        return graph.nodes()
            .stream()
            .map(node -> node.id)
            .collect(Collectors.toCollection(HashSet::new));
    }

    private static void place(BlueprintGraph graph) {
        int index = 0;
        for (Node node : graph.nodes()) node.setPosition(-500.25 + index * 37, 800.5 - index++ * 29);
    }

    private static void assertCurrent(BlueprintGraph current, Result result) {
        assertEquals(BlueprintCompiler.generate(current), BlueprintCompiler.generate(result.graph));
        assertEquals(
            current.nodes()
                .size(),
            result.graph.nodes()
                .size());
        assertEquals(
            current.edges()
                .size(),
            result.graph.edges()
                .size());
    }

    private static void assertPositions(BlueprintGraph previous, Result result) {
        for (UUID id : result.retainedIds.values()) {
            assertNotNull(result.graph.node(id));
            assertEquals(
                previous.node(id)
                    .x(),
                result.graph.node(id)
                    .x());
            assertEquals(
                previous.node(id)
                    .y(),
                result.graph.node(id)
                    .y());
        }
    }
}
