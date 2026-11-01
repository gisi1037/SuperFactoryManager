package ca.teamdman.sfm.client.blueprint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.antlr.v4.runtime.Token;

import ca.teamdman.langs.SFMLParser;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Edge;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Pin;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Type;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;
import ca.teamdman.sfml.program_builder.VisualCapabilityAnalyzer;
import ca.teamdman.sfml.program_builder.VisualCapabilityReport;

/** Explicit, bounded navigation snapshot. Matches syntax roles to current UUIDs, never searches clause text. */
public final class StudioSourceMap {

    public static final class Span {

        public final UUID node;
        public final int sourceStart, sourceEnd, displayStart, displayEnd;

        private Span(UUID node, int start, int end, int[] displayOffsets) {
            this.node = node;
            sourceStart = start;
            sourceEnd = end;
            displayStart = displayOffsets[start];
            displayEnd = displayOffsets[end];
        }
    }

    private static final class Hit {

        final int start, end;
        final Span span;

        Hit(int start, int end, Span span) {
            this.start = start;
            this.end = end;
            this.span = span;
        }
    }

    private static final class Identity {

        final Node instance;
        final String clause;
        final boolean hasElse;

        Identity(Node node) {
            instance = node;
            clause = node.clause();
            hasElse = node.hasElse;
        }
    }

    private final UUID project;
    private final String source, display;
    private final BlueprintGraph owner;
    private final String graphName, graphComments;
    private final int[] codePointOffsets;
    private final Map<UUID, Identity> identities = new LinkedHashMap<>();
    private final List<String> edges = new ArrayList<>();
    private final List<UUID> triggers = new ArrayList<>();
    private final Map<UUID, Span> spans = new LinkedHashMap<>();
    private final List<Hit> hits = new ArrayList<>();

    /** Only explicit requests call this. Refusal precedes lazy graph creation and never applies pending edits. */
    public static StudioSourceMap prepare(UUID project, StudioDocument document, String display, boolean waiting,
        boolean pendingEdits, boolean interaction) {
        if (waiting) throw new IllegalArgumentException("Wait for the current project request before navigating.");
        if (pendingEdits) throw new IllegalArgumentException(
            "Apply or Cancel Fields / Apply or Reset the raw clause before navigating.");
        if (interaction)
            throw new IllegalArgumentException("Finish the canvas gesture or cancel its wire before navigating.");
        if (document.incomplete()) throw new IllegalArgumentException("Connect every node or Undo before navigating.");
        if (!normalize(document.code()).equals(display))
            throw new IllegalArgumentException("Editor and source changed. Retry navigation on the current draft.");
        document.openVisual(); // Complete import is atomic; unsupported source never exposes a partial graph.
        return build(project, document.code(), display, document.graph());
    }

