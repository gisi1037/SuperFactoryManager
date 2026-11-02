package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.gui.syntax.SfmlCompletionCatalog;
import ca.teamdman.sfm.client.gui.syntax.SfmlHighlighter;
import ca.teamdman.sfm.client.gui.syntax.StudioCallHelp;

class StudioCallHelpTest {

    private static StudioCallHelp.Help help(String text) {
        return StudioCallHelp
            .at(text, text.length(), SfmlHighlighter.highlight(text), SfmlCompletionCatalog.scan(text));
    }

    @Test
    void nestedCallsGroupsStructuresStringsAndCommentsKeepArgumentIndex() {
        assertEquals("label", help("transfer(label(").entry.getName());
        StudioCallHelp.Help outer = help("transfer(label(\"x,y\"), label(\"out\"), (1 + 2)");
        assertEquals("transfer", outer.entry.getName());
        assertEquals(2, outer.argument);
        assertEquals(1, help("print({a: [1, 2], b: 3}, ").argument);
        assertEquals(0, help("label(\"unfinished").argument);
        assertNull(help("print(1)"));
        assertNull(help("print( -- hidden"));
        assertEquals(1, help("print(1, -- ignored ,\n ").argument);
        assertNull(help("print(]"));
    }

    @Test
    void receiverAndExactPlayerFunctionHelpWorkWhileTyping() {
        assertEquals("slots", help("label(\"source\").slots(0, ").entry.getName());
        StudioCallHelp.Help player = help(
            "integer math.Need(integer n) { return n } every 20 ticks { let n = math.Need(");
        assertNotNull(player);
        assertEquals("math.Need", player.entry.getName());
        assertNull(help("integer math.Need(integer n) { return n } every 20 ticks { let n = math.need("));
        assertEquals("print", help("-- 😀\nprint(").entry.getName());
    }
}
