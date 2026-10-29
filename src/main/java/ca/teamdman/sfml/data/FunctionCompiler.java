package ca.teamdman.sfml.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;

import ca.teamdman.langs.SFMLParser;
import ca.teamdman.sfml.data.CompiledFunction.Body;
import ca.teamdman.sfml.data.CompiledFunction.Command;

/** Small data-only IR: a function never receives a ProgramContext or manager authority. */
public final class FunctionCompiler {

    private final DataCompiler data;
    private DataType result;
    private int loopDepth;

    public FunctionCompiler(DataCompiler data) {
        this.data = data;
    }

    public void compile(List<SFMLParser.FunctionDefinitionContext> definitions) {
        List<CompiledFunction> functions = new ArrayList<>();
        // Declare every signature first, so body compilation supports forward references.
        for (SFMLParser.FunctionDefinitionContext definition : definitions) {
            List<String> names = new ArrayList<>();
            List<DataType> types = new ArrayList<>();
            for (SFMLParser.FunctionParameterContext parameter : definition.functionParameter()) {
                names.add(
                    parameter.VARIABLE()
                        .getText()
                        .substring(1));
                types.add(type(parameter.dataType()));
            }
            functions.add(
                data.declareFunction(
                    definition.functionName()
                        .getText(),
                    names,
                    types,
                    type(definition.dataType()),
                    source(definition)));
        }
        for (int i = 0; i < definitions.size(); i++) {
            CompiledFunction function = functions.get(i);
            result = function.getReturnType();
            try {
                data.beginFunction(function);
                data.endFunction(
                    block(
                        definitions.get(i)
                            .block()));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Function " + function.getName() + ": " + ex.getMessage());
            }
        }
        data.validateFunctions();
    }

    private DataType type(SFMLParser.DataTypeContext context) {
        DataType type;
        if (context.dataName() != null) {
            String name = context.dataName()
                .getText()
                .toUpperCase(Locale.ROOT);
            switch (name) {
                case "INTEGER":
                    type = DataType.INTEGER;
                    break;
                case "BOOLEAN":
                    type = DataType.BOOLEAN;
                    break;
                case "STRING":
                    type = DataType.STRING;
                    break;
                case "LABEL_REF":
                    type = DataType.LABEL_REF;
                    break;
                case "RESOURCE_SELECTOR":
                    type = DataType.RESOURCE_SELECTOR;
                    break;
                case "ITEM_KEY":
                    type = DataType.ITEM_KEY;
                    break;
                case "ITEM_CONTENTS":
                    type = DataType.ITEM_CONTENTS;
                    break;
                case "ITEM_LOCATIONS":
                    type = DataType.ITEM_LOCATIONS;
                    break;
                default:
                    throw fail(context, "Unknown function type: " + name);
            }
        } else {
            Map<String, DataType> fields = new LinkedHashMap<>();
            for (SFMLParser.TypeFieldContext field : context.typeField()) {
                String name = field.dataName()
                    .getText();
                if (fields.put(name, type(field.dataType())) != null)
                    throw fail(field, "Duplicate type field: " + name);
            }
            type = DataType.struct(fields);
        }
        // Dimensions are written outermost first: INTEGER[3][2] is three rows of two values.
        for (int i = context.NUMBER()
            .size() - 1; i >= 0; i--) type = DataType.array(
                type,
                Integer.parseInt(
                    context.NUMBER(i)
                        .getText()));
        return type;
    }

    private Body block(SFMLParser.BlockContext context) {
        List<Command> commands = new ArrayList<>();
        boolean returns = false;
        data.pushScope();
        try {
            for (SFMLParser.StatementContext statement : context.statement()) {
                if (returns) throw fail(statement, "Unreachable statement after RETURN");
                if (statement.dataDeclaration() != null) {
                    SFMLParser.DataDeclarationContext declaration = statement.dataDeclaration();
                    DataExpression value = expression(declaration.dataExpression());
                    DataCompiler.Binding binding = data.declareLocal(
                        declaration.VARIABLE()
                            .getText()
                            .substring(1),
                        value.getType());
                    commands.add(runtime -> {
                        binding.write(runtime, value.evaluate(runtime));
                        return null;
                    });
                } else if (statement.dataAssignment() != null) {
                    SFMLParser.DataAssignmentContext assignment = statement.dataAssignment();
                    DataCompiler.Assignment compiled = data.assignment(
                        source(assignment.dataTarget()),
                        expression(assignment.dataExpression()),
                        assignment.getStart()
                            .getLine(),
                        assignment.getStart()
                            .getCharPositionInLine());
                    commands.add(runtime -> {
                        compiled.execute(runtime);
                        return null;
                    });
                } else if (statement.returnStatement() != null) {
                    DataExpression value = expression(
                        statement.returnStatement()
                            .dataExpression());
                    require(value.getType(), result, statement);
                    commands.add(value::evaluate);
                    returns = true;
                } else if (statement.ifStatement() != null) {
                    SFMLParser.IfStatementContext branch = statement.ifStatement();
                    List<DataExpression> conditions = new ArrayList<>();
                    List<Body> bodies = new ArrayList<>();
                    for (int i = 0; i < branch.boolexpr()
                        .size(); i++) {
                        conditions.add(condition(branch.boolexpr(i)));
                        bodies.add(block(branch.block(i)));
                    }
                    Body otherwise = branch.block()
                        .size()
                        > branch.boolexpr()
                            .size() ? block(
                                branch.block(
                                    branch.block()
                                        .size() - 1))
                                : null;
                    returns = otherwise != null && otherwise.alwaysReturns();
                    for (Body body : bodies) returns &= body.alwaysReturns();
                    commands.add(runtime -> {
                        for (int i = 0; i < conditions.size(); i++) if (conditions.get(i)
                            .evaluate(runtime)
                            .asBoolean())
                            return bodies.get(i)
                                .execute(runtime);
                        return otherwise == null ? null : otherwise.execute(runtime);
                    });
                } else if (statement.forStatement() != null) {
                    commands.add(forLoop(statement.forStatement()));
                } else if (statement.whileStatement() != null) {
                    SFMLParser.WhileStatementContext loop = statement.whileStatement();
                    DataExpression condition = condition(loop.boolexpr());
                    enterLoop(loop);
                    Body body;
                    try {
                        body = block(loop.block());
                    } finally {
                        loopDepth--;
                    }
                    commands.add(runtime -> {
                        while (true) {
                            runtime.loopStep();
                            if (!condition.evaluate(runtime)
                                .asBoolean()) return null;
                            DataValue value = body.execute(runtime);
                            if (value != null) return value;
                        }
                    });
                } else {
                    throw fail(
                        statement,
                        "Player functions permit only local LET/SET, typed IF/FOR/WHILE and RETURN; effects are unavailable");
                }
            }
        } finally {
            data.popScope();
        }
        return new Body(commands, returns);
    }

