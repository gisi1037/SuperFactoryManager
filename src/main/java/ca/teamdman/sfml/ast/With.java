package ca.teamdman.sfml.ast;

import net.minecraft.item.ItemStack;

public final class With implements ASTNode {

    public static final With ALWAYS_TRUE = new With(null, false);

    private final TagMatcher tagMatcher;
    private final boolean negated;

    public With(TagMatcher tagMatcher, boolean negated) {
        this.tagMatcher = tagMatcher;
        this.negated = negated;
    }

    public boolean matches(ItemStack stack) {
        if (tagMatcher == null) {
            return true;
        }
        boolean matched = tagMatcher.matches(stack);
        return negated ? !matched : matched;
    }

    public TagMatcher getTagMatcher() {
        return tagMatcher;
    }

    public boolean isNegated() {
        return negated;
    }
}
