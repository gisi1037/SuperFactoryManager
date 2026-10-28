package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.command.CommandSfmRecovery;
import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.recovery.ItemRecovery;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;

class RecoveryActionsTest {

    private TileEntityManager pending(Fixture f, int amount, boolean uncertain) throws Exception {
        TileEntityManager manager = RecoveryFixtures.manager(f);
        ItemStack stack = new ItemStack(ITEM, amount, 7);
        stack.setStackDisplayName("owned remainder");
        manager.preserveItem(stack, SRC, 0, ForgeDirection.WEST, uncertain, "fixture");
        return manager;
    }

    private NBTTagCompound record(TileEntityManager manager) {
        return manager.getDisk()
            .getTagCompound()
            .getCompoundTag(ItemRecovery.KEY);
    }

    private String id(TileEntityManager manager) {
        return ItemRecovery.inspect(manager)
            .getString("recoveryId");
    }

    @Test
    void knownRemainderIsClaimedOnceWithMetadataAndNbtPreserved() throws Exception {
        Fixture f = new Fixture();
        TileEntityManager manager = pending(f, 9, false);
        String id = id(manager);
        assertEquals(9, ItemRecovery.claim(manager, id, ForgeDirection.WEST, "operator"));
        assertFalse(manager.hasPendingRecovery());
        assertFalse(manager.isRunning());
        assertTrue(
            manager.getStatusMessage()
                .contains("resolved"));
        assertEquals(9, count(f.dest));
        assertEquals(
            7,
            f.dest.getStackInSlot(0)
                .getItemDamage());
        assertEquals(
            "owned remainder",
            f.dest.getStackInSlot(0)
                .getDisplayName());
        assertEquals(
            "claimed",
            manager.getDisk()
                .getTagCompound()
                .getCompoundTag(ItemRecovery.LAST_KEY)
                .getString("state"));
        assertThrows(
            IllegalStateException.class,
            () -> ItemRecovery.claim(manager, id, ForgeDirection.WEST, "operator"));
        assertEquals(9, count(f.dest));
    }

    @Test
    void partialCapacityKeepsTheRemainderAndCanBeFinishedLater() throws Exception {
        Fixture f = new Fixture();
        TileEntityManager manager = pending(f, 9, false);
        InventoryTile destination = new InventoryTile(1);
        ItemStack existing = ItemStack.loadItemStackFromNBT(record(manager).getCompoundTag("stack"));
        existing.stackSize = 60;
        destination.setInventorySlotContents(0, existing);
        f.world.tiles.put(DST, destination);
        String id = id(manager);
        assertEquals(4, ItemRecovery.claim(manager, id, ForgeDirection.WEST, "operator"));
        assertEquals(64, count(destination));
        assertEquals(
            5,
            record(manager).getCompoundTag("stack")
                .getByte("Count"));
        assertEquals("pending", record(manager).getString("state"));
        destination.setInventorySlotContents(0, null);
        assertEquals(5, ItemRecovery.claim(manager, id, ForgeDirection.WEST, "operator"));
        assertEquals(5, count(destination));
        assertFalse(manager.hasPendingRecovery());
    }

    @Test
    void fullDestinationKeepsKnownOwnershipWithoutChangingRecord() throws Exception {
        Fixture f = new Fixture();
        TileEntityManager manager = pending(f, 9, false);
        for (int slot = 0; slot < 3; slot++) f.dest.setInventorySlotContents(slot, new ItemStack(OTHER, 64));
        NBTTagCompound before = (NBTTagCompound) record(manager).copy();
        assertEquals(0, ItemRecovery.claim(manager, id(manager), ForgeDirection.WEST, "operator"));
        assertEquals(before, record(manager));
        assertEquals(192, count(f.dest));
    }

    @Test
    void knownClaimDoesNotNeedTheOriginalSourceOrOverwriteItsReplacement() throws Exception {
        Fixture f = new Fixture();
        TileEntityManager manager = pending(f, 9, false);
        f.source.setInventorySlotContents(0, new ItemStack(OTHER, 17));
        f.world.unloaded.add(SRC);
        assertEquals(9, ItemRecovery.claim(manager, id(manager), ForgeDirection.WEST, "operator"));
        assertEquals(17, count(f.source));
        assertSame(
            OTHER,
            f.source.getStackInSlot(0)
                .getItem());
    }

