package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

/** Compile actual VS Code snippet defaults in explicit insertion contexts, independently of highlighting. */
class VscodeSfmlExtensionTest {

    @TestFactory
    List<DynamicTest> everySnippetDefaultCompilesInItsInsertionContext() throws Exception {
        JsonObject snippets;
        try (Reader reader = Files
            .newBufferedReader(Paths.get("editors/vscode-sfml/snippets/sfml.json"), StandardCharsets.UTF_8)) {
            snippets = new JsonParser().parse(reader)
                .getAsJsonObject();
        }
        assertEquals(
            35,
            snippets.entrySet()
                .size(),
            "Update contexts deliberately when adding snippets");
        List<DynamicTest> tests = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : snippets.entrySet()) {
            JsonObject snippet = entry.getValue()
                .getAsJsonObject();
            String prefix = snippet.get("prefix")
                .getAsString();
            String fragment = expand(body(snippet.get("body")), new HashMap<>());
            tests.add(DynamicTest.dynamicTest(prefix + ": " + entry.getKey(), () -> {
                String source = completeProgram(prefix, fragment);
                ProgramBuildResult result = new ProgramBuilder(source).build();
                assertTrue(result.isSuccess(), result.getErrorMessage() + "\n" + source);
            }));
        }
        return tests;
    }

    @Test
    void expansionPreservesLiteralSigilsAndMirrorsDefaults() {
        assertEquals(
            "let $value = 7\n$value rising ",
            expand("let \\$${1:value} = ${2:7}\n\\$${1} ${3|rising,falling,pulse|} $0", new HashMap<>()));
    }

    private static String body(JsonElement body) {
        if (!body.isJsonArray()) return body.getAsString();
        StringBuilder text = new StringBuilder();
        for (JsonElement line : body.getAsJsonArray()) {
            if (text.length() > 0) text.append('\n');
            text.append(line.getAsString());
        }
        return text.toString();
    }

    /**
     * Each case describes where a player inserts the fragment. Queries are compiled but never executed;
     * declarations provide types without requiring any inventory or world fixture.
     */
    private static String completeProgram(String prefix, String fragment) {
        switch (prefix) {
            case "name":
            case "globallet":
            case "state":
            case "function":
                return fragment + "\nevery 20 ticks do end";
            case "every":
            case "everyglobal":
            case "pulse":
            case "redstonetrigger":
                return fragment;
            case "return":
                return "function sample() returns integer do\n" + fragment + "\nend";
            case "assign":
                return trigger("let $value = 1\n" + fragment);
            case "forin":
                return trigger("let $values = [1, 2, 3]\n" + fragment);
            case "label":
            case "item":
            case "ore":
            case "string":
            case "array":
            case "struct":
                return trigger("let $example = " + fragment);
            case "contents":
            case "count":
            case "empty":
            case "available":
                return trigger("let $source = label(\"source\")\nlet $example = " + fragment);
            case "side":
            case "slots":
                return trigger("let $example = label(\"source\")" + fragment);
            case "transfer2":
            case "transfer3":
            case "transfer4":
                return trigger(endpoints() + fragment);
            case "into":
                return trigger(endpoints() + "transfer($source, $destination) " + fragment);
            case "redstoneout":
            case "let":
            case "if":
            case "ifelse":
            case "for":
            case "while":
            case "print":
            case "pedestals":
                return trigger(fragment);
            default:
                throw new AssertionError("Document an insertion context for snippet " + prefix);
        }
    }

    private static String trigger(String body) {
        return "every 20 ticks do\n" + body + "\nend";
    }

    private static String endpoints() {
        return "let $source = label(\"source\")\nlet $destination = label(\"destination\")\n";
    }

    /**
     * Expand the snippet subset used by this extension: escaped literals, numeric tab stops,
     * placeholder defaults, mirrored stops and the first choice. Fail on unsupported syntax so future
     * snippet changes cannot silently bypass the compiler checks.
     */
    private static String expand(String template, Map<String, String> stops) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < template.length();) {
            char c = template.charAt(i++);
            if (c == '\\') {
                assertTrue(i < template.length(), "Incomplete snippet escape: " + template);
                char escaped = template.charAt(i++);
                assertTrue("$\\},|".indexOf(escaped) >= 0, "Unsupported snippet escape: " + escaped);
                result.append(escaped);
            } else if (c == '$') {
                boolean braced = i < template.length() && template.charAt(i) == '{';
                if (braced) i++;
                int start = i;
                while (i < template.length() && Character.isDigit(template.charAt(i))) i++;
                assertTrue(i > start, "Expected numeric snippet tab stop: " + template);
                String id = template.substring(start, i);
                String value = stops.containsKey(id) ? stops.get(id) : "";
                if (braced) {
                    assertTrue(i < template.length(), "Unclosed snippet placeholder: " + template);
                    char marker = template.charAt(i++);
                    if (marker == ':') {
                        int end = template.indexOf('}', i);
                        assertTrue(end >= 0, "Unclosed snippet default: " + template);
                        value = expand(template.substring(i, end), stops);
                        i = end + 1;
                    } else if (marker == '|') {
                        int end = template.indexOf("|}", i);
                        assertTrue(end >= 0, "Unclosed snippet choice: " + template);
                        String choices = template.substring(i, end);
                        int comma = choices.indexOf(',');
                        value = expand(comma < 0 ? choices : choices.substring(0, comma), stops);
                        i = end + 2;
                    } else {
                        assertEquals('}', marker, "Unsupported snippet placeholder: " + template);
                    }
                }
                if (!stops.containsKey(id)) stops.put(id, value);
                result.append(stops.get(id));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}
