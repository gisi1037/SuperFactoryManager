package ca.teamdman.sfml.data;

import ca.teamdman.sfml.data.recipe.RecipeCatalog;

/** Observation boundary injected by the server execution context; expressions own no world objects. */
public interface DataQueries {

    default RecipeCatalog.Result recipes(ItemIdentity exactOutput, String map, QueryBudget budget) {
        return RecipeCatalog.unavailable(RecipeCatalog.Status.UNSUPPORTED);
    }

    default RecipeCatalog.Result recipe(RecipeCatalog.Token reference, QueryBudget budget) {
        return RecipeCatalog.unavailable(RecipeCatalog.Status.UNSUPPORTED);
    }

    default long fluidCount(LabelReference source, ResourceSelector selector, QueryBudget budget) {
        throw new UnsupportedOperationException("Fluid observations require a native fluid query service");
    }

    default boolean fluidEmpty(LabelReference source, ResourceSelector selector, QueryBudget budget) {
        throw new UnsupportedOperationException("Fluid observations require a native fluid query service");
    }

    default boolean fluidAvailable(LabelReference source, ResourceSelector selector, QueryBudget budget) {
        throw new UnsupportedOperationException("Fluid observations require a native fluid query service");
    }

    default FluidContents fluidContents(LabelReference source, ResourceSelector selector, QueryBudget budget) {
        throw new UnsupportedOperationException("Fluid observations require a native fluid query service");
    }

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
