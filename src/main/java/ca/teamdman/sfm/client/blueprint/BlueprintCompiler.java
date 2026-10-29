/*
 * Adapted from RetroSFM, Copyright (c) 2026 Saereth, MIT License.
 * Source: breakinblocks/RetroSFM b6776c456e87ace2ada5e5464fea9e4367d74bad.
 * See META-INF/licenses/RetroSFM-MIT.txt for the full license.
 */
package ca.teamdman.sfm.client.blueprint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
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

    private static String generate(BlueprintGraph graph, boolean validate) {
        StringBuilder text = new StringBuilder();
        if (!graph.comments()
            .isEmpty())
            text.append(graph.comments())
                .append('\n');
        if (!graph.nameClause()
            .isEmpty()) {
            SFMLParser.ProgramContext name = validate ? parse(graph.nameClause()) : null;
            if (validate && (name.name() == null || !name.trigger()
                .isEmpty())) throw new IllegalArgumentException("Use NAME \"Program name\"");
            text.append(graph.nameClause())
                .append('\n');
        }
        Set<UUID> visited = new HashSet<>();
        for (Node trigger : graph.triggers()) {
            if (validate) validateClause(trigger.type, trigger.clause());
            visited.add(trigger.id);
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
            if (validate) validateClause(node.type, node.clause());
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
            } else text.append(node.clause())
                .append('\n');
            node = graph.next(node.id, node.type.continuation());
        }
    }

    private static void indent(StringBuilder text, int depth) {
        for (int i = 0; i < depth; i++) text.append("  ");
    }

    public static void validateClause(Type type, String clause) {
        if (clause == null || clause.length() > BlueprintGraph.MAX_CLAUSE)
            throw new IllegalArgumentException("Node clause too long");
        String source = type.isTrigger() ? clause + " DO\nEND"
            : "EVERY 20 TICKS DO\n" + (type == Type.IF ? "IF " + clause + " THEN\nEND" : clause) + "\nEND";
        SFMLParser.ProgramContext tree = parse(source);
        if (tree.name() != null || tree.trigger()
            .size() != 1) throw new IllegalArgumentException("One clause per node");
        SFMLParser.TriggerContext trigger = tree.trigger(0);
        if (type.isTrigger()) {
            boolean right = type == Type.TIMER ? trigger instanceof SFMLParser.TimerTriggerContext
                : trigger instanceof SFMLParser.PulseTriggerContext;
            if (!right || !block(trigger).statement()
                .isEmpty()) throw new IllegalArgumentException("Use the matching trigger header without DO or END");
        } else {
            List<SFMLParser.StatementContext> statements = block(trigger).statement();
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
        }
        CommonTokenStream tokens = tokens(clause);
        for (Token token : tokens.getTokens()) if (token.getType() == SFMLLexer.LINE_COMMENT)
            throw new IllegalArgumentException("Use SFML clauses without inline comments");
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
        if (tree instanceof SFMLParser.TriggerContext || tree instanceof SFMLParser.StatementContext) {
            ParserRuleContext context = (ParserRuleContext) tree;
            if (++nodes[0] == BlueprintGraph.MAX_NODES + 1)
                blockers.add(VisualCapabilityAnalyzer.at(Kind.GRAPH_LIMIT, "Blueprint exceeds 256 nodes", context));
            if (tree instanceof SFMLParser.StatementContext) {
                SFMLParser.IfStatementContext branch = ((SFMLParser.StatementContext) tree).ifStatement();
                clause = branch == null ? context : branch.boolexpr(0);
            } else {
                SFMLParser.TriggerContext trigger = (SFMLParser.TriggerContext) tree;
                int end = trigger instanceof SFMLParser.TimerTriggerContext
                    ? ((SFMLParser.TimerTriggerContext) trigger).DO()
                        .getSymbol()
                        .getTokenIndex()
                    : trigger instanceof SFMLParser.NamedRedstoneTriggerContext
                        ? ((SFMLParser.NamedRedstoneTriggerContext) trigger).DO()
                            .getSymbol()
                            .getTokenIndex()
                        : ((SFMLParser.PulseTriggerContext) trigger).DO()
                            .getSymbol()
                            .getTokenIndex();
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
        if (tree instanceof SFMLParser.IfStatementContext && depth == BlueprintGraph.MAX_DEPTH) blockers.add(
            VisualCapabilityAnalyzer.at(Kind.GRAPH_LIMIT, "Blueprint nesting exceeds 32", (ParserRuleContext) tree));
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
        for (SFMLParser.TriggerContext trigger : tree.trigger()) {
            Type type = trigger instanceof SFMLParser.TimerTriggerContext ? Type.TIMER : Type.PULSE;
            int end = trigger instanceof SFMLParser.TimerTriggerContext
                ? ((SFMLParser.TimerTriggerContext) trigger).DO()
                    .getSymbol()
                    .getTokenIndex()
                : ((SFMLParser.PulseTriggerContext) trigger).DO()
                    .getSymbol()
                    .getTokenIndex();
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
            importBlock(graph, tokens, block(trigger), node, Pin.NEXT, columnX, 1);
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
            String clause = fragment(tokens, statement);
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
            }
            Node node = new Node(
                UUID.randomUUID(),
                type,
                x,
                previous.y() + (previous.type == Type.IF ? 120 : 80),
                clause);
            graph.add(node);
            graph.connect(previous.id, pin, node.id);
            if (type == Type.IF) {
                SFMLParser.IfStatementContext branch = statement.ifStatement();
                importBlock(graph, tokens, branch.block(0), node, Pin.TRUE, x + 190, depth + 1);
                node.hasElse = branch.block()
                    .size() == 2;
                if (node.hasElse) importBlock(graph, tokens, branch.block(1), node, Pin.FALSE, x + 380, depth + 1);
            }
            previous = node;
            pin = type.continuation();
        }
    }

    /** Place complete branches in separate lanes, then their continuation below both branches. */
    private static void layout(BlueprintGraph graph) {
        double x = 20;
        for (Node trigger : graph.triggers()) {
            trigger.setPosition(x, 20);
            double[] extent = layoutChain(graph, graph.next(trigger.id, Pin.NEXT), x, 100);
            x = Math.max(x + 210, extent[0] + 210);
        }
    }

    private static double[] layoutChain(BlueprintGraph graph, Node node, double x, double y) {
        double maxX = x;
        while (node != null) {
            node.setPosition(x, y);
            y += node.type == Type.IF ? 120 : 80;
            if (node.type == Type.IF) {
                double[] yes = layoutChain(graph, graph.next(node.id, Pin.TRUE), x + 190, y);
                double[] no = layoutChain(graph, graph.next(node.id, Pin.FALSE), yes[0] + 190, y);
                maxX = Math.max(maxX, Math.max(yes[0], no[0]));
                y = Math.max(yes[1], no[1]) + 20;
            }
            node = graph.next(node.id, node.type.continuation());
        }
        return new double[] { maxX, y };
    }

    private static SFMLParser.BlockContext block(SFMLParser.TriggerContext trigger) {
        return trigger instanceof SFMLParser.TimerTriggerContext ? ((SFMLParser.TimerTriggerContext) trigger).block()
            : ((SFMLParser.PulseTriggerContext) trigger).block();
    }

    private static Type typeOf(SFMLParser.StatementContext statement) {
        if (statement.inputStatement() != null) return Type.INPUT;
        if (statement.outputStatement() != null) return Type.OUTPUT;
        if (statement.redstoneOutputStatement() != null) return Type.REDSTONE;
        if (statement.forgetStatement() != null) return Type.FORGET;
        if (statement.ifStatement() != null) return Type.IF;
        throw new IllegalArgumentException("Unsupported statement; use the text editor");
    }

    private static SFMLParser.ProgramContext parse(String code) {
        ProgramBuildResult result = new ProgramBuilder(code).buildWithSyntax();
        if (!result.isSuccess()) throw new IllegalArgumentException(result.getErrorMessage());
        VisualCapabilityReport report = VisualCapabilityAnalyzer.analyze(result);
        if (!report.isAvailable()) throw new ImportException(report);
        return result.getSyntax();
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
        List<Token> left = tokens(a).getTokens(), right = tokens(b).getTokens();
        int i = 0, j = 0;
        while (true) {
            while (left.get(i)
                .getChannel() != Token.DEFAULT_CHANNEL) i++;
            while (right.get(j)
                .getChannel() != Token.DEFAULT_CHANNEL) j++;
            Token x = left.get(i++), y = right.get(j++);
            if (x.getType() != y.getType()) return false;
            if (x.getType() == Token.EOF) return true;
            if ((x.getType() == SFMLLexer.IDENTIFIER || x.getType() == SFMLLexer.STRING
                || x.getType() == SFMLLexer.NUMBER
                || x.getType() == SFMLLexer.NUMBER_WITH_G_SUFFIX)
                && !x.getText()
                    .equals(y.getText()))
                return false;
        }
    }
}
