package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.gui.RecipeInspectorState;
import ca.teamdman.sfm.client.gui.RecipeSelectionHint.Value;
import ca.teamdman.sfm.common.net.PacketStationRecipeReply;
import ca.teamdman.sfm.common.net.PacketStationRecipeRequest;
import ca.teamdman.sfml.data.recipe.RecipeCatalog;

class RecipeInspectorStateTest {

    @Test
    void displayedPreviewClearsForAnotherSelectionTimeoutFreshLookupAndClose() {
        RecipeInspectorState state = state();
        PacketStationRecipeRequest search = state.search(OUTPUT);
        state.receive(reply(search, entry(1), entry(2)));
        PacketStationRecipeRequest first = state.select(0);
        PacketStationRecipeReply inspected = reply(first, entry(1));
        inspected.detail = "First recipe\nIts ingredient";
        state.receive(inspected);
        assertEquals(2, state.detailLineCount());
        assertEquals("First recipe", state.detailLine(0));
        PacketStationRecipeRequest second = state.select(1);
        assertEquals(0, state.detailLineCount());
        assertEquals("", state.detail());
        for (int i = 0; i < RecipeInspectorState.TIMEOUT_TICKS; i++) state.tick();
        assertEquals(0, state.detailLineCount());
        assertFalse(state.receive(reply(second, entry(2))));
        first = state.select(0);
        inspected = reply(first, entry(1));
        inspected.detail = "First recipe";
        state.receive(inspected);
        assertEquals(1, state.detailLineCount());
        search = state.search(OUTPUT);
        assertEquals(0, state.detailLineCount());
        state.receive(reply(search, entry(1)));
        first = state.select(0);
        inspected = reply(first, entry(1));
        inspected.detail = "First recipe";
        state.receive(inspected);
        state.cancel();
        assertEquals(0, state.detailLineCount());
        assertEquals("", state.detailLine(0));
        assertFalse(state.canCopy());
    }

    private static final Value OUTPUT = new Value("minecraft:flint", 0, "gt.recipe.macerator");
    private static final UUID CATALOG = UUID.randomUUID();

    private static PacketStationRecipeReply.Entry entry(int id) {
        return new PacketStationRecipeReply.Entry(
            new RecipeCatalog.Token(CATALOG, 1, "fixture", OUTPUT.map, String.format("%064x", id)),
            "Candidate " + id);
    }

    private static PacketStationRecipeReply reply(PacketStationRecipeRequest request,
        PacketStationRecipeReply.Entry... rows) {
        PacketStationRecipeReply reply = PacketStationRecipeReply.error(request, RecipeCatalog.Status.MATCHES, "");
        reply.accepted = true;
        reply.coverageComplete = true;
        reply.entries = Collections.unmodifiableList(Arrays.asList(rows));
        return reply;
    }

    private RecipeInspectorState state() {
        return new RecipeInspectorState(1, 2, 3, 19, UUID.randomUUID());
    }

    @Test
    void noAutomaticSelectionAndOnlyTheExplicitServerResolvedRowEnablesCopy() {
        RecipeInspectorState state = state();
        PacketStationRecipeRequest search = state.search(OUTPUT);
        assertTrue(state.receive(reply(search, entry(1), entry(2))));
        assertFalse(state.canCopy());
        assertThrows(IllegalStateException.class, state::copyQuery);
        PacketStationRecipeRequest select = state.select(1);
        assertTrue(state.busy());
        assertEquals(entry(2).reference.fingerprint, select.reference.fingerprint);
        assertTrue(state.receive(reply(select, entry(2))));
        assertTrue(state.canCopy());
        assertTrue(
            state.copyQuery()
                .contains(entry(2).reference.fingerprint));
        assertFalse(
            state.copyQuery()
                .contains(entry(1).reference.fingerprint));
    }

    @Test
    void changedSearchClosedInspectorExpiredRequestAndWrongRowCannotApplyDelayedSelection() {
        RecipeInspectorState state = state();
        PacketStationRecipeRequest old = state.search(OUTPUT);
        PacketStationRecipeRequest current = state.search(new Value("minecraft:sand", 0, OUTPUT.map));
        assertFalse(state.receive(reply(old, entry(1))));
        assertTrue(state.busy());
        assertTrue(state.receive(reply(current, entry(1), entry(2))));
        PacketStationRecipeRequest select = state.select(0);
        assertTrue(state.receive(reply(select, entry(2))));
        assertFalse(state.canCopy());
        select = state.select(0);
        state.cancel();
        assertFalse(state.receive(reply(select, entry(1))));
        PacketStationRecipeRequest pending = state.search(OUTPUT);
        for (int i = 0; i < RecipeInspectorState.TIMEOUT_TICKS; i++) state.tick();
        assertFalse(state.busy());
        assertFalse(state.receive(reply(pending, entry(1))));
        assertFalse(state.canCopy());
    }

    @Test
    void incompleteUnsupportedOrStaleServerViewsNeverEnableCopyAndNewRequestsRemainMonotonic() {
        RecipeInspectorState state = state();
        PacketStationRecipeRequest search = state.search(OUTPUT);
        state.receive(reply(search, entry(1)));
        PacketStationRecipeRequest select = state.select(0);
        PacketStationRecipeReply incomplete = reply(select, entry(1));
        incomplete.coverageComplete = false;
        state.receive(incomplete);
        assertFalse(state.canCopy());
        select = state.select(0);
        state.receive(PacketStationRecipeReply.error(select, RecipeCatalog.Status.STALE, "Stale"));
        assertFalse(state.canCopy());
        state.clear();
        PacketStationRecipeRequest next = state.search(OUTPUT);
        assertTrue(next.requestId > select.requestId);
        assertThrows(IllegalStateException.class, () -> state.select(0));
    }
}
