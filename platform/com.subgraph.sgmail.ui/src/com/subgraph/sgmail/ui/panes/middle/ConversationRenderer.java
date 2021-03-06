package com.subgraph.sgmail.ui.panes.middle;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.subgraph.sgmail.messages.MessageUser;
import com.subgraph.sgmail.messages.StoredMessage;
import com.subgraph.sgmail.search.HighlightedString;
import com.subgraph.sgmail.search.SearchResult;
import com.subgraph.sgmail.ui.ImageCache;
import com.subgraph.sgmail.ui.Resources;
import com.subgraph.sgmail.ui.utils.TextLineRenderer;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

import java.text.SimpleDateFormat;
import java.util.*;


public class ConversationRenderer {
	
	private final static long TIME_24_HOURS = (24 * 60 * 60 * 1000);
	
	private final static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
	private final static SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/YY");

    private static class HighlightCacheEntry {
        final String string;
        final RangeSet<Integer> highlightedRanges;
        final List<Range<Integer>> ranges;
        HighlightCacheEntry(String string, RangeSet<Integer> highlightedRanges, List<Range<Integer>> ranges) {
            this.string = string;
            this.highlightedRanges = highlightedRanges;
            this.ranges = ranges;
        }
    }

//  +-- Left margin                                  Right margin -+                                                           
//  |                                                              |
//______V______________________________________________________________V_
//|            |                                          |            |  |
//|    Star    |  Sender                                  |   Date     |  |
//|____________|__________________________________________|____________|  |___ line space
//|            |                                                       |  |
//|            |  Subject Line                                         |  |
//|            |_______________________________________________________|  |___ line space
//|            |                                                       |  |
//|            |  Body line 1                                          |  |
//|            |                                                       |  |
//|            |                                                       |  |
//|            |  Body line 2                                          |  |
//|____________|_______________________________________________________|__|
//
	
	private final static int TOP_MARGIN = 8;
	private final static int BOTTOM_MARGIN = 8;
	private final static int LEFT_MARGIN = 40;
	private final static int RIGHT_MARGIN = 4;
	private final static int LINE_SPACE = 4;
	
	private enum Section {
        SENDER (Resources.FONT_SENDER, Resources.COLOR_SENDER_SECTION),
        DATE (Resources.FONT_DATE, Resources.COLOR_DATE_SECTION),
        SUBJECT (Resources.FONT_SUBJECT, Resources.COLOR_SUBJECT_SECTION),
        BODY (Resources.FONT_BODY_SNIPPET, Resources.COLOR_BODY_SECTION);


        private final String fontKey;
        private final String colorKey;
        private int yValue;

        Section(String fontKey, String colorKey) {
            this.fontKey = fontKey;
            this.colorKey = colorKey;
        }

        Font getFont() {
            return getFontByKey(fontKey);
        }

        Color getColor() {
            return getColorByKey(colorKey);
        }

        int getYValue() {
            return yValue;
        }

        void setYValue(int value) {
            this.yValue = value;
        }

        int getFontHeight(GC gc) {
            gc.setFont(getFont());
            return gc.textExtent("").y;
        }

        int getStringWidth(GC gc, String s) {
            gc.setFont(getFont());
            return gc.textExtent(s).x;
        }
    };
	
	private final Display display;
    private final int totalHeight;

    private final Object searchLock = new Object();
    private SearchResult searchResult;
    private Map<Integer, HighlightCacheEntry> bodyHighlightCache = new HashMap<>();

	/** Vertical offsets of each section, and Section.END is total height */
	private final Map<Section, Integer> yMap = new HashMap<>();

	public ConversationRenderer(Display display) {
		this.display = display;
        totalHeight = populateYMap();
	}

    public void setSearchResult(SearchResult result) {
        synchronized (searchLock) {
            searchResult = result;
            bodyHighlightCache.clear();
        }
    }

	private int populateYMap() {
		final Image image = new Image(display, 1,1);
		final GC gc = new GC(image);
        final int senderFontHeight = Section.SENDER.getFontHeight(gc);
        final int subjectFontHeight = Section.SUBJECT.getFontHeight(gc);
        final int bodyFontHeight = Section.BODY.getFontHeight(gc);
		gc.dispose();
		image.dispose();
		
		int y = TOP_MARGIN;

        Section.SENDER.setYValue(y);
        Section.DATE.setYValue(y);

        y += senderFontHeight;
		y += LINE_SPACE;

        Section.SUBJECT.setYValue(y);

		y += subjectFontHeight;
		y += LINE_SPACE;

        Section.BODY.setYValue(y);

		y += bodyFontHeight * 2;
		y += BOTTOM_MARGIN;

        return y;
	}

