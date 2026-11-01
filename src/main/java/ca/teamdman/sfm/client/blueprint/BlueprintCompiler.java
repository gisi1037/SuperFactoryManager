/*
 * Adapted from RetroSFM, Copyright (c) 2026 Saereth, MIT License.
 * Source: breakinblocks/RetroSFM b6776c456e87ace2ada5e5464fea9e4367d74bad.
 * See META-INF/licenses/RetroSFM-MIT.txt for the full license.
 */
package ca.teamdman.sfm.client.blueprint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

import ca.teamdman.langs.SFMLLexer;
import ca.teamdman.langs.SFMLParser;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Pin;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Type;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;
import ca.teamdman.sfml.program_builder.VisualCapabilityAnalyzer;
import ca.teamdman.sfml.program_builder.VisualCapabilityReport;
import ca.teamdman.sfml.program_builder.VisualCapabilityReport.Blocker;
import ca.teamdman.sfml.program_builder.VisualCapabilityReport.Kind;

/** Retro's trigger/branch/continuation emitter, adapted to this port's grammar and Java 8. */
public final class BlueprintCompiler {

    private BlueprintCompiler() {}

    public static String generate(BlueprintGraph graph) {
        return generate(graph, true);
    }

    /** Navigation validates the complete source once and compares this bounded structural projection. */
    static String navigationProjection(BlueprintGraph graph) {
        return generate(graph, false);
    }

    private static String generate(BlueprintGraph graph, boolean validate) {
        StringBuilder text = new StringBuilder();
        if (!graph.comments()
            .isEmpty())
            text.append(graph.comments())
                .append('\n');
        if (!graph.nameClause()
            .isEmpty()) {
            SFMLParser.ProgramContext name = validate ? parseSyntax(graph.nameClause()) : null;
            if (validate && (name.name() == null || !name.trigger()
                .isEmpty()
                || !name.dataDeclaration()
                    .isEmpty()
                || !name.stateDeclaration()
                    .isEmpty()
                || !name.functionDefinition()
                    .isEmpty()))
                throw new IllegalArgumentException("Use NAME \"Program name\"");
            text.append(graph.nameClause())
                .append('\n');
        }
        Set<UUID> visited = new HashSet<>();
        for (Node trigger : graph.roots()) {
            if (validate) validateClause(trigger.type, trigger.clause(), false);
            visited.add(trigger.id);
            if (!trigger.type.isTrigger()) {
                text.append(trigger.clause())
                    .append('\n');
                continue;
            }
            text.append(trigger.clause())
                .append(" DO\n");
            emit(graph, graph.next(trigger.id, Pin.NEXT), 1, visited, text, validate);
            text.append("END\n");
        }
        if (visited.size() != graph.nodes()
            .size()) throw new IllegalArgumentException("Connect every node to a trigger before saving");
        String code = text.toString()
            .trim();
        if (validate && !code.isEmpty()) parse(code);
        return code;
    }

    private static void emit(BlueprintGraph graph, Node node, int depth, Set<UUID> visited, StringBuilder text,
        boolean validate) {
        if (depth > BlueprintGraph.MAX_DEPTH) throw new IllegalArgumentException("Blueprint nesting exceeds 32");
        while (node != null) {
            if (!visited.add(node.id)) throw new IllegalArgumentException("Repeated node in control flow");
            if (validate) validateClause(node.type, node.clause(), false);
            indent(text, depth);
            if (node.type == Type.IF) {
                text.append("IF ")
                    .append(node.clause())
                    .append(" THEN\n");
                emit(graph, graph.next(node.id, Pin.TRUE), depth + 1, visited, text, validate);
                if (node.hasElse || graph.next(node.id, Pin.FALSE) != null) {
                    indent(text, depth);
                    text.append("ELSE\n");
                    emit(graph, graph.next(node.id, Pin.FALSE), depth + 1, visited, text, validate);
                }
                indent(text, depth);
                text.append("END\n");
            } else if (node.type.isLoop()) {
                text.append(node.clause())
                    .append(" DO\n");
                emit(graph, graph.next(node.id, Pin.BODY), depth + 1, visited, text, validate);
                indent(text, depth);
                text.append("END\n");
            } else text.append(node.clause())
                .append('\n');
            node = graph.next(node.id, node.type.continuation());
        }
    }

    private static void indent(StringBuilder text, int depth) {
        for (int i = 0; i < depth; i++) text.append("  ");
    }

