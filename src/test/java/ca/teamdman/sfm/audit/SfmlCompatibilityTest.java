package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

/** Data fixtures are the shared starting contract for later executor/front-end comparisons. */
class SfmlCompatibilityTest {

    @TestFactory
    List<DynamicTest> versionOneCoreCorpus() throws Exception {
        List<DynamicTest> cases = new ArrayList<>();
        InputStream input = getClass().getResourceAsStream("/sfml/v1/core.tsv");
        assertNotNull(input);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] fields = line.split("\t", -1);
                assertEquals(7, fields.length, line);
                cases.add(DynamicTest.dynamicTest(fields[0], () -> check(fields)));
            }
        }
        assertFalse(cases.isEmpty());
        return cases;
    }

    private void check(String[] fields) throws Exception {
        ProgramBuildResult result = new ProgramBuilder(fields[6]).build();
        if (!fields[5].isEmpty()) {
            assertFalse(result.isSuccess());
            assertTrue(
                result.getErrorMessage()
                    .contains(fields[5]),
                result.getErrorMessage());
            return;
        }
        assertTrue(result.isSuccess(), result.getErrorMessage());
        AuditRegressionTest.Fixture f = new AuditRegressionTest.Fixture();
        f.stock(0, Integer.parseInt(fields[1]));
        int initialDest = Integer.parseInt(fields[2]);
        if (initialDest > 0) f.dest.setInventorySlotContents(0, new ItemStack(AuditRegressionTest.ITEM, initialDest));
        result.getProgram()
            .tick(
                new ProgramContext(
                    f.world,
                    null,
                    new CableNetwork(f.world, AuditRegressionTest.MANAGER),
                    f.labels,
                    20,
                    false));
        assertEquals(Integer.parseInt(fields[3]), AuditRegressionTest.count(f.source));
        assertEquals(Integer.parseInt(fields[4]), AuditRegressionTest.count(f.dest));
        assertEquals(
            Integer.parseInt(fields[1]) + initialDest,
            AuditRegressionTest.count(f.source) + AuditRegressionTest.count(f.dest));
    }
}
