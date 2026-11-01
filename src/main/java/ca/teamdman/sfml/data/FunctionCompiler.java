package ca.teamdman.sfml.data;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;

import ca.teamdman.langs.SFMLParser;
import ca.teamdman.sfml.ast.Statement;
import ca.teamdman.sfml.data.CompiledFunction.Body;
import ca.teamdman.sfml.data.CompiledFunction.Command;

/** Value functions mutate only explicit REF arguments; void procedures also allow bounded native actions. */
public final class FunctionCompiler {

    private final DataCompiler data;
    private DataType result;
    private int loopDepth;
    private final Function<SFMLParser.StatementContext, Statement> actionCompiler;

    public FunctionCompiler(DataCompiler data) {
        this(data, null);
    }

    public FunctionCompiler(DataCompiler data, Function<SFMLParser.StatementContext, Statement> actionCompiler) {
        this.data = data;
        this.actionCompiler = actionCompiler;
    }

    public void compile(List<SFMLParser.FunctionDefinitionContext> definitions) {
        List<CompiledFunction> functions = declare(definitions, null, -1);
        compileBodies(definitions, functions);
        data.validateFunctions();
    }

    public List<CompiledFunction> declare(List<SFMLParser.FunctionDefinitionContext> definitions, String moduleAlias,
        long revision) {
        List<CompiledFunction> functions = new ArrayList<>();
        // Declare every signature first, so body compilation supports forward references.
        for (SFMLParser.FunctionDefinitionContext definition : definitions) {
            try {
                List<String> names = new ArrayList<>();
                List<DataType> types = new ArrayList<>();
                List<Boolean> references = new ArrayList<>();
                for (SFMLParser.FunctionParameterContext parameter : definition.functionParameter()) {
                    names.add(variable(parameter.variableName()));
                    types.add(type(parameter.dataType()));
                    references.add(parameter.REF() != null);
                    if (types.get(types.size() - 1) == DataType.VOID)
                        throw fail(parameter, "Function parameters cannot be void");
                }
                functions.add(
                    data.declareFunction(
                        definition.functionName()
                            .getText(),
                        names,
                        types,
                        references,
                        type(definition.dataType()),
                        source(definition),
                        new SourceLocation(
                            definition.getStart()
                                .getLine(),
                            definition.getStart()
                                .getCharPositionInLine(),
                            "Function",
                            moduleAlias,
                            revision)));
            } catch (IllegalArgumentException invalid) {
                if (moduleAlias != null) throw moduleFailure(definition, moduleAlias, revision, invalid);
                throw invalid;
            }
        }
        return functions;
    }

