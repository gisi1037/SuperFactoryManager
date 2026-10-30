package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.compat.guidenh.GuideNhSfml;

class GuideNhSfmlRendererTest {

    @Test
    void optionalBridgeDoesNotRequireGuideNhToLoad() {
        assertThrows(ClassNotFoundException.class, () -> GuideNhSfml.createRenderer(new ClassLoader(null) {}));
    }

    @Test
    void pinnedGuideNhRendersSfmlWithoutOtherLanguageHeuristicsAndPreservesCopyText() throws Exception {
        String jar = System.getProperty("sfm.guideNhJar", "")
            .trim();
        assumeTrue(!jar.isEmpty(), "Supply the pinned GuideNH jar to test its actual renderer API");
        List<URL> urls = new ArrayList<>();
        urls.add(
            Paths.get(jar)
                .toUri()
                .toURL());
        String runtime = System.getProperty("sfm.guideNhRuntimeJar", "")
            .trim();
        if (!runtime.isEmpty()) urls.add(
            Paths.get(runtime)
                .toUri()
                .toURL());
        try (URLClassLoader loader = new URLClassLoader(urls.toArray(new URL[0]), getClass().getClassLoader())) {
            String base = "com.hfstudio.guidenh.";
            Object renderer = GuideNhSfml.createRenderer(loader);
            Class<?> api = loader.loadClass(base + "guide.compiler.tags.CodeFenceRenderer");
            Class<?> compiler = loader.loadClass(base + "guide.compiler.PageCompiler");
            assertEquals(
                java.util.Collections.singleton("sfml"),
                (Set<?>) api.getMethod("getFenceNames")
                    .invoke(renderer));
            for (String source : new String[] { "every 20 ticks do\n input from source\n output to destination\nend\n",
                "every 20 ticks do\n if reserve has < 16 item:* then\n input from source\n end\nend\n" }) {
                Object block = api.getMethod("renderFence", compiler, String.class, String.class, String.class)
                    .invoke(renderer, null, "sfml", source, null);
                assertEquals(
                    "SFML",
                    block.getClass()
                        .getMethod("getLanguageDisplayName")
                        .invoke(block));
                assertEquals(
                    "text",
                    block.getClass()
                        .getMethod("getDetectedLanguageId")
                        .invoke(block));
                assertEquals(
                    source,
                    block.getClass()
                        .getMethod("getCodeText")
                        .invoke(block));
            }
            String html = (String) api.getMethod("renderSiteFence", String.class, String.class, String.class)
                .invoke(renderer, "sfml", "<code>&\"", null);
            assertTrue(html.contains("language-sfml"));
            assertTrue(html.contains("&lt;code&gt;&amp;&quot;"));
            assertTrue(renderer.equals(renderer));
            assertFalse(renderer.equals(new Object()));
        }
    }
}
