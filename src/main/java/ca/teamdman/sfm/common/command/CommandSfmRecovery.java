package ca.teamdman.sfm.common.command;

import java.util.Locale;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.util.ForgeDirection;

import ca.teamdman.sfm.SuperFactoryManager;
import ca.teamdman.sfm.common.recovery.ItemRecovery;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfm.common.util.BlockPos;

/** Operator-only, loaded-world recovery tools. Mutations require the inspected record ID. */
public final class CommandSfmRecovery extends CommandBase {

    @Override
    public String getCommandName() {
        return "sfmrecovery";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/sfmrecovery x y z [inspect|claim <id> <direction>|resolve-present <id>]";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 3) throw new WrongUsageException(getCommandUsage(sender));
        BlockPos pos = new BlockPos(parseInt(sender, args[0]), parseInt(sender, args[1]), parseInt(sender, args[2]));
        TileEntity tile = pos.getTileEntity(sender.getEntityWorld());
        if (!(tile instanceof TileEntityManager))
            throw new CommandException("No loaded SFM manager at those coordinates");
        TileEntityManager manager = (TileEntityManager) tile;
        String action = args.length == 3 ? "inspect" : args[3].toLowerCase(Locale.ROOT);
        String response;
        try {
            if ("inspect".equals(action) && args.length <= 4) {
                NBTTagCompound record = ItemRecovery.inspect(manager);
                NBTTagCompound stack = record.getCompoundTag("stack");
                response = "Item recovery " + record.getString("recoveryId")
                    + ": "
                    + record.getString("state")
                    + ", itemId="
                    + stack.getInteger("id")
                    + ", count="
                    + stack.getByte("Count")
                    + ", source="
                    + record.getInteger("sourceX")
                    + ","
                    + record.getInteger("sourceY")
                    + ","
                    + record.getInteger("sourceZ")
                    + " in dimension "
                    + record.getInteger("sourceDimension")
                    + ", slot="
                    + record.getInteger("sourceSlot")
                    + ", side="
                    + record.getInteger("sourceSide")
                    + ", stage="
                    + record.getString("stage");
                if (record.hasKey("targetX")) response += ", target=" + record.getInteger("targetX")
                    + ","
                    + record.getInteger("targetY")
                    + ","
                    + record.getInteger("targetZ")
                    + " in dimension "
                    + record.getInteger("targetDimension")
                    + ", slot="
                    + record.getInteger("targetSlot")
                    + ", side="
                    + record.getInteger("targetSide");
                if (record.hasKey("attemptStage")) response += ", last attempt=" + record.getString("attemptStage");
            } else if ("claim".equals(action) && args.length == 6) {
                ForgeDirection direction = ForgeDirection.valueOf(args[5].toUpperCase(Locale.ROOT));
                int moved = ItemRecovery.claim(manager, args[4], direction, sender.getCommandSenderName());
                response = "Recovered " + moved
                    + " items; manager remains paused."
                    + (manager.hasPendingRecovery() ? " Recovery is still pending." : " Restart when ready.");
            } else if ("resolve-present".equals(action) && args.length == 5) {
                ItemRecovery.resolvePresent(manager, args[4], sender.getCommandSenderName());
                response = "Acknowledged items already present/reconciled; no items created. Manager remains paused.";
            } else throw new WrongUsageException(getCommandUsage(sender));
        } catch (WrongUsageException e) {
            throw e;
        } catch (RuntimeException e) {
            if (!(e instanceof IllegalStateException) && !(e instanceof IllegalArgumentException))
                SuperFactoryManager.LOG.warn("Recovery callback failed at " + pos, e);
            throw new CommandException("SFM recovery: " + e.getMessage());
        }
        sender.addChatMessage(new ChatComponentText(response));
    }
}
