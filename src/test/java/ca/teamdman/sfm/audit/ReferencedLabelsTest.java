package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.tileentity.LogLevel;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.ast.Program;

class ReferencedLabelsTest {

    @Test
    void selectedReadPreservesPositionOrderDuplicatesAndMalformedEntryRules() throws Exception {
        Fixture f = new Fixture();
        TileEntityManager manager = RecoveryFixtures.manager(f);
        NBTTagCompound labels = new NBTTagCompound();
        NBTTagList positions = new NBTTagList();
        for (int[] coords : new int[][] { { 1, 64, 0 }, { 0, 64, 1 }, { 1, 64, 0 }, { 9, 9 }, { -1, 64, 0, 99 } })
            positions.appendTag(new NBTTagIntArray(coords));
        labels.setTag("selected", positions);
        labels.setString("corrupt", "not a list");
        labels.setTag("unused", positions.copy());
        manager.getDisk()
            .setTagCompound(new NBTTagCompound());
        manager.getDisk()
            .getTagCompound()
            .setTag(LabelPositionHolder.NBT_KEY, labels);
        LabelPositionHolder full = LabelPositionHolder.from(manager.getDisk());
        LabelPositionHolder selected = LabelPositionHolder
            .from(manager.getDisk(), new HashSet<>(Arrays.asList("selected", "corrupt", "missing")));
        assertEquals(Arrays.asList(SRC, EXTRA, DST), new ArrayList<>(selected.getPositions("selected")));
        assertEquals(full.getPositions("selected"), selected.getPositions("selected"));
        assertTrue(
            selected.getPositions("corrupt")
                .isEmpty());
        assertFalse(
            selected.getAllLabels()
                .containsKey("missing"));
        assertFalse(
            selected.getAllLabels()
                .containsKey("unused"));
        assertTrue(
            full.getAllLabels()
                .containsKey("unused"));
        assertTrue(
            LabelPositionHolder.from(manager.getDisk(), Collections.emptySet())
                .getAllLabels()
                .isEmpty());
    }

    @Test
    void snapshotIsLazyButCapturesAllReferencedLabelsTogether() throws Exception {
        Fixture f = new Fixture();
        TileEntityManager manager = RecoveryFixtures.manager(f);
        Program program = parse("EVERY 1 TICKS DO INPUT FROM source OUTPUT 1 TO dest END");
        ProgramContext context = context(f, manager, program);
        // Writes before the first access are visible, exactly as with the old full reader.
        LabelPositionHolder.saveTo(manager.getDisk(), f.labels);
        LabelPositionHolder snapshot = context.getLabelPositionHolder();
        assertEquals(Collections.singleton(SRC), snapshot.getPositions("source"));
        // Mutate the same NBT array after only the source was inspected. Dest must already be frozen.
        int[] coords = manager.getDisk()
            .getTagCompound()
            .getCompoundTag(LabelPositionHolder.NBT_KEY)
            .getTagList("dest", 11)
            .func_150306_c(0);
        coords[0] = EXTRA.x;
        coords[1] = EXTRA.y;
        coords[2] = EXTRA.z;
        assertEquals(
            Collections.singleton(DST),
            context.getLabelPositionHolder()
                .getPositions("dest"));
        assertEquals(
            Collections.singleton(EXTRA),
            context(f, manager, program).getLabelPositionHolder()
                .getPositions("dest"));
    }

    @Test
    void diskReplacementIsVisibleToTheNextContextWithoutChangingTheOldSnapshot() throws Exception {
        Fixture f = new Fixture();
        TileEntityManager manager = RecoveryFixtures.manager(f);
        Program program = parse("EVERY 1 TICKS DO INPUT FROM source OUTPUT 1 TO dest END");
        LabelPositionHolder.saveTo(manager.getDisk(), f.labels);
        LabelPositionHolder first = context(f, manager, program).getLabelPositionHolder();
        ItemStack replacement = manager.getDisk()
            .copy();
        LabelPositionHolder updated = new LabelPositionHolder();
        updated.addPosition("source", EXTRA);
        LabelPositionHolder.saveTo(replacement, updated);
        manager.setInventorySlotContents(0, replacement);
        assertEquals(Collections.singleton(SRC), first.getPositions("source"));
        assertEquals(
            Collections.singleton(EXTRA),
            context(f, manager, program).getLabelPositionHolder()
                .getPositions("source"));
    }

