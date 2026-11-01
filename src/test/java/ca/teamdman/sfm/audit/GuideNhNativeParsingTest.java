package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/** Optional pinned-jar syntax check; deliberately never initializes Minecraft or GuideNH rendering. */
class GuideNhNativeParsingTest {

    private static final Path BOOK = Paths.get("src/main/resources/assets/superfactorymanager/guidenh/_en_us");
    private static final Set<String> TAGS = new HashSet<>(
        Arrays.asList("GameScene", "Block", "ItemImage", "BlockImage", "TextAnnotation", "LineAnnotation"));
    private static final Pattern FENCES = Pattern.compile("(?m)^```([^\\n]*)\\n([\\s\\S]*?)^```[ \\t]*$");

    @Test
    void bookParsesWithPinnedGuideNhMarkdownMdxAndYaml() throws Exception {
        String jarProperty = System.getProperty("sfm.guideNhJar", "")
            .trim();
        assumeTrue(
            !jarProperty.isEmpty(),
            "Optional native GuideNH validation: supply -PguideNhJar=<absolute jar path>");
        Path jar = Paths.get(jarProperty);
        assertTrue(Files.isRegularFile(jar), "GuideNH jar does not exist: " + jar);

        List<Path> pages;
        try (Stream<Path> files = Files.walk(BOOK)) {
            pages = files.filter(
                path -> path.toString()
                    .endsWith(".md"))
                .sorted()
                .collect(Collectors.toList());
        }
        assertTrue(pages.size() >= 43, "Retain the full guide corpus");
        int navigationCount = 0;
        int sfmlCount = 0;
        int imageCount = 0;
        int blockImageCount = 0;
        int sceneCount = 0, diagramCount = 0;
        List<URL> nativeClasspath = new ArrayList<>();
        nativeClasspath.add(
            jar.toUri()
                .toURL());
        // The ordinary test JVM does not install Forge/RFB's JVM-Downgrader runtime libraries.
        String runtimeJar = System.getProperty("sfm.guideNhRuntimeJar", "")
            .trim();
        if (!runtimeJar.isEmpty()) {
            Path runtime = Paths.get(runtimeJar);
            assertTrue(Files.isRegularFile(runtime), "GuideNH compatibility runtime jar does not exist: " + runtime);
            nativeClasspath.add(
                runtime.toUri()
                    .toURL());
        }
        try (URLClassLoader loader = new URLClassLoader(
            nativeClasspath.toArray(new URL[0]),
            getClass().getClassLoader())) {
            NativeParser parser = new NativeParser(loader);
            for (Path page : pages) {
                String relative = BOOK.relativize(page)
                    .toString()
                    .replace('\\', '/');
                try {
                    String source = new String(Files.readAllBytes(page), StandardCharsets.UTF_8).replace("\r\n", "\n")
                        .replace('\r', '\n');
                    Object ast = parser.parse(source);
                    List<Object> nodes = parser.descendants(ast);
                    List<Object> frontmatter = parser.ofType(nodes, parser.frontmatterType);
                    assertEquals(1, frontmatter.size(), "Exactly one native YAML frontmatter node is required");
                    Object loaded = parser.loadYaml(
                        parser.frontmatterType.getField("value")
                            .get(frontmatter.get(0))
                            .toString());
                    assertTrue(loaded instanceof Map, "Frontmatter must be a YAML mapping");
                    Object rawNavigation = ((Map<?, ?>) loaded).get("navigation");
                    assertTrue(rawNavigation instanceof Map, "navigation must be a mapping");
                    Map<?, ?> navigation = (Map<?, ?>) rawNavigation;
                    assertTrue(navigation.get("title") instanceof String, "navigation.title must be a string");
                    assertFalse(
                        ((String) navigation.get("title")).trim()
                            .isEmpty(),
                        "navigation.title must not be blank");
                    if ("index.md".equals(relative)) {
                        assertNull(navigation.get("parent"), "Book home must be a root navigation entry");
                    } else {
                        Object parent = navigation.get("parent");
                        assertTrue(parent instanceof String, "navigation.parent must be a page path");
                        String parentPath = (String) parent;
                        assertTrue(
                            parentPath.startsWith("/") && parentPath.endsWith(".md"),
                            "Expected book-root parent path");
                        Path resolved = BOOK.resolve(parentPath.substring(1))
                            .normalize();
                        assertTrue(
                            resolved.startsWith(BOOK) && Files.isRegularFile(resolved),
                            "Missing parent: " + parentPath);
                        assertNotEquals(page.normalize(), resolved, "A page cannot parent itself");
                    }
                    navigationCount++;

                    List<Object> elements = parser.ofType(nodes, parser.elementType);
                    for (Object element : elements) {
                        String tag = parser.name(element);
                        assertTrue(TAGS.contains(tag), "Unknown MDX tag: " + tag);
                        if ("ItemImage".equals(tag)) {
                            assertAssetId(parser.attribute(element, "id"));
                            imageCount++;
                        }
                        if ("BlockImage".equals(tag)) {
                            assertAssetId(parser.attribute(element, "id"));
                            blockImageCount++;
                        }
                        if ("GameScene".equals(tag)) {
                            assertScene(parser, element);
                            sceneCount++;
                        }
                    }
                    if ("start/hardware.md".equals(relative) || "start/first-transfer.md".equals(relative)) {
                        assertHardwareScene(parser, elements);
                        List<Object> labels = new ArrayList<>();
                        List<Object> arrows = new ArrayList<>();
                        for (Object element : elements) {
                            if ("TextAnnotation".equals(parser.name(element))) labels.add(element);
                            if ("LineAnnotation".equals(parser.name(element))) arrows.add(element);
                        }
                        boolean hardware = "start/hardware.md".equals(relative);
                        assertEquals(hardware ? 3 : 2, labels.size(), "Readable endpoint labels required");
                        for (Object label : labels) {
                            assertFalse(
                                parser.attribute(label, "text")
                                    .isEmpty());
                            assertFalse(
                                parser.attribute(label, "pos")
                                    .isEmpty());
                        }
                        assertEquals(hardware ? 0 : 1, arrows.size(), "Transfer direction arrow required");
                        if (!hardware) assertEquals("end", parser.attribute(arrows.get(0), "arrow"));
                    }

                    // Compare every fence with native Code literals: braces, structs, and angle brackets stay verbatim.
                    List<Object> codeNodes = parser.ofType(nodes, parser.codeType);
                    Matcher fences = FENCES.matcher(source);
                    int fenceIndex = 0;
                    while (fences.find()) {
                        assertTrue(fenceIndex < codeNodes.size(), "A fenced example was not parsed as Code");
                        Object code = codeNodes.get(fenceIndex++);
                        String language = fences.group(1)
                            .trim();
                        assertEquals(
                            language,
                            parser.codeType.getField("lang")
                                .get(code),
                            "Code language changed");
                        String expected = fences.group(2);
                        if (expected.endsWith("\n")) expected = expected.substring(0, expected.length() - 1);
                        assertEquals(
                            expected,
                            parser.codeType.getField("value")
                                .get(code),
                            "Code contents changed during native MDX parsing");
                        if ("sfml".equals(language)) {
                            sfmlCount++;
                        }
                        if ("mermaid".equals(language)) {
                            assertFlowchart(loader, expected);
                            diagramCount++;
                        }
                    }
                    assertEquals(fenceIndex, codeNodes.size(), "Unexpected native Code nodes");
                } catch (Throwable failure) {
                    Throwable cause = failure instanceof InvocationTargetException ? failure.getCause() : failure;
                    throw new AssertionError("Native GuideNH validation failed for " + relative + ": " + cause, cause);
                }
            }
        }
        assertEquals(pages.size(), navigationCount, "Every page must supply navigation");
        assertTrue(sfmlCount >= 58, "Retain complete tutorial coverage");
        assertTrue(imageCount >= 12, "Retain useful item icons");
        assertTrue(blockImageCount >= 3, "Retain equipment previews");
        assertTrue(sceneCount >= 4, "Include spatial explanations beyond the two setup scenes");
        assertTrue(diagramCount >= 2, "Include explanatory Studio/editor workflow diagrams");
    }

