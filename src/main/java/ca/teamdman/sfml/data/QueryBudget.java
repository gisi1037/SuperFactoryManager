package ca.teamdman.sfml.data;

/** Invocation-owned work limits shared across triggers/functions. MAX constants are the base 1x allowances. */
public final class QueryBudget {

    public static final int MAX_POSITIONS = 1024;
    public static final int MAX_PROVIDER_CALLS = 8192;
    public static final int MAX_SLOT_WORK = 16384;
    public static final int MAX_ALLOCATIONS = 16384;
    public static final int MAX_MULTIPLIER = 16;
    private final int positionLimit, providerCallLimit, slotWorkLimit, allocationLimit;
    private int positions, providerCalls, slotWork, allocations;
    private boolean reservationChild, closed;

    /** Standalone compiler/identity work keeps the base limits; live managers supply their server setting. */
    public QueryBudget() {
        this(1);
    }

    public QueryBudget(int multiplier) {
        if (multiplier < 1 || multiplier > MAX_MULTIPLIER)
            throw new IllegalArgumentException("Query budget multiplier must be between 1 and " + MAX_MULTIPLIER);
        positionLimit = MAX_POSITIONS * multiplier;
        providerCallLimit = MAX_PROVIDER_CALLS * multiplier;
        slotWorkLimit = MAX_SLOT_WORK * multiplier;
        allocationLimit = MAX_ALLOCATIONS * multiplier;
    }

    /** Reservation children inherit immutable limits; configuration changes cannot alter held credit. */
    private QueryBudget(QueryBudget owner) {
        positionLimit = owner.positionLimit;
        providerCallLimit = owner.providerCallLimit;
        slotWorkLimit = owner.slotWorkLimit;
        allocationLimit = owner.allocationLimit;
    }

    public void chargePosition() {
        chargePositions(1);
    }

    public void chargePositions(int amount) {
        requireOpen();
        positions = charge(positions, amount, positionLimit, "position");
    }

    public void chargeProvider() {
        chargeProviders(1);
    }

    public void chargeProviders(int amount) {
        requireOpen();
        providerCalls = charge(providerCalls, amount, providerCallLimit, "provider call");
    }

    public void chargeSlot() {
        chargeSlots(1);
    }

    public void chargeSlots(int amount) {
        requireOpen();
        slotWork = charge(slotWork, amount, slotWorkLimit, "slot work");
    }

    public void chargeAllocation(int amount) {
        requireOpen();
        allocations = charge(allocations, amount, allocationLimit, "allocation");
    }

    /**
     * Hold a bounded commit allowance before extracting items. Until release, other work cannot spend
     * that allowance. Closing retains consumed work and returns only unused credit; recovery has
     * already completed at that point. This is server-thread accounting, not a concurrent lock.
     */
    public Reservation reserve(int positionAllowance, int providerAllowance, int workAllowance,
        int allocationAllowance) {
        requireOpen();
        if (reservationChild) throw new IllegalStateException("Nested query reservations are unavailable");
        // Validate all counters before publishing any reservation. Two control objects are retained cost.
        int nextPositions = charge(positions, positionAllowance, positionLimit, "position");
        int nextProviders = charge(providerCalls, providerAllowance, providerCallLimit, "provider call");
        int nextWork = charge(slotWork, workAllowance, slotWorkLimit, "slot work");
        int nextAllocations = charge(
            charge(allocations, 2, allocationLimit, "allocation"),
            allocationAllowance,
            allocationLimit,
            "allocation");
        QueryBudget child = new QueryBudget(this);
        child.reservationChild = true;
        child.positions = positionLimit - positionAllowance;
        child.providerCalls = providerCallLimit - providerAllowance;
        child.slotWork = slotWorkLimit - workAllowance;
        child.allocations = allocationLimit - allocationAllowance;
        Reservation reservation = new Reservation(this, child);
        positions = nextPositions;
        providerCalls = nextProviders;
        slotWork = nextWork;
        allocations = nextAllocations;
        return reservation;
    }

    private void requireOpen() {
        if (closed) throw new IllegalStateException("Query reservation is closed");
    }

    public static final class Reservation implements AutoCloseable {

        private final QueryBudget owner, child;
        private boolean closed;

        private Reservation(QueryBudget owner, QueryBudget child) {
            this.owner = owner;
            this.child = child;
        }

        public QueryBudget budget() {
            if (closed) throw new IllegalStateException("Query reservation is closed");
            return child;
        }

        @Override
        public void close() {
            if (closed) return;
            closed = true;
            child.closed = true;
            owner.positions -= child.positionLimit - child.positions;
            owner.providerCalls -= child.providerCallLimit - child.providerCalls;
            owner.slotWork -= child.slotWorkLimit - child.slotWork;
            owner.allocations -= child.allocationLimit - child.allocations;
        }
    }

    private static int charge(int used, int amount, int limit, String kind) {
        if (amount < 0 || amount > limit - used) throw new IllegalArgumentException(
            "Query " + kind + " budget exhausted (used " + used + ", requested " + amount + ", limit " + limit + ")");
        return used + amount;
    }

    public int getPositions() {
        return positions;
    }

    public int getProviderCalls() {
        return providerCalls;
    }

    public int getSlotWork() {
        return slotWork;
    }

    public int getAllocations() {
        return allocations;
    }

    public int getPositionLimit() {
        return positionLimit;
    }

    public int getProviderCallLimit() {
        return providerCallLimit;
    }

    public int getSlotWorkLimit() {
        return slotWorkLimit;
    }

    public int getAllocationLimit() {
        return allocationLimit;
    }
}