    public static void validateClause(Type type, String clause) {
        validateClause(type, clause, true);
    }

    private static void validateClause(Type type, String clause, boolean checkLiterals) {
        if (type == Type.IF) StudioConditionSyntax.parse(clause);
        if (type == Type.PRINT) StudioExpressionSyntax.print(clause);
        if (type == Type.TRANSFER) StudioExpressionSyntax.transfer(clause);
        if (type == Type.NAMED_REDSTONE) StudioNamedRedstoneHeader.parse(clause);
        if (clause == null || clause.length() > BlueprintGraph.MAX_CLAUSE)
            throw new IllegalArgumentException("Node clause too long");
        for (Token token : tokens(clause).getTokens()) if (token.getType() == SFMLLexer.LINE_COMMENT)
            throw new IllegalArgumentException("Use SFML clauses without inline comments");
        boolean declaration = type.isRoot() && !type.isTrigger();
        String source = declaration ? clause
            : type.isTrigger() ? clause + " DO\nEND"
                : "EVERY 20 TICKS DO\n"
                    + (type == Type.IF ? "IF " + clause + " THEN\nEND" : type.isLoop() ? clause + " DO\nEND" : clause)
                    + "\nEND";
        SFMLParser.ProgramContext tree = parseSyntax(source);
        if (!tree.importDeclaration()
            .isEmpty()) throw new IllegalArgumentException("Use one node clause without imports");
        if (declaration) {
            if (tree.name() != null || !tree.trigger()
                .isEmpty()
                || !tree.functionDefinition()
                    .isEmpty()
                || tree.dataDeclaration()
                    .size() != (type == Type.GLOBAL_LET ? 1 : 0)
                || tree.stateDeclaration()
                    .size() != (type == Type.STATE ? 1 : 0))
                throw new IllegalArgumentException("Use one " + type.title + " declaration");
            if (checkLiterals && !hasScopedReference(tree)) parse(source);
            return;
        }
        if (tree.name() != null || tree.trigger()
            .size() != 1
            || !tree.dataDeclaration()
                .isEmpty()
            || !tree.stateDeclaration()
                .isEmpty()
            || !tree.functionDefinition()
                .isEmpty())
            throw new IllegalArgumentException("One clause per node");
        SFMLParser.TriggerContext trigger = tree.trigger(0);
        if (type.isTrigger()) {
            if (type != triggerType(trigger) || !triggerBlock(trigger).statement()
                .isEmpty()) throw new IllegalArgumentException("Use the matching trigger header without DO or END");
        } else {
            List<SFMLParser.StatementContext> statements = triggerBlock(trigger).statement();
            if (statements.size() != 1 || typeOf(statements.get(0)) != type)
                throw new IllegalArgumentException("Use one " + type.title + " clause");
            if (type == Type.IF) {
                SFMLParser.IfStatementContext condition = statements.get(0)
                    .ifStatement();
                if (condition.boolexpr()
                    .size() != 1
                    || condition.block()
                        .size() != 1
                    || !condition.block(0)
                        .statement()
                        .isEmpty())
                    throw new IllegalArgumentException("Enter only the IF condition, without THEN or END");
            }
            if (type.isLoop() && !loopBlock(statements.get(0)).statement()
                .isEmpty()) throw new IllegalArgumentException("Enter only the loop header, without DO or END");
            if (type == Type.WHILE) StudioConditionSyntax.parse(
                fragment(
                    tokens(source),
                    statements.get(0)
                        .whileStatement()
                        .boolexpr()));
        }
        if (checkLiterals && !hasScopedReference(tree)) parse(source);
    }

    /** Bindings are declarations, while expression primaries, label variables and assignment targets need scope. */
    private static boolean hasScopedReference(org.antlr.v4.runtime.tree.ParseTree tree) {
        if (tree instanceof SFMLParser.RawLabelContext || tree instanceof SFMLParser.VariableLabelContext
            || tree instanceof SFMLParser.DataTargetContext) return true;
        if (tree instanceof SFMLParser.FunctionCallContext && !((SFMLParser.FunctionCallContext) tree).functionName()
            .DOT()
            .isEmpty()) return true;
        if (tree instanceof SFMLParser.DataPrimaryContext) {
            SFMLParser.DataPrimaryContext primary = (SFMLParser.DataPrimaryContext) tree;
            if (primary.IDENTIFIER() != null || primary.VARIABLE() != null
                || primary.IMPORT() != null
                || primary.REVISION() != null
                || primary.DIGEST() != null) return true;
        }
        for (int i = 0; i < tree.getChildCount(); i++) if (hasScopedReference(tree.getChild(i))) return true;
        return false;
    }

