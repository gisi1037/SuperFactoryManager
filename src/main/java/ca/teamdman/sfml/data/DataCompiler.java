package ca.teamdman.sfml.data;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/** Compile-only name/type resolution. Execution uses slots and fixed field offsets. */
public final class DataCompiler {

    // Identifier rules are immutable; each compile uses a private matcher rather than recompiling these regexes.
    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
    private static final Pattern FUNCTION_NAME = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*");

    private final Set<String> referencedLabels = new HashSet<>();
    private final Set<String> stateBuiltinCalls = new HashSet<>();
    private final Map<String, Binding> state = new HashMap<>();
    private final Map<String, Binding> globals = new LinkedHashMap<>();
    private final List<GlobalDeclaration> globalDeclarations = new ArrayList<>();
    private final List<ProgramStateSchema.Declaration> declarations = new ArrayList<>();
    private final Deque<Map<String, Binding>> scopes = new ArrayDeque<>();
    private int locals, maxLocals, constantCells, constantStringCharacters;
    private boolean used;
    private final Map<String, CompiledFunction> functions = new LinkedHashMap<>();
    private final Set<CompiledFunction> rootCalls = new HashSet<>();
    private CompiledFunction currentFunction;
    private boolean usedBeforeFunction;

    public CompiledFunction declareFunction(String name, List<String> names, List<DataType> types, DataType result,
        String source) {
        if (stateBuiltinCalls.contains(name))
            throw new IllegalArgumentException("Function conflicts with a built-in used in STATE defaults: " + name);
        if (functions.size() >= 128) throw new IllegalArgumentException("Function definitions exceed 128");
        if (!FUNCTION_NAME.matcher(name)
            .matches()) throw new IllegalArgumentException("Invalid function name: " + name);
        int separator = name.indexOf('.');
        String first = (separator < 0 ? name : name.substring(0, separator)).toUpperCase(Locale.ROOT);
        if (first.equals("TRUE") || first.equals("FALSE")
            || first.equals("NOT")
            || first.equals("PLUS")
            || name.equalsIgnoreCase("LABEL")
            || name.equalsIgnoreCase("ITEM")
            || name.equalsIgnoreCase("ORE")
            || name.equalsIgnoreCase("TRANSFER"))
            throw new IllegalArgumentException("Reserved built-in function: " + name);
        if (names.size() > 16) throw new IllegalArgumentException("Function parameters exceed 16");
        if (functions.containsKey(name)) throw new IllegalArgumentException("Duplicate function: " + name);
        CompiledFunction function = new CompiledFunction(name, names, types, result, source);
        functions.put(name, function);
        return function;
    }

    public void beginFunction(CompiledFunction function) {
        usedBeforeFunction = used;
        beginTrigger();
        currentFunction = function;
        pushScope();
        for (int i = 0; i < function.getParameterNames()
            .size(); i++)
            declareLocal(
                function.getParameterNames()
                    .get(i),
                function.getParameterTypes()
                    .get(i));
    }

    public void endFunction(CompiledFunction.Body body) {
        currentFunction.seal(body, locals);
        currentFunction = null;
        scopes.clear();
        // Uncalled definitions do not force an otherwise legacy trigger to allocate data frames.
        used = usedBeforeFunction;
    }

    public List<CompiledFunction> getFunctions() {
        return Collections.unmodifiableList(new ArrayList<>(functions.values()));
    }

    public void validateFunctions() {
        Set<CompiledFunction> complete = new HashSet<>();
        Set<CompiledFunction> visiting = new HashSet<>();
        for (CompiledFunction function : functions.values()) validateFunction(function, complete, visiting);
    }

    private void validateFunction(CompiledFunction function, Set<CompiledFunction> complete,
        Set<CompiledFunction> visiting) {
        if (complete.contains(function)) return;
        if (!visiting.add(function))
            throw new IllegalArgumentException("Recursive function call: " + function.getName());
        for (CompiledFunction called : function.getCalls()) validateFunction(called, complete, visiting);
        visiting.remove(function);
        complete.add(function);
    }

