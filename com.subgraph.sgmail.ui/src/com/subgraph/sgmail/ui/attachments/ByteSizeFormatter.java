package com.subgraph.sgmail.ui.attachments;

import com.google.common.collect.ImmutableList;

import java.text.DecimalFormat;
import java.util.List;

public class ByteSizeFormatter {
    private static class SizeUnit {
        private final double divisor;
        private final String symbol;
        SizeUnit(int power, String symbol) {
            this.divisor = Math.pow(1024, power);
            this.symbol = symbol;
        }
    }
    private final static List<SizeUnit> sizeUnits = ImmutableList.of(
            new SizeUnit(0, "bytes"),
            new SizeUnit(1, "kb"),
            new SizeUnit(2, "mb"),
            new SizeUnit(3, "gb")
    );

    public static String formatByteCount(long count) {
        if(count == 0) {
            return "0 bytes";
        } else if (count == 1) {
            return "1 byte";
        } else {
            final SizeUnit sizeUnit = findDividingUnit(count);
            final String formatted = formatDouble(count / sizeUnit.divisor);
            return formatted + " " + sizeUnit.symbol;
        }
    }

    static String formatDouble(double value) {
        final DecimalFormat format = new DecimalFormat("0.00");
        final String s = format.format(value);
        if(s.endsWith(".00")) {
            return s.substring(0, s.length() - 3);
        } else if(s.endsWith("0")) {
            return s.substring(0, s.length() - 1);
        }
        return s;
    }

    static SizeUnit findDividingUnit(long count) {
        SizeUnit best = sizeUnits.get(0);
        for (SizeUnit sizeUnit : sizeUnits) {
            if(sizeUnit.divisor > count) {
                return best;
            } else {
                best = sizeUnit;
            }
        }
        return best;
    }
}
