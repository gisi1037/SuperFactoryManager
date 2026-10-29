package ca.teamdman.sfm.client;

import java.util.function.Predicate;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.google.gson.JsonObject;

import ca.teamdman.sfm.common.block.ModBlocks;
import ca.teamdman.sfm.common.item.ModItems;
import ca.teamdman.sfm.common.program.ProgramDiskData;
import ca.teamdman.sfm.common.program.ProgramStateStore;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;

/** Narrow opt-in fixture; never touches existing blocks or overwrites a held item. */
final class ReferenceDiskIntegrationFixture {

    private static final int X = 520, Y = 200, Z = 0;

    private ReferenceDiskIntegrationFixture() {}

    static String run(EntityPlayerMP player, Predicate<int[][]> claim) {
        requireTestWorld(player.worldObj);
        if (!player.worldObj.blockExists(X, Y, Z) || !player.worldObj.blockExists(X + 2, Y, Z))
            return "ERROR: Teleport near 520,202,0 and wait for the fixture chunk before setup";
        if (player.inventory.getCurrentItem() != null) return "ERROR: Select an empty hotbar slot first";
        if (!claim.test(new int[][] { { X, Y, Z }, { X + 2, Y, Z } }))
            return "ERROR: Reference fixture blocks are occupied";
        World world = player.worldObj;
        if (!world.isAirBlock(X, Y, Z) || !world.isAirBlock(X + 2, Y, Z))
            return "ERROR: Reference fixture already exists; preserving it";
        world.setBlock(X, Y, Z, ModBlocks.manager);
        world.setBlock(X + 2, Y, Z, ModBlocks.programmingStation);
        TileEntityManager manager = (TileEntityManager) world.getTileEntity(X, Y, Z);
        if (manager == null) return "ERROR: Fixture placement did not create a manager; preserving held item";
        manager.setRunning(false);
        player.inventory.setInventorySlotContents(player.inventory.currentItem, new ItemStack(ModItems.disk));
        player.inventory.markDirty();
        player.inventoryContainer.detectAndSendChanges();
        player.setPositionAndUpdate(X + .5, Y + 2.5, Z + .5);
        JsonObject result = new JsonObject();
        result.addProperty("managerX", X);
        result.addProperty("stationX", X + 2);
        result.addProperty("y", Y);
        result.addProperty("z", Z);
        return result.toString();
    }

    static String inspect(EntityPlayerMP player) {
        requireTestWorld(player.worldObj);
        TileEntityManager manager = (TileEntityManager) player.worldObj.getTileEntity(X, Y, Z);
        if (manager == null) return "ERROR: No reference fixture";
        JsonObject result = new JsonObject();
        ItemStack disk = manager.getDisk();
        result.addProperty(
            "mode",
            ProgramDiskData.mode(disk)
                .name());
        result.addProperty("hasDisk", disk != null);
        result.addProperty("compiled", manager.hasCompiledProgram());
        result.addProperty("runRequested", manager.isRunning());
        result.addProperty("status", manager.getStatusMessage());
        result.addProperty("tick", manager.getManagerTick());
        result.addProperty("recovery", manager.hasPendingRecovery());
        result.addProperty(
            "sourceOnDisk",
            disk != null && disk.hasTagCompound()
                && disk.getTagCompound()
                    .hasKey(ca.teamdman.sfm.common.item.ItemDisk.NBT_PROGRAM));
        if (disk != null && ProgramDiskData.mode(disk) == ProgramDiskData.Mode.REFERENCE)
            result.addProperty("digest", ProgramDiskData.readReference(disk).digest);
        ProgramStateStore state = manager.getProgramState();
        if (state != null && state.getSchema()
            .size() > 0
            && state.read(0)
                .getType()
                .getKind() == ca.teamdman.sfml.data.DataType.Kind.INTEGER)
            result.addProperty(
                "firstIntegerState",
                state.read(0)
                    .asLong());
        // Never return grant tokens, source text or automatically reconstructed statements.
        result.addProperty(
            "logCount",
            manager.getLogs()
                .size());
        return result.toString();
    }

    private static void requireTestWorld(World world) {
        if (!(Boolean.getBoolean("sfm.testHarness") || Boolean.getBoolean("gtnh.testHarness")) || world == null
            || !world.getWorldInfo()
                .getWorldName()
                .startsWith("ReferenceDiskQA-"))
            throw new IllegalStateException("Reference fixture requires a disposable ReferenceDiskQA world");
    }
}