    private static void assertAssetId(String id) {
        assertTrue(
            id.matches("(?:superfactorymanager|minecraft):[A-Za-z0-9_./-]+"),
            "Known book asset namespace: " + id);
    }

    private static void assertScene(NativeParser parser, Object scene) throws Exception {
        int width = Integer.parseInt(parser.attribute(scene, "width"));
        int height = Integer.parseInt(parser.attribute(scene, "height"));
        assertTrue(width >= 160 && width <= 320 && height >= 100 && height <= 240, "Compact scene size");
        Set<String> positions = new HashSet<>();
        int blocks = 0, labels = 0;
        for (Object child : parser.descendants(scene)) {
            if (!parser.elementType.isInstance(child)) continue;
            String tag = parser.name(child);
            if ("Block".equals(tag)) {
                assertAssetId(parser.attribute(child, "id"));
                String pos = "";
                for (String axis : new String[] { "x", "y", "z" }) {
                    int coordinate = Integer.parseInt(parser.attribute(child, axis));
                    assertTrue(Math.abs(coordinate) <= 16, "Keep demonstration coordinates bounded");
                    pos += coordinate + ",";
                }
                assertTrue(positions.add(pos), "Two scene blocks occupy the same position");
                blocks++;
            } else if ("TextAnnotation".equals(tag)) {
                assertFalse(
                    parser.attribute(child, "text")
                        .trim()
                        .isEmpty(),
                    "Scene label text");
                assertVector(parser.attribute(child, "pos"));
                labels++;
            } else if ("LineAnnotation".equals(tag)) {
                assertVector(parser.attribute(child, "from"));
                assertVector(parser.attribute(child, "to"));
            }
        }
        assertTrue(blocks >= 1 && labels >= 2, "Scenes need labelled faces/endpoints, not decoration alone");
    }

