package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.gui.ContainerManager;
import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.program.ProgramEditService;
import ca.teamdman.sfm.common.program.ProgramEditSnapshot;
import ca.teamdman.sfm.common.program.ProgramStateStore;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.ast.Program;

/** Exercises document saves at the shared server transaction boundary, independently of either editor. */
class ProgramEditTransactionTest {

    private static final String SOURCE = "EVERY 20 TICKS DO INPUT 1 FROM source OUTPUT TO dest END";
    private static final String EDITED = "EVERY 20 TICKS DO INPUT 2 FROM source OUTPUT TO dest END";
    private static final String COUNTER = "STATE $count = 0 EVERY 1 TICKS DO SET $count = $count + 1 END";

    @Test
    void anotherEditorsAcceptedSaveMakesTheOlderDraftStale() throws Exception {
        EditFixture fixture = new EditFixture(SOURCE);
        ContainerManager second = fixture.otherPlayerContainer();
        ProgramEditSnapshot first = ProgramEditService.open(fixture.container);
        ProgramEditSnapshot stale = ProgramEditService.open(second);
        assertEquals(first.getDocumentId(), stale.getDocumentId());
        assertEquals(SOURCE, first.getSource());
        ProgramEditService.SaveResult saved = ProgramEditService.save(fixture.container, first, EDITED);
        assertAccepted(saved, EDITED);
        assertTrue(saved.snapshot.getRevision() > first.getRevision());
        ProgramEditService.SaveResult rejected = ProgramEditService.save(second, stale, "EVERY 20 TICKS DO FORGET END");
        assertRejected(rejected);
        assertEquals(EDITED, ItemDisk.getProgramString(fixture.manager.getDisk()));
    }

    @Test
    void replacingDiskWithAnIdenticalCopyDoesNotAuthorizeTheOldDraft() throws Exception {
        EditFixture fixture = new EditFixture(SOURCE);
        ProgramEditSnapshot opened = ProgramEditService.open(fixture.container);
        ItemStack replacement = fixture.manager.getDisk()
            .copy();
        fixture.manager.setInventorySlotContents(0, replacement);
        assertRejected(ProgramEditService.save(fixture.container, opened, EDITED));
        assertSame(replacement, fixture.manager.getDisk());
        assertEquals(SOURCE, ItemDisk.getProgramString(replacement));
        ProgramEditSnapshot fresh = ProgramEditService.open(fixture.container);
        assertNotEquals(opened.getBindingGeneration(), fresh.getBindingGeneration());
        assertAccepted(ProgramEditService.save(fixture.container, fresh, EDITED), EDITED);
    }

    @Test
    void removingAndReinsertingTheSameDiskInvalidatesItsEarlierBinding() throws Exception {
        EditFixture fixture = new EditFixture(SOURCE);
        ProgramEditSnapshot opened = ProgramEditService.open(fixture.container);
        ItemStack original = fixture.manager.getDisk();
        ItemStack detached = fixture.manager.decrStackSize(0, 1);
        assertSame(original, detached);
        assertNull(fixture.manager.getDisk());
        fixture.manager.setInventorySlotContents(0, detached);
        assertRejected(ProgramEditService.save(fixture.container, opened, EDITED));
        assertEquals(SOURCE, ItemDisk.getProgramString(detached));
        assertAccepted(
            ProgramEditService.save(fixture.container, ProgramEditService.open(fixture.container), EDITED),
            EDITED);
    }

    @Test
    void snapshotCannotBeReusedByADifferentOrReopenedContainer() throws Exception {
        EditFixture fixture = new EditFixture(SOURCE);
        ProgramEditSnapshot opened = ProgramEditService.open(fixture.container);
        ContainerManager reopened = fixture.container();
        ProgramEditSnapshot reopenedSnapshot = ProgramEditService.open(reopened);
        assertNotEquals(opened.getSessionId(), reopenedSnapshot.getSessionId());
        assertRejected(ProgramEditService.save(reopened, opened, EDITED));
        assertEquals(SOURCE, ItemDisk.getProgramString(fixture.manager.getDisk()));
        assertAccepted(ProgramEditService.save(reopened, reopenedSnapshot, EDITED), EDITED);
    }

