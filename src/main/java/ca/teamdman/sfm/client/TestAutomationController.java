package ca.teamdman.sfm.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;

import com.cleanroommc.modularui.api.IMuiScreen;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ca.teamdman.sfm.Config;
import ca.teamdman.sfm.SuperFactoryManager;
import ca.teamdman.sfm.client.gui.ProgramEditSession;
import ca.teamdman.sfm.client.gui.mui2.Mui2CodeEditor;
import ca.teamdman.sfm.client.gui.mui2.SfmlCodeEditorWidget;
import ca.teamdman.sfm.common.block.ModBlocks;
import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.item.ModItems;
import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.net.ServerUiTasks;
import ca.teamdman.sfm.common.tileentity.LogLevel;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfm.integration.mui2.ManagerMui2Factory;
import ca.teamdman.sfm.integration.mui2.ManagerMui2Screen;
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
    // Server-thread-only ownership of disposable fixture blocks; never overwrite unrelated blocks.
    private final java.util.Map<net.minecraft.world.World, java.util.Set<BlockPos>> fixtureBlocks = new java.util.WeakHashMap<>();
    private BlockPos blueprintFixture;
    private net.minecraft.world.World fixtureWorld;
    private ca.teamdman.sfm.common.net.LabelGunAccess.Token qaGunToken;

    private static class TaskEntry {

        final Callable<String> callable;
        final CompletableFuture<String> future = new CompletableFuture<>();

        TaskEntry(Callable<String> callable) {
            this.callable = callable;
        }
    }

    public static synchronized void init() {
        if (!(Boolean.getBoolean("sfm.testHarness") || Boolean.getBoolean("gtnh.testHarness"))) return;
        if (INSTANCE == null) {
            int port = 25573;
            String portProp = System.getProperty("sfm.harness.port");
            if (portProp == null || portProp.isEmpty()) {
                portProp = System.getProperty("gtnh.sfm.port");
            }
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
        FMLCommonHandler.instance()
            .bus()
            .register(this);
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
            if (serverSocket == null || !serverSocket.isClosed()) {
                SuperFactoryManager.LOG.error("[TestAutomation] Server socket error: " + e.getMessage());
            }
        }
    }

    private void handleClient(Socket client) {
        CompletableFuture.runAsync(() -> {
            try (
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(client.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
                PrintWriter out = new PrintWriter(
                    new java.io.OutputStreamWriter(client.getOutputStream(), java.nio.charset.StandardCharsets.UTF_8),
                    true)) {
                String line = in.readLine();
                if (line == null) return;
                line = line.trim();
                String response = processCommand(line);
                out.println(response);
            } catch (Exception e) {
                SuperFactoryManager.LOG.error("[TestAutomation] Error handling client: " + e.getMessage());
            } finally {
                try {
                    client.close();
                } catch (IOException ignored) {}
            }
        });
    }

    private String processCommand(String commandLine) {
        String[] parts = commandLine.split("\\s+", 2);
        String cmd = parts[0].toLowerCase();
        String arg = parts.length > 1 ? parts[1] : "";

        if (!(Boolean.getBoolean("sfm.testHarness") || Boolean.getBoolean("gtnh.testHarness")))
            return "ERROR: Test harness is disabled";
        if (serverCommand(cmd)) return serverRequest(cmd, arg);

        if ("ping".equals(cmd)) {
            return "pong";
        }

        CompletableFuture<String> future = executeOnClient(() -> {
            Minecraft mc = Minecraft.getMinecraft();
            switch (cmd) {

                case "blueprintstate":
                    return blueprintState();
                case "uistate":
                    return uiState();
                case "scrollinventory":
                    return scrollInventory(arg);
                case "editorstate":
                    return editorState();
                case "stationstate":
                    return stationState();
                case "stationcomplete": {
                    Object station = stationEditor();
                    if (station == null) return "ERROR: No station editor";
                    Method complete = station.getClass()
                        .getDeclaredMethod("complete");
                    complete.setAccessible(true);
                    complete.invoke(station);
                    return "STATION_COMPLETION_REQUESTED";
                }
                case "stationcaret": {
                    Object station = stationEditor();
                    if (station == null) return "ERROR: No station editor";
                    ((SfmlCodeEditorWidget) field(station, "editor")).goTo(Integer.parseInt(arg));
                    return "STATION_CARET_SET";
                }
                case "stationcode": {
                    ca.teamdman.sfm.client.gui.mui2.Mui2ProgrammingStation station = stationEditor();
                    if (station == null || !stationEditable(station)) return "ERROR: No editable station draft";
                    ((SfmlCodeEditorWidget) field(station, "editor")).load(arg.replace("\\n", "\n"));
                    return "STATION_SOURCE_SET";
                }
                case "stationname": {
                    ca.teamdman.sfm.client.gui.mui2.Mui2ProgrammingStation station = stationEditor();
                    if (station == null || !stationEditable(station)
                        || !"sfm_station_editor".equals(
                            currentMui().getPanelManager()
                                .getTopMostPanel()
                                .getName()))
                        return "ERROR: Open the editable station draft before naming it";
                    return setMuiText(arg);
                }
                case "terminalstate":
                    return terminalState();
                case "terminalcommandwhilepoll": {
                    ModularScreen screen = currentMui();
                    if (!(screen instanceof ManagerMui2Screen) || field(screen, "terminal") == null)
                        return "ERROR: Open the manager terminal first";
                    Object terminal = field(screen, "terminal");
                    Method request = terminal.getClass()
                        .getDeclaredMethod("request", String.class);
                    request.setAccessible(true);
                    request.invoke(terminal, "");
                    setMuiText(arg);
                    return clickMuiButton("Run");
                }
                case "terminalcommand": {
                    ModularScreen screen = currentMui();
                    if (!(screen instanceof ManagerMui2Screen) || field(screen, "terminal") == null)
                        return "ERROR: Open the manager terminal first";
                    if (arg.length() > 128) return "ERROR: Terminal command exceeds 128 characters";
                    setMuiText(arg);
                    return clickMuiButton("Run");
                }
                case "savecode":
                    return clickMuiButton("Save");

                case "resolution": {
                    String[] size = arg.trim()
                        .split("\\s+");
                    int w = Integer.parseInt(size[0]), h = Integer.parseInt(size[1]);
                    if (w < 640 || h < 480 || w > 3840 || h > 2160) return "ERROR: Test resolution outside bounds";
                    org.lwjgl.opengl.Display.setDisplayMode(new org.lwjgl.opengl.DisplayMode(w, h));
                    Method resize = cpw.mods.fml.relauncher.ReflectionHelper.findMethod(
                        Minecraft.class,
                        mc,
                        new String[] { "resize", "func_71370_a" },
                        int.class,
                        int.class);
                    resize.invoke(mc, w, h);
                    return "RESOLUTION: " + w + "x" + h;
                }
                case "guiscale":
                    int requestedScale = Integer.parseInt(arg.trim());
                    if (requestedScale < 0 || requestedScale > 3) return "ERROR: Scale must be 0..3";
                    mc.gameSettings.guiScale = requestedScale;
                    net.minecraft.client.gui.ScaledResolution scaled = new net.minecraft.client.gui.ScaledResolution(
                        mc,
                        mc.displayWidth,
                        mc.displayHeight);
                    if (mc.currentScreen != null)
                        mc.currentScreen.setWorldAndResolution(mc, scaled.getScaledWidth(), scaled.getScaledHeight());
                    return "GUI_SCALE: " + mc.gameSettings.guiScale
                        + " "
                        + scaled.getScaledWidth()
                        + "x"
                        + scaled.getScaledHeight();
                // Fixed loopback target only. These run solely behind the opt-in harness guard.
                case "connectqa":
                    if (mc.theWorld != null || mc.thePlayer != null) return "ERROR: Disconnect the QA world first";
                    // Forge initializes its play-client latch here; opening GuiConnecting directly skips it.
                    cpw.mods.fml.client.FMLClientHandler.instance()
                        .connectToServer(
                            new GuiMainMenu(),
                            new net.minecraft.client.multiplayer.ServerData("SFM local QA", "127.0.0.1:25586"));
                    return "CONNECTING_QA_LOOPBACK";
                case "disconnectqa":
                    if (mc.getIntegratedServer() != null) return "ERROR: This is an integrated world; use normal quit";
                    if (mc.theWorld != null) mc.theWorld.sendQuittingDisconnectingPacket();
                    mc.loadWorld(null);
                    mc.displayGuiScreen(new GuiMainMenu());
                    return "DISCONNECTED_QA";
                case "clientinventory":
                    if (mc.thePlayer == null) return "ERROR: Player is null";
                    JsonObject client = new JsonObject();
                    JsonArray clientSlots = new JsonArray();
                    for (int i = 0; i < mc.thePlayer.inventory.getSizeInventory(); i++) {
                        ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
                        if (stack == null) continue;
                        JsonObject slot = stackSnapshot(stack);
                        slot.addProperty("slot", i);
                        clientSlots.add(slot);
                    }
                    client.add("slots", clientSlots);
                    client.add("cursor", stackSnapshot(mc.thePlayer.inventory.getItemStack()));
                    client.addProperty("windowId", mc.thePlayer.openContainer.windowId);
                    client.addProperty("heldSlot", mc.thePlayer.inventory.currentItem);
                    client.addProperty(
                        "playerId",
                        mc.thePlayer.getUniqueID()
                            .toString());
                    client.addProperty("source", "client synchronized inventory; use saved server NBT for authority");
                    return client.toString();
                case "remembergun":
                    qaGunToken = ca.teamdman.sfm.client.gui.ClientLabelGunSession.current(mc.thePlayer);
                    return qaGunToken == null ? "ERROR: No current gun session" : "REMEMBERED_GUN_SESSION";
                case "submitrememberedlabel":
                    if (qaGunToken == null || arg.length() > 256)
                        return "ERROR: Remember a session and use a bounded label";
                    // Deliberately bypass client readiness to exercise the server's stale-packet rejection.
                    ca.teamdman.sfm.common.net.SFMNetwork.WRAPPER
                        .sendToServer(new ca.teamdman.sfm.common.net.PacketSetLabel(arg, qaGunToken));
                    return "SUBMITTED_REMEMBERED_LABEL_PACKET";
                case "quit":
                    mc.shutdown();
                    return "SHUTTING_DOWN";
                case "status":
                    if (mc.thePlayer != null && mc.theWorld != null) {
                        String gui = mc.currentScreen != null ? mc.currentScreen.getClass()
                            .getSimpleName() : "none";
                        ItemStack held = mc.thePlayer.getHeldItem();
                        String heldName = held != null ? held.getItem()
                            .getItemStackDisplayName(held) : "empty";
                        return String.format(
                            "IN_WORLD [pos: %.1f, %.1f, %.1f, gui: %s, held: %s]",
                            mc.thePlayer.posX,
                            mc.thePlayer.posY,
                            mc.thePlayer.posZ,
                            gui,
                            heldName);
                    } else if (mc.currentScreen instanceof GuiMainMenu) {
                        return "MAIN_MENU";
                    } else if (mc.currentScreen != null) {
                        return "GUI: " + mc.currentScreen.getClass()
                            .getName();
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

                case "exportlogs":
                    if (!arg.isEmpty()) {
                        Config.exportLogsToFile = Boolean.parseBoolean(arg.trim());
                    }
                    return "EXPORT_LOGS_TO_FILE: " + Config.exportLogsToFile;

                case "discordbutton":
                    if (!arg.isEmpty()) {
                        Config.showDiscordButton = Boolean.parseBoolean(arg.trim());
                    }
                    return "SHOW_DISCORD_BUTTON: " + Config.showDiscordButton;

                case "opengui":
                    return openMuiGui(arg);

                case "closegui":
                    mc.gameSettings.pauseOnLostFocus = false;
                    if (currentMui() != null) currentMui().close(false);
                    else if (mc.thePlayer != null) mc.thePlayer.closeScreen();
                    return mc.currentScreen == null ? "CLOSED_GUI" : "GUI_REMAINS_OPEN";

                case "slot":
                    if (mc.thePlayer == null) return "ERROR: Player is null";
                    int slot = Integer.parseInt(arg.trim());
                    if (slot < 0 || slot >= 9) return "ERROR: Hotbar slot must be 0..8";
                    mc.thePlayer.inventory.currentItem = slot;
                    ItemStack slotItem = mc.thePlayer.getHeldItem();
                    return "SLOT: " + slot + " (" + (slotItem != null ? slotItem.getDisplayName() : "empty") + ")";

                case "useitem":
                    if (mc.thePlayer == null) return "ERROR: Player is null";
                    ItemStack heldItem = mc.thePlayer.getHeldItem();
                    if (heldItem != null) {
                        heldItem.getItem()
                            .onItemRightClick(heldItem, mc.theWorld, mc.thePlayer);
                        return "USED_ITEM: " + heldItem.getDisplayName();
                    }
                    return "NO_HELD_ITEM";

                case "type": {
                    ca.teamdman.sfm.client.gui.mui2.Mui2CodeEditor code = codeEditor();
                    if (code != null) {
                        if (!editorEditable(code)) return "ERROR: Editor is not editable";
                        codeWidget(code).insert(arg);
                        return "TYPED: " + arg;
                    }
                    com.cleanroommc.modularui.widgets.textfield.TextFieldWidget field = textField();
                    if (field == null) return "ERROR: No active MUI text field";
                    setMuiText(field.getText() + arg);
                    return "TYPED: " + arg;
                }

                case "clickdiagnostic": {
                    ModularScreen screen = currentMui();
                    if (screen == null) return "ERROR: No active MUI screen";
                    for (IWidget widget : widgets(
                        screen.getPanelManager()
                            .getTopMostPanel())) {
                        if (widget instanceof ca.teamdman.sfm.client.gui.mui2.DiagnosticStatusWidget)
                            return "DIAGNOSTIC_CLICK: "
                                + ((ca.teamdman.sfm.client.gui.mui2.DiagnosticStatusWidget) widget).onMousePressed(0);
                    }
                    return "ERROR: No diagnostic status row";
                }
                case "settext":
                    return setMuiText(arg);

                case "setfield": {
                    String[] fieldArgs = arg.split("\\s+", 2);
                    int index = Integer.parseInt(fieldArgs[0]);
                    ModularScreen screen = currentMui();
                    if (screen == null || fieldArgs.length != 2 || index < 0)
                        return "ERROR: Usage: setfield <index> <text>";
                    for (IWidget widget : widgets(
                        screen.getPanelManager()
                            .getTopMostPanel())) {
                        if (widget instanceof TextFieldWidget && index-- == 0) {
                            TextFieldWidget field = (TextFieldWidget) widget;
                            field.setText(fieldArgs[1]);
                            field.onRemoveFocus(screen.getContext());
                            return "MUI_FIELD_SET";
                        }
                    }
                    return "ERROR: Field index not found";
                }

                case "focusfield": {
                    TextFieldWidget field = textField();
                    if (field == null) return "ERROR: No active MUI text field";
                    currentMui().getContext()
                        .focus(field);
                    return "FOCUSED_MUI_TEXT_FIELD";
                }

                case "enter": {
                    com.cleanroommc.modularui.screen.ModularScreen screen = currentMui();
                    if (screen == null) return "ERROR: No active MUI screen";
                    screen.onKeyPressed('\0', org.lwjgl.input.Keyboard.KEY_RETURN);
                    screen.onKeyRelease('\0', org.lwjgl.input.Keyboard.KEY_RETURN);
                    return "ENTER_PRESSED";
                }

                case "key": {
                    com.cleanroommc.modularui.screen.ModularScreen screen = currentMui();
                    if (screen == null) return "ERROR: No active MUI screen";
                    int key = Integer.parseInt(arg.trim());
                    screen.onKeyPressed('\0', key);
                    screen.onKeyRelease('\0', key);
                    return "KEY_PRESSED: " + key;
                }

                case "modernkey":
                    if (!cpw.mods.fml.common.Loader.isModLoaded("lwjgl3ify"))
                        return "ERROR: modernkey requires the lwjgl3ify runtime";
                    return ModernInputFixture.key(arg);

                case "moderntext":
                    if (!cpw.mods.fml.common.Loader.isModLoaded("lwjgl3ify"))
                        return "ERROR: moderntext requires the lwjgl3ify runtime";
                    return ModernInputFixture.text(arg);

                case "chat":
                    if (mc.thePlayer == null) return "ERROR: Player is null";
                    mc.thePlayer.sendChatMessage(arg);
                    return "CHAT_SENT: " + arg;

                case "click": {
                    if (mc.currentScreen == null) return "ERROR: No active GUI";
                    String[] xy = arg.split("\\s+");
                    int x = Integer.parseInt(xy[0]), y = Integer.parseInt(xy[1]);
                    int mouse = xy.length > 2 ? Integer.parseInt(xy[2]) : 0;
                    if (mouse < 0 || mouse > 2) return "ERROR: Click button must be 0..2";
                    com.cleanroommc.modularui.screen.ModularScreen screen = currentMui();
                    if (screen == null) return "ERROR: No active MUI screen";
                    positionMuiMouse(screen, x, y);
                    drawMuiInputFrame(screen, x, y);
                    java.lang.reflect.Field buttonState = com.cleanroommc.modularui.screen.viewport.GuiContext.class
                        .getDeclaredField("mouseButton");
                    buttonState.setAccessible(true);
                    int previousButton = buttonState.getInt(screen.getContext());
                    try {
                        // Buttons use the argument; ItemSlot forwards the event-state button to vanilla.
                        buttonState.setInt(screen.getContext(), mouse);
                        screen.onMousePressed(mouse);
                        screen.onMouseRelease(mouse);
                    } finally {
                        buttonState.setInt(screen.getContext(), previousButton);
                    }
                    return "CLICKED: " + x + "," + y;
                }

                case "hover": {
                    ModularScreen screen = currentMui();
                    if (screen == null) return "ERROR: No active MUI screen";
                    String[] xy = arg.split("\\s+");
                    int x = Integer.parseInt(xy[0]), y = Integer.parseInt(xy[1]);
                    positionMuiMouse(screen, x, y);
                    drawMuiInputFrame(screen, x, y);
                    // Forge/NEI foreground hooks normally run after drawScreen. Exercise
                    // that phase explicitly without depending on the host OS pointer.
                    positionMuiMouse(screen, x, y);
                    screen.drawForeground();
                    Object station = stationEditor();
                    if (station == null) return "HOVERED: " + x + "," + y;
                    Object surface = field(station, "editor");
                    Object entry = field(surface, "hoverEntry");
                    return "HOVER_HELP: "
                        + (entry == null ? "none"
                            : ((ca.teamdman.sfm.client.gui.syntax.SfmlCompletionCatalog.Entry) entry).getSignature())
                        + "; error="
                        + field(surface, "hoveringError");
                }

                case "wheel": {
                    ModularScreen screen = currentMui();
                    if (screen == null) return "ERROR: No active MUI screen";
                    String[] wheelArgs = arg.split("\\s+");
                    int x = Integer.parseInt(wheelArgs[0]), y = Integer.parseInt(wheelArgs[1]);
                    int amount = Math.max(1, Math.min(120, Integer.parseInt(wheelArgs[3])));
                    positionMuiMouse(screen, x, y);
                    boolean up = "up".equalsIgnoreCase(wheelArgs[2]);
                    // Standard MUI scroll areas read the event state rather than method arguments.
                    java.lang.reflect.Field wheelState = com.cleanroommc.modularui.screen.viewport.GuiContext.class
                        .getDeclaredField("mouseWheel");
                    wheelState.setAccessible(true);
                    int previousWheel = wheelState.getInt(screen.getContext());
                    boolean used;
                    try {
                        wheelState.setInt(screen.getContext(), up ? amount : -amount);
                        used = screen.onMouseScroll(
                            up ? com.cleanroommc.modularui.api.UpOrDown.UP
                                : com.cleanroommc.modularui.api.UpOrDown.DOWN,
                            amount);
                    } finally {
                        wheelState.setInt(screen.getContext(), previousWheel);
                    }
                    return "WHEEL: " + used;
                }

                case "drag": {
                    ModularScreen screen = currentMui();
                    if (screen == null) return "ERROR: No active MUI screen";
                    String[] coordinates = arg.split("\\s+");
                    int x0 = Integer.parseInt(coordinates[0]), y0 = Integer.parseInt(coordinates[1]);
                    int x1 = Integer.parseInt(coordinates[2]), y1 = Integer.parseInt(coordinates[3]);
                    int steps = coordinates.length > 4 ? Integer.parseInt(coordinates[4]) : 10;
                    if (steps < 1 || steps > 100) return "ERROR: Drag steps must be 1..100";
                    int dragButton = coordinates.length > 5 ? Integer.parseInt(coordinates[5]) : 0;
                    if (dragButton < 0 || dragButton > 2) return "ERROR: Drag button must be 0..2";
                    positionMuiMouse(screen, x0, y0);
                    drawMuiInputFrame(screen, x0, y0);
                    java.lang.reflect.Field buttonState = com.cleanroommc.modularui.screen.viewport.GuiContext.class
                        .getDeclaredField("mouseButton");
                    buttonState.setAccessible(true);
                    int previousButton = buttonState.getInt(screen.getContext());
                    try {
                        // ItemSlot's vanilla click, distribution and release read event state as well as arguments.
                        buttonState.setInt(screen.getContext(), dragButton);
                        screen.onMousePressed(dragButton);
                        for (int i = 1; i <= steps; i++) {
                            int px = x0 + (x1 - x0) * i / steps, py = y0 + (y1 - y0) * i / steps;
                            positionMuiMouse(screen, px, py);
                            drawMuiInputFrame(screen, px, py);
                            screen.onMouseDrag(dragButton, 50L * i);
                        }
                        screen.onMouseRelease(dragButton);
                    } finally {
                        buttonState.setInt(screen.getContext(), previousButton);
                    }
                    return "DRAGGED_MUI: " + x0 + "," + y0 + " -> " + x1 + "," + y1;
                }

                case "resizegui": {
                    com.cleanroommc.modularui.screen.ModularScreen screen = currentMui();
                    if (screen == null) return "ERROR: No active MUI screen";
                    String[] size = arg.split("\\s+");
                    int width = Integer.parseInt(size[0]), height = Integer.parseInt(size[1]);
                    if (width < 300 || height < 220) return "ERROR: Editor minimum is 300x220";
                    com.cleanroommc.modularui.screen.ModularPanel panel = screen.getPanelManager()
                        .getTopMostPanel();
                    panel.size(
                        Math.min(width, screen.getScreenArea().width - 4),
                        Math.min(height, screen.getScreenArea().height - 4));
                    screen.onResize(screen.getScreenArea().width, screen.getScreenArea().height);
                    return "RESIZED_GUI: " + panel.getArea().width + "x" + panel.getArea().height;
                }

                case "autoindent": {
                    ca.teamdman.sfm.client.gui.mui2.Mui2CodeEditor code = codeEditor();
                    if (code == null || !editorEditable(code)) return "ERROR: No editable code page";
                    codeWidget(code).autoIndent();
                    return "AUTOINDENTED";
                }

                case "getcode": {
                    ca.teamdman.sfm.client.gui.mui2.Mui2CodeEditor code = codeEditor();
                    return code == null ? "NOT_CODE_EDITOR" : code.draft();
                }

                case "setcode": {
                    ca.teamdman.sfm.client.gui.mui2.Mui2CodeEditor code = codeEditor();
                    if (code == null || !editorEditable(code)) return "ERROR: No editable code page";
                    codeWidget(code).load(arg.replace("\\n", "\n"));
                    return "CODE_SET: " + code.draft()
                        .split("\n", -1).length + " lines";
                }

                case "clickslot":
                    if (mc.thePlayer != null && mc.thePlayer.openContainer != null) {
                        String[] sParts = arg.split("\\s+");
                        int slotId = resolveSlot(sParts[0]);
                        if (slotId < 0 || slotId >= mc.thePlayer.openContainer.inventorySlots.size())
                            return "ERROR: Unknown container slot";
                        int mouseBtn = sParts.length > 1 ? Integer.parseInt(sParts[1]) : 0;
                        int clickMode = sParts.length > 2 ? Integer.parseInt(sParts[2]) : 0;
                        mc.playerController.windowClick(
                            mc.thePlayer.openContainer.windowId,
                            slotId,
                            mouseBtn,
                            clickMode,
                            mc.thePlayer);
                        return "CLICKED_SLOT: " + slotId + " (btn " + mouseBtn + ", mode " + clickMode + ")";
                    }
                    return "NO_OPEN_CONTAINER";

                case "clickbutton":
                    return clickMuiButton(arg);

                case "clicklabel": {
                    String result = setMuiText(arg);
                    if (result.startsWith("ERROR")) return result;
                    com.cleanroommc.modularui.widgets.textfield.TextFieldWidget field = textField();
                    field.onKeyPressed('\0', org.lwjgl.input.Keyboard.KEY_RETURN);
                    return "CLICKED_LABEL: " + arg;
                }

                case "look":
                    if (mc.thePlayer == null) return "ERROR: Player is null";
                    String[] lookCoords = arg.split("\\s+");
                    mc.thePlayer.rotationYaw = Float.parseFloat(lookCoords[0]);
                    if (lookCoords.length > 1) {
                        mc.thePlayer.rotationPitch = Float.parseFloat(lookCoords[1]);
                    }
                    return "LOOK_SET: yaw=" + mc.thePlayer.rotationYaw + " pitch=" + mc.thePlayer.rotationPitch;

                case "rightclickblock":
                    if (mc.thePlayer == null) return "ERROR: Player is null";
                    String[] rCoords = arg.split("\\s+");
                    int rx = Integer.parseInt(rCoords[0]);
                    int ry = Integer.parseInt(rCoords[1]);
                    int rz = Integer.parseInt(rCoords[2]);
                    int rside = rCoords.length > 3 ? Integer.parseInt(rCoords[3]) : 1;
                    boolean rsuccess = mc.playerController.onPlayerRightClick(
                        mc.thePlayer,
                        mc.theWorld,
                        mc.thePlayer.getHeldItem(),
                        rx,
                        ry,
                        rz,
                        rside,
                        net.minecraft.util.Vec3.createVectorHelper(rx + 0.5, ry + 0.5, rz + 0.5));
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
                    ModularScreen screenshotScreen = currentMui();
                    if (screenshotScreen != null) {
                        // A background/minimized QA window may tick without rendering a new
                        // frame. Use the normal renderer, including foreground/font hooks,
                        // before reading its framebuffer.
                        mc.getFramebuffer()
                            .bindFramebuffer(true);
                        mc.entityRenderer.updateCameraAndRender(0);
                    }
                    if (mc.guiAchievement != null) {
                        mc.guiAchievement.func_146257_b();
                    }
                    String destPath = arg.trim();
                    File dest = destPath.isEmpty() ? new File(mc.mcDataDir, "screenshots/test.png")
                        : new File(destPath);
                    if (dest.getParentFile() != null) dest.getParentFile()
                        .mkdirs();
                    net.minecraft.util.ScreenShotHelper.saveScreenshot(
                        mc.mcDataDir,
                        dest.getName(),
                        mc.displayWidth,
                        mc.displayHeight,
                        mc.getFramebuffer());
                    File created = new File(new File(mc.mcDataDir, "screenshots"), dest.getName());
                    if (created.exists() && !created.getAbsolutePath()
                        .equals(dest.getAbsolutePath())) {
                        try {
                            java.nio.file.Files.move(
                                created.toPath(),
                                dest.toPath(),
                                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        } catch (Exception e) {
                            return "ERROR_MOVE: " + e.getMessage();
                        }
                    }
                    return dest.isFile() ? "SCREENSHOT_SAVED: " + dest.getAbsolutePath()
                        : "ERROR: Screenshot file was not created";

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

    /**
     * Opt-in synthetic SDL dispatch fixture, not OS keyboard/IME qualification. Keep this class behind the runtime
     * guard:
     * the lwjgl3ify API dependency alone does not provide its replacement Keyboard or SDL implementation.
     */
    private static final class ModernInputFixture {

        private static String key(String argument) throws Exception {
            if (currentMui() == null) return "ERROR: No active MUI screen";
            String[] args = argument.trim()
                .split("\\s+");
            if (args.length < 1 || args.length > 2 || args[0].isEmpty())
                return "ERROR: Usage: modernkey <LWJGL key name or code> [tap|press|repeat|release]";
            int key;
            try {
                key = Integer.parseInt(args[0]);
            } catch (NumberFormatException name) {
                key = org.lwjgl.input.Keyboard.getKeyIndex(args[0].toUpperCase(java.util.Locale.ROOT));
            }
            if (key <= 0 || key >= 256) return "ERROR: Unknown LWJGL key";
            String action = args.length == 1 ? "tap" : args[1].toLowerCase(java.util.Locale.ROOT);
            if (!java.util.Arrays.asList("tap", "press", "repeat", "release")
                .contains(action)) return "ERROR: Expected tap, press, repeat or release";
            Class<?> codes = Class.forName("org.lwjglx.input.KeyCodes");
            int sdlKey = (Integer) codes.getMethod("lwjglToSdlKeycode", int.class)
                .invoke(null, key);
            int sdlScan = (Integer) codes.getMethod("lwjglToSdlScancode", int.class)
                .invoke(null, key);
            if (sdlKey == 0 || sdlScan == 0) return "ERROR: Key has no SDL mapping";
            Method queue = Class.forName("org.lwjglx.input.Keyboard")
                .getMethod(
                    "addSdlKeyEvent",
                    int.class,
                    int.class,
                    me.eigenraven.lwjgl3ify.api.InputEvents.KeyAction.class,
                    int.class,
                    int.class,
                    long.class);
            long namePointer = (Long) Class.forName("org.lwjgl.sdl.SDLKeyboard")
                .getMethod("nSDL_GetKeyName", int.class)
                .invoke(null, sdlKey);
            me.eigenraven.lwjgl3ify.api.InputEvents.KeyAction eventAction = "release".equals(action)
                ? me.eigenraven.lwjgl3ify.api.InputEvents.KeyAction.RELEASED
                : "repeat".equals(action) ? me.eigenraven.lwjgl3ify.api.InputEvents.KeyAction.REPEATED
                    : me.eigenraven.lwjgl3ify.api.InputEvents.KeyAction.PRESSED;
            dispatchKey(key, sdlKey, sdlScan, eventAction, namePointer, queue);
            if ("tap".equals(action)) dispatchKey(
                key,
                sdlKey,
                sdlScan,
                me.eigenraven.lwjgl3ify.api.InputEvents.KeyAction.RELEASED,
                namePointer,
                queue);
            return "MODERN_KEY_QUEUED: " + key
                + " "
                + action
                + " (synthetic; no held-state or modifier injection; inspect after the next client tick)";
        }

        private static void dispatchKey(int key, int sdlKey, int sdlScan,
            me.eigenraven.lwjgl3ify.api.InputEvents.KeyAction action, long namePointer, Method queue) throws Exception {
            // Match Lwjgl3ifyEventLoop: notify the modern listener, then enqueue the legacy event. Do not directly
            // invoke a widget callback: Minecraft/Forge/MUI2 must consume the queue on the next input pass.
            me.eigenraven.lwjgl3ify.api.InputEvents.injectKeyEvent(
                new me.eigenraven.lwjgl3ify.api.InputEvents.KeyEvent(
                    key,
                    key,
                    sdlKey,
                    sdlScan,
                    sdlKey,
                    action,
                    (short) 0,
                    namePointer));
            queue
                .invoke(null, sdlKey, sdlScan, action, 0, sdlKey < Character.MAX_VALUE ? sdlKey : 0, System.nanoTime());
        }

        private static String text(String argument) throws Exception {
            if (currentMui() == null) return "ERROR: No active MUI screen";
            com.google.gson.JsonElement parsed = new com.google.gson.JsonParser().parse(argument);
            if (!parsed.isJsonPrimitive() || !parsed.getAsJsonPrimitive()
                .isString()) return "ERROR: Usage: moderntext <JSON string>";
            String text = parsed.getAsString();
            if (text.length() > 512) return "ERROR: moderntext is limited to 512 UTF-16 characters";
            Method queue = Class.forName("org.lwjglx.input.Keyboard")
                .getMethod("addCharEvent", int.class, int.class);
            me.eigenraven.lwjgl3ify.api.InputEvents
                .injectTextEvent(new me.eigenraven.lwjgl3ify.api.InputEvents.TextEvent(text));
            // The real SDL event loop uses String.chars(), so preserve its UTF-16 sequence, including surrogate pairs.
            for (int i = 0; i < text.length(); i++) queue.invoke(null, 0, (int) text.charAt(i));
            return "MODERN_TEXT_QUEUED: " + text.length() + " (synthetic; inspect after the next client tick)";
        }
    }

    private static boolean serverCommand(String command) {
        return java.util.Arrays
            .asList(
                "blueprintfixture",
                "redstonehistoryfixture",
                "fluidtankfixture",
                "runscenario",
                "setlabel",
                "clearlabels",
                "programmanager",
                "getmanagerstatus",
                "stepmanager",
                "setrunning",
                "settick",
                "teleport",
                "inventorystate",
                "managercode",
                "fixturegun",
                "fixtureinventory")
            .contains(command);
    }

    /** The socket thread waits; the client thread only captures identity and enqueues server work. */
    private String serverRequest(String command, String argument) {
        CompletableFuture<String> result = new CompletableFuture<>();
        try {
            executeOnClient(() -> {
                Minecraft mc = Minecraft.getMinecraft();
                final net.minecraft.server.MinecraftServer server = mc.getIntegratedServer();
                if (server == null || mc.thePlayer == null) {
                    result.complete("ERROR: Integrated test world required");
                    return "REJECTED";
                }
                final UUID playerId = mc.thePlayer.getUniqueID();
                if (!ServerUiTasks.enqueue(() -> {
                    try {
                        net.minecraft.entity.player.EntityPlayerMP player = null;
                        for (Object candidate : server.getConfigurationManager().playerEntityList) {
                            net.minecraft.entity.player.EntityPlayerMP entry = (net.minecraft.entity.player.EntityPlayerMP) candidate;
                            if (playerId.equals(entry.getUniqueID())) player = entry;
                        }
                        if (player == null) result.complete("ERROR: Server player disconnected");
                        else result.complete(serverCommandBody(command, argument, player));
                    } catch (Throwable error) {
                        result.completeExceptionally(error);
                    }
                })) result.complete("ERROR: Server UI queue is full");
                return "QUEUED";
            }).get(5, TimeUnit.SECONDS);
            return result.get(5, TimeUnit.SECONDS);
        } catch (Exception error) {
            return "SERVER_TIMEOUT_OR_ERROR: " + error;
        }
    }

    private boolean claimFixtureBlocks(net.minecraft.world.World world, int[][] coordinates) {
        java.util.Set<BlockPos> owned = fixtureBlocks.get(world);
        if (owned == null) owned = new java.util.HashSet<>();
        for (int[] xyz : coordinates) {
            BlockPos position = new BlockPos(xyz[0], xyz[1], xyz[2]);
            if (!world.isAirBlock(position.x, position.y, position.z) && !owned.contains(position)) return false;
        }
        for (int[] xyz : coordinates) owned.add(new BlockPos(xyz[0], xyz[1], xyz[2]));
        fixtureBlocks.put(world, owned);
        return true;
    }

    private String fixtureCommand(String command, String argument, net.minecraft.entity.player.EntityPlayerMP player)
        throws Exception {
        net.minecraft.world.World world = player.worldObj;
        if ("fixtureinventory".equals(command)) {
            Object shape;
            try {
                // Optional public owner-specific shape API. Never infer storage from armor,
                // a fixed vanilla count, or a private backing-array/offhand convention.
                Class<?> layout = Class.forName("dev.gilbe.dynamicinventory.inventory.InventoryLayout");
                shape = layout.getMethod("of", net.minecraft.entity.player.InventoryPlayer.class)
                    .invoke(null, player.inventory);
            } catch (ClassNotFoundException absent) {
                return "ERROR: Dynamic Inventory public shape API is absent";
            }
            int ordinary = (Integer) shape.getClass()
                .getMethod("mainSlots")
                .invoke(shape);
            int storage = (Integer) shape.getClass()
                .getMethod("storageSlots")
                .invoke(shape);
            int raw = (Integer) shape.getClass()
                .getMethod("rawIndex", int.class)
                .invoke(shape, ordinary - 1);
            if (storage < 1 || raw < 0 || raw >= player.inventory.getSizeInventory())
                return "ERROR: Invalid public inventory shape";
            ItemStack existing = player.inventory.getStackInSlot(raw);
            if (existing != null) return "ERROR: Last storage slot is occupied; existing item preserved";
            String id = UUID.randomUUID()
                .toString();
            ItemStack marker = new ItemStack(net.minecraft.init.Items.stick);
            marker.setStackDisplayName("SFM MUI2 storage endpoint marker");
            marker.getTagCompound()
                .setString("sfm:harness_endpoint", id);
            player.inventory.setInventorySlotContents(raw, marker);
            player.inventory.markDirty();
            player.inventoryContainer.detectAndSendChanges();
            if (player.openContainer != player.inventoryContainer) player.openContainer.detectAndSendChanges();
            JsonObject response = new JsonObject();
            response.addProperty("markerId", id);
            response.addProperty("storageSlots", storage);
            response.addProperty("logicalEndpoint", ordinary - 1);
            response.addProperty("rawEndpoint", raw);
            response.addProperty("serverThread", ServerUiTasks.isServerThread());
            return response.toString();
        }
        if ("teleport".equals(command)) {
            String[] xyz = argument.split("\\s+");
            player.setPositionAndUpdate(
                Double.parseDouble(xyz[0]),
                Double.parseDouble(xyz[1]),
                Double.parseDouble(xyz[2]));
            return "TELEPORTED_SERVER_PLAYER";
        }
        if ("redstonehistoryfixture".equals(command)) return redstoneHistoryFixture(player);
        if ("fluidtankfixture".equals(command))
            return FluidIntegrationFixture.run(player, positions -> claimFixtureBlocks(world, positions));
        if ("blueprintfixture".equals(command)) {
            int x = (int) Math.floor(player.posX), y = 200, z = (int) Math.floor(player.posZ);
            if (blueprintFixture != null && fixtureWorld == world) {
                x = blueprintFixture.x;
                y = blueprintFixture.y;
                z = blueprintFixture.z;
            }
            if (blueprintFixture == null || fixtureWorld != world) {
                if (!claimFixtureBlocks(world, new int[][] { { x, y, z }, { x + 2, y, z } }))
                    return "ERROR: Blueprint fixture would overwrite unrelated blocks";
                world.setBlock(x, y, z, ModBlocks.manager);
                world.setBlock(x + 2, y, z, net.minecraft.init.Blocks.stone);
                TileEntityManager manager = (TileEntityManager) world.getTileEntity(x, y, z);
                ItemStack disk = new ItemStack(ModItems.disk);
                ItemDisk.setProgramString(
                    disk,
                    "NAME \"Visual test\"\nEVERY 20 TICKS DO INPUT 8 RETAIN 2 FROM source IF dest HAS < 32 THEN OUTPUT 4 TO dest ELSE FORGET END OUTPUT 1 TO dest END");
                manager.setInventorySlotContents(0, disk);
                manager.setRunning(false);
                blueprintFixture = new BlockPos(x, y, z);
                fixtureWorld = world;
                world.markBlockForUpdate(x, y, z);
            }
            player.setPositionAndUpdate(x + 2.5, y + 1, z + .5);
            JsonObject response = new JsonObject();
            response.addProperty("x", x);
            response.addProperty("y", y);
            response.addProperty("z", z);
            response.addProperty("serverThread", ServerUiTasks.isServerThread());
            return response.toString();
        }
        if ("fixturegun".equals(command)) {
            int selected = -1;
            for (int i = 0; i < 9; i++) {
                ItemStack stack = player.inventory.getStackInSlot(i);
                if (stack != null && stack.getItem() == ModItems.labelGun) {
                    selected = i;
                    break;
                }
            }
            if (selected < 0) for (int i = 0; i < 9; i++) if (player.inventory.getStackInSlot(i) == null) {
                ItemStack gun = new ItemStack(ModItems.labelGun);
                LabelPositionHolder labels = new LabelPositionHolder();
                labels.addPosition("fixture_source", new BlockPos((int) player.posX, 200, (int) player.posZ));
                LabelPositionHolder.saveTo(gun, labels);
                player.inventory.setInventorySlotContents(i, gun);
                player.inventory.markDirty();
                selected = i;
                break;
            }
            if (selected < 0) return "ERROR: No empty hotbar slot; existing items were preserved";
            player.inventoryContainer.detectAndSendChanges();
            return "FIXTURE_GUN_SLOT: " + selected;
        }
        if ("inventorystate".equals(command) || "managercode".equals(command)) {
            String[] xyz = argument.trim()
                .split("\\s+");
            TileEntityManager manager = null;
            if (!argument.trim()
                .isEmpty()) {
                net.minecraft.tileentity.TileEntity tile = world
                    .getTileEntity(Integer.parseInt(xyz[0]), Integer.parseInt(xyz[1]), Integer.parseInt(xyz[2]));
                if (!(tile instanceof TileEntityManager)) return "ERROR: No manager at requested coordinates";
                manager = (TileEntityManager) tile;
            }
            if ("managercode".equals(command)) return manager == null ? "ERROR: Manager coordinates required"
                : java.util.Base64.getEncoder()
                    .encodeToString(
                        ItemDisk.getProgramString(manager.getDisk())
                            .getBytes(java.nio.charset.StandardCharsets.UTF_8));
            JsonObject snapshot = new JsonObject();
            JsonArray slots = new JsonArray();
            long items = 0, disks = 0;
            for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                ItemStack stack = player.inventory.getStackInSlot(i);
                if (stack == null) continue;
                JsonObject slot = stackSnapshot(stack);
                slot.addProperty("slot", i);
                slots.add(slot);
                items += stack.stackSize;
                if (stack.getItem() instanceof ItemDisk) disks += stack.stackSize;
            }
            ItemStack cursor = player.inventory.getItemStack();
            ItemStack disk = manager == null ? null : manager.getDisk();
            if (cursor != null) {
                items += cursor.stackSize;
                if (cursor.getItem() instanceof ItemDisk) disks += cursor.stackSize;
            }
            if (disk != null) {
                items += disk.stackSize;
                if (disk.getItem() instanceof ItemDisk) disks += disk.stackSize;
            }
            snapshot.add("slots", slots);
            snapshot.add("cursor", stackSnapshot(cursor));
            snapshot.add("managerDisk", stackSnapshot(disk));
            snapshot.addProperty("itemCount", items);
            snapshot.addProperty("diskCount", disks);
            snapshot.addProperty("windowId", player.openContainer.windowId);
            snapshot.addProperty("serverThread", ServerUiTasks.isServerThread());
            return snapshot.toString();
        }
        return "UNKNOWN_SERVER_COMMAND: " + command;
    }

    private static JsonObject stackSnapshot(ItemStack stack) {
        JsonObject result = new JsonObject();
        result.addProperty("count", stack == null ? 0 : stack.stackSize);
        if (stack != null) {
            result.addProperty(
                "item",
                String.valueOf(net.minecraft.item.Item.itemRegistry.getNameForObject(stack.getItem())));
            result.addProperty("damage", stack.getItemDamage());
            result.addProperty(
                "nbt",
                stack.hasTagCompound() ? stack.getTagCompound()
                    .toString() : "");
        }
        return result;
    }

    private static ModularScreen currentMui() {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        return screen instanceof IMuiScreen ? ((IMuiScreen) screen).getScreen() : null;
    }

    private static Object field(Object owner, String name) throws Exception {
        if (owner == null) return null;
        for (Class<?> type = owner.getClass(); type != null; type = type.getSuperclass()) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                return field.get(owner);
            } catch (NoSuchFieldException absent) { /* Continue through MUI wrapper subclasses. */ }
        }
        throw new NoSuchFieldException(name);
    }

    private static Mui2CodeEditor codeEditor() throws Exception {
        ModularScreen screen = currentMui();
        return screen instanceof ManagerMui2Screen ? (Mui2CodeEditor) field(screen, "text") : null;
    }

    private static SfmlCodeEditorWidget codeWidget(Mui2CodeEditor editor) throws Exception {
        return (SfmlCodeEditorWidget) field(editor, "editor");
    }

    private static boolean editorEditable(Mui2CodeEditor editor) throws Exception {
        Method method = Mui2CodeEditor.class.getDeclaredMethod("editable");
        method.setAccessible(true);
        return (Boolean) method.invoke(editor);
    }

    private static List<IWidget> widgets(ModularPanel panel) {
        List<IWidget> result = new ArrayList<>();
        ArrayDeque<IWidget> queue = new ArrayDeque<>();
        queue.add(panel);
        while (!queue.isEmpty()) {
            IWidget widget = queue.removeFirst();
            if (!widget.isEnabled()) continue;
            result.add(widget);
            queue.addAll(widget.getChildren());
        }
        return result;
    }

    private static void positionMuiMouse(ModularScreen screen, int x, int y) {
        // Hover discovery uses the context's previous rendered position. Set it before
        // discovering hovered widgets so a scripted click reaches the requested surface.
        screen.getContext()
            .updateState(x, y, 0);
        screen.onFrameUpdate();
    }

    private static void drawMuiInputFrame(ModularScreen screen, int x, int y) {
        // Forge normally intercepts and replaces the wrapper's vanilla drawScreen.
        // Use that MUI path so vanilla release/drag sees the transformed hovered slot.
        com.cleanroommc.modularui.screen.ClientScreenHandler
            .drawScreen(screen, Minecraft.getMinecraft().currentScreen, x, y, 0);
    }

    private static TextFieldWidget textField() {
        ModularScreen screen = currentMui();
        if (screen != null) for (IWidget widget : widgets(
            screen.getPanelManager()
                .getTopMostPanel()))
            if (widget instanceof TextFieldWidget) return (TextFieldWidget) widget;
        return null;
    }

    private static String setMuiText(String text) {
        TextFieldWidget field = textField();
        if (field == null) return "ERROR: No active MUI text field";
        field.setText(text);
        field.onRemoveFocus(currentMui().getContext());
        return "SET_TEXT: " + text;
    }

    private static String clickMuiButton(String requested) {
        ModularScreen screen = currentMui();
        if (screen == null) return "ERROR: No active MUI screen";
        String target = requested.trim();
        String[] oldIds = { "", "paste", "text", "example", "discord", "copy", "logs", "rebuild", "config", "reset",
            "diagnostics", "visual", "reset_state", "run" };
        if (screen.getPanelManager()
            .getTopMostPanel() == screen.getMainPanel() && target.matches("[0-9]+")) {
            int id = Integer.parseInt(target);
            if (id > 0 && id < oldIds.length) target = "action_" + oldIds[id];
        }
        List<IWidget> candidates = widgets(
            screen.getPanelManager()
                .getTopMostPanel());
        for (boolean exact : new boolean[] { true, false }) for (IWidget widget : candidates) {
            if (!(widget instanceof ButtonWidget)) continue;
            ButtonWidget<?> button = (ButtonWidget<?>) widget;
            String label = button.getOverlay() instanceof IKey ? ((IKey) button.getOverlay()).getFormatted()
                .replaceAll("\u00a7.", "") : "";
            String name = button.getName() == null ? "" : button.getName();
            boolean matched = exact ? target.equalsIgnoreCase(label) || target.equalsIgnoreCase(name)
                : label.toLowerCase(java.util.Locale.ROOT)
                    .contains(target.toLowerCase(java.util.Locale.ROOT));
            if (matched) {
                button.onMousePressed(0);
                button.onMouseRelease(0);
                return "CLICKED_MUI_BUTTON: " + name + " " + label;
            }
        }
        return "BUTTON_NOT_FOUND: " + requested;
    }

    private static String openMuiGui(String argument) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return "ERROR: Player is not in a world";
        String[] parameters = argument.trim()
            .split("\\s+");
        String page = parameters[0].toLowerCase(java.util.Locale.ROOT);
        if ("inventory".equals(page)) {
            mc.displayGuiScreen(new GuiInventory(mc.thePlayer));
            return "OPENED_INVENTORY_GUI";
        }
        if ("station".equals(page)) {
            if (parameters.length != 4) return "ERROR: Usage: opengui station <x> <y> <z>";
            int x = Integer.parseInt(parameters[1]), y = Integer.parseInt(parameters[2]),
                z = Integer.parseInt(parameters[3]);
            if (!(mc.theWorld
                .getTileEntity(x, y, z) instanceof ca.teamdman.sfm.common.tileentity.TileEntityProgrammingStation))
                return "ERROR: Programming Station has not synced to the client";
            com.cleanroommc.modularui.factory.GuiManager.openFromClient(
                ca.teamdman.sfm.integration.mui2.ProgrammingStationMui2Factory.INSTANCE,
                new ca.teamdman.sfm.integration.mui2.ProgrammingStationMui2Factory.Data(mc.thePlayer, x, y, z));
            return "REQUESTED_MUI_STATION";
        }
        if ("labelgun".equals(page)) {
            ItemStack held = mc.thePlayer.getHeldItem();
            if (held == null || held.getItem() != ModItems.labelGun)
                return "ERROR: Hold a real label gun (fixturegun supplies one safely)";
            ca.teamdman.sfm.client.gui.mui2.Mui2LabelGun.open(mc.thePlayer, held);
            return "OPENED_MUI_LABEL_GUN";
        }
        if ("manager".equals(page)) {
            if (parameters.length != 4) return "ERROR: Usage: opengui manager <x> <y> <z>";
            int x = Integer.parseInt(parameters[1]), y = Integer.parseInt(parameters[2]),
                z = Integer.parseInt(parameters[3]);
            if (!(mc.theWorld.getTileEntity(x, y, z) instanceof TileEntityManager))
                return "ERROR: Manager has not synced to the client";
            com.cleanroommc.modularui.factory.GuiManager.openFromClient(
                ManagerMui2Factory.INSTANCE,
                new ManagerMui2Factory.ManagerGuiData(mc.thePlayer, x, y, z));
            return "REQUESTED_MUI_MANAGER";
        }
        if ("editor".equals(page)) page = "text";
        if (!(currentMui() instanceof ManagerMui2Screen)) return "ERROR: Open the manager container first";
        if (!java.util.Arrays.asList("text", "visual", "logs", "terminal")
            .contains(page)) return "UNKNOWN_GUI: " + argument;
        ((ManagerMui2Screen) currentMui()).openPage(page);
        return "REQUESTED_MUI_PAGE: " + page;
    }

    private static String uiState() {
        Minecraft mc = Minecraft.getMinecraft();
        ModularScreen screen = currentMui();
        JsonObject result = new JsonObject();
        result.addProperty(
            "wrapper",
            mc.currentScreen == null ? "none"
                : mc.currentScreen.getClass()
                    .getName());
        result.addProperty("displayWidth", mc.displayWidth);
        result.addProperty("displayHeight", mc.displayHeight);
        result.addProperty("guiScale", mc.gameSettings.guiScale);
        if (mc.thePlayer != null) {
            result.addProperty("playerInventorySize", mc.thePlayer.inventory.getSizeInventory());
            result.addProperty("mainInventoryArrayLength", mc.thePlayer.inventory.mainInventory.length);
        }
        if (screen == null) return result.toString();
        result.addProperty(
            "screen",
            screen.getClass()
                .getName());
        result.addProperty("scaledWidth", screen.getScreenArea().width);
        result.addProperty("scaledHeight", screen.getScreenArea().height);
        result.addProperty("windowId", mc.thePlayer.openContainer.windowId);
        result.addProperty("slots", mc.thePlayer.openContainer.inventorySlots.size());
        result.addProperty("managerDiskSlot", resolveSlot("disk"));
        boolean optionalInventory = false;
        for (IWidget widget : widgets(screen.getMainPanel())) {
            if ("dev.gilbe.dynamicinventory.compat.mui2.PlayerInventoryWidgetGroup".equals(
                widget.getClass()
                    .getName())) {
                optionalInventory = true;
                result.addProperty(
                    "playerInventoryWidgetClass",
                    widget.getClass()
                        .getName());
            }
        }
        result.addProperty("optionalInventoryWidget", optionalInventory);
        com.cleanroommc.modularui.widget.AbstractScrollWidget<?, ?> inventoryScroll = inventoryScroller();
        if (inventoryScroll != null) result.add("inventoryScroller", scrollState(inventoryScroll));
        JsonArray panels = new JsonArray();
        boolean inside = true;
        for (ModularPanel panel : screen.getPanelManager()
            .getOpenPanels()) {
            JsonObject area = new JsonObject();
            area.addProperty("name", panel.getName());
            area.addProperty("x", panel.getArea().x);
            area.addProperty("y", panel.getArea().y);
            area.addProperty("width", panel.getArea().width);
            area.addProperty("height", panel.getArea().height);
            inside &= panel.getArea().x >= 0 && panel.getArea().y >= 0
                && panel.getArea().x + panel.getArea().width <= screen.getScreenArea().width
                && panel.getArea().y + panel.getArea().height <= screen.getScreenArea().height;
            panels.add(area);
        }
        result.add("panels", panels);
        result.addProperty("panelsInsideScreen", inside);
        return result.toString();
    }

    private static com.cleanroommc.modularui.widget.AbstractScrollWidget<?, ?> inventoryScroller() {
        ModularScreen screen = currentMui();
        if (screen == null) return null;
        for (IWidget widget : widgets(screen.getMainPanel())) {
            if (("dev.gilbe.dynamicinventory.compat.mui2.InventoryScrollWidget".equals(
                widget.getClass()
                    .getName())
                || "ca.teamdman.sfm.integration.mui2.ManagerPlayerInventory$StorageViewport".equals(
                    widget.getClass()
                        .getName()))
                && widget instanceof com.cleanroommc.modularui.widget.AbstractScrollWidget)
                return (com.cleanroommc.modularui.widget.AbstractScrollWidget<?, ?>) widget;
        }
        return null;
    }

    private static String scrollInventory(String destination) {
        if (!"top".equalsIgnoreCase(destination.trim()) && !"bottom".equalsIgnoreCase(destination.trim()))
            return "ERROR: Usage: scrollinventory top|bottom";
        com.cleanroommc.modularui.widget.AbstractScrollWidget<?, ?> widget = inventoryScroller();
        if (widget == null) return "ERROR: No optional player inventory scroller is open";
        com.cleanroommc.modularui.widget.scroll.ScrollArea area = widget.getScrollArea();
        com.cleanroommc.modularui.widget.scroll.VerticalScrollData scroll = area.getScrollY();
        if (scroll == null) return "ERROR: Inventory viewport has no vertical scroll data";
        int maximum = Math.max(0, scroll.getScrollSize() - scroll.getFullVisibleSize(area));
        scroll.scrollTo(area, "bottom".equalsIgnoreCase(destination.trim()) ? maximum : 0);
        return scrollState(widget).toString();
    }

    private static JsonObject scrollState(com.cleanroommc.modularui.widget.AbstractScrollWidget<?, ?> widget) {
        com.cleanroommc.modularui.widget.scroll.ScrollArea area = widget.getScrollArea();
        com.cleanroommc.modularui.widget.scroll.VerticalScrollData scroll = area.getScrollY();
        JsonObject state = new JsonObject();
        state.addProperty(
            "class",
            widget.getClass()
                .getName());
        state.addProperty("x", area.x);
        state.addProperty("y", area.y);
        state.addProperty("width", area.width);
        state.addProperty("height", area.height);
        if (scroll == null) return state;
        int visible = scroll.getFullVisibleSize(area);
        int maximum = Math.max(0, scroll.getScrollSize() - visible);
        state.addProperty("scrollSize", scroll.getScrollSize());
        state.addProperty("visibleSize", visible);
        state.addProperty("offsetY", scroll.getScroll());
        state.addProperty("maxY", maximum);
        state.addProperty("atTop", scroll.getScroll() == 0);
        state.addProperty("atBottom", scroll.getScroll() == maximum);
        JsonArray visibleSlots = new JsonArray();
        boolean markerVisible = false;
        for (IWidget child : widget.getChildren()) {
            if (!(child instanceof com.cleanroommc.modularui.widgets.slot.ItemSlot)) continue;
            int x = child.getArea().rx - widget.getScrollX();
            int y = child.getArea().ry - scroll.getScroll();
            if (x + child.getArea().width <= 0 || x >= area.width || y + child.getArea().height <= 0 || y >= visible)
                continue;
            com.cleanroommc.modularui.widgets.slot.ItemSlot item = (com.cleanroommc.modularui.widgets.slot.ItemSlot) child;
            JsonObject slot = new JsonObject();
            slot.addProperty("name", child.getName());
            slot.addProperty("visibleY", y);
            if (item.getSlot() != null) {
                slot.addProperty("containerSlot", item.getSlot().slotNumber);
                ItemStack stack = item.getSlot()
                    .getStack();
                slot.add("item", stackSnapshot(stack));
                if (stack != null && stack.hasTagCompound()
                    && stack.getTagCompound()
                        .hasKey("sfm:harness_endpoint")) {
                    slot.addProperty(
                        "markerId",
                        stack.getTagCompound()
                            .getString("sfm:harness_endpoint"));
                    markerVisible = true;
                }
            }
            visibleSlots.add(slot);
        }
        state.add("visibleStorageSlots", visibleSlots);
        state.addProperty("endpointMarkerVisible", markerVisible);
        return state;
    }

    private static int resolveSlot(String requested) {
        net.minecraft.inventory.Container container = Minecraft.getMinecraft().thePlayer.openContainer;
        if ("disk".equalsIgnoreCase(requested) || "playerdisk".equalsIgnoreCase(requested)) {
            boolean manager = "disk".equalsIgnoreCase(requested);
            for (int i = 0; i < container.inventorySlots.size(); i++) {
                net.minecraft.inventory.Slot slot = container.getSlot(i);
                boolean managerSlot = slot instanceof ca.teamdman.sfm.integration.mui2.ManagerDiskSlot;
                ItemStack stack = slot.getStack();
                if (manager && managerSlot
                    || !manager && !managerSlot && stack != null && stack.getItem() instanceof ItemDisk) return i;
            }
            return -1;
        }
        return Integer.parseInt(requested);
    }

    private static String editorState() throws Exception {
        Mui2CodeEditor editor = codeEditor();
        if (editor == null) return "NOT_CODE_EDITOR";
        ProgramEditSession session = (ProgramEditSession) field(editor, "session");
        JsonObject state = new JsonObject();
        state.addProperty("status", String.valueOf(field(editor, "status")));
        state.addProperty("pending", session.state.isPending());
        state.addProperty("editable", editorEditable(editor));
        state.addProperty("saving", editor.isSaving());
        state.addProperty("caret", codeWidget(editor).buffer.caret());
        state.addProperty("line", codeWidget(editor).line());
        state.addProperty("column", codeWidget(editor).column());
        state.addProperty("hasDraft", editor.hasDraft());
        state.addProperty("action", String.valueOf(session.state.action()));
        state.addProperty("loaded", session.state.snapshot() != null);
        if (session.state.snapshot() != null) {
            state.addProperty("revision", session.state.snapshot().revision);
            state.addProperty(
                "draftMatchesSnapshot",
                editor.draft()
                    .equals(session.state.snapshot().source));
        }
        return state.toString();
    }

    private static ca.teamdman.sfm.client.gui.mui2.Mui2ProgrammingStation stationEditor() throws Exception {
        ModularScreen screen = currentMui();
        return screen instanceof ca.teamdman.sfm.integration.mui2.ProgrammingStationScreen
            ? (ca.teamdman.sfm.client.gui.mui2.Mui2ProgrammingStation) field(screen, "editor")
            : null;
    }

    private static boolean stationEditable(ca.teamdman.sfm.client.gui.mui2.Mui2ProgrammingStation editor)
        throws Exception {
        Method editable = editor.getClass()
            .getDeclaredMethod("editable");
        editable.setAccessible(true);
        return (Boolean) editable.invoke(editor);
    }

    /** Read-only client acknowledgement snapshot; mutations still use the real station UI and packets. */
    private static String stationState() throws Exception {
        ca.teamdman.sfm.client.gui.mui2.Mui2ProgrammingStation editor = stationEditor();
        if (editor == null) return "NOT_STATION_EDITOR";
        ca.teamdman.sfm.client.gui.ProgrammingStationDraftState draft = (ca.teamdman.sfm.client.gui.ProgrammingStationDraftState) field(
            editor,
            "state");
        JsonObject state = new JsonObject();
        state.addProperty("name", draft.name);
        state.addProperty("source", draft.source);
        if (draft.publication != null) {
            JsonObject publication = new JsonObject();
            publication.addProperty("revision", draft.publication.revision);
            publication.addProperty("digest", draft.publication.digest);
            state.add("publication", publication);
        }
        ca.teamdman.sfm.common.program.project.PublicationPage builds = (ca.teamdman.sfm.common.program.project.PublicationPage) field(
            editor,
            "publications");
        JsonArray published = new JsonArray();
        for (ca.teamdman.sfm.common.program.project.PublicationReceipt entry : builds.entries) {
            JsonObject build = new JsonObject();
            build.addProperty("revision", entry.revision);
            build.addProperty("digest", entry.digest);
            build.addProperty("name", entry.name);
            published.add(build);
        }
        state.add("publications", published);
        state.addProperty("publicationTotal", builds.total);
        state.addProperty("publicationOffset", builds.offset);
        state.addProperty("dirty", draft.dirty());
        ca.teamdman.sfm.client.gui.mui2.SfmlCodeEditorWidget surface = (ca.teamdman.sfm.client.gui.mui2.SfmlCodeEditorWidget) field(
            editor,
            "editor");
        state.addProperty("caret", surface.buffer.caret());
        state.addProperty("line", surface.line());
        state.addProperty("column", surface.column());
        state.addProperty("pending", draft.awaitingReply());
        state.addProperty("busy", draft.busy());
        state.addProperty("editable", stationEditable(editor));
        state.addProperty("view", String.valueOf(field(editor, "view")));
        state.addProperty("inspected", String.valueOf(field(editor, "inspected")));
        state.addProperty("clause", String.valueOf(field(editor, "clause")));
        state.addProperty(
            "mouseX",
            currentMui().getContext()
                .getAbsMouseX());
        state.addProperty(
            "mouseY",
            currentMui().getContext()
                .getAbsMouseY());
        ca.teamdman.sfm.client.blueprint.StudioDocument studio = (ca.teamdman.sfm.client.blueprint.StudioDocument) field(
            editor,
            "document");
        state.addProperty("code", studio.code());
        state.addProperty("visualIncomplete", studio.incomplete());
        state.addProperty("visualProblem", studio.problem());
        state.addProperty("draftDiagnostic", String.valueOf(field(editor, "draftDiagnostic")));
        Object graphWidget = field(editor, "graphEditor");
        if (graphWidget instanceof IWidget) {
            IWidget surfaceWidget = (IWidget) graphWidget;
            JsonObject graphView = new JsonObject();
            graphView.addProperty("x", surfaceWidget.getArea().x);
            graphView.addProperty("y", surfaceWidget.getArea().y);
            graphView.addProperty("width", surfaceWidget.getArea().width);
            graphView.addProperty("height", surfaceWidget.getArea().height);
            double zoom = (Double) field(graphWidget, "zoom"), panX = (Double) field(graphWidget, "panX"),
                panY = (Double) field(graphWidget, "panY");
            graphView.addProperty("zoom", zoom);
            JsonArray nodes = new JsonArray();
            if (studio.graph() != null) for (ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node node : studio.graph()
                .nodes()) {
                    JsonObject entry = new JsonObject();
                    entry.addProperty("id", node.id.toString());
                    entry.addProperty("type", node.type.name());
                    entry.addProperty("clause", node.clause());
                    entry.addProperty("x", surfaceWidget.getArea().x + panX + node.x() * zoom);
                    entry.addProperty("y", surfaceWidget.getArea().y + panY + node.y() * zoom);
                    nodes.add(entry);
                }
            graphView.add("nodes", nodes);
            state.add("graphView", graphView);
        }
        state.addProperty("uncertainWrite", draft.uncertainWrite);
        state.addProperty("ignoredLateLoad", draft.ignoredLateLoad);
        state.addProperty(
            "action",
            draft.action() == null ? "none"
                : draft.action()
                    .name());
        state.addProperty("requestId", (Long) field(draft, "requestId"));
        state.addProperty("status", String.valueOf(field(editor, "status")));
        state.addProperty("revisionLine", draft.revisionLine());
        state.addProperty("windowId", (Integer) field(editor, "windowId"));
        state.addProperty("sessionId", String.valueOf(field(editor, "sessionId")));
        state.addProperty(
            "topPanel",
            currentMui().getPanelManager()
                .getTopMostPanel()
                .getName());
        if (draft.saved != null) {
            JsonObject saved = new JsonObject();
            saved.addProperty("id", draft.saved.id.toString());
            saved.addProperty("owner", draft.saved.owner.toString());
            saved.addProperty("revision", draft.saved.revision);
            saved.addProperty("name", draft.saved.name);
            saved.addProperty("source", draft.saved.source);
            state.add("saved", saved);
        }
        if (draft.validation != null) {
            JsonObject validation = new JsonObject();
            validation.addProperty("id", draft.validation.id.toString());
            validation.addProperty("revision", draft.validation.revision);
            validation.addProperty("valid", draft.validation.valid);
            validation.addProperty("message", draft.validation.message);
            state.add("validation", validation);
        }
        ca.teamdman.sfm.common.program.project.ProjectPage page = (ca.teamdman.sfm.common.program.project.ProjectPage) field(
            editor,
            "catalog");
        state.addProperty("catalogTotal", page.total);
        state.addProperty("catalogOffset", page.offset);
        JsonArray entries = new JsonArray();
        for (ca.teamdman.sfm.common.program.project.ProjectPage.Entry entry : page.entries) {
            JsonObject value = new JsonObject();
            value.addProperty("id", entry.id.toString());
            value.addProperty("name", entry.name);
            value.addProperty("revision", entry.revision);
            entries.add(value);
        }
        state.add("catalog", entries);
        return state.toString();
    }

    private static String terminalState() throws Exception {
        ModularScreen screen = currentMui();
        Object terminal = screen instanceof ManagerMui2Screen
            || screen instanceof ca.teamdman.sfm.integration.mui2.ProgrammingStationScreen ? field(screen, "terminal")
                : null;
        if (terminal == null) return "NOT_DEBUG_TERMINAL";
        JsonObject state = new JsonObject();
        for (String name : new String[] { "pending", "capturing", "showResult", "cancelled" })
            state.addProperty(name, (Boolean) field(terminal, name));
        if (terminal instanceof ca.teamdman.sfm.client.gui.mui2.StudioDebugTerminal)
            state.addProperty("connected", (Boolean) field(terminal, "connected"));
        state.addProperty("dropped", (Long) field(terminal, "dropped"));
        state.addProperty("notice", String.valueOf(field(terminal, "notice")));
        state.addProperty("command", String.valueOf(field(terminal, "command")));
        for (String name : new String[] { "history", "result" }) {
            JsonArray lines = new JsonArray();
            for (Object line : (List<?>) field(terminal, name))
                lines.add(new com.google.gson.JsonPrimitive((String) line));
            state.add(name, lines);
        }
        return state.toString();
    }

    private static String blueprintState() throws Exception {
        ModularScreen screen = currentMui();
        Object visual = screen instanceof ManagerMui2Screen ? field(screen, "visual") : null;
        if (visual == null) return "NOT_BLUEPRINT_EDITOR";
        JsonObject state = new JsonObject();
        for (String name : new String[] { "status", "valid", "dirty", "selected", "zoom", "panX", "panY" })
            state.addProperty(name, String.valueOf(field(visual, name)));
        ProgramEditSession session = (ProgramEditSession) field(visual, "editSession");
        state.addProperty("pending", session.state.isPending());
        ca.teamdman.sfm.client.blueprint.BlueprintGraph graph = (ca.teamdman.sfm.client.blueprint.BlueprintGraph) field(
            visual,
            "graph");
        state.addProperty(
            "nodes",
            graph == null ? 0
                : graph.nodes()
                    .size());
        JsonArray positions = new JsonArray();
        if (graph != null) for (ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node node : graph.nodes()) {
            JsonObject position = new JsonObject();
            position.addProperty("id", node.id.toString());
            position.addProperty("type", node.type.name());
            position.addProperty("clause", node.clause());
            position.addProperty("x", node.x());
            position.addProperty("y", node.y());
            positions.add(position);
        }
        state.add("nodeStates", positions);
        return state.toString();
    }

    private String serverCommandBody(String cmd, String arg, net.minecraft.entity.player.EntityPlayerMP player)
        throws Exception {
        switch (cmd) {
            case "runscenario": {
                int scen = Integer.parseInt(arg.trim());
                boolean prevExport = Config.exportLogsToFile;
                try {
                    Config.exportLogsToFile = true;
                    File logF = new File("logs/sfm_server_export.log");
                    if (logF.exists()) logF.delete();

                    net.minecraft.world.World ws = null;
                    if (net.minecraft.server.MinecraftServer.getServer() != null) {
                        ws = net.minecraft.server.MinecraftServer.getServer()
                            .worldServerForDimension(player.dimension);
                    }
                    if (ws == null) return "ERROR: No world";

                    int bx = 500, by = 60, bz = 0;
                    BlockPos c1 = new BlockPos(bx + 1, by, bz);
                    BlockPos c2 = new BlockPos(bx + 2, by, bz);
                    BlockPos posA = new BlockPos(bx + 1, by + 1, bz);
                    BlockPos posB = new BlockPos(bx + 2, by + 1, bz);

                    if (!claimFixtureBlocks(
                        ws,
                        new int[][] { { bx, by, bz }, { bx + 1, by, bz }, { bx + 2, by, bz },
                            { posA.x, posA.y, posA.z }, { posB.x, posB.y, posB.z } }))
                        return "ERROR: Scenario fixture would overwrite unrelated blocks";
                    ws.setBlock(bx, by, bz, ModBlocks.manager);
                    ws.setBlock(c1.x, c1.y, c1.z, ModBlocks.cable);
                    ws.setBlock(c2.x, c2.y, c2.z, ModBlocks.cable);
                    ws.setBlock(posA.x, posA.y, posA.z, net.minecraft.init.Blocks.chest);
                    ws.setBlock(posB.x, posB.y, posB.z, net.minecraft.init.Blocks.chest);

                    net.minecraft.tileentity.TileEntityChest cIn = (net.minecraft.tileentity.TileEntityChest) ws
                        .getTileEntity(posA.x, posA.y, posA.z);
                    net.minecraft.tileentity.TileEntityChest cOut = (net.minecraft.tileentity.TileEntityChest) ws
                        .getTileEntity(posB.x, posB.y, posB.z);
                    if (cIn != null) {
                        for (int i = 0; i < cIn.getSizeInventory(); i++) cIn.setInventorySlotContents(i, null);
                    }
                    if (cOut != null) {
                        for (int i = 0; i < cOut.getSizeInventory(); i++) cOut.setInventorySlotContents(i, null);
                    }

                    String pCode;
                    LogLevel pLvl;
                    if (scen == 1) {
                        if (cIn != null)
                            cIn.setInventorySlotContents(0, new ItemStack(net.minecraft.init.Items.apple, 64));
                        pCode = "NAME \"Test 1\"\nEVERY 20 TICKS DO\n  INPUT FROM a\n  OUTPUT TO b\nEND\n";
                        pLvl = LogLevel.TRACE;
                    } else if (scen == 2) {
                        if (cIn != null)
                            cIn.setInventorySlotContents(0, new ItemStack(net.minecraft.init.Items.apple, 64));
                        pCode = "NAME \"Test 2\"\nEVERY 20 TICKS DO\n  IF a HAS >= 1 THEN\n    INPUT FROM a\n    OUTPUT TO b\n  END\nEND\n";
                        pLvl = LogLevel.DEBUG;
                    } else if (scen == 4) {
                        if (cIn != null)
                            cIn.setInventorySlotContents(0, new ItemStack(net.minecraft.init.Items.apple, 64));
                        pCode = "NAME \"Manual data demo\"\nSTATE $stats = { runs: 0 }\nEVERY 20 TICKS DO\n"
                            + "  LET $amounts = [1, 2, 4]\n  LET $config = { reserve: 8, target: 16 }\n"
                            + "  SET $stats.runs = $stats.runs + 1\n"
                            + "  INPUT ($amounts[2]) RETAIN ($config.reserve) FROM a\n"
                            + "  OUTPUT RETAIN ($config.target) TO b\nEND";
                        pLvl = LogLevel.OFF;
                    } else {
                        pCode = "NAME \"Test 3\"\nEVERY 20 TICKS DO\n  INPUT FROM a\n  OUTPUT TO b\nEND\n";
                        pLvl = LogLevel.DEBUG;
                    }

                    TileEntityManager mgr = (TileEntityManager) ws.getTileEntity(bx, by, bz);
                    if (mgr == null) return "ERROR: No manager tile entity at " + bx + "," + by + "," + bz;

                    ItemStack dStack = new ItemStack(ModItems.disk);
                    LabelPositionHolder lpHolder = new LabelPositionHolder();
                    lpHolder.addPosition("a", posA);
                    lpHolder.addPosition("b", posB);
                    LabelPositionHolder.saveTo(dStack, lpHolder);
                    ItemDisk.setProgramString(dStack, pCode);
                    mgr.setInventorySlotContents(0, dStack);
                    mgr.recompile(pCode);
                    mgr.setLogLevel(pLvl);
                    mgr.setManagerTick(19);
                    if (scen == 4) mgr.setRunning(false);

                    mgr.updateEntity();
                    return "SCENARIO_RAN: " + scen;
                } finally {
                    Config.exportLogsToFile = prevExport;
                }
            }

            case "setlabel": {
                String[] sp = arg.trim()
                    .split("\\s+");
                if (sp.length < 7) return "ERROR: Usage: setlabel <mx> <my> <mz> <label> <tx> <ty> <tz>";
                int mx = Integer.parseInt(sp[0]);
                int my = Integer.parseInt(sp[1]);
                int mz = Integer.parseInt(sp[2]);
                String labelName = sp[3];
                int tx = Integer.parseInt(sp[4]);
                int ty = Integer.parseInt(sp[5]);
                int tz = Integer.parseInt(sp[6]);

                net.minecraft.world.World ws = null;
                if (net.minecraft.server.MinecraftServer.getServer() != null) {
                    ws = net.minecraft.server.MinecraftServer.getServer()
                        .worldServerForDimension(player.dimension);
                }
                if (ws == null) return "ERROR: No world";

                TileEntityManager mgr = (TileEntityManager) ws.getTileEntity(mx, my, mz);
                if (mgr == null) return "ERROR: No manager at " + mx + "," + my + "," + mz;

                ItemStack disk = mgr.getStackInSlot(0);
                if (disk == null || disk.getItem() != ModItems.disk) {
                    disk = new ItemStack(ModItems.disk);
                }
                LabelPositionHolder holder = LabelPositionHolder.from(disk);
                holder.addPosition(labelName, new BlockPos(tx, ty, tz));
                LabelPositionHolder.saveTo(disk, holder);
                mgr.setInventorySlotContents(0, disk);
                mgr.rebuildNetwork();
                mgr.markDirty();
                ws.markBlockForUpdate(mx, my, mz);

                return "LABEL_SET: " + labelName + " -> " + tx + "," + ty + "," + tz;
            }

            case "clearlabels": {
                String[] sp = arg.trim()
                    .split("\\s+");
                if (sp.length < 3) return "ERROR: Usage: clearlabels <mx> <my> <mz>";
                int mx = Integer.parseInt(sp[0]);
                int my = Integer.parseInt(sp[1]);
                int mz = Integer.parseInt(sp[2]);

                net.minecraft.world.World ws = null;
                if (net.minecraft.server.MinecraftServer.getServer() != null) {
                    ws = net.minecraft.server.MinecraftServer.getServer()
                        .worldServerForDimension(player.dimension);
                }
                if (ws == null) return "ERROR: No world";

                TileEntityManager mgr = (TileEntityManager) ws.getTileEntity(mx, my, mz);
                if (mgr == null) return "ERROR: No manager at " + mx + "," + my + "," + mz;

                ItemStack disk = mgr.getStackInSlot(0);
                if (disk == null || disk.getItem() != ModItems.disk) {
                    disk = new ItemStack(ModItems.disk);
                }
                LabelPositionHolder holder = new LabelPositionHolder();
                LabelPositionHolder.saveTo(disk, holder);
                mgr.setInventorySlotContents(0, disk);
                mgr.rebuildNetwork();
                mgr.markDirty();
                ws.markBlockForUpdate(mx, my, mz);

                return "LABELS_CLEARED";
            }

            case "programmanager": {
                String[] sp = arg.trim()
                    .split("\\s+", 4);
                if (sp.length < 4) return "ERROR: Usage: programmanager <mx> <my> <mz> <code>";
                int mx = Integer.parseInt(sp[0]);
                int my = Integer.parseInt(sp[1]);
                int mz = Integer.parseInt(sp[2]);
                String pCode = sp[3].replace("\\n", "\n");

                net.minecraft.world.World ws = null;
                if (net.minecraft.server.MinecraftServer.getServer() != null) {
                    ws = net.minecraft.server.MinecraftServer.getServer()
                        .worldServerForDimension(player.dimension);
                }
                if (ws == null) return "ERROR: No world";

                TileEntityManager mgr = (TileEntityManager) ws.getTileEntity(mx, my, mz);
                if (mgr == null) return "ERROR: No manager at " + mx + "," + my + "," + mz;

                ItemStack disk = mgr.getStackInSlot(0);
                if (disk == null || disk.getItem() != ModItems.disk) {
                    disk = new ItemStack(ModItems.disk);
                }
                ItemDisk.setProgramString(disk, pCode);
                mgr.setInventorySlotContents(0, disk);
                mgr.recompile(pCode);
                mgr.rebuildNetwork();
                mgr.setManagerTick(0);
                mgr.setRunning(true);
                mgr.setLogLevel(LogLevel.DEBUG);
                mgr.markDirty();
                ws.markBlockForUpdate(mx, my, mz);

                return "PROGRAM_SET: " + (mgr.hasCompiledProgram() ? "VALID" : "INVALID");
            }

            case "getmanagerstatus": {
                String[] sp = arg.trim()
                    .split("\\s+");
                if (sp.length < 3) return "ERROR: Usage: getmanagerstatus <mx> <my> <mz>";
                int mx = Integer.parseInt(sp[0]);
                int my = Integer.parseInt(sp[1]);
                int mz = Integer.parseInt(sp[2]);

                net.minecraft.world.World ws = null;
                if (net.minecraft.server.MinecraftServer.getServer() != null) {
                    ws = net.minecraft.server.MinecraftServer.getServer()
                        .worldServerForDimension(player.dimension);
                }
                if (ws == null) return "ERROR: No world";

                TileEntityManager mgr = (TileEntityManager) ws.getTileEntity(mx, my, mz);
                if (mgr == null) return "ERROR: No manager at " + mx + "," + my + "," + mz;

                return String.format(
                    "{\"tick\":%d,\"running\":%s,\"hasDisk\":%s,\"hasProgram\":%s,\"status\":\"%s\"}",
                    mgr.getManagerTick(),
                    mgr.isRunning(),
                    mgr.getDisk() != null,
                    mgr.hasCompiledProgram(),
                    mgr.getStatusMessage()
                        .replace("\"", "\\\""));
            }

            case "stepmanager": {
                String[] sp = arg.trim()
                    .split("\\s+");
                if (sp.length < 3) return "ERROR: Usage: stepmanager <mx> <my> <mz> [ticks]";
                int mx = Integer.parseInt(sp[0]);
                int my = Integer.parseInt(sp[1]);
                int mz = Integer.parseInt(sp[2]);
                int steps = sp.length > 3 ? Integer.parseInt(sp[3]) : 1;
                if (steps < 1 || steps > 200) return "ERROR: Steps must be 1..200";

                net.minecraft.world.World ws = null;
                if (net.minecraft.server.MinecraftServer.getServer() != null) {
                    ws = net.minecraft.server.MinecraftServer.getServer()
                        .worldServerForDimension(player.dimension);
                }
                if (ws == null) return "ERROR: No world";

                TileEntityManager mgr = (TileEntityManager) ws.getTileEntity(mx, my, mz);
                if (mgr == null) return "ERROR: No manager at " + mx + "," + my + "," + mz;

                boolean wasRunning = mgr.isRunning();
                mgr.setRunning(true);
                for (int s = 0; s < steps; s++) {
                    mgr.updateEntity();
                }
                mgr.setRunning(wasRunning);
                return "STEPPED: " + steps + " (tick=" + mgr.getManagerTick() + ")";
            }

            case "setrunning": {
                String[] sp = arg.trim()
                    .split("\\s+");
                if (sp.length < 4) return "ERROR: Usage: setrunning <mx> <my> <mz> <true|false>";
                int mx = Integer.parseInt(sp[0]);
                int my = Integer.parseInt(sp[1]);
                int mz = Integer.parseInt(sp[2]);
                boolean run = Boolean.parseBoolean(sp[3]);

                net.minecraft.world.World ws = null;
                if (net.minecraft.server.MinecraftServer.getServer() != null) {
                    ws = net.minecraft.server.MinecraftServer.getServer()
                        .worldServerForDimension(player.dimension);
                }
                if (ws == null) return "ERROR: No world";

                TileEntityManager mgr = (TileEntityManager) ws.getTileEntity(mx, my, mz);
                if (mgr == null) return "ERROR: No manager at " + mx + "," + my + "," + mz;

                mgr.setRunning(run);
                mgr.markDirty();
                ws.markBlockForUpdate(mx, my, mz);
                return "RUNNING_SET: " + mgr.isRunning();
            }

            case "settick": {
                String[] sp = arg.trim()
                    .split("\\s+");
                if (sp.length < 4) return "ERROR: Usage: settick <mx> <my> <mz> <tick>";
                int mx = Integer.parseInt(sp[0]);
                int my = Integer.parseInt(sp[1]);
                int mz = Integer.parseInt(sp[2]);
                long tick = Long.parseLong(sp[3]);

                net.minecraft.world.World ws = null;
                if (net.minecraft.server.MinecraftServer.getServer() != null) {
                    ws = net.minecraft.server.MinecraftServer.getServer()
                        .worldServerForDimension(player.dimension);
                }
                if (ws == null) return "ERROR: No world";

                TileEntityManager mgr = (TileEntityManager) ws.getTileEntity(mx, my, mz);
                if (mgr == null) return "ERROR: No manager at " + mx + "," + my + "," + mz;

                mgr.setManagerTick(tick);
                mgr.markDirty();
                ws.markBlockForUpdate(mx, my, mz);
                return "TICK_SET: " + mgr.getManagerTick();
            }

            default:
                return fixtureCommand(cmd, arg, player);
        }
    }

    /** Opt-in, one server task: real native callbacks with precisely controlled observations. */
    private String redstoneHistoryFixture(net.minecraft.entity.player.EntityPlayerMP player) {
        net.minecraft.world.World world = player.worldObj;
        int x = (int) Math.floor(player.posX) + 6, y = 70, z = (int) Math.floor(player.posZ) + 1;
        int[][] positions = { { x, y, z }, { x + 1, y, z }, { x + 2, y, z }, { x + 2, y, z - 1 },
            { x + 2, y - 1, z - 1 }, { x + 12, y, z } };
        for (int[] pos : positions) if (!world.blockExists(pos[0], pos[1], pos[2]))
            return "ERROR: Native-history fixture requires already-loaded chunks";
        if (!claimFixtureBlocks(world, positions))
            return "ERROR: Native-history fixture would overwrite unrelated blocks";
        world.setBlock(x, y, z, ModBlocks.manager);
        world.setBlock(x + 1, y, z, ModBlocks.cable);
        world.setBlock(x + 2, y, z, ca.teamdman.sfm.common.block.ModBlocks.redstonePort);
        world.setBlock(x + 2, y - 1, z - 1, net.minecraft.init.Blocks.stone);
        world.setBlock(x + 2, y, z - 1, net.minecraft.init.Blocks.lever, 5, 3);
        world.setBlockMetadataWithNotify(x + 2, y, z - 1, 5, 3);
        world.setBlockToAir(x + 12, y, z);
        ca.teamdman.sfm.common.tileentity.TileEntityRedstonePort port = (ca.teamdman.sfm.common.tileentity.TileEntityRedstonePort) world
            .getTileEntity(x + 2, y, z);
        port.refreshInputs();
        ca.teamdman.sfm.common.util.BlockPos anchor = new ca.teamdman.sfm.common.util.BlockPos(x, y, z);
        ca.teamdman.sfm.common.network.CableNetwork network = new ca.teamdman.sfm.common.network.CableNetwork(
            world,
            anchor);
        LabelPositionHolder labels = new LabelPositionHolder();
        labels.addPosition("history_signal", new ca.teamdman.sfm.common.util.BlockPos(x + 2, y, z));
        ca.teamdman.sfml.program_builder.ProgramBuildResult built = new ca.teamdman.sfml.program_builder.ProgramBuilder(
            "EVERY REDSTONE PULSE FROM history_signal NORTH SIDE DO FORGET END "
                + "EVERY REDSTONE FALLING FROM history_signal NORTH SIDE DO FORGET END").build();
        if (!built.isSuccess()) return "ERROR: Native-history fixture could not compile";
        java.util.List<ca.teamdman.sfml.ast.NamedRedstoneTrigger> triggers = java.util.Arrays.asList(
            (ca.teamdman.sfml.ast.NamedRedstoneTrigger) built.getProgram()
                .getTriggers()
                .get(0),
            (ca.teamdman.sfml.ast.NamedRedstoneTrigger) built.getProgram()
                .getTriggers()
                .get(1));
        ca.teamdman.sfm.common.program.RedstoneTriggerTracker tracker = new ca.teamdman.sfm.common.program.RedstoneTriggerTracker();
        JsonArray steps = new JsonArray();
        historyStep("initial baseline", world, network, labels, tracker, triggers, false, false, steps);
        toggleHistoryLever(world, player, x + 2, y, z - 1);
        if (port.getInputLevel(2) != 15) throw new IllegalStateException("Native lever did not power the north face");
        // Real cable onBlockAdded/breakBlock callbacks occur between edge and observation.
        world.setBlock(x + 12, y, z, ModBlocks.cable);
        historyStep("unrelated cable added", world, network, labels, tracker, triggers, true, false, steps);
        historyStep("held signal has no repeat", world, network, labels, tracker, triggers, false, false, steps);
        toggleHistoryLever(world, player, x + 2, y, z - 1);
        world.setBlockToAir(x + 12, y, z);
        historyStep("unrelated cable removed", world, network, labels, tracker, triggers, false, true, steps);
        // Inject event notifications only; never load/unload distant chunks for this fixture.
        net.minecraft.world.chunk.Chunk unrelated = new net.minecraft.world.chunk.Chunk(
            world,
            (x >> 4) + 20,
            (z >> 4) + 20);
        ca.teamdman.sfm.common.network.NetworkEvents events = new ca.teamdman.sfm.common.network.NetworkEvents();
        toggleHistoryLever(world, player, x + 2, y, z - 1);
        events.onLoad(new net.minecraftforge.event.world.ChunkEvent.Load(unrelated));
        historyStep("unrelated chunk load notification", world, network, labels, tracker, triggers, true, false, steps);
        toggleHistoryLever(world, player, x + 2, y, z - 1);
        events.onUnload(new net.minecraftforge.event.world.ChunkEvent.Unload(unrelated));
        historyStep(
            "unrelated chunk unload notification",
            world,
            network,
            labels,
            tracker,
            triggers,
            false,
            true,
            steps);
        world.setBlockToAir(x + 1, y, z);
        toggleHistoryLever(world, player, x + 2, y, z - 1);
        world.setBlock(x + 1, y, z, ModBlocks.cable);
        historyStep("transient real cable gap rebases", world, network, labels, tracker, triggers, false, false, steps);
        toggleHistoryLever(world, player, x + 2, y, z - 1);
        historyStep("new edge after reconnect", world, network, labels, tracker, triggers, false, true, steps);
        historyStep("edge delivered once", world, network, labels, tracker, triggers, false, false, steps);
        JsonObject result = new JsonObject();
        result.addProperty("passed", true);
        result.addProperty("serverThread", ServerUiTasks.isServerThread());
        result.addProperty(
            "scope",
            "Real lever/port/cable callbacks; synthetic unrelated chunk notifications; one server task");
        result.addProperty("x", x);
        result.addProperty("y", y);
        result.addProperty("z", z);
        result.addProperty("northRises", port.getRisingCount(2));
        result.addProperty("northFalls", port.getFallingCount(2));
        result.add("steps", steps);
        return result.toString();
    }

    private static void toggleHistoryLever(net.minecraft.world.World world,
        net.minecraft.entity.player.EntityPlayerMP player, int x, int y, int z) {
        net.minecraft.init.Blocks.lever.onBlockActivated(world, x, y, z, player, 1, .5f, .5f, .5f);
    }

    private static void historyStep(String name, net.minecraft.world.World world,
        ca.teamdman.sfm.common.network.CableNetwork network, LabelPositionHolder labels,
        ca.teamdman.sfm.common.program.RedstoneTriggerTracker tracker,
        java.util.List<ca.teamdman.sfml.ast.NamedRedstoneTrigger> triggers, boolean rising, boolean falling,
        JsonArray steps) {
        ca.teamdman.sfm.common.program.ProgramContext context = new ca.teamdman.sfm.common.program.ProgramContext(
            world,
            null,
            network,
            labels,
            20,
            false);
        java.util.Map<ca.teamdman.sfml.ast.NamedRedstoneTrigger, Boolean> observed = tracker
            .capture(context, triggers, new ca.teamdman.sfml.data.QueryBudget());
        boolean up = Boolean.TRUE.equals(observed.get(triggers.get(0))),
            down = Boolean.TRUE.equals(observed.get(triggers.get(1)));
        if (up != rising || down != falling)
            throw new IllegalStateException("Native history failed: " + name + " (" + up + ", " + down + ")");
        JsonObject step = new JsonObject();
        step.addProperty("phase", name);
        step.addProperty("rising", up);
        step.addProperty("falling", down);
        steps.add(step);
    }

    private CompletableFuture<String> executeOnClient(Callable<String> callable) {
        TaskEntry entry = new TaskEntry(callable);
        taskQueue.add(entry);
        return entry.future;
    }

    private void launchWorld(String worldName) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.getSaveLoader() != null && mc.getSaveLoader()
            .canLoadWorld(worldName)) {
            mc.launchIntegratedServer(worldName, worldName, (WorldSettings) null);
        } else {
            WorldSettings settings = new WorldSettings(
                0L,
                WorldSettings.GameType.CREATIVE,
                false,
                false,
                WorldType.FLAT);
            settings.enableCommands();
            mc.launchIntegratedServer(worldName, worldName, settings);
        }
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
        if (mc.guiAchievement != null
            && (Boolean.getBoolean("sfm.testHarness") || Boolean.getBoolean("gtnh.testHarness"))) {
            mc.guiAchievement.func_146257_b();
        }

        String autoWorld = System.getProperty("gtnh.autoworld");
        if (autoWorld != null && !autoWorldTriggered && !"false".equalsIgnoreCase(autoWorld)) {
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