    private static void assertVector(String vector) {
        String[] coordinates = vector.trim()
            .split("\\s+");
        assertEquals(3, coordinates.length, "Scene position must have three coordinates");
        for (String coordinate : coordinates) assertTrue(Double.isFinite(Double.parseDouble(coordinate)));
    }

    private static void assertFlowchart(ClassLoader loader, String source) throws Exception {
        Class<?> type = loader.loadClass("com.hfstudio.guidenh.guide.internal.mermaid.flowchart.FlowchartParser");
        Object graph = type.getMethod("parse", String.class)
            .invoke(null, source);
        Map<?, ?> nodes = (Map<?, ?>) graph.getClass()
            .getMethod("getNodes")
            .invoke(graph);
        List<?> edges = (List<?>) graph.getClass()
            .getMethod("getEdges")
            .invoke(graph);
        assertTrue(nodes.size() >= 2 && !edges.isEmpty(), "Workflow diagram needs linked steps");
        for (Object edge : edges) {
            assertTrue(
                nodes.containsKey(
                    edge.getClass()
                        .getMethod("getFrom")
                        .invoke(edge)),
                "Missing diagram source");
            assertTrue(
                nodes.containsKey(
                    edge.getClass()
                        .getMethod("getTo")
                        .invoke(edge)),
                "Missing diagram destination");
        }
    }

    private static void assertHardwareScene(NativeParser parser, List<Object> elements) throws Exception {
        List<Object> scenes = new ArrayList<>();
        for (Object element : elements) {
            if ("GameScene".equals(parser.name(element))) scenes.add(element);
        }
        assertEquals(1, scenes.size(), "Hardware page must have one native GameScene");
        Object scene = scenes.get(0);
        assertEquals("260", parser.attribute(scene, "width"));
        assertEquals("150", parser.attribute(scene, "height"));
        Object interactive = parser.elementType.getMethod("getAttribute", String.class)
            .invoke(scene, "interactive");
        assertNotNull(interactive, "interactive attribute missing");
        assertEquals(
            true,
            interactive.getClass()
                .getMethod("hasExpressionValue")
                .invoke(interactive));
        assertEquals(
            "true",
            interactive.getClass()
                .getMethod("getExpressionValue")
                .invoke(interactive));

        List<Object> blocks = new ArrayList<>();
        for (Object child : parser.descendants(scene)) {
            if (parser.elementType.isInstance(child) && "Block".equals(parser.name(child))) blocks.add(child);
        }
        assertEquals(5, blocks.size(), "GameScene children must parse as five Block elements");
        String[][] expected = { { "superfactorymanager:manager", "0", "0", "0" },
            { "superfactorymanager:cable", "1", "0", "0" }, { "minecraft:chest", "2", "0", "0" },
            { "superfactorymanager:cable", "1", "0", "1" }, { "minecraft:chest", "1", "0", "2" } };
        for (int i = 0; i < expected.length; i++) {
            String[] fields = { "id", "x", "y", "z" };
            for (int j = 0; j < fields.length; j++)
                assertEquals(expected[i][j], parser.attribute(blocks.get(i), fields[j]));
        }
    }

