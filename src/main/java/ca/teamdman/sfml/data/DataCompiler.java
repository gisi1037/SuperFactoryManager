package ca.teamdman.sfml.data;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Compile-only name/type resolution. Execution uses slots and fixed field offsets. */
public final class DataCompiler {

    private final Map<String, Binding> state = new HashMap<>();
    private final List<ProgramStateSchema.Declaration> declarations = new ArrayList<>();
    private final Deque<Map<String, Binding>> scopes = new ArrayDeque<>();
    private int locals, maxLocals, constantCells, constantStringCharacters;
    private boolean used;

    public void beginTrigger() {
        scopes.clear();
        locals = 0;
    }

    public void pushScope() {
        scopes.push(new HashMap<>());
    }

    public void popScope() {
        scopes.pop();
    }

    public int getMaxLocals() {
        return maxLocals;
    }

    public boolean isUsed() {
        return used;
    }

    public ProgramStateSchema getStateSchema() {
        return declarations.isEmpty() ? ProgramStateSchema.EMPTY : new ProgramStateSchema(declarations);
    }

    public void declareState(String name, String expression, int line, int column) {
        used = true;
        if (state.containsKey(name)) throw error(line, column, "Duplicate STATE: $" + name);
        DataExpression compiled = compile(expression, line, column, true);
        DataValue value = compiled.evaluate(new DataRuntime(null, 0));
        Binding binding = new Binding(name, value.getType(), declarations.size(), true);
        declarations.add(new ProgramStateSchema.Declaration(name, value));
        getStateSchema(); // Bounds before publishing another declaration.
        state.put(name, binding);
    }

    public Binding declareLocal(String name, DataType type) {
        used = true;
        if (find(name) != null) throw new IllegalArgumentException("Duplicate/shadowed variable: $" + name);
        if (locals >= 256) throw new IllegalArgumentException("Local bindings exceed 256");
        Binding binding = new Binding(name, type, locals++, false);
        maxLocals = Math.max(maxLocals, locals);
        scopes.peek()
            .put(name, binding);
        return binding;
    }

    private Binding find(String name) {
        for (Map<String, Binding> scope : scopes) {
            Binding binding = scope.get(name);
            if (binding != null) return binding;
        }
        return state.get(name);
    }

    public DataExpression compile(String source, int line, int column) {
        return compile(source, line, column, false);
    }

    private DataExpression compile(String source, int line, int column, boolean constantOnly) {
        used = true;
        try {
            Parser parser = new Parser(source, constantOnly);
            Node node = parser.expression();
            parser.end();
            if (node.constant) {
                try {
                    DataValue value = node.evaluate(new DataRuntime(null, 0));
                    int cells = value.getType()
                        .getCellCount();
                    if (cells > 16384 - constantCells)
                        throw new IllegalArgumentException("Constant pool exceeds 16384 cells");
                    constantCells += cells;
                    node = literal(value);
                } catch (ArithmeticException ex) {
                    if (constantOnly) throw ex;
                } catch (IllegalArgumentException ex) {
                    // Keep fault timing for a constant operation in a conditional branch.
                    if (constantOnly || ex.getMessage()
                        .startsWith("Constant pool")) throw ex;
                }
            }
            final Node compiled = node;
            return new DataExpression() {

                @Override
                public DataType getType() {
                    return compiled.type;
                }

                @Override
                public DataValue evaluate(DataRuntime runtime) {
                    try {
                        return compiled.evaluate(runtime);
                    } catch (ArithmeticException ex) {
                        throw error(line, column, "Arithmetic overflow or division by zero");
                    } catch (IllegalArgumentException ex) {
                        throw error(line, column, ex.getMessage());
                    }
                }

                @Override
                public String toString() {
                    return source.trim();
                }
            };
        } catch (IllegalArgumentException ex) {
            throw error(line, column, ex.getMessage());
        }
    }

    public Assignment assignment(String target, DataExpression value, int line, int column) {
        used = true;
        try {
            Parser parser = new Parser(target, false);
            String name = parser.take();
            if (!name.startsWith("$")) throw new IllegalArgumentException("SET requires a variable path");
            Binding binding = find(name.substring(1));
            if (binding == null) throw new IllegalArgumentException("Unknown variable: " + name);
            DataType type = binding.type;
            List<Node> indices = new ArrayList<>();
            while (parser.peek(".") || parser.peek("[")) {
                if (parser.accept(".")) {
                    int index = type.fieldIndex(parser.take());
                    indices.add(literal(DataValue.integer(index)));
                    type = type.getFieldType(index);
                } else {
                    parser.expect("[");
                    if (type.getKind() != DataType.Kind.ARRAY)
                        throw new IllegalArgumentException("Index requires an array");
                    Node index = parser.expression();
                    require(index.type, DataType.INTEGER);
                    parser.expect("]");
                    indices.add(index);
                    type = type.getElementType();
                }
            }
            parser.end();
            require(value.getType(), type);
            return new Assignment(binding, indices, value, line, column, target);
        } catch (IllegalArgumentException ex) {
            throw error(line, column, ex.getMessage());
        }
    }

