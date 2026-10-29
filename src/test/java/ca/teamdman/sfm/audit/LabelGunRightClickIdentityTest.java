package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.network.play.server.S30PacketWindowItems;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.world.WorldServer;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.item.ItemLabelGun;

/** Exercise vanilla's real right-click callback, including its defensive held-stack copy. */
class LabelGunRightClickIdentityTest {

    @Test
    void openingKeepsTheActualGunWithoutStartingUseAndStillSynchronizesInventory() throws Exception {
        Fixture fixture = new Fixture(new ItemLabelGun());
        fixture.rightClick();
        assertSame(fixture.gun, fixture.player.getHeldItem());
        assertFalse(fixture.player.isUsingItem());
        assertEquals(2, fixture.sent.size());
        assertTrue(fixture.sent.get(0) instanceof S30PacketWindowItems);
        assertTrue(fixture.sent.get(1) instanceof S2FPacketSetSlot);
        assertEquals("original", ItemLabelGun.getActiveLabel(fixture.player.getHeldItem()));
    }

    @Test
    void zeroDurationControlReproducesVanillasServerSideIdentityChange() throws Exception {
        Fixture fixture = new Fixture(new ItemLabelGun() {

            @Override
            public int getMaxItemUseDuration(ItemStack stack) {
                return 0;
            }
        });
        fixture.rightClick();
        assertNotSame(fixture.gun, fixture.player.getHeldItem());
        assertTrue(ItemStack.areItemStacksEqual(fixture.gun, fixture.player.getHeldItem()));
        assertFalse(fixture.player.isUsingItem());
    }

    private static final class Fixture {

        final EntityPlayerMP player = allocate(EntityPlayerMP.class);
        final ItemStack gun;
        final List<Packet> sent = new ArrayList<>();
        final NetHandlerPlayServer handler;

        Fixture(ItemLabelGun item) throws Exception {
            TestServer server = allocate(TestServer.class);
            server.world = allocate(WorldServer.class);
            player.worldObj = server.world;
            player.inventory = new InventoryPlayer(player);
            gun = new ItemStack(item);
            ItemLabelGun.setActiveLabel(gun, "original");
            player.inventory.mainInventory[0] = gun;
            Field managerField = EntityPlayerMP.class.getDeclaredField("theItemInWorldManager");
            managerField.setAccessible(true);
            managerField.set(player, new ItemInWorldManager(server.world));
            player.theItemInWorldManager.thisPlayerMP = player;
            player.inventoryContainer = new Container() {

                {
                    addSlotToContainer(new Slot(player.inventory, 0, 0, 0));
                }

                @Override
                public boolean canInteractWith(EntityPlayer owner) {
                    return true;
                }
            };
            player.openContainer = player.inventoryContainer;
            handler = new NetHandlerPlayServer(server, new NetworkManager(false), player) {

                @Override
                public void sendPacket(Packet packet) {
                    sent.add(packet);
                }
            };
        }

        void rightClick() {
            handler.processPlayerBlockPlacement(new C08PacketPlayerBlockPlacement(-1, -1, -1, 255, gun, 0, 0, 0));
        }
    }

    private static final class TestServer extends DedicatedServer {

        WorldServer world;

        private TestServer() {
            super(null);
        }

        @Override
        public WorldServer worldServerForDimension(int dimension) {
            return world;
        }
    }
}
