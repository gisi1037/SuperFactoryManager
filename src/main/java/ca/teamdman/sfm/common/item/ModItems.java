package ca.teamdman.sfm.common.item;

import net.minecraft.item.Item;

import cpw.mods.fml.common.registry.GameRegistry;

public final class ModItems {

    public static Item disk;
    public static Item labelGun;

    public static void init() {
        disk = new ItemDisk();
        labelGun = new ItemLabelGun();

        GameRegistry.registerItem(disk, "disk");
        GameRegistry.registerItem(labelGun, "labelgun");
    }
}
