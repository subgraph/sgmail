package com.subgraph.sgmail.ui.panes.middle;

import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;

import com.subgraph.sgmail.model.Conversation;

public class ConversationLabelProvider extends OwnerDrawLabelProvider {

	private final TableViewer tableViewer;
	private final ConversationRenderer renderer;

	public ConversationLabelProvider(TableViewer tableViewer) {
		this.tableViewer = tableViewer;
		
		final Table t = tableViewer.getTable();
		this.renderer = new ConversationRenderer(t.getDisplay());
	}

	@Override
	protected void measure(Event event, Object element) {
		event.width = tableViewer.getTable().getColumn(event.index).getWidth();
		event.height = renderer.getTotalHeight();
	}

	@Override
	protected void paint(Event event, Object element) {
		final RenderContext ctx = new RenderContext(event, isSelected(element), renderer);
		renderer.renderAll(event, ctx, (Conversation)element);
	}

	private boolean isSelected(Object element) {
		StructuredSelection ss = (StructuredSelection) tableViewer.getSelection();
		for(Object s: ss.toArray()) {
			if(s == element) {
				return true;
			}
		}
		return false;
	}
}