    private static final class NativeParser {

        private final Class<?> parentType;
        private final Class<?> elementType;
        private final Class<?> frontmatterType;
        private final Class<?> codeType;
        private final Method parse;
        private final Object options;
        private final Object yaml;
        private final Method yamlLoad;

        NativeParser(ClassLoader loader) throws Exception {
            String base = "com.hfstudio.guidenh.";
            Class<?> optionsType = loader.loadClass(base + "libs.mdast.MdastOptions");
            options = loader.loadClass(base + "guide.compiler.GuideMarkdownOptions")
                .getMethod("runtime")
                .invoke(null);
            parse = loader.loadClass(base + "libs.mdast.MdAst")
                .getMethod("fromMarkdown", String.class, optionsType);
            parentType = loader.loadClass(base + "libs.mdast.model.MdAstParent");
            elementType = loader.loadClass(base + "libs.mdast.mdx.model.MdxJsxElementFields");
            frontmatterType = loader.loadClass(base + "libs.mdast.MdAstYamlFrontmatter");
            codeType = loader.loadClass(base + "libs.mdast.model.MdAstCode");

            String yamlBase = base + "shadow.org.yaml.snakeyaml.";
            Class<?> loaderOptionsType = loader.loadClass(yamlBase + "LoaderOptions");
            Object loaderOptions = loaderOptionsType.getConstructor()
                .newInstance();
            loaderOptionsType.getMethod("setAllowDuplicateKeys", boolean.class)
                .invoke(loaderOptions, false);
            Class<?> safeType = loader.loadClass(yamlBase + "constructor.SafeConstructor");
            Object constructor = safeType.getConstructor(loaderOptionsType)
                .newInstance(loaderOptions);
            Class<?> yamlType = loader.loadClass(yamlBase + "Yaml");
            yaml = yamlType.getConstructor(loader.loadClass(yamlBase + "constructor.BaseConstructor"))
                .newInstance(constructor);
            yamlLoad = yamlType.getMethod("load", String.class);
        }

        Object parse(String source) throws Exception {
            return parse.invoke(null, source, options);
        }

        Object loadYaml(String source) throws Exception {
            return yamlLoad.invoke(yaml, source);
        }

        String name(Object element) throws Exception {
            return (String) elementType.getMethod("name")
                .invoke(element);
        }

        String attribute(Object element, String name) throws Exception {
            Object attribute = elementType.getMethod("getAttribute", String.class)
                .invoke(element, name);
            assertNotNull(attribute, "Missing " + name + " on " + name(element));
            assertEquals(
                true,
                attribute.getClass()
                    .getMethod("hasStringValue")
                    .invoke(attribute),
                "Expected quoted " + name);
            return (String) attribute.getClass()
                .getMethod("getStringValue")
                .invoke(attribute);
        }

        List<Object> descendants(Object root) throws Exception {
            List<Object> nodes = new ArrayList<>();
            visit(root, nodes);
            return nodes;
        }

        private void visit(Object node, List<Object> nodes) throws Exception {
            nodes.add(node);
            if (parentType.isInstance(node)) {
                for (Object child : (List<?>) parentType.getMethod("children")
                    .invoke(node)) visit(child, nodes);
            }
        }

        List<Object> ofType(List<Object> nodes, Class<?> type) {
            return nodes.stream()
                .filter(type::isInstance)
                .collect(Collectors.toList());
        }
    }
}
