package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class ParserNestingRegressionTest {

    @Test
    void structuralKeywordsAsFieldsAndMembersDoNotAccumulateNesting() {
        StringBuilder source = new StringBuilder("EVERY 20 TICKS DO\n");
        for (int i = 0; i < 128; i++) {
            source.append("LET $v")
                .append(i)
                .append(" = {IF: 1, DO: 2, END: 3}\n");
            source.append("SET $v")
                .append(i)
                .append(".IF = $v")
                .append(i)
                .append(".DO\n");
        }
        source.append("END");
        accepted(source.toString());
    }

    @Test
    void keywordFunctionNamesAndFlatElseIfBranchesAreNotNestedBlocks() {
        String functions = "FUNCTION IF() RETURNS INTEGER DO RETURN 1 END\n"
            + "FUNCTION DO() RETURNS INTEGER DO RETURN 2 END\n"
            + "FUNCTION END() RETURNS INTEGER DO RETURN 3 END\n";
        StringBuilder source = new StringBuilder(functions + "EVERY 20 TICKS DO\n");
        for (int i = 0; i < 128; i++) {
            source.append("LET $v")
                .append(i)
                .append(" = IF() + DO() + END()\n");
        }
        source.append("IF FALSE THEN\n");
        for (int i = 0; i < 160; i++) source.append("ELSE IF FALSE THEN\n");
        source.append("ELSE FORGET END\nEND");
        accepted(source.toString());
    }

    @Test
    void endFieldsCannotHideDeepBlocksAndTheParserRejectsBeforeTheirChildren() {
        StringBuilder source = new StringBuilder("EVERY 20 TICKS DO\n");
        for (int i = 0; i < 140; i++) {
            source.append("LET $v")
                .append(i)
                .append(" = {END: 0}\nIF TRUE THEN\n");
        }
        for (int i = 0; i < 141; i++) source.append("END\n");
        ProgramBuildResult result = new ProgramBuilder(source.toString()).build();
        assertFalse(result.isSuccess());
        assertTrue(
            result.getErrorMessage()
                .contains("Program nesting exceeds 128"),
            result.getErrorMessage());
        assertTrue(
            result.getErrorMessage()
                .contains("Line "),
            result.getErrorMessage());
    }

    @Test
    void delimiterAndUnaryPrechecksStillRejectExcessiveNesting() {
        StringBuilder brackets = new StringBuilder("EVERY 20 TICKS DO LET $x = ");
        for (int i = 0; i < 33; i++) brackets.append('[');
        brackets.append('1');
        for (int i = 0; i < 33; i++) brackets.append(']');
        brackets.append(" END");
        assertFalse(
            new ProgramBuilder(brackets.toString()).build()
                .isSuccess());
        StringBuilder unary = new StringBuilder("EVERY 20 TICKS DO LET $x = ");
        for (int i = 0; i < 33; i++) unary.append('~');
        unary.append("1 END");
        ProgramBuildResult result = new ProgramBuilder(unary.toString()).build();
        assertFalse(result.isSuccess());
        assertTrue(
            result.getErrorMessage()
                .contains("Data expression nesting exceeds 32"));
    }

    private static void accepted(String source) {
        ProgramBuildResult result = new ProgramBuilder(source).build();
        assertTrue(result.isSuccess(), result.getErrorMessage());
    }
}
