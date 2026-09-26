package ca.teamdman.sfml.ast;

import java.util.regex.Pattern;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public final class TagMatcher implements ASTNode {

    private final String patternString;
    private final Pattern pattern;

    public TagMatcher(String patternString) {
        this.patternString = patternString != null ? patternString : ".*";
        String regex = this.patternString.replace(".", "\\.")
            .replace("*", ".*")
            .replace("?", ".");
        this.pattern = Pattern.compile("^" + regex + "$", Pattern.CASE_INSENSITIVE);
    }

    public boolean matches(ItemStack stack) {
        if (stack == null || stack.getItem() == null) {
            return false;
        }
        int[] oreIds = OreDictionary.getOreIDs(stack);
        for (int id : oreIds) {
            String oreName = OreDictionary.getOreName(id);
            if (pattern.matcher(oreName)
                .matches()) {
                return true;
            }
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
