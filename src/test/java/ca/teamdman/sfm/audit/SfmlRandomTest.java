package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfml.data.DataCompiler;
import ca.teamdman.sfml.data.DataExpression;
import ca.teamdman.sfml.data.DataRuntime;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class SfmlRandomTest {

    @Test
    void inclusiveBoundsSupportTheFullSignedIntegerRangeWithoutOverflow() {
        String[] ranges = { "-9223372036854775808,-9223372036854775808", "9223372036854775807,9223372036854775807",
            "-9223372036854775808,9223372036854775807", "-4,3", "9223372036854775806,9223372036854775807", "0,0" };
        for (String range : ranges) {
            String[] ends = range.split(",");
            long min = Long.parseLong(ends[0]), max = Long.parseLong(ends[1]);
            DataExpression expression = new DataCompiler().compile("random(" + range + ")", 1, 0);
            DataRuntime runtime = new DataRuntime(null, 0);
            for (int i = 0; i < 100; i++) {
                long value = expression.evaluate(runtime)
                    .asLong();
                assertTrue(value >= min && value <= max, range);
            }
        }
    }

    @Test
    void rejectsBadRangesAndTypesAndCannotBecomeAConstantStateDefault() {
        DataExpression invalid = new DataCompiler().compile("RANDOM(5,4)", 1, 0);
        assertThrows(IllegalArgumentException.class, () -> invalid.evaluate(new DataRuntime(null, 0)));
        for (String source : new String[] { "state phase=random(0,19) every 20 ticks{}",
            "every 20 ticks {let value=random(true,4)}", "every 20 ticks {let value=random(4)}" })
            assertFalse(
                new ProgramBuilder(source).build()
                    .isSuccess(),
                source);
        assertTrue(
            new ProgramBuilder("let phase=random(0,19) every 20 ticks {print(phase)}").build()
                .isSuccess());
    }

    @Test
    void exactNamePlayerFunctionsStillShadowTheDefault() {
        DataCompiler compiler = new DataCompiler();
        assertTrue(
            new ProgramBuilder(
                "integer random(integer low,integer high){return 42;} every 20 ticks {print(random(1,2))}").build()
                    .isSuccess());
        assertEquals(
            7,
            compiler.compile("rAnDoM(7,7)", 1, 0)
                .evaluate(new DataRuntime(null, 0))
                .asLong());
    }
}
