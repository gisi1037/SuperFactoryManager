package ca.teamdman.sfm.compat.guidenh;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;

import ca.teamdman.sfm.SuperFactoryManager;
import cpw.mods.fml.common.Loader;

/** Optional client bridge: SFML fences must not be guessed as another programming language. */
public final class GuideNhSfml {

    private static final String BASE = "com.hfstudio.guidenh.";

    private GuideNhSfml() {}

    public static void register() {
        if (!Loader.isModLoaded("guidenh")) return;
        try {
            ClassLoader loader = GuideNhSfml.class.getClassLoader();
            Class<?> registry = loader.loadClass(BASE + "integration.api.GuideNhIntegrationRegistry");
            Class<?> renderer = loader.loadClass(BASE + "guide.compiler.tags.CodeFenceRenderer");
            registry.getMethod("registerCodeFenceRenderer", renderer)
                .invoke(
                    registry.getMethod("global")
                        .invoke(null),
                    createRenderer(loader));
        } catch (ReflectiveOperationException | LinkageError unavailable) {
            SuperFactoryManager.LOG.warn("GuideNH SFML code-block integration is unavailable", unavailable);
        }
    }

    /** Resolve the optional public API once; neither common startup nor a dedicated server links GuideNH. */
    public static Object createRenderer(ClassLoader loader) throws ReflectiveOperationException {
        Class<?> renderer = loader.loadClass(BASE + "guide.compiler.tags.CodeFenceRenderer");
        Class<?> block = loader.loadClass(BASE + "guide.document.block.LytCodeBlock");
        Constructor<?> constructor = block.getConstructor();
        Method setContent = block.getMethod("setCodeContent", String.class, String.class);
        Method setLabel = block.getMethod("setLanguageDisplayName", String.class);
        return Proxy.newProxyInstance(loader, new Class<?>[] { renderer }, (proxy, method, args) -> {
            switch (method.getName()) {
                case "getFenceNames":
                    return Collections.singleton("sfml");
                case "renderFence":
                    Object code = constructor.newInstance();
                    // Explicit plain highlighting prevents GuideNH's Lua/Bash heuristics; copy text is untouched.
                    setContent.invoke(code, "text", args[2]);
                    setLabel.invoke(code, "SFML");
                    return code;
                case "renderSiteFence":
                    return "<pre data-language=\"SFML\"><code class=\"language-sfml\">" + escape((String) args[1])
                        + "</code></pre>";
                case "equals":
                    return proxy == args[0];
                case "hashCode":
                    return System.identityHashCode(proxy);
                case "toString":
                    return "SFM GuideNH SFML renderer";
                default:
                    return null;
            }
        });
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;");
    }
}