    @Test
    void supportedSourceSetterDetectsChangeAwayAndBackToIdenticalText() throws Exception {
        EditFixture fixture = new EditFixture(SOURCE);
        ProgramEditSnapshot opened = ProgramEditService.open(fixture.container);
        ItemDisk.setProgramString(fixture.manager.getDisk(), EDITED);
        ItemDisk.setProgramString(fixture.manager.getDisk(), SOURCE);
        assertEquals(SOURCE, ItemDisk.getProgramString(fixture.manager.getDisk()));
        assertRejected(ProgramEditService.save(fixture.container, opened, EDITED));
        ProgramEditSnapshot current = ProgramEditService.open(fixture.container);
        assertTrue(current.getRevision() > opened.getRevision());
        assertAccepted(ProgramEditService.save(fixture.container, current, EDITED), EDITED);
    }

    @Test
    void repeatedSavesUseTheReturnedSnapshotAndPreserveStoppedState() throws Exception {
        EditFixture fixture = new EditFixture(SOURCE);
        fixture.manager.setRunning(false);
        ProgramEditSnapshot opened = ProgramEditService.open(fixture.container);
        ProgramEditService.SaveResult unchanged = ProgramEditService.save(fixture.container, opened, SOURCE);
        assertAccepted(unchanged, SOURCE);
        assertFalse(fixture.manager.isRunning());
        ProgramEditService.SaveResult edited = ProgramEditService.save(fixture.container, unchanged.snapshot, EDITED);
        assertAccepted(edited, EDITED);
        assertFalse(fixture.manager.isRunning());
        ProgramEditService.SaveResult again = ProgramEditService.save(fixture.container, edited.snapshot, EDITED);
        assertAccepted(again, EDITED);
        assertFalse(fixture.manager.isRunning());
        assertTrue(fixture.manager.hasCompiledProgram());
    }

    @Test
    void acceptedSavePreservesRecoveryEvidenceAndDoesNotRestartABlockedManager() throws Exception {
        EditFixture fixture = new EditFixture(SOURCE);
        NBTTagCompound recovery = fixture.manager.preserveItem(
            new ItemStack(AuditRegressionTest.ITEM, 3),
            AuditRegressionTest.SRC,
            0,
            ForgeDirection.UNKNOWN,
            true,
            "save transaction fixture");
        assertFalse(fixture.manager.isRunning());
        ProgramEditService.SaveResult saved = ProgramEditService
            .save(fixture.container, ProgramEditService.open(fixture.container), EDITED);
        assertAccepted(saved, EDITED);
        assertFalse(fixture.manager.isRunning());
        assertTrue(fixture.manager.hasPendingRecovery());
        assertEquals(
            recovery,
            fixture.manager.getDisk()
                .getTagCompound()
                .getTag("sfm:recovery_item"));
    }

    @Test
    void incompatibleStateCandidatePreservesExecutionLiveValuesLabelsAndRecovery() throws Exception {
        EditFixture fixture = new EditFixture(COUNTER);
        fixture.manager.updateEntity();
        fixture.manager.updateEntity();
        assertEquals(
            2,
            fixture.manager.getProgramState()
                .read(0)
                .asLong());
        LabelPositionHolder labels = new LabelPositionHolder();
        labels.addPosition("kept", AuditRegressionTest.SRC);
        LabelPositionHolder.saveTo(fixture.manager.getDisk(), labels);
        NBTTagCompound recovery = new NBTTagCompound();
        recovery.setString("sentinel", "preserve ownership evidence");
        fixture.manager.getDisk()
            .getTagCompound()
            .setTag("sfm:recovery_item", recovery);
        Program originalProgram = fixture.manager.getCompiledProgram();
        ProgramStateStore originalState = fixture.manager.getProgramState();
        boolean running = fixture.manager.isRunning();
        ProgramEditSnapshot opened = ProgramEditService.open(fixture.container);
        ProgramEditService.SaveResult rejected = ProgramEditService
            .save(fixture.container, opened, "STATE $count = [1, 2] EVERY 1 TICKS DO END");
        assertRejected(rejected);
        assertEquals(COUNTER, ItemDisk.getProgramString(fixture.manager.getDisk()));
        assertSame(originalProgram, fixture.manager.getCompiledProgram());
        assertSame(originalState, fixture.manager.getProgramState());
        assertEquals(
            2,
            originalState.read(0)
                .asLong());
        assertEquals(running, fixture.manager.isRunning());
        assertEquals(
            Collections.singleton(AuditRegressionTest.SRC),
            LabelPositionHolder.from(fixture.manager.getDisk())
                .getAllLabels()
                .get("kept"));
        assertEquals(
            recovery,
            fixture.manager.getDisk()
                .getTagCompound()
                .getTag("sfm:recovery_item"));
        ProgramEditSnapshot current = ProgramEditService.open(fixture.container);
        assertEquals(opened.getDocumentId(), current.getDocumentId());
        assertEquals(opened.getRevision(), current.getRevision());
    }

