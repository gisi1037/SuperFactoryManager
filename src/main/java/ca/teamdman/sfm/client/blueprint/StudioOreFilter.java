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

/** One pending item-only legacy ore filter. Control state also matters when Off emits no source. */
public final class StudioOreFilter {

    public enum Mode {
        OFF,
        WITH,
        WITHOUT
    }

    private Mode mode;
    private String pattern;

    public StudioOreFilter(String source) {
        if (source == null) throw new IllegalArgumentException("Missing ore filter");
        if (source.isEmpty()) {
            mode = Mode.OFF;
            pattern = "";
        } else {
            SFMLParser.WithContext parsed = parse(source);
            mode = parsed.WITHOUT() == null ? Mode.WITH : Mode.WITHOUT;
            pattern = text(source, ((SFMLParser.WithTagContext) parsed.withClause()).tagMatcher());
        }
    }

    public StudioOreFilter(StudioOreFilter other) {
        mode = other.mode;
        pattern = other.pattern;
    }

    public Mode mode() {
        return mode;
    }

    public String pattern() {
        return pattern;
    }

    public boolean setMode(Mode value) {
        if (value == null) throw new IllegalArgumentException("Choose Off, With or Without");
        if (mode == value) return false;
        mode = value;
        return true;
    }

    /** Keep invalid text for correction, including while Off; Apply validates enabled filters only. */
    public boolean setPattern(String value) {
        if (value == null || value.length() > BlueprintGraph.MAX_CLAUSE)
            throw new IllegalArgumentException("Ore pattern exceeds 4096 characters");
        if (pattern.equals(value)) return false;
        pattern = value;
        return true;
    }

    public boolean sameContent(StudioOreFilter other) {
        return other != null && mode == other.mode && pattern.equals(other.pattern);
    }

    public String source() {
        return mode == Mode.OFF ? "" : mode.name() + " #" + pattern;
    }

    /** Validate the pattern separately: an empty cell must never become a missing filter by accident. */
    public void validate() {
        if (mode == Mode.OFF) return;
        try {
            SFMLParser parser = parser(pattern);
            parser.tagMatcher();
            requireEnd(parser);
        } catch (ParseCancellationException invalid) {
            throw new IllegalArgumentException(
                "Enter one OreDictionary pattern; use * wildcards, not ? or regex",
                invalid);
        }
    }

    static SFMLParser.WithContext parse(String source) {
        try {
            SFMLParser parser = parser(source);
            SFMLParser.WithContext result = parser.with();
            requireEnd(parser);
            if (!(result.withClause() instanceof SFMLParser.WithTagContext))
                throw new IllegalArgumentException("Use one WITH/WITHOUT OreDictionary tag");
            return result;
        } catch (ParseCancellationException invalid) {
            throw new IllegalArgumentException("Use one WITH/WITHOUT OreDictionary tag", invalid);
        }
    }

    private static SFMLParser parser(String source) {
        if (source == null || source.length() > BlueprintGraph.MAX_CLAUSE
            || source.indexOf('\n') >= 0
            || source.indexOf('\r') >= 0
            || source.indexOf('\0') >= 0) throw new IllegalArgumentException("Ore filter must be one bounded line");
        SFMLLexer lexer = new SFMLLexer(CharStreams.fromString(source));
        lexer.removeErrorListeners();
        lexer.addErrorListener(new BaseErrorListener() {

            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int column,
                String message, RecognitionException error) {
                throw new ParseCancellationException(message);
            }
        });
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        tokens.fill();
        for (Token token : tokens.getTokens()) if (token.getType() == SFMLLexer.LINE_COMMENT)
            throw new IllegalArgumentException("Comments are not OreDictionary patterns");
        SFMLParser parser = new SFMLParser(tokens);
        parser.removeErrorListeners();
        parser.setErrorHandler(new BailErrorStrategy());
        return parser;
    }

    private static void requireEnd(SFMLParser parser) {
        if (parser.getCurrentToken()
            .getType() != Token.EOF)
            throw new IllegalArgumentException("Enter only one OreDictionary pattern, without extra clauses");
    }

    static String text(String source, org.antlr.v4.runtime.ParserRuleContext context) {
        return source.substring(
            source.offsetByCodePoints(
                0,
                context.getStart()
                    .getStartIndex()),
            source.offsetByCodePoints(
                0,
                context.getStop()
                    .getStopIndex() + 1));
    }

    /** Preserve TAG/# spelling, whitespace and keyword case unless that particular value changes. */
    static String replaceExisting(String original, String replacement) {
        SFMLParser.WithContext before = parse(original), after = parse(replacement);
        SFMLParser.TagMatcherContext oldPattern = ((SFMLParser.WithTagContext) before.withClause()).tagMatcher();
        String pattern = text(replacement, ((SFMLParser.WithTagContext) after.withClause()).tagMatcher());
        int start = original.offsetByCodePoints(
            0,
            oldPattern.getStart()
                .getStartIndex());
        int end = original.offsetByCodePoints(
            0,
            oldPattern.getStop()
                .getStopIndex() + 1);
        String result = original.substring(0, start) + pattern + original.substring(end);
        if ((before.WITHOUT() != null) != (after.WITHOUT() != null)) {
            Token keyword = before.getStart();
            int left = original.offsetByCodePoints(0, keyword.getStartIndex());
            int right = original.offsetByCodePoints(0, keyword.getStopIndex() + 1);
            result = result.substring(0, left) + (after.WITHOUT() == null ? "WITH" : "WITHOUT")
                + result.substring(right);
        }
        return result;
    }
}
