package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.ast.Statement;

class ProgramContextLazyTransferStorageTest {

    private static final String[] MAPS = { "inputCache", "inputPositions", "fluidInputQuotas", "fluidReservations",
        "reservedFluids" };

    private static Object storage(ProgramContext context, String name) throws Exception {
        Field field = ProgramContext.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(context);
    }

    private static void assertUnmaterialized(ProgramContext context) throws Exception {
        assertSame(Collections.emptyList(), storage(context, "inputs"));
        for (String name : MAPS) assertSame(Collections.emptyMap(), storage(context, name), name);
    }

    @Test
    void dataRedstoneAndForgetWithoutInputsDoNotMaterializeTransferCollections() throws Exception {
        Program program = parse(
            "EVERY 20 TICKS DO LET $count = 1 SET $count = $count + 1 "
                + "OUTPUT ($count) REDSTONE LEVEL TO signal NORTH SIDE FORGET signal FORGET END");
        ProgramContext context = new ProgramContext(null, null, null, new LabelPositionHolder(), 20, false);
        assertUnmaterialized(context);
        program.tick(context);
        context.clearInputsForLabel("absent");
        context.clearAllInputs();
        assertUnmaterialized(context);
        assertTrue(context.didSomething(), "Due triggers count as execution even without a successful transfer");
    }

    @Test
    void forgetRetainsPrivateMaterializedCachesWhileFreshContextsGetFreshQuotas() throws Exception {
        Fixture fixture = new Fixture();
        fixture.stock(0, 8);
        Program program = parse("EVERY 20 TICKS DO INPUT 1 FROM source OUTPUT 3 TO dest FORGET source END");
        List<Statement> statements = program.getTriggers()
            .get(0)
            .getBlock()
            .getStatements();
        ProgramContext first = new ProgramContext(
            fixture.world,
            null,
            new CableNetwork(fixture.world, MANAGER),
            fixture.labels,
            20,
            false);
        first.beginDataInvocation(0, false);
        statements.get(0)
            .tick(first);
        for (String name : MAPS) assertSame(Collections.emptyMap(), storage(first, name), name);
        statements.get(1)
            .tick(first);
        assertEquals(1, count(fixture.dest));
        statements.get(1)
            .tick(first);
        assertEquals(1, count(fixture.dest), "Repeated OUTPUT must not renew the captured INPUT quota");
        Object registrations = storage(first, "inputs");
        Object itemCache = storage(first, "inputCache");
        Object positions = storage(first, "inputPositions");
        Object fluidQuotas = storage(first, "fluidInputQuotas");
        assertTrue(itemCache instanceof IdentityHashMap);
        assertTrue(positions instanceof IdentityHashMap);
        assertTrue(fluidQuotas instanceof IdentityHashMap);
        // No fluid was observed, so retention storage is still absent.
        assertSame(Collections.emptyMap(), storage(first, "fluidReservations"));
        assertSame(Collections.emptyMap(), storage(first, "reservedFluids"));
        statements.get(2)
            .tick(first);
        assertTrue(((List<?>) registrations).isEmpty());
        assertTrue(((Map<?, ?>) itemCache).isEmpty());
        assertTrue(((Map<?, ?>) positions).isEmpty());
        assertTrue(((Map<?, ?>) fluidQuotas).isEmpty());
        statements.get(0)
            .tick(first);
        statements.get(1)
            .tick(first);
        assertSame(registrations, storage(first, "inputs"));
        assertSame(itemCache, storage(first, "inputCache"));
        assertSame(positions, storage(first, "inputPositions"));
        assertSame(fluidQuotas, storage(first, "fluidInputQuotas"));
        assertEquals(2, count(fixture.dest));
        ProgramContext second = new ProgramContext(
            fixture.world,
            null,
            new CableNetwork(fixture.world, MANAGER),
            fixture.labels,
            20,
            false);
        assertUnmaterialized(second);
        program.tick(second);
        assertNotSame(registrations, storage(second, "inputs"));
        assertNotSame(itemCache, storage(second, "inputCache"));
        assertNotSame(positions, storage(second, "inputPositions"));
        assertNotSame(fluidQuotas, storage(second, "fluidInputQuotas"));
        assertEquals(3, count(fixture.dest));
        assertEquals(5, count(fixture.source));
    }
}
