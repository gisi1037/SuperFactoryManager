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
import java.util.Base64;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;

import ca.teamdman.langs.SFMLLexer;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Edge;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Pin;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Type;
import ca.teamdman.sfm.common.util.ProgramRevision;

/** Bounded layout comment, replacing Retro's modern Mojang codecs. Not wire-format compatible with Retro. */
public final class BlueprintDocument {

    public static final String MARKER = "-- sfm:blueprint:v1 ";
    public static final int MAX_BYTES = 32700;
    private static final int MAX_LAYOUT_BYTES = 262144;
    public final BlueprintGraph graph;
    public final String notice;
    public final boolean restoredLayout;

    private BlueprintDocument(BlueprintGraph graph, String notice, boolean restoredLayout) {
        this.graph = graph;
        this.notice = notice;
        this.restoredLayout = restoredLayout;
    }

    public static String save(BlueprintGraph graph) {
        String code = BlueprintCompiler.generate(graph);
        try {
            ByteArrayOutputStream raw = new ByteArrayOutputStream();
            try (DataOutputStream data = new DataOutputStream(raw)) {
                data.writeInt(1);
                data.write(ProgramRevision.digest(code));
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
            String source = code + "\n"
                + MARKER
                + Base64.getEncoder()
                    .encodeToString(compressed.toByteArray());
            if (source.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > MAX_BYTES)
                throw new IllegalArgumentException("Program and layout exceed the 32700-byte editor save limit");
            return source;
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot encode blueprint", e);
        }
    }

    public static BlueprintDocument open(String source) {
        if (source.length() > 32767) throw new IllegalArgumentException("Program exceeds 32767 characters");
        String plain = withoutMetadata(source);
        Token metadata = null;
        int count = 0;
        for (Token token : BlueprintCompiler.tokens(source)
            .getTokens()) {
            if (isMetadata(token)) {
                metadata = token;
                count++;
            }
        }
        if (count == 1) {
            try {
                String encoded = metadata.getText()
                    .substring(MARKER.length());
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
                    if (data.readInt() != 1) throw new IllegalArgumentException("Unknown layout version");
                    byte[] hash = new byte[32];
                    data.readFully(hash);
                    if (!ProgramRevision.matches(hash, plain.trim()))
                        throw new IllegalArgumentException("Stale layout");
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
                        graph.add(node);
                    }
                    int edges = count(data.readInt());
                    for (int i = 0; i < edges; i++) {
                        UUID from = uuid(data);
                        Pin pin = Pin.valueOf(data.readUTF());
                        UUID to = uuid(data);
                        // Serialized duplicates/replacements are invalid, even though interactive rewiring is allowed.
                        if (graph.next(from, pin) != null) throw new IllegalArgumentException("Duplicate output wire");
                        for (Edge edge : graph.edges())
                            if (edge.to.equals(to)) throw new IllegalArgumentException("Duplicate input wire");
                        graph.connect(from, pin, to);
                    }
                    if (data.available() != 0 || !BlueprintCompiler.generate(graph)
                        .equals(plain.trim()))
                        throw new IllegalArgumentException("Layout does not describe the current text");
                    return new BlueprintDocument(graph, "Layout restored", true);
                }
            } catch (IOException | IllegalArgumentException e) {
                // Text is authoritative. Never revive an older graph after editing its generated SFML.
            }
        }
        return new BlueprintDocument(
            BlueprintCompiler.importText(plain),
            count == 0 ? "Imported SFML; comments kept above the program"
                : "Layout stale or invalid; rebuilt from current SFML",
            false);
    }

    public static String withoutMetadata(String source) {
        // Work with lexer tokens, so a marker inside a quoted label cannot be mistaken for metadata.
        org.antlr.v4.runtime.CharStream stream = org.antlr.v4.runtime.CharStreams.fromString(source);
        StringBuilder plain = new StringBuilder();
        int start = 0;
        for (Token token : BlueprintCompiler.tokens(source)
            .getTokens()) if (isMetadata(token)) {
                if (token.getStartIndex() > start)
                    plain.append(stream.getText(Interval.of(start, token.getStartIndex() - 1)));
                start = token.getStopIndex() + 1;
            }
        if (start < stream.size()) plain.append(stream.getText(Interval.of(start, stream.size() - 1)));
        return plain.toString();
    }

    private static boolean isMetadata(Token token) {
        return token.getType() == SFMLLexer.LINE_COMMENT && token.getText()
            .startsWith(MARKER);
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