    private Command forLoop(SFMLParser.ForStatementContext context) {
        enterLoop(context);
        data.pushScope();
        try {
            if (context instanceof SFMLParser.CountedForContext) {
                SFMLParser.CountedForContext loop = (SFMLParser.CountedForContext) context;
                DataExpression first = expression(loop.dataExpression(0));
                DataExpression last = expression(loop.dataExpression(1));
                DataExpression step = loop.dataExpression()
                    .size() == 3 ? expression(loop.dataExpression(2)) : null;
                require(first.getType(), DataType.INTEGER, loop);
                require(last.getType(), DataType.INTEGER, loop);
                if (step != null) require(step.getType(), DataType.INTEGER, loop);
                DataCompiler.Binding binder = data.declareReadOnlyLocal(
                    loop.VARIABLE()
                        .getText()
                        .substring(1),
                    DataType.INTEGER);
                Body body = block(loop.block());
                return runtime -> {
                    long current = first.evaluate(runtime)
                        .asLong();
                    long end = last.evaluate(runtime)
                        .asLong();
                    long increment = step == null ? 1
                        : step.evaluate(runtime)
                            .asLong();
                    if (increment == 0) throw new IllegalArgumentException("FOR STEP cannot be zero");
                    while (increment > 0 ? current <= end : current >= end) {
                        runtime.loopStep();
                        binder.write(runtime, DataValue.integer(current));
                        DataValue value = body.execute(runtime);
                        if (value != null) return value;
                        if (increment > 0 ? current > Long.MAX_VALUE - increment : current < Long.MIN_VALUE - increment)
                            break;
                        current += increment;
                    }
                    return null;
                };
            }
            SFMLParser.ArrayForContext loop = (SFMLParser.ArrayForContext) context;
            DataExpression array = expression(loop.dataExpression());
            if (!array.getType()
                .isSequence()) throw fail(loop, "FOR IN requires a sequence");
            DataCompiler.Binding binder = data.declareReadOnlyLocal(
                loop.VARIABLE()
                    .getText()
                    .substring(1),
                array.getType()
                    .getElementType());
            Body body = block(loop.block());
            return runtime -> {
                DataValue snapshot = array.evaluate(runtime);
                for (int i = 0; i < snapshot.size(); i++) {
                    runtime.loopStep();
                    binder.write(runtime, snapshot.get(i));
                    DataValue value = body.execute(runtime);
                    if (value != null) return value;
                }
                return null;
            };
        } finally {
            data.popScope();
            loopDepth--;
        }
    }

    private void enterLoop(ParserRuleContext context) {
        if (++loopDepth > 16) throw fail(context, "Loop nesting exceeds 16");
    }

    private DataExpression condition(SFMLParser.BoolexprContext context) {
        DataExpression value = data.compile(
            source(context),
            context.getStart()
                .getLine(),
            context.getStart()
                .getCharPositionInLine());
        require(value.getType(), DataType.BOOLEAN, context);
        return value;
    }

    private DataExpression expression(SFMLParser.DataExpressionContext context) {
        return data.compile(
            source(context),
            context.getStart()
                .getLine(),
            context.getStart()
                .getCharPositionInLine());
    }

    private static void require(DataType actual, DataType expected, ParserRuleContext context) {
        if (!actual.equals(expected))
            throw fail(context, "Type/shape mismatch: expected " + expected + ", got " + actual);
    }

    private static IllegalArgumentException fail(ParserRuleContext context, String message) {
        return new IllegalArgumentException(
            "Line " + context.getStart()
                .getLine()
                + ", Col "
                + context.getStart()
                    .getCharPositionInLine()
                + ": "
                + message);
    }

    private static String source(ParserRuleContext context) {
        return context.getStart()
            .getInputStream()
            .getText(
                new Interval(
                    context.getStart()
                        .getStartIndex(),
                    context.getStop()
                        .getStopIndex()));
    }
}
