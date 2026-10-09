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
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;

import ca.teamdman.sfm.Config;
import ca.teamdman.sfm.SuperFactoryManager;
import ca.teamdman.sfm.common.block.ModBlocks;
import ca.teamdman.sfm.common.gui.GuiLabelGun;
import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.item.ModItems;
import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.tileentity.LogLevel;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfm.common.util.BlockPos;
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

        if ("ping".equals(cmd)) {
            return "pong";
        }

        CompletableFuture<String> future = executeOnClient(() -> {
            Minecraft mc = Minecraft.getMinecraft();
            switch (cmd) {
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

                case "runscenario": {
                    int scen = Integer.parseInt(arg.trim());
                    boolean prevExport = Config.exportLogsToFile;
                    Config.exportLogsToFile = true;
                    File logF = new File("logs/sfm_server_export.log");
                    if (logF.exists()) logF.delete();

                    net.minecraft.world.World ws = null;
                    if (net.minecraft.server.MinecraftServer.getServer() != null) {
                        ws = net.minecraft.server.MinecraftServer.getServer()
                            .worldServerForDimension(mc.thePlayer != null ? mc.thePlayer.dimension : 0);
                    }
                    if (ws == null) {
                        ws = mc.theWorld;
                    }
                    if (ws == null) return "ERROR: No world";

                    int bx = 500, by = 60, bz = 0;
                    BlockPos c1 = new BlockPos(bx + 1, by, bz);
                    BlockPos c2 = new BlockPos(bx + 2, by, bz);
                    BlockPos posA = new BlockPos(bx + 1, by + 1, bz);
                    BlockPos posB = new BlockPos(bx + 2, by + 1, bz);

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
                    } else {
                        pCode = "NAME \"Test 3\"\nEVERY 20 TICKS DO\n  INPUT FROM a\n  OUTPUT TO b\nEND\n";
                        pLvl = LogLevel.DEBUG;
                    }

                    TileEntityManager mgr = (TileEntityManager) ws.getTileEntity(bx, by, bz);
                    if (mgr == null) return "ERROR: No manager tile entity at " + bx + "," + by + "," + bz;

                    for (Object te : ws.loadedTileEntityList) {
                        if (te instanceof TileEntityManager && te != mgr) {
                            ((TileEntityManager) te).setLogLevel(LogLevel.OFF);
                        }
                    }

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

                    mgr.updateEntity();
                    Config.exportLogsToFile = prevExport;

                    return "SCENARIO_RAN: " + scen;
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
                            .worldServerForDimension(mc.thePlayer != null ? mc.thePlayer.dimension : 0);
                    }
                    if (ws == null) ws = mc.theWorld;
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
                    mgr.markDirty();
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
                            .worldServerForDimension(mc.thePlayer != null ? mc.thePlayer.dimension : 0);
                    }
                    if (ws == null) ws = mc.theWorld;
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
                    mgr.markDirty();
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
                            .worldServerForDimension(mc.thePlayer != null ? mc.thePlayer.dimension : 0);
                    }
                    if (ws == null) ws = mc.theWorld;
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
                    mgr.setLogLevel(LogLevel.DEBUG);
                    mgr.markDirty();
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
                            .worldServerForDimension(mc.thePlayer != null ? mc.thePlayer.dimension : 0);
                    }
                    if (ws == null) ws = mc.theWorld;
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

                    net.minecraft.world.World ws = null;
                    if (net.minecraft.server.MinecraftServer.getServer() != null) {
                        ws = net.minecraft.server.MinecraftServer.getServer()
                            .worldServerForDimension(mc.thePlayer != null ? mc.thePlayer.dimension : 0);
                    }
                    if (ws == null) ws = mc.theWorld;
                    if (ws == null) return "ERROR: No world";

                    TileEntityManager mgr = (TileEntityManager) ws.getTileEntity(mx, my, mz);
                    if (mgr == null) return "ERROR: No manager at " + mx + "," + my + "," + mz;

                    for (int s = 0; s < steps; s++) {
                        mgr.updateEntity();
                    }
                    return "STEPPED: " + steps + " (tick=" + mgr.getManagerTick() + ")";
                }

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
                    } else if ("editor".equalsIgnoreCase(arg) || arg.toLowerCase()
                        .startsWith("editor")) {
                            if (mc.thePlayer == null) return "ERROR: Player is null";
                            int mx = 10, my = 6, mz = 420;
                            String[] ep = arg.trim()
                                .split("\\s+");
                            if (ep.length >= 4) {
                                mx = Integer.parseInt(ep[1]);
                                my = Integer.parseInt(ep[2]);
                                mz = Integer.parseInt(ep[3]);
                            }
                            TileEntityManager m = (TileEntityManager) mc.theWorld.getTileEntity(mx, my, mz);
                            if (m == null) return "ERROR: No manager at " + mx + "," + my + "," + mz;
                            mc.displayGuiScreen(new ca.teamdman.sfm.client.gui.GuiCodeEditor(m, mc.currentScreen));
                            return "OPENED_EDITOR_GUI";
                        } else if ("manager".equalsIgnoreCase(arg) || arg.toLowerCase()
                            .startsWith("manager")) {
                                if (mc.thePlayer == null) return "ERROR: Player is null";
                                int mx = 10, my = 6, mz = 420;
                                String[] ep = arg.trim()
                                    .split("\\s+");
                                if (ep.length >= 4) {
                                    mx = Integer.parseInt(ep[1]);
                                    my = Integer.parseInt(ep[2]);
                                    mz = Integer.parseInt(ep[3]);
                                } else if (mc.theWorld.getTileEntity(500, 60, 0) instanceof TileEntityManager) {
                                    mx = 500;
                                    my = 60;
                                    mz = 0;
                                }
                                TileEntityManager m = (TileEntityManager) mc.theWorld.getTileEntity(mx, my, mz);
                                if (m == null) return "ERROR: No manager at " + mx + "," + my + "," + mz;
                                mc.displayGuiScreen(
                                    new ca.teamdman.sfm.common.gui.GuiManager(mc.thePlayer.inventory, m));
                                return "OPENED_MANAGER_GUI";
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
                        heldItem.getItem()
                            .onItemRightClick(heldItem, mc.theWorld, mc.thePlayer);
                        return "USED_ITEM: " + heldItem.getDisplayName();
                    }
                    return "NO_HELD_ITEM";

                case "type":
                    if (mc.currentScreen == null) return "ERROR: No active GUI";
                    for (char c : arg.toCharArray()) {
                        try {
                            if (mc.currentScreen instanceof ca.teamdman.sfm.client.gui.GuiCodeEditor) {
                                java.lang.reflect.Method km = ca.teamdman.sfm.client.gui.GuiCodeEditor.class
                                    .getDeclaredMethod("keyTyped", char.class, int.class);
                                km.setAccessible(true);
                                km.invoke(mc.currentScreen, c, 0);
                            } else {
                                java.lang.reflect.Method km = GuiScreen.class
                                    .getDeclaredMethod("keyTyped", char.class, int.class);
                                km.setAccessible(true);
                                km.invoke(mc.currentScreen, c, 0);
                            }
                        } catch (Exception e) {
                            return "ERROR_TYPE: " + e.getMessage();
                        }
                    }
                    return "TYPED: " + arg;

                case "settext":
                    if (mc.currentScreen instanceof ca.teamdman.sfm.common.gui.GuiLabelGun) {
                        try {
                            java.lang.reflect.Field f = ca.teamdman.sfm.common.gui.GuiLabelGun.class
                                .getDeclaredField("labelField");
                            f.setAccessible(true);
                            GuiTextField tf = (GuiTextField) f.get(mc.currentScreen);
                            tf.setText(arg);
                            java.lang.reflect.Method m = ca.teamdman.sfm.common.gui.GuiLabelGun.class
                                .getDeclaredMethod("rebuildLabelButtons");
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
                            java.lang.reflect.Method m = ca.teamdman.sfm.common.gui.GuiLabelGun.class
                                .getDeclaredMethod("onDone");
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
                        if (mc.currentScreen instanceof ca.teamdman.sfm.client.gui.GuiCodeEditor) {
                            java.lang.reflect.Method km = ca.teamdman.sfm.client.gui.GuiCodeEditor.class
                                .getDeclaredMethod("keyTyped", char.class, int.class);
                            km.setAccessible(true);
                            km.invoke(mc.currentScreen, '\0', keyCode);
                        } else {
                            java.lang.reflect.Method km = GuiScreen.class
                                .getDeclaredMethod("keyTyped", char.class, int.class);
                            km.setAccessible(true);
                            km.invoke(mc.currentScreen, '\0', keyCode);
                        }
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
                        if (mc.currentScreen instanceof ca.teamdman.sfm.client.gui.GuiCodeEditor) {
                            java.lang.reflect.Method m = ca.teamdman.sfm.client.gui.GuiCodeEditor.class
                                .getDeclaredMethod("mouseClicked", int.class, int.class, int.class);
                            m.setAccessible(true);
                            m.invoke(mc.currentScreen, x, y, button);
                        } else {
                            java.lang.reflect.Method m = GuiScreen.class
                                .getDeclaredMethod("mouseClicked", int.class, int.class, int.class);
                            m.setAccessible(true);
                            m.invoke(mc.currentScreen, x, y, button);
                        }
                    } catch (Exception e) {
                        return "ERROR_CLICK: " + e.getMessage();
                    }
                    return "CLICKED: " + x + ", " + y + " (button " + button + ")";

                case "drag":
                    if (mc.currentScreen == null) return "ERROR: No active GUI to drag";
                    String[] dCoords = arg.split("\\s+");
                    int dx1 = Integer.parseInt(dCoords[0]);
                    int dy1 = Integer.parseInt(dCoords[1]);
                    int dx2 = Integer.parseInt(dCoords[2]);
                    int dy2 = Integer.parseInt(dCoords[3]);
                    int steps = dCoords.length > 4 ? Integer.parseInt(dCoords[4]) : 10;
                    try {
                        Method mDown = null;
                        Method mMove = null;
                        Method mUp = null;
                        Class<?> cls = mc.currentScreen.getClass();
                        while (cls != null && cls != Object.class) {
                            for (Method m : cls.getDeclaredMethods()) {
                                if (m.getName()
                                    .equals("mouseClicked") && m.getParameterTypes().length == 3) mDown = m;
                                if (m.getName()
                                    .equals("mouseClickMove") && m.getParameterTypes().length == 4) mMove = m;
                                if (m.getName()
                                    .equals("mouseMovedOrUp") && m.getParameterTypes().length == 3) mUp = m;
                            }
                            cls = cls.getSuperclass();
                        }
                        if (mDown != null) {
                            mDown.setAccessible(true);
                            mDown.invoke(mc.currentScreen, dx1, dy1, 0);
                        }
                        if (mMove != null) {
                            mMove.setAccessible(true);
                            for (int s = 1; s <= steps; s++) {
                                int cx = dx1 + (dx2 - dx1) * s / steps;
                                int cy = dy1 + (dy2 - dy1) * s / steps;
                                mMove.invoke(mc.currentScreen, cx, cy, 0, 50L * s);
                            }
                        }
                        if (mUp != null) {
                            mUp.setAccessible(true);
                            mUp.invoke(mc.currentScreen, dx2, dy2, 0);
                        }
                        return "DRAGGED: (" + dx1 + "," + dy1 + ") -> (" + dx2 + "," + dy2 + ")";
                    } catch (Exception e) {
                        return "ERROR_DRAG: " + e.getMessage();
                    }

                case "resizegui":
                    if (mc.currentScreen instanceof ca.teamdman.sfm.client.gui.GuiCodeEditor) {
                        try {
                            ca.teamdman.sfm.client.gui.GuiCodeEditor ed = (ca.teamdman.sfm.client.gui.GuiCodeEditor) mc.currentScreen;
                            String[] dims = arg.split("\\s+");
                            int newW = Integer.parseInt(dims[0]);
                            int newH = Integer.parseInt(dims[1]);
                            Field fW = ca.teamdman.sfm.client.gui.GuiCodeEditor.class.getDeclaredField("editorW");
                            Field fH = ca.teamdman.sfm.client.gui.GuiCodeEditor.class.getDeclaredField("editorH");
                            fW.setAccessible(true);
                            fH.setAccessible(true);
                            fW.setInt(ed, newW);
                            fH.setInt(ed, newH);
                            ed.updateLayout();
                            return "RESIZED_GUI: " + newW + "x" + newH;
                        } catch (Exception e) {
                            return "ERROR_RESIZE_GUI: " + e.getMessage();
                        }
                    }
                    return "NOT_CODE_EDITOR";

                case "autoindent":
                    if (mc.currentScreen instanceof ca.teamdman.sfm.client.gui.GuiCodeEditor) {
                        try {
                            ca.teamdman.sfm.client.gui.GuiCodeEditor ed = (ca.teamdman.sfm.client.gui.GuiCodeEditor) mc.currentScreen;
                            ed.autoIndentAll();
                            return "AUTOINDENTED";
                        } catch (Exception e) {
                            return "ERROR_AUTOINDENT: " + e.getMessage();
                        }
                    }
                    return "NOT_CODE_EDITOR";

                case "getcode":
                    if (mc.currentScreen instanceof ca.teamdman.sfm.client.gui.GuiCodeEditor) {
                        try {
                            ca.teamdman.sfm.client.gui.GuiCodeEditor ed = (ca.teamdman.sfm.client.gui.GuiCodeEditor) mc.currentScreen;
                            Field fLines = ca.teamdman.sfm.client.gui.GuiCodeEditor.class.getDeclaredField("lines");
                            fLines.setAccessible(true);
                            java.util.List<String> edLines = (java.util.List<String>) fLines.get(ed);
                            return String.join("\n", edLines);
                        } catch (Exception e) {
                            return "ERROR_GET_CODE: " + e.getMessage();
                        }
                    }
                    return "NOT_CODE_EDITOR";

                case "setcode":
                    if (mc.currentScreen instanceof ca.teamdman.sfm.client.gui.GuiCodeEditor) {
                        try {
                            ca.teamdman.sfm.client.gui.GuiCodeEditor ed = (ca.teamdman.sfm.client.gui.GuiCodeEditor) mc.currentScreen;
                            java.lang.reflect.Field fLines = ca.teamdman.sfm.client.gui.GuiCodeEditor.class
                                .getDeclaredField("lines");
                            fLines.setAccessible(true);
                            java.util.List<String> edLines = (java.util.List<String>) fLines.get(ed);
                            edLines.clear();
                            String codeText = arg.replace("\\n", "\n");
                            edLines.addAll(java.util.Arrays.asList(codeText.split("\n", -1)));
                            Field fCursorLine = ca.teamdman.sfm.client.gui.GuiCodeEditor.class
                                .getDeclaredField("cursorLine");
                            Field fCursorCol = ca.teamdman.sfm.client.gui.GuiCodeEditor.class
                                .getDeclaredField("cursorCol");
                            fCursorLine.setAccessible(true);
                            fCursorCol.setAccessible(true);
                            fCursorLine.setInt(ed, 0);
                            fCursorCol.setInt(ed, 0);
                            ed.updateLayout();
                            java.lang.reflect.Method m = ca.teamdman.sfm.client.gui.GuiCodeEditor.class
                                .getDeclaredMethod("validateSyntax");
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
                    if (mc.currentScreen != null) {
                        try {
                            java.lang.reflect.Field f = GuiScreen.class.getDeclaredField("buttonList");
                            f.setAccessible(true);
                            java.util.List<net.minecraft.client.gui.GuiButton> btns = (java.util.List<net.minecraft.client.gui.GuiButton>) f
                                .get(mc.currentScreen);
                            for (net.minecraft.client.gui.GuiButton b : btns) {
                                if (String.valueOf(b.id)
                                    .equals(arg.trim())
                                    || b.displayString.toLowerCase()
                                        .contains(
                                            arg.trim()
                                                .toLowerCase())) {
                                    java.lang.reflect.Method m = GuiScreen.class
                                        .getDeclaredMethod("actionPerformed", net.minecraft.client.gui.GuiButton.class);
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
                            java.lang.reflect.Field f = ca.teamdman.sfm.common.gui.GuiLabelGun.class
                                .getDeclaredField("labelBtnMap");
                            f.setAccessible(true);
                            java.util.Map<net.minecraft.client.gui.GuiButton, String> map = (java.util.Map<net.minecraft.client.gui.GuiButton, String>) f
                                .get(gunGui);
                            for (java.util.Map.Entry<net.minecraft.client.gui.GuiButton, String> entry : map
                                .entrySet()) {
                                if (entry.getValue()
                                    .equalsIgnoreCase(arg.trim())) {
                                    java.lang.reflect.Method m = GuiScreen.class
                                        .getDeclaredMethod("actionPerformed", net.minecraft.client.gui.GuiButton.class);
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
