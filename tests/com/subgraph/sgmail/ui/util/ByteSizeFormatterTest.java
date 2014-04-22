package com.subgraph.sgmail.ui.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.subgraph.sgmail.ui.attachments.ByteSizeFormatter;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class ByteSizeFormatterTest {
    @Test
    public void testFormatByteCount() throws Exception {
        final List<String> output =
                ImmutableList.of(0L, 1L, 2L, 3L, 1000L, 1024L + 512L, 2000L, 5000000L, 1234567890L)
                        .stream()
                        .map(ByteSizeFormatter::formatByteCount)
                        .collect(Collectors.toList());
        assertEquals(
                Lists.newArrayList("0 bytes", "1 byte", "2 bytes", "3 bytes", "1000 bytes", "1.5 kb", "1.95 kb", "4.77 mb", "1.15 gb"),
                output);
    }
}
