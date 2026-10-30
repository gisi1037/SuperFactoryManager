package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ca.teamdman.sfm.client.gui.mui2.StudioLayout;
import ca.teamdman.sfm.client.gui.mui2.StudioProjectMemory;
import ca.teamdman.sfm.client.gui.mui2.StudioTemplates;
import ca.teamdman.sfm.client.gui.syntax.SnippetCategory;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class StudioWorkspaceTest {

    @TempDir
    Path directory;

    @Test
    void projectsRememberedAcrossRestartStayIsolatedByWorldServerAndPlayer() {
        Path path = directory.resolve("studio.properties");
        UUID player = UUID.randomUUID(), other = UUID.randomUUID(), project = UUID.randomUUID();
        String scope = StudioProjectMemory.scope("server:example.org:25565", player);
        new StudioProjectMemory(path).remember(scope, project);
        StudioProjectMemory reopened = new StudioProjectMemory(path);
        assertEquals(project, reopened.selected(scope));
        assertNull(reopened.selected(StudioProjectMemory.scope("server:example.org:25565", other)));
        assertNull(reopened.selected(StudioProjectMemory.scope("world:example.org:25565", player)));
        assertNull(reopened.selected(StudioProjectMemory.scope("server:other.example.org:25565", player)));
        reopened.remember(scope, null);
        assertNull(
            new StudioProjectMemory(path).selected(scope),
            "New draft clears the selected saved-project preference");
    }

    @Test
    void invalidOrOversizedPreferencesCannotSupplySourceOrCrashAuthoring() throws Exception {
        Path path = directory.resolve("corrupt.properties");
        Files.write(path, "scope=not-a-project\n".getBytes(StandardCharsets.UTF_8));
        assertNull(new StudioProjectMemory(path).selected("scope"));
        Files.write(path, new byte[65537]);
        assertNull(new StudioProjectMemory(path).selected("scope"));
        Files.write(path, "bad=\\uNOTA".getBytes(StandardCharsets.UTF_8));
        assertDoesNotThrow(() -> new StudioProjectMemory(path));
    }

    @Test
    void resizedWorkspaceKeepsDockSidebarAndEveryExpandedInsertCategoryInsideTheScreen() {
        int categories = SnippetCategory.values().length + 1;
        for (int width : new int[] { 320, 427, 640, 854, 960, 1280, 1920 })
            for (int height : new int[] { 240, 360, 540, 1080 })
                for (int sidebar : new int[] { -999, 76, 154, 260, 9999 })
                    for (double ratio : new double[] { -5, 0, .28, .9, 5, Double.NaN }) {
                        StudioLayout.Workspace layout = StudioLayout
                            .workspace(width, height, sidebar, ratio, categories);
                        assertTrue(layout.sidebar >= 76 && layout.sidebar <= width / 3);
                        assertTrue(layout.mainLeft > layout.sidebar);
                        assertTrue(layout.editorTop < layout.dockTop - 5, width + "x" + height);
                        assertTrue(layout.dockTop + layout.dockHeight <= height - 18);
                        assertTrue(layout.dockHeight >= 28);
                        int count = layout.compact ? 2 : categories;
                        for (int i = 0; i < count; i++) {
                            assertTrue(layout.insertX(i) >= 14);
                            assertTrue(layout.insertX(i) + layout.insertWidth <= width - 8);
                            assertTrue(layout.insertY(i) + 16 < layout.viewTop);
                        }
                    }
    }

    @Test
    void everySidebarTemplateIsACompleteCompilableProgram() {
        for (StudioTemplates.Template template : StudioTemplates.Template.values()) {
            ProgramBuildResult result = new ProgramBuilder(template.source).build();
            assertTrue(result.isSuccess(), template.title + ": " + result.getErrorMessage());
        }
    }
}