    public static BlueprintGraph importText(String code) {
        ImportResult result = inspectText(code);
        if (!result.report.isAvailable()) throw new ImportException(result.report);
        return result.graph;
    }

    /** One guarded compile of a draft; a failed inspection never exposes a partial graph. */
    public static ImportResult inspectText(String source) {
        String code = source == null ? "" : source;
        if (code.length() <= 32767 && code.trim()
            .isEmpty())
            return new ImportResult(new BlueprintGraph(), new VisualCapabilityReport(Collections.emptyList()));
        ProgramBuildResult built = new ProgramBuilder(code).buildWithSyntax();
        VisualCapabilityReport report = VisualCapabilityAnalyzer.analyze(built);
        if (!built.isSuccess()) return new ImportResult(null, report);
        SFMLParser.ProgramContext tree = built.getSyntax();
        List<Blocker> limits = new ArrayList<>(report.getBlockers());
        inspectLimits(tree, built.getTokens(), 0, new int[] { 0 }, limits);
        if (!limits.isEmpty()) return new ImportResult(null, new VisualCapabilityReport(limits));
        try {
            BlueprintGraph graph = importParsed(code, tree, built.getTokens());
            return new ImportResult(graph, report);
        } catch (IllegalArgumentException e) {
            return new ImportResult(
                null,
                new VisualCapabilityReport(
                    Collections
                        .singletonList(VisualCapabilityAnalyzer.at(Kind.SOURCE_FIDELITY, e.getMessage(), tree))));
        }
    }

    public static final class ImportResult {

        public final BlueprintGraph graph;
        public final VisualCapabilityReport report;

        private ImportResult(BlueprintGraph graph, VisualCapabilityReport report) {
            this.graph = graph;
            this.report = report;
        }
    }

    public static final class ImportException extends IllegalArgumentException {

        private final VisualCapabilityReport report;

        private ImportException(VisualCapabilityReport report) {
            super(report.getMessage());
            this.report = report;
        }

        public VisualCapabilityReport getReport() {
            return report;
        }
    }

    private static void inspectLimits(org.antlr.v4.runtime.tree.ParseTree tree, CommonTokenStream tokens, int depth,
        int[] nodes, List<Blocker> blockers) {
        ParserRuleContext clause = null;
        if (tree instanceof SFMLParser.NameContext) clause = (ParserRuleContext) tree;
        boolean declaration = tree instanceof SFMLParser.StateDeclarationContext
            || tree instanceof SFMLParser.DataDeclarationContext
                && tree.getParent() instanceof SFMLParser.ProgramContext;
        if (declaration) clause = (ParserRuleContext) tree;
        if (tree instanceof SFMLParser.TriggerContext || tree instanceof SFMLParser.StatementContext || declaration) {
            ParserRuleContext context = (ParserRuleContext) tree;
            if (++nodes[0] == BlueprintGraph.MAX_NODES + 1)
                blockers.add(VisualCapabilityAnalyzer.at(Kind.GRAPH_LIMIT, "Blueprint exceeds 256 nodes", context));
            if (tree instanceof SFMLParser.StatementContext) {
                SFMLParser.IfStatementContext branch = ((SFMLParser.StatementContext) tree).ifStatement();
                SFMLParser.StatementContext statement = (SFMLParser.StatementContext) tree;
                if (statement.forStatement() != null || statement.whileStatement() != null) {
                    try {
                        validateClause(
                            typeOf(statement),
                            fragment(
                                tokens,
                                context.getStart()
                                    .getTokenIndex(),
                                loopDo(statement).getTokenIndex() - 1),
                            false);
                    } catch (IllegalArgumentException unsafe) {
                        blockers.add(VisualCapabilityAnalyzer.at(Kind.GRAPH_LIMIT, unsafe.getMessage(), context));
                    }
                } else clause = branch == null ? context : branch.boolexpr(0);
                if (branch != null) for (SFMLParser.BoolexprContext predicate : branch.boolexpr()) {
                    try {
                        StudioConditionSyntax.parse(fragment(tokens, predicate));
                    } catch (IllegalArgumentException unsafe) {
                        blockers.add(VisualCapabilityAnalyzer.at(Kind.GRAPH_LIMIT, unsafe.getMessage(), predicate));
                    }
                }
            } else if (tree instanceof SFMLParser.TriggerContext) {
                SFMLParser.TriggerContext trigger = (SFMLParser.TriggerContext) tree;
                int end = triggerDo(trigger).getTokenIndex();
                if (fragment(
                    tokens,
                    context.getStart()
                        .getTokenIndex(),
                    end - 1).length() > BlueprintGraph.MAX_CLAUSE)
                    blockers.add(
                        VisualCapabilityAnalyzer.at(Kind.GRAPH_LIMIT, "Node clause exceeds 4096 characters", context));
            }
        }
        if (clause != null && fragment(tokens, clause).length() > BlueprintGraph.MAX_CLAUSE)
            blockers.add(VisualCapabilityAnalyzer.at(Kind.GRAPH_LIMIT, "Node clause exceeds 4096 characters", clause));
        if ((tree instanceof SFMLParser.IfStatementContext || tree instanceof SFMLParser.ForStatementContext
            || tree instanceof SFMLParser.WhileStatementContext) && depth == BlueprintGraph.MAX_DEPTH)
            blockers.add(
                VisualCapabilityAnalyzer
                    .at(Kind.GRAPH_LIMIT, "Blueprint nesting exceeds 32", (ParserRuleContext) tree));
        if (tree instanceof SFMLParser.BlockContext) depth++;
        for (int i = 0; i < tree.getChildCount(); i++) inspectLimits(tree.getChild(i), tokens, depth, nodes, blockers);
    }

