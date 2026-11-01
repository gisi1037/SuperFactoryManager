package ca.teamdman.sfml.data;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import ca.teamdman.langs.SFMLLexer;
import ca.teamdman.langs.SFMLParser;

/** Compile-time type literals and function signatures share the actual language grammar and type factories. */
public final class DataTypeParser {

    public static final int MAX_SOURCE_LENGTH = 1024;

    private DataTypeParser() {}

    /** Parse one non-void type literal without compiling a program or resolving runtime values. */
    public static DataType parse(String source) {
        if (source == null || source.length() > MAX_SOURCE_LENGTH
            || source.trim()
                .isEmpty())
            throw new IllegalArgumentException("Type literal must contain 1–1024 characters");
        BaseErrorListener errors = new BaseErrorListener() {

            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object symbol, int line, int column, String message,
                RecognitionException cause) {
                throw new IllegalArgumentException("Invalid type literal: " + message);
            }
        };
        SFMLLexer lexer = new SFMLLexer(CharStreams.fromString(source));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errors);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        tokens.fill();
        int depth = 0;
        for (Token token : tokens.getTokens()) {
            int kind = token.getType();
            if (kind == SFMLLexer.LINE_COMMENT) throw new IllegalArgumentException("Comments are not type literals");
            if (kind == SFMLLexer.LBRACE || kind == SFMLLexer.LBRACKET) depth++;
            if (kind == SFMLLexer.RBRACE || kind == SFMLLexer.RBRACKET) depth--;
            if (depth > ExpressionLimits.MAX_DEPTH) throw new IllegalArgumentException("Data nesting exceeds 32");
        }
        SFMLParser parser = new SFMLParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errors);
        parser.setErrorHandler(new BailErrorStrategy());
        try {
            DataType type = fromSyntax(parser.dataType());
            if (parser.getCurrentToken()
                .getType() != Token.EOF) throw new IllegalArgumentException("Enter one complete type literal");
            if (type == DataType.VOID) throw new IllegalArgumentException("Non-void type literal required");
            return type;
        } catch (ParseCancellationException invalid) {
            throw new IllegalArgumentException("Enter one complete type literal", invalid);
        } catch (StackOverflowError invalid) {
            throw new IllegalArgumentException("Data nesting exceeds 32", invalid);
        }
    }

    /** VOID is retained only for a function return marker; all container factories reject void children. */
    public static DataType fromSyntax(SFMLParser.DataTypeContext context) {
        DataType type;
        if (context.dataName() != null) {
            String name = context.dataName()
                .getText()
                .toUpperCase(Locale.ROOT);
            switch (name) {
                case "VOID":
                    type = DataType.VOID;
                    break;
                case "INTEGER":
                    type = DataType.INTEGER;
                    break;
                case "BOOLEAN":
                    type = DataType.BOOLEAN;
                    break;
                case "STRING":
                    type = DataType.STRING;
                    break;
                case "LABEL_REF":
                    type = DataType.LABEL_REF;
                    break;
                case "RESOURCE_SELECTOR":
                    type = DataType.RESOURCE_SELECTOR;
                    break;
                case "ITEM_KEY":
                    type = DataType.ITEM_KEY;
                    break;
                case "FLUID_KEY":
                    type = DataType.FLUID_KEY;
                    break;
                case "FLUID_CONTENTS":
                    type = DataType.FLUID_CONTENTS;
                    break;
                case "ITEM_CONTENTS":
                    type = DataType.ITEM_CONTENTS;
                    break;
                case "ITEM_LOCATIONS":
                    type = DataType.ITEM_LOCATIONS;
                    break;
                default:
                    throw fail(context, "Unknown function type: " + name);
            }
        } else {
            Map<String, DataType> fields = new LinkedHashMap<>();
            for (SFMLParser.TypeFieldContext field : context.typeField()) {
                String name = field.dataName()
                    .getText();
                if (fields.put(name, fromSyntax(field.dataType())) != null)
                    throw fail(field, "Duplicate type field: " + name);
            }
            type = DataType.struct(fields);
        }
        // Dimensions are written outermost first: INTEGER[][2] is a dynamic array of fixed pairs.
        for (int i = context.typeDimension()
            .size() - 1; i >= 0; i--) {
            SFMLParser.TypeDimensionContext dimension = context.typeDimension(i);
            type = dimension.NUMBER() == null ? DataType.dynamicArray(type)
                : DataType.array(
                    type,
                    Integer.parseInt(
                        dimension.NUMBER()
                            .getText()));
        }
        return type;
    }

    private static IllegalArgumentException fail(ParserRuleContext context, String message) {
        return new IllegalArgumentException(
            "Line " + context.getStart()
                .getLine()
                + ", Col "
                + context.getStart()
                    .getCharPositionInLine()
                + ": "
                + message);
    }
}