    private static Color getColorByKey(String key) {
        return JFaceResources.getColorRegistry().get(key);
    }

    private static Font getFontByKey(String name) {
        final Font font = JFaceResources.getFontRegistry().get(name);
        if(font == null) {
            return Display.getDefault().getSystemFont();
        } else {
            return font;
        }
    }

	int getTotalHeight() {
        return totalHeight;
	}

    void renderAll(Event event, List<StoredMessage> messages) {
     
           if(messages.size() == 0) {
               return;
           }
           final StoredMessage sm = messages.get(0);
           if(hasNewMessage(messages)) {
               renderNewMessageDot(event);
           }
           synchronized (searchLock) {
               if(searchResult != null && searchResult.resultContainsMessageId(sm.getMessageId())) {
                   renderHighlightedSubject(event, sm);
                   renderHighlightedBody(event, sm);
               } else {
                   renderSubject(event, sm.getSubject());
                   renderBody(event, sm.getBodyText());
               }
           }
           renderSender(event, sm);
           renderDate(event, sm);
           renderDividerLine(event);

       

       

    }

    private Color getEventColor(Event event, Section section) {
        if(isSelected(event)) {
            return JFaceResources.getColorRegistry().get(Resources.COLOR_SELECTED_ELEMENT_FOREGROUND);
        } else {
            return section.getColor();
        }
    }

    private void renderHighlightedSubject(Event event, StoredMessage msg) {
        final HighlightedString highlightedSubject = searchResult.getHighlightedSubject(msg.getMessageId());
        final Color foreground = getEventColor(event, Section.SUBJECT);
        final Color highlightBackground = JFaceResources.getColorRegistry().get(Resources.COLOR_HIGHLIGHT_BACKGROUND);
        final Color highlightForeground = JFaceResources.getColorRegistry().get(Resources.COLOR_HIGHLIGHT_FOREGROUND);
        TextLineRenderer.TextStyle basic = TextLineRenderer.createStyle(Section.SUBJECT.getFont(), foreground);
        TextLineRenderer.TextStyle highlighted = TextLineRenderer.createStyle(JFaceResources.getFont(Resources.FONT_SUBJECT_BOLD), highlightForeground, highlightBackground);
        final String trimmed = trimToMaxWidth(event.gc, highlightedSubject.getString(), event.width - (LEFT_MARGIN + RIGHT_MARGIN));

        final TextLineRenderer renderer = TextLineRenderer.createHighlighted(trimmed, highlightedSubject.getHighlightedRanges(), basic, highlighted);
        final int x = event.x + LEFT_MARGIN;
        final int y = event.y + Section.SUBJECT.getYValue();
        renderer.render(event.gc, x, y);
    }

    private void renderHighlightedBody(Event event, StoredMessage msg) {
        HighlightCacheEntry entry = getHighlightDetails(event, msg.getMessageId());
        /*
        final HighlightedString highlightedBody = searchResult.getHighlightedBody(msg.getUniqueMessageId());
        final int snippetStart = getSnippetStart(highlightedBody, 80);
        final BodySnippetGenerator gen = new BodySnippetGenerator(highlightedBody.getString(), event.width - (LEFT_MARGIN + RIGHT_MARGIN), event.gc);
        final List<Range<Integer>> ranges = gen.generateSnippetRanges(snippetStart);
        */
        final Color foreground = getEventColor(event, Section.BODY);
        final Color highlightBackground = JFaceResources.getColorRegistry().get(Resources.COLOR_HIGHLIGHT_BACKGROUND);
        final Color highlightForeground = JFaceResources.getColorRegistry().get(Resources.COLOR_HIGHLIGHT_FOREGROUND);
        final TextLineRenderer.TextStyle basic = TextLineRenderer.createStyle(Section.BODY.getFont(), foreground);
        final TextLineRenderer.TextStyle highlighted = TextLineRenderer.createStyle(JFaceResources.getFont(Resources.FONT_BODY_SNIPPET_BOLD), highlightForeground, highlightBackground);
        final int x = event.x + LEFT_MARGIN;
        int y = event.y + Section.BODY.getYValue();
        for(Range<Integer> r: entry.ranges) {
            // TextLineRenderer renderer = TextLineRenderer.createHighlighted(highlightedBody.getString(), r, highlightedBody.getHighlightedRanges(), basic, highlighted);
            TextLineRenderer renderer = TextLineRenderer.createHighlighted(entry.string, r, entry.highlightedRanges, basic, highlighted);
            renderer.render(event.gc, x, y);
            y += Section.BODY.getFontHeight(event.gc);
        }
    }