    @Test
    void unloadedManagerOrDestinationDoesNotForceChunkAccess() throws Exception {
        Fixture f = new Fixture();
        TileEntityManager manager = pending(f, 9, false);
        String id = id(manager);
        f.world.unloaded.add(DST);
        assertThrows(
            IllegalStateException.class,
            () -> ItemRecovery.claim(manager, id, ForgeDirection.WEST, "operator"));
        assertEquals("pending", record(manager).getString("state"));
        f.world.unloaded.add(MANAGER);
        assertThrows(IllegalStateException.class, () -> ItemRecovery.inspect(manager));
    }

    @Test
    void sidedRestrictionsAndDuplicateSlotsAreRespected() throws Exception {
        Fixture f = new Fixture();
        TileEntityManager manager = pending(f, 9, false);
        int[] sideSeen = { -1 };
        SidedTile destination = new SidedTile() {

            @Override
            public int[] getAccessibleSlotsFromSide(int side) {
                sideSeen[0] = side;
                return new int[] { -1, 0, 0, 19 };
            }

            @Override
            public boolean canInsertItem(int slot, ItemStack stack, int side) {
                return side == ForgeDirection.EAST.ordinal();
            }
        };
        f.world.tiles.put(DST, destination);
        assertEquals(9, ItemRecovery.claim(manager, id(manager), ForgeDirection.WEST, "operator"));
        assertEquals(ForgeDirection.EAST.ordinal(), sideSeen[0]);
        assertEquals(9, count(destination));
    }

    @Test
    void staleIdCannotClaimANewRecordAtTheSameManager() throws Exception {
        Fixture f = new Fixture();
        TileEntityManager manager = pending(f, 9, false);
        String oldId = id(manager);
        ItemRecovery.claim(manager, oldId, ForgeDirection.WEST, "operator");
        manager.preserveItem(new ItemStack(ITEM, 2), SRC, 0, ForgeDirection.WEST, false, "new fixture");
        assertNotEquals(oldId, id(manager));
        assertThrows(
            IllegalStateException.class,
            () -> ItemRecovery.claim(manager, oldId, ForgeDirection.WEST, "operator"));
        assertEquals(
            2,
            record(manager).getCompoundTag("stack")
                .getByte("Count"));
        assertEquals(9, count(f.dest));
    }

    @Test
    void uncertainOutcomeCannotBeClaimedAndAcknowledgementCreatesNothing() throws Exception {
        Fixture f = new Fixture();
        TileEntityManager manager = pending(f, 9, true);
        String id = id(manager);
        f.dest.setInventorySlotContents(0, new ItemStack(ITEM, 9));
        assertThrows(
            IllegalStateException.class,
            () -> ItemRecovery.claim(manager, id, ForgeDirection.WEST, "operator"));
        ItemRecovery.resolvePresent(manager, id, "operator");
        assertEquals(9, count(f.dest));
        assertFalse(manager.hasPendingRecovery());
        assertFalse(manager.isRunning());
        assertEquals(
            "resolved-present",
            manager.getDisk()
                .getTagCompound()
                .getCompoundTag(ItemRecovery.LAST_KEY)
                .getString("state"));
        assertThrows(IllegalStateException.class, () -> ItemRecovery.resolvePresent(manager, id, "operator"));
    }

    @Test
    void acknowledgementCannotDiscardKnownOwnedItems() throws Exception {
        Fixture f = new Fixture();
        TileEntityManager manager = pending(f, 9, false);
        String id = id(manager);
        assertThrows(IllegalStateException.class, () -> ItemRecovery.resolvePresent(manager, id, "operator"));
        assertEquals(
            9,
            record(manager).getCompoundTag("stack")
                .getByte("Count"));
        assertEquals(0, count(f.dest));
    }

