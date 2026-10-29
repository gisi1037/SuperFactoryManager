package ca.teamdman.sfml.ast;

import java.util.Locale;

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

    private final GlobMatcher namespacePattern;
    private final GlobMatcher namePattern;
    private final GlobMatcher tagPattern;

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
        } else if (parts.length == 3) {
            namespace = parts[0];
            name = parts[1];
            String supplied = parts[2];
            // Preserve explicit historic wildcard spellings, but never broaden a malformed filter.
            if (!supplied.isEmpty() && !supplied.equals("*") && !supplied.equals("-1")) {
                if (!supplied.matches("[0-9]+"))
                    throw new IllegalArgumentException("Resource metadata must be a nonnegative integer or wildcard");
                try {
                    meta = Integer.parseInt(supplied);
                } catch (NumberFormatException exception) {
                    throw new IllegalArgumentException("Resource metadata exceeds signed 32-bit range", exception);
                }
            }
        } else {
            throw new IllegalArgumentException("Resource selector has unsupported extra colon segments");
        }
        if (namespace.isEmpty()) namespace = ".*";
        if (name.isEmpty()) name = ".*";

        return new ResourceIdentifier(type, namespace, name, meta, null);
    }

    /** Source-only decoding; direct selector construction keeps fromString's literal semantics. */
    public static ResourceIdentifier fromSourceLiteral(String quoted) {
        if (quoted == null || quoted.length() < 2
            || quoted.charAt(0) != '"'
            || quoted.charAt(quoted.length() - 1) != '"')
            throw new IllegalArgumentException("Resource literal requires surrounding quotes");
        StringBuilder decoded = new StringBuilder(quoted.length() - 2);
        for (int i = 1; i < quoted.length() - 1; i++) {
            char c = quoted.charAt(i);
            if (c == '\\') {
                if (++i >= quoted.length() - 1) throw new IllegalArgumentException("Incomplete resource escape");
                c = quoted.charAt(i);
                switch (c) {
                    case '"':
                    case '\\':
                        break;
                    case 'n':
                        c = '\n';
                        break;
                    case 'r':
                        c = '\r';
                        break;
                    case 't':
                        c = '\t';
                        break;
                    case 'b':
                        c = '\b';
                        break;
                    case 'f':
                        c = '\f';
                        break;
                    case 'u':
                        if (i + 4 >= quoted.length() - 1)
                            throw new IllegalArgumentException("Resource Unicode escape requires four digits");
                        int value = 0;
                        for (int digit = 0; digit < 4; digit++) {
                            int next = Character.digit(quoted.charAt(++i), 16);
                            if (next < 0) throw new IllegalArgumentException("Invalid resource Unicode escape");
                            value = value * 16 + next;
                        }
                        c = (char) value;
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown resource escape: \\" + c);
                }
            }
            decoded.append(c);
        }
        return fromString(decoded.toString());
    }

    private static GlobMatcher compilePattern(String glob) {
        // Resource identifiers historically expose ".*" as an explicit match-all alias.
        return new GlobMatcher(glob == null || glob.isEmpty() || glob.equals(".*") ? "*" : glob);
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
                if (tagPattern.matches(oreName)) {
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

        if (!namespacePattern.matches(stackNamespace)) {
            return false;
        }
        if (!namePattern.matches(stackName)) {
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
        if (namePattern.matches(fluidName)) {
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
        String source = this == MATCH_ALL ? "*"
            : tag != null ? "#" + tag
                : (type != Type.ANY ? type.name()
                    .toLowerCase(Locale.ROOT) + ":" : "") + sourcePattern(namespace)
                    + ":"
                    + sourcePattern(name)
                    + (metadata >= 0 ? ":" + metadata : "");
        StringBuilder quoted = new StringBuilder(source.length() + 2).append('"');
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            switch (c) {
                case '"':
                case '\\':
                    quoted.append('\\')
                        .append(c);
                    break;
                case '\n':
                    quoted.append("\\n");
                    break;
                case '\r':
                    quoted.append("\\r");
                    break;
                case '\t':
                    quoted.append("\\t");
                    break;
                case '\b':
                    quoted.append("\\b");
                    break;
                case '\f':
                    quoted.append("\\f");
                    break;
                default:
                    if (c < 0x20 || c == 0x7f) {
                        quoted.append("\\u");
                        for (int shift = 12; shift >= 0; shift -= 4)
                            quoted.append("0123456789abcdef".charAt(c >>> shift & 15));
                    } else quoted.append(c);
            }
        }
        return quoted.append('"')
            .toString();
    }

    private static String sourcePattern(String pattern) {
        return pattern == null || pattern.isEmpty() || pattern.equals(".*") ? "*" : pattern;
    }
}
