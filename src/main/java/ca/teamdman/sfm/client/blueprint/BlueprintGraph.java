/*
 * Adapted from RetroSFM, Copyright (c) 2026 Saereth, MIT License.
 * Source: breakinblocks/RetroSFM b6776c456e87ace2ada5e5464fea9e4367d74bad.
 * See META-INF/licenses/RetroSFM-MIT.txt for the full license.
 */
package ca.teamdman.sfm.client.blueprint;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Editor-only control flow. The server executes generated SFML, never this graph. */
public final class BlueprintGraph {

    public static final int MAX_NODES = 256;
    public static final int MAX_CLAUSE = 4096;
    public static final int MAX_DEPTH = 32;

    public enum Pin {

        NEXT,
        TRUE,
        FALSE,
        AFTER;

        public String label() {
            return this == NEXT ? "Next" : name();
        }
    }

    public enum Type {

        TIMER("Timer", "EVERY 20 TICKS", 0x527b9d),
        PULSE("Pulse", "EVERY REDSTONE PULSE", 0x9b5656),
        INPUT("Input", "INPUT FROM source", 0x568760),
        OUTPUT("Output", "OUTPUT TO destination", 0x986f46),
        IF("If", "source HAS >= 1", 0x8b719d),
        FORGET("Forget", "FORGET", 0x737b82),
        REDSTONE("Redstone output", "OUTPUT 15 REDSTONE LEVEL TO target", 0x9b5656);

        public final String title;
        public final String defaultClause;
        public final int color;

        Type(String title, String defaultClause, int color) {
            this.title = title;
            this.defaultClause = defaultClause;
            this.color = color;
        }

        public boolean isTrigger() {
            return this == TIMER || this == PULSE;
        }

        public Pin continuation() {
            return this == IF ? Pin.AFTER : Pin.NEXT;
        }

        public boolean hasPin(Pin pin) {
            return this == IF ? pin != Pin.NEXT : pin == Pin.NEXT;
        }
    }

    public static final class Node {

        public final UUID id;
        public final Type type;
        private double x, y;
        private String clause;
        public boolean hasElse;

        public Node(UUID id, Type type, double x, double y, String clause) {
            if (id == null || type == null) throw new IllegalArgumentException("Missing node identity");
            this.id = id;
            this.type = type;
            setPosition(x, y);
            setClause(clause);
        }

        public Node(Type type, double x, double y) {
            this(UUID.randomUUID(), type, x, y, type.defaultClause);
        }

        public double x() {
            return x;
        }

        public double y() {
            return y;
        }

        public String clause() {
            return clause;
        }

        public void setPosition(double x, double y) {
            if (!Double.isFinite(x) || !Double.isFinite(y) || Math.abs(x) > 1000000 || Math.abs(y) > 1000000)
                throw new IllegalArgumentException("Invalid node position");
            this.x = x;
            this.y = y;
        }

        public void setClause(String clause) {
            if (clause == null || clause.length() > MAX_CLAUSE)
                throw new IllegalArgumentException("Node clause exceeds 4096 characters");
            this.clause = clause;
        }
    }

    public static final class Edge {

        public final UUID from, to;
        public final Pin pin;

        Edge(UUID from, Pin pin, UUID to) {
            this.from = from;
            this.pin = pin;
            this.to = to;
        }
    }

    private final Map<UUID, Node> nodes = new LinkedHashMap<>();
    // Index connections once per edit; drawing and generation do not repeatedly scan every edge.
    private final Map<UUID, EnumMap<Pin, Edge>> outgoing = new LinkedHashMap<>();
    private final Map<UUID, Edge> incoming = new LinkedHashMap<>();
    private String nameClause = "";
    private String comments = "";

    public Collection<Node> nodes() {
        return Collections.unmodifiableCollection(nodes.values());
    }

    public Collection<Edge> edges() {
        return Collections.unmodifiableCollection(incoming.values());
    }

    public Node node(UUID id) {
        return nodes.get(id);
    }

    public String nameClause() {
        return nameClause;
    }

    public void setNameClause(String value) {
        if (value == null || value.length() > MAX_CLAUSE) throw new IllegalArgumentException("Program name too long");
        nameClause = value;
    }

    public String comments() {
        return comments;
    }

    public void setComments(String value) {
        if (value == null || value.length() > 32767) throw new IllegalArgumentException("Comments too long");
        comments = value;
    }

    public void add(Node node) {
        if (nodes.containsKey(node.id)) throw new IllegalArgumentException("Duplicate node");
        if (nodes.size() >= MAX_NODES) throw new IllegalArgumentException("Blueprint exceeds 256 nodes");
        nodes.put(node.id, node);
        outgoing.put(node.id, new EnumMap<Pin, Edge>(Pin.class));
    }

    public Node next(UUID id, Pin pin) {
        Map<Pin, Edge> edges = outgoing.get(id);
        Edge edge = edges == null ? null : edges.get(pin);
        return edge == null ? null : node(edge.to);
    }

    public void disconnect(UUID from, Pin pin) {
        Map<Pin, Edge> edges = outgoing.get(from);
        Edge edge = edges == null ? null : edges.remove(pin);
        if (edge != null) incoming.remove(edge.to);
    }

    public void disconnectInput(UUID to) {
        Edge edge = incoming.get(to);
        if (edge != null) disconnect(edge.from, edge.pin);
    }

    public void connect(UUID from, Pin pin, UUID to) {
        Node source = node(from), target = node(to);
        if (source == null || target == null || pin == null) throw new IllegalArgumentException("Missing node or pin");
        if (!source.type.hasPin(pin) || target.type.isTrigger())
            throw new IllegalArgumentException("Invalid connection");
        if (from.equals(to) || reaches(to, from)) throw new IllegalArgumentException("Connections cannot form a loop");
        // Replacement is intentional, but only after validation: a failed connection preserves the old wires.
        disconnect(from, pin);
        disconnectInput(to);
        Edge edge = new Edge(from, pin, to);
        outgoing.get(from)
            .put(pin, edge);
        incoming.put(to, edge);
    }

    private boolean reaches(UUID start, UUID target) {
        Set<UUID> visited = new HashSet<>();
        Deque<UUID> pending = new ArrayDeque<>();
        pending.push(start);
        while (!pending.isEmpty()) {
            UUID id = pending.pop();
            if (id.equals(target)) return true;
            if (visited.add(id)) for (Edge edge : outgoing.get(id)
                .values()) pending.push(edge.to);
        }
        return false;
    }

    public void remove(UUID id) {
        if (!nodes.containsKey(id)) return;
        disconnectInput(id);
        for (Pin pin : Pin.values()) disconnect(id, pin);
        nodes.remove(id);
        outgoing.remove(id);
    }

    public List<Node> triggers() {
        List<Node> result = new ArrayList<>();
        for (Node node : nodes.values()) if (node.type.isTrigger()) result.add(node);
        return result;
    }

    public BlueprintGraph copy() {
        BlueprintGraph copy = new BlueprintGraph();
        copy.nameClause = nameClause;
        copy.comments = comments;
        for (Node node : nodes.values()) {
            Node other = new Node(node.id, node.type, node.x, node.y, node.clause);
            other.hasElse = node.hasElse;
            copy.add(other);
        }
        for (Edge edge : edges()) copy.connect(edge.from, edge.pin, edge.to);
        return copy;
    }
}
