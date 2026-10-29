package ca.teamdman.sfml.ast;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public final class TagMatcher implements ASTNode {

    private final String patternString;
    private final GlobMatcher pattern;

    public TagMatcher(String patternString) {
        this.patternString = patternString != null ? patternString : ".*";
        // Glob-only: interpreting other regex metacharacters was accidental and could broaden filters.
        this.pattern = new GlobMatcher(this.patternString);
    }

    public boolean matches(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return false;
        int[] oreIds = OreDictionary.getOreIDs(stack);
        for (int id : oreIds) {
            if (pattern.matches(OreDictionary.getOreName(id))) return true;
        }
        return false;
    }

    public String getPatternString() {
        return patternString;
    }

    @Override
    public String toString() {
        return "#" + patternString;
    }
}
