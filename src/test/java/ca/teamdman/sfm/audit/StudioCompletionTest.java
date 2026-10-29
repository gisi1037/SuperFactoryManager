package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.gui.syntax.SfmlCompletionCatalog;
import ca.teamdman.sfm.client.gui.syntax.StudioCompletion;
import ca.teamdman.sfm.client.gui.syntax.StudioCompletion.Candidate;

class StudioCompletionTest {

    @Test
    void constructorAndFunctionInsertionTouchOnlyTheIdentifierRange() {
        String source = "let $target = la -- retain this comment and CASE";
        Candidate label = named(source, source.indexOf("la --") + 2, "label");
        assertEquals("let $target = label(\"<name>\") -- retain this comment and CASE", apply(source, label));
        assertFalse(
            label.getSignature()
                .isEmpty());
        assertFalse(
            label.getHelp()
                .isEmpty());
        assertEquals("let $target = ", source.substring(0, label.getStart()));
        assertEquals(" -- retain this comment and CASE", source.substring(label.getEnd()));
    }

    @Test
    void bareAndConstructedReceiversNeverDuplicateTheReceiverPrefix() {
        for (String receiver : new String[] { "$peds", "label(\"ped\")", "$peds.side(\"north\")",
            "label(\"ped\").side(\"up\").slots(0, 8)" }) {
            String source = "if(" + receiver + ".em) then\nend";
            Candidate empty = named(source, source.indexOf(".em") + 3, "empty");
            assertEquals("empty()", empty.getText());
            assertEquals(source.replace(".em", ".empty()"), apply(source, empty));
            assertFalse(
                empty.getText()
                    .contains("$<label>"));
        }
    }

    @Test
    void existingParenthesesArePreservedForFunctionsAndMethods() {
        String source = "let $a = cl(3, 0, 15)";
        Candidate clamp = named(source, source.indexOf("cl(") + 2, "clamp");
        assertEquals("clamp", clamp.getText());
        assertEquals("let $a = clamp(3, 0, 15)", apply(source, clamp));
        String method = "if ($ped.em   ()) then end";
        Candidate empty = named(method, method.indexOf(".em") + 3, "empty");
        assertEquals("empty", empty.getText());
        assertEquals("if ($ped.empty   ()) then end", apply(method, empty));
    }

    @Test
    void completionInTheMiddleOfAnIdentifierReplacesItsRemainingSuffixOnce() {
        String source = "let $a = clmp(3, 0, 15) -- trailing";
        Candidate clamp = named(source, source.indexOf("clmp") + 2, "clamp");
        assertEquals("let $a = clamp(3, 0, 15) -- trailing", apply(source, clamp));
        String method = "$ped.emty()";
        assertEquals("$ped.empty()", apply(method, named(method, method.indexOf("emty") + 2, "empty")));
    }

    @Test
    void namespacedPlayerFunctionsPreserveExactCaseUnderscoresAndArgumentNames() {
        String definitions = "function Logistics_tools.Load_Items($Want_Count: INTEGER) returns INTEGER do return $Want_Count end\n";
        String source = definitions + "every 20 ticks do let $moved = logistics_tools.lo end";
        Candidate player = named(
            source,
            source.indexOf("logistics_tools.lo") + "logistics_tools.lo".length(),
            "Logistics_tools.Load_Items");
        assertEquals("Logistics_tools.Load_Items(<Want_Count>)", player.getText());
        assertEquals(
            definitions + "every 20 ticks do let $moved = Logistics_tools.Load_Items(<Want_Count>) end",
            apply(source, player));
        String existing = definitions + "every 20 ticks do let $moved = Logistics_tools.lo(4) end";
        Candidate nameOnly = named(existing, existing.lastIndexOf(".lo") + 3, "Logistics_tools.Load_Items");
        assertEquals("Logistics_tools.Load_Items", nameOnly.getText());
        assertTrue(apply(existing, nameOnly).endsWith("Logistics_tools.Load_Items(4) end"));
    }

    @Test
    void unicodeBeforeTheCaretPreservesUtf16OffsetsAndUntouchedSurroundingText() {
        String source = "name \"Factory \uD83C\uDFED\"\nevery 20 ticks do let $x = la end";
        int caret = source.lastIndexOf("la end") + 2;
        Candidate label = named(source, caret, "label");
        assertEquals(source.lastIndexOf("la end"), label.getStart());
        assertEquals(caret, label.getEnd());
        assertEquals(source.substring(0, label.getStart()) + "label(\"<name>\") end", apply(source, label));
        int surrogateMiddle = source.indexOf("\uD83C\uDFED") + 1;
        assertTrue(
            StudioCompletion.suggest(source, surrogateMiddle, 10)
                .isEmpty());
    }

