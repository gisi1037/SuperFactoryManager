package ca.teamdman.sfm.common.net;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import ca.teamdman.sfm.common.item.ItemLabelGun;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import io.netty.buffer.ByteBuf;

/** Server-issued authority for one player, held slot and actual stack binding. */
public final class LabelGunAccess {

    public static final LabelGunAccess INSTANCE = new LabelGunAccess();
    private static final Map<EntityPlayerMP, Binding> BINDINGS = new WeakHashMap<>();

    private LabelGunAccess() {}

    public static final class Token {

        public final UUID session;
        public final int entityId, slot;

        public Token(UUID session, int entityId, int slot) {
            if (session == null || slot < 0 || slot > 8) throw new IllegalArgumentException("Invalid label gun token");
            this.session = session;
            this.entityId = entityId;
            this.slot = slot;
        }

        public void write(ByteBuf buf) {
            buf.writeInt(entityId);
            buf.writeInt(slot);
            ProgramEditWire.uuid(buf, session);
        }

        public static Token read(ByteBuf buf) {
            int entity = buf.readInt(), slot = buf.readInt();
            return new Token(ProgramEditWire.uuid(buf), entity, slot);
        }
    }

    private static final class Binding {

        final ItemStack stack;
        final Token token;

        Binding(EntityPlayerMP player, ItemStack stack) {
            this.stack = stack;
            token = new Token(UUID.randomUUID(), player.getEntityId(), player.inventory.currentItem);
        }
    }

    @SubscribeEvent
    public void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof EntityPlayerMP)) return;
        EntityPlayerMP player = (EntityPlayerMP) event.player;
        if (!PlayerUiAccess.isLive(player)) {
            BINDINGS.remove(player);
            return;
        }
        ItemStack held = player.getHeldItem();
        if (held == null || !(held.getItem() instanceof ItemLabelGun)) {
            BINDINGS.remove(player);
            return;
        }
        Binding before = BINDINGS.get(player);
        if (before == null || before.stack != held || before.token.slot != player.inventory.currentItem) {
            Binding next = new Binding(player, held);
            BINDINGS.put(player, next);
            SFMNetwork.WRAPPER.sendTo(new PacketLabelGunSession(next.token), player);
        }
    }

    public static ItemStack authorized(EntityPlayerMP player, Token token) {
        if (!PlayerUiAccess.isLive(player) || token == null) return null;
        Binding binding = BINDINGS.get(player);
        if (binding == null || !binding.token.session.equals(token.session)
            || token.entityId != player.getEntityId()
            || token.slot != player.inventory.currentItem
            || binding.token.slot != token.slot
            || binding.stack != player.getHeldItem()) return null;
        return binding.stack;
    }

    public static void clear() {
        BINDINGS.clear();
    }
}
