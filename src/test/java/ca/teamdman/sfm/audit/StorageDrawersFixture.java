package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.ResourceSelector;

/** Optional real Storage Drawers code in the isolated Forge runtime, with only capacity/upgrade policy supplied. */
public final class StorageDrawersFixture {

    private static final String ROOT = "com.jaquadro.minecraft.storagedrawers.";
    private static final LabelReference SOURCE = LabelReference.of("source")
        .slots(1, 1);
    private static final LabelReference TARGET = LabelReference.of("dest")
        .slots(0, 0);
    private static final DataValue ALL = DataValue.selector(ResourceSelector.item("*"));
    private static boolean configured;

    private StorageDrawersFixture() {}

    public static void virtualStacksUseBackingQuantity() throws Exception {
        for (Item item : new Item[] { Items.stick, Items.bow }) {
            Case test = new Case(item, 100);
            assertEquals(100, test.stored());
            int visible = Math.min(100, item.getItemStackLimit());
            assertEquals(visible, test.source.getStackInSlot(1).stackSize);
            assertEquals(
                1,
                test.context()
                    .transferItems(SOURCE, TARGET, ALL, 1)
                    .getMoved());
            assertEquals(99, test.stored());
            assertEquals(
                visible,
                test.source.getStackInSlot(1).stackSize,
                "The real drawer output replenishes immediately, including stack-size-one items");
            assertEquals(1, count(test.fixture.dest));
            assertFalse(test.manager.hasPendingRecovery());
        }
        Case drained = new Case(Items.stick, 1);
        assertEquals(
            1,
            drained.context()
                .transferItems(SOURCE, TARGET, ALL, 1)
                .getMoved());
        assertEquals(0, drained.stored());
        assertFalse(drained.manager.hasPendingRecovery());
    }

    public static void creativeAndFiniteRollbackConserveOwnedItems() throws Exception {
        Case creative = new Case(Items.stick, 100);
        creative.infinite = true;
        assertEquals(
            5,
            creative.context()
                .transferItems(SOURCE, TARGET, ALL, 5)
                .getMoved());
        assertEquals(Integer.MAX_VALUE, creative.stored());
        assertEquals(5, count(creative.fixture.dest));
        assertFalse(creative.manager.hasPendingRecovery());
        for (boolean infinite : new boolean[] { false, true }) {
            Case partial = new Case(Items.stick, 100);
            partial.infinite = infinite;
            InventoryTile target = shrinksAfterSimulation(1);
            partial.fixture.world.tiles.put(DST, target);
            assertEquals(
                1,
                partial.context()
                    .transferItems(SOURCE, TARGET, ALL, 5)
                    .getMoved());
            assertEquals(infinite ? Integer.MAX_VALUE : 99, partial.stored());
            assertEquals(1, count(target));
            assertFalse(partial.manager.hasPendingRecovery(), "Known unused items must return without a false pause");
        }
        Case emptyReturn = new Case(Items.stick, 1);
        emptyReturn.fixture.world.tiles.put(DST, shrinksAfterSimulation(0));
        assertEquals(
            0,
            emptyReturn.context()
                .transferItems(SOURCE, TARGET, ALL, 1)
                .getMoved());
        assertEquals(1, emptyReturn.stored(), "Rollback reestablishes a cleared ordinary drawer prototype");
        assertFalse(emptyReturn.manager.hasPendingRecovery());
    }

    private static InventoryTile shrinksAfterSimulation(int limit) {
        return new InventoryTile(1) {

            int checks;
            boolean actual;

            @Override
            public boolean isItemValidForSlot(int slot, ItemStack stack) {
                if (++checks == 2) actual = true;
                return true;
            }

            @Override
            public int getInventoryStackLimit() {
                return actual ? limit : 64;
            }
        };
    }

