package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.gui.ContainerManager;
import ca.teamdman.sfm.common.gui.ManagerControlToken;
import ca.teamdman.sfm.common.net.LabelGunAccess;
import ca.teamdman.sfm.common.net.ManagerAccess;
import ca.teamdman.sfm.common.net.PacketLabelGunAction;
import ca.teamdman.sfm.common.net.PacketManagerClearLogs;
import ca.teamdman.sfm.common.net.PacketManagerSetLogLevel;
import ca.teamdman.sfm.common.net.PacketSetLabel;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

class AuthoringAuthorityTest {

    @Test
    void queuedLoggingAuthorityRejectsReplacementDiskReopenedWindowAndDeadPlayer() throws Exception {
        Fixture fixture = new Fixture();
        TileEntityManager manager = RecoveryFixtures.manager(fixture);
        FakePlayer player = allocate(FakePlayer.class);
        player.worldObj = fixture.world;
        player.inventory = new InventoryPlayer(player);
        player.posX = manager.xCoord + .5;
        player.posY = manager.yCoord + .5;
        player.posZ = manager.zCoord + .5;
        ContainerManager window = new ContainerManager(player.inventory, manager);
        window.windowId = 42;
        player.openContainer = window;
        ManagerControlToken queued = new ManagerControlToken(
            window.getEditSessionId(),
            manager.getDiskGeneration(),
            42);
        assertSame(manager, access(player, manager, queued));
        ItemStack disk = manager.getDisk();
        manager.setInventorySlotContents(0, null);
        manager.setInventorySlotContents(0, disk);
        assertNull(access(player, manager, queued));
        ManagerControlToken current = new ManagerControlToken(
            window.getEditSessionId(),
            manager.getDiskGeneration(),
            42);
        assertSame(manager, access(player, manager, current));
        player.openContainer = new ContainerManager(player.inventory, manager);
        player.openContainer.windowId = 42;
        assertNull(access(player, manager, current));
        player.openContainer = window;
        player.isDead = true;
        assertNull(access(player, manager, current));
    }

    @Test
    void labelActionsRequireOriginalStackSlotSessionAndConnectedLivingEntity() throws Exception {
        Fixture fixture = new Fixture();
        EntityPlayerMP player = allocate(EntityPlayerMP.class);
        player.worldObj = fixture.world;
        player.inventory = new InventoryPlayer(player);
        boolean[] connected = { true };
        player.playerNetServerHandler = new NetHandlerPlayServer(null, new NetworkManager(false) {

            @Override
            public boolean isChannelOpen() {
                return connected[0];
            }
        }, player);
        ItemStack original = new ItemStack(new Item());
        player.inventory.mainInventory[0] = original;
        Class<?> bindingClass = Class.forName(LabelGunAccess.class.getName() + "$Binding");
        Constructor<?> constructor = bindingClass.getDeclaredConstructor(EntityPlayerMP.class, ItemStack.class);
        constructor.setAccessible(true);
        Object binding = constructor.newInstance(player, original);
        Field bindingsField = LabelGunAccess.class.getDeclaredField("BINDINGS");
        bindingsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<EntityPlayerMP, Object> bindings = (Map<EntityPlayerMP, Object>) bindingsField.get(null);
        Field tokenField = bindingClass.getDeclaredField("token");
        tokenField.setAccessible(true);
        LabelGunAccess.Token token = (LabelGunAccess.Token) tokenField.get(binding);
        LabelGunAccess.clear();
        try {
            bindings.put(player, binding);
            assertSame(original, LabelGunAccess.authorized(player, token));
            player.inventory.mainInventory[0] = original.copy();
            assertNull(LabelGunAccess.authorized(player, token));
            player.inventory.mainInventory[0] = original;
            player.inventory.mainInventory[1] = original;
            player.inventory.currentItem = 1;
            assertNull(LabelGunAccess.authorized(player, token));
            player.inventory.currentItem = 0;
            assertNull(
                LabelGunAccess.authorized(player, new LabelGunAccess.Token(UUID.randomUUID(), token.entityId, 0)));
            player.isDead = true;
            assertNull(LabelGunAccess.authorized(player, token));
            player.isDead = false;
            connected[0] = false;
            assertNull(LabelGunAccess.authorized(player, token));
        } finally {
            LabelGunAccess.clear();
        }
    }

    @Test
    void mutatingFramesRejectMissingAuthorityTruncationAndTrailingBytes() throws Exception {
        ManagerControlToken manager = new ManagerControlToken(UUID.randomUUID(), 4, 42);
        LabelGunAccess.Token gun = new LabelGunAccess.Token(UUID.randomUUID(), 1, 0);
        verifyFrame(new PacketManagerSetLogLevel(1, 2, 3, "DEBUG", true, manager), PacketManagerSetLogLevel::new);
        verifyFrame(new PacketManagerClearLogs(1, 2, 3, manager), PacketManagerClearLogs::new);
        verifyFrame(new PacketLabelGunAction(0, gun), PacketLabelGunAction::new);
        verifyFrame(new PacketSetLabel("label", gun), PacketSetLabel::new);
        assertInvalid(new PacketManagerSetLogLevel(1, 2, 3, "DEBUG", true), PacketManagerSetLogLevel::new);
        assertInvalid(new PacketManagerClearLogs(1, 2, 3), PacketManagerClearLogs::new);
        assertInvalid(new PacketLabelGunAction(0), PacketLabelGunAction::new);
        assertInvalid(new PacketSetLabel("label"), PacketSetLabel::new);
        assertThrows(
            IllegalArgumentException.class,
            () -> new PacketSetLabel(new String(new char[257]).replace('\0', 'x'), gun));
        assertThrows(IllegalArgumentException.class, () -> new PacketSetLabel("\ud800", gun));
    }

    private static TileEntityManager access(FakePlayer player, TileEntityManager manager, ManagerControlToken token) {
        return ManagerAccess.get(player, manager.xCoord, manager.yCoord, manager.zCoord, token);
    }

    private static void verifyFrame(IMessage original, Supplier<IMessage> factory) throws Exception {
        ByteBuf bytes = Unpooled.buffer();
        try {
            original.toBytes(bytes);
            IMessage decoded = factory.get();
            decoded.fromBytes(bytes.duplicate());
            assertTrue(valid(decoded));
            for (int length = 0; length < bytes.readableBytes(); length++) {
                decoded = factory.get();
                decoded.fromBytes(bytes.slice(0, length));
                assertFalse(valid(decoded), "Truncated frame " + length);
            }
            bytes.writeByte(0);
            decoded = factory.get();
            decoded.fromBytes(bytes);
            assertFalse(valid(decoded));
        } finally {
            bytes.release();
        }
    }

    private static void assertInvalid(IMessage original, Supplier<IMessage> factory) throws Exception {
        ByteBuf bytes = Unpooled.buffer();
        try {
            original.toBytes(bytes);
            IMessage decoded = factory.get();
            decoded.fromBytes(bytes);
            assertFalse(valid(decoded));
        } finally {
            bytes.release();
        }
    }

    private static boolean valid(IMessage packet) throws Exception {
        Field field = packet.getClass()
            .getDeclaredField("valid");
        field.setAccessible(true);
        return field.getBoolean(packet);
    }
}
