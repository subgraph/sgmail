package com.subgraph.sgmail.ui.panes.middle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

import com.subgraph.sgmail.model.Conversation;
import com.subgraph.sgmail.model.StoredMessage;
import com.subgraph.sgmail.ui.ImageCache;
import com.subgraph.sgmail.ui.MessageBodyUtils;
import com.subgraph.sgmail.ui.MessageUtils;


public class ConversationRenderer {
	private final int FONT_SIZE_LARGER = 2;
	private final int FONT_SIZE_SMALLER = -2;
	
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
	
	private enum Section { SENDER, DATE, SUBJECT, BODY, END };
	
	private final Display display;
	private final Map<Section, Font> fontMap = new HashMap<>();
	
	/** Vertical offsets of each section, and Section.END is total height */
	private final Map<Section, Integer> yMap = new HashMap<>();

	public ConversationRenderer(Display display) {
		this.display = display;
	}
	
	void setBaseFont(Font base) {
		clearFontMap();
		final FontData[] baseData = base.getFontData();
		final FontData bd = baseData[0];
		fontMap.put(Section.SENDER, createFont(bd, FONT_SIZE_LARGER, SWT.BOLD));
		fontMap.put(Section.DATE, createFont(bd, FONT_SIZE_LARGER));
		fontMap.put(Section.SUBJECT, createFont(bd, -2));
		fontMap.put(Section.BODY, createFont(bd, FONT_SIZE_SMALLER -2));
		
		populateYMap();
	}
	
	
	private Font createFont(FontData baseData, int size) {
		return createFont(baseData, size, 0);
	}

	private Font createFont(FontData baseData, int size, int flags) {
		final FontData fd = new FontData(
				baseData.getName(), 
				baseData.getHeight() + size, 
				baseData.getStyle() | flags);
		
		return new Font(display, fd);
	}
	
	private void clearFontMap() {
		for(Font font: fontMap.values()) {
			font.dispose();
		}
		fontMap.clear();
	}
	
	private void populateYMap() {
		final Image image = new Image(display, 1,1);
		final GC gc = new GC(image);
		final int senderFontHeight = getFontHeight(Section.SENDER, gc);
		final int subjectFontHeight = getFontHeight(Section.SUBJECT, gc);
		final int bodyFontHeight = getFontHeight(Section.BODY, gc);
		gc.dispose();
		image.dispose();
		
		int y = TOP_MARGIN;
		
		yMap.put(Section.SENDER, y);
		
		y += senderFontHeight;
		y += LINE_SPACE;
		
		yMap.put(Section.SUBJECT, y);
		
		y += subjectFontHeight;
		y += LINE_SPACE;
		
		yMap.put(Section.BODY, y);
		
		y += bodyFontHeight * 2;
		y += BOTTOM_MARGIN;
		
		yMap.put(Section.END, y);
		
	}
	
	private int getFontHeight(Section section, GC gc) {
		gc.setFont(fontMap.get(section));
		return gc.textExtent("").y;
	}
	
	Font getSenderFont() {
		return fontMap.get(Section.SENDER);
	}
	
	Font getDateFont() {
		return fontMap.get(Section.DATE);
	}
	
	Font getSubjectFont() {
		return fontMap.get(Section.SUBJECT);
	}
	
	Font getBodyFont() {
		return fontMap.get(Section.BODY);
	}
	
	int getSenderY() {
		return yMap.get(Section.SENDER);
	}
	
	int getSubjectY() {
		return yMap.get(Section.SUBJECT);
	}
	
	int getBodyY() {
		return yMap.get(Section.BODY);
	}
	
	int getTotalHeight() {
		return yMap.get(Section.END);
	}
	