    public static StudioSourceMap build(UUID project, String source, String display, BlueprintGraph graph) {
        if (project == null || source == null
            || source.length() > 32767
            || graph == null
            || !normalize(source).equals(display))
            throw new IllegalArgumentException("No exact current source/graph to navigate.");
        ProgramBuildResult built = new ProgramBuilder(source).buildWithSyntax();
        VisualCapabilityReport report = VisualCapabilityAnalyzer.analyze(built);
        if (!report.isAvailable()) throw new IllegalArgumentException("Navigation unavailable: " + report.getMessage());
        if (!BlueprintCompiler.sameTokens(source, BlueprintCompiler.navigationProjection(graph)))
            throw new IllegalArgumentException("Source and graph structure differ. Reopen the current Visual draft.");
        StudioSourceMap result = new StudioSourceMap(project, source, display, graph);
        List<org.antlr.v4.runtime.ParserRuleContext> rootsSyntax = new ArrayList<>();
        for (org.antlr.v4.runtime.tree.ParseTree child : built.getSyntax().children)
            if (child instanceof SFMLParser.TriggerContext || child instanceof SFMLParser.StateDeclarationContext
                || child instanceof SFMLParser.DataDeclarationContext)
                rootsSyntax.add((org.antlr.v4.runtime.ParserRuleContext) child);
        List<Node> roots = graph.roots();
        if (rootsSyntax.size() != roots.size()) throw mismatch();
        Set<UUID> visited = new HashSet<>();
        int[] offsets = displayOffsets(source);
        for (int i = 0; i < rootsSyntax.size(); i++) {
            Node root = roots.get(i);
            org.antlr.v4.runtime.ParserRuleContext syntax = rootsSyntax.get(i);
            if (!(syntax instanceof SFMLParser.TriggerContext)) {
                Type expected = syntax instanceof SFMLParser.StateDeclarationContext ? Type.STATE : Type.GLOBAL_LET;
                if (root.type != expected) throw mismatch();
                result.bind(root, syntax.getStart(), syntax.getStop(), built, offsets, visited);
                continue;
            }
            SFMLParser.TriggerContext trigger = (SFMLParser.TriggerContext) syntax;
            if (root.type != BlueprintCompiler.triggerType(trigger)) throw mismatch();
            Token end = BlueprintCompiler.triggerDo(trigger);
            result.bind(root, trigger.getStart(), end, built, offsets, visited);
            SFMLParser.BlockContext block = BlueprintCompiler.triggerBlock(trigger);
            result.chain(block, graph.next(root.id, Pin.NEXT), built, offsets, visited, 0);
        }
        if (visited.size() != graph.nodes()
            .size()) throw mismatch();
        result.hits.sort((a, b) -> Integer.compare(a.start, b.start));
        return result;
    }

    private StudioSourceMap(UUID project, String source, String display, BlueprintGraph graph) {
        this.project = project;
        this.source = source;
        this.display = display;
        owner = graph;
        graphName = graph.nameClause();
        graphComments = graph.comments();
        codePointOffsets = new int[source.codePointCount(0, source.length()) + 1];
        int point = 0;
        for (int i = 0; i < source.length();) {
            codePointOffsets[point++] = i;
            i += Character.charCount(source.codePointAt(i));
        }
        codePointOffsets[point] = source.length();
        for (Node node : graph.nodes()) identities.put(node.id, new Identity(node));
        edges.addAll(edgeKeys(graph));
        for (Node trigger : graph.roots()) triggers.add(trigger.id);
    }

    private void chain(SFMLParser.BlockContext block, Node node, ProgramBuildResult built, int[] offsets,
        Set<UUID> visited, int depth) {
        if (depth > BlueprintGraph.MAX_DEPTH) throw mismatch();
        for (SFMLParser.StatementContext statement : block.statement()) {
            if (node == null || node.type != type(statement)) throw mismatch();
            SFMLParser.IfStatementContext branch = statement.ifStatement();
            if (node.type.isLoop()) {
                bind(node, statement.getStart(), BlueprintCompiler.loopDo(statement), built, offsets, visited);
                chain(
                    BlueprintCompiler.loopBlock(statement),
                    owner.next(node.id, Pin.BODY),
                    built,
                    offsets,
                    visited,
                    depth + 1);
            } else if (branch == null) bind(node, statement.getStart(), statement.getStop(), built, offsets, visited);
            else {
                if (branch.boolexpr()
                    .size() != 1 || (node.hasElse || owner.next(node.id, Pin.FALSE) != null)
                        != (branch.block()
                            .size() == 2))
                    throw mismatch();
                bind(
                    node,
                    statement.getStart(),
                    (branch.THEN()
                        .isEmpty() ? branch.LBRACE(0) : branch.THEN(0)).getSymbol(),
                    built,
                    offsets,
                    visited);
                chain(branch.block(0), owner.next(node.id, Pin.TRUE), built, offsets, visited, depth + 1);
                if (branch.block()
                    .size() == 2)
                    chain(branch.block(1), owner.next(node.id, Pin.FALSE), built, offsets, visited, depth + 1);
            }
            node = owner.next(node.id, node.type.continuation());
        }
        if (node != null) throw mismatch();
    }

