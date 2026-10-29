package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.gui.ContainerManager;
import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.net.PacketDebugTerminalReply;
import ca.teamdman.sfm.common.net.PacketDebugTerminalRequest;
import ca.teamdman.sfm.common.program.DebugTerminalService;
import ca.teamdman.sfm.common.program.ProgramStateStore;
import ca.teamdman.sfm.common.tileentity.LogLevel;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.ProgramStateSchema;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

class DebugTerminalTest {

    @Test
    void commandsAreAllowlistedCaptureIsExplicitAndResultsNeverErasePrintHistory() {
        TileEntityManager manager = new TileEntityManager();
        assertFalse(manager.hasDebugOutputEnabled());
        manager.getDebugConsole()
            .append("earlier PRINT");
        List<String> help = DebugTerminalService.execute(manager, "help");
        assertTrue(
            help.get(0)
                .contains("state"));
        assertTrue(
            DebugTerminalService.execute(manager, "status")
                .get(0)
                .contains("capture=OFF"));
        assertTrue(
            DebugTerminalService.execute(manager, "state")
                .get(0)
                .contains("No active"));
        assertEquals(
            Collections.singletonList("earlier PRINT"),
            manager.getDebugConsole()
                .snapshot());
        assertTrue(
            DebugTerminalService.execute(manager, "")
                .isEmpty(),
            "Polling must preserve the command result");
        DebugTerminalService.execute(manager, "capture on");
        assertTrue(manager.hasDebugOutputEnabled());
        for (String command : Arrays.asList(
            "SET $x = 1",
            "PRINT(1)",
            "state $x extra",
            "capture on extra",
            "capture maybe",
            "clear extra",
            "java System.exit(0)",
            "help extra"))
            assertTrue(
                DebugTerminalService.execute(manager, command)
                    .get(0)
                    .contains("Unknown"),
                command);
        assertEquals(
            Collections.singletonList("earlier PRINT"),
            manager.getDebugConsole()
                .snapshot());
        DebugTerminalService.execute(manager, "capture off");
        assertFalse(manager.hasDebugOutputEnabled());
        DebugTerminalService.execute(manager, "clear");
        assertTrue(
            manager.getDebugConsole()
                .snapshot()
                .isEmpty());
        assertFalse(manager.hasDebugOutputEnabled());
        assertTrue(
            DebugTerminalService.execute(manager, repeat("x", 129))
                .get(0)
                .contains("too long"));
    }

    @Test
    void stateInspectionReadsDirtyHeapValuesWithoutFlushingOrReplacingDiskNbt() throws Exception {
        ItemStack disk = new ItemStack(new Item());
        disk.setTagCompound(new NBTTagCompound());
        ProgramStateSchema schema = new ProgramStateSchema(
            Collections.singletonList(new ProgramStateSchema.Declaration("count", DataValue.integer(0))));
        ProgramStateStore state = ProgramStateStore.activate(disk, schema);
        state.write(0, DataValue.integer(123));
        TileEntityManager manager = new TileEntityManager();
        Field field = TileEntityManager.class.getDeclaredField("programState");
        field.setAccessible(true);
        field.set(manager, state);
        NBTTagCompound before = disk.getTagCompound();
        assertEquals(Collections.singletonList("$count = 123"), DebugTerminalService.execute(manager, "state"));
        assertEquals(Collections.singletonList("$count = 123"), DebugTerminalService.execute(manager, "state $count"));
        assertTrue(
            DebugTerminalService.execute(manager, "state missing")
                .get(0)
                .contains("No STATE"));
        assertTrue(state.isDirty());
        assertSame(before, disk.getTagCompound());
        assertFalse(before.hasKey(ProgramStateStore.NBT_KEY));
        assertEquals(
            123,
            state.read(0)
                .asLong());
        Field diskField = TileEntityManager.class.getDeclaredField("diskStack");
        diskField.setAccessible(true);
        diskField.set(manager, disk);
        DebugTerminalService.execute(manager, "labels");
        DebugTerminalService.execute(manager, "logs");
        assertTrue(state.isDirty(), "Metadata/log inspection must not flush live STATE");
        assertSame(before, disk.getTagCompound());
        assertFalse(before.hasKey(ProgramStateStore.NBT_KEY));
    }