    // XXX need to regen cache on resize
    HighlightCacheEntry getHighlightDetails(Event event, int uid) {
        if(!bodyHighlightCache.containsKey(uid)) {
            final HighlightedString highlightedBody = searchResult.getHighlightedBody(uid);
            final int snippetStart = getSnippetStart(highlightedBody, 80);
            final BodySnippetGenerator gen = new BodySnippetGenerator(highlightedBody.getString(), event.width - (LEFT_MARGIN + RIGHT_MARGIN), event.gc);
            final List<Range<Integer>> ranges = gen.generateSnippetRanges(snippetStart);
            bodyHighlightCache.put(uid, new HighlightCacheEntry(highlightedBody.getString(), highlightedBody.getHighlightedRanges(), ranges));
        }
        return bodyHighlightCache.get(uid);
    }


    private int getSnippetStart(HighlightedString highlightedBody, int minOffset) {
        if(highlightedBody.getHighlightedRanges().isEmpty()) {
            return 0;
        }
        final int firstHighlightOffset = highlightedBody.getHighlightedRanges().span().lowerEndpoint();
        if(firstHighlightOffset < minOffset) {
            return 0;
        }
        final String str = highlightedBody.getString();

        for(int i = firstHighlightOffset - minOffset; i < firstHighlightOffset; i++) {
            if(Character.isWhitespace(str.charAt(i))) {
                while(Character.isWhitespace(str.charAt(i)) && i < firstHighlightOffset) {
                   i++;
                }
                return i;
            }
        }
        return firstHighlightOffset - minOffset;
    }

    boolean hasNewMessage(List<StoredMessage> messages) {
        for(StoredMessage msg: messages) {
            if(isNewMessage(msg)) {
                return true;
            }
        }
        return false;
    }

    private boolean isNewMessage(StoredMessage msg) {
        return ((msg.getConversationId() & StoredMessage.FLAG_SEEN) == 0);
    }


    void renderNewMessageDot(Event event) {
		final Image image = ImageCache.getInstance().getImage(ImageCache.BLUE_DOT_IMAGE);
		final int imageWidth = image.getImageData().width;
		final int xoff = (LEFT_MARGIN / 2)  - (imageWidth / 2);
        final int x = event.x + xoff;
        event.gc.drawImage(image, x, Section.SUBJECT.getYValue() + event.y);
	}

	void renderSender(Event event, StoredMessage message) {
        final int width = getDateX(event, message) - (event.x + LEFT_MARGIN);
        setFontAndColor(event, Section.SENDER);
        final int x = event.x + LEFT_MARGIN;
        final int y = event.y + Section.SENDER.getYValue();
        MessageUser messageSender = message.getSender();
        final String sender = (messageSender == null) ? ("") : (messageSender.getText(true));
		if(sender != null) {
            final String s = trimToMaxWidth(event.gc, sender, width);
            drawText(event.gc, s, x, y);
		}
	}

    private void setFontAndColor(Event event, Section section) {
        event.gc.setFont(section.getFont());
        if(isSelected(event)) {
            event.gc.setForeground(JFaceResources.getColorRegistry().get(Resources.COLOR_SELECTED_ELEMENT_FOREGROUND));
        } else {
            event.gc.setForeground(section.getColor());
        }

    }

    private boolean isSelected(Event event) {
        return (event.detail & SWT.SELECTED) == SWT.SELECTED;
    }
	
	void renderDate(Event event, StoredMessage message) {
        setFontAndColor(event, Section.DATE);
		final String dateString = getDateText(message);
		final Point sz = event.gc.textExtent(dateString);
		final int x = (event.x + event.width) - (RIGHT_MARGIN + sz.x);
		final int y = event.y + Section.DATE.getYValue();
        drawText(event.gc, dateString, x, y);
	}
	
	private int getDateX(Event event, StoredMessage message) {
        final String dateString = getDateText(message);
        final int stringWidth = Section.DATE.getStringWidth(event.gc, dateString);

		return (event.x + event.width) - (RIGHT_MARGIN + stringWidth);
	}
	
