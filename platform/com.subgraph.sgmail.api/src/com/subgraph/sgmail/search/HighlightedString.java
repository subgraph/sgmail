package com.subgraph.sgmail.search;

import com.google.common.collect.RangeSet;

public interface HighlightedString {
    String getString();

    RangeSet<Integer> getHighlightedRanges();
}
