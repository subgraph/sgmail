package com.subgraph.sgmail.ui.panes.left;

import com.subgraph.sgmail.accounts.Account;
import com.subgraph.sgmail.messages.StoredIMAPFolder;
import com.subgraph.sgmail.messages.StoredMessageLabel;
import com.subgraph.sgmail.ui.ImageCache;
import com.subgraph.sgmail.ui.Resources;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class LabelProvider extends OwnerDrawLabelProvider {

    private enum BadgeType { BADGE_NEW_COUNT, BADGE_SEARCH_MATCHES };

    private final LeftPane pane;

    LabelProvider(LeftPane pane) {
        this.pane = pane;
    }

	@Override
	protected void measure(Event event, Object element) {
        event.width = getWidth(event);
		event.height = event.gc.getFontMetrics().getHeight() + 4;
        if(event.width == 0 && getText(element) != null) {
            event.width = event.gc.textExtent(getText(element)).x;
        }
    }
	
	private int getWidth(Event event) {
		final TreeItem item = (TreeItem) event.item;
		final Tree tree = item.getParent();
        return tree.getClientArea().width;
	}
	

	@Override
	protected void paint(Event event, Object element) {
        Rectangle b = event.getBounds();
		String text = getText(element);
		Image image = getImage(element);
		
		int x = b.x + 10;
		if(image != null) {
			event.gc.drawImage(image, x, b.y + 4);
			x += image.getImageData().width + 5;
		}
		
		event.gc.drawText(text, x, b.y);
		Point extent = event.gc.textExtent(text);
		x += extent.x + 5;
		

        final int searchMatches = getSearchMatchCount(element);
        final int newMessageCount = getNewMessageCount(element);
        if(searchMatches > 0) {
            paintBadge(event.gc, b, getWidth(event), x, searchMatches, BadgeType.BADGE_SEARCH_MATCHES);
        } else if(newMessageCount > 0) {
            paintBadge(event.gc, b, getWidth(event), x, newMessageCount, BadgeType.BADGE_NEW_COUNT);
		}
	}

	private void paintBadge(GC gc, Rectangle bounds, int width, int minX, int n, BadgeType badgeType) {
		String number = Integer.toString(n);
		Point p = gc.textExtent(number);
		final int badgeHeight = p.y;
		final int badgeWidth = p.x + 20;
		final int x = calculateBadgeX(width, badgeWidth, minX);
		paintBadge(gc, x, bounds.y + 2, badgeWidth, badgeHeight, number, badgeType);
	}
	
	private int calculateBadgeX(int width, int badgeWidth, int minX) {
		int x = (width - badgeWidth) - 10;
		return (x < minX) ? minX : x;
	}
	
	private void paintBadge(GC gc, int x, int y, int width, int height, String number, BadgeType badgeType) {
        gc.setForeground(getBadgeForeground(badgeType));
        gc.setBackground(getBadgeBackground(badgeType));
        gc.fillOval(x, y, height, height);
		gc.fillOval(x + width - height, y, height, height);
		gc.fillRectangle(x + (height/2), y, width - height, height);
		
		Point p = gc.textExtent(number);
		int nx = x + (width / 2) - (p.x / 2);
		gc.drawText(number, nx, y);
	}

    private Color getBadgeForeground(BadgeType badgeType) {
        switch (badgeType) {
            case BADGE_NEW_COUNT:
                return JFaceResources.getColorRegistry().get(Resources.COLOR_WHITE);
            case BADGE_SEARCH_MATCHES:
                return JFaceResources.getColorRegistry().get(Resources.COLOR_SELECTED_ELEMENT_FOREGROUND);
        }
        throw new IllegalStateException("Unknown badge type");
    }

    private Color getBadgeBackground(BadgeType badgeType) {
        switch (badgeType) {
            case BADGE_NEW_COUNT:
                return JFaceResources.getColorRegistry().get(Resources.COLOR_NEW_MESSAGE_BADGE);

            case BADGE_SEARCH_MATCHES:
                return JFaceResources.getColorRegistry().get(Resources.COLOR_HIGHLIGHT_BACKGROUND);
        }
        throw new IllegalStateException("Unknown badge type");
    }
	
	protected void erase(Event event, Object element) {
		
	}
	
	private String getText(Object element) {
        if(element instanceof StoredIMAPFolder) {
			final StoredIMAPFolder folder = (StoredIMAPFolder) element;
			return folder.getName();
		} else if(element instanceof Account) {
			return ((Account)element).getLabel();
		} else if(element instanceof StoredMessageLabel) {
			String label = ((StoredMessageLabel) element).getName();
			if(label.startsWith("\\")) {
				return label.substring(1);
			} else {
				return label;
			}
		} else {
			return "???";
		}
	}
	
	private int getNewMessageCount(Object element) {
        return pane.getEventListStackFor(element).getNewMessageCounter().getValue();
	}

    private int getSearchMatchCount(Object element) {
        if(pane.isSearchActive()) {
            return pane.getEventListStackFor(element).getSearchMatchCounter().getValue();
        } else {
            return -1;
        }
    }

	private Image getImage(Object element) {
		if(element instanceof Account) {
			return ImageCache.getInstance().getImage(ImageCache.INBOX_IMAGE);
		} else if(element instanceof com.subgraph.sgmail.messages.StoredFolder) {
			return ImageCache.getInstance().getImage(ImageCache.FOLDER_IMAGE);
		} else if(element instanceof StoredMessageLabel) {
			return ImageCache.getInstance().getImage(ImageCache.TAG_IMAGE);
		} else {
			return null;
		}
	}
}
