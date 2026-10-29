package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;

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
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.world.WorldServer;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.gui.ClientLabelGunSession;
import ca.teamdman.sfm.client.gui.mui2.Mui2LabelGun;
import ca.teamdman.sfm.common.item.ItemLabelGun;
import ca.teamdman.sfm.common.net.LabelGunAccess;
import cpw.mods.fml.common.gameevent.TickEvent;

class LabelGunEditorSessionTest {

    @Test
    void openingThroughVanillaRightClickKeepsActualGunAndOpeningAuthority() throws Exception {
        try (Session session = new Session()) {
            Object binding = installBinding(session);
            Mui2LabelGun editor = session.editor();
            processRightClick(session);
            assertSame(session.gun, session.player.getHeldItem());
            assertFalse(session.player.isUsingItem());
            assertSame(session.gun, LabelGunAccess.authorized(session.player, session.originalToken));
            // A player tick must retain the binding instead of issuing a replacement token.
            LabelGunAccess.INSTANCE.tick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END, session.player));
            assertSame(binding, bindings().get(session.player));
            cacheField("token").set(null, session.originalToken);
            assertSame(session.originalToken, invoke(editor, "token"));
            assertEquals("Search or enter a label", invoke(editor, "readiness"));
        } finally {
            LabelGunAccess.clear();
        }
    }

    @Test
    void vanillaZeroDurationRightClickReproducesTheServerIdentityChange() throws Exception {
        try (Session session = new Session(new ItemLabelGun() {

            @Override
            public int getMaxItemUseDuration(ItemStack stack) {
                return 0;
            }
        })) {
            installBinding(session);
            Mui2LabelGun editor = session.editor();
            processRightClick(session);
            assertNotSame(session.gun, session.player.getHeldItem());
            assertNull(LabelGunAccess.authorized(session.player, session.originalToken));
            // This is the new token that the next player tick delivers for the copied stack.
            cacheField("token").set(null, new LabelGunAccess.Token(UUID.randomUUID(), session.player.getEntityId(), 0));
            assertEquals("Label gun changed. Close and reopen.", invoke(editor, "readiness"));
        } finally {
            LabelGunAccess.clear();
        }
    }

    @Test
    void actualSameSlotReplacementStillRejectsOpeningAuthority() throws Exception {
        try (Session session = new Session()) {
            installBinding(session);
            processRightClick(session);
            session.player.inventory.mainInventory[0] = session.gun.copy();
            assertNull(LabelGunAccess.authorized(session.player, session.originalToken));
        } finally {
            LabelGunAccess.clear();
        }
    }

    @Test
    void normalInventorySyncCopyKeepsTheOriginalSessionReady() throws Exception {
        try (Session session = new Session()) {
            Mui2LabelGun editor = session.editor();
            assertSame(session.originalToken, invoke(editor, "token"));
            session.player.inventory.mainInventory[0] = session.gun.copy();
            assertSame(session.originalToken, invoke(editor, "token"));
            assertEquals("Search or enter a label", invoke(editor, "readiness"));
        }
    }

    @Test
    void missingInitialTokenRequiresReopenAndNeverAdoptsALateToken() throws Exception {
        try (Session session = new Session()) {
            cacheField("token").set(null, null);
            Mui2LabelGun editor = session.editor();
            assertNull(invoke(editor, "token"));
            cacheField("token").set(null, session.originalToken);
            assertNull(invoke(editor, "token"));
            assertEquals("Sync pending. Close and reopen.", invoke(editor, "readiness"));
            select(editor);
            assertEquals("original", ItemLabelGun.getActiveLabel(session.gun));
            assertSame(session.originalToken, invoke(session.editor(), "token"));
        }
    }

    @Test
    void replacementSessionStalesTheOldEditorWithoutMutatingEitherGun() throws Exception {
        try (Session session = new Session()) {
            Mui2LabelGun editor = session.editor();
            ItemStack replacement = session.gun.copy();
            ItemLabelGun.setActiveLabel(replacement, "replacement");
            session.player.inventory.mainInventory[0] = replacement;
            LabelGunAccess.Token next = new LabelGunAccess.Token(UUID.randomUUID(), session.player.getEntityId(), 0);
            cacheField("token").set(null, next);
            assertNull(invoke(editor, "token"));
            assertEquals("Label gun changed. Close and reopen.", invoke(editor, "readiness"));
            select(editor);
            assertEquals("original", ItemLabelGun.getActiveLabel(session.gun));
            assertEquals("replacement", ItemLabelGun.getActiveLabel(replacement));
            assertSame(next, invoke(session.editor(), "token"));
            cacheField("token").set(null, session.originalToken);
            assertNull(invoke(editor, "token"));
        }
    }

    @Test
    void leavingTheHeldSlotPermanentlyStalesTheEditor() throws Exception {
        try (Session session = new Session()) {
            Mui2LabelGun editor = session.editor();
            session.player.inventory.currentItem = 1;
            assertNull(invoke(editor, "token"));
            session.player.inventory.currentItem = 0;
            assertNull(invoke(editor, "token"));
            select(editor);
            assertEquals("original", ItemLabelGun.getActiveLabel(session.gun));
        }
    }

    private static Object invoke(Mui2LabelGun editor, String name) throws Exception {
        Method method = Mui2LabelGun.class.getDeclaredMethod(name);
        method.setAccessible(true);
        return method.invoke(editor);
    }

    private static void select(Mui2LabelGun editor) throws Exception {
        Method method = Mui2LabelGun.class.getDeclaredMethod("select", String.class);
        method.setAccessible(true);
        // A blocked selection must return before touching the stack, network or GUI.
        method.invoke(editor, "forbidden");
    }

    private static Field cacheField(String name) throws Exception {
        Field field = ClientLabelGunSession.class.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    @SuppressWarnings("unchecked")
    private static Map<EntityPlayerMP, Object> bindings() throws Exception {
        Field field = LabelGunAccess.class.getDeclaredField("BINDINGS");
        field.setAccessible(true);
        return (Map<EntityPlayerMP, Object>) field.get(null);
    }

    private static Object installBinding(Session session) throws Exception {
        Class<?> type = Class.forName(LabelGunAccess.class.getName() + "$Binding");
        Constructor<?> constructor = type.getDeclaredConstructor(EntityPlayerMP.class, ItemStack.class);
        constructor.setAccessible(true);
        Object binding = constructor.newInstance(session.player, session.gun);
        Field tokenField = type.getDeclaredField("token");
        tokenField.setAccessible(true);
        session.originalToken = (LabelGunAccess.Token) tokenField.get(binding);
        cacheField("token").set(null, session.originalToken);
        bindings().put(session.player, binding);
        return binding;
    }

    private static void processRightClick(Session session) throws Exception {
        TestServer server = allocate(TestServer.class);
        server.world = allocate(WorldServer.class);
        Field managerField = EntityPlayerMP.class.getDeclaredField("theItemInWorldManager");
        managerField.setAccessible(true);
        managerField.set(session.player, new ItemInWorldManager(server.world));
        session.player.theItemInWorldManager.thisPlayerMP = session.player;
        session.player.inventoryContainer = new Container() {

            {
                addSlotToContainer(new Slot(session.player.inventory, 0, 0, 0));
            }

            @Override
            public boolean canInteractWith(EntityPlayer player) {
                return true;
            }
        };
        session.player.openContainer = session.player.inventoryContainer;
        NetHandlerPlayServer handler = new NetHandlerPlayServer(server, new NetworkManager(false) {

            @Override
            public boolean isChannelOpen() {
                return true;
            }
        }, session.player) {

            @Override
            public void sendPacket(Packet packet) {}
        };
        handler.processPlayerBlockPlacement(
            new C08PacketPlayerBlockPlacement(-1, -1, -1, 255, session.player.getHeldItem(), 0, 0, 0));
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

    private static final class Session implements AutoCloseable {

        final Object previousWorld = cacheField("world").get(null);
        final Object previousToken = cacheField("token").get(null);
        final EntityPlayerMP player = allocate(EntityPlayerMP.class);
        final ItemStack gun;
        LabelGunAccess.Token originalToken = new LabelGunAccess.Token(UUID.randomUUID(), player.getEntityId(), 0);

        Session() throws Exception {
            this(new ItemLabelGun());
        }

        Session(ItemLabelGun item) throws Exception {
            gun = new ItemStack(item);
            player.worldObj = world();
            player.inventory = new InventoryPlayer(player);
            player.inventory.mainInventory[0] = gun;
            ItemLabelGun.setActiveLabel(gun, "original");
            cacheField("world").set(null, player.worldObj);
            cacheField("token").set(null, originalToken);
        }

        Mui2LabelGun editor() throws Exception {
            Constructor<Mui2LabelGun> constructor = Mui2LabelGun.class
                .getDeclaredConstructor(EntityPlayer.class, ItemStack.class);
            constructor.setAccessible(true);
            return constructor.newInstance(player, player.getHeldItem());
        }

        @Override
        public void close() throws Exception {
            cacheField("world").set(null, previousWorld);
            cacheField("token").set(null, previousToken);
        }
    }
}