    @Test
    void oversizedConstantAndMultibyteCandidatesAreRejectedBeforeSourceOrExecutionChanges() throws Exception {
        for (String candidate : new String[] { "-- " + repeat("a", 31000) + "\n" + SOURCE,
            "NAME \"" + repeat("箱", 11000) + "\"\n" + SOURCE }) {
            assertTrue(candidate.length() < 32767);
            EditFixture fixture = new EditFixture(SOURCE);
            Program original = fixture.manager.getCompiledProgram();
            ProgramEditSnapshot opened = ProgramEditService.open(fixture.container);
            assertRejected(ProgramEditService.save(fixture.container, opened, candidate));
            assertEquals(SOURCE, ItemDisk.getProgramString(fixture.manager.getDisk()));
            assertSame(original, fixture.manager.getCompiledProgram());
            assertTrue(fixture.manager.isRunning());
            assertNull(fixture.manager.getProgramState());
            assertEquals(
                opened.getRevision(),
                ProgramEditService.open(fixture.container)
                    .getRevision());
        }
    }

    @Test
    void validMultibyteSourceSurvivesTheAcceptedTransactionExactly() throws Exception {
        EditFixture fixture = new EditFixture(SOURCE);
        String candidate = "NAME \"" + repeat("箱😀", 1000) + "\"\n" + SOURCE;
        ProgramEditService.SaveResult result = ProgramEditService
            .save(fixture.container, ProgramEditService.open(fixture.container), candidate);
        assertAccepted(result, candidate);
        assertEquals(candidate, ItemDisk.getProgramString(fixture.manager.getDisk()));
        assertEquals(
            candidate,
            ProgramEditService.open(fixture.container)
                .getSource());
    }

    private static void assertAccepted(ProgramEditService.SaveResult result, String source) {
        assertTrue(result.accepted, result.message);
        assertNotNull(result.snapshot);
        assertEquals(source, result.snapshot.getSource());
        assertNotNull(result.snapshot.getDocumentId());
        assertNotNull(result.snapshot.getSessionId());
    }

    @Test
    void existingForeignDocumentCannotOpenWithUntransportableSource() throws Exception {
        EditFixture fixture = new EditFixture(SOURCE);
        ProgramEditSnapshot before = ProgramEditService.open(fixture.container);
        for (String source : new String[] { repeat("箱", 11000), "NAME \"\ud800\" " + SOURCE }) {
            // Simulate foreign direct NBT edits, bypassing SFM's guarded writer.
            fixture.manager.getDisk()
                .getTagCompound()
                .setString(ItemDisk.NBT_PROGRAM, source);
            assertThrows(IllegalArgumentException.class, () -> ProgramEditService.open(fixture.container));
            assertEquals(source, ItemDisk.getProgramString(fixture.manager.getDisk()));
            assertEquals(
                before.revision,
                ca.teamdman.sfm.common.program.ProgramDocument.read(fixture.manager.getDisk()).revision);
        }
    }

    @Test
    void malformedUnicodeSavePreservesTheCurrentExecutableAndDocument() throws Exception {
        EditFixture fixture = new EditFixture(SOURCE);
        ProgramEditSnapshot before = ProgramEditService.open(fixture.container);
        Program executable = fixture.manager.getCompiledProgram();
        assertRejected(ProgramEditService.save(fixture.container, before, "NAME \"\ud800\" " + SOURCE));
        assertSame(executable, fixture.manager.getCompiledProgram());
        assertEquals(SOURCE, ItemDisk.getProgramString(fixture.manager.getDisk()));
        assertEquals(before.revision, ProgramEditService.open(fixture.container).revision);
    }