    @Test
    void labelsUseExactCaseAndSpacesAndNeverRequireLiveInventoryOrWorldAccess() throws Exception {
        ItemStack disk = new ItemStack(new Item());
        LabelPositionHolder labels = new LabelPositionHolder();
        labels.addPosition("Input Chest", SRC);
        labels.addPosition("Input Chest", DST);
        LabelPositionHolder.saveTo(disk, labels);
        TileEntityManager manager = new TileEntityManager(); // No world exists for this diagnostic.
        Field diskField = TileEntityManager.class.getDeclaredField("diskStack");
        diskField.setAccessible(true);
        diskField.set(manager, disk);
        NBTTagCompound original = disk.getTagCompound();
        assertTrue(
            DebugTerminalService.execute(manager, "labels")
                .get(1)
                .contains("Input Chest: 2 saved"));
        List<String> result = DebugTerminalService.execute(manager, "labels Input Chest");
        assertEquals(3, result.size());
        assertTrue(
            result.get(0)
                .contains("Connectivity not checked"));
        assertEquals(SRC.x + ", " + SRC.y + ", " + SRC.z, result.get(1));
        assertEquals(DST.x + ", " + DST.y + ", " + DST.z, result.get(2));
        assertTrue(
            DebugTerminalService.execute(manager, "labels input chest")
                .get(0)
                .contains("No saved label"));
        assertSame(original, disk.getTagCompound());
        assertFalse(manager.hasDebugOutputEnabled());
    }

    @Test
    void labelInspectionBoundsNamesAndPositionReadsAndReportsMalformedSavedBindings() {
        ItemStack disk = new ItemStack(new Item());
        NBTTagCompound root = new NBTTagCompound(), bindings = new NBTTagCompound();
        root.setTag(LabelPositionHolder.NBT_KEY, bindings);
        disk.setTagCompound(root);
        CountingPositions positions = new CountingPositions();
        bindings.setTag("large", positions);
        List<String> result = DebugTerminalService.inspectLabels(disk, "large");
        assertEquals(16, result.size());
        assertEquals(15, positions.reads);
        assertTrue(
            result.get(0)
                .contains("15 / 10000"));
        for (int i = 0; i < 32; i++) bindings.setTag("extra" + i, new NBTTagList());
        result = DebugTerminalService.inspectLabels(disk, null);
        assertEquals(16, result.size());
        assertEquals(15, positions.reads, "Listing names must not decode their positions");
        assertTrue(
            result.get(0)
                .contains("15 / 33"));
        bindings.setString("malformed", "not a position list");
        assertTrue(
            DebugTerminalService.inspectLabels(disk, "malformed")
                .get(0)
                .contains("Invalid saved"));
        NBTTagList invalidCoordinate = new NBTTagList();
        invalidCoordinate.appendTag(new NBTTagIntArray(new int[] { 1 }));
        bindings.setTag("short", invalidCoordinate);
        assertEquals(
            "[invalid coordinate entry]",
            DebugTerminalService.inspectLabels(disk, "short")
                .get(1));
        bindings.setString("bad\n\u00a7name", "invalid");
        assertTrue(
            DebugTerminalService.inspectLabels(disk, "missing\n\u00a7name")
                .stream()
                .allMatch(line -> line.indexOf('\n') < 0 && line.indexOf('\u00a7') < 0 && line.length() <= 512));
        assertTrue(
            DebugTerminalService.inspectLabels(null, null)
                .get(0)
                .contains("No saved labels"));
    }