    private static void configure() throws Exception {
        if (configured) return;
        Field home = cpw.mods.fml.relauncher.FMLInjectionData.class.getDeclaredField("minecraftHome");
        home.setAccessible(true);
        home.set(null, new File(".").getCanonicalFile());
        Class<?> config = Class.forName(ROOT + "config.ConfigManager");
        File file = File.createTempFile("sfm-storage-drawers-", ".cfg");
        file.deleteOnExit();
        Object instance = config.getConstructor(File.class)
            .newInstance(file);
        config.getMethod("syncConfig")
            .invoke(instance);
        Class.forName(ROOT + "StorageDrawers")
            .getField("config")
            .set(null, instance);
        Class.forName(ROOT + "StorageDrawers")
            .getField("oreDictRegistry")
            .set(
                null,
                Class.forName(ROOT + "config.OreDictRegistry")
                    .getConstructor()
                    .newInstance());
        configured = true;
    }

    private static final class Case {

        final Fixture fixture = new Fixture();
        final TileEntityManager manager;
        final IInventory source;
        final Object drawer;
        boolean infinite;

        Case(Item item, int count) throws Exception {
            configure();
            for (int x = -2; x <= 4; x++)
                for (int y = 62; y <= 66; y++) for (int z = -2; z <= 2; z++) fixture.world.blocks
                    .put(new ca.teamdman.sfm.common.util.BlockPos(x, y, z), net.minecraft.init.Blocks.air);
            Class<?> tileClass = Class.forName(ROOT + "block.tile.TileEntityDrawersStandard");
            TileEntity tile = (TileEntity) tileClass.getConstructor()
                .newInstance();
            Class<?> group = Class.forName(ROOT + "api.storage.IDrawerGroup");
            Class<?> drawerType = Class.forName(ROOT + "api.storage.IDrawer");
            Class<?> provider = Class.forName(ROOT + "storage.IStorageProvider");
            Object storage = Proxy.newProxyInstance(
                provider.getClassLoader(),
                new Class<?>[] { provider },
                (proxy, method, arguments) -> {
                    if (method.getName()
                        .equals("getSlotStackCapacity")) return 256;
                    if (method.getName()
                        .equals("isVendingUnlimited")) return infinite;
                    if (method.getReturnType() == boolean.class) return false;
                    if (method.getReturnType() == int.class) return 0;
                    return null;
                });
            drawer = Class.forName(ROOT + "storage.DrawerData")
                .getConstructor(provider, int.class)
                .newInstance(storage, 0);
            Object drawers = Array.newInstance(drawerType, 1);
            Array.set(drawers, 0, drawer);
            Class<?> base = Class.forName(ROOT + "block.tile.TileEntityDrawers");
            Field drawerField = base.getDeclaredField("drawers");
            drawerField.setAccessible(true);
            drawerField.set(tile, drawers);
            Class<?> sides = Class.forName(ROOT + "inventory.ISideManager");
            Object sidePolicy = Proxy.newProxyInstance(
                sides.getClassLoader(),
                new Class<?>[] { sides },
                (proxy, method, arguments) -> new int[] { 0, 1, 2, 3, 4, 5 });
            Class<?> upgrades = Class.forName(ROOT + "storage.IUpgradeProvider");
            Object upgradePolicy = Proxy.newProxyInstance(
                upgrades.getClassLoader(),
                new Class<?>[] { upgrades },
                (proxy, method, arguments) -> false);
            Object inventory = Class.forName(ROOT + "inventory.StorageInventory")
                .getConstructor(group, sides, upgrades)
                .newInstance(tile, sidePolicy, upgradePolicy);
            Field inventoryField = base.getDeclaredField("inventory");
            inventoryField.setAccessible(true);
            inventoryField.set(tile, inventory);
            tile.setWorldObj(fixture.world);
            tile.xCoord = SRC.x;
            tile.yCoord = SRC.y;
            tile.zCoord = SRC.z;
            fixture.world.tiles.put(SRC, tile);
            source = (IInventory) tile;
            manager = RecoveryFixtures.manager(fixture);
            drawerType.getMethod("setStoredItem", ItemStack.class, int.class)
                .invoke(drawer, new ItemStack(item), count);
        }

        int stored() throws Exception {
            return (Integer) drawer.getClass()
                .getMethod("getStoredItemCount")
                .invoke(drawer);
        }

        ProgramContext context() {
            return new ProgramContext(
                fixture.world,
                manager,
                new CableNetwork(fixture.world, MANAGER),
                fixture.labels,
                20,
                false);
        }
    }
}