    public Set<String> getReferencedLabels() {
        Set<String> reachableLabels = new HashSet<>(referencedLabels);
        Set<CompiledFunction> visited = new HashSet<>();
        for (CompiledFunction function : rootCalls) collectLabels(function, visited, reachableLabels);
        return Collections.unmodifiableSet(reachableLabels);
    }

    private void collectLabels(CompiledFunction function, Set<CompiledFunction> visited, Set<String> labels) {
        if (!visited.add(function)) return;
        labels.addAll(function.getReferencedLabels());
        for (CompiledFunction called : function.getCalls()) collectLabels(called, visited, labels);
    }

    private void referenceLabel(String label) {
        if (currentFunction == null) referencedLabels.add(label);
        else currentFunction.referencesLabel(label);
    }

    public void beginTrigger() {
        scopes.clear();
        locals = 0;
        currentFunction = null;
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

    public void markUsed() {
        used = true;
    }

    public ProgramStateSchema getStateSchema() {
        return declarations.isEmpty() ? ProgramStateSchema.EMPTY : new ProgramStateSchema(declarations);
    }

    public List<GlobalDeclaration> getGlobalDeclarations() {
        return Collections.unmodifiableList(globalDeclarations);
    }

    public void declareGlobal(String name, String expression, int line, int column) {
        if (globals.containsKey(name) || state.containsKey(name))
            throw error(line, column, "Duplicate/shadowed variable: $" + name);
        if (globals.size() >= ProgramGlobals.MAX_GLOBALS)
            throw error(line, column, "Program globals exceed " + ProgramGlobals.MAX_GLOBALS);
        // Resolve before publishing the name: self and forward global reads are rejected.
        DataExpression initializer = compile(expression, line, column);
        Binding binding = new Binding(name, initializer.getType(), globals.size(), BindingKind.GLOBAL, false);
        globals.put(name, binding);
        globalDeclarations.add(new GlobalDeclaration(binding, initializer));
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
        return declareLocal(name, type, false);
    }

    public Binding declareReadOnlyLocal(String name, DataType type) {
        return declareLocal(name, type, true);
    }

    private Binding declareLocal(String name, DataType type, boolean readOnly) {
        used = true;
        if (find(name) != null) throw new IllegalArgumentException("Duplicate/shadowed variable: $" + name);
        if (locals >= 256) throw new IllegalArgumentException("Local bindings exceed 256");
        Binding binding = new Binding(name, type, locals++, false, readOnly);
        if (currentFunction == null) maxLocals = Math.max(maxLocals, locals);
        scopes.peek()
            .put(name, binding);
        return binding;
    }

    private Binding find(String name) {
        for (Map<String, Binding> scope : scopes) {
            Binding binding = scope.get(name);
            if (binding != null) return binding;
        }
        if (currentFunction != null) return null;
        Binding global = globals.get(name);
        return global == null ? state.get(name) : global;
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
            return expressionPlan(node, source, line, column);
        } catch (ArithmeticException ex) {
            throw error(line, column, "Arithmetic overflow or division by zero");
        } catch (IllegalArgumentException ex) {
            throw error(line, column, ex.getMessage());
        }
    }

    private static DataExpression expressionPlan(final Node compiled, String source, int line, int column) {
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
    }

