package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;

import java.lang.reflect.Field;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.RegistryNamespaced;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;

import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;

final class RecoveryFixtures {

    private static final ItemDisk DISK = new ItemDisk();

    static {
        // Register only fixture identities needed by real ItemStack/TileEntity NBT codecs.
        TileEntity.addMapping(TileEntityManager.class, "sfm.manager");
        try {
            // The Forge registration entry point requires LaunchClassLoader, absent in JUnit.
            // Populate only the vanilla ID lookup exercised by ItemStack's actual NBT codec.
            Field ids = RegistryNamespaced.class.getDeclaredField("underlyingIntegerMap");
            ids.setAccessible(true);
            ObjectIntIdentityMap lookup = (ObjectIntIdentityMap) ids.get(Item.itemRegistry);
            lookup.func_148746_a(DISK, 32000);
            lookup.func_148746_a(ITEM, 32001);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    static TileEntityManager manager(Fixture f) throws Exception {
        Field provider = World.class.getDeclaredField("provider");
        provider.setAccessible(true);
        provider.set(f.world, new WorldProviderSurface());
        TileEntityManager manager = new TileEntityManager();
        manager.setWorldObj(f.world);
        manager.xCoord = MANAGER.x;
        manager.yCoord = MANAGER.y;
        manager.zCoord = MANAGER.z;
        manager.setInventorySlotContents(0, new ItemStack(DISK));
        f.world.tiles.put(MANAGER, manager);
        return manager;
    }

    private RecoveryFixtures() {}
}
