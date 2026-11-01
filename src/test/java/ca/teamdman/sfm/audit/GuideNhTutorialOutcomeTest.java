package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfml.ast.Program;

/** Execute the actual book examples against ordinary inventory fixtures, not duplicated test programs. */
class GuideNhTutorialOutcomeTest {

    @Test
    void firstTransferAndReplenishmentMatchThePrintedCounts() throws Exception {
        Fixture f = new Fixture();
        f.stock(0, 8);
        Program program = example("start/first-transfer.md", "First transfer");
        tick(f, program, 20);
        assertEquals(7, count(f.source));
        assertEquals(1, count(f.dest));
        f.dest.decrStackSize(0, 1);
        tick(f, program, 40);
        assertEquals(6, count(f.source));
        assertEquals(1, count(f.dest));
    }

    @Test
    void batchingLimitsEachRunRatherThanFinalStock() throws Exception {
        Fixture f = new Fixture();
        f.stock(0, 8);
        Program program = example("start/change-route.md", "Two each run");
        tick(f, program, 100);
        assertEquals(6, count(f.source));
        assertEquals(2, count(f.dest));
        tick(f, program, 200);
        assertEquals(4, count(f.source));
        assertEquals(4, count(f.dest));
        tick(f, program, 300);
        tick(f, program, 400);
        tick(f, program, 500);
        assertEquals(0, count(f.source));
        assertEquals(8, count(f.dest));
    }

    @Test
    void stockTargetReplacesOnlyTheShortage() throws Exception {
        Fixture f = new Fixture();
        f.stock(0, 8);
        Program program = example("start/change-route.md", "Keep three ready");
        tick(f, program, 100);
        assertEquals(5, count(f.source));
        assertEquals(3, count(f.dest));
        f.dest.decrStackSize(0, 1);
        tick(f, program, 200);
        assertEquals(4, count(f.source));
        assertEquals(3, count(f.dest));
    }

    private static Program example(String path, String name) throws Exception {
        String page = new String(Files.readAllBytes(GuideNhBookTest.ROOT.resolve(path)), StandardCharsets.UTF_8);
        Matcher examples = Pattern.compile("(?s)```sfml\\s*\\R(.*?)```")
            .matcher(page);
        while (examples.find()) if (examples.group(1)
            .contains("\"" + name + "\"")) return parse(examples.group(1));
        throw new AssertionError("Tutorial program missing: " + name);
    }

    private static void tick(Fixture fixture, Program program, long now) {
        fixture.world.time = now;
        program.tick(
            new ProgramContext(
                fixture.world,
                null,
                new CableNetwork(fixture.world, MANAGER),
                fixture.labels,
                now,
                false));
    }
}