    public static final class Binding {

        public final String name;
        public final DataType type;
        public final int slot;
        public final boolean persistent;

        private Binding(String name, DataType type, int slot, boolean persistent) {
            this.name = name;
            this.type = type;
            this.slot = slot;
            this.persistent = persistent;
        }

        public void write(DataRuntime runtime, DataValue value) {
            require(value.getType(), type);
            runtime.write(persistent, slot, value);
        }
    }

    public static final class Assignment {

        private final Binding binding;
        private final List<Node> indices;
        private final DataExpression rhs;
        private final int line, column;
        private final String target;

        private Assignment(Binding binding, List<Node> indices, DataExpression rhs, int line, int column,
            String target) {
            this.binding = binding;
            this.indices = indices;
            this.rhs = rhs;
            this.line = line;
            this.column = column;
            this.target = target;
        }

        public void execute(DataRuntime runtime) {
            try {
                if (indices.isEmpty()) {
                    binding.write(runtime, rhs.evaluate(runtime));
                    return;
                }
                DataValue[] parents = new DataValue[indices.size()];
                int[] offsets = new int[indices.size()];
                int cells = 0;
                if (!indices.isEmpty()) {
                    DataValue cursor = runtime.read(binding.persistent, binding.slot);
                    for (int i = 0; i < indices.size(); i++) {
                        long index = indices.get(i)
                            .evaluate(runtime)
                            .asLong();
                        runtime.work(1);
                        if (index < 0 || index >= cursor.size())
                            throw new IllegalArgumentException("Index out of range: " + index);
                        parents[i] = cursor;
                        offsets[i] = (int) index;
                        cells = Math.addExact(cells, cursor.size());
                        cursor = cursor.get((int) index);
                    }
                }
                DataValue replacement = rhs.evaluate(runtime);
                runtime.allocate(cells);
                // Reserve root publication before any copy or state mutation.
                runtime.work(1);
                for (int i = parents.length - 1; i >= 0; i--) replacement = parents[i].with(offsets[i], replacement);
                runtime.publish(binding.persistent, binding.slot, replacement);
            } catch (ArithmeticException ex) {
                throw error(line, column, "Arithmetic overflow");
            } catch (IllegalArgumentException ex) {
                throw error(line, column, ex.getMessage());
            }
        }

        @Override
        public String toString() {
            return "SET " + target + " = " + rhs;
        }
    }

    private static IllegalArgumentException error(int line, int column, String message) {
        return new IllegalArgumentException("Line " + line + ", Col " + column + ": " + message);
    }

    private static void require(DataType actual, DataType expected) {
        if (!actual.equals(expected))
            throw new IllegalArgumentException("Type/shape mismatch: expected " + expected + ", got " + actual);
    }

    private abstract static class Node implements DataExpression {

        final DataType type;
        final int depth;
        final boolean constant;

        Node(DataType type, int depth, boolean constant) {
            if (depth > 32) throw new IllegalArgumentException("Expression nesting exceeds 32");
            this.type = type;
            this.depth = depth;
            this.constant = constant;
        }

        @Override
        public DataType getType() {
            return type;
        }
    }

    private static Node literal(DataValue value) {
        return new Node(value.getType(), 1, true) {

            @Override
            public DataValue evaluate(DataRuntime runtime) {
                runtime.work(1);
                return value;
            }
        };
    }

    private Node variable(String name, boolean constantOnly) {
        if (constantOnly) throw new IllegalArgumentException("STATE defaults must be constant (no variable reads)");
        Binding binding = find(name);
        if (binding == null) throw new IllegalArgumentException("Unknown variable: $" + name);
        return new Node(binding.type, 1, false) {

            @Override
            public DataValue evaluate(DataRuntime runtime) {
                return runtime.read(binding.persistent, binding.slot);
            }
        };
    }

