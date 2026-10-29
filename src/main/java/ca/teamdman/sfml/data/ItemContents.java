package ca.teamdman.sfml.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable native contents observation. The budget identity is an owner guard, not expiration enforcement
 * or transfer authority. No world, inventory, ItemStack or mutable NBT is retained. Provisional caps may
 * be reached after the shared query budget has already failed. Not exposed as an SFML value yet.
 */
public final class ItemContents {

    public static final int MAX_GROUPS = 256, MAX_LOCATIONS = 1024;
    private final QueryBudget owner;
    private final List<Group> groups;

    private ItemContents(QueryBudget owner, List<Group> groups) {
        this.owner = owner;
        this.groups = Collections.unmodifiableList(groups);
    }

    public List<Group> groups(QueryBudget sameOwner) {
        if (sameOwner == null || sameOwner != owner)
            throw new IllegalArgumentException("Item contents require their owning query budget");
        return groups;
    }

    public static final class Group {

        private final ItemIdentity item;
        private final long total;
        private final List<Location> locations;

        private Group(ItemIdentity item, long total, List<Location> locations) {
            this.item = item;
            this.total = total;
            this.locations = Collections.unmodifiableList(locations);
        }

        public ItemIdentity getItem() {
            return item;
        }

        public long getTotal() {
            return total;
        }

        public List<Location> getLocations() {
            return locations;
        }
    }

    /** Physical origin and observed amount; sideMask records selected observation sides, not extractability. */
    public static final class Location {

        public final int dimension, x, y, z, slot, sideMask;
        public final long amount;

        private Location(int dimension, int x, int y, int z, int slot, int sideMask, long amount) {
            this.dimension = dimension;
            this.x = x;
            this.y = y;
            this.z = z;
            this.slot = slot;
            this.sideMask = sideMask;
            this.amount = amount;
        }
    }

    /** Native scan accumulator. Frozen results copy its collections; subsequent additions cannot change them. */
    public static final class Builder {

        private final QueryBudget owner;
        private final MutableGroup[] buckets;
        private final List<MutableGroup> ordered;
        private int locations;

        public Builder(QueryBudget owner) {
            if (owner == null) throw new IllegalArgumentException("Item contents require a query budget");
            owner.chargeAllocation(MAX_GROUPS + 2);
            this.owner = owner;
            buckets = new MutableGroup[MAX_GROUPS];
            ordered = new ArrayList<>();
        }

        public void add(ItemIdentity item, int dimension, int x, int y, int z, int slot, int sideMask, long amount) {
            if (item == null || slot < 0 || sideMask <= 0 || (sideMask & ~63) != 0 || amount <= 0)
                throw new IllegalArgumentException("Invalid item contents observation");
            if (locations >= MAX_LOCATIONS) throw new IllegalArgumentException("Item contents location limit exceeded");
            int hash = item.hashCode();
            int bucket = (hash ^ (hash >>> 16)) & (MAX_GROUPS - 1);
            MutableGroup group = buckets[bucket];
            while (group != null) {
                owner.chargeSlot(); // Every collision/chain step is charged before any key comparison.
                if (item.matches(group.item, owner)) break;
                group = group.next;
            }
            if (group == null) {
                if (ordered.size() >= MAX_GROUPS)
                    throw new IllegalArgumentException("Item contents group limit exceeded");
                owner.chargeAllocation(4); // Group/list plus amortized ordered-list references.
                group = new MutableGroup(item, buckets[bucket]);
                buckets[bucket] = group;
                ordered.add(group);
            }
            long total = Math.addExact(group.total, amount);
            owner.chargeAllocation(3); // Immutable location plus amortized list references.
            group.locations.add(new Location(dimension, x, y, z, slot, sideMask, amount));
            group.total = total;
            locations++;
        }

        public ItemContents build() {
            owner.chargeSlots(ordered.size() + locations);
            owner.chargeAllocation(3 + ordered.size() * 4 + locations);
            List<Group> result = new ArrayList<>(ordered.size());
            for (MutableGroup group : ordered)
                result.add(new Group(group.item, group.total, new ArrayList<>(group.locations)));
            return new ItemContents(owner, result);
        }
    }

    private static final class MutableGroup {

        final ItemIdentity item;
        final MutableGroup next;
        final List<Location> locations = new ArrayList<>();
        long total;

        MutableGroup(ItemIdentity item, MutableGroup next) {
            this.item = item;
            this.next = next;
        }
    }
}
