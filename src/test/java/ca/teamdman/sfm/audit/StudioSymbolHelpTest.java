package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.gui.syntax.SfmlCompletionCatalog;
import ca.teamdman.sfm.client.gui.syntax.SfmlHighlighter;
import ca.teamdman.sfm.client.gui.syntax.StudioSymbolHelp;

class StudioSymbolHelpTest {

    private static SfmlCompletionCatalog.Entry help(String source, int offset) {
        return StudioSymbolHelp
            .at(source, SfmlHighlighter.highlight(source), SfmlCompletionCatalog.scan(source), offset);
    }

    @Test
    void builtInsAndReceiverChainsHaveTheirOwnSignatures() {
        String source = "PRINT(label(\"ped\").slots(0, 2).empty())";
        for (String name : new String[] { "PRINT", "label", "slots", "empty" }) {
            SfmlCompletionCatalog.Entry entry = help(source, source.indexOf(name) + 1);
            assertNotNull(entry, name);
            assertEquals(name.toLowerCase(java.util.Locale.ROOT), entry.getName());
            assertFalse(
                entry.getDescription()
                    .isEmpty());
        }
    }

    @Test
    void variablesFieldsSelectorsCommentsAndStringsAreNotCalls() {
        for (String source : new String[] { "-- print(1)", "\"print(1)\"", "$print", "$x.empty", "item:print",
            "let $x = {print: 1}" }) {
            assertNull(help(source, source.indexOf("print") >= 0 ? source.indexOf("print") : source.indexOf("empty")));
        }
    }

    @Test
    void playerFunctionsRetainExactNamespaceAndCase() {
        String source = "function logistics.Need($n: integer) returns integer do return $n end\n"
            + "every 20 ticks do print(logistics.Need(2)) end";
        SfmlCompletionCatalog.Entry entry = help(source, source.lastIndexOf("Need") + 1);
        assertNotNull(entry);
        assertEquals("logistics.Need", entry.getName());
        String wrongCase = source.substring(0, source.lastIndexOf("Need")) + "need(2)) end";
        assertNull(help(wrongCase, wrongCase.lastIndexOf("need") + 1));
    }

    @Test
    void supplementaryCharactersDoNotShiftHoverOffsets() {
        String source = "-- 😀\nprint(label(\"箱\").empty())";
        assertEquals("empty", help(source, source.indexOf("empty") + 2).getName());
        assertNull(help(source, source.indexOf("箱")));
        assertNull(help(source, source.length()));
    }
}