    private static BlueprintGraph importParsed(String code, SFMLParser.ProgramContext tree, CommonTokenStream tokens) {
        BlueprintGraph graph = new BlueprintGraph();
        if (tree.name() != null) graph.setNameClause(fragment(tokens, tree.name()));
        StringBuilder comments = new StringBuilder();
        for (Token token : tokens.getTokens())
            if (token.getType() == SFMLLexer.LINE_COMMENT) comments.append(token.getText())
                .append('\n');
        graph.setComments(
            comments.toString()
                .trim());
        int columnX = 20;
        // Program declaration accessors separate LET and STATE; children preserve their mixed source order.
        for (int i = 0; i < tree.getChildCount(); i++) {
            org.antlr.v4.runtime.tree.ParseTree child = tree.getChild(i);
            Type type = child instanceof SFMLParser.DataDeclarationContext ? Type.GLOBAL_LET
                : child instanceof SFMLParser.StateDeclarationContext ? Type.STATE : null;
            if (type != null) {
                graph.add(new Node(UUID.randomUUID(), type, columnX, 20, fragment(tokens, (ParserRuleContext) child)));
                columnX += 210;
            }
        }
        for (SFMLParser.TriggerContext trigger : tree.trigger()) {
            Type type = triggerType(trigger);
            int end = triggerDo(trigger).getTokenIndex();
            Node node = new Node(
                UUID.randomUUID(),
                type,
                columnX,
                20,
                fragment(
                    tokens,
                    trigger.getStart()
                        .getTokenIndex(),
                    end - 1));
            graph.add(node);
            importBlock(graph, tokens, triggerBlock(trigger), node, Pin.NEXT, columnX, 1);
            for (Node placed : graph.nodes()) columnX = Math.max(columnX, (int) placed.x() + 210);
        }
        layout(graph);
        // Compare grammar tokens, not AST.toString(): it can omit modifiers and reorder clauses.
        if (!sameTokens(code, generate(graph, false)))
            throw new IllegalArgumentException("This text cannot be imported losslessly yet; use the text editor");
        return graph;
    }