	void renderAll(Event event, RenderContext ctx, Conversation conversation) {
		try {
			final Message m = getTopMessage(conversation);
			if(m == null) {
				return;
			}
			if(conversation.getNewMessageCount() > 0) {
				renderNewMessageDot(ctx);
			}
			renderSender(ctx, m);
			renderDate(ctx, m);
			renderSubject(ctx, m);
			renderBody(ctx, m);
			renderDividerLine(event, ctx.getBounds());
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private Message getTopMessage(Conversation conversation) throws MessagingException {
		final StoredMessage msg = conversation.getLeadMessage();
		if(msg == null) {
			return null;
		} else {
			return msg.toMimeMessage();
		}
	}
	
	void renderAll(Event event, RenderContext ctx, Message message) {
		try {
			renderSender(ctx, message);
			renderDate(ctx, message);
			renderSubject(ctx, message);
			renderBody(ctx, message);
			renderDividerLine(event, ctx.getBounds());
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
	
	void renderNewMessageDot(RenderContext ctx) {
		final Image image = ImageCache.getInstance().getImage(ImageCache.BLUE_DOT_IMAGE);
		final Rectangle b = ctx.getBounds();
		final int imageWidth = image.getImageData().width;
		final int xoff = (LEFT_MARGIN / 2)  - (imageWidth / 2); 
		final int x = b.x + xoff;
		ctx.getGC().drawImage(image, x, getSubjectY() + b.y);
	}

	void renderSender(RenderContext ctx, Message message) throws MessagingException {
		final Rectangle b = ctx.getBounds();
		final int width = getDateX(ctx, message) - (b.x + LEFT_MARGIN);
		ctx.startSenderSection();
		final GC gc = ctx.getGC();
		final String sender = MessageUtils.getSender(message);
		if(sender != null) {
			final String s = MessageUtils.trimToMaxWidth(gc, sender, width);
			gc.drawText(s, b.x + LEFT_MARGIN, b.y + getSenderY());
		}
		ctx.restore();
	}
	
	void renderDate(RenderContext ctx, Message message) throws MessagingException {
		ctx.startDateSection();
		final Rectangle b = ctx.getBounds();
		final GC gc = ctx.getGC();
		final String dateString = MessageUtils.getSentDate(message);
		final Point sz = gc.textExtent(dateString);
		final int x = (b.x + b.width) - (RIGHT_MARGIN + sz.x);
		final int y = b.y + getSenderY();
		gc.drawText(dateString, x, y, true);
		ctx.restore();
	}
	
	private int getDateX(RenderContext ctx, Message message) {
		ctx.startDateSection();
		final String dateString = MessageUtils.getSentDate(message);
		final Point sz = ctx.getGC().textExtent(dateString);
		final Rectangle b = ctx.getBounds();
		ctx.restore();
		return (b.x + b.width) - (RIGHT_MARGIN + sz.x);
	}
	
	void renderSubject(RenderContext ctx, Message message) throws MessagingException {
		final GC gc = ctx.getGC();
		final int x = ctx.getBounds().x + LEFT_MARGIN;
		final int y = ctx.getBounds().y + getSubjectY();
		ctx.startSubjectSection();
		final String subject = MessageUtils.getSubject(message);
		final String trimmed = MessageUtils.trimToMaxWidth(gc, subject, ctx.getBounds().width - (LEFT_MARGIN + RIGHT_MARGIN));
		
		gc.drawText(trimmed, x, y, true);
		ctx.restore();
	}
	
	void renderBody(RenderContext ctx, Message message) throws MessagingException {
		ctx.startBodySection();
		final GC gc = ctx.getGC();
		final Rectangle b = ctx.getBounds();
		final int x = b.x + LEFT_MARGIN;
		final int y1 = b.y + getBodyY();
		final int y2 = y1 + getFontHeight(Section.BODY, gc);
		final String[] lines = getLinesForBody(message, gc, b);
		gc.drawText(lines[0], x, y1, true);
		if(!lines[1].isEmpty()) {
			gc.drawText(lines[1], x, y2, true);
		}
		ctx.restore();
	}

	private String[] getLinesForBody(Message message, GC gc, Rectangle bounds) throws MessagingException {
		
		final String[] result = new String[] { "", "" };

		final String body = MessageUtils.trimToMaxLength(
				MessageBodyUtils.getTextBody(message), 1000);
		
		int maxWidth = bounds.width - (LEFT_MARGIN + RIGHT_MARGIN);
		String[] words = body.split("\\s+", 200);
		if(words.length == 0) {
			return result;
		}
		List<String> wordsList = new ArrayList<String>(Arrays.asList(words));
		result[0] = getBodyLine(gc, wordsList, maxWidth, false);
		result[1] = getBodyLine(gc, wordsList, maxWidth, true);
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
	
	private void renderDividerLine(Event event, Rectangle bounds) {
		GC gc = event.gc;
		gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_GRAY));
		final int x1 = event.x;
		final int x2 = event.x + bounds.width;
		final int y = event.y;
		gc.drawLine(x1, y, x2, y);
	}
}