    public void compileBodies(List<SFMLParser.FunctionDefinitionContext> definitions,
        List<CompiledFunction> functions) {
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
                if (function.getModuleAlias() != null) throw moduleFailure(
                    definitions.get(i),
                    function.getModuleAlias(),
                    function.getModuleRevision(),
                    ex);
                throw new IllegalArgumentException("Function " + function.getName() + ": " + ex.getMessage());
            }
        }
    }

    private static RuntimeException moduleFailure(ParserRuleContext definition, String alias, long revision,
        IllegalArgumentException error) {
        if (error instanceof SfmlRuntimeException && ((SfmlRuntimeException) error).isModuleLocation()) return error;
        java.util.regex.Matcher coordinates = java.util.regex.Pattern.compile("Line (\\d+), Col (\\d+):")
            .matcher(error.getMessage() == null ? "" : error.getMessage());
        boolean found = coordinates.find();
        return new SourceLocation(
            found ? Integer.parseInt(coordinates.group(1))
                : definition.getStart()
                    .getLine(),
            found ? Integer.parseInt(coordinates.group(2))
                : definition.getStart()
                    .getCharPositionInLine(),
            "Function",
            alias,
            revision).annotate(error);
    }

    private DataType type(SFMLParser.DataTypeContext context) {
        return DataTypeParser.fromSyntax(context);
    }

    private Body block(SFMLParser.BlockContext context) {
        List<Command> commands = new ArrayList<>();
        boolean returns = false;
        data.pushScope();
        try {
            for (SFMLParser.StatementContext statement : context.statement()) {
                if (returns) throw fail(statement, "Unreachable statement after RETURN");
                if (statement.breakStatement() != null) {
                    if (loopDepth == 0) throw fail(statement, "BREAK requires an enclosing loop in this function");
                    commands.add(runtime -> { throw LoopBreak.INSTANCE; });
                } else if (statement.waitStatement() != null) {
                    DataExpression ticks = expression(
                        statement.waitStatement()
                            .dataExpression());
                    require(ticks.getType(), DataType.INTEGER, statement);
                    data.markWait();
                    commands.add(runtime -> {
                        runtime.waitTicks(
                            ticks,
                            ticks.evaluate(runtime)
                                .asLong());
                        return null;
                    });
                } else if (statement.dataDeclaration() != null) {
                    SFMLParser.DataDeclarationContext declaration = statement.dataDeclaration();
                    DataExpression value = expression(declaration.dataExpression());
                    DataCompiler.Binding binding = data
                        .declareLocal(variable(declaration.variableName()), value.getType());
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
                    SFMLParser.DataExpressionContext returned = statement.returnStatement()
                        .dataExpression();
                    if (returned == null) {
                        if (result != DataType.VOID) throw fail(statement, "A value function must RETURN a value");
                        commands.add(runtime -> DataValue.voidValue());
                    } else {
                        if (result == DataType.VOID) throw fail(statement, "A void procedure cannot RETURN a value");
                        DataExpression value = expression(returned);
                        require(value.getType(), result, statement);
                        commands.add(value::evaluate);
                    }
                    returns = true;
                } else if (statement.functionCallStatement() != null) {
                    SFMLParser.FunctionCallStatementContext call = statement.functionCallStatement();
                    DataExpression invocation = data.compileCallStatement(
                        source(call),
                        call.getStart()
                            .getLine(),
                        call.getStart()
                            .getCharPositionInLine());
                    commands.add(runtime -> {
                        invocation.evaluate(runtime);
                        return null;
                    });
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
                    commands.add(loopAuthority(forLoop(statement.forStatement())));
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
                    commands.add(loopAuthority(runtime -> {
                        if (runtime.isResumable()) return ResumableLoops.whileLoop(
                            runtime,
                            condition,
                            () -> condition.evaluate(runtime)
                                .asBoolean(),
                            () -> body.execute(runtime));
                        while (true) {
                            runtime.loopStep();
                            if (!condition.evaluate(runtime)
                                .asBoolean()) return null;
                            DataValue value = body.execute(runtime);
                            if (value != null) return value;
                        }
                    }));
                } else if (result == DataType.VOID && actionCompiler != null
                    && (statement.printStatement() != null || statement.transferStatement() != null
                        || statement.inputStatement() != null
                        || statement.outputStatement() != null
                        || statement.redstoneOutputStatement() != null
                        || statement.forgetStatement() != null
                        || statement.circuitStatement() != null)) {
                            Statement action = actionCompiler.apply(statement);
                            commands.add(runtime -> {
                                runtime.runAction(action);
                                return null;
                            });
                        } else {
                            throw fail(
                                statement,
                                "Value functions permit only local data, typed control flow, WAIT and RETURN; use a void procedure for actions");
                        }
                int index = commands.size() - 1;
                Command command = commands.get(index);
                SourceLocation location = data.location(
                    statement.getStart()
                        .getLine(),
                    statement.getStart()
                        .getCharPositionInLine(),
                    statement.getChild(0)
                        .getClass()
                        .getSimpleName()
                        .replace("Context", ""));
                commands.set(index, runtime -> {
                    try {
                        return command.execute(runtime);
                    } catch (RuntimeException failure) {
                        throw location.annotate(failure);
                    }
                });
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
            if (context instanceof SFMLParser.JavaForContext) return javaFor((SFMLParser.JavaForContext) context);
            if (context instanceof SFMLParser.IndexedArrayForContext)
                return indexedArrayFor((SFMLParser.IndexedArrayForContext) context);
            if (context instanceof SFMLParser.CountedForContext) {
                SFMLParser.CountedForContext loop = (SFMLParser.CountedForContext) context;
                DataExpression first = expression(loop.dataExpression(0));
                DataExpression last = expression(loop.dataExpression(1));
                DataExpression step = loop.dataExpression()
                    .size() == 3 ? expression(loop.dataExpression(2)) : null;
                require(first.getType(), DataType.INTEGER, loop);
                require(last.getType(), DataType.INTEGER, loop);
                if (step != null) require(step.getType(), DataType.INTEGER, loop);
                DataCompiler.Binding binder = data
                    .declareReadOnlyLocal(variable(loop.variableName()), DataType.INTEGER);
                Body body = block(loop.block());
                return runtime -> {
                    if (runtime.isResumable()) return ResumableLoops
                        .counted(runtime, binder, binder, first, last, step, () -> body.execute(runtime));
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
                variable(loop.variableName()),
                array.getType()
                    .getElementType());
            Body body = block(loop.block());
            return runtime -> {
                if (runtime.isResumable())
                    return ResumableLoops.array(runtime, binder, binder, array, () -> body.execute(runtime));
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

    private Command javaFor(SFMLParser.JavaForContext loop) {
        SFMLParser.ForInitializerContext initializer = loop.forInitializer();
        Command first = initializer == null ? runtime -> null
            : forClause(
                initializer.dataDeclaration(),
                initializer.dataAssignment(),
                initializer.functionCallStatement());
        DataExpression test = loop.dataExpression() == null ? data.compile(
            "TRUE",
            loop.getStart()
                .getLine(),
            loop.getStart()
                .getCharPositionInLine())
            : expression(loop.dataExpression());
        require(test.getType(), DataType.BOOLEAN, loop);
        Body body = block(loop.block());
        // The body owns a child scope. Its declarations must not resolve from the update clause.
        SFMLParser.ForUpdateContext update = loop.forUpdate();
        Command step = update == null ? runtime -> null
            : forClause(null, update.dataAssignment(), update.functionCallStatement());
        boolean hasInitializer = initializer != null, hasUpdate = update != null;
        Object key = new Object();
        return runtime -> {
            if (runtime.isResumable()) return ResumableLoops.javaFor(
                runtime,
                key,
                !hasInitializer ? null : () -> first.execute(runtime),
                () -> test.evaluate(runtime)
                    .asBoolean(),
                () -> body.execute(runtime),
                !hasUpdate ? null : () -> step.execute(runtime));
            first.execute(runtime);
            while (true) {
                runtime.loopStep();
                if (!test.evaluate(runtime)
                    .asBoolean()) return null;
                DataValue value = body.execute(runtime);
                if (value != null) return value;
                step.execute(runtime);
            }
        };
    }

    private Command forClause(SFMLParser.DataDeclarationContext declaration,
        SFMLParser.DataAssignmentContext assignment, SFMLParser.FunctionCallStatementContext call) {
        if (declaration != null) {
            DataExpression value = expression(declaration.dataExpression());
            DataCompiler.Binding binding = data.declareLocal(variable(declaration.variableName()), value.getType());
            return runtime -> {
                binding.write(runtime, value.evaluate(runtime));
                return null;
            };
        }
        if (assignment != null) {
            DataCompiler.Assignment compiled = data.assignment(
                source(assignment.dataTarget()),
                expression(assignment.dataExpression()),
                assignment.getStart()
                    .getLine(),
                assignment.getStart()
                    .getCharPositionInLine());
            return runtime -> {
                compiled.execute(runtime);
                return null;
            };
        }
        if (call != null) {
            DataExpression invocation = data.compileCallStatement(
                source(call),
                call.getStart()
                    .getLine(),
                call.getStart()
                    .getCharPositionInLine());
            return runtime -> {
                invocation.evaluate(runtime);
                return null;
            };
        }
        throw new IllegalArgumentException("Invalid FOR clause");
    }

    private Command indexedArrayFor(SFMLParser.IndexedArrayForContext loop) {
        DataExpression array = expression(loop.dataExpression());
        if (!array.getType()
            .isSequence()) throw fail(loop, "FOR IN requires a sequence");
        DataCompiler.Binding index = data.declareReadOnlyLocal(variable(loop.variableName(0)), DataType.INTEGER);
        DataCompiler.Binding binder = data.declareReadOnlyLocal(
            variable(loop.variableName(1)),
            array.getType()
                .getElementType());
        Body body = block(loop.block());
        Object key = new Object();
        return runtime -> {
            if (runtime.isResumable())
                return ResumableLoops.array(runtime, key, index, binder, array, () -> body.execute(runtime));
            DataValue snapshot = array.evaluate(runtime);
            for (int i = 0; i < snapshot.size(); i++) {
                runtime.loopStep();
                index.write(runtime, DataValue.integer(i));
                binder.write(runtime, snapshot.get(i));
                DataValue value = body.execute(runtime);
                if (value != null) return value;
            }
            return null;
        };
    }

    private Command loopAuthority(Command loop) {
        Command action = result == DataType.VOID ? runtime -> runtime.runLoop(() -> loop.execute(runtime)) : loop;
        return runtime -> {
            try {
                return action.execute(runtime);
            } catch (LoopBreak done) {
                // Body.execute owns a separate command frame; normal completion drops all nested WAIT progress.
                return null;
            }
        };
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

    private static String variable(SFMLParser.VariableNameContext context) {
        String name = context.getText();
        return name.startsWith("$") ? name.substring(1) : name;
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
