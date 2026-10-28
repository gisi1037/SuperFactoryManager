package ca.teamdman.sfm.common.program;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import ca.teamdman.sfml.ast.ResourceLimit;

/** One limit in one statement scope. Quantity EACH and RETAIN EACH are independent. */
final class TransferQuota {

    private final ResourceLimit limit;
    private long moved;
    private long retained;
    private final Map<Object, Long> movedByResource = new HashMap<>();
    private final Map<Object, Long> retainedByResource = new HashMap<>();

    TransferQuota(ResourceLimit limit) {
        this.limit = limit;
    }

    static Object key(ItemStack stack) {
        return new ItemKey(stack);
    }

    static Object key(FluidStack stack) {
        return stack.getFluid();
    }

    long reserve(Object key, long available) {
        long held = retained(key);
        long amount = Math.max(
            0,
            Math.min(
                available,
                limit.getLimit()
                    .getRetention()
                    .getAmount() - held));
        observe(key, amount);
        return amount;
    }

    void observe(Object key, long amount) {
        if (limit.getLimit()
            .getRetention()
            .isEach()) retainedByResource.merge(key, amount, Long::sum);
        else retained += amount;
    }

    long retained(Object key) {
        return limit.getLimit()
            .getRetention()
            .isEach() ? retainedByResource.getOrDefault(key, 0L) : retained;
    }

    long remaining(Object key, boolean output) {
        long used = limit.getLimit()
            .getQuantity()
            .isEach() ? movedByResource.getOrDefault(key, 0L) : moved;
        long remaining = Math.max(
            0,
            limit.getLimit()
                .getQuantity()
                .getAmount() - used);
        if (output) remaining = Math.min(
            remaining,
            Math.max(
                0,
                limit.getLimit()
                    .getRetention()
                    .getAmount() - retained(key)));
        return remaining;
    }

    void moved(Object key, long amount, boolean output) {
        if (limit.getLimit()
            .getQuantity()
            .isEach()) movedByResource.merge(key, amount, Long::sum);
        else moved += amount;
        if (output) observe(key, amount);
    }

    private static final class ItemKey {

        private final Object item;
        private final int damage;

        ItemKey(ItemStack stack) {
            item = stack.getItem();
            damage = stack.getItemDamage();
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof ItemKey)) return false;
            ItemKey key = (ItemKey) other;
            return item == key.item && damage == key.damage;
        }

        @Override
        public int hashCode() {
            return 31 * System.identityHashCode(item) + damage;
        }
    }
}