    @Test
    void mutationThenThrowStaysUncertainAndCannotBeInsertedAgain() throws Exception {
        Fixture f = new Fixture();
        TileEntityManager manager = pending(f, 9, false);
        String id = id(manager);
        InventoryTile destination = new InventoryTile(1) {

            @Override
            public void setInventorySlotContents(int slot, ItemStack stack) {
                super.setInventorySlotContents(slot, stack);
                throw new NullPointerException("after mutation");
            }
        };
        f.world.tiles.put(DST, destination);
        assertThrows(
            NullPointerException.class,
            () -> ItemRecovery.claim(manager, id, ForgeDirection.WEST, "operator"));
        assertEquals("uncertain", record(manager).getString("state"));
        assertEquals(DST.x, record(manager).getInteger("targetX"));
        assertEquals(0, record(manager).getInteger("targetDimension"));
        assertEquals(9, count(destination));
        assertThrows(
            IllegalStateException.class,
            () -> ItemRecovery.claim(manager, id, ForgeDirection.WEST, "operator"));
        assertEquals(9, count(destination));
    }

    @Test
    void diskSwapDuringInsertionLeavesOldRecordUncertain() throws Exception {
        Fixture f = new Fixture();
        TileEntityManager manager = pending(f, 9, false);
        ItemStack owner = manager.getDisk();
        String id = id(manager);
        InventoryTile destination = new InventoryTile(1) {

            @Override
            public void setInventorySlotContents(int slot, ItemStack stack) {
                super.setInventorySlotContents(slot, stack);
                manager.setInventorySlotContents(0, new ItemStack(new ItemDisk()));
            }
        };
        f.world.tiles.put(DST, destination);
        assertThrows(
            IllegalStateException.class,
            () -> ItemRecovery.claim(manager, id, ForgeDirection.WEST, "operator"));
        assertEquals(
            "uncertain",
            owner.getTagCompound()
                .getCompoundTag(ItemRecovery.KEY)
                .getString("state"));
        assertFalse(manager.isRunning());
        assertEquals(9, count(destination));
        manager.setInventorySlotContents(0, owner);
        assertThrows(
            IllegalStateException.class,
            () -> ItemRecovery.claim(manager, id, ForgeDirection.WEST, "operator"));
    }

    @Test
    void laterCallbackFailureRecordsOnlyTheRemainingItems() throws Exception {
        Fixture f = new Fixture();
        TileEntityManager manager = pending(f, 9, false);
        String id = id(manager);
        InventoryTile destination = new InventoryTile(2) {

            @Override
            public void setInventorySlotContents(int slot, ItemStack stack) {
                super.setInventorySlotContents(slot, stack);
                if (slot == 1) throw new IllegalStateException("second slot mutated");
            }
        };
        ItemStack existing = ItemStack.loadItemStackFromNBT(record(manager).getCompoundTag("stack"));
        existing.stackSize = 60;
        destination.setInventorySlotContents(0, existing);
        f.world.tiles.put(DST, destination);
        assertThrows(
            IllegalStateException.class,
            () -> ItemRecovery.claim(manager, id, ForgeDirection.WEST, "operator"));
        assertEquals(69, count(destination));
        assertEquals("uncertain", record(manager).getString("state"));
        assertEquals(
            5,
            record(manager).getCompoundTag("stack")
                .getByte("Count"));
        assertThrows(
            IllegalStateException.class,
            () -> ItemRecovery.claim(manager, id, ForgeDirection.WEST, "operator"));
        assertEquals(69, count(destination));
    }

    @Test
    void diskSwapDuringSimulationCannotInsertFromTheDetachedRecord() throws Exception {
        Fixture f = new Fixture();
        TileEntityManager manager = pending(f, 9, false);
        String id = id(manager);
        ItemStack owner = manager.getDisk();
        InventoryTile destination = new InventoryTile(1) {

            @Override
            public boolean isItemValidForSlot(int slot, ItemStack stack) {
                manager.setInventorySlotContents(0, new ItemStack(new ItemDisk()));
                return true;
            }
        };
        f.world.tiles.put(DST, destination);
        assertThrows(
            IllegalStateException.class,
            () -> ItemRecovery.claim(manager, id, ForgeDirection.WEST, "operator"));
        assertEquals(0, count(destination));
        assertEquals(
            "uncertain",
            owner.getTagCompound()
                .getCompoundTag(ItemRecovery.KEY)
                .getString("state"));
        assertFalse(manager.isRunning());
    }

