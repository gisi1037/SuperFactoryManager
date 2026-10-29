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
    private static final Set<String> TAGS = new HashSet<>(Arrays.asList("GameScene", "Block", "ItemImage"));
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
        assertEquals(35, pages.size(), "Unexpected guide corpus size");
        int navigationCount = 0;
        int sfmlCount = 0;
        int imageCount = 0;
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
                            assertTrue(
                                parser.attribute(element, "id")
                                    .startsWith("superfactorymanager:"),
                                "Expected SFM item icon");
                            imageCount++;
                        }
                    }
                    if ("start/hardware.md".equals(relative)) {
                        assertHardwareScene(parser, elements);
                    } else {
                        for (Object element : elements) {
                            assertNotEquals(
                                "GameScene",
                                parser.name(element),
                                "Unexpected scene outside hardware page");
                            assertNotEquals(
                                "Block",
                                parser.name(element),
                                "Unexpected scene block outside hardware page");
                        }
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
                        if ("sfml".equals(language)) sfmlCount++;
                    }
                    assertEquals(fenceIndex, codeNodes.size(), "Unexpected native Code nodes");
                } catch (Throwable failure) {
                    Throwable cause = failure instanceof InvocationTargetException ? failure.getCause() : failure;
                    throw new AssertionError("Native GuideNH validation failed for " + relative + ": " + cause, cause);
                }
            }
        }
        assertEquals(35, navigationCount, "Every page must supply navigation");
        assertEquals(51, sfmlCount, "Every complete program must remain a native sfml Code node");
        assertEquals(5, imageCount, "Hardware and station item icons must remain native MDX elements");
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
