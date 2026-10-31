package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Type;
import ca.teamdman.sfm.client.blueprint.StudioFieldDraft;
import ca.teamdman.sfm.client.blueprint.StudioFieldFeedback;
import ca.teamdman.sfm.client.blueprint.StudioFieldFeedback.Snapshot;
import ca.teamdman.sfm.client.blueprint.StudioFieldPicker;
import ca.teamdman.sfm.client.blueprint.StudioIoAllowances.Part;
import ca.teamdman.sfm.client.blueprint.StudioNodeProperties;
import ca.teamdman.sfm.client.blueprint.StudioOreFilter;

class StudioFieldFeedbackTest {

    private static final UUID PROJECT = new UUID(18, 1037);
    private static final String IO = "OUTPUT 16 RETAIN 2 item:a OR item:b WITH #ingot* EXCEPT item:dirt,fluid:lava TO source,spare";

    private static Node node(Type type, String clause) {
        return new Node(UUID.randomUUID(), type, 0, 0, clause);
    }

    private static StudioFieldDraft draft(Node node) {
        return new StudioFieldDraft(
            PROJECT,
            node,
            "every 20 ticks do " + node.clause() + " end",
            StudioNodeProperties.inspect(node.type, node.clause()));
    }

    private static StudioFieldDraft io() {
        return draft(node(Type.OUTPUT, IO));
    }

    private static StudioFieldFeedback opened(StudioFieldDraft draft) {
        StudioFieldFeedback feedback = new StudioFieldFeedback();
        assertEquals("unrelated", feedback.open(draft, "unrelated"));
        return feedback;
    }

    private static void mutationInvalidates(Type type, String clause, Function<StudioFieldDraft, Boolean> mutation) {
        Node node = node(type, clause);
        StudioFieldDraft draft = draft(node);
        StudioFieldFeedback feedback = opened(draft);
        String footer = feedback.failedApply(draft, "last failure", "unrelated");
        Snapshot old = feedback.snapshot(draft);
        assertTrue(mutation.apply(draft));
        assertEquals("Fields changed; Apply to check.", feedback.changed(draft, footer));
        assertEquals("", feedback.message(draft));
        assertNull(feedback.snapshot(draft));
        assertFalse(feedback.current(old, draft));
        assertEquals(clause, node.clause());
        assertEquals(clause, draft.original);
    }

    @Test
    void failedApplyPublishesOwnedFooterAndExactDetailsGeneration() {
        StudioFieldDraft draft = io();
        StudioFieldFeedback feedback = opened(draft);
        String footer = feedback.failedApply(draft, "complete error", "unrelated");
        Snapshot snapshot = feedback.snapshot(draft);
        assertEquals("Fields kept: complete error", footer);
        assertEquals("complete error", snapshot.message);
        assertSame(draft, snapshot.owner);
        assertTrue(feedback.current(snapshot, draft));
        assertFalse(feedback.current(snapshot, new StudioFieldDraft(draft)));
        assertFalse(feedback.current(null, draft));
    }

    @Test
    void allScalarNumericAndGlobalMutationFamiliesInvalidateWithoutValidation() {
        mutationInvalidates(Type.PRINT, "PRINT(1)", draft -> draft.set("expression0", "unknown()"));
        mutationInvalidates(Type.OUTPUT, IO, draft -> draft.set("sides", "SOUTH"));
        mutationInvalidates(Type.OUTPUT, IO, draft -> draft.set("slots", "1-3"));
        mutationInvalidates(Type.OUTPUT, IO, draft -> draft.set("roundRobin", "block"));
        mutationInvalidates(Type.OUTPUT, IO, draft -> draft.set("each", "true"));
        mutationInvalidates(Type.OUTPUT, IO, draft -> draft.set("emptySlots", "true"));
        mutationInvalidates(Type.OUTPUT, IO, draft -> draft.set("amount", "7"));
        mutationInvalidates(Type.OUTPUT, IO, draft -> draft.set("retain", "3"));
        mutationInvalidates(Type.OUTPUT, IO, draft -> draft.set("amountEach", "true"));
        mutationInvalidates(Type.OUTPUT, IO, draft -> draft.set("retainEach", "true"));
        for (Part part : Part.values()) {
            mutationInvalidates(Type.OUTPUT, IO, draft -> draft.setAllowanceEnabled(part, false));
            mutationInvalidates(Type.OUTPUT, IO, draft -> draft.setAllowanceText(part, "\ninvalid pending"));
            mutationInvalidates(Type.OUTPUT, IO, draft -> draft.setAllowanceEach(part, true));
        }
        mutationInvalidates(Type.OUTPUT, IO, draft -> draft.setOreMode(StudioOreFilter.Mode.WITHOUT));
        mutationInvalidates(Type.OUTPUT, IO, draft -> draft.setOrePattern("?invalid"));
    }

