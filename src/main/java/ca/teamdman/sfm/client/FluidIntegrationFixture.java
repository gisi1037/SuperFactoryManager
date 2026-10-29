package ca.teamdman.sfm.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ca.teamdman.sfm.common.block.ModBlocks;
import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.item.ModItems;
import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.net.ServerUiTasks;
import ca.teamdman.sfm.common.tileentity.LogLevel;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfm.common.util.BlockPos;

/** Opt-in functional qualification against real pack tanks, never an in-game command. */
public final class FluidIntegrationFixture {

    private FluidIntegrationFixture() {}

    public static String run(EntityPlayerMP player, Predicate<int[][]> claim) throws Exception {
        if (!(Boolean.getBoolean("sfm.testHarness") || Boolean.getBoolean("gtnh.testHarness"))
            || !ServerUiTasks.isServerThread()) throw new IllegalStateException("Server-thread test harness required");
        World world = player.worldObj;
        if (world.isRemote || !world.getWorldInfo()
            .getWorldName()
            .startsWith("FullPackAudit-")) throw new IllegalStateException("Use the disposable FullPackAudit world");
        Block tank = (Block) Class.forName("buildcraft.BuildCraftFactory")
            .getField("tankBlock")
            .get(null);
        int x = (int) Math.floor(player.posX) + 6, y = 80, z = (int) Math.floor(player.posZ) + 1;
        BlockPos anchor = new BlockPos(x, y, z), sourcePos = new BlockPos(x + 1, y, z - 1),
            destPos = new BlockPos(x + 2, y, z + 1);
        List<int[]> box = new ArrayList<>();
        box.add(new int[] { x, y, z });
        box.add(new int[] { x + 1, y, z });
        box.add(new int[] { x + 2, y, z });
        // Reserve the tank halo before placement; especially no vertical composite tanks.
        for (BlockPos pos : new BlockPos[] { sourcePos, destPos }) {
            box.add(new int[] { pos.x, pos.y, pos.z });
            for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
                BlockPos neighbor = pos.offset(side);
                box.add(new int[] { neighbor.x, neighbor.y, neighbor.z });
            }
        }
        int[][] positions = box.toArray(new int[box.size()][]);
        for (int[] pos : positions) if (!world.blockExists(pos[0], pos[1], pos[2]))
            throw new IllegalStateException("Fixture requires already-loaded chunks");
        if (!claim.test(positions)) throw new IllegalStateException("Fixture would overwrite unrelated blocks");
        world.setBlock(x, y, z, ModBlocks.manager);
        world.setBlock(x + 1, y, z, ModBlocks.cable);
        world.setBlock(x + 2, y, z, ModBlocks.cable);
        world.setBlock(sourcePos.x, sourcePos.y, sourcePos.z, tank);
        world.setBlock(destPos.x, destPos.y, destPos.z, tank);
        TileEntity sourceTile = sourcePos.getTileEntity(world), destTile = destPos.getTileEntity(world);
        if (!"buildcraft.factory.TileTank".equals(
            sourceTile.getClass()
                .getName())
            || !"buildcraft.factory.TileTank".equals(
                destTile.getClass()
                    .getName()))
            throw new IllegalStateException("Expected real BuildCraft singleton tanks");
        IFluidHandler source = (IFluidHandler) sourceTile, dest = (IFluidHandler) destTile;
        TileEntityManager manager = (TileEntityManager) anchor.getTileEntity(world);
        JsonArray phases = new JsonArray();
        try {
            int capacity = capacity(dest);
            if (capacity < 5000 || capacity(source) < 5000) throw new IllegalStateException("Tank too small");
            reset(source, water(5000));
            reset(dest, null);
            FluidStack simulated = source.drain(ForgeDirection.NORTH, water(250), false);
            int accepted = dest.fill(ForgeDirection.SOUTH, water(250), false);
            if (simulated == null || simulated.amount != 250
                || accepted != 250
                || amount(source) != 5000
                || amount(dest) != 0)
                throw new IllegalStateException("Native simulation changed stock or returned unexpected capacity");
            phase(
                manager,
                sourcePos,
                destPos,
                source,
                dest,
                "quantity",
                "INPUT fluid:water FROM source OUTPUT 1000 fluid:water TO dest",
                4000,
                1000,
                phases);
            phase(
                manager,
                sourcePos,
                destPos,
                source,
                dest,
                "destination retention",
                "INPUT fluid:water FROM source OUTPUT RETAIN 1200 fluid:water TO dest",
                3800,
                1200,
                phases);
            reset(source, water(5000));
            reset(dest, null);
            phase(
                manager,
                sourcePos,
                destPos,
                source,
                dest,
                "source retention",
                "INPUT RETAIN 1500 fluid:water FROM source OUTPUT fluid:water TO dest",
                1500,
                3500,
                phases);
            reset(source, water(5000));
            reset(dest, null);
            phase(
                manager,
                sourcePos,
                destPos,
                source,
                dest,
                "explicit faces",
                "INPUT fluid:water FROM source NORTH SIDE OUTPUT 1000 fluid:water TO dest SOUTH SIDE",
                4000,
                1000,
                phases);
            FluidStack tagged = water(5000);
            tagged.tag = new NBTTagCompound();
            tagged.tag.setString("sfm:fixture", "original water");
            reset(source, tagged);
            reset(dest, null);
            phase(
                manager,
                sourcePos,
                destPos,
                source,
                dest,
                "tagged payload",
                "INPUT fluid:water FROM source OUTPUT 1000 fluid:water TO dest",
                4000,
                1000,
                phases);
            if (!tagged.isFluidEqual(fluid(source)) || !tagged.isFluidEqual(fluid(dest)))
                throw new IllegalStateException("Native transfer changed fluid NBT");
            FluidStack otherTag = water(1000);
            otherTag.tag = new NBTTagCompound();
            otherTag.tag.setString("sfm:fixture", "incompatible water");
            reset(dest, otherTag);
            if (dest.fill(ForgeDirection.SOUTH, tagged.copy(), false) != 0)
                throw new IllegalStateException("Provider unexpectedly accepts distinct water tags");
            phase(
                manager,
                sourcePos,
                destPos,
                source,
                dest,
                "distinct tag rejected",
                "INPUT fluid:water FROM source OUTPUT 1000 fluid:water TO dest",
                4000,
                1000,
                phases);
            if (!tagged.isFluidEqual(fluid(source)) || !otherTag.isFluidEqual(fluid(dest)))
                throw new IllegalStateException("Rejected transfer changed fluid NBT");
            reset(source, water(5000));
            reset(dest, water(capacity));
            phase(
                manager,
                sourcePos,
                destPos,
                source,
                dest,
                "full destination",
                "INPUT fluid:water FROM source OUTPUT 1000 fluid:water TO dest",
                5000,
                capacity,
                phases);
            JsonObject result = new JsonObject();
            result.addProperty("passed", true);
            result.addProperty("serverThread", ServerUiTasks.isServerThread());
            result.addProperty(
                "provider",
                sourceTile.getClass()
                    .getName());
            result.addProperty("capacity", capacity);
            result.addProperty("x", x);
            result.addProperty("y", y);
            result.addProperty("z", z);
            result.addProperty("sourceX", sourcePos.x);
            result.addProperty("sourceZ", sourcePos.z);
            result.addProperty("destX", destPos.x);
            result.addProperty("destZ", destPos.z);
            result.addProperty("simulationConserved", true);
            result.addProperty("tagPreserved", true);
            result.addProperty(
                "scope",
                "Real singleton BuildCraft tanks, loaded-only server task, actual manager updates; no covers, composite tanks or injected native failures");
            result.add("phases", phases);
            return result.toString();
        } finally {
            manager.setRunning(false);
        }
    }

    private static FluidStack water(int amount) {
        return new FluidStack(FluidRegistry.WATER, amount);
    }

    private static FluidStack fluid(IFluidHandler tank) {
        FluidTankInfo[] info = tank.getTankInfo(ForgeDirection.UP);
        if (info == null || info.length != 1) throw new IllegalStateException("Expected one physical tank");
        return info[0].fluid == null ? null : info[0].fluid.copy();
    }

    private static int amount(IFluidHandler tank) {
        FluidStack stack = fluid(tank);
        return stack == null ? 0 : stack.amount;
    }

    private static int capacity(IFluidHandler tank) {
        FluidTankInfo[] info = tank.getTankInfo(ForgeDirection.UP);
        if (info == null || info.length != 1) throw new IllegalStateException("Expected one physical tank");
        return info[0].capacity;
    }

    private static void reset(IFluidHandler tank, FluidStack seed) {
        // Explicit independent-case setup; no recovery record or player inventory is consumed.
        tank.drain(ForgeDirection.UP, Integer.MAX_VALUE, true);
        if (amount(tank) != 0) throw new IllegalStateException("Native tank did not empty");
        if (seed != null && tank.fill(ForgeDirection.UP, seed.copy(), true) != seed.amount)
            throw new IllegalStateException("Native tank refused fixture seed");
    }

    private static void phase(TileEntityManager manager, BlockPos sourcePos, BlockPos destPos, IFluidHandler source,
        IFluidHandler dest, String name, String body, int sourceExpected, int destExpected, JsonArray phases) {
        if (manager.hasPendingRecovery()) throw new IllegalStateException("Unexpected fixture recovery before case");
        int sourceBefore = amount(source), destBefore = amount(dest);
        String code = "EVERY 20 TICKS DO " + body + " END";
        ItemStack disk = new ItemStack(ModItems.disk);
        LabelPositionHolder labels = new LabelPositionHolder();
        labels.addPosition("source", sourcePos);
        labels.addPosition("dest", destPos);
        LabelPositionHolder.saveTo(disk, labels);
        ItemDisk.setProgramString(disk, code);
        manager.setInventorySlotContents(0, disk);
        manager.recompile(code);
        manager.setLogLevel(LogLevel.OFF);
        if (!manager.hasCompiledProgram()) throw new IllegalStateException("Fixture compile failed: " + name);
        manager.setManagerTick(19);
        manager.setRunning(true);
        manager.updateEntity();
        boolean executed = manager.isRunning() && !manager.hasPendingRecovery();
        manager.setRunning(false);
        int sourceAfter = amount(source), destAfter = amount(dest);
        if (!executed || sourceAfter != sourceExpected
            || destAfter != destExpected
            || (long) sourceBefore + destBefore != (long) sourceAfter + destAfter)
            throw new IllegalStateException(
                "Native fluid case failed: " + name
                    + " ("
                    + sourceAfter
                    + ", "
                    + destAfter
                    + "): "
                    + manager.getStatusMessage());
        JsonObject phase = new JsonObject();
        phase.addProperty("name", name);
        phase.addProperty("sourceBefore", sourceBefore);
        phase.addProperty("destinationBefore", destBefore);
        phase.addProperty("sourceAfter", sourceAfter);
        phase.addProperty("destinationAfter", destAfter);
        phase.addProperty("program", code);
        phase.addProperty("managerTick", manager.getManagerTick());
        phase.addProperty("conserved", true);
        phases.add(phase);
    }
}
