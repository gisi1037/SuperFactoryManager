package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.block.BlockCable;
import ca.teamdman.sfm.common.gui.ManagerEditContext;
import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.net.CableLabelData;
import ca.teamdman.sfm.common.net.CableLabelData.Action;
import ca.teamdman.sfm.common.net.CableLabelData.Mode;
import ca.teamdman.sfm.common.net.CableLabelData.Request;
import ca.teamdman.sfm.common.net.CableLabelData.View;
import ca.teamdman.sfm.common.net.ServerUiTasks;
import ca.teamdman.sfm.common.network.CableLabelEditor;
import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramStateStore;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfm.integration.mui2.CableLabelsMui2Container;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.ProgramStateSchema;
import cpw.mods.fml.common.gameevent.TickEvent;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

class CableLabelEditorTest {

    private static final BlockPos ENDPOINT = new BlockPos(-1, 64, 0);

    @BeforeEach
    void server() {
        ServerUiTasks.clear();
        ServerUiTasks.INSTANCE.tick(new TickEvent.ServerTickEvent(TickEvent.Phase.START));
    }

    @AfterEach
    void cleanup() {
        ServerUiTasks.clear();
    }

    @Test
    void explicitManagerSelectionAndBulkAssignmentsPreserveMultiLabelsAndLiveState() throws Exception {
        Fixture f = new Fixture();
        BlockPos other = new BlockPos(0, 65, 0);
        f.world.tiles.put(other, new TileEntity());
        f.initial.addPosition("old", ENDPOINT);
        f.initial.addPosition("unrelated", new BlockPos(300, 64, 0));
        LabelPositionHolder.saveTo(f.disk, f.initial);
        ProgramStateStore state = ProgramStateStore.activate(
            f.disk,
            new ProgramStateSchema(
                Collections.singletonList(new ProgramStateSchema.Declaration("count", DataValue.integer(0)))));
        set(f.manager, "programState", state);
        state.write(0, DataValue.integer(123));
        NBTTagCompound root = f.disk.getTagCompound();
        f.open();
        assertEquals(Mode.MANAGERS, f.editor.view().mode);
        assertEquals(1, f.editor.view().rows.size());
        f.act(Action.ROW, 0, "");
        f.finish();
        assertEquals(Mode.BLOCKS, f.editor.view().mode);
        assertEquals(2, f.editor.view().total);
        f.select(ENDPOINT);
        f.select(other);
        long generation = f.manager.getDiskGeneration();
        f.act(Action.ADD, 0, "shared");
        f.finish();
        LabelPositionHolder result = LabelPositionHolder.from(f.disk);
        assertTrue(
            result.getPositions("shared")
                .containsAll(Arrays.asList(ENDPOINT, other)));
        assertEquals(Collections.singleton(ENDPOINT), result.getPositions("old"));
        assertEquals(f.initial.getPositions("unrelated"), result.getPositions("unrelated"));
        assertSame(root, f.disk.getTagCompound(), "Label-only commit keeps the live root identity");
        assertSame(state, f.manager.getProgramState());
        assertSame(f.disk, f.manager.getDisk());
        assertEquals(generation, f.manager.getDiskGeneration());
        assertEquals(
            123,
            state.read(0)
                .asLong());
        assertFalse(state.isDirty(), "Pending heap STATE is flushed before complete disk sizing");
        assertTrue(state.isCurrent(f.disk));
        NBTBase envelope = root.getTag(ProgramStateStore.NBT_KEY);
        f.act(Action.REMOVE, 0, "shared");
        f.finish();
        assertTrue(
            LabelPositionHolder.from(f.disk)
                .getPositions("shared")
                .isEmpty());
        assertSame(
            envelope,
            root.getTag(ProgramStateStore.NBT_KEY),
            "Another label edit does not rewind or replace STATE");
        assertTrue(state.isCurrent(f.disk));
        assertEquals(
            123,
            ProgramStateStore.activate(f.disk, state.getSchema())
                .read(0)
                .asLong());
    }

