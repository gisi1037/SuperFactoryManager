package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/** Exercise the exported development launch contract in a real offline child JVM. */
class SfmlCompilerServerTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    @Timeout(60)
    void compilesMultipleRequestsAndReportsActualSyntaxAndSemanticErrors() throws Exception {
        List<JsonObject> responses = run(
            request(1, "every 20 ticks do\nlet $value =\nend"),
            request(2, "every 20 ticks do\nprint($missing)\nend"),
            request(3, "every 20 ticks do\nlet $value = 1\nprint($value)\nend"),
            request(4, "every 20 ticks do\nprint(\"\uD83D\uDE00\") @\nend"));
        assertEquals(4, responses.size(), "Stdout contains exactly one JSON response per request");
        assertEquals(
            1,
            responses.get(0)
                .get("id")
                .getAsInt());
        JsonObject syntax = responses.get(0)
            .getAsJsonArray("diagnostics")
            .get(0)
            .getAsJsonObject();
        assertEquals(
            2,
            syntax.get("line")
                .getAsInt(),
            "ANTLR's third line becomes zero-based line 2");
        assertEquals(
            3,
            syntax.get("column")
                .getAsInt());
        assertEquals(
            "error",
            syntax.get("severity")
                .getAsString());
        assertEquals(
            2,
            responses.get(1)
                .get("id")
                .getAsInt());
        JsonObject semantic = responses.get(1)
            .getAsJsonArray("diagnostics")
            .get(0)
            .getAsJsonObject();
        assertTrue(
            semantic.get("message")
                .getAsString()
                .contains("missing"));
        assertEquals(
            1,
            semantic.get("line")
                .getAsInt(),
            "The compiler provides the expression's source location");
        assertEquals(
            6,
            semantic.get("column")
                .getAsInt());
        assertEquals(
            3,
            responses.get(2)
                .get("id")
                .getAsInt());
        assertEquals(
            0,
            responses.get(2)
                .getAsJsonArray("diagnostics")
                .size());
        JsonObject unicode = responses.get(3)
            .getAsJsonArray("diagnostics")
            .get(0)
            .getAsJsonObject();
        assertEquals(
            1,
            unicode.get("line")
                .getAsInt());
        assertEquals(
            12,
            unicode.get("column")
                .getAsInt(),
            "A supplementary character occupies two VS Code columns");
    }

    @Test
    @Timeout(60)
    void boundsRequestsAndRecoversForTheNextCompilation() throws Exception {
        char[] excessiveSource = new char[32768];
        Arrays.fill(excessiveSource, 'a');
        char[] excessiveLine = new char[262145];
        Arrays.fill(excessiveLine, ' ');
        List<JsonObject> responses = run(
            "{",
            request(4, new String(excessiveSource)),
            new String(excessiveLine),
            request(5, "every 20 ticks do end"));
        assertEquals(4, responses.size());
        for (int i = 0; i < 3; i++) {
            JsonArray diagnostics = responses.get(i)
                .getAsJsonArray("diagnostics");
            assertTrue(diagnostics.size() > 0 && diagnostics.size() <= 32);
            assertTrue(
                diagnostics.get(0)
                    .getAsJsonObject()
                    .get("message")
                    .getAsString()
                    .length() <= 4096);
        }
        assertEquals(
            4,
            responses.get(1)
                .get("id")
                .getAsInt());
        assertTrue(
            responses.get(1)
                .getAsJsonArray("diagnostics")
                .toString()
                .contains("32767"));
        assertTrue(
            responses.get(2)
                .getAsJsonArray("diagnostics")
                .toString()
                .contains("262144"));
        assertEquals(
            5,
            responses.get(3)
                .get("id")
                .getAsInt());
        assertEquals(
            0,
            responses.get(3)
                .getAsJsonArray("diagnostics")
                .size());
    }

    private List<JsonObject> run(String... requests) throws Exception {
        String manifestPath = System.getProperty("sfm.compilerManifest");
        assertNotNull(manifestPath, "prepareSfmlCompiler must export the checked launch manifest before tests");
        JsonObject manifest;
        try (Reader reader = Files.newBufferedReader(Paths.get(manifestPath), StandardCharsets.UTF_8)) {
            manifest = new JsonParser().parse(reader)
                .getAsJsonObject();
        }
        List<String> entries = new ArrayList<>();
        for (JsonElement entry : manifest.getAsJsonArray("classpath")) entries.add(entry.getAsString());
        assertFalse(entries.isEmpty());
        String javaExecutable = manifest.get("javaExecutable")
            .getAsString();
        assertTrue(Files.isRegularFile(Paths.get(javaExecutable)), "Exported Java executable exists");
        Path log = temporaryDirectory.resolve("compiler-" + System.nanoTime() + ".log");
        Process process = new ProcessBuilder(
            javaExecutable,
            "-cp",
            String.join(File.pathSeparator, entries),
            manifest.get("mainClass")
                .getAsString()).redirectError(log.toFile())
                    .start();
        try {
            try (BufferedWriter input = new BufferedWriter(
                new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8))) {
                for (String request : requests) {
                    input.write(request);
                    input.newLine();
                }
            }
            assertTrue(process.waitFor(30, TimeUnit.SECONDS), "Compiler server must finish after stdin closes");
            assertEquals(0, process.exitValue(), new String(Files.readAllBytes(log), StandardCharsets.UTF_8));
            List<JsonObject> responses = new ArrayList<>();
            try (BufferedReader output = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = output.readLine()) != null) responses.add(
                    new JsonParser().parse(line)
                        .getAsJsonObject());
            }
            return responses;
        } finally {
            process.destroyForcibly();
        }
    }

    private static String request(int id, String source) {
        JsonObject request = new JsonObject();
        request.addProperty("id", id);
        request.addProperty("source", source);
        return request.toString();
    }
}