    @Test
    void allRowMutationFamiliesInvalidateAndDoNotApplySource() {
        mutationInvalidates(
            Type.OUTPUT,
            IO,
            draft -> draft.setLabel(
                draft.labelRows()
                    .get(0).id,
                ""));
        mutationInvalidates(Type.OUTPUT, IO, draft -> {
            draft.addLabel("new_label");
            return true;
        });
        mutationInvalidates(Type.OUTPUT, IO, draft -> {
            draft.removeLabel(
                draft.labelRows()
                    .get(0).id);
            return true;
        });
        mutationInvalidates(
            Type.OUTPUT,
            IO,
            draft -> draft.setResource(
                draft.resourceRows()
                    .get(0).id,
                ""));
        mutationInvalidates(Type.OUTPUT, IO, draft -> {
            draft.addResource("");
            return true;
        });
        mutationInvalidates(Type.OUTPUT, IO, draft -> {
            draft.removeResource(
                draft.resourceRows()
                    .get(0).id);
            return true;
        });
        mutationInvalidates(
            Type.OUTPUT,
            IO,
            draft -> draft.setExclusion(
                draft.exclusionRows()
                    .get(0).id,
                ""));
        mutationInvalidates(Type.OUTPUT, IO, draft -> {
            draft.addExclusion("");
            return true;
        });
        mutationInvalidates(Type.OUTPUT, IO, draft -> {
            draft.removeExclusion(
                draft.exclusionRows()
                    .get(0).id);
            return true;
        });
        String multiple = "OUTPUT 4 item:a,8 item:b TO source";
        mutationInvalidates(
            Type.OUTPUT,
            multiple,
            draft -> draft.setLimit(
                draft.limitRows()
                    .get(0).id,
                "invalid pending"));
        mutationInvalidates(Type.OUTPUT, multiple, draft -> {
            draft.addLimit("");
            return true;
        });
        mutationInvalidates(Type.OUTPUT, multiple, draft -> {
            draft.removeLimit(
                draft.limitRows()
                    .get(0).id);
            return true;
        });
    }

    @Test
    void noOpAndRejectedMutationsKeepExistingErrorFooterAndSnapshot() {
        StudioFieldDraft draft = io();
        StudioFieldFeedback feedback = opened(draft);
        String footer = feedback.failedApply(draft, "last failure", "unrelated");
        Snapshot snapshot = feedback.snapshot(draft);
        assertFalse(draft.setAllowanceText(Part.AMOUNT, "16"));
        assertFalse(draft.setAllowanceEnabled(Part.AMOUNT, true));
        assertThrows(IllegalArgumentException.class, () -> draft.removeResource(UUID.randomUUID()));
        feedback.rejected(draft, "rejected mutation");
        assertEquals("last failure", feedback.message(draft));
        assertTrue(feedback.current(snapshot, draft));
        assertEquals("Fields changed; Apply to check.", feedback.changed(draft, footer));
    }

    @Test
    void unrelatedNewerStatusesIncludingIdenticalTextSurviveEveryCleanupPath() {
        for (int action = 0; action < 3; action++) {
            StudioFieldDraft draft = io();
            StudioFieldFeedback feedback = opened(draft);
            String published = feedback.failedApply(draft, "last failure", "unrelated");
            String newer = new String(published);
            assertEquals(published, newer);
            assertNotSame(published, newer);
            String result = action == 0 ? feedback.changed(draft, newer)
                : action == 1 ? feedback.finish(draft, newer, "Fields canceled.")
                    : feedback.open(new StudioFieldDraft(draft), newer);
            assertSame(newer, result);
        }
        StudioFieldDraft draft = io();
        StudioFieldFeedback feedback = opened(draft);
        feedback.failedApply(draft, "failure", "older");
        assertEquals("New server reply", feedback.changed(draft, "New server reply"));
    }

