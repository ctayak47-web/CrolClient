
package crol.client.base.font;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.Text;
import net.minecraft.Style;
import net.minecraft.TextColor;

public class FormattedTextProcessor {
    public static List<TextSegment> processText(Text text, int defaultColor) {
        ArrayList<TextSegment> segments = new ArrayList<TextSegment>();
        text.visit((style, string) -> {
            if (!string.isEmpty()) {
                int color = FormattedTextProcessor.extractColor(style, defaultColor);
                boolean bold = style.isBold();
                boolean italic = style.isItalic();
                boolean underlined = style.isUnderlined();
                boolean strikethrough = style.isStrikethrough();
                segments.add(new TextSegment(string, color, bold, italic, underlined, strikethrough));
            }
            return Optional.empty();
        }, Style.EMPTY);
        return segments;
    }

    private static int extractColor(Style style, int defaultColor) {
        TextColor textColor = style.getColor();
        if (textColor != null) {
            return textColor.getRgb() | 0xFF000000;
        }
        return defaultColor;
    }

    public record TextSegment(String text, int color, boolean bold, boolean italic, boolean underlined, boolean strikethrough) {
    }
}

