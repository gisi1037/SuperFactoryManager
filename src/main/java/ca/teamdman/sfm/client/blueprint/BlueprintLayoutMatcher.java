package ca.teamdman.sfm.client.blueprint;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

import ca.teamdman.langs.SFMLLexer;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Edge;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Pin;

/**
 * Best-effort editor identity only. Callers must validate both graphs separately; this never establishes source
 * equivalence, save authority or runtime identity. Output clauses, branch roles and ordering always come from current.
 */
public final class BlueprintLayoutMatcher {

    private BlueprintLayoutMatcher() {}

    public static Result match(BlueprintGraph previous, BlueprintGraph current) {
        Snapshot old = new Snapshot(previous), next = new Snapshot(current);
        Matching matching = new Matching(old, next);
        matching.sequence(previous.triggers(), current.triggers());
        return matching.rebuild();
    }

    /**
     * For a caller's independently verified unchanged source. Ordered structure and exact clause tokens are checked
     * again here; mismatches use conservative matching instead of assigning positional identities.
     */
    public static Result matchExact(BlueprintGraph previous, BlueprintGraph current) {
        Snapshot old = new Snapshot(previous), next = new Snapshot(current);
        Matching matching = new Matching(old, next);
        if (!matching.exactSequence(previous.triggers(), current.triggers())) {
            matching.matches.clear();
            matching.usedOld.clear();
            matching.sequence(previous.triggers(), current.triggers());
        }
        return matching.rebuild();
    }

    public static final class Result {

        public final BlueprintGraph graph;
        /** Original current IDs mapped to retained previous IDs; never a source or revision token. */
        public final Map<UUID, UUID> retainedIds;
        public final int reusedNodes, freshNodes, ambiguousNodes;

        private Result(BlueprintGraph graph, Map<UUID, UUID> retainedIds, int ambiguousNodes) {
            this.graph = graph;
            this.retainedIds = Collections.unmodifiableMap(new LinkedHashMap<>(retainedIds));
            reusedNodes = retainedIds.size();
            freshNodes = graph.nodes()
                .size() - reusedNodes;
            this.ambiguousNodes = ambiguousNodes;
        }
    }

    private static final class Keys {

        final String exact, skeleton;
        String tree;

