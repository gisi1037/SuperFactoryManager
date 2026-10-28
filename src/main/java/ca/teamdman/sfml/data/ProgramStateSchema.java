package ca.teamdman.sfml.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Immutable declared schema; never owns current disk values. */
public final class ProgramStateSchema {

    public static final ProgramStateSchema EMPTY = new ProgramStateSchema(Collections.emptyList());
    private final List<Declaration> declarations;

    public ProgramStateSchema(List<Declaration> declarations) {
        if (declarations.size() > 256) throw new IllegalArgumentException("STATE declarations exceed 256");
        Set<String> names = new HashSet<>();
        int cells = 0, characters = 0;
        for (Declaration declaration : declarations) {
            if (!names.add(declaration.name))
                throw new IllegalArgumentException("Duplicate STATE: $" + declaration.name);
            cells = Math.addExact(
                cells,
                declaration.getType()
                    .getCellCount());
            characters = Math.addExact(characters, declaration.defaultValue.getStringCharacterCount());
            if (characters > ExpressionLimits.MAX_STATE_STRING_CHARACTERS) throw new IllegalArgumentException(
                "Persistent state string characters exceed " + ExpressionLimits.MAX_STATE_STRING_CHARACTERS);
            if (cells > 4096) throw new IllegalArgumentException("Persistent state cells exceed 4096");
        }
        this.declarations = Collections.unmodifiableList(new ArrayList<>(declarations));
    }

    public List<Declaration> getDeclarations() {
        return declarations;
    }

    public int size() {
        return declarations.size();
    }

    public static final class Declaration {

        private final String name;
        private final DataValue defaultValue;

        public Declaration(String name, DataValue defaultValue) {
            if (name == null || !name.matches("[A-Za-z_][A-Za-z0-9_]*") || defaultValue == null)
                throw new IllegalArgumentException("Invalid STATE declaration");
            this.name = name;
            this.defaultValue = defaultValue;
        }

        public String getName() {
            return name;
        }

        public DataType getType() {
            return defaultValue.getType();
        }

        public DataValue getDefaultValue() {
            return defaultValue;
        }
    }
}
