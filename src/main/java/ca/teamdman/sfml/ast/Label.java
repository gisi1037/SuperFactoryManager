package ca.teamdman.sfml.ast;

import java.util.Objects;

public final class Label implements ASTNode {

    private final String name;

    public Label(String name) {
        this.name = name != null ? name.trim() : "";
    }

    public String name() {
        return name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Label that)) return false;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