    private static Node unary(String operator, Node value) {
        require(value.type, operator.equals("NOT") ? DataType.BOOLEAN : DataType.INTEGER);
        return new Node(value.type, value.depth + 1, value.constant) {

            @Override
            public DataValue evaluate(DataRuntime runtime) {
                DataValue operand = value.evaluate(runtime);
                runtime.work(1);
                if (operator.equals("NOT")) return DataValue.bool(!operand.asBoolean());
                return DataValue.integer(operator.equals("-") ? Math.negateExact(operand.asLong()) : operand.asLong());
            }
        };
    }

    private static Node binary(String operator, Node left, Node right) {
        boolean logic = operator.equals("AND") || operator.equals("OR");
        boolean concatenate = operator.equals("+") && left.type == DataType.STRING;
        boolean compare = operator.equals("=") || operator.equals("<")
            || operator.equals(">")
            || operator.equals("<=")
            || operator.equals(">=");
        if (logic) {
            require(left.type, DataType.BOOLEAN);
            require(right.type, DataType.BOOLEAN);
        } else if (operator.equals("=")) {
            require(left.type, right.type);
            if (left.type != DataType.INTEGER && left.type != DataType.BOOLEAN && left.type != DataType.STRING)
                throw new IllegalArgumentException("Collection equality is not supported");
        } else if (concatenate) {
            require(right.type, DataType.STRING);
        } else {
            require(left.type, DataType.INTEGER);
            require(right.type, DataType.INTEGER);
        }
        return new Node(
            logic || compare ? DataType.BOOLEAN : concatenate ? DataType.STRING : DataType.INTEGER,
            1 + Math.max(left.depth, right.depth),
            left.constant && right.constant) {

            @Override
            public DataValue evaluate(DataRuntime runtime) {
                DataValue a = left.evaluate(runtime);
                runtime.work(1);
                if (operator.equals("AND") && !a.asBoolean()) return DataValue.bool(false);
                if (operator.equals("OR") && a.asBoolean()) return DataValue.bool(true);
                DataValue b = right.evaluate(runtime);
                if (logic) return DataValue.bool(b.asBoolean());
                if (operator.equals("=") && left.type == DataType.BOOLEAN)
                    return DataValue.bool(a.asBoolean() == b.asBoolean());
                if (left.type == DataType.STRING) {
                    String x = a.asString(), y = b.asString();
                    if (concatenate) {
                        int length = x.length() + y.length();
                        if (length > ExpressionLimits.MAX_STRING_LENGTH) throw new IllegalArgumentException(
                            "String length exceeds " + ExpressionLimits.MAX_STRING_LENGTH);
                        runtime.work(length);
                        runtime.allocateString(length);
                        return DataValue.string(x + y);
                    }
                    // String.equals scans at most the common length, and shared values compare in constant time.
                    runtime.work(x == y || x.length() != y.length() ? 1 : x.length());
                    return DataValue.bool(x.equals(y));
                }
                long x = a.asLong(), y = b.asLong();
                switch (operator) {
                    case "+":
                        return DataValue.integer(Math.addExact(x, y));
                    case "-":
                        return DataValue.integer(Math.subtractExact(x, y));
                    case "*":
                        return DataValue.integer(Math.multiplyExact(x, y));
                    case "/":
                        if (x == Long.MIN_VALUE && y == -1) throw new ArithmeticException("overflow");
                        return DataValue.integer(x / y);
                    case "%":
                        return DataValue.integer(x % y);
                    case "=":
                        return DataValue.bool(x == y);
                    case "<":
                        return DataValue.bool(x < y);
                    case ">":
                        return DataValue.bool(x > y);
                    case "<=":
                        return DataValue.bool(x <= y);
                    case ">=":
                        return DataValue.bool(x >= y);
                    default:
                        throw new IllegalArgumentException("Unknown operator " + operator);
                }
            }
        };
    }

    private static Node access(Node base, Node index, DataType type) {
        require(index.type, DataType.INTEGER);
        return new Node(type, 1 + Math.max(base.depth, index.depth), base.constant && index.constant) {

            @Override
            public DataValue evaluate(DataRuntime runtime) {
                DataValue root = base.evaluate(runtime);
                long offset = index.evaluate(runtime)
                    .asLong();
                runtime.work(1);
                if (offset < 0 || offset >= root.size())
                    throw new IllegalArgumentException("Index out of range: " + offset);
                return root.get((int) offset);
            }
        };
    }

