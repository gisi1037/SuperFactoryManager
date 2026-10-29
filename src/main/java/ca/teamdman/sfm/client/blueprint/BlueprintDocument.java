/*
 * Adapted from RetroSFM, Copyright (c) 2026 Saereth, MIT License.
 * Source: breakinblocks/RetroSFM b6776c456e87ace2ada5e5464fea9e4367d74bad.
 * See META-INF/licenses/RetroSFM-MIT.txt for the full license.
 */
package ca.teamdman.sfm.client.blueprint;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;

import ca.teamdman.langs.SFMLLexer;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Edge;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Pin;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Type;
import ca.teamdman.sfm.common.program.ProgramDocument;
import ca.teamdman.sfm.common.util.ProgramRevision;

/** Bounded legacy-disk layout sidecar. Current source always supplies instructions and control flow. */
public final class BlueprintDocument {

    public static final String MARKER = "-- sfm:blueprint:v2 ";
    public static final String LEGACY_MARKER = "-- sfm:blueprint:v1 ";
    public static final int MAX_BYTES = ProgramDocument.MAX_SOURCE_BYTES;
    private static final int MAX_LAYOUT_BYTES = 262144;
    public final BlueprintGraph graph;
    public final String notice;
    /** True only for an exact source/layout restore; stale-source matches are reported separately. */
    public final boolean restoredLayout;
    public final int matchedNodes;
    private final String plainSource;
    private final String baselineGenerated;

    private BlueprintDocument(BlueprintGraph graph, String notice, boolean restoredLayout, int matchedNodes,
        String plainSource, String baselineGenerated) {
        this.graph = graph;
        this.notice = notice;
        this.restoredLayout = restoredLayout;
        this.matchedNodes = matchedNodes;
        this.plainSource = plainSource;
        this.baselineGenerated = baselineGenerated;
    }

    public static String save(BlueprintGraph graph) {
        String generated = BlueprintCompiler.generate(graph);
        return encode(graph, generated, generated);
    }

    /** Layout-only changes retain source formatting and comments, adding a separator newline only if needed. */
    public String saveEdited(BlueprintGraph edited) {
        String generated = BlueprintCompiler.generate(edited);
        return encode(edited, generated.equals(baselineGenerated) ? plainSource : generated, generated);
    }

    private static String encode(BlueprintGraph graph, String source, String generated) {
        // One required separator at most. Repeated saves never accumulate blank lines.
        String body = source.endsWith("\n") ? source : source + "\n";
        try {
            ByteArrayOutputStream raw = new ByteArrayOutputStream();
            try (DataOutputStream data = new DataOutputStream(raw)) {
                data.writeInt(2);
                data.write(ProgramRevision.digest(body));
                data.write(ProgramRevision.digest(generated));
                data.writeUTF(graph.nameClause());
                data.writeUTF(graph.comments());
                data.writeInt(
                    graph.nodes()
                        .size());
                for (Node node : graph.nodes()) {
                    uuid(data, node.id);
                    data.writeUTF(node.type.name());
                    data.writeDouble(node.x());
                    data.writeDouble(node.y());
                    data.writeUTF(node.clause());
                    data.writeBoolean(node.hasElse);
                }
                data.writeInt(
                    graph.edges()
                        .size());
                for (Edge edge : graph.edges()) {
                    uuid(data, edge.from);
                    data.writeUTF(edge.pin.name());
                    uuid(data, edge.to);
                }
            }
            if (raw.size() > MAX_LAYOUT_BYTES) throw new IllegalArgumentException("Blueprint layout is too large");
            ByteArrayOutputStream compressed = new ByteArrayOutputStream();
            try (GZIPOutputStream zip = new GZIPOutputStream(compressed)) {
                raw.writeTo(zip);
            }
            String result = body + MARKER
                + Base64.getEncoder()
                    .encodeToString(compressed.toByteArray());
            ProgramDocument.validateSource(result);
            return result;
        } catch (IOException error) {
            throw new IllegalArgumentException("Cannot encode blueprint", error);
        }
    }

    public static BlueprintDocument open(String source) {
        ProgramDocument.validateSource(source);
        Metadata metadata = scan(source);
        // Parse and classify the current text before reading any old graph data.
        BlueprintGraph current = BlueprintCompiler.importText(metadata.plain);
        String generated = BlueprintCompiler.generate(current);
        if (metadata.tokens.size() == 1) {
            try {
                Layout previous = decode(metadata.tokens.get(0));
                boolean exact = ProgramRevision
                    .matches(previous.sourceHash, previous.version == 1 ? metadata.plain.trim() : metadata.plain)
                    && previous.generated.equals(generated);
                // Unchanged ordered clauses can retain duplicate-node positions after spacing/comment edits.
                // matchExact checks all clause tokens and branch roles itself, then falls back conservatively.
                BlueprintLayoutMatcher.Result matched = BlueprintLayoutMatcher.matchExact(previous.graph, current);
                return new BlueprintDocument(
                    matched.graph,
                    exact ? "Layout restored"
                        : "Layout stale; kept " + matched.reusedNodes + " node positions; reset " + matched.freshNodes,
                    exact,
                    matched.reusedNodes,
                    metadata.plain,
                    generated);
            } catch (IOException | IllegalArgumentException error) {
                // A malformed layout never replaces even a single instruction from current source.
            }
        }
        return new BlueprintDocument(
            current,
            metadata.tokens.isEmpty() ? "Imported SFML; comments kept in the source"
                : "Layout stale or invalid; rebuilt from current SFML",
            false,
            0,
            metadata.plain,
            generated);
    }

    public static final class TextSave {

        public final String source, notice;