        Keys(Node node) {
            StringBuilder exact = new StringBuilder(node.type.name()), skeleton = new StringBuilder(node.type.name());
            SFMLLexer lexer = new SFMLLexer(CharStreams.fromString(node.clause()));
            lexer.removeErrorListeners();
            lexer.addErrorListener(new BaseErrorListener() {

                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int column,
                    String message, RecognitionException error) {
                    throw new IllegalArgumentException("Layout candidate has invalid clause tokens");
                }
            });
            for (Token token : lexer.getAllTokens()) {
                if (token.getChannel() != Token.DEFAULT_CHANNEL) continue;
                int type = token.getType();
                boolean atom = type == SFMLLexer.IDENTIFIER || type == SFMLLexer.STRING;
                boolean number = type == SFMLLexer.NUMBER || type == SFMLLexer.NUMBER_WITH_G_SUFFIX;
                boolean bool = type == SFMLLexer.TRUE || type == SFMLLexer.FALSE;
                exact.append('|')
                    .append(type);
                if (atom || number) exact.append(':')
                    .append(
                        token.getText()
                            .length())
                    .append(':')
                    .append(token.getText());
                skeleton.append('|')
                    .append(atom ? "atom" : number ? "number" : bool ? "bool" : Integer.toString(type));
            }
            // Empty ELSE is explicit visual control-flow metadata and is retained only within its own role.
            this.exact = exact.append(node.hasElse ? "|else" : "|noelse")
                .toString();
            this.skeleton = skeleton.toString();
        }
    }

    private static final class Snapshot {

        final BlueprintGraph graph;
        final Map<UUID, Keys> keys = new HashMap<>();

        Snapshot(BlueprintGraph graph) {
            if (graph == null || graph.nodes()
                .size() > BlueprintGraph.MAX_NODES)
                throw new IllegalArgumentException("Missing or oversized layout graph");
            this.graph = graph;
            Set<UUID> seen = new HashSet<>();
            for (Node trigger : graph.triggers()) {
                if (!seen.add(trigger.id)) throw new IllegalArgumentException("Repeated trigger");
                validateChain(graph.next(trigger.id, Pin.NEXT), 1, seen);
            }
            if (seen.size() != graph.nodes()
                .size()) throw new IllegalArgumentException("Layout graph contains disconnected nodes");
            for (Node node : graph.nodes()) keys.put(node.id, new Keys(node));
        }

        private void validateChain(Node first, int depth, Set<UUID> seen) {
            if (first != null && depth > BlueprintGraph.MAX_DEPTH)
                throw new IllegalArgumentException("Layout graph exceeds nesting limit");
            for (Node node = first; node != null; node = graph.next(node.id, node.type.continuation())) {
                if (node.type.isTrigger() || !seen.add(node.id))
                    throw new IllegalArgumentException("Invalid or repeated control-flow node");
                if (node.type == BlueprintGraph.Type.IF) {
                    validateChain(graph.next(node.id, Pin.TRUE), depth + 1, seen);
                    validateChain(graph.next(node.id, Pin.FALSE), depth + 1, seen);
                }
            }
        }

        List<Node> chain(Node first) {
            List<Node> nodes = new ArrayList<>();
            for (Node node = first; node != null; node = graph.next(node.id, node.type.continuation())) nodes.add(node);
            return nodes;
        }

        String key(Node node, int phase) {
            Keys key = keys.get(node.id);
            if (phase == 1) return key.exact;
            if (phase == 2) return key.skeleton;
            if (key.tree == null) {
                MessageDigest digest = digest();
                update(digest, key.exact);
                if (node.type.isTrigger()) updateChain(digest, graph.next(node.id, Pin.NEXT));
                if (node.type == BlueprintGraph.Type.IF) {
                    update(digest, "true");
                    updateChain(digest, graph.next(node.id, Pin.TRUE));
                    update(digest, "false");
                    updateChain(digest, graph.next(node.id, Pin.FALSE));
                }
                StringBuilder hex = new StringBuilder();
                for (byte value : digest.digest()) hex.append(Character.forDigit((value >>> 4) & 15, 16))
                    .append(Character.forDigit(value & 15, 16));
                key.tree = hex.toString();
            }
            return key.tree;
        }

        private void updateChain(MessageDigest digest, Node first) {
            update(digest, "[");
            for (Node node : chain(first)) update(digest, key(node, 0));
            update(digest, "]");
        }
    }

    private static final class Matching {

        final Snapshot old, next;
        final Map<UUID, UUID> matches = new LinkedHashMap<>();
        final Set<UUID> usedOld = new HashSet<>(), ambiguous = new HashSet<>();

        Matching(Snapshot old, Snapshot next) {
            this.old = old;
            this.next = next;
        }

        boolean exactSequence(List<Node> before, List<Node> after) {
            if (before.size() != after.size()) return false;
            for (int i = 0; i < before.size(); i++) {
                Node previous = before.get(i), current = after.get(i);
                if (!old.key(previous, 1)
                    .equals(next.key(current, 1))) return false;
                matches.put(current.id, previous.id);
                usedOld.add(previous.id);
                if (current.type.isTrigger() && !exactSequence(
                    old.chain(old.graph.next(previous.id, Pin.NEXT)),
                    next.chain(next.graph.next(current.id, Pin.NEXT)))) return false;
                if (current.type == BlueprintGraph.Type.IF) {
                    if (!exactSequence(
                        old.chain(old.graph.next(previous.id, Pin.TRUE)),
                        next.chain(next.graph.next(current.id, Pin.TRUE)))) return false;
                    if (!exactSequence(
                        old.chain(old.graph.next(previous.id, Pin.FALSE)),
                        next.chain(next.graph.next(current.id, Pin.FALSE)))) return false;
                }
            }
            return true;
        }

        void sequence(List<Node> before, List<Node> after) {
            for (int phase = 0; phase < 3; phase++) {
                Map<String, List<Node>> left = groups(old, before, phase, usedOld);
                Map<String, List<Node>> right = groups(next, after, phase, matches.keySet());
                for (Node node : after) {
                    if (matches.containsKey(node.id)) continue;
                    String key = next.key(node, phase);
                    List<Node> candidates = left.get(key), alternatives = right.get(key);
                    if (candidates != null && candidates.size() == 1 && alternatives.size() == 1) {
                        matches.put(node.id, candidates.get(0).id);
                        usedOld.add(candidates.get(0).id);
                    }
                }
            }
            Map<String, List<Node>> left = groups(old, before, 2, usedOld);
            Map<String, List<Node>> right = groups(next, after, 2, matches.keySet());
            for (Node node : after) {
                UUID previousId = matches.get(node.id);
                if (previousId == null) {
                    List<Node> candidates = left.get(next.key(node, 2));
                    if (candidates != null && (candidates.size() > 1 || right.get(next.key(node, 2))
                        .size() > 1)) ambiguous.add(node.id);
                    continue;
                }
                Node previous = old.graph.node(previousId);
                if (node.type.isTrigger()) sequence(
                    old.chain(old.graph.next(previous.id, Pin.NEXT)),
                    next.chain(next.graph.next(node.id, Pin.NEXT)));
                if (node.type == BlueprintGraph.Type.IF) {
                    sequence(
                        old.chain(old.graph.next(previous.id, Pin.TRUE)),
                        next.chain(next.graph.next(node.id, Pin.TRUE)));
                    sequence(
                        old.chain(old.graph.next(previous.id, Pin.FALSE)),
                        next.chain(next.graph.next(node.id, Pin.FALSE)));
                }
            }
        }

        Result rebuild() {
            BlueprintGraph result = new BlueprintGraph();
            result.setNameClause(next.graph.nameClause());
            result.setComments(next.graph.comments());
            Set<UUID> reserved = new HashSet<>(), output = new HashSet<>(), previousIds = new HashSet<>();
            for (Node node : old.graph.nodes()) {
                reserved.add(node.id);
                previousIds.add(node.id);
            }
            for (Node node : next.graph.nodes()) reserved.add(node.id);
            Map<UUID, UUID> ids = new HashMap<>();
            // Reserve reused IDs first, so an unmatched caller-supplied UUID cannot shadow a retained identity.
            output.addAll(matches.values());
            for (Node node : next.graph.nodes()) {
                UUID previousId = matches.get(node.id), id = previousId == null ? node.id : previousId;
                if (previousId == null && (previousIds.contains(id) || !output.add(id))) {
                    int attempt = 0;
                    do {
                        // At most 3*MAX_NODES IDs are reserved/output. These candidates have distinct low bits,
                        // so one of the first 3*MAX_NODES+1 candidates must be free, even for adversarial input.
                        if (attempt > BlueprintGraph.MAX_NODES * 3)
                            throw new IllegalStateException("No fresh layout ID");
                        id = new UUID(
                            node.id.getMostSignificantBits() ^ 0x53464d4c41594f55L,
                            node.id.getLeastSignificantBits() + attempt++);
                    } while (reserved.contains(id) || !output.add(id));
                }
                ids.put(node.id, id);
                Node previous = previousId == null ? null : old.graph.node(previousId);
                Node placed = new Node(
                    id,
                    node.type,
                    previous == null ? node.x() : previous.x(),
                    previous == null ? node.y() : previous.y(),
                    node.clause());
                placed.hasElse = node.hasElse;
                result.add(placed);
            }
            for (Edge edge : next.graph.edges()) result.connect(ids.get(edge.from), edge.pin, ids.get(edge.to));
            return new Result(result, matches, ambiguous.size());
        }
    }

    private static Map<String, List<Node>> groups(Snapshot graph, List<Node> nodes, int phase, Set<UUID> excluded) {
        Map<String, List<Node>> groups = new HashMap<>();
        for (Node node : nodes)
            if (!excluded.contains(node.id)) groups.computeIfAbsent(graph.key(node, phase), key -> new ArrayList<>())
                .add(node);
        return groups;
    }

    private static MessageDigest digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    private static void update(MessageDigest digest, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        digest.update((byte) (bytes.length >>> 24));
        digest.update((byte) (bytes.length >>> 16));
        digest.update((byte) (bytes.length >>> 8));
        digest.update((byte) bytes.length);
        digest.update(bytes);
    }
}