    private void bind(Node node, Token first, Token last, ProgramBuildResult built, int[] offsets, Set<UUID> visited) {
        if (!visited.add(node.id)) throw mismatch();
        int start = codePointOffsets[first.getStartIndex()], end = codePointOffsets[last.getStopIndex() + 1];
        Span span = new Span(node.id, start, end, offsets);
        spans.put(node.id, span);
        // Match tokens only: indentation, comments, NAME, ELSE and END never guess an adjacent node.
        for (int i = first.getTokenIndex(); i <= last.getTokenIndex(); i++) {
            Token token = built.getTokens()
                .get(i);
            if (token.getChannel() != Token.DEFAULT_CHANNEL || token.getType() == Token.EOF) continue;
            int a = codePointOffsets[token.getStartIndex()], b = codePointOffsets[token.getStopIndex() + 1];
            hits.add(new Hit(offsets[a], offsets[b], span));
        }
    }

    public boolean matches(UUID currentProject, String currentSource, String currentDisplay, BlueprintGraph current) {
        if (!project.equals(currentProject) || owner != current
            || !source.equals(currentSource)
            || !display.equals(currentDisplay)
            || !graphName.equals(current.nameClause())
            || !graphComments.equals(current.comments())
            || current.nodes()
                .size() != identities.size()
            || !edges.equals(edgeKeys(current))) return false;
        List<UUID> orderedTriggers = new ArrayList<>();
        for (Node trigger : current.roots()) orderedTriggers.add(trigger.id);
        if (!triggers.equals(orderedTriggers)) return false;
        for (Node node : current.nodes()) {
            Identity prior = identities.get(node.id);
            if (prior == null || prior.instance != node
                || !prior.clause.equals(node.clause())
                || prior.hasElse != node.hasElse) return false;
        }
        return true;
    }

    public void requireCurrent(UUID project, String source, String display, BlueprintGraph graph) {
        if (!matches(project, source, display, graph)) throw new IllegalArgumentException(
            "Navigation target changed. Reveal again from the current project/source.");
    }

    public Span node(UUID id) {
        return spans.get(id);
    }

    /** Half-open token spans. A caret splitting an astral character, outside source, or on trivia has no target. */
    public Span atCaret(int caret) {
        if (caret < 0 || caret >= display.length()
            || caret > 0 && Character.isHighSurrogate(display.charAt(caret - 1))
                && Character.isLowSurrogate(display.charAt(caret)))
            return null;
        int low = 0, high = hits.size() - 1;
        while (low <= high) {
            int middle = (low + high) >>> 1;
            Hit hit = hits.get(middle);
            if (caret < hit.start) high = middle - 1;
            else if (caret >= hit.end) low = middle + 1;
            else return hit.span;
        }
        return null;
    }

    private static List<String> edgeKeys(BlueprintGraph graph) {
        List<String> result = new ArrayList<>();
        for (Edge edge : graph.edges()) result.add(edge.from + ":" + edge.pin + ":" + edge.to);
        Collections.sort(result);
        return result;
    }

    private static Type type(SFMLParser.StatementContext statement) {
        return BlueprintCompiler.typeOf(statement);
    }

    private static IllegalArgumentException mismatch() {
        return new IllegalArgumentException("No complete current source/graph mapping.");
    }

    public static String normalize(String source) {
        return source.replace("\r\n", "\n")
            .replace('\r', '\n');
    }

    private static int[] displayOffsets(String source) {
        int[] offsets = new int[source.length() + 1];
        int display = 0;
        for (int i = 0; i < source.length(); i++) {
            offsets[i] = display++;
            if (source.charAt(i) == '\r' && i + 1 < source.length() && source.charAt(i + 1) == '\n')
                offsets[++i] = display;
        }
        offsets[source.length()] = display;
        return offsets;
    }
}
