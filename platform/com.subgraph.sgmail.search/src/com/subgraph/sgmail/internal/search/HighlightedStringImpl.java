package com.subgraph.sgmail.internal.search;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import com.subgraph.sgmail.search.HighlightedString;

class HighlightedStringImpl implements HighlightedString {
    final static HighlightedStringImpl EMPTY = new HighlightedStringImpl("", ImmutableRangeSet.of());

    private final static String TAG_START = "<b>";
    private final static String TAG_END = "</b>";

    static HighlightedStringImpl createFromTaggedString(String tagged) {
        if(tagged == null) {
            return EMPTY;
        }

        final StringBuilder sb = new StringBuilder();
        final RangeSet<Integer> highlightedRanges = TreeRangeSet.create();

        boolean insideHighlight = false;
        int startIndex = -1;
        for (int i = 0; i < tagged.length();) {
            if(insideHighlight && tagged.startsWith(TAG_END, i)) {
                highlightedRanges.add(Range.closedOpen(startIndex, sb.length()));
                insideHighlight = false;
                i += TAG_END.length();
            } else if(!insideHighlight && tagged.startsWith(TAG_START, i)) {
                insideHighlight = true;
                startIndex = sb.length();
                i += TAG_START.length();
            } else {
                sb.append(tagged.charAt(i));
                i += 1;
            }
        }
        return new HighlightedStringImpl(sb.toString(), highlightedRanges);
    }

    private final String string;
    private final RangeSet<Integer> highlightedRanges;

    private HighlightedStringImpl(String string, RangeSet<Integer> highlightedRanges) {
        this.string = string;
        this.highlightedRanges = highlightedRanges;
    }

    public RangeSet<Integer> getHighlightedRanges() {
        return highlightedRanges;
    }

    public String getString() {
        return string;
    }
}
