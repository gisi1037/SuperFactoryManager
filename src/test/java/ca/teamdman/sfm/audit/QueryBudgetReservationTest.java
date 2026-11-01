package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfml.data.QueryBudget;

class QueryBudgetReservationTest {

    @Test
    void multiplierScalesEveryCapAndFailedChargesLeaveUsageUnchanged() {
        for (int multiplier : new int[] { 1, 4, 16 }) {
            QueryBudget budget = new QueryBudget(multiplier);
            assertEquals(QueryBudget.MAX_POSITIONS * multiplier, budget.getPositionLimit());
            assertEquals(QueryBudget.MAX_PROVIDER_CALLS * multiplier, budget.getProviderCallLimit());
            assertEquals(QueryBudget.MAX_SLOT_WORK * multiplier, budget.getSlotWorkLimit());
            assertEquals(QueryBudget.MAX_ALLOCATIONS * multiplier, budget.getAllocationLimit());
            budget.chargePositions(budget.getPositionLimit());
            budget.chargeProviders(budget.getProviderCallLimit());
            budget.chargeSlots(budget.getSlotWorkLimit());
            budget.chargeAllocation(budget.getAllocationLimit());
            assertThrows(IllegalArgumentException.class, budget::chargePosition);
            assertThrows(IllegalArgumentException.class, budget::chargeProvider);
            assertThrows(IllegalArgumentException.class, budget::chargeSlot);
            assertThrows(IllegalArgumentException.class, () -> budget.chargeAllocation(1));
            assertEquals(budget.getPositionLimit(), budget.getPositions());
            assertEquals(budget.getProviderCallLimit(), budget.getProviderCalls());
            assertEquals(budget.getSlotWorkLimit(), budget.getSlotWork());
            assertEquals(budget.getAllocationLimit(), budget.getAllocations());
        }
        assertEquals(QueryBudget.MAX_SLOT_WORK, new QueryBudget().getSlotWorkLimit());
    }

    @Test
    void invalidMultipliersCannotProduceUnboundedOrOverflowedAllowances() {
        for (int multiplier : new int[] { Integer.MIN_VALUE, -1, 0, 17, Integer.MAX_VALUE }) {
            assertThrows(IllegalArgumentException.class, () -> new QueryBudget(multiplier));
        }
    }

    @Test
    void scaledReservationCreditRemainsPrivateAndReturnsOnlyUnusedRecoveryAllowance() {
        QueryBudget owner = new QueryBudget(4);
        int positions = QueryBudget.MAX_POSITIONS + 7;
        int providers = QueryBudget.MAX_PROVIDER_CALLS + 7;
        int slots = QueryBudget.MAX_SLOT_WORK + 7;
        int allocations = QueryBudget.MAX_ALLOCATIONS + 7;
        QueryBudget.Reservation held = owner.reserve(positions, providers, slots, allocations);
        QueryBudget child = held.budget();
        assertEquals(owner.getPositionLimit(), child.getPositionLimit());
        assertEquals(owner.getProviderCallLimit(), child.getProviderCallLimit());
        assertEquals(owner.getSlotWorkLimit(), child.getSlotWorkLimit());
        assertEquals(owner.getAllocationLimit(), child.getAllocationLimit());
        owner.chargeSlots(owner.getSlotWorkLimit() - slots);
        assertThrows(IllegalArgumentException.class, owner::chargeSlot);
        child.chargePositions(positions - 2);
        child.chargeProviders(providers - 3);
        child.chargeSlots(slots - 5);
        child.chargeAllocation(allocations - 7);
        assertThrows(IllegalArgumentException.class, () -> child.chargeSlots(6));
        held.close();
        assertEquals(positions - 2, owner.getPositions());
        assertEquals(providers - 3, owner.getProviderCalls());
        assertEquals(owner.getSlotWorkLimit() - 5, owner.getSlotWork());
        assertEquals(allocations - 7 + 2, owner.getAllocations());
        owner.chargeSlots(5);
        assertThrows(IllegalArgumentException.class, owner::chargeSlot);
        held.close();
        assertEquals(owner.getSlotWorkLimit(), owner.getSlotWork());
        assertThrows(IllegalStateException.class, child::chargeSlot);
    }

    @Test
    void scaledReservationFailureIsAtomicEvenWhenLastCounterRejectsIt() {
        QueryBudget owner = new QueryBudget(16);
        owner.chargeAllocation(owner.getAllocationLimit() - 3);
        assertThrows(IllegalArgumentException.class, () -> owner.reserve(1, 1, 1, 2));
        assertEquals(0, owner.getPositions());
        assertEquals(0, owner.getProviderCalls());
        assertEquals(0, owner.getSlotWork());
        assertEquals(owner.getAllocationLimit() - 3, owner.getAllocations());
    }

