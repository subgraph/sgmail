package com.subgraph.sgmail.ui.panes.middle;

import com.google.common.collect.Range;
import org.eclipse.swt.graphics.GC;

import java.util.ArrayList;
import java.util.List;

public class BodySnippetGenerator {
    private final static int DEFAULT_LINE_COUNT = 2;

    private final int lineCount;
    private final String inputString;
    private final int maxWidth;
    private final GC gc;

    private int currentOffset;

    public BodySnippetGenerator(String inputString, int maxWidth, GC gc) {
        this(inputString, maxWidth, gc, DEFAULT_LINE_COUNT);
    }

    public BodySnippetGenerator(String inputString, int maxWidth, GC gc, int lineCount) {
        this.inputString = inputString;
        this.maxWidth = maxWidth;
        this.gc = gc;
        this.lineCount = lineCount;
    }

    public List<Range<Integer>> generateSnippetRanges() {
       return generateSnippetRanges(0);
    }
    public List<Range<Integer>> generateSnippetRanges(int offset) {
        final List<Range<Integer>> ranges = new ArrayList<>();
        currentOffset = offset;
        for(int i = 0; i < lineCount; i++) {
            ranges.add(generateNextRange());
        }
        return ranges;
    }

    private Range<Integer> generateNextRange() {
        skipWhitespace();
        if(currentOffset == inputString.length()) {
            return Range.closedOpen(0,0);
        }
        final int startIndex = currentOffset;
        final StringBuilder sb = new StringBuilder();

        sb.append(inputString.charAt(currentOffset));
        currentOffset += 1;

        int lastWhitespace= -1;
        while(currentOffset < inputString.length()) {
            char c = inputString.charAt(currentOffset);
            if(Character.isWhitespace(c)) {
                lastWhitespace = currentOffset;
            }
            sb.append(inputString.charAt(currentOffset));
            if(stringExceedsWidth(sb.toString())) {
                if(lastWhitespace >= 0) {
                    currentOffset = lastWhitespace + 1;
                    return Range.closedOpen(startIndex, lastWhitespace);
                } else {
                    return Range.closedOpen(startIndex, currentOffset);
                }
            }
            currentOffset += 1;
        }
        return Range.closedOpen(startIndex, currentOffset);
    }

    private void skipWhitespace() {
        while(currentOffset < inputString.length() && Character.isWhitespace(inputString.charAt(currentOffset))) {
           currentOffset += 1;
        }
    }

    private boolean stringExceedsWidth(String s) {
        return gc.textExtent(s, 0).x > maxWidth;
    }
}