    @Test
    void inspectionUpgradesOnlyMissingIdsAndReturnsAnIndependentCopy() throws Exception {
        Fixture f = new Fixture();
        TileEntityManager manager = pending(f, 9, false);
        record(manager).removeTag("recoveryId");
        NBTTagCompound snapshot = ItemRecovery.inspect(manager);
        String id = snapshot.getString("recoveryId");
        assertFalse(id.isEmpty());
        snapshot.getCompoundTag("stack")
            .setByte("Count", (byte) 1);
        assertEquals(
            9,
            record(manager).getCompoundTag("stack")
                .getByte("Count"));
        assertEquals(id, id(manager));
        record(manager).setInteger("version", 99);
        NBTTagCompound before = (NBTTagCompound) record(manager).copy();
        assertThrows(IllegalStateException.class, () -> ItemRecovery.inspect(manager));
        assertEquals(before, record(manager));
    }

    @Test
    void malformedPayloadAndUnknownDirectionRemainBlocked() throws Exception {
        Fixture f = new Fixture();
        TileEntityManager manager = pending(f, 9, false);
        String id = id(manager);
        assertThrows(
            IllegalArgumentException.class,
            () -> ItemRecovery.claim(manager, id, ForgeDirection.UNKNOWN, "operator"));
        record(manager).getCompoundTag("stack")
            .setByte("Count", (byte) 0);
        assertThrows(
            IllegalStateException.class,
            () -> ItemRecovery.claim(manager, id, ForgeDirection.WEST, "operator"));
        assertTrue(manager.hasPendingRecovery());
        assertEquals(0, count(f.dest));
    }

    @Test
    void commandUsesOperatorPermissionAndLoadedWorldScope() throws Exception {
        Fixture f = new Fixture();
        TileEntityManager manager = pending(f, 9, false);
        Sender sender = new Sender(f.world);
        CommandSfmRecovery command = new CommandSfmRecovery();
        assertFalse(command.canCommandSenderUseCommand(sender));
        sender.operator = true;
        assertTrue(command.canCommandSenderUseCommand(sender));
        // A moved disk must identify the original dimension, not the command sender's world.
        record(manager).setInteger("sourceDimension", -1);
        command.processCommand(sender, new String[] { "0", "64", "0", "inspect" });
        assertTrue(
            sender.messages.get(0)
                .contains("source=1,64,0 in dimension -1"));
        String id = id(manager);
        assertTrue(
            sender.messages.get(0)
                .contains(id));
        command.processCommand(sender, new String[] { "0", "64", "0", "claim", id, "west" });
        assertEquals(9, count(f.dest));
        assertTrue(
            sender.messages.get(1)
                .contains("Recovered 9"));
        assertThrows(
            WrongUsageException.class,
            () -> command.processCommand(sender, new String[] { "0", "64", "0", "bad-action" }));
        f.world.unloaded.add(MANAGER);
        assertThrows(
            CommandException.class,
            () -> command.processCommand(sender, new String[] { "0", "64", "0", "inspect" }));
    }

    static final class Sender implements ICommandSender {

        final World world;
        final List<String> messages = new ArrayList<>();
        boolean operator;

        Sender(World world) {
            this.world = world;
        }

        @Override
        public String getCommandSenderName() {
            return "fixture operator";
        }

        @Override
        public IChatComponent func_145748_c_() {
            return new ChatComponentText(getCommandSenderName());
        }

        @Override
        public void addChatMessage(IChatComponent message) {
            messages.add(message.getUnformattedText());
        }

        @Override
        public boolean canCommandSenderUseCommand(int level, String command) {
            return operator && level <= 4;
        }

        @Override
        public ChunkCoordinates getPlayerCoordinates() {
            return new ChunkCoordinates(0, 64, 0);
        }

        @Override
        public World getEntityWorld() {
            return world;
        }
    }
}
