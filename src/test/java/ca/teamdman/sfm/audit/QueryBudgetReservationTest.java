package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfml.data.QueryBudget;

class QueryBudgetReservationTest {

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