    private static void importBlock(BlueprintGraph graph, CommonTokenStream tokens, SFMLParser.BlockContext block,
        Node previous, Pin pin, int x, int depth) {
        if (depth > BlueprintGraph.MAX_DEPTH) throw new IllegalArgumentException("Blueprint nesting exceeds 32");
        for (SFMLParser.StatementContext statement : block.statement()) {
            Type type = typeOf(statement);
            String clause = statement.SEMICOLON() == null ? fragment(tokens, statement)
                : fragment(
                    tokens,
                    statement.getStart()
                        .getTokenIndex(),
                    statement.getStop()
                        .getTokenIndex() - 1);
            if (type == Type.IF) {
                if (statement.ifStatement()
                    .boolexpr()
                    .size() != 1)
                    throw new IllegalArgumentException(
                        "ELSE IF import is not available yet; keep this program in the text editor");
                clause = fragment(
                    tokens,
                    statement.ifStatement()
                        .boolexpr(0));
            } else if (type.isLoop()) {
                clause = fragment(
                    tokens,
                    statement.getStart()
                        .getTokenIndex(),
                    loopDo(statement).getTokenIndex() - 1);
            }
            Node node = new Node(
                UUID.randomUUID(),
                type,
                x,
                previous.y() + (previous.type == Type.IF || previous.type.isLoop() ? 120 : 80),
                clause);
            graph.add(node);
            graph.connect(previous.id, pin, node.id);
            if (type == Type.IF) {
                SFMLParser.IfStatementContext branch = statement.ifStatement();
                importBlock(graph, tokens, branch.block(0), node, Pin.TRUE, x + 190, depth + 1);
                node.hasElse = branch.block()
                    .size() == 2;
                if (node.hasElse) importBlock(graph, tokens, branch.block(1), node, Pin.FALSE, x + 380, depth + 1);
            } else
                if (type.isLoop()) importBlock(graph, tokens, loopBlock(statement), node, Pin.BODY, x + 190, depth + 1);
            previous = node;
            pin = type.continuation();
        }
    }

    /** Place complete branches in separate lanes, then their continuation below both branches. */
    private static void layout(BlueprintGraph graph) {
        double x = 20;
        for (Node trigger : graph.roots()) {
            trigger.setPosition(x, 20);
            double[] extent = trigger.type.isTrigger() ? layoutChain(graph, graph.next(trigger.id, Pin.NEXT), x, 100)
                : new double[] { x, 100 };
            x = Math.max(x + 210, extent[0] + 210);
        }
    }

    private static double[] layoutChain(BlueprintGraph graph, Node node, double x, double y) {
        double maxX = x;
        while (node != null) {
            node.setPosition(x, y);
            y += node.type == Type.IF || node.type.isLoop() ? 120 : 80;
            if (node.type == Type.IF) {
                double[] yes = layoutChain(graph, graph.next(node.id, Pin.TRUE), x + 190, y);
                double[] no = layoutChain(graph, graph.next(node.id, Pin.FALSE), yes[0] + 190, y);
                maxX = Math.max(maxX, Math.max(yes[0], no[0]));
                y = Math.max(yes[1], no[1]) + 20;
            } else if (node.type.isLoop()) {
                double[] body = layoutChain(graph, graph.next(node.id, Pin.BODY), x + 190, y);
                maxX = Math.max(maxX, body[0]);
                y = body[1] + 20;
            }
            node = graph.next(node.id, node.type.continuation());
        }
        return new double[] { maxX, y };
    }

    public static Type triggerType(SFMLParser.TriggerContext trigger) {
        if (trigger instanceof SFMLParser.TimerTriggerContext) return Type.TIMER;
        if (trigger instanceof SFMLParser.PulseTriggerContext) return Type.PULSE;
        if (trigger instanceof SFMLParser.NamedRedstoneTriggerContext) return Type.NAMED_REDSTONE;
        throw new IllegalArgumentException("Unsupported trigger type");
    }

    public static Token triggerDo(SFMLParser.TriggerContext trigger) {
        if (trigger instanceof SFMLParser.TimerTriggerContext) {
            SFMLParser.TimerTriggerContext timer = (SFMLParser.TimerTriggerContext) trigger;
            return (timer.DO() != null ? timer.DO() : timer.LBRACE()).getSymbol();
        }
        if (trigger instanceof SFMLParser.PulseTriggerContext) {
            SFMLParser.PulseTriggerContext pulse = (SFMLParser.PulseTriggerContext) trigger;
            return (pulse.DO() != null ? pulse.DO() : pulse.LBRACE()).getSymbol();
        }
        if (trigger instanceof SFMLParser.NamedRedstoneTriggerContext) {
            SFMLParser.NamedRedstoneTriggerContext named = (SFMLParser.NamedRedstoneTriggerContext) trigger;
            return (named.DO() != null ? named.DO() : named.LBRACE()).getSymbol();
        }
        throw new IllegalArgumentException("Unsupported trigger type");
    }