    @Test
    void separateManagersAreNeverChosenImplicitlyOrGrantedProgramControl() throws Exception {
        Fixture f = new Fixture();
        TileEntityManager second = new TileEntityManager();
        f.place(new BlockPos(0, 64, 1), second);
        ItemStack secondDisk = new ItemStack(new ItemDisk());
        set(second, "diskStack", secondDisk);
        f.open();
        assertEquals(Mode.MANAGERS, f.editor.view().mode);
        assertEquals(2, f.editor.view().total);
        NBTTagCompound before = (NBTTagCompound) f.disk.getTagCompound()
            .copy();
        f.act(Action.ADD, 0, "bad");
        assertEquals(before, f.disk.getTagCompound());
        assertFalse((Object) new CableLabelsMui2Container(f.editor) instanceof ManagerEditContext);
        assertNull(new CableLabelsMui2Container(f.editor).slotClick(0, 0, 0, f.player));
        assertFalse(new CableLabelsMui2Container(f.editor).canDragIntoSlot(null));
    }

    @Test
    void staleLabelsTopologyDiskAndPhysicalEndpointRefuseWrites() throws Exception {
        for (int change = 0; change < 4; change++) {
            Fixture f = new Fixture();
            f.open();
            f.act(Action.ROW, 0, "");
            f.finish();
            f.select(ENDPOINT);
            NBTTagCompound before = (NBTTagCompound) f.disk.getTagCompound()
                .copy();
            if (change == 0) {
                LabelPositionHolder changed = LabelPositionHolder.from(f.disk);
                changed.addPosition("someone-else", ENDPOINT);
                LabelPositionHolder.saveTo(f.disk, changed);
            } else if (change == 1) CableNetwork.invalidate(f.world, 0, 64, 0);
            else if (change == 2) set(f.manager, "diskGeneration", f.manager.getDiskGeneration() + 1);
            else f.world.tiles.put(ENDPOINT, new TileEntity());
            f.act(Action.ADD, 0, "must-not-apply");
            assertTrue(
                LabelPositionHolder.from(f.disk)
                    .getPositions("must-not-apply")
                    .isEmpty());
            if (change != 0) assertEquals(before, f.disk.getTagCompound());
        }
    }

    @Test
    void oldRowRequestsForeignSessionsOutOfRangeAndRecoveryCannotEdit() throws Exception {
        Fixture f = new Fixture();
        f.open();
        View old = f.editor.view();
        f.act(Action.ROW, 0, "");
        f.finish();
        f.select(ENDPOINT);
        NBTTagCompound before = (NBTTagCompound) f.disk.getTagCompound()
            .copy();
        f.world.time++;
        f.editor.act(new Request(old.session, old.revision, Action.ADD, 0, "stale"));
        f.world.time++;
        f.editor.act(new Request(UUID.randomUUID(), f.editor.view().revision, Action.ADD, 0, "foreign"));
        f.player.posX = 200;
        f.act(Action.ADD, 0, "far");
        f.player.posX = 0;
        assertEquals(before, f.disk.getTagCompound());
        Fixture recovery = new Fixture();
        recovery.open();
        recovery.act(Action.ROW, 0, "");
        recovery.finish();
        recovery.select(ENDPOINT);
        recovery.disk.getTagCompound()
            .setString("sfm:recovery_item", "blocked");
        NBTTagCompound blocked = (NBTTagCompound) recovery.disk.getTagCompound()
            .copy();
        recovery.act(Action.ADD, 0, "recovery");
        assertTrue(recovery.editor.view().message.contains("recovery"));
        assertEquals(blocked, recovery.disk.getTagCompound());
    }

    @Test
    void forgedFrameworkApprovalAndDeniedClientOpenCannotCreateLabelAuthority() throws Exception {
        net.minecraft.entity.player.EntityPlayerMP player = allocate(net.minecraft.entity.player.EntityPlayerMP.class);
        net.minecraft.network.PacketBuffer buffer = new net.minecraft.network.PacketBuffer(Unpooled.buffer());
        buffer.writeInt(0);
        buffer.writeInt(64);
        buffer.writeInt(0);
        buffer.writeBoolean(true);
        buffer.writeLong(1);
        buffer.writeLong(2);
        ca.teamdman.sfm.integration.mui2.CableLabelsMui2Factory factory = ca.teamdman.sfm.integration.mui2.CableLabelsMui2Factory.INSTANCE;
        ca.teamdman.sfm.integration.mui2.CableLabelsMui2Factory.Data data = factory.readGuiData(player, buffer);
        assertFalse(factory.canInteractWith(player, data));
        com.cleanroommc.modularui.screen.UISettings settings = new com.cleanroommc.modularui.screen.UISettings();
        assertEquals(
            ca.teamdman.sfm.integration.mui2.CableLabelsMui2Factory.DENIED,
            factory.createPanel(data, null, settings)
                .getName());
        assertFalse(settings.hasCustomContainer());
        assertFalse(settings.canPlayerInteractWithUI(player));
        buffer.release();
        FakePlayer client = allocate(FakePlayer.class);
        assertEquals(
            ca.teamdman.sfm.integration.mui2.CableLabelsMui2Factory.DENIED,
            factory
                .createPanel(
                    new ca.teamdman.sfm.integration.mui2.CableLabelsMui2Factory.Data(client, 0, 64, 0),
                    null,
                    new com.cleanroommc.modularui.screen.UISettings())
                .getName());
    }

