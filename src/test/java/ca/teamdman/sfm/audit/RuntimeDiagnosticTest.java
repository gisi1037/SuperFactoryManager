package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.net.PacketDebugTerminalReply;
import ca.teamdman.sfm.common.program.RuntimeDiagnostic;
import ca.teamdman.sfml.data.SfmlRuntimeException;
import ca.teamdman.sfml.data.SourceLocation;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

class RuntimeDiagnosticTest {

    @Test
    void showsLocationFunctionAndActualCauseWithOneBasedDisplayColumns() {
        SfmlRuntimeException failure = SfmlRuntimeException.inFunction(
            "findempty",
            SfmlRuntimeException.at(
                new SourceLocation(9, 4, "Query"),
                new IllegalArgumentException("Query unavailable for LABEL(assembler8)")));
        List<String> lines = RuntimeDiagnostic.capture(failure, 123, true)
            .lines();
        assertTrue(
            lines.get(0)
                .contains("manual Run"));
        assertTrue(lines.contains("SFML line 9, column 5 | Query"));
        assertTrue(lines.contains("Function: findempty"));
        assertTrue(lines.contains("Query unavailable for LABEL(assembler8)"));
        assertThrows(UnsupportedOperationException.class, () -> lines.add("changed"));
    }

    @Test
    void deepFunctionContextsNeverCrowdOutTheRootCause() {
        SfmlRuntimeException failure = SfmlRuntimeException
            .at(new SourceLocation(3, 0, "Expression"), new ArithmeticException("/ by zero"));
        for (int i = 0; i < 16; i++) failure = SfmlRuntimeException.inFunction("helper" + i, failure);
        List<String> lines = RuntimeDiagnostic.capture(failure, 0, false)
            .lines();
        assertTrue(lines.contains("Cause: ArithmeticException"));
        assertTrue(lines.contains("/ by zero"));
        assertTrue(lines.contains("Additional function frames: 12"));
        assertTrue(lines.size() <= RuntimeDiagnostic.MAX_LINES);
    }

    @Test
    void hugeProviderMessagesAreSanitizedBoundedAndFitTheActualTerminalCodec() {
        RuntimeDiagnostic diagnostic = RuntimeDiagnostic.capture(
            new IllegalArgumentException(
                "native\n\u00a7c\u202e" + String.join("", java.util.Collections.nCopies(100000, "x")) + "tail"),
            9,
            false);
        assertTrue(
            diagnostic.lines()
                .stream()
                .allMatch(line -> line.length() <= 512 && line.indexOf('\n') < 0));
        assertTrue(
            diagnostic.lines()
                .toString()
                .contains("tail"));
        PacketDebugTerminalReply sent = new PacketDebugTerminalReply(
            0,
            0,
            0,
            1,
            1,
            false,
            0,
            diagnostic.lines(),
            java.util.Collections.emptyList());
        ByteBuf wire = Unpooled.buffer();
        try {
            sent.toBytes(wire);
            PacketDebugTerminalReply received = new PacketDebugTerminalReply();
            received.fromBytes(wire);
            assertFalse(wire.isReadable());
        } finally {
            wire.release();
        }
    }

    @Test
    void cyclicCausesTerminateAndTheStoredRecordNeverRetainsAnException() throws Exception {
        RuntimeException cyclic = new RuntimeException("cyclic") {

            @Override
            public synchronized Throwable getCause() {
                return this;
            }
        };
        RuntimeDiagnostic result = RuntimeDiagnostic.capture(cyclic, 0, false);
        assertTrue(
            result.lines()
                .contains("cyclic"));
        for (Field field : RuntimeDiagnostic.class.getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) continue;
            field.setAccessible(true);
            assertFalse(field.get(result) instanceof Throwable);
        }
    }

    @Test
    void budgetFailuresExplainThatIntervalsDoNotRaisePerInvocationLimits() {
        String text = RuntimeDiagnostic
            .capture(new IllegalArgumentException("Query provider budget exhausted"), 0, false)
            .lines()
            .toString();
        assertTrue(text.contains("does not increase a per-run limit"));
    }
}