    @Test
    void recentLogsAreBoundedChronologicalAndReadOnlyEvenWithCaptureDisabled() {
        TileEntityManager manager = new TileEntityManager();
        assertTrue(
            DebugTerminalService.execute(manager, "logs")
                .get(0)
                .contains("No retained logs"));
        for (int i = 0; i < 30; i++) manager.log(LogLevel.WARN, "entry" + i);
        List<String> result = DebugTerminalService.execute(manager, "logs 2");
        assertEquals(Arrays.asList("[WARN] entry28", "[WARN] entry29"), result.subList(1, result.size()));
        assertEquals(
            11,
            DebugTerminalService.execute(manager, "logs")
                .size());
        assertEquals(
            16,
            DebugTerminalService.execute(manager, "logs 15")
                .size());
        manager.log(LogLevel.ERROR, "bad\n\u00a7text" + repeat("x", 3000));
        result = DebugTerminalService.execute(manager, "logs 1");
        assertEquals(2, result.size());
        assertTrue(
            result.get(1)
                .length() <= 512);
        assertFalse(
            result.get(1)
                .contains("\n"));
        assertFalse(
            result.get(1)
                .contains("\u00a7"));
        assertEquals(
            31,
            manager.getLogs()
                .size());
        assertEquals(LogLevel.OFF, manager.getLogLevel());
        assertFalse(manager.hasDebugOutputEnabled());
        for (String command : Arrays.asList("logs 0", "logs 16", "logs -1", "logs 99999999999999999999", "logs nope"))
            assertTrue(
                DebugTerminalService.execute(manager, command)
                    .get(0)
                    .contains("1 to 15"),
                command);
        assertTrue(
            DebugTerminalService.execute(manager, "logs 1 extra")
                .get(0)
                .contains("Unknown"));
        DebugTerminalService.execute(manager, "clear");
        assertEquals(
            31,
            manager.getLogs()
                .size(),
            "Clearing PRINT does not clear the engine log");
    }

    private static final class CountingPositions extends NBTTagList {

        int reads;

        @Override
        public int tagCount() {
            return 10000;
        }

        @Override
        public int func_150303_d() {
            return 11;
        }

        @Override
        public int[] func_150306_c(int index) {
            assertTrue(++reads <= 15, "Only the displayed coordinate prefix may be read");
            return new int[] { index, 64, 0 };
        }
    }

    @Test
    void authorizationRequiresTheSameOpenManagerWindowRangeAndLoadedTile() throws Exception {
        Fixture fixture = new Fixture();
        TileEntityManager manager = RecoveryFixtures.manager(fixture);
        FakePlayer player = allocate(FakePlayer.class);
        player.worldObj = fixture.world;
        player.inventory = new InventoryPlayer(player);
        player.posX = manager.xCoord + .5;
        player.posY = manager.yCoord + .5;
        player.posZ = manager.zCoord + .5;
        ContainerManager container = new ContainerManager(player.inventory, manager);
        container.windowId = 42;
        player.openContainer = container;
        assertSame(manager, authorized(player, manager, 42));
        player.isDead = true;
        assertNull(authorized(player, manager, 42));
        player.isDead = false;
        assertNull(authorized(player, manager, 41));
        assertNull(
            DebugTerminalService.authorizedManager(player, manager.xCoord + 1, manager.yCoord, manager.zCoord, 42));
        player.posX += 9;
        assertNull(authorized(player, manager, 42));
        player.posX -= 9;
        fixture.world.unloaded.add(MANAGER);
        assertNull(authorized(player, manager, 42));
        fixture.world.unloaded.clear();
        player.openContainer = null;
        assertNull(authorized(player, manager, 42));
        assertNull(DebugTerminalService.authorizedManager(null, 0, 0, 0, 1));
    }

