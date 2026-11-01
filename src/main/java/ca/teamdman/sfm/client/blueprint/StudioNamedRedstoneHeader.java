package ca.teamdman.sfm.client.blueprint;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import ca.teamdman.langs.SFMLLexer;
import ca.teamdman.langs.SFMLParser;

/** Flat legacy label headers only. Bail before an injected block can reach recursive program parsing. */
public final class StudioNamedRedstoneHeader {

    private StudioNamedRedstoneHeader() {}

    /** Read-only inspector summary. The entire suffix, including gaps and spelling, stays in the clause. */
    public static String physicalSideSuffix(String header) {
        SFMLParser.LabelAccessContext labels = parse(header).labelAccess();
        Token last = labels.label(
            labels.label()
                .size() - 1)
            .getStop();
        return header.substring(header.offsetByCodePoints(0, last.getStopIndex() + 1));
    }

    /** Field text omits SIDE; its original spelling and surrounding gaps belong to the header. */
    public static String physicalSideValue(String header) {
        SFMLParser.SidequalifierContext sides = parse(header).labelAccess()
            .sidequalifier();
        if (sides == null) return "";
        return header.substring(
            offset(
                header,
                sides.getStart()
                    .getStartIndex()),
            offset(
                header,
                sides.getStop()
                    .getStartIndex()))
            .trim();
    }

    /** Physical-only input, independent of the more permissive inventory side grammar. */
    private static String physicalSideInput(String value) {
        if (value == null || value.length() > BlueprintGraph.MAX_CLAUSE
            || value.indexOf('\n') >= 0
            || value.indexOf('\r') >= 0
            || value.indexOf('\0') >= 0) throw new IllegalArgumentException("Physical sides must be one bounded line");
        for (Token token : tokens(value).getTokens()) {
            int type = token.getType();
            if (type != Token.EOF && type != SFMLLexer.WS
                && type != SFMLLexer.EACH
                && type != SFMLLexer.COMMA
                && type != SFMLLexer.TOP
                && type != SFMLLexer.BOTTOM
                && type != SFMLLexer.NORTH
                && type != SFMLLexer.SOUTH
                && type != SFMLLexer.EAST
                && type != SFMLLexer.WEST)
                throw new IllegalArgumentException("Use EACH, physical faces separated by commas, or blank; omit SIDE");
        }
        String text = value.trim();
        if (text.isEmpty()) return "";
        SFMLParser parser = new SFMLParser(tokens(text + " SIDE"));
        parser.removeErrorListeners();
        parser.setErrorHandler(new BailErrorStrategy());
        try {
            parser.sidequalifier();
            if (parser.getCurrentToken()
                .getType() != Token.EOF) throw new ParseCancellationException();
        } catch (ParseCancellationException invalid) {
            throw new IllegalArgumentException("Use EACH or one or more physical faces separated by commas", invalid);
        }
        return text;
    }

    /** Change only the qualifier's value; never rebuild labels or an untouched SIDE keyword. */
    public static String updatePhysicalSides(String header, String value) {
        String text = physicalSideInput(value);
        SFMLParser.LabelAccessContext labels = parse(header).labelAccess();
        SFMLParser.SidequalifierContext sides = labels.sidequalifier();
        if (text.equalsIgnoreCase(physicalSideValue(header))) return header;
        String result;
        if (sides == null) {
            int at = offset(
                header,
                labels.getStop()
                    .getStopIndex() + 1);
            // The new qualifier owns its separator. All pre-existing trailing whitespace stays outside it.
            result = header.substring(0, at) + " " + text + " SIDE" + header.substring(at);
        } else {
            int start = offset(
                header,
                sides.getStart()
                    .getStartIndex());
            int end;
            if (text.isEmpty()) {
                // Removal includes SIDE but preserves both existing boundary gaps, even when the label touches it.
                end = offset(
                    header,
                    sides.getStop()
                        .getStopIndex() + 1);
            } else {
                Token last = sides instanceof SFMLParser.EachSideContext ? ((SFMLParser.EachSideContext) sides).EACH()
                    .getSymbol()
                    : ((SFMLParser.ListedSidesContext) sides).side(
                        ((SFMLParser.ListedSidesContext) sides).side()
                            .size() - 1)
                        .getStop();
                end = offset(header, last.getStopIndex() + 1);
            }
            result = header.substring(0, start) + text + header.substring(end);
        }
        BlueprintCompiler.validateClause(BlueprintGraph.Type.NAMED_REDSTONE, result);
        return result;
    }

    private static int offset(String text, int codePoint) {
        return text.offsetByCodePoints(0, codePoint);
    }

    public static SFMLParser.NamedRedstoneTriggerContext parse(String header) {
        if (header == null || header.length() > BlueprintGraph.MAX_CLAUSE
            || header.trim()
                .isEmpty()
            || header.indexOf('\n') >= 0
            || header.indexOf('\r') >= 0
            || header.indexOf('\0') >= 0)
            throw new IllegalArgumentException("A Port edge header must be one nonblank bounded line");
        CommonTokenStream original = tokens(header);
        for (Token token : original.getTokens()) {
            int type = token.getType();
            if (type == SFMLLexer.LINE_COMMENT || type == SFMLLexer.DO
                || type == SFMLLexer.END
                || type == SFMLLexer.LPAREN
                || type == SFMLLexer.RPAREN
                || type == SFMLLexer.LBRACKET
                || type == SFMLLexer.RBRACKET
                || type == SFMLLexer.LBRACE
                || type == SFMLLexer.RBRACE)
                throw new IllegalArgumentException(
                    "Use a named redstone header without DO, END, comments or expressions");
        }
        SFMLParser parser = new SFMLParser(tokens(header + " DO END"));
        parser.removeErrorListeners();
        parser.setErrorHandler(new BailErrorStrategy());
        try {
            SFMLParser.TriggerContext trigger = parser.trigger();
            if (!(trigger instanceof SFMLParser.NamedRedstoneTriggerContext) || parser.getCurrentToken()
                .getType() != Token.EOF)
                throw new IllegalArgumentException(
                    "Use EVERY REDSTONE RISING, PULSE or FALLING FROM labels [physical SIDE]");
            SFMLParser.NamedRedstoneTriggerContext named = (SFMLParser.NamedRedstoneTriggerContext) trigger;
            for (SFMLParser.LabelContext label : named.labelAccess()
                .label())
                if (!(label instanceof SFMLParser.StringLabelContext)) throw new IllegalArgumentException(
                    "Quote literal Port labels; use the code editor for label variables");
            return named;
        } catch (ParseCancellationException invalid) {
            throw new IllegalArgumentException("Use one complete named redstone header, without DO or END", invalid);
        }
    }

    private static CommonTokenStream tokens(String text) {
        SFMLLexer lexer = new SFMLLexer(CharStreams.fromString(text));
        lexer.removeErrorListeners();
        lexer.addErrorListener(new BaseErrorListener() {

            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object symbol, int line, int column, String message,
                RecognitionException cause) {
                throw new IllegalArgumentException("Invalid Port edge header: " + message);
            }
        });
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        tokens.fill();
        return tokens;
    }
}
