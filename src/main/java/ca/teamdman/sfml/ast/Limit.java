package ca.teamdman.sfml.ast;

public final class Limit implements ASTNode {

    public static final Limit UNLIMITED = new Limit(ResourceQuantity.UNLIMITED, ResourceQuantity.ZERO);

    private final ResourceQuantity quantity;
    private final ResourceQuantity retention;

    public Limit(ResourceQuantity quantity, ResourceQuantity retention) {
        this.quantity = quantity != null ? quantity : ResourceQuantity.UNLIMITED;
        this.retention = retention != null ? retention : ResourceQuantity.ZERO;
    }

    public ResourceQuantity getQuantity() {
        return quantity;
    }

    public ResourceQuantity getRetention() {
        return retention;
    }
}
