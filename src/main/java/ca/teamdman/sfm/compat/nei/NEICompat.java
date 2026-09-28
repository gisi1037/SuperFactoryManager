package ca.teamdman.sfm.compat.nei;

import net.minecraft.item.ItemStack;

import ca.teamdman.sfm.SuperFactoryManager;
import ca.teamdman.sfm.common.block.ModBlocks;
import ca.teamdman.sfm.compat.Compat;
import codechicken.nei.api.API;

public final class NEICompat {

    private NEICompat() {}

    public static void hideItems() {
        try {
            if (Compat.isSFMLoaded()) {
                API.hideItem(new ItemStack(ModBlocks.cable));
                SuperFactoryManager.LOG
                    .info("Hidden SuperFM redundant inventory cable from NEI (using SteveFM cables).");
            }
            if (Compat.isStevesAddonsLoaded()) {
                if (stevesaddons.registry.ItemRegistry.labeler != null) {
                    API.hideItem(new ItemStack(stevesaddons.registry.ItemRegistry.labeler));
                    SuperFactoryManager.LOG
                        .info("Hidden superseded StevesAddons Labeler from NEI in favor of SuperFM Label Gun.");
                }
            }
        } catch (Throwable t) {
            SuperFactoryManager.LOG.warn("Failed to hide items in NEI: {}", t.getMessage());
        }
    }
}
