package ca.teamdman.sfm.client;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;

import ca.teamdman.sfm.SuperFactoryManager;
import ca.teamdman.sfm.common.gui.GuiLabelGun;
import ca.teamdman.sfm.common.item.ModItems;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class TestAutomationController {

    private static TestAutomationController INSTANCE;
    private final int port;
    private ServerSocket serverSocket;
    private final ConcurrentLinkedQueue<TaskEntry> taskQueue = new ConcurrentLinkedQueue<>();
    private boolean autoWorldTriggered = false;
    private int ticksInMainMenu = 0;

    private static class TaskEntry {
        final Callable<String> callable;
        final CompletableFuture<String> future = new CompletableFuture<>();

        TaskEntry(Callable<String> callable) {
            this.callable = callable;
        }
    }

    public static synchronized void init() {
        if (INSTANCE == null) {
            int port = 25569;
            String portProp = System.getProperty("gtnh.harness.port");
            if (portProp != null && !portProp.isEmpty()) {
                try {
                    port = Integer.parseInt(portProp);
                } catch (NumberFormatException ignored) {}
            }
            INSTANCE = new TestAutomationController(port);
            INSTANCE.start();
        }
    }

    public TestAutomationController(int port) {
        this.port = port;
    }

    public void start() {
        FMLCommonHandler.instance().bus().register(this);
        Thread serverThread = new Thread(this::runServer, "SFM-Test-Automation-Server");
        serverThread.setDaemon(true);
        serverThread.start();
        SuperFactoryManager.LOG.info("[TestAutomation] SFM Controller initialized on port " + port);
    }

    private void runServer() {
        try {
            serverSocket = new ServerSocket(port, 10, InetAddress.getByName("127.0.0.1"));
            SuperFactoryManager.LOG.info("[TestAutomation] Listening on 127.0.0.1:" + port);
            while (!serverSocket.isClosed()) {
                Socket client = serverSocket.accept();
                handleClient(client);
            }
        } catch (IOException e) {
            if (!serverSocket.isClosed()) {
                SuperFactoryManager.LOG.error("[TestAutomation] Server socket error: " + e.getMessage());
            }
        }
    }

    private void handleClient(Socket client) {
        CompletableFuture.runAsync(() -> {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                 PrintWriter out = new PrintWriter(client.getOutputStream(), true)) {
                String line = in.readLine();
                if (line == null) return;
                line = line.trim();
                String response = processCommand(line);
                out.println(response);
            } catch (Exception e) {
                SuperFactoryManager.LOG.error("[TestAutomation] Error handling client: " + e.getMessage());
            } finally {
                try { client.close(); } catch (IOException ignored) {}
            }
        });
    }

    private String processCommand(String commandLine) {
        String[] parts = commandLine.split("\\s+", 2);
        String cmd = parts[0].toLowerCase();
        String arg = parts.length > 1 ? parts[1] : "";

        if ("ping".equals(cmd)) {
            return "pong";
        }

        CompletableFuture<String> future = executeOnClient(() -> {
            Minecraft mc = Minecraft.getMinecraft();
            switch (cmd) {
                case "status":
                    if (mc.thePlayer != null && mc.theWorld != null) {
                        String gui = mc.currentScreen != null ? mc.currentScreen.getClass().getSimpleName() : "none";
                        ItemStack held = mc.thePlayer.getHeldItem();
                        String heldName = held != null ? held.getItem().getItemStackDisplayName(held) : "empty";
                        return String.format("IN_WORLD [pos: %.1f, %.1f, %.1f, gui: %s, held: %s]",
                                mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, gui, heldName);
                    } else if (mc.currentScreen instanceof GuiMainMenu) {
                        return "MAIN_MENU";
                    } else if (mc.currentScreen != null) {
                        return "GUI: " + mc.currentScreen.getClass().getName();
                    } else {
                        return "LOADING";
                    }

                case "autoworld":
                    if (mc.thePlayer != null) {
                        return "ALREADY_IN_WORLD";
                    }
                    String worldName = arg.isEmpty() ? "TestWorld" : arg;
                    launchWorld(worldName);
                    return "LAUNCHING_WORLD: " + worldName;

                case "opengui":
                    if ("labelgun".equalsIgnoreCase(arg)) {
                        if (mc.thePlayer == null) return "ERROR: Player is null";
                        ItemStack stack = mc.thePlayer.getHeldItem();
                        if (stack == null || stack.getItem() != ModItems.labelGun) {
                            stack = new ItemStack(ModItems.labelGun);
                        }
                        mc.displayGuiScreen(new GuiLabelGun(mc.thePlayer, stack));
                        return "OPENED_LABEL_GUN_GUI";
                    } else if ("inventory".equalsIgnoreCase(arg)) {
                        if (mc.thePlayer == null) return "ERROR: Player is null";
                        mc.displayGuiScreen(new GuiInventory(mc.thePlayer));
                        return "OPENED_INVENTORY_GUI";
                    } else {
                        return "UNKNOWN_GUI: " + arg;
                    }

                case "closegui":
                    mc.gameSettings.pauseOnLostFocus = false;
                    mc.inGameHasFocus = true;
                    mc.displayGuiScreen(null);
                    return "CLOSED_GUI";

                case "slot":
                    if (mc.thePlayer == null) return "ERROR: Player is null";
                    int slot = Integer.parseInt(arg.trim());
                    mc.thePlayer.inventory.currentItem = slot;
                    ItemStack slotItem = mc.thePlayer.getHeldItem();
                    return "SLOT: " + slot + " (" + (slotItem != null ? slotItem.getDisplayName() : "empty") + ")";

                case "useitem":
                    if (mc.thePlayer == null) return "ERROR: Player is null";
                    ItemStack heldItem = mc.thePlayer.getHeldItem();
                    if (heldItem != null) {
                        heldItem.getItem().onItemRightClick(heldItem, mc.theWorld, mc.thePlayer);
                        return "USED_ITEM: " + heldItem.getDisplayName();
                    }
                    return "NO_HELD_ITEM";

                case "type":
                    if (mc.currentScreen == null) return "ERROR: No active GUI";
                    for (char c : arg.toCharArray()) {
                        try {
                            java.lang.reflect.Method km = GuiScreen.class.getDeclaredMethod("keyTyped", char.class, int.class);
                            km.setAccessible(true);
                            km.invoke(mc.currentScreen, c, 0);
                        } catch (Exception e) {
                            return "ERROR_TYPE: " + e.getMessage();
                        }
                    }
                    return "TYPED: " + arg;

                case "settext":
                    if (mc.currentScreen instanceof ca.teamdman.sfm.common.gui.GuiLabelGun) {
                        try {
                            java.lang.reflect.Field f = ca.teamdman.sfm.common.gui.GuiLabelGun.class.getDeclaredField("labelField");
                            f.setAccessible(true);
                            GuiTextField tf = (GuiTextField) f.get(mc.currentScreen);
                            tf.setText(arg);
                            java.lang.reflect.Method m = ca.teamdman.sfm.common.gui.GuiLabelGun.class.getDeclaredMethod("rebuildLabelButtons");
                            m.setAccessible(true);
                            m.invoke(mc.currentScreen);
                            return "SET_TEXT: " + arg;
                        } catch (Exception e) {
                            return "ERROR_SET_TEXT: " + e.getMessage();
                        }
                    }
                    return "NOT_LABEL_GUN_GUI";

                case "enter":
                    if (mc.currentScreen instanceof ca.teamdman.sfm.common.gui.GuiLabelGun) {
                        try {
                            java.lang.reflect.Method m = ca.teamdman.sfm.common.gui.GuiLabelGun.class.getDeclaredMethod("onDone");
                            m.setAccessible(true);
                            m.invoke(mc.currentScreen);
                            return "DONE_TRIGGERED";
                        } catch (Exception e) {
                            return "ERROR_DONE: " + e.getMessage();
                        }
                    }
                    return "NOT_LABEL_GUN_GUI";

                case "key":
                    if (mc.currentScreen == null) return "ERROR: No active GUI";
                    int keyCode = Integer.parseInt(arg.trim());
                    try {
                        java.lang.reflect.Method km = GuiScreen.class.getDeclaredMethod("keyTyped", char.class, int.class);
                        km.setAccessible(true);
                        km.invoke(mc.currentScreen, '\0', keyCode);
                    } catch (Exception e) {
                        return "ERROR_KEY: " + e.getMessage();
                    }
                    return "KEY_PRESSED: " + keyCode;

                case "chat":
                    if (mc.thePlayer == null) return "ERROR: Player is null";
                    mc.thePlayer.sendChatMessage(arg);
                    return "CHAT_SENT: " + arg;

                case "click":
                    if (mc.currentScreen == null) return "ERROR: No active GUI to click";
                    String[] coords = arg.split("\\s+");
                    int x = Integer.parseInt(coords[0]);
                    int y = Integer.parseInt(coords[1]);
                    int button = coords.length > 2 ? Integer.parseInt(coords[2]) : 0;
                    try {
                        java.lang.reflect.Method m = GuiScreen.class.getDeclaredMethod("mouseClicked", int.class, int.class, int.class);
                        m.setAccessible(true);
                        m.invoke(mc.currentScreen, x, y, button);
                    } catch (Exception e) {
                        return "ERROR_CLICK: " + e.getMessage();
                    }
                    return "CLICKED: " + x + ", " + y + " (button " + button + ")";

                case "setcode":
                    if (mc.currentScreen instanceof ca.teamdman.sfm.client.gui.GuiCodeEditor) {
                        try {
                            ca.teamdman.sfm.client.gui.GuiCodeEditor ed = (ca.teamdman.sfm.client.gui.GuiCodeEditor) mc.currentScreen;
                            java.lang.reflect.Field fLines = ca.teamdman.sfm.client.gui.GuiCodeEditor.class.getDeclaredField("lines");
                            fLines.setAccessible(true);
                            java.util.List<String> edLines = (java.util.List<String>) fLines.get(ed);
                            edLines.clear();
                            String codeText = arg.replace("\\n", "\n");
                            edLines.addAll(java.util.Arrays.asList(codeText.split("\n", -1)));
                            java.lang.reflect.Method m = ca.teamdman.sfm.client.gui.GuiCodeEditor.class.getDeclaredMethod("validateSyntax");
                            m.setAccessible(true);
                            m.invoke(ed);
                            return "CODE_SET: " + edLines.size() + " lines";
                        } catch (Exception e) {
                            return "ERROR_SET_CODE: " + e.getMessage();
                        }
                    }
                    return "NOT_CODE_EDITOR";

                case "clickslot":
                    if (mc.thePlayer != null && mc.thePlayer.openContainer != null) {
                        String[] sParts = arg.split("\\s+");
                        int slotId = Integer.parseInt(sParts[0]);
                        int mouseBtn = sParts.length > 1 ? Integer.parseInt(sParts[1]) : 0;
                        int clickMode = sParts.length > 2 ? Integer.parseInt(sParts[2]) : 0;
                        mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, slotId, mouseBtn, clickMode, mc.thePlayer);
                        return "CLICKED_SLOT: " + slotId + " (btn " + mouseBtn + ", mode " + clickMode + ")";
                    }
                    return "NO_OPEN_CONTAINER";

                case "clickbutton":
                    if (mc.currentScreen != null) {
                        try {
                            java.lang.reflect.Field f = GuiScreen.class.getDeclaredField("buttonList");
                            f.setAccessible(true);
                            java.util.List<net.minecraft.client.gui.GuiButton> btns = (java.util.List<net.minecraft.client.gui.GuiButton>) f.get(mc.currentScreen);
                            for (net.minecraft.client.gui.GuiButton b : btns) {
                                if (String.valueOf(b.id).equals(arg.trim()) || b.displayString.toLowerCase().contains(arg.trim().toLowerCase())) {
                                    java.lang.reflect.Method m = GuiScreen.class.getDeclaredMethod("actionPerformed", net.minecraft.client.gui.GuiButton.class);
                                    m.setAccessible(true);
                                    m.invoke(mc.currentScreen, b);
                                    return "CLICKED_BUTTON: " + b.id + " (" + b.displayString + ")";
                                }
                            }
                            return "BUTTON_NOT_FOUND: " + arg;
                        } catch (Exception e) {
                            return "ERROR_CLICK_BUTTON: " + e.getMessage();
                        }
                    }
                    return "NO_ACTIVE_GUI";

                case "clicklabel":
                    if (mc.currentScreen instanceof ca.teamdman.sfm.common.gui.GuiLabelGun) {
                        try {
                            ca.teamdman.sfm.common.gui.GuiLabelGun gunGui = (ca.teamdman.sfm.common.gui.GuiLabelGun) mc.currentScreen;
                            java.lang.reflect.Field f = ca.teamdman.sfm.common.gui.GuiLabelGun.class.getDeclaredField("labelBtnMap");
                            f.setAccessible(true);
                            java.util.Map<net.minecraft.client.gui.GuiButton, String> map = (java.util.Map<net.minecraft.client.gui.GuiButton, String>) f.get(gunGui);
                            for (java.util.Map.Entry<net.minecraft.client.gui.GuiButton, String> entry : map.entrySet()) {
                                if (entry.getValue().equalsIgnoreCase(arg.trim())) {
                                    java.lang.reflect.Method m = GuiScreen.class.getDeclaredMethod("actionPerformed", net.minecraft.client.gui.GuiButton.class);
                                    m.setAccessible(true);
                                    m.invoke(gunGui, entry.getKey());
                                    return "CLICKED_LABEL: " + entry.getValue();
                                }
                            }
                            return "LABEL_NOT_FOUND: " + arg;
                        } catch (Exception e) {
                            return "ERROR_CLICK_LABEL: " + e.getMessage();
                        }
                    }
                    return "NOT_LABEL_GUN_GUI";

                case "look":
                    if (mc.thePlayer == null) return "ERROR: Player is null";
                    String[] lookCoords = arg.split("\\s+");
                    mc.thePlayer.rotationYaw = Float.parseFloat(lookCoords[0]);
                    if (lookCoords.length > 1) {
                        mc.thePlayer.rotationPitch = Float.parseFloat(lookCoords[1]);
                    }
                    return "LOOK_SET: yaw=" + mc.thePlayer.rotationYaw + " pitch=" + mc.thePlayer.rotationPitch;

                case "teleport":
                    if (mc.thePlayer == null) return "ERROR: Player is null";
                    String[] tpCoords = arg.split("\\s+");
                    double tx = Double.parseDouble(tpCoords[0]);
                    double ty = Double.parseDouble(tpCoords[1]);
                    double tz = Double.parseDouble(tpCoords[2]);
                    mc.thePlayer.setPositionAndUpdate(tx, ty, tz);
                    return "TELEPORTED: " + tx + ", " + ty + ", " + tz;

                case "rightclickblock":
                    if (mc.thePlayer == null) return "ERROR: Player is null";
                    String[] rCoords = arg.split("\\s+");
                    int rx = Integer.parseInt(rCoords[0]);
                    int ry = Integer.parseInt(rCoords[1]);
                    int rz = Integer.parseInt(rCoords[2]);
                    int rside = rCoords.length > 3 ? Integer.parseInt(rCoords[3]) : 1;
                    boolean rsuccess = mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(), rx, ry, rz, rside, net.minecraft.util.Vec3.createVectorHelper(rx + 0.5, ry + 0.5, rz + 0.5));
                    return "RIGHT_CLICK_BLOCK: " + rx + "," + ry + "," + rz + " (side " + rside + ") -> " + rsuccess;

                case "leftclickblock":
                    if (mc.thePlayer == null) return "ERROR: Player is null";
                    String[] lCoords = arg.split("\\s+");
                    int lx = Integer.parseInt(lCoords[0]);
                    int ly = Integer.parseInt(lCoords[1]);
                    int lz = Integer.parseInt(lCoords[2]);
                    int lside = lCoords.length > 3 ? Integer.parseInt(lCoords[3]) : 1;
                    mc.playerController.clickBlock(lx, ly, lz, lside);
                    return "LEFT_CLICK_BLOCK: " + lx + "," + ly + "," + lz + " (side " + lside + ")";

                case "screenshot":
                    if (mc.guiAchievement != null) {
                        mc.guiAchievement.func_146257_b();
                    }
                    String destPath = arg.trim();
                    File dest = destPath.isEmpty() ? new File(mc.mcDataDir, "screenshots/test.png") : new File(destPath);
                    if (dest.getParentFile() != null) dest.getParentFile().mkdirs();
                    net.minecraft.util.ScreenShotHelper.saveScreenshot(mc.mcDataDir, dest.getName(), mc.displayWidth, mc.displayHeight, mc.getFramebuffer());
                    File created = new File(new File(mc.mcDataDir, "screenshots"), dest.getName());
                    if (created.exists() && !created.getAbsolutePath().equals(dest.getAbsolutePath())) {
                        try {
                            java.nio.file.Files.move(created.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        } catch (Exception e) {
                            return "ERROR_MOVE: " + e.getMessage();
                        }
                    }
                    return "SCREENSHOT_SAVED: " + dest.getAbsolutePath();

                default:
                    return "UNKNOWN_COMMAND: " + cmd;
            }
        });

        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            return "TIMEOUT_OR_ERROR: " + e.getMessage();
        }
    }

    private CompletableFuture<String> executeOnClient(Callable<String> callable) {
        TaskEntry entry = new TaskEntry(callable);
        taskQueue.add(entry);
        return entry.future;
    }

    private void launchWorld(String worldName) {
        Minecraft mc = Minecraft.getMinecraft();
        WorldSettings settings = new WorldSettings(0L, WorldSettings.GameType.CREATIVE, false, false, WorldType.FLAT);
        settings.enableCommands();
        mc.launchIntegratedServer(worldName, worldName, settings);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();

        TaskEntry entry;
        while ((entry = taskQueue.poll()) != null) {
            try {
                String result = entry.callable.call();
                entry.future.complete(result);
            } catch (Throwable t) {
                entry.future.completeExceptionally(t);
            }
        }

        // Permanently suppress achievement / tutorial toasts (e.g. "Press E to open your inventory")
        // to guarantee deterministic and reproducible UI testing / screenshots without toast overlays.
        if (mc.guiAchievement != null) {
            mc.guiAchievement.func_146257_b();
        }

        String autoWorld = System.getProperty("gtnh.autoworld");
        if (autoWorld != null && !autoWorldTriggered) {
            if (mc.currentScreen instanceof GuiMainMenu) {
                ticksInMainMenu++;
                if (ticksInMainMenu > 20) {
                    autoWorldTriggered = true;
                    String worldName = "true".equalsIgnoreCase(autoWorld) ? "TestWorld" : autoWorld;
                    SuperFactoryManager.LOG.info("[TestAutomation] Auto-loading world: " + worldName);
                    launchWorld(worldName);
                }
            }
        }
    }
}
