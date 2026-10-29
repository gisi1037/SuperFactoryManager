package ca.teamdman.sfm.client.gui.syntax;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyntaxSnippet {

    // A placeholder is a short named token, never an expression or an operator.
    // In particular, do not consume `<`, `<=` or `<<` through a later `>`.
    public static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("(?<!<)<[A-Za-z0-9_][A-Za-z0-9_ .:/-]*>");

    private final String id;
    private final String title;
    private final SnippetCategory category;
    private final String template;
    private final String description;
    private final List<String> placeholders;

    public SyntaxSnippet(String id, String title, SnippetCategory category, String template, String description) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.template = template;
        this.description = description;

        List<String> found = new ArrayList<>();
        Matcher m = PLACEHOLDER_PATTERN.matcher(template);
        while (m.find()) {
            found.add(m.group());
        }
        this.placeholders = Collections.unmodifiableList(found);
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public SnippetCategory getCategory() {
        return category;
    }

    public String getTemplate() {
        return template;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getPlaceholders() {
        return placeholders;
    }

    /** Includes the end boundary so a placeholder selected by Tab is replaced as one token. */
    public static int[] placeholderAt(String source, int caret) {
        if (caret < 0 || caret > source.length()) return null;
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(source);
        while (matcher.find()) {
            if (matcher.start() > caret) return null;
            if (caret <= matcher.end()) return new int[] { matcher.start(), matcher.end() };
        }
        return null;
    }

    @Override
    public String toString() {
        return title;
    }
}
