package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.gui.ProgramEditState;
import ca.teamdman.sfm.common.gui.ContainerManager;
import ca.teamdman.sfm.common.gui.ManagerControlToken;
import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.program.ProgramDocument;
import ca.teamdman.sfm.common.program.ProgramEditService;
import ca.teamdman.sfm.common.program.ProgramEditSnapshot;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;

class ManagerNbtBindingTest {

    private static final String SOURCE = "EVERY 20 TICKS DO PRINT(1) END";

    @Test
    void sameTileNbtCloneInvalidatesOldPhysicalDiskEditorAndControlAuthority() throws Exception {
        Fixture f = new Fixture();
        ProgramEditSnapshot old = ProgramEditService.open(f.container);
        ItemStack physical = f.manager.getDisk();
        ManagerControlToken token = new ManagerControlToken(old.sessionId, old.bindingGeneration, 42);
        NBTTagCompound saved = new NBTTagCompound();
        f.manager.writeToNBT(saved);
        f.manager.readFromNBT(saved);
        assertNotSame(physical, f.manager.getDisk());
        assertEquals(old.documentId, ProgramDocument.read(f.manager.getDisk()).id);
        assertEquals(old.revision, ProgramDocument.read(f.manager.getDisk()).revision);
        assertEquals(old.source, ItemDisk.getProgramString(f.manager.getDisk()));
        assertTrue(f.manager.getDiskGeneration() > old.bindingGeneration);
        assertFalse(token.matches(f.container.getEditSessionId(), f.manager.getDiskGeneration(), 42));
        ProgramEditService.SaveResult stale = ProgramEditService.save(f.container, old, "-- stale\n" + SOURCE);
        assertFalse(stale.accepted);
        assertEquals(SOURCE, ItemDisk.getProgramString(f.manager.getDisk()));
        ProgramEditSnapshot fresh = ProgramEditService.open(f.container);
        assertTrue(ProgramEditService.save(f.container, fresh, "-- current\n" + SOURCE).accepted);
    }

    @Test
    void clientDescriptionRefreshDoesNotRewriteServerIssuedEditorOrControlTokens() throws Exception {
        Fixture f = new Fixture();
        ProgramEditSnapshot issued = ProgramEditService.open(f.container);
        ManagerControlToken token = new ManagerControlToken(issued.sessionId, issued.bindingGeneration, 42);
        ProgramEditState clientEditor = new ProgramEditState();
        long open = clientEditor.begin(ProgramEditState.Action.OPEN);
        assertTrue(clientEditor.receive(open, true, issued));
        TileEntityManager client = new TileEntityManager();
        NBTTagCompound description = new NBTTagCompound();
        f.manager.writeToNBT(description);
        for (int i = 0; i < 3; i++)
            client.onDataPacket(null, new S35PacketUpdateTileEntity(0, 0, 0, 1, (NBTTagCompound) description.copy()));
        assertNotEquals(f.manager.getDiskGeneration(), client.getDiskGeneration());
        assertSame(issued, clientEditor.snapshot());
        assertEquals(f.manager.getDiskGeneration(), clientEditor.snapshot().bindingGeneration);
        assertTrue(token.matches(f.container.getEditSessionId(), f.manager.getDiskGeneration(), 42));
        assertTrue(
            ProgramEditService.save(f.container, clientEditor.snapshot(), "-- still current\n" + SOURCE).accepted);
    }

    private static final class Fixture {

        final AuditRegressionTest.Fixture world = new AuditRegressionTest.Fixture();
        final TileEntityManager manager = RecoveryFixtures.manager(world);
        final ContainerManager container;

        Fixture() throws Exception {
            manager.saveCodeAndRecompile(SOURCE);
            AuditRegressionTest.FakePlayer player = AuditRegressionTest.allocate(AuditRegressionTest.FakePlayer.class);
            player.worldObj = world.world;
            player.inventory = new InventoryPlayer(player);
            container = new ContainerManager(player.inventory, manager);
        }
    }
}
