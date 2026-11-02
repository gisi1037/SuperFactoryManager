package ca.teamdman.sfm.common.recipe;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import ca.teamdman.sfm.SuperFactoryManager;
import ca.teamdman.sfm.common.net.ServerUiTasks;
import ca.teamdman.sfm.compat.gregtech.GregTechRecipeCatalogAdapter;
import ca.teamdman.sfml.data.ItemIdentity;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.recipe.RecipeCatalog;
import ca.teamdman.sfml.data.recipe.RecipeCatalog.Result;
import ca.teamdman.sfml.data.recipe.RecipeCatalog.Status;
import ca.teamdman.sfml.data.recipe.RecipeCatalog.Token;
import ca.teamdman.sfml.data.recipe.RecipeDescriptor;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/** Server-session startup snapshot; no world access, per-manager copies, NEI dependency, or periodic registry scans. */
public final class RecipeCatalogService {

    public static final RecipeCatalogService INSTANCE = new RecipeCatalogService(() -> {
        try {
            return GregTechRecipeCatalogAdapter.open();
        } catch (ReflectiveOperationException | LinkageError failure) {
            throw new UnsupportedOperationException("Recipe API unavailable", failure);
        }
    });

    private enum State {
        STOPPED,
        WAITING,
        STALE,
        READY,
        UNSUPPORTED,
        LIMIT_EXCEEDED
    }

    private final Supplier<GregTechRecipeCatalogAdapter> factory;
    private final AtomicLong epoch = new AtomicLong();
    private volatile State state = State.STOPPED;
    private volatile RecipeCatalog catalog;
    private volatile GregTechRecipeCatalogAdapter adapter;
    private volatile UUID session;
    private volatile boolean started;
    private long tick;
    private int waited, visited, unsupported, incompleteMaps;
    private long buildNanos;
    private String lastFailure = "";

    RecipeCatalogService(Supplier<GregTechRecipeCatalogAdapter> factory) {
        this.factory = Objects.requireNonNull(factory, "Recipe source factory");
    }

    public synchronized void start() {
        close();
        session = UUID.randomUUID();
        state = State.WAITING;
    }

    public synchronized void started() {
        started = true;
    }

    /** Withdraw immediately even if remapping is reported from a different lifecycle thread. */
    public synchronized void invalidate() {
        epoch.incrementAndGet();
        catalog = null;
        adapter = null;
        waited = 0;
        lastFailure = "";
        if (state != State.STOPPED) state = State.STALE;
    }

    public synchronized void close() {
        state = State.STOPPED;
        epoch.incrementAndGet();
        catalog = null;
        adapter = null;
        session = null;
        started = false;
        tick = 0;
        waited = visited = unsupported = incompleteMaps = 0;
        buildNanos = 0;
        lastFailure = "";
    }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || state == State.STOPPED) return;
        thread();
        final long stamp;
        final UUID owner;
        synchronized (this) {
            if (state == State.STOPPED) return;
            tick++;
            if (!started || state == State.READY || state == State.UNSUPPORTED || state == State.LIMIT_EXCEEDED) return;
            stamp = epoch.get();
            owner = session;
        }
        long attemptStarted = System.nanoTime();
        try {
            GregTechRecipeCatalogAdapter source = adapter;
            if (source == null) {
                source = factory.get();
                synchronized (this) {
                    if (!current(stamp, owner)) return;
                    adapter = source;
                }
            }
            if (!source.firstWorldTickReady()) {
                synchronized (this) {
                    if (current(stamp, owner) && ++waited > 1200) {
                        state = State.UNSUPPORTED;
                        lastFailure = "GregTech first-world startup timed out";
                    }
                }
                return;
            }
            GregTechRecipeCatalogAdapter.Build build = source.build(owner, stamp);
            // Publication and withdrawal share the same short critical section. Native scans stay outside it.
            synchronized (this) {
                if (!current(stamp, owner)) return;
                visited = build.visited;
                unsupported = build.unsupported;
                incompleteMaps = build.incompleteMaps;
                buildNanos = build.elapsedNanos;
                catalog = build.catalog;
                adapter = null;
                state = State.READY;
                lastFailure = "";
            }
            SuperFactoryManager.LOG.info(summary());
        } catch (IllegalArgumentException limit) {
            synchronized (this) {
                if (current(stamp, owner)) {
                    buildNanos = System.nanoTime() - attemptStarted;
                    fail(State.LIMIT_EXCEEDED, limit);
                }
            }
        } catch (ReflectiveOperationException | RuntimeException | LinkageError unavailable) {
            synchronized (this) {
                if (current(stamp, owner)) {
                    buildNanos = System.nanoTime() - attemptStarted;
                    fail(State.UNSUPPORTED, unavailable);
                }
            }
        }
    }

    private boolean current(long stamp, UUID owner) {
        return epoch.get() == stamp && owner != null && owner.equals(session) && state != State.STOPPED;
    }

    private void fail(State reason, Throwable failure) {
        catalog = null;
        adapter = null;
        state = reason;
        Throwable cause = failure;
        for (int i = 0; i < 3 && cause.getCause() != null; i++) cause = cause.getCause();
        String message = cause.getClass()
            .getSimpleName() + ": "
            + String.valueOf(cause.getMessage());
        StringBuilder bounded = new StringBuilder();
        for (int i = 0; i < Math.min(160, message.length()); i++) {
            char c = message.charAt(i);
            bounded.append(c < 32 || c == 127 || c == '\u00a7' ? ' ' : c);
        }
        lastFailure = bounded.toString();
        SuperFactoryManager.LOG.warn("Recipe catalog {}: {}; no partial snapshot published", reason, lastFailure);
    }

    public synchronized Result find(ItemIdentity exactOutput, String map, QueryBudget budget) {
        thread();
        RecipeCatalog current = catalog;
        return current == null ? unavailable() : current.find(exactOutput, map, budget, tick);
    }

    /** Inspection fence only; a map association does not grant catalog or machine execution authority. */
    public synchronized long observationEpoch() {
        return session == null ? -1 : epoch.get();
    }

    public synchronized Result resolve(Token token, QueryBudget budget) {
        thread();
        RecipeCatalog current = catalog;
        return current == null ? unavailable() : current.resolve(token, budget, tick);
    }

    public synchronized Token token(RecipeDescriptor descriptor, QueryBudget budget) {
        thread();
        RecipeCatalog current = catalog;
        if (current == null) throw new IllegalArgumentException("Recipe catalog is not ready");
        return current.token(descriptor, budget, tick);
    }

    private Result unavailable() {
        State current = state;
        return RecipeCatalog.unavailable(
            current == State.LIMIT_EXCEEDED ? Status.LIMIT_EXCEEDED
                : current == State.UNSUPPORTED ? Status.UNSUPPORTED
                    : current == State.STALE ? Status.STALE : Status.NOT_READY);
    }

    public synchronized String summary() {
        RecipeCatalog current = catalog;
        return String.format(
            Locale.ROOT,
            "SFM recipe catalog %s: %d visited, %d unsupported, %d incomplete maps, %d descriptors, %d postings; %.3f ms cold build; %d conservative retained bytes",
            state,
            visited,
            unsupported,
            incompleteMaps,
            current == null ? 0 : current.recipeCount(),
            current == null ? 0 : current.postingCount(),
            buildNanos / 1e6,
            current == null ? 0 : current.retainedBytes()) + (lastFailure.isEmpty() ? "" : "; " + lastFailure);
    }

    private static void thread() {
        if (!ServerUiTasks.isServerThread())
            throw new IllegalStateException("Recipe catalog requires the server thread");
    }
}
