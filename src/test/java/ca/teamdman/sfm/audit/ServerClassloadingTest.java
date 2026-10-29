package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

/** The common entry points must verify without resolving client GUI/native classes. */
class ServerClassloadingTest {

    @Test
    void commonItemsProxyAndGuiHandlerLoadWithoutClientClasses() throws Exception {
        ClassLoader loader = new ClassLoader(getClass().getClassLoader()) {

            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.startsWith("vswe.") || name.startsWith("stevesaddons.")
                    || name.startsWith("net.minecraft.client.")
                    || name.startsWith("org.lwjgl.")
                    || name.equals("ca.teamdman.sfm.common.gui.GuiLabelGun")
                    || name.equals("ca.teamdman.sfm.common.gui.GuiManager"))
                    throw new ClassNotFoundException("Client class forbidden in server fixture: " + name);
                if (!name.startsWith("ca.teamdman.sfm.") && !name.startsWith("ca.teamdman.sfml."))
                    return super.loadClass(name, resolve);
                Class<?> loaded = findLoadedClass(name);
                if (loaded == null) {
                    try (InputStream input = getParent().getResourceAsStream(name.replace('.', '/') + ".class")) {
                        if (input == null) throw new ClassNotFoundException(name);
                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        byte[] buffer = new byte[4096];
                        int read;
                        while ((read = input.read(buffer)) != -1) bytes.write(buffer, 0, read);
                        byte[] code = bytes.toByteArray();
                        loaded = defineClass(name, code, 0, code.length);
                    } catch (java.io.IOException e) {
                        throw new ClassNotFoundException(name, e);
                    }
                }
                if (resolve) resolveClass(loaded);
                return loaded;
            }
        };
        for (String name : new String[] { "ca.teamdman.sfm.common.item.ItemDisk",
            "ca.teamdman.sfm.common.item.ItemLabelGun", "ca.teamdman.sfm.CommonProxy",
            "ca.teamdman.sfm.common.gui.GuiHandler", "ca.teamdman.sfm.common.recipe.ModRecipes",
            "ca.teamdman.sfm.common.net.PacketSaveProgram", "ca.teamdman.sfm.common.net.PacketDebugTerminalRequest",
            "ca.teamdman.sfm.common.net.PacketDebugTerminalReply", "ca.teamdman.sfm.common.net.PacketStationRequest",
            "ca.teamdman.sfm.common.net.PacketStationReply", "ca.teamdman.sfm.common.net.PacketStationDebugRequest",
            "ca.teamdman.sfm.common.net.PacketStationDebugReply",
            "ca.teamdman.sfm.common.tileentity.TileEntityProgrammingStation" }) {
            assertNotNull(
                Class.forName(name, true, loader)
                    .getConstructor()
                    .newInstance());
        }
        for (String name : new String[] { "ca.teamdman.sfm.common.program.DebugConsole",
            "ca.teamdman.sfm.common.program.DebugValueFormatter", "ca.teamdman.sfm.common.program.DebugTerminalService",
            "ca.teamdman.sfml.ast.PrintStatement", "ca.teamdman.sfm.common.program.project.StationProjectService" }) {
            assertNotNull(
                Class.forName(name, true, loader)
                    .getDeclaredMethods());
        }
    }
}
