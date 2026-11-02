package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class InGameChecklistExamplesTest {

    @Test
    void everyCopyableChecklistProgramCompilesAgainstTheRealCompiler() throws Exception {
        String document = new String(
            Files.readAllBytes(Paths.get("docs/IN_GAME_TEST_CHECKLIST.md")),
            StandardCharsets.UTF_8);
        Matcher examples = Pattern.compile("(?s)```sfml\\s*\\R(.*?)```")
            .matcher(document);
        int count = 0;
        while (examples.find()) {
            ProgramBuildResult result = new ProgramBuilder(examples.group(1)).build();
            assertTrue(result.isSuccess(), "Checklist program " + (++count) + ": " + result.getErrorMessage());
        }
        assertEquals(13, count);
    }
}
