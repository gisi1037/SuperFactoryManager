package ca.teamdman.sfml.data;

/** Observation boundary injected by the server execution context; expressions own no world objects. */
public interface DataQueries {

    long count(LabelReference source, ResourceSelector selector, QueryBudget budget);

    boolean empty(LabelReference source, ResourceSelector selector, QueryBudget budget);

    boolean available(LabelReference source, ResourceSelector selector, QueryBudget budget);

    default ItemContents contents(LabelReference source, ResourceSelector selector, QueryBudget budget) {
        throw new UnsupportedOperationException("Contents observations require a native item query service");
    }

    default long redstone(LabelReference source, QueryBudget budget) {
        throw new UnsupportedOperationException("Redstone observations require a native redstone query service");
    }

    default boolean powered(LabelReference source, QueryBudget budget) {
        return redstone(source, budget) > 0;
    }

    default boolean redstoneAvailable(LabelReference source, QueryBudget budget) {
        throw new UnsupportedOperationException("Redstone observations require a native redstone query service");
    }
}