    @Test
    void compilerReferencesCoverConditionsForgetQuotedLabelsAndRedstoneOutputs() {
        Program program = parse(
            "EVERY 1 TICKS DO INPUT FROM \"source label\" " + "IF observer HAS > 0 THEN OUTPUT 1 TO \"dest label\" END "
                + "OUTPUT 15 REDSTONE LEVEL TO emitter FORGET \"source label\", forgotten END");
        assertEquals(
            new HashSet<>(Arrays.asList("source label", "observer", "dest label", "emitter", "forgotten")),
            program.getReferencedLabels());
    }

    @Test
    void scheduledExecutionReadsOnlyReferencedLabelsAndPreservesDiskData() throws Exception {
        runtimeReadsOnlyUsedLabels(false);
    }

    @Test
    void manualExecutionReadsOnlyReferencedLabelsAndPreservesDiskData() throws Exception {
        runtimeReadsOnlyUsedLabels(true);
    }

    private void runtimeReadsOnlyUsedLabels(boolean manual) throws Exception {
        Fixture f = new Fixture();
        TileEntityManager manager = RecoveryFixtures.manager(f);
        f.stock(0, 20);
        manager.saveCodeAndRecompile("EVERY 1 TICKS DO INPUT FROM source OUTPUT 1 TO dest END");
        CountingLabels labels = new CountingLabels();
        f.labels.writeToNBT(
            manager.getDisk()
                .getTagCompound());
        NBTTagCompound existing = manager.getDisk()
            .getTagCompound()
            .getCompoundTag(LabelPositionHolder.NBT_KEY);
        for (Object key : existing.func_150296_c()) labels.setTag((String) key, existing.getTag((String) key));
        for (int i = 0; i < 1000; i++) labels.setTag("unused" + i, new NBTTagList());
        manager.getDisk()
            .getTagCompound()
            .setTag(LabelPositionHolder.NBT_KEY, labels);
        NBTTagCompound before = (NBTTagCompound) manager.getDisk()
            .getTagCompound()
            .copy();
        if (manual) manager.runOnceWithLogLevel(LogLevel.OFF);
        else manager.updateEntity();
        assertEquals(2, labels.reads);
        assertEquals(19, count(f.source));
        assertEquals(1, count(f.dest));
        assertEquals(
            before,
            manager.getDisk()
                .getTagCompound());
    }

    @Test
    void roundRobinKeepsItsPositionOrderAcrossSelectiveContexts() throws Exception {
        Fixture f = new Fixture();
        TileEntityManager manager = RecoveryFixtures.manager(f);
        InventoryTile extra = new InventoryTile(3);
        f.add("dest", EXTRA, extra);
        f.stock(0, 20);
        manager.saveCodeAndRecompile("EVERY 1 TICKS DO INPUT FROM source OUTPUT 1 TO dest ROUND ROBIN BY BLOCK END");
        LabelPositionHolder.saveTo(manager.getDisk(), f.labels);
        manager.updateEntity();
        assertEquals(1, count(f.dest));
        assertEquals(0, count(extra));
        manager.updateEntity();
        assertEquals(1, count(f.dest));
        assertEquals(1, count(extra));
        assertEquals(18, count(f.source));
    }

    @Test
    void genericContextRetainsTheFullReaderForUnspecifiedReferences() throws Exception {
        Fixture f = new Fixture();
        TileEntityManager manager = RecoveryFixtures.manager(f);
        f.labels.addPosition("unused", EXTRA);
        LabelPositionHolder.saveTo(manager.getDisk(), f.labels);
        ProgramContext context = new ProgramContext(
            f.world,
            manager,
            new CableNetwork(f.world, MANAGER),
            null,
            1,
            false);
        assertEquals(
            f.labels.getAllLabels(),
            context.getLabelPositionHolder()
                .getAllLabels());
    }

    private ProgramContext context(Fixture f, TileEntityManager manager, Program program) {
        return new ProgramContext(
            f.world,
            manager,
            new CableNetwork(f.world, MANAGER),
            null,
            1,
            false,
            program.getReferencedLabels());
    }

    private static final class CountingLabels extends NBTTagCompound {

        int reads;

        @Override
        public NBTTagList getTagList(String key, int type) {
            reads++;
            return super.getTagList(key, type);
        }
    }
}
