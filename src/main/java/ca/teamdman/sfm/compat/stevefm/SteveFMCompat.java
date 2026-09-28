package ca.teamdman.sfm.compat.stevefm;

import net.minecraft.block.Block;

public final class SteveFMCompat {

    private SteveFMCompat() {}

    public static boolean isCable(Block block, int meta) {
        try {
            if (vswe.stevesfactory.blocks.ModBlocks.blockCable != null) {
                return vswe.stevesfactory.blocks.ModBlocks.blockCable.isCable(block, meta);
            }
        } catch (Throwable ignored) {}
        return false;
    }
}