        private TextSave(String source, String notice) {
            this.source = source;
            this.notice = notice;
        }
    }

    /** Carry a validated old layout as a matching candidate, never as executable replacement source. */
    public static TextSave prepareTextSave(String draft, String authoritativeSource) {
        ProgramDocument.validateSource(draft);
        // Explicitly pasted metadata is left intact; open() applies all bounds and source checks later.
        if (!scan(draft).tokens.isEmpty()) return new TextSave(draft, "");
        Metadata previous = scan(authoritativeSource);
        if (previous.tokens.size() != 1) return new TextSave(draft, "");
        try {
            Token token = previous.tokens.get(0);
            decode(token);
            String candidate = draft + (draft.endsWith("\n") ? "" : "\n") + token.getText();
            try {
                ProgramDocument.validateSource(candidate);
            } catch (IllegalArgumentException tooLarge) {
                return new TextSave(draft, "Visual layout reset to keep the source within the save limit");
            }
            return new TextSave(candidate, "");
        } catch (IOException | IllegalArgumentException invalid) {
            return new TextSave(draft, "Invalid old visual layout removed");
        }
    }

    private static final class Layout {

        final int version;
        final byte[] sourceHash;
        final BlueprintGraph graph;
        final String generated;

        Layout(int version, byte[] sourceHash, BlueprintGraph graph, String generated) {
            this.version = version;
            this.sourceHash = sourceHash;
            this.graph = graph;
            this.generated = generated;
        }
    }

    private static Layout decode(Token token) throws IOException {
        String marker = token.getText()
            .startsWith(MARKER) ? MARKER : LEGACY_MARKER;
        String encoded = token.getText()
            .substring(marker.length());
        if (encoded.length() > MAX_BYTES) throw new IllegalArgumentException("Layout too large");
        byte[] compressed = Base64.getDecoder()
            .decode(encoded);
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        try (GZIPInputStream zip = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = zip.read(buffer)) != -1) {
                if (raw.size() + read > MAX_LAYOUT_BYTES)
                    throw new IllegalArgumentException("Expanded layout too large");
                raw.write(buffer, 0, read);
            }
        }
        try (DataInputStream data = new DataInputStream(new ByteArrayInputStream(raw.toByteArray()))) {
            int version = data.readInt();
            if (version != (marker.equals(MARKER) ? 2 : 1))
                throw new IllegalArgumentException("Unknown layout version");
            byte[] sourceHash = new byte[32];
            data.readFully(sourceHash);
            byte[] graphHash = sourceHash;
            if (version == 2) {
                graphHash = new byte[32];
                data.readFully(graphHash);
            }
            BlueprintGraph graph = new BlueprintGraph();
            graph.setNameClause(data.readUTF());
            graph.setComments(data.readUTF());
            int nodes = count(data.readInt());
            for (int i = 0; i < nodes; i++) {
                Node node = new Node(
                    uuid(data),
                    Type.valueOf(data.readUTF()),
                    data.readDouble(),
                    data.readDouble(),
                    data.readUTF());
                node.hasElse = data.readBoolean();
                if (node.hasElse && node.type != Type.IF)
                    throw new IllegalArgumentException("ELSE flag on non-IF node");
                graph.add(node);
            }
            int edges = count(data.readInt());
            for (int i = 0; i < edges; i++) {
                UUID from = uuid(data);
                Pin pin = Pin.valueOf(data.readUTF());
                UUID to = uuid(data);
                if (graph.next(from, pin) != null) throw new IllegalArgumentException("Duplicate output wire");
                for (Edge edge : graph.edges())
                    if (edge.to.equals(to)) throw new IllegalArgumentException("Duplicate input wire");
                graph.connect(from, pin, to);
            }
            if (data.available() != 0) throw new IllegalArgumentException("Trailing layout data");
            String generated = BlueprintCompiler.generate(graph);
            if (!ProgramRevision.matches(graphHash, generated))
                throw new IllegalArgumentException("Layout graph digest differs");
            return new Layout(version, sourceHash, graph, generated);
        }
    }

    private static final class Metadata {

        final String plain;
        final List<Token> tokens;

        Metadata(String plain, List<Token> tokens) {
            this.plain = plain;
            this.tokens = tokens;
        }
    }

    public static String withoutMetadata(String source) {
        return scan(source).plain;
    }

    private static Metadata scan(String source) {
        CharStream stream = CharStreams.fromString(source);
        StringBuilder plain = new StringBuilder();
        List<Token> metadata = new ArrayList<>();
        int start = 0;
        for (Token token : BlueprintCompiler.tokens(source)
            .getTokens()) if (isMetadata(token)) {
                metadata.add(token);
                if (token.getStartIndex() > start)
                    plain.append(stream.getText(Interval.of(start, token.getStartIndex() - 1)));
                start = token.getStopIndex() + 1;
            }
        if (start < stream.size()) plain.append(stream.getText(Interval.of(start, stream.size() - 1)));
        return new Metadata(plain.toString(), metadata);
    }

    private static boolean isMetadata(Token token) {
        return token.getType() == SFMLLexer.LINE_COMMENT && (token.getText()
            .startsWith(MARKER)
            || token.getText()
                .startsWith(LEGACY_MARKER));
    }

    private static int count(int value) {
        if (value < 0 || value > BlueprintGraph.MAX_NODES) throw new IllegalArgumentException("Invalid layout count");
        return value;
    }

    private static void uuid(DataOutputStream data, UUID uuid) throws IOException {
        data.writeLong(uuid.getMostSignificantBits());
        data.writeLong(uuid.getLeastSignificantBits());
    }

    private static UUID uuid(DataInputStream data) throws IOException {
        return new UUID(data.readLong(), data.readLong());
    }
}
