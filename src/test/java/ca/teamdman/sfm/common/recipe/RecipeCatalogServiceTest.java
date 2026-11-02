package ca.teamdman.sfm.common.recipe;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.net.ServerUiTasks;
import ca.teamdman.sfm.compat.gregtech.RecipeAdapterFixture;
import ca.teamdman.sfml.data.ItemIdentity;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.recipe.RecipeCatalog.Status;
import ca.teamdman.sfml.data.recipe.RecipeCatalog.Token;
import cpw.mods.fml.common.gameevent.TickEvent;

class RecipeCatalogServiceTest {

    private static final String MAP = "gt.recipe.macerator";
    private final ItemIdentity output = ItemIdentity.of("service_test:item", 0, null, new QueryBudget());
    private RecipeCatalogService service;

    @BeforeEach
    void serverThread() {
        ServerUiTasks.clear();
        ServerUiTasks.INSTANCE.tick(new TickEvent.ServerTickEvent(TickEvent.Phase.START));
    }

    @AfterEach
    void close() {
        if (service != null) service.close();
        ServerUiTasks.clear();
    }

    private void tick() {
        service.tick(new TickEvent.ServerTickEvent(TickEvent.Phase.END));
    }

    private Status status() {
        return service.find(output, MAP, new QueryBudget()).status;
    }

    @Test
    void waitsForStartedAndFirstWorldWorkThenBuildsOnlyOnce() {
        AtomicInteger opened = new AtomicInteger();
        service = new RecipeCatalogService(() -> {
            opened.incrementAndGet();
            ca.teamdman.sfm.compat.gregtech.GregTechRecipeCatalogAdapter adapter = RecipeAdapterFixture.openEmpty();
            RecipeAdapterFixture.Values.worldTickHappened = false;
            return adapter;
        });
        service.start();
        tick();
        assertEquals(0, opened.get());
        assertEquals(Status.NOT_READY, status());
        service.started();
        tick();
        tick();
        assertEquals(1, opened.get());
        assertEquals(0, RecipeAdapterFixture.Backend.reads);
        RecipeAdapterFixture.Values.worldTickHappened = true;
        tick();
        assertEquals(Status.NO_MATCH, status());
        assertEquals(3, RecipeAdapterFixture.Backend.reads);
        for (int i = 0; i < 50; i++) {
            tick();
            assertEquals(Status.NO_MATCH, status());
        }
        assertEquals(1, opened.get());
        assertEquals(3, RecipeAdapterFixture.Backend.reads);
    }

    @Test
    void aggregateAllowanceResetsOnActualServiceTicksAndLookupNeverScansNativeMaps() {
        service = new RecipeCatalogService(RecipeAdapterFixture::openEmpty);
        service.start();
        service.started();
        tick();
        int reads = RecipeAdapterFixture.Backend.reads;
        for (int i = 0; i < 256; i++) assertEquals(Status.NO_MATCH, status());
        assertEquals(Status.LIMIT_EXCEEDED, status());
        tick();
        assertEquals(Status.NO_MATCH, status());
        assertEquals(reads, RecipeAdapterFixture.Backend.reads);
    }

    @Test
    void invalidationWithdrawsImmediatelyAndRestartDoesNotReuseOldAuthority() {
        AtomicInteger opened = new AtomicInteger();
        service = new RecipeCatalogService(() -> {
            opened.incrementAndGet();
            return RecipeAdapterFixture.openEmpty();
        });
        service.start();
        service.started();
        tick();
        assertEquals(Status.NO_MATCH, status());
        service.invalidate();
        assertEquals(Status.STALE, status());
        tick();
        assertEquals(Status.NO_MATCH, status());
        assertEquals(2, opened.get());
        service.close();
        assertEquals(Status.NOT_READY, status());
        service.start();
        assertEquals(Status.NOT_READY, status());
        service.started();
        tick();
        assertEquals(3, opened.get());
        Token old = new Token(
            UUID.randomUUID(),
            1,
            "old",
            MAP,
            String.join("", java.util.Collections.nCopies(64, "a")));
        assertEquals(Status.STALE, service.resolve(old, new QueryBudget()).status);
    }

    @Test
    void concurrentWithdrawalDuringNativeBuildCannotRepublishAnOldSnapshotOrHoldTheLifecycleLock() {
        for (boolean stop : new boolean[] { false, true }) {
            AtomicInteger opened = new AtomicInteger();
            service = new RecipeCatalogService(() -> {
                ca.teamdman.sfm.compat.gregtech.GregTechRecipeCatalogAdapter adapter = RecipeAdapterFixture.openEmpty();
                if (opened.incrementAndGet() == 1) RecipeAdapterFixture.Maps.cutterRecipes.backend.onRead = () -> {
                    try {
                        CompletableFuture.runAsync(() -> {
                            if (stop) service.close();
                            else service.invalidate();
                        })
                            .get(5, TimeUnit.SECONDS);
                    } catch (Exception failure) {
                        throw new AssertionError("Lifecycle withdrawal blocked behind a native scan", failure);
                    }
                };
                return adapter;
            });
            service.start();
            service.started();
            tick();
            assertEquals(stop ? Status.NOT_READY : Status.STALE, status());
            if (stop) {
                service.start();
                service.started();
            }
            tick();
            assertEquals(Status.NO_MATCH, status());
            assertEquals(2, opened.get());
            service.close();
        }
    }

    @Test
    void optionalApiFailureDoesNotRetryEveryTickButCanRecoverAfterANewSession() {
        AtomicInteger opened = new AtomicInteger();
        service = new RecipeCatalogService(() -> {
            if (opened.incrementAndGet() == 1) throw new UnsupportedOperationException("Optional API absent");
            return RecipeAdapterFixture.openEmpty();
        });
        service.start();
        service.started();
        tick();
        assertEquals(Status.UNSUPPORTED, status());
        for (int i = 0; i < 10; i++) tick();
        assertEquals(1, opened.get());
        service.start();
        service.started();
        tick();
        assertEquals(Status.NO_MATCH, status());
    }

    @Test
    void recipeAccessRefusesForeignThreads() {
        service = new RecipeCatalogService(RecipeAdapterFixture::openEmpty);
        service.start();
        service.started();
        tick();
        assertThrows(
            java.util.concurrent.CompletionException.class,
            () -> CompletableFuture.supplyAsync(this::status)
                .join());
    }

    @Test
    void unavailableApiReportsABoundedReadableCauseWithoutControlCharacters() {
        service = new RecipeCatalogService(() -> {
            throw new UnsupportedOperationException(
                "Optional API unavailable",
                new NoSuchMethodException(
                    "missing\n\u00a7method" + String.join("", java.util.Collections.nCopies(500, "x"))));
        });
        service.start();
        service.started();
        tick();
        String summary = service.summary();
        assertEquals(Status.UNSUPPORTED, status());
        assertTrue(summary.contains("NoSuchMethodException"));
        assertFalse(summary.contains("\n"));
        assertFalse(summary.contains("\u00a7"));
        assertTrue(summary.length() < 600);
    }
}
