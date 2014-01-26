package com.subgraph.sgmail.ui.panes.middle;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.subgraph.sgmail.model.Conversation;
import com.subgraph.sgmail.model.ConversationSource;

public class ContentProvider implements IStructuredContentProvider {

	private ConversationSource currentSource;
	
	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if(newInput instanceof ConversationSource) {
			this.currentSource = (ConversationSource) newInput;
		} else {
			currentSource = null;
		}
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if(currentSource == null || currentSource.getConversations() == null) {
			return new Object[0];
		}
		List<Object> elements = new ArrayList<>();
		for(Conversation c: currentSource.getConversations()) {
			if(c.hasUndeletedMessages()) {
				elements.add(c);
			}
		}
		
		return elements.toArray();
	}
}