    @Test
    void staleCallbacksAndSameOwnerCopiesCannotClearOrPublishCurrentFeedback() {
        StudioFieldDraft old = io(), current = new StudioFieldDraft(old);
        StudioFieldFeedback feedback = opened(old);
        String oldFooter = feedback.failedApply(old, "old failure", "initial");
        feedback.open(current, oldFooter);
        String currentFooter = feedback.failedApply(current, "current failure", "initial");
        Snapshot snapshot = feedback.snapshot(current);
        assertSame(currentFooter, feedback.changed(old, currentFooter));
        assertSame(currentFooter, feedback.finish(old, currentFooter, "closed"));
        assertSame(currentFooter, feedback.failedApply(old, "late", currentFooter));
        feedback.rejected(old, "late rejection");
        assertEquals("", feedback.message(old));
        assertNull(feedback.snapshot(old));
        assertTrue(feedback.current(snapshot, current));
    }

    @Test
    void latestFailedApplyReplacesDetailsEvenWhenMessageContentsMatch() {
        StudioFieldDraft draft = io();
        StudioFieldFeedback feedback = opened(draft);
        String first = feedback.failedApply(draft, "same error", "initial");
        Snapshot old = feedback.snapshot(draft);
        String second = feedback.failedApply(draft, "same error", first);
        assertEquals(first, second);
        assertNotSame(first, second);
        assertFalse(feedback.current(old, draft));
        assertTrue(feedback.current(feedback.snapshot(draft), draft));
    }

    @Test
    void cancelNoOpApplyAndReopenClearOnlyThePublishedFooter() {
        for (String completion : new String[] { "Fields canceled.", "Fields applied.", "Fields closed." }) {
            StudioFieldDraft draft = io();
            StudioFieldFeedback feedback = opened(draft);
            String footer = feedback.failedApply(draft, "failure", "initial");
            Snapshot old = feedback.snapshot(draft);
            assertEquals(completion, feedback.finish(draft, footer, completion));
            assertFalse(feedback.current(old, draft));
            assertEquals("", feedback.message(draft));
            assertEquals("different status", feedback.failedApply(draft, "late", "different status"));
        }
        StudioFieldDraft draft = io();
        StudioFieldFeedback feedback = opened(draft);
        String footer = feedback.failedApply(draft, "failure", "initial");
        Snapshot old = feedback.snapshot(draft);
        assertEquals("Fields reopened; Apply to check.", feedback.open(draft, footer));
        assertFalse(feedback.current(old, draft));
        assertNull(feedback.snapshot(draft));
    }

    @Test
    void acceptedInvalidTextClearsLastAttemptButNextApplyCanReportItAgain() {
        Node node = node(Type.INPUT, "INPUT FROM source");
        StudioFieldDraft draft = draft(node);
        StudioFieldFeedback feedback = opened(draft);
        draft.setAllowanceEnabled(Part.AMOUNT, true);
        IllegalArgumentException first = assertThrows(
            IllegalArgumentException.class,
            () -> draft.appliedClause(PROJECT, node, draft.source));
        String footer = feedback.failedApply(draft, first.getMessage(), "initial");
        assertTrue(draft.setAllowanceText(Part.AMOUNT, "still invalid"));
        assertEquals("Fields changed; Apply to check.", feedback.changed(draft, footer));
        assertNull(feedback.snapshot(draft));
        IllegalArgumentException next = assertThrows(
            IllegalArgumentException.class,
            () -> draft.appliedClause(PROJECT, node, draft.source));
        assertEquals("Fields kept: " + next.getMessage(), feedback.failedApply(draft, next.getMessage(), "initial"));
        assertEquals("INPUT FROM source", node.clause());
    }

