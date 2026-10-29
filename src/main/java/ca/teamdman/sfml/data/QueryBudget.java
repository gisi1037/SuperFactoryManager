package ca.teamdman.sfml.data;

/** Invocation-owned provisional work limits, shared across due triggers and later function calls. */
public final class QueryBudget {

    public static final int MAX_POSITIONS = 1024;
    public static final int MAX_PROVIDER_CALLS = 8192;
    public static final int MAX_SLOT_WORK = 16384;
    public static final int MAX_ALLOCATIONS = 16384;
    private int positions, providerCalls, slotWork, allocations;
    private boolean reservationChild, closed;

    public void chargePosition() {
        chargePositions(1);
    }

    public void chargePositions(int amount) {
        requireOpen();
        positions = charge(positions, amount, MAX_POSITIONS, "position");
    }

    public void chargeProvider() {
        chargeProviders(1);
    }

    public void chargeProviders(int amount) {
        requireOpen();
        providerCalls = charge(providerCalls, amount, MAX_PROVIDER_CALLS, "provider call");
    }

    public void chargeSlot() {
        chargeSlots(1);
    }

    public void chargeSlots(int amount) {
        requireOpen();
        slotWork = charge(slotWork, amount, MAX_SLOT_WORK, "slot work");
    }

    public void chargeAllocation(int amount) {
        requireOpen();
        allocations = charge(allocations, amount, MAX_ALLOCATIONS, "allocation");
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
        int nextPositions = charge(positions, positionAllowance, MAX_POSITIONS, "position");
        int nextProviders = charge(providerCalls, providerAllowance, MAX_PROVIDER_CALLS, "provider call");
        int nextWork = charge(slotWork, workAllowance, MAX_SLOT_WORK, "slot work");
        int nextAllocations = charge(
            charge(allocations, 2, MAX_ALLOCATIONS, "allocation"),
            allocationAllowance,
            MAX_ALLOCATIONS,
            "allocation");
        QueryBudget child = new QueryBudget();
        child.reservationChild = true;
        child.positions = MAX_POSITIONS - positionAllowance;
        child.providerCalls = MAX_PROVIDER_CALLS - providerAllowance;
        child.slotWork = MAX_SLOT_WORK - workAllowance;
        child.allocations = MAX_ALLOCATIONS - allocationAllowance;
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
            owner.positions -= MAX_POSITIONS - child.positions;
            owner.providerCalls -= MAX_PROVIDER_CALLS - child.providerCalls;
            owner.slotWork -= MAX_SLOT_WORK - child.slotWork;
            owner.allocations -= MAX_ALLOCATIONS - child.allocations;
        }
    }

    private static int charge(int used, int amount, int limit, String kind) {
        if (amount < 0 || amount > limit - used)
            throw new IllegalArgumentException("Query " + kind + " budget exhausted");
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
}