    private static Node collection(DataType type, List<Node> nodes, int[] offsets) {
        int depth = 1;
        boolean constant = true;
        for (Node node : nodes) {
            depth = Math.max(depth, node.depth + 1);
            constant &= node.constant;
        }
        return new Node(type, depth, constant) {

            @Override
            public DataValue evaluate(DataRuntime runtime) {
                runtime.work(1);
                runtime.allocate(nodes.size());
                DataValue[] values = new DataValue[nodes.size()];
                for (int i = 0; i < nodes.size(); i++) values[offsets == null ? i : offsets[i]] = nodes.get(i)
                    .evaluate(runtime);
                return DataValue.ownedCollection(type, values);
            }
        };
    }

    private static DataValue parseString(String token) {
        StringBuilder out = new StringBuilder(Math.min(token.length() - 2, ExpressionLimits.MAX_STRING_LENGTH));
        for (int i = 1; i < token.length() - 1; i++) {
            char c = token.charAt(i);
            if (c == '\\') {
                if (++i >= token.length() - 1) throw new IllegalArgumentException("Incomplete string escape");
                c = token.charAt(i);
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
                        if (i + 4 >= token.length() - 1)
                            throw new IllegalArgumentException("Unicode escape requires four hexadecimal digits");
                        int decoded = 0;
                        for (int n = 0; n < 4; n++) {
                            int digit = Character.digit(token.charAt(++i), 16);
                            if (digit < 0) throw new IllegalArgumentException("Invalid Unicode escape");
                            decoded = (decoded << 4) | digit;
                        }
                        c = (char) decoded;
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown string escape: \\" + c);
                }
            }
            if (out.length() >= ExpressionLimits.MAX_STRING_LENGTH)
                throw new IllegalArgumentException("String length exceeds " + ExpressionLimits.MAX_STRING_LENGTH);
            out.append(c);
        }
        return DataValue.string(out.toString());
    }

    private final class Parser {

        private final List<String> tokens = new ArrayList<>();
        private final boolean constantOnly;
        private int position, nesting;

        Parser(String source, boolean constantOnly) {
            this.constantOnly = constantOnly;
            for (int i = 0; i < source.length();) {
                char c = source.charAt(i);
                if (Character.isWhitespace(c)) {
                    i++;
                    continue;
                }
                if (c == '-' && i + 1 < source.length() && source.charAt(i + 1) == '-') {
                    while (i < source.length() && source.charAt(i) != '\n' && source.charAt(i) != '\r') i++;
                    continue;
                }
                int start = i++;
                if (c == '"') {
                    boolean closed = false;
                    while (i < source.length()) {
                        char next = source.charAt(i++);
                        if (next == '\\') {
                            if (i < source.length()) i++;
                        } else if (next == '"') {
                            closed = true;
                            break;
                        }
                    }
                    if (!closed) throw new IllegalArgumentException("Unterminated string literal");
                } else if (c == '$' || Character.isLetter(c) || c == '_' || Character.isDigit(c)) {
                    while (i < source.length()
                        && (Character.isLetterOrDigit(source.charAt(i)) || source.charAt(i) == '_')) i++;
                } else if ((c == '<' || c == '>') && i < source.length() && source.charAt(i) == '=') i++;
                else if (c == '*' && (start == 0 || !Character.isWhitespace(source.charAt(start - 1))
                    || i >= source.length()
                    || !Character.isWhitespace(source.charAt(i))))
                    throw new IllegalArgumentException("Use whitespace around multiplication: a * b");
                tokens.add(source.substring(start, i));
            }
            tokens.add("<EOF>");
        }

        String take() {
            String value = tokens.get(position);
            if (!value.equals("<EOF>")) position++;
            return value;
        }

        boolean peek(String value) {
            return tokens.get(position)
                .equalsIgnoreCase(value);
        }

        boolean accept(String value) {
            if (!peek(value)) return false;
            position++;
            return true;
        }

        void expect(String value) {
            if (!accept(value))
                throw new IllegalArgumentException("Expected " + value + ", found " + tokens.get(position));
        }

        void end() {
            expect("<EOF>");
        }

        Node expression() {
            if (++nesting > 32) throw new IllegalArgumentException("Expression nesting exceeds 32");
            Node node = or();
            nesting--;
            return node;
        }

        Node or() {
            Node node = and();
            while (accept("OR")) node = binary("OR", node, and());
            return node;
        }

        Node and() {
            Node node = not();
            while (accept("AND")) node = binary("AND", node, not());
            return node;
        }

        Node not() {
            if (accept("NOT")) {
                if (++nesting > 32) throw new IllegalArgumentException("Expression nesting exceeds 32");
                Node node = unary("NOT", not());
                nesting--;
                return node;
            }
            return comparison();
        }

        Node comparison() {
            Node node = add();
            String op = tokens.get(position)
                .toUpperCase(Locale.ROOT);
            String normalized = op.equals("GT") ? ">"
                : op.equals("LT") ? "<" : op.equals("EQ") ? "=" : op.equals("GE") ? ">=" : op.equals("LE") ? "<=" : op;
            if (normalized.equals(">") || normalized.equals("<")
                || normalized.equals("=")
                || normalized.equals(">=")
                || normalized.equals("<=")) {
                take();
                node = binary(normalized, node, add());
            }
            return node;
        }

        Node add() {
            Node node = multiply();
            while (peek("+") || peek("PLUS") || peek("-")) {
                String op = take();
                node = binary(op.equalsIgnoreCase("PLUS") ? "+" : op, node, multiply());
            }
            return node;
        }

        Node multiply() {
            Node node = sign();
            while (peek("*") || peek("/") || peek("%")) {
                String op = take();
                node = binary(op, node, sign());
            }
            return node;
        }

        Node sign() {
            if (peek("-") || peek("+") || peek("PLUS")) {
                String op = take();
                if (op.equals("-") && peek("9223372036854775808")) {
                    take();
                    return literal(DataValue.integer(Long.MIN_VALUE));
                }
                if (++nesting > 32) throw new IllegalArgumentException("Expression nesting exceeds 32");
                Node node = unary(op.equals("-") ? "-" : "+", sign());
                nesting--;
                return node;
            }
            return postfix();
        }

        Node postfix() {
            Node node = primary();
            while (peek(".") || peek("[")) {
                if (accept(".")) {
                    int index = node.type.fieldIndex(take());
                    node = access(node, literal(DataValue.integer(index)), node.type.getFieldType(index));
                } else {
                    expect("[");
                    if (node.type.getKind() != DataType.Kind.ARRAY)
                        throw new IllegalArgumentException("Index requires an array");
                    Node index = expression();
                    expect("]");
                    node = access(node, index, node.type.getElementType());
                }
            }
            return node;
        }

        Node primary() {
            if (accept("(")) {
                Node node = expression();
                expect(")");
                return node;
            }
            if (accept("TRUE")) return literal(DataValue.bool(true));
            if (accept("FALSE")) return literal(DataValue.bool(false));
            if (accept("[")) {
                List<Node> nodes = new ArrayList<>();
                if (!peek("]")) do {
                    nodes.add(expression());
                    if (nodes.size() > 256) throw new IllegalArgumentException("Array width exceeds 256");
                } while (accept(",") && !peek("]"));
                expect("]");
                if (nodes.isEmpty()) throw new IllegalArgumentException("Empty array has no inferable element type");
                DataType element = nodes.get(0).type;
                for (Node node : nodes) require(node.type, element);
                return collection(DataType.array(element, nodes.size()), nodes, null);
            }
            if (accept("{")) {
                List<Node> nodes = new ArrayList<>();
                List<String> names = new ArrayList<>();
                Map<String, DataType> fields = new LinkedHashMap<>();
                if (!peek("}")) do {
                    String name = take();
                    expect(":");
                    Node node = expression();
                    if (fields.put(name, node.type) != null)
                        throw new IllegalArgumentException("Duplicate field: " + name);
                    names.add(name);
                    nodes.add(node);
                    if (nodes.size() > 256) throw new IllegalArgumentException("Struct width exceeds 256");
                } while (accept(",") && !peek("}"));
                expect("}");
                DataType type = DataType.struct(fields);
                int[] offsets = new int[names.size()];
                for (int i = 0; i < offsets.length; i++) offsets[i] = type.fieldIndex(names.get(i));
                return collection(type, nodes, offsets);
            }
            String token = take();
            if (token.startsWith("$")) return variable(token.substring(1), constantOnly);
            if (token.startsWith("\"")) {
                DataValue value = parseString(token);
                int characters = value.getStringCharacterCount();
                if (characters > ExpressionLimits.MAX_CONSTANT_STRING_CHARACTERS - constantStringCharacters)
                    throw new IllegalArgumentException(
                        "Constant pool exceeds " + ExpressionLimits.MAX_CONSTANT_STRING_CHARACTERS
                            + " string characters");
                // Count literals in dynamic expressions too; folding cannot create more characters than its inputs.
                constantStringCharacters += characters;
                return literal(value);
            }
            try {
                return literal(DataValue.integer(Long.parseLong(token)));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Expected value or integer in signed 64-bit range, found " + token);
            }
        }
    }
}
