package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.gui.EditorDiagnostic;
import ca.teamdman.sfm.client.gui.syntax.SfmlHighlighter;
import ca.teamdman.sfm.common.program.RuntimeDiagnostic;
import ca.teamdman.sfm.common.program.RuntimeSourceMap;
import ca.teamdman.sfml.data.SfmlRuntimeException;
import ca.teamdman.sfml.data.SourceLocation;

class LibraryDiagnosticBoundaryTest {

    @Test
    void importedRuntimeCoordinatesNeverMapToOrSelectAnUnrelatedRootLine() {
        String root = "EVERY 1 TICKS DO PRINT(1) END";
        RuntimeSourceMap map = RuntimeSourceMap.remapForSave(null, root, "\n\n" + root);
        SourceLocation module = new SourceLocation(1, 0, "Assignment", "math", 3);
        assertSame(module, map.map(module));
        SfmlRuntimeException failure = SfmlRuntimeException
            .at(module, new IllegalArgumentException("Arithmetic overflow"));
        String diagnostic = String.join(
            "\n",
            RuntimeDiagnostic.capture(failure, 5, false, map)
                .lines());
        assertTrue(diagnostic.contains("Library math r3 line 1, column 1 | Assignment"), diagnostic);
        assertNull(EditorDiagnostic.first(diagnostic));
        assertEquals(
            3,
            map.map(new SourceLocation(1, 0, "Statement"))
                .getLine());
    }

    @Test
    void importHeaderColorsDoNotReservePreviouslyValidVariableOrResourceNames() {
        String header = "import math from \"00000000-0000-0000-0000-000000000001\" revision 3 digest \""
            + String.join("", java.util.Collections.nCopies(64, "a"))
            + "\"";
        SfmlHighlighter.Line line = SfmlHighlighter.highlight(header).lines.get(0);
        assertEquals(SfmlHighlighter.Style.FLOW, line.styleAt(0));
        assertEquals(SfmlHighlighter.Style.KEYWORD, line.styleAt(header.indexOf("revision")));
        assertEquals(SfmlHighlighter.Style.KEYWORD, line.styleAt(header.indexOf("digest")));
        for (String name : new String[] { "import", "revision", "digest" }) {
            String source = "EVERY 1 TICKS { LET " + name + " = 1 PRINT(" + name + ") }";
            SfmlHighlighter.Line body = SfmlHighlighter.highlight(source).lines.get(0);
            assertEquals(SfmlHighlighter.Style.VARIABLE, body.styleAt(source.indexOf(name)));
            assertEquals(SfmlHighlighter.Style.VARIABLE, body.styleAt(source.lastIndexOf(name)));
            String item = "EVERY 1 TICKS { INPUT item:minecraft:" + name + " FROM \"source\" }";
            assertEquals(
                SfmlHighlighter.Style.RESOURCE,
                SfmlHighlighter.highlight(item).lines.get(0)
                    .styleAt(item.indexOf(name)));
        }
    }
}
