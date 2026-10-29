package ca.teamdman.sfm.common.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;

import ca.teamdman.sfm.common.recovery.FluidRecovery;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfm.common.util.BlockPos;

/** Explicit operator-only acknowledgement of manually reconciled uncertain fluid. */
public final class CommandSfmFluidRecovery extends CommandBase {

    @Override
    public String getCommandName() {
        return "sfmfluidrecovery";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/sfmfluidrecovery x y z [inspect|resolve-present <recoveryId>]";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        // Explicit guard also protects direct callers, before sender world/tile access.
        if (!canCommandSenderUseCommand(sender)) throw new CommandException("Operator permission level 2 required");
        boolean inspect = args.length == 4 && "inspect".equals(args[3]);
        boolean resolve = args.length == 5 && "resolve-present".equals(args[3]);
        if (!inspect && !resolve) throw new WrongUsageException(getCommandUsage(sender));
        String response;
        try {
            FluidRecovery.requireServerThread();
            BlockPos pos = new BlockPos(
                parseInt(sender, args[0]),
                parseInt(sender, args[1]),
                parseInt(sender, args[2]));
            TileEntity tile = pos.getTileEntity(sender.getEntityWorld());
            if (!(tile instanceof TileEntityManager))
                throw new IllegalStateException("No loaded SFM manager at those coordinates");
            TileEntityManager manager = (TileEntityManager) tile;
            if (inspect) {
                NBTTagCompound record = FluidRecovery.inspect(manager);
                response = "Fluid recovery " + text(record, "recoveryId")
                    + ": version="
                    + record.getInteger("version")
                    + ", state="
                    + text(record, "state")
                    + ", fluid="
                    + text(record, "FluidName")
                    + ", evidence amount="
                    + record.getInteger("Amount")
                    + ", stage="
                    + text(record, "stage")
                    + ", source="
                    + position(record, "source");
                if (record.hasKey("targetX")) response += ", target=" + position(record, "target");
                response += ". Uncertain amounts are evidence only. Reconcile actual tanks before resolve-present; "
                    + "pending/unsupported/corrupt/conflicting records cannot be acknowledged. Manager remains paused.";
            } else {
                FluidRecovery.resolvePresent(manager, args[4], sender.getCommandSenderName());
                response = "Acknowledged manually reconciled fluid evidence; no fluid moved or created. Manager remains paused.";
            }
        } catch (IllegalStateException error) {
            throw new CommandException("SFM fluid recovery: " + error.getMessage());
        }
        sender.addChatMessage(new ChatComponentText(response));
    }

    private static String text(NBTTagCompound record, String key) {
        if (!record.hasKey(key, 8)) return "<missing/invalid>";
        String value = record.getString(key);
        return value.substring(0, Math.min(value.length(), 256))
            .replace('\n', ' ')
            .replace('\r', ' ');
    }

    private static String position(NBTTagCompound record, String prefix) {
        return record.getInteger(prefix + "X") + ","
            + record.getInteger(prefix + "Y")
            + ","
            + record.getInteger(prefix + "Z")
            + " in dimension "
            + record.getInteger(prefix + "Dimension")
            + ", side="
            + record.getInteger(prefix + "Side");
    }
}
