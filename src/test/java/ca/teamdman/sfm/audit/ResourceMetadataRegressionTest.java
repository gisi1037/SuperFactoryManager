package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfml.ast.ResourceIdentifier;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class ResourceMetadataRegressionTest {

    @Test
    void malformedMetadataAndExtraSegmentsFailCompilationInsteadOfBroadeningTransfers() {
        String[] selectors = { "item:minecraft:wool:2147483648", "item:minecraft:wool:abc", "item:minecraft:wool:-2",
            "item:minecraft:wool:1.0", "item:minecraft:wool:+1", "item:minecraft:wool:14:extra",
            "item:minecraft:wool:14:" };
        for (String selector : selectors) {
            assertThrows(IllegalArgumentException.class, () -> ResourceIdentifier.fromString(selector), selector);
            ProgramBuildResult result = new ProgramBuilder(
                "EVERY 20 TICKS DO INPUT \"" + selector + "\" FROM source OUTPUT TO dest END").build();
            assertFalse(result.isSuccess(), selector);
            assertNull(result.getProgram(), "Rejected selectors must never publish an executable plan");
        }
    }

    @Test
    void numericMetadataAndExplicitHistoricalWildcardAliasesRemainAvailable() {
        for (String wildcard : new String[] { "", "*", "-1" }) {
            assertEquals(
                -1,
                ResourceIdentifier.fromString("item:minecraft:wool:" + wildcard)
                    .getMetadata());
        }
        assertEquals(
            14,
            ResourceIdentifier.fromString("item:minecraft:wool:14")
                .getMetadata());
        assertEquals(
            Integer.MAX_VALUE,
            ResourceIdentifier.fromString("item:minecraft:wool:2147483647")
                .getMetadata());
        assertEquals(
            32767,
            ResourceIdentifier.fromString("item:minecraft:wool:32767")
                .getMetadata());
    }
}
