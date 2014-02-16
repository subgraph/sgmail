package com.subgraph.sgmail.ui;


import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;

public class Resources {

    public final static String COLOR_ERROR_MESSAGE = "com.subgraph.sgmail.colors.error";
    public final static String COLOR_SENDER_SECTION = "com.subgraph.sgmail.colors.sender";
    public final static String COLOR_DATE_SECTION = "com.subgraph.sgmail.colors.date";
    public final static String COLOR_SUBJECT_SECTION = "com.subgraph.sgmail.colors.subject";
    public final static String COLOR_BODY_SECTION = "com.subgraph.sgmail.colors.body";

    public final static String FONT_SENDER = "com.subgraph.sgmail.fonts.sender";
    public final static String FONT_DATE = "com.subgraph.sgmail.fonts.date";
    public final static String FONT_SUBJECT = "com.subgraph.sgmail.fonts.subject";
    public final static String FONT_BODY_SNIPPET = "com.subgraph.sgmail.fonts.body";

    public static void initialize() {
        final String red = "255,0,0";
        final String black = "0,0,0";
        final String greyish = "119,136,153";

        addColor(COLOR_ERROR_MESSAGE, red);
        addColor(COLOR_SENDER_SECTION, black);
        addColor(COLOR_SUBJECT_SECTION, black);
        addColor(COLOR_DATE_SECTION, greyish);
        addColor(COLOR_BODY_SECTION, greyish);

        initializeFonts();
    }

    public static void addColor(String name, String rgb) {
        JFaceResources.getColorRegistry().put(name, StringConverter.asRGB(rgb));
    }

    public static void initializeFonts() {

        final FontRegistry fonts = JFaceResources.getFontRegistry();
        final Display display = Display.getDefault();
        final Font systemFont = Display.getDefault().getSystemFont();
        final FontData[] systemFontData = systemFont.getFontData();
        final FontData base = systemFontData[0];
        final String baseName = base.getName();

        fonts.put(FONT_SENDER, createFont(baseName, "bold-13", display));
        fonts.put(FONT_DATE, createFont(baseName, "regular-13", display));
        fonts.put(FONT_SUBJECT, createFont(baseName, "regular-11", display));
        fonts.put(FONT_BODY_SNIPPET, createFont(baseName, "regular-10", display));
    }

    private static FontData[] createFont(String baseName, String fontInfo, Device device) {
        final FontData fd = StringConverter.asFontData(baseName + "-" + fontInfo);
        return new FontData[] { fd };
    }
}
