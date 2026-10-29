package ca.teamdman.sfm.common.net;

import java.util.concurrent.ArrayBlockingQueue;

import ca.teamdman.sfm.SuperFactoryManager;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/** Small UI requests never touch live inventories from Netty threads or create unbounded work. */
public final class ServerUiTasks {

    public static final ServerUiTasks INSTANCE = new ServerUiTasks();
    private static final ArrayBlockingQueue<Runnable> QUEUE = new ArrayBlockingQueue<>(64);
    private static volatile Thread serverThread;

    private ServerUiTasks() {}

    public static boolean enqueue(Runnable task) {
        return QUEUE.offer(task);
    }

    public static boolean isServerThread() {
        return Thread.currentThread() == serverThread;
    }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        serverThread = Thread.currentThread();
        for (int i = 0; i < 8; i++) {
            Runnable task = QUEUE.poll();
            if (task == null) break;
            try {
                task.run();
            } catch (RuntimeException error) {
                SuperFactoryManager.LOG.error("Manager UI request failed", error);
            }
        }
    }

    public static void clear() {
        QUEUE.clear();
        serverThread = null;
    }
}
