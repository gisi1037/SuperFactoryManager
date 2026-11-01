package ca.teamdman.sfm.compat.stevesaddons;

import java.util.Map;
import java.util.Set;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import ca.teamdman.sfm.SuperFactoryManager;
import ca.teamdman.sfm.common.item.ModItems;
import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.util.BlockPos;

public final class StevesAddonsCompat {

    private StevesAddonsCompat() {}

    public static void onBlockTagged(World world, int x, int y, int z, String label) {
        try {
            stevesaddons.naming.NameRegistry.saveName(world, x, y, z, label);
        } catch (Throwable t) {
            SuperFactoryManager.LOG
                .warn("Failed to sync label to StevesAddons NameRegistry at {},{},{}: {}", x, y, z, t.getMessage());
        }
    }

    public static void onBlockUntagged(World world, int x, int y, int z) {
        try {
            stevesaddons.naming.NameRegistry.removeName(world, x, y, z);
        } catch (Throwable t) {
            SuperFactoryManager.LOG
                .warn("Failed to remove label from StevesAddons NameRegistry at {},{},{}: {}", x, y, z, t.getMessage());
        }
    }

    public static String getSavedName(World world, int x, int y, int z) {
        try {
            return stevesaddons.naming.NameRegistry.getSavedName(world, x, y, z);
        } catch (Throwable t) {
            return null;
        }
    }

    public static void syncAll(World world, LabelPositionHolder holder) {
        if (holder == null || world == null) return;
        try {
            for (Map.Entry<String, Set<BlockPos>> entry : holder.getAllLabels()
                .entrySet()) {
                String label = entry.getKey();
                for (BlockPos pos : entry.getValue()) {
                    stevesaddons.naming.NameRegistry.saveName(world, pos.x, pos.y, pos.z, label);
                }
            }
        } catch (Throwable t) {
            SuperFactoryManager.LOG
                .warn("Failed to batch sync labels to StevesAddons NameRegistry: {}", t.getMessage());
        }
    }

    /** Preserve the pack's original labeler recipe; convert only plain, exact metadata-zero tools. */
    public static void integrateRecipes() {
        try {
            Item labeler = stevesaddons.registry.ItemRegistry.labeler;
            if (labeler != null) ca.teamdman.sfm.common.recipe.ModRecipes
                .convert(new ItemStack(labeler), new ItemStack(ModItems.labelGun));
        } catch (RuntimeException | LinkageError failure) {
            SuperFactoryManager.LOG.warn("Could not register Steve's Addons labeler conversions", failure);
        }
    }
}
