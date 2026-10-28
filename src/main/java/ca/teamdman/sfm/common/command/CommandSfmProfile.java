package ca.teamdman.sfm.common.command;

import java.util.Locale;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentText;

import ca.teamdman.sfm.common.diagnostics.ManagerTickProfiler;

/** Operator-only capture control. Profiling is disabled on every server start. */
public final class CommandSfmProfile extends CommandBase {

    @Override
    public String getCommandName() {
        return "sfmprofile";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/sfmprofile [start|status|stop]";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length > 1) throw new WrongUsageException(getCommandUsage(sender));
        String action = args.length == 0 ? "status" : args[0];
        ManagerTickProfiler profiler = ManagerTickProfiler.INSTANCE;
        if ("start".equals(action)) profiler.setEnabled(true);
        else if ("stop".equals(action)) profiler.setEnabled(false);
        else if (!"status".equals(action)) throw new WrongUsageException(getCommandUsage(sender));
        ManagerTickProfiler.Snapshot snapshot = profiler.snapshot();
        sender.addChatMessage(
            new ChatComponentText(
                String.format(
                    Locale.ROOT,
                    "SFM manager profiling %s: %d ticks, %d updates; total %.3f ms; per tick p50/p95/p99/max %.3f/%.3f/%.3f/%.3f ms",
                    snapshot.enabled ? "on" : "off",
                    snapshot.ticks,
                    snapshot.updates,
                    snapshot.totalNanos / 1e6,
                    snapshot.p50Nanos / 1e6,
                    snapshot.p95Nanos / 1e6,
                    snapshot.p99Nanos / 1e6,
                    snapshot.maxNanos / 1e6)));
    }
}