    @Test
    void denseLabelIndexAndLargeSearchHaveABoundedTickCostAndSearchEveryLabel() throws Exception {
        Fixture f = new Fixture();
        // Dense label groups include empty groups too; every group/assignment is budgeted while indexing.
        for (int i = 0; i < 350; i++) {
            f.initial.addPosition("group-" + i, ENDPOINT);
            f.initial.addReferencedLabel("empty-" + i);
        }
        LabelPositionHolder.saveTo(f.disk, f.initial);
        BlockCable cable = new BlockCable();
        for (int i = 2; i < 1600; i++) {
            BlockPos pos = new BlockPos(i, 64, 0);
            f.world.blocks.put(pos, cable);
            f.world.tiles.put(new BlockPos(i, 65, 0), new TileEntity());
        }
        // This branch starts at the selected Manager, not at another independent cable component.
        f.open();
        f.act(Action.ROW, 0, "");
        f.finish();
        assertTrue(f.editor.view().total > 1500);
        int before = f.world.blockReads;
        f.act(Action.SEARCH, 0, "group-349");
        assertEquals(Mode.BUSY, f.editor.view().mode);
        assertEquals(0, f.editor.view().rows.size());
        int ticks = f.finish();
        assertTrue(ticks >= 12, "Search is spread across ticks rather than scanning all rows on its action tick");
        assertEquals(1, f.editor.view().total);
        assertTrue(f.editor.view().rows.get(0).text.contains("..."), "Large display labels are explicitly shortened");
        assertTrue(
            f.world.blockReads - before <= ticks + 3,
            "Search reads cached metadata, apart from the root cable guard");
        View view = f.editor.view();
        f.world.time++;
        f.editor.act(new Request(view.session, view.revision, Action.SEARCH, 0, "group-0"));
        long revision = f.editor.view().revision;
        f.editor.act(new Request(view.session, revision, Action.REFRESH, 0, ""));
        assertEquals(Mode.BUSY, f.editor.view().mode);
        assertTrue(f.editor.view().message.contains("One action per tick"));
        assertTrue(
            LabelPositionHolder.from(f.disk)
                .getPositions("group-349")
                .contains(ENDPOINT));
    }

    @Test
    void oversizedCandidateRefusesAtomicallyAndNeverChangesRuntimeMetadata() throws Exception {
        Fixture f = new Fixture();
        f.disk.getTagCompound()
            .setString("padding", String.join("", Collections.nCopies(29400, "a")));
        f.open();
        f.act(Action.ROW, 0, "");
        f.finish();
        f.select(ENDPOINT);
        NBTTagCompound before = (NBTTagCompound) f.disk.getTagCompound()
            .copy();
        // Valid original disk, but 768-byte Unicode label exceeds remaining packet-safe disk space.
        f.act(Action.ADD, 0, String.join("", Collections.nCopies(256, "\u4e00")));
        assertEquals(before, f.disk.getTagCompound());
        assertTrue(f.editor.view().message.contains("30") || f.editor.view().message.contains("NBT"));
    }

    @Test
    void filteringAndInvalidationFromLaterPagesNeverExposePartialRowsOrInvalidPageBounds() throws Exception {
        Fixture f = new Fixture();
        for (int x = 2; x < 20; x++) {
            f.world.blocks.put(new BlockPos(x, 64, 0), new BlockCable());
            f.world.tiles.put(new BlockPos(x, 65, 0), new TileEntity());
        }
        f.open();
        f.act(Action.ROW, 0, "");
        f.finish();
        f.act(Action.NEXT, 0, "");
        assertEquals(8, f.editor.view().page);
        f.act(Action.SEARCH, 0, "64");
        assertEquals(Mode.BUSY, f.editor.view().mode);
        assertEquals(0, f.editor.view().page);
        assertTrue(f.editor.view().rows.isEmpty());
        f.finish();
        f.act(Action.SEARCH, 0, "");
        f.finish();
        f.act(Action.NEXT, 0, "");
        CableNetwork.invalidate(f.world);
        f.world.time++;
        f.editor.tick();
        assertEquals(Mode.STALE, f.editor.view().mode);
        assertEquals(0, f.editor.view().page);
        assertEquals(0, f.editor.view().total);
    }

