package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;

import net.minecraft.entity.player.InventoryPlayer;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.gui.ContainerManager;
import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.program.ProgramDocument;
import ca.teamdman.sfm.common.program.ProgramEditService;
import ca.teamdman.sfm.common.program.ProgramEditSnapshot;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfm.common.util.ProgramRevision;
import ca.teamdman.sfml.ast.InputStatement;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.ast.RoundRobin;

class ExecutableSaveReuseTest {

    private static final String SOURCE = "EVERY 20 TICKS DO INPUT 1 FROM source ROUND ROBIN BY BLOCK OUTPUT TO dest END";

    @Test
    void comparisonIgnoresOnlyNonExecutableTokensAndRejectsLexicalErrors() {
        assertTrue(
            ProgramRevision.sameExecutableTokens(SOURCE, "-- layout change\n" + SOURCE + "\n-- sfm:blueprint:v2 abc"));
        assertTrue(ProgramRevision.sameExecutableTokens(SOURCE, SOURCE.replace(" DO ", "\nDO\n  ")));
        assertFalse(ProgramRevision.sameExecutableTokens(SOURCE, SOURCE.replace("INPUT 1", "INPUT 2")));
        assertFalse(ProgramRevision.sameExecutableTokens(SOURCE, SOURCE.replace("EVERY", "every")));
        assertFalse(ProgramRevision.sameExecutableTokens(SOURCE, SOURCE.replace("source", "Source")));
        String quoted = "EVERY 20 TICKS DO INPUT FROM \"-- sfm:blueprint:v2 first\" END";
        assertFalse(ProgramRevision.sameExecutableTokens(quoted, quoted.replace("first", "second")));
        assertFalse(ProgramRevision.sameExecutableTokens(SOURCE, SOURCE + '\u0000'));
        assertFalse(ProgramRevision.sameExecutableTokens(SOURCE + '\u0000', SOURCE));
        assertFalse(ProgramRevision.sameExecutableTokens(SOURCE + '\u0000', SOURCE + '\u0000'));
        assertFalse(ProgramRevision.sameExecutableTokens(null, SOURCE));
    }

    @Test
    void commentOnlySaveAdvancesEditAuthorityAndRetainsTheLiveRoundRobinCursor() throws Exception {
        Fixture fixture = new Fixture(SOURCE);
        Program original = fixture.manager.getCompiledProgram();
        RoundRobin cursor = ((InputStatement) original.getTriggers()
            .get(0)
            .getBlock()
            .getStatements()
            .get(0)).getLabelAccess()
                .getRoundRobin();
        assertEquals(0, cursor.nextIndex(5));
        assertEquals(1, cursor.nextIndex(5));
        fixture.manager.setRunning(false);
        ProgramEditSnapshot before = ProgramEditService.open(fixture.container);
        String edited = "-- layout updated\n" + SOURCE + "\n-- sfm:blueprint:v2 changed-layout";
        ProgramEditService.SaveResult result = ProgramEditService.save(fixture.container, before, edited);
        assertTrue(result.accepted, result.message);
        assertSame(original, fixture.manager.getCompiledProgram());
        assertEquals(2, cursor.nextIndex(5), "A source-comment save must not reset mutable engine cursors");
        assertTrue(result.snapshot.revision > before.revision);
        assertTrue(result.snapshot.bindingGeneration > before.bindingGeneration);
        assertEquals(edited, ItemDisk.getProgramString(fixture.manager.getDisk()));
        assertFalse(fixture.manager.isRunning());
        assertFalse(ProgramEditService.save(fixture.container, before, SOURCE).accepted);
        assertTrue(ProgramEditService.save(fixture.container, result.snapshot, edited).accepted);
        assertSame(
            original,
            fixture.manager.getCompiledProgram(),
            "The accepted source baseline must refresh after reuse");
    }

    @Test
    void actualTokenChangesAndAnExternallyChangedInstalledBaselineRecompile() throws Exception {
        Fixture fixture = new Fixture(SOURCE);
        Program original = fixture.manager.getCompiledProgram();
        assertNull(fixture.manager.trySaveProgram(SOURCE.replace("INPUT 1", "INPUT 2")));
        assertNotSame(original, fixture.manager.getCompiledProgram());
        Program second = fixture.manager.getCompiledProgram();
        String external = SOURCE.replace("INPUT 1", "INPUT 3");
        ItemDisk.setProgramString(fixture.manager.getDisk(), external);
        assertNull(fixture.manager.trySaveProgram("-- comment\n" + external));
        assertNotSame(
            second,
            fixture.manager.getCompiledProgram(),
            "Uncompiled external source must not authorize reuse");
    }

    @Test
    void reuseStillReconcilesLiveStateAndRejectsOversizedCompleteDisks() throws Exception {
        String counter = "STATE $count = 0 EVERY 1 TICKS DO SET $count = $count + 1 END";
        Fixture fixture = new Fixture(counter);
        fixture.manager.updateEntity();
        fixture.manager.updateEntity();
        Program original = fixture.manager.getCompiledProgram();
        ProgramEditSnapshot before = ProgramEditService.open(fixture.container);
        ProgramEditService.SaveResult saved = ProgramEditService
            .save(fixture.container, before, "-- layout\n" + counter);
        assertTrue(saved.accepted, saved.message);
        assertSame(original, fixture.manager.getCompiledProgram());
        assertEquals(
            2,
            fixture.manager.getProgramState()
                .read(0)
                .asLong());
        fixture.manager.updateEntity();
        assertEquals(
            3,
            fixture.manager.getProgramState()
                .read(0)
                .asLong());

        Fixture oversized = new Fixture(SOURCE);
        Program installed = oversized.manager.getCompiledProgram();
        ProgramDocument.ensure(oversized.manager.getDisk());
        oversized.manager.getDisk()
            .getTagCompound()
            .setString("oversized", String.join("", Collections.nCopies(31000, "x")));
        long generation = oversized.manager.getDiskGeneration();
        long revision = ProgramDocument.read(oversized.manager.getDisk()).revision;
        assertNotNull(oversized.manager.trySaveProgram("-- layout\n" + SOURCE));
        assertSame(installed, oversized.manager.getCompiledProgram());
        assertEquals(SOURCE, ItemDisk.getProgramString(oversized.manager.getDisk()));
        assertEquals(generation, oversized.manager.getDiskGeneration());
        assertEquals(revision, ProgramDocument.read(oversized.manager.getDisk()).revision);
    }

    private static final class Fixture {

        final AuditRegressionTest.Fixture world = new AuditRegressionTest.Fixture();
        final TileEntityManager manager = RecoveryFixtures.manager(world);
        final ContainerManager container;

        Fixture(String source) throws Exception {
            manager.saveCodeAndRecompile(source);
            assertTrue(manager.hasCompiledProgram(), manager.getStatusMessage());
            AuditRegressionTest.FakePlayer player = AuditRegressionTest.allocate(AuditRegressionTest.FakePlayer.class);
            player.worldObj = world.world;
            player.inventory = new InventoryPlayer(player);
            container = new ContainerManager(player.inventory, manager);
        }
    }
}
