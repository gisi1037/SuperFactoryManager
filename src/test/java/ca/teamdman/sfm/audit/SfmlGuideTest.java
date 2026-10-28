package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

/** Keep player-facing complete examples executable as SFML evolves. Fragments/proposals use text fences. */
class SfmlGuideTest {

    @TestFactory
    List<DynamicTest> guideProgramsCompile() throws Exception {
        String guide = new String(Files.readAllBytes(Paths.get("docs/SFML_GUIDE.md")), StandardCharsets.UTF_8);
        Matcher blocks = Pattern.compile("(?s)```sfml\\s*\\r?\\n(.*?)```")
            .matcher(guide);
        List<DynamicTest> tests = new ArrayList<>();
        while (blocks.find()) {
            String source = blocks.group(1);
            String title = "Guide example " + (tests.size() + 1);
            tests.add(DynamicTest.dynamicTest(title, () -> {
                ProgramBuildResult result = new ProgramBuilder(source).build();
                assertTrue(result.isSuccess(), result.getErrorMessage() + "\n" + source);
            }));
        }
        assertFalse(tests.isEmpty(), "Guide must contain complete SFML examples");
        return tests;
    }
}