    @Test
    void pickerChooseAndNoOpChooseFollowTheSameMutationContract() {
        Node node = node(Type.INPUT, "INPUT item:a FROM source");
        StudioFieldDraft draft = draft(node);
        StudioFieldFeedback feedback = opened(draft);
        String footer = feedback.failedApply(draft, "last error", "initial");
        StudioFieldPicker.Target target = StudioFieldPicker.resourceRowTarget(
            draft,
            draft.resourceRows()
                .get(0).id);
        StudioFieldPicker.Snapshot items = StudioFieldPicker.items(Collections.singletonList("minecraft:coal"), target);
        StudioFieldPicker.Session picker = new StudioFieldPicker.Session(draft, target, items);
        picker.select(items.search("coal").entries.get(0));
        assertTrue(picker.choose(draft, PROJECT, node, draft.source));
        assertEquals("Fields changed; Apply to check.", feedback.changed(draft, footer));
        footer = feedback.failedApply(draft, "new failure", "initial");
        Snapshot snapshot = feedback.snapshot(draft);
        target = StudioFieldPicker.resourceRowTarget(
            draft,
            draft.resourceRows()
                .get(0).id);
        items = StudioFieldPicker.items(Collections.singletonList("minecraft:coal"), target);
        picker = new StudioFieldPicker.Session(draft, target, items);
        picker.select(items.search("coal").entries.get(0));
        assertFalse(picker.choose(draft, PROJECT, node, draft.source));
        assertTrue(feedback.current(snapshot, draft));
        assertEquals("Fields kept: new failure", footer);
    }

    @Test
    void promotionAndRecoveryReplaceOwnersWithoutRestoringOldDetails() {
        Node node = node(Type.INPUT, "INPUT 8 item:a FROM source");
        StudioFieldDraft draft = draft(node);
        StudioFieldFeedback feedback = opened(draft);
        String footer = feedback.failedApply(draft, "last failure", "initial");
        Snapshot old = feedback.snapshot(draft);
        StudioFieldDraft promoted = draft.addIndependentLimit(PROJECT, node, draft.source);
        assertEquals("promoted", feedback.finish(draft, footer, "promoted"));
        feedback.open(promoted, "Independent rows");
        assertFalse(feedback.current(old, promoted));
        String rowFailure = feedback.failedApply(promoted, "blank second row", "Independent rows");
        Snapshot rowDetails = feedback.snapshot(promoted);
        StudioFieldDraft recovered = new StudioFieldDraft(promoted);
        assertEquals("Fields reopened; Apply to check.", feedback.open(recovered, rowFailure));
        assertFalse(feedback.current(rowDetails, recovered));
        assertTrue(recovered.hasLimitRows());
        assertEquals(
            "",
            recovered.limitRows()
                .get(1).text);
        assertEquals("INPUT 8 item:a FROM source", node.clause());
    }

    @Test
    void missingOwnersCannotCreateFeedbackAndInlineRejectionDoesNotOwnStatus() {
        StudioFieldFeedback feedback = new StudioFieldFeedback();
        feedback.rejected(null, "ignored");
        assertEquals("unrelated", feedback.failedApply(null, "ignored", "unrelated"));
        assertNull(feedback.snapshot(null));
        assertThrows(IllegalArgumentException.class, () -> feedback.open(null, "unrelated"));
        StudioFieldDraft draft = io();
        feedback.open(draft, "unrelated");
        feedback.rejected(draft, "cannot open picker");
        assertEquals("cannot open picker", feedback.message(draft));
        assertEquals("unrelated", feedback.finish(draft, "unrelated", "canceled"));
    }

    @Test
    void repairThenCancelRetiresOwnedNeutralButNotNewerIdenticalNeutralText() {
        for (boolean newer : new boolean[] { false, true }) {
            StudioFieldDraft draft = io();
            StudioFieldFeedback feedback = opened(draft);
            String footer = feedback.failedApply(draft, "failure", "initial");
            String neutral = feedback.changed(draft, footer);
            assertEquals("Fields changed; Apply to check.", neutral);
            String current = newer ? new String(neutral) : neutral;
            String result = feedback.finish(draft, current, "Fields canceled.");
            if (newer) assertSame(current, result);
            else assertEquals("Fields canceled.", result);
            assertSame(result, feedback.finish(draft, result, "late"));
        }
        StudioFieldDraft draft = io();
        StudioFieldFeedback feedback = opened(draft);
        String footer = feedback.failedApply(draft, "failure", "initial");
        String reopened = feedback.open(draft, feedback.changed(draft, footer));
        assertEquals("Fields reopened; Apply to check.", reopened);
        assertEquals("Fields canceled.", feedback.finish(draft, reopened, "Fields canceled."));
    }
}
