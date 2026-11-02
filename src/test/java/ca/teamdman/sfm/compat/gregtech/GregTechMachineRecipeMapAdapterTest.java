package ca.teamdman.sfm.compat.gregtech;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.tileentity.TileEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.recipe.MachineRecipeMapService.Access;
import ca.teamdman.sfml.data.QueryBudget;

class GregTechMachineRecipeMapAdapterTest {

    private GregTechMachineRecipeMapAdapter.Bridge api;
    private Host host;
    private Basic machine;

    @BeforeEach
    void setup() throws Exception {
        NativeMap.ALL_RECIPE_MAPS.clear();
        Maps.maceratorRecipes = new NativeMap("gt.recipe.macerator");
        Maps.benderRecipes = new NativeMap("gt.recipe.metalbender");
        Maps.cutterRecipes = new NativeMap("gt.recipe.cuttingsaw");
        api = new GregTechMachineRecipeMapAdapter.Bridge(
            Host.class,
            Basic.class,
            Meta.class,
            Maps.class,
            NativeMap.class,
            Backend.class);
        host = new Host();
        machine = new Basic(host, Maps.maceratorRecipes);
        host.machine = machine;
    }

    @Test
    void qualifiesOnlyExactStockMachineAndThreeRegisteredNativeMaps() {
        for (NativeMap map : new NativeMap[] { Maps.maceratorRecipes, Maps.benderRecipes, Maps.cutterRecipes }) {
            machine.map = map;
            Access access = api.find(host, new QueryBudget());
            assertNotNull(access);
            assertEquals(map.unlocalizedName, access.maps().current);
            assertTrue(access.current(host, new QueryBudget()));
        }
        machine.map = new NativeMap("gt.recipe.assembler");
        assertNull(api.find(host, new QueryBudget()));
        host.machine = new Derived(host, Maps.maceratorRecipes);
        assertNull(api.find(host, new QueryBudget()));
        assertNull(api.find(new TileEntity(), new QueryBudget()));
    }

    @Test
    void registryIdentityBackendAndHostBackReferenceMustAllAgree() {
        machine.owner = new Host();
        assertNull(api.find(host, new QueryBudget()));
        machine.owner = host;
        Maps.maceratorRecipes.backend = new OtherBackend();
        assertNull(api.find(host, new QueryBudget()));
        Maps.maceratorRecipes.backend = new Backend();
        NativeMap.ALL_RECIPE_MAPS.put("gt.recipe.macerator", new NativeMap("gt.recipe.macerator"));
        assertNull(api.find(host, new QueryBudget()));
    }

    @Test
    void observationDetectsModeMetaTileOrRegistryChangesAndAdvertisedMapMismatch() {
        Access access = api.find(host, new QueryBudget());
        assertNotNull(access);
        machine.map = Maps.benderRecipes;
        assertFalse(access.current(host, new QueryBudget()));
        machine.map = Maps.maceratorRecipes;
        machine.choices = Collections.emptyList();
        assertFalse(access.current(host, new QueryBudget()));
        machine.choices = null;
        host.machine = new Basic(host, Maps.maceratorRecipes);
        assertFalse(access.current(host, new QueryBudget()));
        host.machine = machine;
        NativeMap.ALL_RECIPE_MAPS.clear();
        assertFalse(access.current(host, new QueryBudget()));
    }

    @Test
    void callbacksRemainBoundedAndFailuresArePropagated() {
        QueryBudget budget = new QueryBudget();
        Access access = api.find(host, budget);
        assertNotNull(access);
        assertTrue(access.current(host, budget));
        assertTrue(budget.getProviderCalls() > 0);
        assertTrue(budget.getProviderCalls() < 64);
        budget.chargeProviders(budget.getProviderCallLimit() - budget.getProviderCalls());
        assertThrows(IllegalArgumentException.class, () -> access.current(host, budget));
        machine.failure = new IllegalStateException("native failure");
        assertSame(machine.failure, assertThrows(IllegalStateException.class, () -> api.find(host, new QueryBudget())));
    }

    public interface Meta {

        Host getBaseMetaTileEntity();
    }

    public static class Host extends TileEntity {

        public Basic machine;

        public Basic getMetaTileEntity() {
            return machine;
        }
    }

    public static class Basic implements Meta {

        Host owner;
        NativeMap map;
        Collection<NativeMap> choices;
        RuntimeException failure;

        Basic(Host owner, NativeMap map) {
            this.owner = owner;
            this.map = map;
        }

        @Override
        public Host getBaseMetaTileEntity() {
            return owner;
        }

        public NativeMap getRecipeMap() {
            if (failure != null) throw failure;
            return map;
        }

        public Collection<NativeMap> getAvailableRecipeMaps() {
            return choices == null ? Collections.singletonList(map) : choices;
        }
    }

    public static final class Derived extends Basic {

        Derived(Host host, NativeMap map) {
            super(host, map);
        }
    }

    public static class Backend {
    }

    public static final class OtherBackend extends Backend {
    }

    public static final class NativeMap {

        public static final Map<String, NativeMap> ALL_RECIPE_MAPS = new HashMap<>();
        public final String unlocalizedName;
        Backend backend = new Backend();

        NativeMap(String id) {
            unlocalizedName = id;
            ALL_RECIPE_MAPS.put(id, this);
        }

        public Backend getBackend() {
            return backend;
        }
    }

    public static final class Maps {

        public static NativeMap maceratorRecipes, benderRecipes, cutterRecipes;
    }
}