    public static SFMLParser.BlockContext triggerBlock(SFMLParser.TriggerContext trigger) {
        if (trigger instanceof SFMLParser.TimerTriggerContext)
            return ((SFMLParser.TimerTriggerContext) trigger).block();
        if (trigger instanceof SFMLParser.PulseTriggerContext)
            return ((SFMLParser.PulseTriggerContext) trigger).block();
        if (trigger instanceof SFMLParser.NamedRedstoneTriggerContext)
            return ((SFMLParser.NamedRedstoneTriggerContext) trigger).block();
        throw new IllegalArgumentException("Unsupported trigger type");
    }

    public static Token loopDo(SFMLParser.StatementContext statement) {
        if (statement.forStatement() instanceof SFMLParser.CountedForContext) {
            SFMLParser.CountedForContext loop = (SFMLParser.CountedForContext) statement.forStatement();
            return (loop.DO() != null ? loop.DO() : loop.LBRACE()).getSymbol();
        }
        if (statement.forStatement() instanceof SFMLParser.ArrayForContext) {
            SFMLParser.ArrayForContext loop = (SFMLParser.ArrayForContext) statement.forStatement();
            return (loop.DO() != null ? loop.DO() : loop.LBRACE()).getSymbol();
        }
        if (statement.forStatement() instanceof SFMLParser.IndexedArrayForContext) {
            SFMLParser.IndexedArrayForContext loop = (SFMLParser.IndexedArrayForContext) statement.forStatement();
            return (loop.DO() != null ? loop.DO() : loop.LBRACE()).getSymbol();
        }
        if (statement.forStatement() instanceof SFMLParser.JavaForContext) {
            SFMLParser.JavaForContext loop = (SFMLParser.JavaForContext) statement.forStatement();
            return (loop.DO() != null ? loop.DO() : loop.LBRACE()).getSymbol();
        }
        if (statement.whileStatement() != null) {
            SFMLParser.WhileStatementContext loop = statement.whileStatement();
            return (loop.DO() != null ? loop.DO() : loop.LBRACE()).getSymbol();
        }
        throw new IllegalArgumentException("Expected a loop statement");
    }

    public static SFMLParser.BlockContext loopBlock(SFMLParser.StatementContext statement) {
        if (statement.forStatement() instanceof SFMLParser.CountedForContext)
            return ((SFMLParser.CountedForContext) statement.forStatement()).block();
        if (statement.forStatement() instanceof SFMLParser.ArrayForContext)
            return ((SFMLParser.ArrayForContext) statement.forStatement()).block();
        if (statement.forStatement() instanceof SFMLParser.IndexedArrayForContext)
            return ((SFMLParser.IndexedArrayForContext) statement.forStatement()).block();
        if (statement.forStatement() instanceof SFMLParser.JavaForContext)
            return ((SFMLParser.JavaForContext) statement.forStatement()).block();
        if (statement.whileStatement() != null) return statement.whileStatement()
            .block();
        throw new IllegalArgumentException("Expected a loop statement");
    }

    public static Type typeOf(SFMLParser.StatementContext statement) {
        if (statement.forStatement() instanceof SFMLParser.CountedForContext) return Type.FOR;
        if (statement.forStatement() instanceof SFMLParser.ArrayForContext) return Type.FOREACH;
        if (statement.forStatement() instanceof SFMLParser.IndexedArrayForContext) return Type.INDEXED_FOREACH;
        if (statement.forStatement() instanceof SFMLParser.JavaForContext) return Type.JAVA_FOR;
        if (statement.whileStatement() != null) return Type.WHILE;
        if (statement.dataDeclaration() != null) return Type.LET;
        if (statement.dataAssignment() != null) return Type.SET;
        if (statement.inputStatement() != null) return Type.INPUT;
        if (statement.outputStatement() != null) return Type.OUTPUT;
        if (statement.redstoneOutputStatement() != null) return Type.REDSTONE;
        if (statement.forgetStatement() != null) return Type.FORGET;
        if (statement.ifStatement() != null) return Type.IF;
        if (statement.transferStatement() != null) return Type.TRANSFER;
        if (statement.printStatement() != null) return Type.PRINT;
        throw new IllegalArgumentException("Unsupported statement; use the text editor");
    }

    private static SFMLParser.ProgramContext parse(String code) {
        ProgramBuildResult result = new ProgramBuilder(code).buildWithSyntax();
        if (!result.isSuccess()) throw new IllegalArgumentException(result.getErrorMessage());
        VisualCapabilityReport report = VisualCapabilityAnalyzer.analyze(result);
        if (!report.isAvailable()) throw new ImportException(report);
        return result.getSyntax();
    }

