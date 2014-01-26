package com.subgraph.sgmail.ui.panes.middle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TableItem;

public class RenderContext {
	private final GC gc;
	private final Rectangle bounds;
	private final boolean isSelected;
	
	private final Font savedFont;
	private final Color savedForeground;
	private final Color savedBackground;
	
	private final ConversationRenderer renderer;
	
	
	RenderContext(Event event, boolean isSelected, ConversationRenderer renderer) {
		this.gc = event.gc;
		
		TableItem item = (TableItem) event.item;
		this.bounds = item.getBounds(event.index);
		this.isSelected = isSelected;
		this.savedFont = gc.getFont();
		this.savedBackground = gc.getBackground();
		this.savedForeground = gc.getForeground();
		
		this.renderer = renderer;
	}
	
	GC getGC() {
		return gc;
	}
	
	Rectangle getBounds() {
		return bounds;
	}

	void startSenderSection() {
		gc.setFont(renderer.getSenderFont());
		if(isSelected) {
			setSelectedColors();
		} else {
			gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_BLACK));
			
		}
	}
	
	void startDateSection() {
		gc.setFont(renderer.getDateFont());
		if(isSelected) {
			setSelectedColors();
		} else {
			gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_GRAY));
		}
	}
	
	void startSubjectSection() {
		gc.setFont(renderer.getSubjectFont());
		if(isSelected) {
			setSelectedColors();
		} else {
			gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_BLACK));
		}
	}
	
	void startBodySection() {
		gc.setFont(renderer.getBodyFont());
		if(isSelected) {
			setSelectedColors();
		} else {
			gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_GRAY));
		}
	}
	
	void setSelectedColors() {
		gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT));
		gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_LIST_SELECTION));
	}

	void restore() {
		gc.setFont(savedFont);
		gc.setForeground(savedForeground);
		gc.setBackground(savedBackground);
	}
}
