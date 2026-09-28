package ca.teamdman.sfml.ast;

import java.util.Locale;

public enum ComparisonOperator implements ASTNode {

    GREATER,
    LESSER,
    EQUALS,
    LESSER_OR_EQUAL,
    GREATER_OR_EQUAL;

    public boolean test(long a, long b) {
        switch (this) {
            case GREATER:
                return a > b;
            case LESSER:
                return a < b;
            case EQUALS:
                return a == b;
            case LESSER_OR_EQUAL:
                return a <= b;
            case GREATER_OR_EQUAL:
                return a >= b;
            default:
                return false;
        }
    }

    public String getSymbol() {
        switch (this) {
            case GREATER:
                return ">";
            case LESSER:
                return "<";
            case EQUALS:
                return "==";
            case LESSER_OR_EQUAL:
                return "<=";
            case GREATER_OR_EQUAL:
                return ">=";
            default:
                return "==";
        }
    }

    public static ComparisonOperator from(String text) {
        if (text == null) return EQUALS;
        switch (text.toUpperCase(Locale.ROOT)) {
            case "GT":
            case ">":
                return GREATER;
            case "LT":
            case "<":
                return LESSER;
            case "EQ":
            case "=":
                return EQUALS;
            case "LE":
            case "<=":
                return LESSER_OR_EQUAL;
            case "GE":
            case ">=":
                return GREATER_OR_EQUAL;
            default:
                return EQUALS;
        }
    }
}