    @Test
    void stringsCommentsAndIncompleteQuotedTextNeverOfferCompletions() {
        for (String source : new String[] { "-- fake function fa", "print(\"la", "print(\"escaped \\\" la",
            "-- $peds.em", "print(\"-- $peds.em", "print(\"multiline\nla" })
            assertTrue(
                StudioCompletion.suggest(source, source.length(), 20)
                    .isEmpty(),
                source);
        String complete = "print(\"la\")";
        assertTrue(
            StudioCompletion.suggest(complete, complete.indexOf("la") + 1, 20)
                .isEmpty());
        String newline = "-- do not complete this\nlet $x = la";
        assertEquals("label", named(newline, newline.length(), "label").getName());
    }

    @Test
    void quotedAndCommentedFakeDefinitionsDoNotBecomeUserFunctions() {
        String source = "-- function Ghost() returns INTEGER do return 1 end\n"
            + "let $text = \"function GhostTwo() returns INTEGER do return 1 end\"\n"
            + "every 20 ticks do let $x = gh end";
        int caret = source.lastIndexOf("gh end") + 2;
        assertTrue(
            StudioCompletion.suggest(source, caret, 20)
                .isEmpty());
    }

    @Test
    void unsupportedFieldChainsDoNotPretendToInferAReceiverType() {
        for (String source : new String[] { "$record.field.em", "$items[0].em", "$ped.contents().em",
            "$ped.available().em", "arbitrary().em", "foo.label(\"ped\").em", "$ped", "item:minecraft:la" })
            assertTrue(
                StudioCompletion.suggest(source, source.length(), 20)
                    .isEmpty(),
                source);
    }

    @Test
    void emptyMemberPrefixListsOnlyStaticLabelMethodsAndIsBounded() {
        List<Candidate> entries = StudioCompletion.suggest("$ped.", 5, 3);
        assertEquals(3, entries.size());
        for (Candidate entry : entries) assertEquals(
            SfmlCompletionCatalog.Kind.METHOD,
            entry.getEntry()
                .getKind());
        assertThrows(UnsupportedOperationException.class, entries::clear);
        assertTrue(
            StudioCompletion.suggest("unknown", 7, 20)
                .isEmpty());
        assertTrue(
            StudioCompletion.suggest("la", 2, 0)
                .isEmpty());
    }

    @Test
    void oversizedDraftsInvalidCaretsAndExcessiveTokenStreamsAreRefused() {
        String huge = new String(new char[SfmlCompletionCatalog.MAX_SOURCE_CHARACTERS + 1]).replace('\0', ' ');
        assertTrue(
            StudioCompletion.suggest(huge, huge.length(), 10)
                .isEmpty());
        assertTrue(
            StudioCompletion.suggest(null, 0, 10)
                .isEmpty());
        assertTrue(
            StudioCompletion.suggest("la", -1, 10)
                .isEmpty());
        assertTrue(
            StudioCompletion.suggest("la", 3, 10)
                .isEmpty());
        String many = new String(new char[17000]).replace('\0', '+') + "la";
        assertTrue(
            StudioCompletion.suggest(many, many.length(), 10)
                .isEmpty());
    }

    @Test
    void cachedCurrentSnapshotAndConvenienceApiReturnTheSameEdit() {
        String source = "let $x = ma";
        Candidate fresh = named(source, source.length(), "max");
        Candidate cached = StudioCompletion.suggest(source, source.length(), SfmlCompletionCatalog.scan(source), 10)
            .stream()
            .filter(
                candidate -> candidate.getName()
                    .equals("max"))
            .findFirst()
            .get();
        assertEquals(fresh.getStart(), cached.getStart());
        assertEquals(fresh.getEnd(), cached.getEnd());
        assertEquals(fresh.getText(), cached.getText());
    }

    private static Candidate named(String source, int caret, String name) {
        return StudioCompletion.suggest(source, caret, 30)
            .stream()
            .filter(
                candidate -> candidate.getName()
                    .equals(name))
            .findFirst()
            .orElseThrow(() -> new AssertionError("No " + name + " completion at " + caret + " in " + source));
    }

    private static String apply(String source, Candidate candidate) {
        return source.substring(0, candidate.getStart()) + candidate.getText() + source.substring(candidate.getEnd());
    }
}