    @Test
    void wireBoundsAndRoundTripsDoNotTransportSourceOrRuntimeState() {
        UUID session = UUID.randomUUID();
        for (Action action : Action.values()) {
            Request request = new Request(session, 7, action, 0, action == Action.ADD ? "a-label" : "");
            ByteBuf buffer = Unpooled.buffer();
            request.write(buffer);
            Request read = Request.read(buffer);
            assertEquals(request.session, read.session);
            assertEquals(request.revision, read.revision);
            assertEquals(request.action, read.action);
            assertEquals(request.text, read.text);
            buffer.release();
        }
        View view = new View(
            session,
            8,
            Mode.BLOCKS,
            "Manager [1, 64, 0]",
            "Ready",
            0,
            1,
            1,
            Collections.singletonList(new CableLabelData.Row("chest\nLabels: a", true)));
        ByteBuf buffer = Unpooled.buffer();
        view.write(buffer);
        assertEquals(view, View.read(buffer));
        buffer.release();
        buffer = Unpooled.buffer();
        view.write(buffer);
        buffer.writeByte(1);
        final ByteBuf trailing = buffer;
        assertThrows(IllegalArgumentException.class, () -> View.read(trailing));
        buffer.release();
        assertThrows(IllegalArgumentException.class, () -> new Request(session, 0, Action.ADD, 0, "a"));
        assertThrows(IllegalArgumentException.class, () -> new Request(session, 1, Action.ROW, 8, ""));
        assertThrows(IllegalArgumentException.class, () -> new Request(session, 1, Action.REFRESH, 0, "source"));
    }

    private static void set(Object object, String name, Object value) throws Exception {
        Field field = TileEntityManager.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(object, value);
    }

    private static final class Fixture {

        final FakeWorld world = world();
        final FakePlayer player = allocate(FakePlayer.class);
        final TileEntityManager manager = new TileEntityManager();
        final ItemStack disk = new ItemStack(new ItemDisk());
        final LabelPositionHolder initial = new LabelPositionHolder();
        CableLabelEditor editor;

        Fixture() throws Exception {
            player.worldObj = world;
            player.posY = 64;
            player.inventory = new InventoryPlayer(player);
            world.blocks.put(MANAGER, new BlockCable());
            place(new BlockPos(1, 64, 0), manager);
            world.tiles.put(ENDPOINT, new TileEntity());
            disk.setTagCompound(new NBTTagCompound());
            set(manager, "diskStack", disk);
        }

        void place(BlockPos pos, TileEntity tile) {
            tile.setWorldObj(world);
            tile.xCoord = pos.x;
            tile.yCoord = pos.y;
            tile.zCoord = pos.z;
            world.tiles.put(pos, tile);
        }

        void open() {
            editor = new CableLabelEditor(player, MANAGER, UUID.randomUUID());
            finish();
        }

        int finish() {
            int ticks = 0;
            do {
                world.time++;
                editor.tick();
                ticks++;
                assertTrue(editor.getLastWorkUnits() <= CableLabelEditor.WORK_PER_TICK);
                if (ticks > 1000) fail("Discovery did not finish");
            } while (editor.view().mode == Mode.BUSY);
            assertNotEquals(Mode.STALE, editor.view().mode, editor.view().message);
            return ticks;
        }

        void act(Action action, int row, String text) {
            world.time++;
            View view = editor.view();
            editor.act(new Request(view.session, view.revision, action, row, text));
        }

        void select(BlockPos pos) {
            // Use the server's full coordinate string, never a guessed raw coordinate in an edit request.
            for (int pages = 0; pages < 100; pages++) {
                View view = editor.view();
                for (int i = 0; i < view.rows.size(); i++) if (view.rows.get(i).text.startsWith(pos.toString() + " ")) {
                    act(Action.ROW, i, "");
                    return;
                }
                act(Action.NEXT, 0, "");
            }
            fail("Endpoint not found: " + pos);
        }
    }
}
