package ca.teamdman.sfm.common.net;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;

import ca.teamdman.sfm.common.gui.GuiManager;
import ca.teamdman.sfm.common.tileentity.LogEntry;
import ca.teamdman.sfm.common.tileentity.LogLevel;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;

public class PacketManagerLogs implements IMessage {

    private int x;
    private int y;
    private int z;
    private String logLevel;
    private List<LogEntry> logs = new ArrayList<>();

    public PacketManagerLogs() {}

    public PacketManagerLogs(int x, int y, int z, String logLevel, List<LogEntry> logs) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.logLevel = logLevel != null ? logLevel : LogLevel.OFF.name();
        if (logs != null) {
            this.logs.addAll(logs);
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public List<LogEntry> getLogs() {
        return logs;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        logLevel = ByteBufUtils.readUTF8String(buf);
        int count = buf.readInt();
        if (count < 0 || count > 200) throw new IllegalArgumentException("Invalid log count");
        logs = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            long ts = buf.readLong();
            String lvl = ByteBufUtils.readUTF8String(buf);
            String msg = ByteBufUtils.readUTF8String(buf);
            logs.add(new LogEntry(ts, LogLevel.fromString(lvl), msg));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        ByteBufUtils.writeUTF8String(buf, logLevel);

        // Bound encoded bytes as well as entry count (UTF-8 can use multiple bytes per character).
        int count = 0;
        int bytes = 0;
        for (int i = logs.size() - 1; i >= 0 && count < 200; i--) {
            int size = clipped(
                logs.get(i)
                    .getMessage()).getBytes(java.nio.charset.StandardCharsets.UTF_8).length
                + 32;
            if (bytes + size > 24000) break;
            bytes += size;
            count++;
        }
        buf.writeInt(count);
        for (int i = logs.size() - count; i < logs.size(); i++) {
            LogEntry entry = logs.get(i);
            buf.writeLong(entry.getTimestamp());
            ByteBufUtils.writeUTF8String(
                buf,
                entry.getLevel()
                    .name());
            ByteBufUtils.writeUTF8String(buf, clipped(entry.getMessage()));
        }
    }

    private static String clipped(String message) {
        return message.length() <= 1024 ? message : message.substring(0, 1024);
    }

    public static class Handler implements IMessageHandler<PacketManagerLogs, IMessage> {

        @Override
        public IMessage onMessage(PacketManagerLogs message, MessageContext ctx) {
            handleClient(message);
            return null;
        }

        @SideOnly(Side.CLIENT)
        private void handleClient(PacketManagerLogs message) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.theWorld != null) {
                TileEntity te = mc.theWorld.getTileEntity(message.x, message.y, message.z);
                if (te instanceof TileEntityManager) {
                    TileEntityManager manager = (TileEntityManager) te;
                    manager.setLogLevel(LogLevel.fromString(message.logLevel));
                    manager.setClientLogs(message.logs);
                }
            }

            if (mc.currentScreen instanceof ca.teamdman.sfm.client.gui.GuiLogs) {
                ((ca.teamdman.sfm.client.gui.GuiLogs) mc.currentScreen).onLogsReceived(message.logLevel, message.logs);
            } else if (mc.currentScreen instanceof GuiManager) {
                ((GuiManager) mc.currentScreen).onLogsReceived(message.logLevel, message.logs);
            }
        }
    }
}
