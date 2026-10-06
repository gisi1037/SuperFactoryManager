package ca.teamdman.sfm.client.gui.syntax;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyntaxSnippet {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("<([^>]+)>");

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

    @Override
    public String toString() {
        return title;
    }
}
