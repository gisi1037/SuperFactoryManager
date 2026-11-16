package ca.teamdman.sfm.common.storage;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import ca.teamdman.sfml.data.QueryBudget;

/** Optional mod storage behind a physical SFM adapter. No inventories or AE types enter SFML values. */
public interface StorageBackend {

    /** Shared backing identity, used to deduplicate aliases and refuse feeding a network into itself. */
    Object identity();

    boolean current(QueryBudget budget);

    default boolean supportsItems() {
        return true;
    }

    default boolean supportsFluids() {
        return true;
    }

    /** Opt in only when item callbacks support amounts greater than the item's stack limit. */
    default boolean supportsBulkItems() {
        return false;
    }

    List<StoredItem> items(QueryBudget budget);

    List<StoredFluid> fluids(QueryBudget budget);

    ItemStack extract(ItemStack requested, boolean simulate, QueryBudget budget);

    int insert(ItemStack offered, boolean simulate, QueryBudget budget);

    FluidStack drain(FluidStack requested, boolean simulate, QueryBudget budget);

    int fill(FluidStack offered, boolean simulate, QueryBudget budget);

    final class StoredItem {

        public final ItemStack item;
        public final long amount;

        public StoredItem(ItemStack item, long amount) {
            this.item = item;
            this.amount = amount;
        }
    }

    final class StoredFluid {

        public final FluidStack fluid;
        public final long amount;

        public StoredFluid(FluidStack fluid, long amount) {
            this.fluid = fluid;
            this.amount = amount;
        }
    }
}
