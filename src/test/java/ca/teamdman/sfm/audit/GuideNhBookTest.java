package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

/** Player examples and navigation are executable content contracts, independent of optional GuideNH. */
class GuideNhBookTest {

    static final Path ROOT = Paths.get("src/main/resources/assets/superfactorymanager/guidenh/_en_us");
    private static final Pattern PROGRAM = Pattern.compile("(?ms)^```sfml[ \\t]*\\n(.*?)^```[ \\t]*$");
    private static final Pattern FENCE = Pattern.compile("(?ms)^```[^\\n]*\\n.*?^```[ \\t]*$");
    private static final Pattern LINK = Pattern.compile("\\[[^]\\n]*]\\(([^)\\s]+)\\)");
    private static final Pattern PARENT = Pattern.compile("(?m)^  parent: [\"']?([^\"'\\n]+)[\"']?$");
    private static final Pattern ITEM = Pattern.compile("superfactorymanager:([A-Za-z_][A-Za-z0-9_]*)");
    private static final Set<String> ITEMS = new HashSet<>();
    static {
        ITEMS.add("manager");
        ITEMS.add("disk");
        ITEMS.add("labelgun");
        ITEMS.add("cable");
        ITEMS.add("fancyCable");
        ITEMS.add("programmingStation");
        ITEMS.add("redstonePort");
    }

    static Map<Path, String> pages() throws Exception {
        try (Stream<Path> files = Files.walk(ROOT)) {
            Map<Path, String> result = new LinkedHashMap<>();
            for (Path path : files.filter(
                p -> p.toString()
                    .endsWith(".md"))
                .sorted()
                .collect(Collectors.toList()))
                result.put(path, new String(Files.readAllBytes(path), StandardCharsets.UTF_8).replace("\r\n", "\n"));
            return result;
        }
    }

    @TestFactory
    List<DynamicTest> completeProgramsCompile() throws Exception {
        List<DynamicTest> result = new ArrayList<>();
        for (Map.Entry<Path, String> page : pages().entrySet()) {
            Matcher blocks = PROGRAM.matcher(page.getValue());
            int number = 0;
            while (blocks.find()) {
                String source = blocks.group(1);
                String title = ROOT.relativize(page.getKey()) + " example " + ++number;
                result.add(DynamicTest.dynamicTest(title, () -> {
                    ProgramBuildResult built = new ProgramBuilder(source).build();
                    assertTrue(built.isSuccess(), built.getErrorMessage() + "\n" + source);
                }));
            }
        }
        assertTrue(result.size() >= 40, "The full book must retain its complete tutorial examples");
        return result;
    }

    @Test
    void allChaptersHaveReachableNavigationValidLocalLinksAndRegisteredSfmItems() throws Exception {
        Map<Path, String> pages = pages();
        assertTrue(pages.size() >= 30, "The book must include tutorials, automation, language and reference");
        Map<Path, Path> parents = new HashMap<>();
        Set<String> indexed = new HashSet<>();
        for (Map.Entry<Path, String> entry : pages.entrySet()) {
            Path page = entry.getKey();
            String source = entry.getValue();
            assertTrue(source.startsWith("---\nnavigation:\n"), page.toString());
            int end = source.indexOf("\n---\n", 4);
            assertTrue(end > 0, "Closed frontmatter: " + page);
            String frontmatter = source.substring(4, end);
            assertTrue(frontmatter.contains("  title:"), "Navigation title: " + page);
            String body = source.substring(end + 5);
            assertTrue(
                body.trim()
                    .startsWith("# "),
                "H1 title: " + page);
            int fenceCount = 0;
            for (String line : source.split("\n")) {
                if (!line.startsWith("```")) continue;
                if (fenceCount % 2 == 0) assertTrue(
                    line.trim()
                        .equals("```sfml")
                        || line.trim()
                            .equals("```text"),
                    "Explicit SFML program or text-fragment language required: " + page + ": " + line);
                fenceCount++;
            }
            assertEquals(0, fenceCount % 2, "Paired code fences: " + page);
            Matcher parent = PARENT.matcher(frontmatter);
            if (!page.equals(ROOT.resolve("index.md"))) {
                assertTrue(parent.find(), "Navigation parent: " + page);
                Path resolved = resolve(
                    page,
                    parent.group(1)
                        .trim());
                assertTrue(pages.containsKey(resolved), "Missing parent from " + page + ": " + resolved);
                parents.put(page, resolved);
            }
            Matcher links = LINK.matcher(
                FENCE.matcher(body)
                    .replaceAll(""));
            while (links.find()) {
                String target = links.group(1);
                if (target.startsWith("https://") || target.startsWith("http://")) continue;
                Path resolved = resolve(page, target);
                assertTrue(pages.containsKey(resolved), "Broken book link in " + page + ": " + target);
            }
            Matcher items = ITEM.matcher(
                FENCE.matcher(source)
                    .replaceAll(""));
            while (items.find())
                assertTrue(ITEMS.contains(items.group(1)), "Unknown SFM item in " + page + ": " + items.group());
            int itemIndex = frontmatter.indexOf("item_ids:");
            if (itemIndex >= 0) {
                Matcher bound = ITEM.matcher(frontmatter.substring(itemIndex));
                while (bound.find())
                    assertTrue(indexed.add(bound.group(1)), "Duplicate item shortcut: " + bound.group());
            }
        }
        assertEquals(ITEMS, indexed, "Every current SFM block/item must have exactly one guide shortcut");
        for (Path page : pages.keySet()) {
            Set<Path> seen = new HashSet<>();
            Path next = page;
            while (!next.equals(ROOT.resolve("index.md"))) {
                assertTrue(seen.add(next), "Navigation cycle at " + next);
                next = parents.get(next);
                assertNotNull(next, "Page is not reachable from home: " + page);
            }
        }
    }

    private static Path resolve(Path from, String link) {
        String path = link.split("#", 2)[0];
        if (path.isEmpty()) return from;
        assertFalse(path.contains(":"), "Use book-local links, not filesystem/developer paths: " + link);
        Path result = (path.startsWith("/") ? ROOT.resolve(path.substring(1))
            : from.getParent()
                .resolve(path)).normalize();
        assertTrue(result.startsWith(ROOT), "Link escaped book: " + link);
        return result;
    }
}
