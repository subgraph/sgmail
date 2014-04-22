package com.subgraph.sgmail.search.impl;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HighlightedStringTest {

    @Test
    public void test1() {
        /*                     0123   456    789   01 */
        final String tagged = "abcd<b>efg</b>hij<b>ZZ</b>";
        final HighlightedStringImpl hs = HighlightedStringImpl.createFromTaggedString(tagged);
        assertEquals("abcdefghijZZ", hs.getString());
        RangeSet rs = TreeRangeSet.create();
        rs.add(Range.closedOpen(4, 7));
        rs.add(Range.closedOpen(10,12));
        assertEquals(rs, hs.getHighlightedRanges());
    }
}
