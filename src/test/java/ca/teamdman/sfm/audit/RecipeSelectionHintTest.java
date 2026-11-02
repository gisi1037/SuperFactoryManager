package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.gui.RecipeSelectionHint;
import ca.teamdman.sfm.client.gui.RecipeSelectionHint.Value;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class RecipeSelectionHintTest {

    @Test
    void onlyOneBoundedImmutableHintBelongsToTheSameLiveWorld() {
        Object first = new Object(), second = new Object();
        RecipeSelectionHint hints = new RecipeSelectionHint();
        Value value = new Value("minecraft:flint", 0, "gt.recipe.macerator");
        hints.capture(first, value);
        assertSame(value, hints.current(first));
        hints.capture(first, new Value("minecraft:sand", 0, "gt.recipe.macerator"));
        assertEquals("minecraft:sand", hints.current(first).item);
        assertNull(hints.current(second));
        assertNull(hints.current(first), "Changing worlds must not resurrect an earlier hint");
        assertThrows(IllegalArgumentException.class, () -> hints.capture(null, value));
        assertThrows(IllegalArgumentException.class, () -> new Value("bad\"id", 0, "gt.recipe.macerator"));
        assertThrows(IllegalArgumentException.class, () -> new Value("minecraft:flint", 0, "unknown"));
    }

    @Test
    void copiedInspectionUsesFreshReferencesAndRequiresAnExplicitFingerprintWithoutChoosingFirst() {
        Value value = new Value("gregtech:gt.metaitem.01", 17032, "gt.recipe.metalbender");
        String fingerprint = String.join("", java.util.Collections.nCopies(64, "a"));
        String query = RecipeSelectionHint.query(value, fingerprint);
        assertTrue(query.contains("found.coverageComplete"));
        assertTrue(query.contains("candidate.reference.fingerprint == \"" + fingerprint + "\""));
        assertFalse(query.contains("found.recipes[0]"));
        assertTrue(
            new ProgramBuilder("name \"Inspect recipe\" every 100 ticks {\n" + query + "\n}").build()
                .isSuccess());
        assertThrows(IllegalArgumentException.class, () -> RecipeSelectionHint.query(value, "invalid"));
    }
}
