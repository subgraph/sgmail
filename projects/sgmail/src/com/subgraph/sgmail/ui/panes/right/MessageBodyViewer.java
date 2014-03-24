package com.subgraph.sgmail.ui.panes.right;

import com.subgraph.sgmail.ui.MessageBodyUtils;
import com.subgraph.sgmail.ui.Resources;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import javax.mail.Message;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MessageBodyViewer extends Composite {

    private final StyledText styledText;
    private final String lowercaseBody;

    public MessageBodyViewer(Composite parent, Message message) {
		super(parent, SWT.NONE);

        setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
        GridLayout layout = new GridLayout();
        layout.marginLeft = 26;
        layout.marginRight = 10;
        layout.marginTop = 10;
        layout.marginBottom = 10;
        setLayout(layout);

        styledText = new StyledText(this, SWT.WRAP | SWT.READ_ONLY);

		styledText.setFont(JFaceResources.getFont(Resources.FONT_MESSAGE_BODY));
        styledText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		final String body = MessageBodyUtils.getTextBody(message);
		if(body != null) {
			styledText.setText(body);
            lowercaseBody = body.toLowerCase();
		} else {
            lowercaseBody = null;
        }
	}

    public void highlightTerms(List<String> terms) {
        setStyleRangesAsync(createHighlightRanges(terms));
    }

    private void setStyleRangesAsync(List<StyleRange> ranges) {
        final StyleRange[] rangeArray = ranges.toArray(new StyleRange[ranges.size()]);
        getDisplay().asyncExec(() -> {
            if(!styledText.isDisposed()) {
                styledText.setStyleRanges(rangeArray);
            }
        });
    }

    private List<StyleRange> createHighlightRanges(List<String> terms) {
        if(lowercaseBody == null) {
            return Collections.emptyList();
        }
        final List<StyleRange> styleRanges = new ArrayList<>();
        int offset = 0;
        while(offset < lowercaseBody.length()) {
            StyleRange style = highlightNextTerm(terms, offset);
            if(style == null) {
                return styleRanges;
            }
            styleRanges.add(style);
            offset = style.start + style.length;
        }
        return styleRanges;
    }

    private StyleRange highlightNextTerm(List<String> terms, int offset) {
        String bestTerm = null;
        int bestIndex = Integer.MAX_VALUE;

        for(String t: terms) {
            int idx = lowercaseBody.indexOf(t, offset);
            if(idx != -1 && idx < bestIndex) {
                bestIndex = idx;
                bestTerm = t;
            }
        }
        if(bestTerm != null) {
            return createHighlightRange(bestIndex, bestTerm.length());
        } else {
            return null;
        }
    }

    private StyleRange createHighlightRange(int start, int length) {
        return new StyleRange(start, length,
                getColor(Resources.COLOR_HIGHLIGHT_FOREGROUND),
                getColor(Resources.COLOR_HIGHLIGHT_BACKGROUND));
    }

    private Color getColor(String key) {
        return JFaceResources.getColorRegistry().get(key);
    }
}
