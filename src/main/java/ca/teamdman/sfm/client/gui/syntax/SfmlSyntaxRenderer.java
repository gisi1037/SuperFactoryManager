package ca.teamdman.sfm.client.gui.syntax;

import net.minecraft.client.gui.FontRenderer;

/** Renders a physical-line slice, retaining token colors when wrapping or scrolling starts inside a token. */
public final class SfmlSyntaxRenderer {

    private SfmlSyntaxRenderer() {}

    public static void draw(FontRenderer font, SfmlHighlighter.Highlighted highlighted, int line, int start, int end,
        int x, int y) {
        if (line < 0 || line >= highlighted.lines.size()) return;
        SfmlHighlighter.Line source = highlighted.lines.get(line);
        for (SfmlHighlighter.Span span : source.spans) {
            int left = Math.max(start, span.start), right = Math.min(end, span.end);
            if (left >= right) continue;
            String text = source.text.substring(left, right);
            font.drawString(text, x, y, span.style.color);
            x += font.getStringWidth(text);
        }
    }
}
