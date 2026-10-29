package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.RegistryNamespaced;
import net.minecraft.util.RegistrySimple;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.data.ProgramGlobals;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

/** Runs the published example against real query/transfer paths and mutable fake inventories. */
class InfusionProgramTest {

    private static final Item INGREDIENT = new Item(), BLOCKER = new Item();

    static {
        try {
            Field namesField = RegistrySimple.class.getDeclaredField("registryObjects");
            namesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Object> names = (Map<String, Object>) namesField.get(Item.itemRegistry);
            names.put("sfm_infusion_program:ingredient", INGREDIENT);
            names.put("sfm_infusion_program:blocker", BLOCKER);
            Field idsField = RegistryNamespaced.class.getDeclaredField("underlyingIntegerMap");
            idsField.setAccessible(true);
            ObjectIntIdentityMap ids = (ObjectIntIdentityMap) idsField.get(Item.itemRegistry);
            ids.func_148746_a(INGREDIENT, 32015);
            ids.func_148746_a(BLOCKER, 32016);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError(failure);
        }
    }

    @Test
    void automaticRecipeRetriesPedestalsDetectsNbtOnlyCompletionAndCollectsAfterCapacityReturns() throws Exception {
        InfusionFixture infusion = new InfusionFixture();
        infusion.tick();
        assertEquals(1, infusion.phase());
        assertNull(infusion.input.getStackInSlot(0));
        assertEquals(1, count(infusion.center));
        assertEquals(2, count(infusion.input));
        assertEquals(0, count(infusion.firstPedestal));
        assertEquals(0, count(infusion.secondPedestal));

        // Keep the second pedestal unavailable for insertion while the first accepts its ingredient.
        infusion.tick();
        assertEquals(1, infusion.phase());
        assertEquals(1, count(infusion.firstPedestal));
        assertEquals(0, count(infusion.secondPedestal));
        assertEquals(1, count(infusion.input));
        for (int retry = 0; retry < 3; retry++) {
            infusion.tick();
            assertEquals(1, infusion.phase());
            assertEquals(1, count(infusion.firstPedestal), "A retry must not refill the occupied pedestal");
            assertEquals(0, count(infusion.secondPedestal));
            assertEquals(1, count(infusion.input));
        }
        infusion.secondPedestal.accepting = true;
        infusion.tick();
        assertEquals(2, infusion.phase());
        assertEquals(1, count(infusion.firstPedestal));
        assertEquals(1, count(infusion.secondPedestal));
        assertEquals(0, count(infusion.input));
        assertEquals(3, count(infusion.center) + count(infusion.firstPedestal) + count(infusion.secondPedestal));

        // Consuming ingredients alone is insufficient: the center's exact identity must change too.
        infusion.firstPedestal.setInventorySlotContents(0, null);
        infusion.secondPedestal.setInventorySlotContents(0, null);
        infusion.tick();
        assertEquals(2, infusion.phase());
        ItemStack center = infusion.center.getStackInSlot(0);
        center.stackSize = 2;
        infusion.tick();
        assertEquals(2, infusion.phase(), "A count-only change must not be treated as a finished result");
        assertEquals(64, count(infusion.output));
        center.stackSize = 1;

        NBTTagCompound completed = (NBTTagCompound) center.getTagCompound()
            .copy();
        completed.setString("stage", "finished");
        completed.setInteger("infusionProgress", 100);
        center.setTagCompound(completed);
        assertSame(INGREDIENT, center.getItem());
        assertEquals(7, center.getItemDamage());
        infusion.tick();
        assertEquals(3, infusion.phase(), "NBT-only changes should advance to collection automatically");
        assertSame(center, infusion.center.getStackInSlot(0));
        assertEquals(1, count(infusion.center));
        assertEquals(64, count(infusion.output));
        assertSame(
            BLOCKER,
            infusion.output.getStackInSlot(0)
                .getItem());
        for (int retry = 0; retry < 3; retry++) {
            infusion.tick();
            assertEquals(3, infusion.phase(), "A full destination must preserve collection mode");
            assertEquals(1, count(infusion.center));
            assertEquals(
                completed,
                infusion.center.getStackInSlot(0)
                    .getTagCompound());
            assertEquals(64, count(infusion.output));
            assertFalse(infusion.manager.hasPendingRecovery());
        }

        ItemStack removedBlockers = infusion.output.decrStackSize(0, 64);
        assertEquals(64, removedBlockers.stackSize);
        infusion.tick();
        assertEquals(0, infusion.phase());
        assertNull(infusion.center.getStackInSlot(0));
        assertEquals(1, count(infusion.output));
        ItemStack result = infusion.output.getStackInSlot(0);
        assertSame(INGREDIENT, result.getItem());
        assertEquals(7, result.getItemDamage());
        assertEquals(completed, result.getTagCompound());
        assertEquals(0, count(infusion.input) + count(infusion.firstPedestal) + count(infusion.secondPedestal));
        assertFalse(infusion.manager.hasPendingRecovery());
        infusion.tick();
        assertEquals(0, infusion.phase());
        assertEquals(1, count(infusion.output), "An idle retry must not duplicate the result");
    }

    private static final class InfusionFixture {

        final Fixture fixture = new Fixture();
        final InventoryTile input = new InventoryTile(27), output = new InventoryTile(1), center = new InventoryTile(1);
        final InventoryTile firstPedestal = new InventoryTile(1);
        final GatedPedestal secondPedestal = new GatedPedestal();
        final TileEntityManager manager;
        final Program program;
        int tick;

        InfusionFixture() throws Exception {
            fixture.add("chestin", SRC, input);
            fixture.add("chestout", DST, output);
            fixture.add("pedc", EXTRA, center);
            fixture.add("ped", new BlockPos(0, 64, -1), firstPedestal);
            fixture.add("ped", new BlockPos(0, 65, 0), secondPedestal);
            NBTTagCompound original = new NBTTagCompound();
            original.setString("stage", "ingredient");
            original.setInteger("infusionProgress", 0);
            ItemStack ingredient = new ItemStack(INGREDIENT, 1, 7);
            ingredient.setTagCompound(original);
            input.setInventorySlotContents(0, ingredient);
            input.setInventorySlotContents(1, new ItemStack(INGREDIENT, 2, 3));
            output.setInventorySlotContents(0, new ItemStack(BLOCKER, 64));
            manager = RecoveryFixtures.manager(fixture);
            String source = new String(
                Files.readAllBytes(Paths.get("examples", "logistics", "infusion-auto-nbt.sfml")),
                StandardCharsets.UTF_8);
            ProgramBuildResult built = new ProgramBuilder(source).build();
            assertTrue(built.isSuccess(), built.getErrorMessage());
            program = built.getProgram();
        }

        void tick() {
            tick += 20;
            program.tick(
                new ProgramContext(
                    fixture.world,
                    manager,
                    new CableNetwork(fixture.world, MANAGER),
                    fixture.labels,
                    tick,
                    false));
        }

        long phase() {
            ProgramGlobals globals = manager.getProgramGlobals(program);
            for (int i = 0; i < program.getGlobalDeclarations()
                .size(); i++) {
                if (program.getGlobalDeclarations()
                    .get(i)
                    .toString()
                    .startsWith("LET $phase = "))
                    return globals.read(i)
                        .asLong();
            }
            throw new AssertionError("Example has no global phase declaration");
        }
    }

    private static final class GatedPedestal extends InventoryTile {

        boolean accepting;

        GatedPedestal() {
            super(1);
        }

        @Override
        public boolean isItemValidForSlot(int slot, ItemStack stack) {
            return accepting;
        }
    }
}