    /** Clause edits have no symbol environment. Resolve names only in the final whole-program build. */
    private static SFMLParser.ProgramContext parseSyntax(String code) {
        BaseErrorListener errors = new BaseErrorListener() {

            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object symbol, int line, int column, String message,
                RecognitionException cause) {
                throw new IllegalArgumentException("Line " + line + ", Col " + column + ": " + message);
            }
        };
        SFMLLexer lexer = new SFMLLexer(CharStreams.fromString(code));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errors);
        CommonTokenStream stream = new CommonTokenStream(lexer);
        stream.fill();
        boolean hasData = false;
        for (Token token : stream.getTokens())
            if (token.getType() == SFMLLexer.VARIABLE || token.getType() == SFMLLexer.LBRACKET
                || token.getType() == SFMLLexer.LBRACE) hasData = true;
        int nesting = 0, unary = 0;
        for (Token token : stream.getTokens()) {
            if (token.getChannel() != Token.DEFAULT_CHANNEL) continue;
            int kind = token.getType();
            if (kind == SFMLLexer.LPAREN || kind == SFMLLexer.LBRACKET || kind == SFMLLexer.LBRACE) nesting++;
            if (kind == SFMLLexer.RPAREN || kind == SFMLLexer.RBRACKET || kind == SFMLLexer.RBRACE) nesting--;
            unary = kind == SFMLLexer.NOT || kind == SFMLLexer.PLUS
                || kind == SFMLLexer.DASH
                || kind == SFMLLexer.BIT_NOT ? unary + 1 : 0;
            if (nesting > 128 || unary > 128) throw new IllegalArgumentException("Program nesting exceeds 128");
            if (hasData && (nesting > StudioExpressionSyntax.MAX_DEPTH || unary > StudioExpressionSyntax.MAX_DEPTH))
                throw new IllegalArgumentException("Data expression nesting exceeds 32");
        }
        SFMLParser parser = new SFMLParser(stream);
        parser.removeErrorListeners();
        parser.addErrorListener(errors);
        parser.setErrorHandler(new BailErrorStrategy());
        try {
            SFMLParser.ProgramContext tree = parser.program();
            validateExpressionSyntax(tree, stream);
            return tree;
        } catch (org.antlr.v4.runtime.misc.ParseCancellationException invalid) {
            throw new IllegalArgumentException("Enter one complete SFML clause", invalid);
        } catch (StackOverflowError invalid) {
            throw new IllegalArgumentException("Clause is too complex; use Text", invalid);
        }
    }

    private static void validateExpressionSyntax(org.antlr.v4.runtime.tree.ParseTree tree, CommonTokenStream stream) {
        if (tree instanceof SFMLParser.DataExpressionContext) {
            StudioExpressionSyntax.expression(fragment(stream, (ParserRuleContext) tree));
            return;
        }
        for (int i = 0; i < tree.getChildCount(); i++) validateExpressionSyntax(tree.getChild(i), stream);
    }

    static CommonTokenStream tokens(String code) {
        SFMLLexer lexer = new SFMLLexer(CharStreams.fromString(code));
        lexer.removeErrorListeners();
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        tokens.fill();
        return tokens;
    }

    private static String fragment(CommonTokenStream tokens, ParserRuleContext context) {
        return fragment(
            tokens,
            context.getStart()
                .getTokenIndex(),
            context.getStop()
                .getTokenIndex());
    }

    private static String fragment(CommonTokenStream tokens, int start, int end) {
        StringBuilder text = new StringBuilder();
        for (int i = start; i <= end; i++) {
            Token token = tokens.get(i);
            if (token.getChannel() == Token.DEFAULT_CHANNEL) {
                if (text.length() > 0) text.append(' ');
                text.append(token.getText());
            }
        }
        return text.toString();
    }

    static boolean sameTokens(String a, String b) {
        CommonTokenStream first = tokens(a), second = tokens(b);
        List<Token> left = first.getTokens(), right = second.getTokens();
        Map<Integer, Integer> leftBlocks = blockTokens(first), rightBlocks = blockTokens(second);
        int i = 0, j = 0;
        while (true) {
            while (left.get(i)
                .getChannel() != Token.DEFAULT_CHANNEL || normalized(left.get(i), leftBlocks) == -2) i++;
            while (right.get(j)
                .getChannel() != Token.DEFAULT_CHANNEL || normalized(right.get(j), rightBlocks) == -2) j++;
            Token x = left.get(i++), y = right.get(j++);
            if (normalized(x, leftBlocks) != normalized(y, rightBlocks)) return false;
            if (x.getType() == Token.EOF) return true;
            if ((x.getType() == SFMLLexer.IDENTIFIER || x.getType() == SFMLLexer.VARIABLE
                || x.getType() == SFMLLexer.STRING
                || x.getType() == SFMLLexer.NUMBER
                || x.getType() == SFMLLexer.NUMBER_WITH_G_SUFFIX)
                && !x.getText()
                    .equals(y.getText()))
                return false;
        }
    }

    private static int normalized(Token token, Map<Integer, Integer> blocks) {
        return blocks.getOrDefault(token.getTokenIndex(), token.getType());
    }

    /** Normalize only grammar-owned control delimiters. Struct literal braces remain exact source tokens. */
    private static Map<Integer, Integer> blockTokens(CommonTokenStream tokens) {
        boolean braces = false;
        for (Token token : tokens.getTokens())
            if (token.getType() == SFMLLexer.LBRACE || token.getType() == SFMLLexer.SEMICOLON) {
                braces = true;
                break;
            }
        if (!braces) return Collections.emptyMap();
        SFMLParser parser = new SFMLParser(tokens);
        parser.removeErrorListeners();
        SFMLParser.ProgramContext tree = parser.program();
        Map<Integer, Integer> replacements = new HashMap<>();
        if (parser.getNumberOfSyntaxErrors() == 0) normalizeBlocks(tree, replacements);
        return replacements;
    }

    private static void normalizeBlocks(org.antlr.v4.runtime.tree.ParseTree tree, Map<Integer, Integer> replacements) {
        if (tree instanceof org.antlr.v4.runtime.tree.TerminalNode) {
            Token token = ((org.antlr.v4.runtime.tree.TerminalNode) tree).getSymbol();
            // Header separators are executable syntax. Only statement/global terminators are optional.
            if (token.getType() == SFMLLexer.SEMICOLON && (tree.getParent() instanceof SFMLParser.StatementContext
                || tree.getParent() instanceof SFMLParser.ProgramContext)) replacements.put(token.getTokenIndex(), -2);
        }
        if (tree instanceof SFMLParser.TriggerContext) {
            SFMLParser.TriggerContext trigger = (SFMLParser.TriggerContext) tree;
            Token open = triggerDo(trigger);
            if (open.getType() == SFMLLexer.LBRACE) {
                replacements.put(open.getTokenIndex(), SFMLLexer.DO);
                replacements.put(
                    trigger.getStop()
                        .getTokenIndex(),
                    SFMLLexer.END);
            }
        } else if (tree instanceof SFMLParser.StatementContext
            && (((SFMLParser.StatementContext) tree).forStatement() != null
                || ((SFMLParser.StatementContext) tree).whileStatement() != null)) {
                    SFMLParser.StatementContext statement = (SFMLParser.StatementContext) tree;
                    Token open = loopDo(statement);
                    if (open.getType() == SFMLLexer.LBRACE) {
                        replacements.put(open.getTokenIndex(), SFMLLexer.DO);
                        ParserRuleContext loop = statement.forStatement() != null ? statement.forStatement()
                            : statement.whileStatement();
                        replacements.put(
                            loop.getStop()
                                .getTokenIndex(),
                            SFMLLexer.END);
                    }
                } else
            if (tree instanceof SFMLParser.IfStatementContext) {
                SFMLParser.IfStatementContext branch = (SFMLParser.IfStatementContext) tree;
                if (!branch.LBRACE()
                    .isEmpty())
                    for (int i = 0; i < branch.block()
                        .size(); i++) {
                            replacements.put(
                                branch.LBRACE(i)
                                    .getSymbol()
                                    .getTokenIndex(),
                                i < branch.boolexpr()
                                    .size() ? SFMLLexer.THEN : -2);
                            replacements.put(
                                branch.RBRACE(i)
                                    .getSymbol()
                                    .getTokenIndex(),
                                i == branch.block()
                                    .size() - 1 ? SFMLLexer.END : -2);
                        }
            }
        for (int i = 0; i < tree.getChildCount(); i++) normalizeBlocks(tree.getChild(i), replacements);
    }
}