    @Test
    void commandCodecBoundsCharactersUtf8FrameAndMalformedRequests() {
        assertThrows(IllegalArgumentException.class, () -> request(repeat("x", 129)));
        ByteBuf bytes = Unpooled.buffer();
        try {
            request(repeat("\u4e2d", 128)).toBytes(bytes);
            PacketDebugTerminalRequest decoded = new PacketDebugTerminalRequest();
            ByteBuf copy = bytes.copy();
            try {
                decoded.fromBytes(copy);
            } finally {
                copy.release();
            }
            assertTrue(decoded.valid);
            assertEquals(128, decoded.command.length());
            for (int length : new int[] { 0, 4, 27, bytes.readableBytes() - 1 }) {
                decoded = new PacketDebugTerminalRequest();
                decoded.fromBytes(bytes.slice(0, length));
                assertFalse(decoded.valid);
            }
            ByteBuf trailing = bytes.copy();
            try {
                trailing.writeByte(0);
                decoded = new PacketDebugTerminalRequest();
                decoded.fromBytes(trailing);
                assertFalse(decoded.valid);
            } finally {
                trailing.release();
            }
            bytes.clear();
            request("x").toBytes(bytes);
            bytes.setByte(bytes.writerIndex() - 1, 0xff);
            decoded = new PacketDebugTerminalRequest();
            decoded.fromBytes(bytes);
            assertFalse(decoded.valid, "Malformed UTF-8 must fail closed");
        } finally {
            bytes.release();
        }
    }

    @Test
    void replyCodecBoundsMultibyteHistoryBeforeEncodingAndRejectsCountsAndTrailingBytes() {
        PacketDebugTerminalReply reply = new PacketDebugTerminalReply(
            1,
            2,
            3,
            42,
            9,
            true,
            77,
            Collections.singletonList("status result"),
            Collections.nCopies(200, repeat("\u4e2d", 512)));
        ByteBuf bytes = Unpooled.buffer();
        try {
            reply.toBytes(bytes);
            assertTrue(bytes.readableBytes() <= PacketDebugTerminalReply.MAX_WIRE_BYTES);
            PacketDebugTerminalReply decoded = new PacketDebugTerminalReply();
            ByteBuf copy = bytes.copy();
            try {
                decoded.fromBytes(copy);
            } finally {
                copy.release();
            }
            assertEquals(Collections.singletonList("status result"), decoded.result);
            assertTrue(decoded.capturing);
            assertEquals(77, decoded.dropped);
            assertTrue(decoded.history.size() < 200);
            assertTrue(
                decoded.history.stream()
                    .allMatch(line -> line.length() <= 512));
            assertThrows(UnsupportedOperationException.class, () -> decoded.history.add("x"));
            ByteBuf badCount = bytes.copy();
            ByteBuf trailing = bytes.copy();
            try {
                badCount.setInt(37, 17);
                assertThrows(IllegalArgumentException.class, () -> new PacketDebugTerminalReply().fromBytes(badCount));
                trailing.writeByte(0);
                assertThrows(IllegalArgumentException.class, () -> new PacketDebugTerminalReply().fromBytes(trailing));
            } finally {
                badCount.release();
                trailing.release();
            }
        } finally {
            bytes.release();
        }
        ByteBuf oversized = Unpooled.buffer();
        try {
            oversized.writeZero(24001);
            assertThrows(IllegalArgumentException.class, () -> new PacketDebugTerminalReply().fromBytes(oversized));
        } finally {
            oversized.release();
        }
    }

    private static PacketDebugTerminalRequest request(String command) {
        return new PacketDebugTerminalRequest(1, 2, 3, 42, 9, command);
    }

    private static TileEntityManager authorized(FakePlayer player, TileEntityManager manager, int windowId) {
        return DebugTerminalService.authorizedManager(player, manager.xCoord, manager.yCoord, manager.zCoord, windowId);
    }

    private static String repeat(String text, int count) {
        return String.join("", Collections.nCopies(count, text));
    }
}
