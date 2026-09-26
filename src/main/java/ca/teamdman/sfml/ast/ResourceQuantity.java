package ca.teamdman.sfml.ast;

public final class ResourceQuantity implements ASTNode {

    public static final ResourceQuantity UNLIMITED = new ResourceQuantity(Long.MAX_VALUE, false);
    public static final ResourceQuantity ZERO = new ResourceQuantity(0, false);

    private final long amount;
    private final boolean each;

    public ResourceQuantity(long amount, boolean each) {
        this.amount = amount;
        this.each = each;
    }

    public long getAmount() {
        return amount;
    }

    public boolean isEach() {
        return each;
    }
}