    @Test
    void heldCreditCannotBeSpentAgainAndOnlyUnusedWorkReturns() {
        QueryBudget owner = new QueryBudget();
        owner.chargePositions(3);
        owner.chargeProviders(4);
        owner.chargeSlots(5);
        owner.chargeAllocation(6);
        QueryBudget.Reservation held = owner.reserve(12, 20, 30, 40);
        assertEquals(15, owner.getPositions());
        assertEquals(24, owner.getProviderCalls());
        assertEquals(35, owner.getSlotWork());
        assertEquals(48, owner.getAllocations());
        QueryBudget commit = held.budget();
        commit.chargePositions(2);
        commit.chargeProviders(7);
        commit.chargeSlots(11);
        commit.chargeAllocation(13);
        // Independent work during a provider callback still observes the held reservation.
        owner.chargeProviders(3);
        assertThrows(IllegalArgumentException.class, () -> owner.chargeProviders(QueryBudget.MAX_PROVIDER_CALLS - 26));
        held.close();
        assertEquals(5, owner.getPositions());
        assertEquals(14, owner.getProviderCalls());
        assertEquals(16, owner.getSlotWork());
        assertEquals(21, owner.getAllocations());
        held.close();
        assertEquals(14, owner.getProviderCalls(), "Double release cannot manufacture budget credit");
    }

    @Test
    void allowanceExhaustionCannotBorrowUnreservedCredit() {
        QueryBudget owner = new QueryBudget();
        try (QueryBudget.Reservation held = owner.reserve(2, 3, 4, 5)) {
            QueryBudget commit = held.budget();
            commit.chargePositions(2);
            commit.chargeProviders(3);
            commit.chargeSlots(4);
            commit.chargeAllocation(5);
            assertThrows(IllegalArgumentException.class, commit::chargePosition);
            assertThrows(IllegalArgumentException.class, commit::chargeProvider);
            assertThrows(IllegalArgumentException.class, commit::chargeSlot);
            assertThrows(IllegalArgumentException.class, () -> commit.chargeAllocation(1));
        }
        assertEquals(2, owner.getPositions());
        assertEquals(3, owner.getProviderCalls());
        assertEquals(4, owner.getSlotWork());
        assertEquals(7, owner.getAllocations());
    }

    @Test
    void failedReservationsAreAtomicAndRejectNegativeAllowances() {
        QueryBudget owner = new QueryBudget();
        owner.chargeAllocation(QueryBudget.MAX_ALLOCATIONS - 3);
        assertThrows(IllegalArgumentException.class, () -> owner.reserve(1, 1, 1, 2));
        assertEquals(0, owner.getPositions());
        assertEquals(0, owner.getProviderCalls());
        assertEquals(0, owner.getSlotWork());
        assertEquals(QueryBudget.MAX_ALLOCATIONS - 3, owner.getAllocations());
        assertThrows(IllegalArgumentException.class, () -> new QueryBudget().reserve(-1, 1, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new QueryBudget().reserve(1, -1, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new QueryBudget().reserve(1, 1, -1, 1));
        assertThrows(IllegalArgumentException.class, () -> new QueryBudget().reserve(1, 1, 1, -1));
    }

    @Test
    void releasedChildrenCannotBeReusedOrNestedToMintCredit() {
        QueryBudget owner = new QueryBudget();
        QueryBudget.Reservation held = owner.reserve(4, 4, 4, 4);
        QueryBudget child = held.budget();
        assertThrows(IllegalStateException.class, () -> child.reserve(1, 1, 1, 1));
        held.close();
        assertThrows(IllegalStateException.class, held::budget);
        assertThrows(IllegalStateException.class, child::chargePosition);
        assertThrows(IllegalStateException.class, child::chargeProvider);
        assertThrows(IllegalStateException.class, child::chargeSlot);
        assertThrows(IllegalStateException.class, () -> child.chargeAllocation(0));
        assertEquals(2, owner.getAllocations());
        assertEquals(0, owner.getPositions());
    }

    @Test
    void repeatedSmallCommitsDoNotConsumeUnusedSafetyMargins() {
        QueryBudget owner = new QueryBudget();
        for (int i = 0; i < 40; i++) {
            try (QueryBudget.Reservation held = owner.reserve(16, 512, 1200, 1000)) {
                QueryBudget commit = held.budget();
                commit.chargePositions(2);
                commit.chargeProviders(10);
                commit.chargeSlots(20);
                commit.chargeAllocation(6);
            }
        }
        assertEquals(80, owner.getPositions());
        assertEquals(400, owner.getProviderCalls());
        assertEquals(800, owner.getSlotWork());
        assertEquals(320, owner.getAllocations());
    }
}
