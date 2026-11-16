package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.gui.syntax.InsertCatalog;
import ca.teamdman.sfm.client.gui.syntax.InsertCatalog.Card;
import ca.teamdman.sfm.client.gui.syntax.InsertCatalog.Match;
import ca.teamdman.sfm.client.gui.syntax.InsertNavigation;
import ca.teamdman.sfm.client.gui.syntax.InsertNavigation.Section;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class InsertNavigationTest {

    private final InsertCatalog catalog = InsertCatalog.INSTANCE;

    @Test
    void allCardsHaveARouteAndOnlyCompleteProgramsAppearAsTemplates() {
        Set<Card> reachable = new HashSet<>();
        for (Section section : Section.values()) for (Match match : search(section, "")) {
            reachable.add(match.card);
            assertEquals(section == Section.TEMPLATES, InsertNavigation.wholeProgram(match.card));
            assertFalse(
                InsertNavigation.kind(match.card.role)
                    .isEmpty());
        }
        assertEquals(new HashSet<>(catalog.cards), reachable);
        assertEquals(5, search(Section.TEMPLATES, "").size());
        assertTrue(Section.NBT.includes(catalog.card("example_query_item_nbt")));
    }

    @Test
    void itemAndFluidSectionsBothOfferCompleteIoAndIndependentInputAndOutput() {
        for (Section section : new Section[] { Section.ITEMS, Section.FLUIDS }) {
            for (String id : new String[] { "io_basic", "io_input_all", "io_output_all" }) {
                Match match = match(section, "", id);
                assertEquals(
                    1,
                    InsertNavigation.forms(match.card, section, "")
                        .size());
                if (section == Section.FLUIDS) {
                    assertTrue(match.form.code.contains("fluid:*"));
                    if (id.equals("io_basic")) assertTrue(
                        match.form.field("inputs")
                            .contains("mB"));
                } else assertFalse(match.form.code.contains("fluid:"));
                String code = match.form.code.replace("<source>", "source")
                    .replace("<destination>", "destination");
                ProgramBuildResult built = new ProgramBuilder("every 20 ticks {\n" + code + "\n}").build();
                assertTrue(built.isSuccess(), built.getErrorMessage());
            }
        }
    }

    @Test
    void globalSearchHasNoHiddenCategoryAndChoosesTheNamedForm() {
        Match divide = match(Section.FLUIDS, "divide", "math_arithmetic");
        assertTrue(
            divide.form.name.toLowerCase(java.util.Locale.ROOT)
                .contains("divid"));
        assertEquals(search(Section.ITEMS, "divide").size(), search(Section.FLUIDS, "divide").size());
        assertEquals(search(Section.FLUIDS, "").size(), search(Section.FLUIDS, "   ").size());
        assertTrue(search(Section.ITEMS, "nonexistent_insert_123").isEmpty());
    }

    @Test
    void fluidFormsStayScopedUntilAnExplicitGlobalSearch() {
        Match stock = match(Section.FLUIDS, "", "io_input_retain");
        assertTrue(stock.form.code.contains("fluid:water"));
        assertTrue(
            stock.form.field("inputs")
                .contains("mB"));
        assertEquals(
            1,
            InsertNavigation.forms(stock.card, Section.FLUIDS, "")
                .size());
        assertTrue(
            InsertNavigation.forms(stock.card, Section.FLUIDS, "iron")
                .size() > 1);
    }

    @Test
    void mixedCategoriesKeepKindsAndNumericComparisonsAreDiscoverable() {
        Set<String> kinds = new HashSet<>();
        for (Match match : search(Section.REDSTONE, "")) kinds.add(match.card.role);
        assertTrue(kinds.contains("triggers"));
        assertTrue(kinds.contains("conditions"));
        assertTrue(kinds.contains("read"));
        assertTrue(kinds.contains("transfers"));
        assertNotNull(match(Section.MATH, "", "math_compare"));
        assertTrue(Section.MATH.role.isEmpty());
    }

    @Test
    void relatedRoutesPreferTheCurrentCategoryAndDynamicCallsRemainDiscoverable() {
        Card example = catalog.card("example_query_item_nbt");
        assertEquals(Section.NBT, InsertNavigation.sectionFor(example, Section.NBT));
        assertEquals(
            Section.TEMPLATES,
            InsertNavigation.sectionFor(catalog.card("example_first_transfer"), Section.NBT));
        List<Card> contextual = catalog
            .withContext("integer Mine.Find(integer n){return n}", Collections.singleton("箱"));
        assertTrue(
            InsertNavigation.search(contextual, Section.FUNCTIONS, "")
                .stream()
                .anyMatch(m -> m.card.id.equals("call:Mine.Find")));
        assertTrue(
            InsertNavigation.search(contextual, Section.CONTAINERS, "")
                .stream()
                .anyMatch(m -> m.card.id.equals("label:箱")));
    }

    private List<Match> search(Section section, String query) {
        return InsertNavigation.search(catalog.cards, section, query);
    }

    private Match match(Section section, String query, String id) {
        return search(section, query).stream()
            .filter(m -> m.card.id.equals(id))
            .findFirst()
            .orElseThrow(AssertionError::new);
    }
}