	private String getDateText(StoredMessage message) {
		final long ts = message.getMessageDate() * 1000L;
		final long now = System.currentTimeMillis();
		
		if(ts == 0) {
			return "No sent date";
		}
		final Date d = new Date(ts);

		if((now - ts) < TIME_24_HOURS) {
			return timeFormat.format(d);
		} else if ((now - ts) < (2 * TIME_24_HOURS)) {
			return "Yesterday";
		} else {
			return dateFormat.format(d);
		}
	}
	
	void renderSubject(Event event, String subject) {
		final int x = event.x + LEFT_MARGIN;
		final int y = event.y + Section.SUBJECT.getYValue();
        setFontAndColor(event, Section.SUBJECT);
		final String trimmed = trimToMaxWidth(event.gc, subject, event.width - (LEFT_MARGIN + RIGHT_MARGIN));
        drawText(event.gc, trimmed, x, y);
    }
	
	void renderBody(Event event, String body) {
        setFontAndColor(event, Section.BODY);
		final int x = event.x + LEFT_MARGIN;
		final int y1 = event.y + Section.BODY.getYValue();
        final int y2 = y1 + Section.BODY.getFontHeight(event.gc);
        final String[] lines = getLinesForBody(event, body);
        drawText(event.gc, lines[0], x, y1);
		if(!lines[1].isEmpty()) {
            drawText(event.gc, lines[1], x, y2);
		}
	}

	private String[] getLinesForBody(Event event, String body) {
		
		final String[] result = new String[] { "", "" };

        final String trimmed = trimToMaxLength(body, 1000);

		int maxWidth = event.width - (LEFT_MARGIN + RIGHT_MARGIN);
		String[] words = trimmed.split("\\s+", 200);
		if(words.length == 0) {
			return result;
		}
		List<String> wordsList = new ArrayList<String>(Arrays.asList(words));
		result[0] = getBodyLine(event.gc, wordsList, maxWidth, false);
		result[1] = getBodyLine(event.gc, wordsList, maxWidth, true);
		return result;
	}

	private String getBodyLine(GC gc, List<String> words, int maxWidth, boolean lastLine) {
		if(words.isEmpty()) {
			return "";
		}
		final String firstWord = words.get(0);
		
		if(stringWidth(gc, firstWord) > maxWidth) {
			String trimmed = trimWordToMax(gc, firstWord, maxWidth, lastLine);
			words.set(0, firstWord.substring(trimmed.length()));
			return trimmed;
		}
		StringBuilder sb = new StringBuilder();
		
		sb.append(firstWord);
		for(int i = 1; i < words.size(); i++) {
			String testString = sb.toString() + " "+ words.get(i);
			if(stringWidth(gc, testString) > maxWidth) {
				words.subList(0, i).clear();
				return sb.toString();
			}
			sb.append(" ");
			sb.append(words.get(i));
		}
		words.clear();
		return sb.toString();
	}

	private String trimWordToMax(GC gc, String word, int maxWidth, boolean lastLine) {
		int len = 1;
		String last = "";
		while(len <= word.length()) {
			String s = word.substring(0, len);
			if(gc.textExtent(s).x > maxWidth) {
				return last;
			}
			len += 1;
			last = s;
		}
		return word;
	}

	private int stringWidth(GC gc, String s) {
		return gc.textExtent(s).x;
	}
	
	private void renderDividerLine(Event event) {
		GC gc = event.gc;
		gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_GRAY));
		final int x1 = event.x;
		final int x2 = event.x + event.width;
		final int y = event.y;
		gc.drawLine(x1, y, x2, y);
	}

    private void drawText(GC gc, String text, int x, int y) {
        gc.setAntialias(SWT.ON);
        gc.setTextAntialias(SWT.ON);
        gc.drawText(text, x, y, true);
    }
    
	public static String trimToMaxWidth(GC gc, String str, int maxWidth) {
		if(maxWidth < 0) {
			return "";
		}
		if(getWidth(gc, str) < maxWidth) {
			return str;
		}
		for(int i = 1; i < str.length(); i++) {
			if(getWidth(gc, str.substring(0, i) + "...") > maxWidth) {
				return str.substring(0, i - 1) + "...";
			}
		}
		return str;
	}
	
	private static int getWidth(GC gc, String s) {
		return gc.textExtent(s).x;
	}
	
	public static String trimToMaxLength(String str, int maxLength) {
		if(str.length() <= maxLength) {
			return str;
		} else {
			return str.substring(0, maxLength - 3) + "...";
		}
	}
}
