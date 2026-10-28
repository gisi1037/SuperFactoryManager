package ca.teamdman.sfml.ast;

import java.util.Locale;
import java.util.regex.Pattern;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

public final class ResourceIdentifier implements ASTNode {

    public enum Type {
        ITEM,
        FLUID,
        ANY
    }

    public static final ResourceIdentifier MATCH_ALL = new ResourceIdentifier(Type.ANY, ".*", ".*", -1, null);

    private final Type type;
    private final String namespace;
    private final String name;
    private final int metadata;
    private final String tag;

    private final Pattern namespacePattern;
    private final Pattern namePattern;
    private final Pattern tagPattern;

    public ResourceIdentifier(Type type, String namespace, String name, int metadata, String tag) {
        this.type = type != null ? type : Type.ANY;
        this.namespace = namespace != null ? namespace : ".*";
        this.name = name != null ? name : ".*";
        this.metadata = metadata;
        this.tag = tag;

        this.namespacePattern = compilePattern(this.namespace);
        this.namePattern = compilePattern(this.name);
        this.tagPattern = tag != null ? compilePattern(this.tag) : null;
    }

    public static ResourceIdentifier fromString(String raw) {
        if (raw == null || raw.isEmpty() || raw.equals("*") || raw.equals(".*")) {
            return MATCH_ALL;
        }

        raw = raw.trim();

        // Tag matching: #tag or #ore:tag
        if (raw.startsWith("#")) {
            String tag = raw.substring(1);
            if (tag.startsWith("ore:")) {
                tag = tag.substring(4);
            }
            return new ResourceIdentifier(Type.ITEM, ".*", ".*", -1, tag);
        }

        Type type = Type.ANY;
        String withoutType = raw;
        if (raw.startsWith("item:")) {
            type = Type.ITEM;
            withoutType = raw.substring(5);
        } else if (raw.startsWith("fluid:")) {
            type = Type.FLUID;
            withoutType = raw.substring(6);
        }

        String[] parts = withoutType.split(":", -1);
        String namespace = ".*";
        String name = ".*";
        int meta = -1;

        if (parts.length == 1) {
            name = parts[0];
        } else if (parts.length == 2) {
            namespace = parts[0];
            name = parts[1];
        } else if (parts.length >= 3) {
            namespace = parts[0];
            name = parts[1];
            try {
                meta = Integer.parseInt(parts[2]);
            } catch (NumberFormatException ignored) {}
        }

        if (namespace.isEmpty()) namespace = ".*";
        if (name.isEmpty()) name = ".*";

        return new ResourceIdentifier(type, namespace, name, meta, null);
    }

    private static Pattern compilePattern(String globOrRegex) {
        if (globOrRegex == null || globOrRegex.isEmpty() || globOrRegex.equals(".*") || globOrRegex.equals("*")) {
            return Pattern.compile(".*");
        }
        StringBuilder regex = new StringBuilder();
        for (int i = 0; i < globOrRegex.length(); i++) {
            char c = globOrRegex.charAt(i);
            if (c == '*') regex.append(".*");
            else if (c == '?') regex.append('.');
            else regex.append(Pattern.quote(String.valueOf(c)));
        }
        return Pattern.compile(regex.toString(), Pattern.CASE_INSENSITIVE);
    }

    public boolean matchesItem(ItemStack stack) {
        if (stack == null || stack.getItem() == null) {
            return false;
        }
        if (type == Type.FLUID) {
            return false;
        }
        if (this == MATCH_ALL
            || (tagPattern == null && ".*".equals(namespace) && ".*".equals(name) && metadata == -1)) {
            return true;
        }

        // Tag match (OreDictionary)
        if (tagPattern != null) {
            int[] oreIds = OreDictionary.getOreIDs(stack);
            for (int id : oreIds) {
                String oreName = OreDictionary.getOreName(id);
                if (tagPattern.matcher(oreName)
                    .matches()) {
                    return true;
                }
            }
            return false;
        }

        String fullId = Item.itemRegistry != null ? Item.itemRegistry.getNameForObject(stack.getItem()) : null;
        if (fullId == null && stack.getItem().delegate != null) {
            fullId = stack.getItem().delegate.name();
        }
        if (fullId == null) {
            return false;
        }

        String stackNamespace = "minecraft";
        String stackName = fullId;
        int colonIdx = fullId.indexOf(':');
        if (colonIdx >= 0) {
            stackNamespace = fullId.substring(0, colonIdx);
            stackName = fullId.substring(colonIdx + 1);
        }

        if (!namespacePattern.matcher(stackNamespace)
            .matches()) {
            return false;
        }
        if (!namePattern.matcher(stackName)
            .matches()) {
            return false;
        }

        if (metadata >= 0 && stack.getItemDamage() != metadata && metadata != OreDictionary.WILDCARD_VALUE) {
            return false;
        }

        return true;
    }

    public boolean matchesFluid(FluidStack fluid) {
        if (fluid == null || fluid.getFluid() == null) {
            return false;
        }
        if (type == Type.ITEM) {
            return false;
        }

        String fluidName = fluid.getFluid()
            .getName();
        if (namePattern.matcher(fluidName)
            .matches()) {
            return true;
        }
        return false;
    }

    public Type getType() {
        return type;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getName() {
        return name;
    }

    public int getMetadata() {
        return metadata;
    }

    public String getTag() {
        return tag;
    }

    @Override
    public String toString() {
        if (tag != null) return "#" + tag;
        return (type != Type.ANY ? type.name()
            .toLowerCase(Locale.ROOT) + ":" : "") + namespace + ":" + name + (metadata >= 0 ? ":" + metadata : "");
    }
}