    public Assignment assignment(String target, DataExpression value, int line, int column) {
        used = true;
        try {
            Parser parser = new Parser(target, false);
            String name = parser.take();
            if (!name.startsWith("$")) throw new IllegalArgumentException("SET requires a variable path");
            Binding binding = find(name.substring(1));
            if (binding == null) throw new IllegalArgumentException("Unknown variable: " + name);
            if (binding.readOnly) throw new IllegalArgumentException("Loop binding is read-only: " + name);
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

    public enum BindingKind {
        LOCAL,
        GLOBAL,
        STATE
    }

    public static final class Binding {

        public final String name;
        public final DataType type;
        public final int slot;
        public final boolean persistent;
        public final BindingKind kind;
        public final boolean readOnly;

        private Binding(String name, DataType type, int slot, boolean persistent) {
            this(name, type, slot, persistent, false);
        }

        private Binding(String name, DataType type, int slot, boolean persistent, boolean readOnly) {
            this(name, type, slot, persistent ? BindingKind.STATE : BindingKind.LOCAL, readOnly);
        }

        private Binding(String name, DataType type, int slot, BindingKind kind, boolean readOnly) {
            this.name = name;
            this.type = type;
            this.slot = slot;
            this.kind = kind;
            this.persistent = kind == BindingKind.STATE;
            this.readOnly = readOnly;
        }

        public void write(DataRuntime runtime, DataValue value) {
            require(value.getType(), type);
            if (kind == BindingKind.GLOBAL) runtime.writeGlobal(slot, value);
            else runtime.write(persistent, slot, value);
        }

        DataValue read(DataRuntime runtime) {
            return kind == BindingKind.GLOBAL ? runtime.readGlobal(slot) : runtime.read(persistent, slot);
        }

        void publish(DataRuntime runtime, DataValue value) {
            if (kind == BindingKind.GLOBAL) runtime.publishGlobal(slot, value);
            else runtime.publish(persistent, slot, value);
        }

        /** Compiler-owned action results reserve publication before the action can change the world. */
        public void writeActionResult(DataRuntime runtime, Supplier<DataValue> action) {
            if (kind != BindingKind.LOCAL) throw new IllegalArgumentException("Action results require a fresh local");
            runtime.work(1);
            DataValue value = action.get();
            require(value.getType(), type);
            runtime.publish(false, slot, value);
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
                    DataValue cursor = binding.read(runtime);
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
                binding.publish(runtime, replacement);
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
        return variablePlan(binding);
    }

    private static Node variablePlan(Binding binding) {
        return new Node(binding.type, 1, false) {

            @Override
            public DataValue evaluate(DataRuntime runtime) {
                return binding.read(runtime);
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
                if (operator.equals("~")) return DataValue.integer(~operand.asLong());
                return DataValue.integer(operator.equals("-") ? Math.negateExact(operand.asLong()) : operand.asLong());
            }
        };
    }

    private static Node binary(String operator, Node left, Node right) {
        boolean logic = operator.equals("AND") || operator.equals("OR");
        boolean concatenate = operator.equals("+") && left.type == DataType.STRING;
        boolean equality = operator.equals("=") || operator.equals("!=");
        boolean compare = equality || operator.equals("<")
            || operator.equals(">")
            || operator.equals("<=")
            || operator.equals(">=");
        if (logic) {
            require(left.type, DataType.BOOLEAN);
            require(right.type, DataType.BOOLEAN);
        } else if (equality) {
            require(left.type, right.type);
            if (left.type != DataType.INTEGER && left.type != DataType.BOOLEAN
                && left.type != DataType.STRING
                && left.type != DataType.ITEM_KEY)
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
                if (equality && left.type == DataType.BOOLEAN) {
                    boolean same = a.asBoolean() == b.asBoolean();
                    return DataValue.bool(operator.equals("!=") ? !same : same);
                }
                if (equality && left.type == DataType.ITEM_KEY) {
                    boolean same = runtime.sameItem(a.asItemIdentity(), b.asItemIdentity());
                    return DataValue.bool(operator.equals("!=") ? !same : same);
                }
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
                    boolean same = x.equals(y);
                    return DataValue.bool(operator.equals("!=") ? !same : same);
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
                    case "&":
                        return DataValue.integer(x & y);
                    case "|":
                        return DataValue.integer(x | y);
                    case "^":
                        return DataValue.integer(x ^ y);
                    case "<<":
                        return DataValue.integer(x << shiftDistance(y));
                    case ">>":
                        return DataValue.integer(x >> shiftDistance(y));
                    case ">>>":
                        return DataValue.integer(x >>> shiftDistance(y));
                    case "=":
                        return DataValue.bool(x == y);
                    case "!=":
                        return DataValue.bool(x != y);
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

    private static int shiftDistance(long value) {
        // Java masks shift distances; validate the full SFML integer before narrowing it.
        if (value < 0 || value > 63)
            throw new IllegalArgumentException("Shift distance must be between 0 and 63, got " + value);
        return (int) value;
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

    private static Node side(Node receiver, int side) {
        require(receiver.type, DataType.LABEL_REF);
        return new Node(DataType.LABEL_REF, receiver.depth + 1, receiver.constant) {

            @Override
            public DataValue evaluate(DataRuntime runtime) {
                LabelReference label = receiver.evaluate(runtime)
                    .asLabel();
                runtime.work(1);
                runtime.allocate(1);
                return DataValue.label(label.side(side));
            }
        };
    }

    private static Node slots(Node receiver, Node first, Node last) {
        require(receiver.type, DataType.LABEL_REF);
        require(first.type, DataType.INTEGER);
        require(last.type, DataType.INTEGER);
        return new Node(
            DataType.LABEL_REF,
            1 + Math.max(receiver.depth, Math.max(first.depth, last.depth)),
            receiver.constant && first.constant && last.constant) {

            @Override
            public DataValue evaluate(DataRuntime runtime) {
                LabelReference label = receiver.evaluate(runtime)
                    .asLabel();
                long start = first.evaluate(runtime)
                    .asLong();
                long end = last.evaluate(runtime)
                    .asLong();
                runtime.work(1);
                runtime.allocate(1);
                return DataValue.label(label.slots(start, end));
            }
        };
    }

    private static Node query(Node receiver, Node selector, String method) {
        require(receiver.type, DataType.LABEL_REF);
        require(selector.type, DataType.RESOURCE_SELECTOR);
        DataType result = method.equals("CONTENTS") ? DataType.ITEM_CONTENTS
            : method.equals("COUNT") ? DataType.INTEGER : DataType.BOOLEAN;
        return new Node(result, 1 + Math.max(receiver.depth, selector.depth), false) {

            @Override
            public DataValue evaluate(DataRuntime runtime) {
                LabelReference label = receiver.evaluate(runtime)
                    .asLabel();
                ResourceSelector resource = selector.evaluate(runtime)
                    .asSelector();
                runtime.work(1);
                if (method.equals("CONTENTS")) return runtime.contents(label, resource);
                if (method.equals("COUNT")) return DataValue.integer(runtime.count(label, resource));
                if (method.equals("EMPTY")) return DataValue.bool(runtime.empty(label, resource));
                return DataValue.bool(runtime.available(label, resource));
            }
        };
    }

    private static Node redstoneQuery(Node receiver, String method) {
        require(receiver.type, DataType.LABEL_REF);
        return new Node(method.equals("REDSTONE") ? DataType.INTEGER : DataType.BOOLEAN, receiver.depth + 1, false) {

            @Override
            public DataValue evaluate(DataRuntime runtime) {
                LabelReference label = receiver.evaluate(runtime)
                    .asLabel();
                runtime.work(1);
                if (label.hasSlotSelection())
                    throw new IllegalArgumentException("Redstone observations do not accept slot restrictions");
                if (method.equals("REDSTONE")) return DataValue.integer(runtime.redstone(label));
                if (method.equals("POWERED")) return DataValue.bool(runtime.powered(label));
                return DataValue.bool(runtime.redstoneAvailable(label));
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

    private Node functionCall(String name, List<Node> arguments, boolean constantOnly) {
        // Preserve exact-name player functions; defaults fill only otherwise unresolved names.
        CompiledFunction function = functions.get(name);
        if (function == null) {
            Node builtin = builtinCall(name, arguments);
            if (builtin != null) {
                if (constantOnly) stateBuiltinCalls.add(name);
                return builtin;
            }
            throw new IllegalArgumentException("Unknown function: " + name);
        }
        if (constantOnly) throw new IllegalArgumentException("STATE defaults cannot call player functions");
        if (arguments.size() != function.getParameterTypes()
            .size())
            throw new IllegalArgumentException(
                "Function " + name
                    + " requires "
                    + function.getParameterTypes()
                        .size()
                    + " arguments");
        int depth = 1;
        for (int i = 0; i < arguments.size(); i++) {
            DataType expected = function.getParameterTypes()
                .get(i);
            if (!arguments.get(i).type.equals(expected)) throw new IllegalArgumentException(
                "Function " + name
                    + " argument "
                    + (i + 1)
                    + ": expected "
                    + expected
                    + ", got "
                    + arguments.get(i).type);
            depth = Math.max(depth, arguments.get(i).depth + 1);
        }
        if (currentFunction != null) currentFunction.calls(function);
        else rootCalls.add(function);
        return callPlan(function, arguments, depth);
    }

    /** Fixed-arity pure primitives share expression work without allocating a player call frame. */
    private static Node builtinCall(String name, List<Node> arguments) {
        String method = name.toUpperCase(Locale.ROOT);
        if (method.equals("TOTAL") || method.equals("STOCK") || method.equals("SHORTAGE"))
            return stockBuiltin(method, arguments);
        int arity;
        switch (method) {
            case "ABS":
            case "LEN":
                arity = 1;
                break;
            case "MIN":
            case "MAX":
                arity = 2;
                break;
            case "CLAMP":
                arity = 3;
                break;
            default:
                return null;
        }
        if (arguments.size() != arity) throw new IllegalArgumentException(method + " requires " + arity + " arguments");
        int depth = 1;
        boolean constant = true;
        for (Node argument : arguments) {
            if (method.equals("LEN")) {
                if (argument.type != DataType.STRING && !argument.type.isSequence())
                    throw new IllegalArgumentException("LEN requires a string or sequence");
            } else require(argument.type, DataType.INTEGER);
            depth = Math.max(depth, argument.depth + 1);
            constant &= argument.constant;
        }
        Node first = arguments.get(0), second = arity > 1 ? arguments.get(1) : null,
            third = arity > 2 ? arguments.get(2) : null;
        return new Node(DataType.INTEGER, depth, constant) {

            @Override
            public DataValue evaluate(DataRuntime runtime) {
                runtime.work(1);
                // Evaluate every argument once, left to right, even when an extremum is already known.
                DataValue a = first.evaluate(runtime);
                if (method.equals("LEN")) {
                    if (a.getType()
                        .isSequence()) return DataValue.integer(a.size());
                    String value = a.asString();
                    // Charge the complete scan before counting Unicode code points.
                    runtime.work(value.length());
                    return DataValue.integer(value.codePointCount(0, value.length()));
                }
                long x = a.asLong();
                if (method.equals("ABS")) return DataValue.integer(x < 0 ? Math.negateExact(x) : x);
                long y = second.evaluate(runtime)
                    .asLong();
                if (method.equals("MIN")) return DataValue.integer(Math.min(x, y));
                if (method.equals("MAX")) return DataValue.integer(Math.max(x, y));
                long z = third.evaluate(runtime)
                    .asLong();
                if (y > z) throw new IllegalArgumentException("CLAMP lower bound exceeds upper bound");
                return DataValue.integer(Math.max(y, Math.min(x, z)));
            }
        };
    }

    private static Node stockBuiltin(String method, List<Node> arguments) {
        int arity = method.equals("TOTAL") ? 1 : 2;
        if (arguments.size() != arity) throw new IllegalArgumentException(method + " requires " + arity + " arguments");
        Node first = arguments.get(0), second = arity == 2 ? arguments.get(1) : null;
        if (method.equals("TOTAL")) {
            if (first.type != DataType.ITEM_CONTENTS && first.type != DataType.ITEM_LOCATIONS)
                throw new IllegalArgumentException("TOTAL requires item contents or locations");
        } else if (method.equals("STOCK")) {
            require(first.type, DataType.ITEM_CONTENTS);
            require(second.type, DataType.ITEM_KEY);
        } else {
            require(first.type, DataType.INTEGER);
            require(second.type, DataType.INTEGER);
        }
        int depth = first.depth + 1;
        if (second != null) depth = Math.max(depth, second.depth + 1);
        boolean constant = method.equals("SHORTAGE") && first.constant && second.constant;
        return new Node(DataType.INTEGER, depth, constant) {

            @Override
            public DataValue evaluate(DataRuntime runtime) {
                runtime.work(1);
                DataValue a = first.evaluate(runtime);
                if (method.equals("TOTAL")) return DataValue.integer(ItemStockFunctions.total(a, runtime));
                DataValue b = second.evaluate(runtime);
                return DataValue.integer(
                    method.equals("STOCK") ? ItemStockFunctions.stock(a, b.asItemIdentity(), runtime)
                        : ItemStockFunctions.shortage(a.asLong(), b.asLong()));
            }
        };
    }

    private static Node callPlan(CompiledFunction function, List<Node> arguments, int depth) {
        return new Node(function.getReturnType(), depth, false) {

            @Override
            public DataValue evaluate(DataRuntime runtime) {
                runtime.work(1);
                runtime.allocate(arguments.size());
                DataValue[] values = new DataValue[arguments.size()];
                for (int i = 0; i < values.length; i++) values[i] = arguments.get(i)
                    .evaluate(runtime);
                return function.invoke(runtime, values);
            }
        };
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
                } else if ((c == '!' || c == '=') && i < source.length() && source.charAt(i) == '=') {
                    i++;
                } else if ((c == '&' || c == '|') && i < source.length() && source.charAt(i) == c) {
                    i++;
                } else if ((c == '<' || c == '>') && i < source.length()) {
                    if (source.charAt(i) == c) {
                        i++;
                        if (c == '>' && i < source.length() && source.charAt(i) == '>') i++;
                    } else if (source.charAt(i) == '=') i++;
                } else if (c == '*' && (start == 0 || !Character.isWhitespace(source.charAt(start - 1))
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
            while (accept("OR") || accept("||")) node = binary("OR", node, and());
            return node;
        }

        Node and() {
            Node node = not();
            while (accept("AND") || accept("&&")) node = binary("AND", node, not());
            return node;
        }

        Node not() {
            if (accept("NOT") || accept("!")) {
                if (++nesting > 32) throw new IllegalArgumentException("Expression nesting exceeds 32");
                Node node = unary("NOT", not());
                nesting--;
                return node;
            }
            return comparison();
        }

        Node comparison() {
            Node node = bitOr();
            String op = tokens.get(position)
                .toUpperCase(Locale.ROOT);
            String normalized = op.equals("GT") ? ">"
                : op.equals("LT") ? "<"
                    : op.equals("EQ") || op.equals("==") ? "=" : op.equals("GE") ? ">=" : op.equals("LE") ? "<=" : op;
            if (normalized.equals(">") || normalized.equals("<")
                || normalized.equals("=")
                || normalized.equals("!=")
                || normalized.equals(">=")
                || normalized.equals("<=")) {
                take();
                node = binary(normalized, node, bitOr());
            }
            return node;
        }

        Node bitOr() {
            Node node = bitXor();
            while (accept("|")) node = binary("|", node, bitXor());
            return node;
        }

        Node bitXor() {
            Node node = bitAnd();
            while (accept("^")) node = binary("^", node, bitAnd());
            return node;
        }

        Node bitAnd() {
            Node node = shift();
            while (accept("&")) node = binary("&", node, shift());
            return node;
        }

        Node shift() {
            Node node = add();
            while (peek("<<") || peek(">>") || peek(">>>")) {
                String op = take();
                node = binary(op, node, add());
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
            if (peek("-") || peek("+") || peek("PLUS") || peek("~")) {
                String op = take();
                if (op.equals("-") && peek("9223372036854775808")) {
                    take();
                    return literal(DataValue.integer(Long.MIN_VALUE));
                }
                if (++nesting > 32) throw new IllegalArgumentException("Expression nesting exceeds 32");
                Node node = unary(op.equalsIgnoreCase("PLUS") ? "+" : op, sign());
                nesting--;
                return node;
            }
            return postfix();
        }

        Node postfix() {
            Node node = primary();
            while (peek(".") || peek("[")) {
                if (accept(".")) {
                    String member = take();
                    if (accept("(")) {
                        String method = member.toUpperCase(Locale.ROOT);
                        if (method.equals("SIDE")) {
                            String argument = take();
                            if (!argument.startsWith("\"") || !peek(")")) throw new IllegalArgumentException(
                                "side requires exactly one absolute side string literal");
                            int direction = LabelReference.sideIndex(stringLiteral(argument).asString());
                            expect(")");
                            node = side(node, direction);
                            continue;
                        }
                        if (method.equals("SLOTS")) {
                            Node first = expression();
                            expect(",");
                            Node last = expression();
                            expect(")");
                            node = slots(node, first, last);
                            continue;
                        }
                        if (method.equals("REDSTONE") || method.equals("POWERED")
                            || method.equals("REDSTONEAVAILABLE")) {
                            if (constantOnly)
                                throw new IllegalArgumentException("STATE defaults cannot perform live label queries");
                            if (!peek(")")) throw new IllegalArgumentException(member + " requires no arguments");
                            expect(")");
                            node = redstoneQuery(node, method);
                            continue;
                        }
                        if (!method.equals("COUNT") && !method.equals("EMPTY")
                            && !method.equals("AVAILABLE")
                            && !method.equals("CONTENTS"))
                            throw new IllegalArgumentException("Unsupported label query: " + member);
                        if (constantOnly)
                            throw new IllegalArgumentException("STATE defaults cannot perform live label queries");
                        Node selector;
                        if (peek(")")) {
                            if (method.equals("COUNT"))
                                throw new IllegalArgumentException("count requires exactly one resource selector");
                            selector = literal(DataValue.selector(ResourceSelector.item("*")));
                        } else selector = expression();
                        if (peek(","))
                            throw new IllegalArgumentException("Label query requires exactly one resource selector");
                        expect(")");
                        node = query(node, selector, method);
                    } else {
                        int index = node.type.fieldIndex(member);
                        node = access(node, literal(DataValue.integer(index)), node.type.getFieldType(index));
                    }
                } else {
                    expect("[");
                    if (!node.type.isSequence()) throw new IllegalArgumentException("Index requires a sequence");
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
            if (IDENTIFIER.matcher(token)
                .matches()) {
                while (accept(".")) token += "." + take();
            }
            if (accept("(")) {
                String constructor = token.toUpperCase(Locale.ROOT);
                if (!constructor.equals("LABEL") && !constructor.equals("ITEM") && !constructor.equals("ORE")) {
                    List<Node> arguments = new ArrayList<>();
                    if (!peek(")")) do {
                        arguments.add(expression());
                        if (arguments.size() > 16) throw new IllegalArgumentException("Function arguments exceed 16");
                    } while (accept(","));
                    expect(")");
                    return functionCall(token, arguments, constantOnly);
                }
                String argument = take();
                if (!argument.startsWith("\"")) throw new IllegalArgumentException(
                    constructor + " requires exactly one string literal; dynamic arguments are not supported");
                DataValue string = stringLiteral(argument);
                if (!peek(")")) throw new IllegalArgumentException(
                    constructor + " requires exactly one string literal; expressions are not supported");
                expect(")");
                if (constructor.equals("LABEL")) {
                    referenceLabel(string.asString());
                    return literal(DataValue.label(LabelReference.literal(string.asString())));
                }
                return literal(
                    DataValue.selector(
                        constructor.equals("ORE") ? ResourceSelector.ore(string.asString())
                            : ResourceSelector.item(string.asString())));
            }
            if (token.startsWith("$")) return variable(token.substring(1), constantOnly);
            if (token.startsWith("\"")) return literal(stringLiteral(token));
            try {
                return literal(DataValue.integer(Long.parseLong(token)));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Expected value or integer in signed 64-bit range, found " + token);
            }
        }

        private DataValue stringLiteral(String token) {
            DataValue value = parseString(token);
            int characters = value.getStringCharacterCount();
            if (characters > ExpressionLimits.MAX_CONSTANT_STRING_CHARACTERS - constantStringCharacters)
                throw new IllegalArgumentException(
                    "Constant pool exceeds " + ExpressionLimits.MAX_CONSTANT_STRING_CHARACTERS + " string characters");
            constantStringCharacters += characters;
            return value;
        }
    }
}
