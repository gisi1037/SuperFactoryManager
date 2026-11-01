package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.gui.ProgrammingStationDraftState;
import ca.teamdman.sfm.client.gui.mui2.SfmlEditorBuffer;
import ca.teamdman.sfm.client.gui.mui2.StudioLibraryImports;
import ca.teamdman.sfm.client.gui.mui2.StudioTemplates;
import ca.teamdman.sfm.common.net.PacketStationRequest.Action;
import ca.teamdman.sfm.common.program.project.LibraryImport;
import ca.teamdman.sfm.common.program.project.LibraryModule;
import ca.teamdman.sfm.common.program.project.ProjectDraft;
import ca.teamdman.sfm.common.program.project.ProjectValidation;
import ca.teamdman.sfm.common.program.project.PublicationReceipt;
import ca.teamdman.sfm.common.program.project.PublishedLibrary;
import ca.teamdman.sfm.common.program.project.PublishedProgram;
import ca.teamdman.sfml.program_builder.BundleCompiler;
import ca.teamdman.sfml.program_builder.LibraryImports;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class StudioLibraryAuthoringTest {

    private static final UUID OWNER = new UUID(1, 2), CURRENT = new UUID(3, 4), LIBRARY = new UUID(5, 6);
    private static final String DIGEST = String.join("", Collections.nCopies(64, "a"));

    @Test
    void exactImportFollowsNameAndItsCommentWithoutChangingAnyExistingText() {
        String source = "-- kept header\nNAME \"Factory \uD83C\uDFED\" -- name comment\nSTATE answer = 0\nEVERY 20 TICKS DO answer = math.twice(21) END\n";
        StudioLibraryImports.Insertion insertion = StudioLibraryImports.prepare(source, "math", receipt());
        assertEquals(source.indexOf("STATE"), insertion.offset);
        assertEquals("import math from \"" + LIBRARY + "\" revision 3 digest \"" + DIGEST + "\";\n", insertion.text);
        String inserted = apply(source, insertion);
        assertTrue(inserted.startsWith("-- kept header\nNAME \"Factory \uD83C\uDFED\" -- name comment\nimport math"));
        assertEquals(
            source,
            inserted.substring(0, insertion.offset) + inserted.substring(insertion.offset + insertion.text.length()));
        LibraryImport reference = LibraryImports.parse(inserted)
            .get(0);
        assertEquals("math", reference.alias);
        assertEquals(LIBRARY, reference.project);
        assertEquals(3, reference.revision);
        assertEquals(DIGEST, reference.digest);
    }

    @Test
    void sameLineNameLeadingCommentsAndCommentOnlyDraftsStayValidHeaders() {
        for (String source : new String[] { "NAME \"same line\" EVERY 20 TICKS DO END",
            "-- kept\nEVERY 20 TICKS DO END", "-- comment without newline", "NAME \"empty\" -- kept", "" }) {
            StudioLibraryImports.Insertion insertion = StudioLibraryImports.prepare(source, "Math_tools", receipt());
            String candidate = apply(source, insertion);
            assertEquals(
                1,
                LibraryImports.parse(candidate)
                    .size(),
                source);
            assertTrue(candidate.contains("import Math_tools from"));
        }
    }

    @Test
    void insertionIsOneUndoAndTheExactBundleCompilesWhileLocalLinkingStaysUnresolved() {
        PublishedLibrary library = new PublishedLibrary(StudioTemplates.Template.LIBRARY.source);
        PublicationReceipt receipt = new PublicationReceipt(OWNER, LIBRARY, 3, library.digest(), "Math library");
        String source = "NAME \"factory\"\nSTATE answer = 0\nEVERY 20 TICKS DO answer = math.twice(21) END\n";
        SfmlEditorBuffer buffer = new SfmlEditorBuffer();
        buffer.load(source);
        StudioLibraryImports.Insertion insertion = StudioLibraryImports.prepare(source, "math", receipt);
        buffer.select(insertion.offset, insertion.offset);
        buffer.insert(insertion.text);
        assertFalse(
            new ProgramBuilder(buffer.source()).build()
                .isSuccess());
        PublishedProgram bundle = new PublishedProgram(
            buffer.source(),
            Collections
                .singletonList(new LibraryModule(new LibraryImport("math", LIBRARY, 3, library.digest()), library)));
        ca.teamdman.sfml.program_builder.ProgramBuildResult linked = BundleCompiler.build(bundle);
        assertTrue(linked.isSuccess(), linked.getErrorMessage());
        assertTrue(buffer.undo());
        assertEquals(source, buffer.source());
        assertFalse(buffer.undo());
    }

    @Test
    void invalidAliasesDuplicateAliasesOrReferencesAndOversizeDraftsAreRejectedBeforeEditing() {
        String source = "EVERY 20 TICKS DO END";
        for (String alias : new String[] { "", "1math", "math.tools", " math", "a\"b",
            String.join("", Collections.nCopies(33, "a")) })
            assertThrows(IllegalArgumentException.class, () -> StudioLibraryImports.prepare(source, alias, receipt()));
        String existing = apply(source, StudioLibraryImports.prepare(source, "math", receipt()));
        assertThrows(IllegalArgumentException.class, () -> StudioLibraryImports.prepare(existing, "math", receipt()));
        assertThrows(IllegalArgumentException.class, () -> StudioLibraryImports.prepare(existing, "other", receipt()));
        String large = "--" + String.join("", Collections.nCopies(29900, "x")) + "\nEVERY 20 TICKS DO END\n";
        assertThrows(IllegalArgumentException.class, () -> StudioLibraryImports.prepare(large, "math", receipt()));
    }

    @Test
    void browsingAndLibraryPublicationKeepProgramReceiptsSelectionAndDraftIdentitySeparate() {
        ProgrammingStationDraftState state = loaded();
        PublicationReceipt program = new PublicationReceipt(OWNER, CURRENT, 2, DIGEST, "Program");
        state.publication = program;
        state.selectBuild(program);
        ProjectDraft saved = state.saved;
        ProgrammingStationDraftState.DraftSnapshot snapshot = state.snapshot();
        long list = state.begin(Action.LIST_LIBRARY_PUBLICATIONS);
        ProjectDraft browsed = new ProjectDraft(LIBRARY, OWNER, 3, "Library", "integer f() { return 1; }");
        assertTrue(state.receive(list, Action.LIST_LIBRARY_PUBLICATIONS, true, browsed, null));
        assertSame(saved, state.saved);
        assertTrue(state.current(snapshot));
        assertEquals(saved.source, state.source);
        long publish = state.begin(Action.PUBLISH_LIBRARY);
        PublicationReceipt library = new PublicationReceipt(OWNER, CURRENT, 2, DIGEST, "Function library");
        assertFalse(state.receive(publish, Action.PUBLISH, true, null, null, library));
        assertTrue(state.receive(publish, Action.PUBLISH_LIBRARY, true, null, null, library));
        assertSame(library, state.libraryPublication);
        assertSame(program, state.publication);
        assertSame(program, state.selectedBuild());
        assertSame(saved, state.saved);
        assertTrue(state.current(snapshot));
        state.newDraft();
        assertNull(state.libraryPublication);
        assertNull(state.publication);
        assertNull(state.selectedBuild());
    }

    @Test
    void publishRequiresCleanSavedRevisionAndPickerSnapshotRejectsEditsUndoAndProjectChanges() {
        ProgrammingStationDraftState uncreated = new ProgrammingStationDraftState();
        assertThrows(IllegalStateException.class, () -> uncreated.begin(Action.PUBLISH_LIBRARY));
        uncreated.source = "unsaved work";
        long list = uncreated.begin(Action.LIST_LIBRARY_PUBLICATIONS);
        assertTrue(uncreated.receive(list, Action.LIST_LIBRARY_PUBLICATIONS, true, null, null));
        assertEquals("unsaved work", uncreated.source);
        ProgrammingStationDraftState state = loaded();
        ProgrammingStationDraftState loaded = state;
        ProgrammingStationDraftState.DraftSnapshot snapshot = state.snapshot();
        state.source += " -- edit";
        state.noteLocalEdit();
        assertFalse(state.current(snapshot));
        assertThrows(IllegalStateException.class, () -> loaded.begin(Action.PUBLISH_LIBRARY));
        state.source = state.saved.source;
        assertFalse(state.current(snapshot), "Undoing text does not revive an older picker session");
        state.uncertainWrite = true;
        assertThrows(IllegalStateException.class, () -> loaded.begin(Action.PUBLISH_LIBRARY));
        state.uncertainWrite = false;
        snapshot = state.snapshot();
        state.saved = new ProjectDraft(new UUID(7, 8), OWNER, 2, state.name, state.source);
        assertFalse(state.current(snapshot));
    }

    @Test
    void linkedValidationDescribesOnlyTheExactCleanSavedDraft() {
        ProgrammingStationDraftState state = loaded();
        state.validation = new ProjectValidation(CURRENT, 2, true, "Exact linked revision checked");
        assertSame(state.validation, state.currentValidation());
        state.source += " -- local edit";
        assertNull(state.currentValidation());
        assertFalse(
            state.revisionLine()
                .contains("Validated"));
        state.source = state.saved.source;
        state.name += " changed";
        assertNull(state.currentValidation());
        state.name = state.saved.name;
        state.validation = new ProjectValidation(CURRENT, 1, true, "old revision");
        assertNull(state.currentValidation());
        state.validation = new ProjectValidation(LIBRARY, 2, true, "other project");
        assertNull(state.currentValidation());
        assertTrue(
            BundleCompiler.validateLibrary(StudioTemplates.Template.LIBRARY.source)
                .isSuccess());
    }

    private static ProgrammingStationDraftState loaded() {
        ProgrammingStationDraftState state = new ProgrammingStationDraftState();
        state.saved = new ProjectDraft(CURRENT, OWNER, 2, "Current", StudioTemplates.Template.LIBRARY.source);
        state.name = state.saved.name;
        state.source = state.saved.source;
        return state;
    }

    private static PublicationReceipt receipt() {
        return new PublicationReceipt(OWNER, LIBRARY, 3, DIGEST, "Math library");
    }

    private static String apply(String source, StudioLibraryImports.Insertion insertion) {
        return source.substring(0, insertion.offset) + insertion.text + source.substring(insertion.offset);
    }
}