    @Test
    void muiDiskValidityAndPollingDoNotChangeBindingOrSerializeLiveState() throws Exception {
        EditFixture fixture = new EditFixture(COUNTER);
        fixture.manager.updateEntity();
        long generation = fixture.manager.getDiskGeneration();
        NBTTagCompound nbt = (NBTTagCompound) fixture.manager.getDisk()
            .getTagCompound()
            .copy();
        ca.teamdman.sfm.integration.mui2.ManagerDiskSlot slot = new ca.teamdman.sfm.integration.mui2.ManagerDiskSlot(
            fixture.manager);
        assertTrue(slot.isItemValid(fixture.manager.getDisk()));
        assertEquals(1, slot.getItemStackLimit(fixture.manager.getDisk()));
        assertSame(fixture.manager.getDisk(), slot.getStack());
        assertEquals(generation, fixture.manager.getDiskGeneration());
        assertEquals(
            nbt,
            fixture.manager.getDisk()
                .getTagCompound());
    }

    @Test
    void muiContainerUsesTheSameGuardedDocumentTransaction() throws Exception {
        EditFixture fixture = new EditFixture(SOURCE);
        ca.teamdman.sfm.integration.mui2.ManagerMui2Container container = new ca.teamdman.sfm.integration.mui2.ManagerMui2Container(
            fixture.manager);
        ProgramEditSnapshot snapshot = ProgramEditService.open(container);
        assertNotEquals(fixture.container.getEditSessionId(), snapshot.sessionId);
        assertRejected(ProgramEditService.save(fixture.container, snapshot, EDITED));
        assertAccepted(ProgramEditService.save(container, snapshot, EDITED), EDITED);
    }

    @Test
    void remoteFrameworkOpenIsRejectedBeforeReadingWorldOrBindingSlots() throws Exception {
        net.minecraft.entity.player.EntityPlayerMP player = AuditRegressionTest
            .allocate(net.minecraft.entity.player.EntityPlayerMP.class);
        ca.teamdman.sfm.integration.mui2.ManagerMui2Factory.ManagerGuiData data = new ca.teamdman.sfm.integration.mui2.ManagerMui2Factory.ManagerGuiData(
            player,
            0,
            64,
            0);
        com.cleanroommc.modularui.screen.UISettings settings = new com.cleanroommc.modularui.screen.UISettings();
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            com.cleanroommc.modularui.screen.ModularPanel panel = assertDoesNotThrow(
                () -> ca.teamdman.sfm.integration.mui2.ManagerMui2Factory.INSTANCE.createPanel(data, null, settings));
            assertEquals(ca.teamdman.sfm.integration.mui2.ManagerMui2Factory.DENIED_PANEL, panel.getName());
            assertFalse(data.isApproved());
            assertFalse(settings.hasCustomContainer());
            assertFalse(settings.canPlayerInteractWithUI(player));
        })
            .get();
    }

    private static void assertRejected(ProgramEditService.SaveResult result) {
        assertFalse(result.accepted);
        assertNotNull(result.message);
        assertFalse(result.message.isEmpty());
    }

    private static String repeat(String text, int count) {
        return String.join("", Collections.nCopies(count, text));
    }

    private static final class EditFixture {

        final AuditRegressionTest.Fixture world = new AuditRegressionTest.Fixture();
        final TileEntityManager manager = RecoveryFixtures.manager(world);
        final AuditRegressionTest.FakePlayer player;
        final ContainerManager container;

        EditFixture(String source) throws Exception {
            manager.saveCodeAndRecompile(source);
            assertTrue(manager.hasCompiledProgram(), manager.getStatusMessage());
            player = AuditRegressionTest.allocate(AuditRegressionTest.FakePlayer.class);
            player.worldObj = world.world;
            player.posX = AuditRegressionTest.MANAGER.x + 0.5;
            player.posY = AuditRegressionTest.MANAGER.y + 0.5;
            player.posZ = AuditRegressionTest.MANAGER.z + 0.5;
            player.inventory = new InventoryPlayer(player);
            container = container();
        }

        ContainerManager container() {
            ContainerManager opened = new ContainerManager(player.inventory, manager);
            player.openContainer = opened;
            return opened;
        }

        ContainerManager otherPlayerContainer() throws Exception {
            AuditRegressionTest.FakePlayer other = AuditRegressionTest.allocate(AuditRegressionTest.FakePlayer.class);
            other.worldObj = world.world;
            other.posX = player.posX;
            other.posY = player.posY;
            other.posZ = player.posZ;
            other.inventory = new InventoryPlayer(other);
            ContainerManager opened = new ContainerManager(other.inventory, manager);
            other.openContainer = opened;
            return opened;
        }
    }
}
