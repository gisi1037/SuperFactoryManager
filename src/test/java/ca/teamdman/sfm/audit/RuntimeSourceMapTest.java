package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.junit.jupiter.api.Test;

import ca.teamdman.langs.SFMLLexer;
import ca.teamdman.sfm.common.program.RuntimeSourceMap;
import ca.teamdman.sfml.data.SourceLocation;

class RuntimeSourceMapTest {

    private static final String SOURCE = "EVERY 20 TICKS {\n  OUTPUT 1 REDSTONE LEVEL TO \"signal\";\n}";

    @Test
    void leadingCommentsAndIndentationMapTheOriginalStatementAndRetainItsKind() {
        SourceLocation compiled = token(SOURCE, SFMLLexer.OUTPUT);
        String edited = "-- formatting\n" + SOURCE.replace("  OUTPUT", "     OUTPUT");
        SourceLocation mapped = RuntimeSourceMap.remapForSave(null, SOURCE, edited)
            .map(compiled);
        assertEquals(3, mapped.getLine());
        assertEquals(5, mapped.getColumn());
        assertEquals(compiled.getStatementKind(), mapped.getStatementKind());
    }

    @Test
    void repeatedEditsComposeFromOriginalKeysAndLeaveEarlierMapsImmutable() {
        SourceLocation compiled = token(SOURCE, SFMLLexer.OUTPUT);
        String first = "-- first\n" + SOURCE;
        String second = "-- second\n\n" + SOURCE.replace("  OUTPUT", "      OUTPUT");
        RuntimeSourceMap one = RuntimeSourceMap.remapForSave(null, SOURCE, first);
        RuntimeSourceMap two = RuntimeSourceMap.remapForSave(one, first, second);
        RuntimeSourceMap restored = RuntimeSourceMap.remapForSave(two, second, SOURCE);
        assertEquals(
            3,
            one.map(compiled)
                .getLine());
        assertEquals(
            2,
            one.map(compiled)
                .getColumn());
        assertEquals(
            4,
            two.map(compiled)
                .getLine());
        assertEquals(
            6,
            two.map(compiled)
                .getColumn());
        assertSame(compiled, restored.map(compiled));
    }

    @Test
    void unchangedSourcesAndTrailingLayoutCommentsIgnoreEofMovement() {
        SourceLocation compiled = token(SOURCE, SFMLLexer.OUTPUT);
        RuntimeSourceMap identical = RuntimeSourceMap.remapForSave(null, SOURCE, SOURCE);
        assertSame(compiled, identical.map(compiled));
        RuntimeSourceMap layout = RuntimeSourceMap
            .remapForSave(identical, SOURCE, SOURCE + "\n\n-- sfm:blueprint:v2 abc\n");
        assertSame(compiled, layout.map(compiled));
    }

    @Test
    void unicodeUsesTheSameLexerCoordinatesAsCompiledAnnotations() {
        String before = "NAME \"装置😀\" EVERY 20 TICKS { OUTPUT 1 REDSTONE LEVEL TO \"信号😀\"; }";
        String after = "-- 😀\n    " + before;
        SourceLocation compiled = token(before, SFMLLexer.OUTPUT), expected = token(after, SFMLLexer.OUTPUT);
        SourceLocation mapped = RuntimeSourceMap.remapForSave(null, before, after)
            .map(compiled);
        assertEquals(expected.getLine(), mapped.getLine());
        assertEquals(expected.getColumn(), mapped.getColumn());
    }

    @Test
    void invalidOrChangedExecutablesAndStaleMapCoordinatesAreRejected() {
        assertThrows(
            IllegalArgumentException.class,
            () -> RuntimeSourceMap.remapForSave(null, SOURCE, SOURCE.replace("OUTPUT 1", "OUTPUT 2")));
        assertThrows(
            IllegalArgumentException.class,
            () -> RuntimeSourceMap.remapForSave(null, SOURCE, SOURCE + '\u0000'));
        assertThrows(
            IllegalArgumentException.class,
            () -> RuntimeSourceMap.remapForSave(null, SOURCE + '\u0000', SOURCE));
        assertThrows(IllegalArgumentException.class, () -> RuntimeSourceMap.remapForSave(null, null, SOURCE));
        RuntimeSourceMap mapped = RuntimeSourceMap.remapForSave(null, SOURCE, "-- first\n" + SOURCE);
        assertThrows(IllegalArgumentException.class, () -> RuntimeSourceMap.remapForSave(mapped, SOURCE, SOURCE));
    }

    @Test
    void unknownLocationsAndEmptyExecutablesRemainSafeAndMapsRetainNoSourceBuffers() throws Exception {
        RuntimeSourceMap mapped = RuntimeSourceMap.remapForSave(null, SOURCE, "-- header\n" + SOURCE);
        SourceLocation unknown = new SourceLocation(900, 77, "Fixture");
        assertSame(unknown, mapped.map(unknown));
        assertNull(mapped.map(null));
        assertSame(
            unknown,
            RuntimeSourceMap.remapForSave(null, "-- one", "-- two\n")
                .map(unknown));
        for (Field field : RuntimeSourceMap.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            assertTrue(Modifier.isFinal(field.getModifiers()));
            field.setAccessible(true);
            Object value = field.get(mapped);
            assertTrue(value instanceof int[] || value instanceof long[], "Source maps retain coordinates only");
        }
    }

    private static SourceLocation token(String source, int type) {
        SFMLLexer lexer = new SFMLLexer(CharStreams.fromString(source));
        while (true) {
            Token token = lexer.nextToken();
            if (token.getType() == type)
                return new SourceLocation(token.getLine(), token.getCharPositionInLine(), "OutputStatement");
            if (token.getType() == Token.EOF) throw new AssertionError("Missing token");
        }
    }
}
