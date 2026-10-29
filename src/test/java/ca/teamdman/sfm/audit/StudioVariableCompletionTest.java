package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.gui.syntax.SfmlCompletionCatalog;
import ca.teamdman.sfm.client.gui.syntax.StudioCompletion;
import ca.teamdman.sfm.client.gui.syntax.StudioCompletion.Candidate;

class StudioVariableCompletionTest {

    @Test
    void globalsStateAndLocalsCompleteWithTheirExactCaseAndDollar() {
        String source = "state $Saved = 0\nlet $Shared = 1\nevery 20 ticks do let $Local = 2 print($s~) end";
        List<Candidate> suggestions = at(source);
        assertEquals(Arrays.asList("$Saved", "$Shared"), names(suggestions));
        Candidate saved = suggestions.get(0);
        assertEquals(
            SfmlCompletionCatalog.Kind.VARIABLE,
            saved.getEntry()
                .getKind());
        assertTrue(
            saved.getHelp()
                .contains("Persistent state declared on line 1"));
        String plain = source.replace("~", "");
        assertEquals(
            plain.replace("print($s)", "print($Saved)"),
            plain.substring(0, saved.getStart()) + saved.getText() + plain.substring(saved.getEnd()));
    }

    @Test
    void bareDollarAndMiddleOfANameUseOneExactReplacementRange() {
        assertEquals(
            Arrays.asList("$Items", "$Inner"),
            names(at("let $Items = [1]\nevery 20 ticks do let $Inner = 2 print($~) end")));
        String marked = "name \"\uD83C\uDFED\"\nlet $Items = [1]\nevery 20 ticks do print($It~ms) end";
        Candidate item = at(marked).get(0);
        String source = marked.replace("~", "");
        assertEquals(source.indexOf("$Itms"), item.getStart());
        assertEquals(source.indexOf("$Itms") + 5, item.getEnd());
        assertEquals("$Items", item.getText());
    }

    @Test
    void declarationsDoNotOfferTheirOwnNameOrForwardBindings() {
        assertTrue(at("let $First = $~\nlet $Later = 1").isEmpty());
        assertTrue(at("state $First = 1\nstate $Second = $~").isEmpty());
        assertTrue(at("let $First = 1\nlet $~ = 2").isEmpty());
        assertTrue(at("every 20 ticks do let $First = 1 let $~ = 2 end").isEmpty());
        assertEquals(Arrays.asList("$First"), names(at("let $First = 1\nlet $Second = $~\nlet $Later = 2")));
    }

    @Test
    void functionParametersAndLocalsExcludeProgramGlobalsAndOtherFunctions() {
        String prefix = "state $Saved = 0\nlet $Global = 1\n"
            + "function Other($OtherArg: integer) returns integer do return $OtherArg end\n";
        List<Candidate> candidates = at(
            prefix + "function Work($Argument: integer) returns integer do let $Local = 2 return $~ end");
        assertEquals(Arrays.asList("$Argument", "$Local"), names(candidates));
        assertTrue(
            candidates.get(0)
                .getHelp()
                .contains("Function parameter: integer"));
        assertTrue(at(prefix + "function Work($~: integer) returns integer do return 1 end").isEmpty());
        assertEquals(Arrays.asList("$Saved", "$Global"), names(at(prefix + "every 20 ticks do print($~) end")));
    }

    @Test
    void onlyTheCurrentTriggerAndEnclosingBlocksAreVisible() {
        String source = "let $Global = 0\nevery 20 ticks do let $Other = 1 end\n"
            + "every 40 ticks do let $Outer = 1 if true then let $Inner = 2 print($~) else let $Sibling = 3 end end";
        assertEquals(Arrays.asList("$Global", "$Outer", "$Inner"), names(at(source)));
        assertEquals(
            Arrays.asList("$Global", "$Outer"),
            names(
                at(
                    source.replace("print($~)", "print(0)")
                        .replace("else let $Sibling", "else print($~) let $Sibling"))));
        assertEquals(
            Arrays.asList("$Global", "$Outer"),
            names(
                at(
                    source.replace("print($~)", "print(0)")
                        .replace("end end", "end print($~) end"))));
    }

    @Test
    void loopBindersAreReadOnlyAndVisibleOnlyInsideTheirBody() {
        String loop = "every 20 ticks do for $Index = 0 to 4 do print($~) end end";
        assertEquals(Arrays.asList("$Index"), names(at(loop)));
        assertTrue(
            at(
                loop.replace("print($~)", "print(0)")
                    .replace("to 4", "to $~")).isEmpty());
        assertTrue(
            at(
                loop.replace("print($~)", "print(0)")
                    .replace("end end", "end print($~) end")).isEmpty());
        List<Candidate> array = at("every 20 ticks do for $Entry in [1, 2] do print($~) end end");
        assertEquals(Arrays.asList("$Entry"), names(array));
        assertTrue(
            array.get(0)
                .getHelp()
                .contains("Read-only loop variable"));
    }

    @Test
    void transferResultIsAvailableAfterTheStatementAndNotInItsDeclaration() {
        String before = "every 20 ticks do transfer(label(\"a\"), label(\"b\")) into $Moved ";
        assertEquals(Arrays.asList("$Moved"), names(at(before + "print($~) end")));
        assertTrue(at(before.replace("$Moved", "$~") + "end").isEmpty());
        assertTrue(at("every 20 ticks do print($~) transfer(label(\"a\"), label(\"b\")) into $Moved end").isEmpty());
    }

    @Test
    void quotedAndCommentedDeclarationsNeverAppear() {
        String source = "-- let $Fake = 1\nlet $Text = \"let $FakeToo = 1\"\n" + "every 20 ticks do print($~) end";
        assertEquals(Arrays.asList("$Text"), names(at(source)));
        assertTrue(at("let $Global = 0\nevery 20 ticks do print(\"$~\") end").isEmpty());
        assertTrue(at("let $Global = 0\n-- $~").isEmpty());
    }

    @Test
    void malformedAndPathologicallyNestedDraftsFailClosedWithoutChangingText() {
        assertTrue(at("let $Global = 0\nevery 20 ticks do print($~)").isEmpty());
        String nested = "let $Global = 0\nevery 20 ticks do print(" + String.join("", Collections.nCopies(40, "("))
            + "$~"
            + String.join("", Collections.nCopies(40, ")"))
            + ") end";
        assertTrue(at(nested).isEmpty());
        String unary = "let $Global = 0\nevery 20 ticks do print(" + String.join("", Collections.nCopies(50, "not "))
            + "$~) end";
        assertTrue(at(unary).isEmpty());
    }

    @Test
    void resultLimitsAndImmutabilityApplyToVariablesToo() {
        String marked = "let $First = 1\nlet $Second = 2\nevery 20 ticks do print($~) end";
        int caret = marked.indexOf('~');
        List<Candidate> result = StudioCompletion.suggest(marked.replace("~", ""), caret, 1);
        assertEquals(Arrays.asList("$First"), names(result));
        assertThrows(UnsupportedOperationException.class, result::clear);
    }

    private static List<Candidate> at(String marked) {
        int caret = marked.indexOf('~');
        assertTrue(caret >= 0);
        return StudioCompletion.suggest(marked.replace("~", ""), caret, 64);
    }

    private static List<String> names(List<Candidate> candidates) {
        return candidates.stream()
            .map(Candidate::getName)
            .collect(Collectors.toList());
    }
}
