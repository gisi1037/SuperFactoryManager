package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class RecipeInspectorOptionalClientTest {

    @Test
    void inspectorLoadsAndConstructsWithoutNeiOrGregTechClasses() throws Exception {
        ClassLoader parent = getClass().getClassLoader();
        ClassLoader loader = new ClassLoader(parent) {

            @Override
            protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.startsWith("codechicken.nei.") || name.startsWith("gregtech."))
                    throw new ClassNotFoundException("Optional mod absent: " + name);
                if (!name.startsWith("ca.teamdman.sfm.")) return super.loadClass(name, resolve);
                Class<?> found = findLoadedClass(name);
                if (found == null) {
                    String resource = name.replace('.', '/') + ".class";
                    try (InputStream input = parent.getResourceAsStream(resource)) {
                        if (input == null) throw new ClassNotFoundException(name);
                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        byte[] buffer = new byte[4096];
                        int count;
                        while ((count = input.read(buffer)) != -1) bytes.write(buffer, 0, count);
                        byte[] code = bytes.toByteArray();
                        found = defineClass(name, code, 0, code.length);
                    } catch (java.io.IOException failure) {
                        throw new ClassNotFoundException(name, failure);
                    }
                }
                if (resolve) resolveClass(found);
                return found;
            }
        };
        Class<?> inspector = Class.forName("ca.teamdman.sfm.client.gui.mui2.StudioRecipeInspector", true, loader);
        assertNotNull(
            inspector.getConstructor(int.class, int.class, int.class, int.class, UUID.class)
                .newInstance(1, 2, 3, 19, UUID.randomUUID()));
    }
}
