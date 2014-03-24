package com.subgraph.sgmail.search.impl;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;

import java.util.ArrayList;
import java.util.List;

public class TextLineRenderer {
    public static TextStyle createStyle(Font font, Color foreground) {
        return new TextStyle(foreground, null, font);
    }

    public static TextStyle createStyle(Font font, Color foreground, Color background) {
        return new TextStyle(foreground, background, font);
    }

    public static TextLineRenderer createSimple(String inputString, TextStyle style) {
        final Range<Integer> fullRange = Range.closedOpen(0, inputString.length());
        return createSimpleRanged(inputString, fullRange, style);
    }

    public static TextLineRenderer createSimpleRanged(String inputString, Range<Integer> range, TextStyle style) {
        final TextLineRenderer renderer = new TextLineRenderer(inputString);
        renderer.addSection(range.lowerEndpoint(), range.upperEndpoint(), style);
        return renderer;
    }

    public static TextLineRenderer createHighlighted(String inputString, RangeSet<Integer> highlightedRange, TextStyle basicStyle, TextStyle highlightedStyle) {
        final Range<Integer> fullRange = Range.closedOpen(0, inputString.length());
        return createHighlighted(inputString, fullRange, highlightedRange, basicStyle, highlightedStyle);
    }

    public static TextLineRenderer createHighlighted(String inputString, Range<Integer> inputRange, RangeSet<Integer> highlightedRanges, TextStyle basicStyle, TextStyle highlightedStyle) {
        final TextLineRenderer renderer = new TextLineRenderer(inputString);
        int currentRangeStart = 0;
        TextStyle currentStyle = null;

        for(int i = inputRange.lowerEndpoint(); i < inputRange.upperEndpoint(); i++) {
            if(currentStyle == null) {
                currentStyle = (highlightedRanges.contains(i) ? (highlightedStyle) : (basicStyle));
                currentRangeStart = i;
            } else if(currentStyle == highlightedStyle) {
                if(!highlightedRanges.contains(i)) {
                   renderer.addSection(currentRangeStart, i, highlightedStyle);
                    currentRangeStart = i;
                    currentStyle = basicStyle;
                }
            } else if(currentStyle == basicStyle) {
                if(highlightedRanges.contains(i)) {
                    renderer.addSection(currentRangeStart, i, basicStyle);
                    currentRangeStart = i;
                    currentStyle = highlightedStyle;
                }
            }
        }
        if(currentStyle != null && currentRangeStart != inputRange.upperEndpoint()) {
            renderer.addSection(currentRangeStart, inputRange.upperEndpoint(), currentStyle);
        }
        return renderer;
    }

    public static class TextStyle {
        private final Color foreground;
        private final Color background;
        private final Font font;

        TextStyle(Color foreground, Color background, Font font) {
            this.foreground = foreground;
            this.background = background;
            this.font = font;
        }
    }

    class Section {
        private final Range<Integer> textRange;
        private final TextStyle style;

        Section(Range<Integer> textRange, TextStyle style) {
            this.textRange = textRange;
            this.style = style;
        }

        String getSectionString() {
            final int start = textRange.lowerEndpoint();
            final int end = textRange.upperEndpoint();
            if(start >= inputString.length()) {
                return "";
            }
            if(end >= inputString.length()) {
                return stripNewlines(inputString.substring(start));
            } else {
                return stripNewlines(inputString.substring(start, end));
            }
        }

        private String stripNewlines(String s) {
            if(s.indexOf('\n') == -1) {
                return s;
            }
            final StringBuilder sb = new StringBuilder(s.length());
            for(char c: s.toCharArray()) {
                if(c == '\n' || c == '\r') {
                    sb.append(" ");
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }
    }

    private final List<Section>  sections = new ArrayList<>();
    private final String inputString;

    TextLineRenderer(String inputString) {
        this.inputString = inputString;
    }

    private void addSection(Section section) {
        sections.add(section);
    }

    private void addSection(int start, int end, TextStyle style) {
        sections.add(new Section(Range.closedOpen(start, end), style));
    }

    public void render(GC gc, int x, int y) {
       int currentX = x;
       for(Section section: sections) {
           currentX += drawSection(section, gc, currentX, y);
       }
    }

    private int drawSection(Section s, GC gc, int x, int y) {
        gc.setFont(s.style.font);
        gc.setForeground(s.style.foreground);
        Color savedBackground = null;
        if(s.style.background != null) {
            savedBackground = gc.getBackground();
            gc.setBackground(s.style.background);
        }
        final String text = s.getSectionString();
        final int textWidth = gc.textExtent(text, 0).x;
        gc.drawText(text, x, y, 0);
        if(savedBackground != null) {
            gc.setBackground(savedBackground);
        }
        return textWidth;
    }
}
