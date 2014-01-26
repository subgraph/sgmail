package com.subgraph.sgmail.ui.panes.right;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.google.common.eventbus.Subscribe;
import com.subgraph.sgmail.events.ConversationSelectedEvent;
import com.subgraph.sgmail.events.DeleteMessageEvent;
import com.subgraph.sgmail.events.MessageStateChangedEvent;
import com.subgraph.sgmail.events.NextConversationEvent;
import com.subgraph.sgmail.events.NextMessageEvent;
import com.subgraph.sgmail.events.PreviousMessageEvent;
import com.subgraph.sgmail.events.ReplyMessageEvent;
import com.subgraph.sgmail.model.Conversation;
import com.subgraph.sgmail.model.LocalMimeMessage;
import com.subgraph.sgmail.model.Model;
import com.subgraph.sgmail.model.StoredMessage;
import com.subgraph.sgmail.ui.compose.ComposeWindow;

public class RightPane extends Composite {
	private final static Logger logger = Logger.getLogger(RightPane.class.getName());
	
	private final Model model;
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private Composite composite;
	private ScrolledComposite scrolled;
	
	private Conversation currentConversation;
	private DisplayConversationTask currentTask;
	
	private List<MessageViewer> messageViewers = new ArrayList<MessageViewer>();
	private int currentIndex = 0;
	
	public RightPane(Composite parent, Model model) {
		super(parent, SWT.NONE);
		this.model = model;
		setLayout(new FillLayout());
		
		scrolled = new ScrolledComposite(this, SWT.BORDER | SWT.V_SCROLL);
		
		scrolled.setExpandHorizontal(true);
		scrolled.setExpandVertical(true);
		
		model.registerEventListener(this);
		addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent event) {
				executor.shutdownNow();
			}
		});
	}
		
	private void recomputeScrolledHeight(Composite composite) {
		if(composite.isDisposed()) {
			return;
		}
		final int width = getClientArea().width;
		final Point size = composite.computeSize(width, SWT.DEFAULT, false);
		composite.layout(false);
		scrolled.setMinSize(SWT.DEFAULT, size.y);
	}

	@Subscribe
	public void onDeleteMessage(DeleteMessageEvent event) {
		synchronized(messageViewers) {
			final MessageViewer mv = getCurrentMessageViewer();
			seekToNextMessage(true, false);
			if(mv == null) {
				return;
			}
			final Message m = mv.getMessage();
			final StoredMessage sm = ((LocalMimeMessage)m).getStoredMessage();
			sm.addFlag(StoredMessage.FLAG_DELETED);
			model.commit();

			messageViewers.remove(mv);
			mv.dispose();
			recomputeScrolledHeight(composite);
			model.postEvent(new MessageStateChangedEvent(sm));
		}
	}
	
	private MessageViewer getCurrentMessageViewer() {
		if(currentIndex >= messageViewers.size()) {
			return null;
		}
		return messageViewers.get(currentIndex);
	}

	@Subscribe
	public void onNextMessageEvent(NextMessageEvent event) {
		synchronized(messageViewers) {
			seekToNextMessage(event.isNextConversation(), event.isNewOnly());
		}
	}
	
	private void seekToNextMessage(boolean allowNextConversation, boolean isNewOnly) {
		if(currentIndex + 1 >= messageViewers.size()) {
			if(allowNextConversation) {
				model.postEvent(new NextConversationEvent(isNewOnly));
			}
			return;
		}
		selectMessage(currentIndex + 1, true);
	}
	
	@Subscribe
	public void onPreviousMessageEvent(PreviousMessageEvent event) {
		synchronized(messageViewers) {
			if(currentIndex > 0) {
				selectMessage(currentIndex - 1, true);
			}
		}
	}
	
	private void selectMessage(int index, boolean display) {
		if(index == currentIndex || index < 0 || index >= messageViewers.size()) {
			return;
		}
		setHighlight(messageViewers.get(currentIndex), false);
		setHighlight(messageViewers.get(index), true);
		if(display) {
			displayViewer(messageViewers.get(index));
		} else {
			redraw();
		}
		currentIndex = index;
	}
	
	void selectMessageViewer(MessageViewer viewer) {
		synchronized(messageViewers) {
			int idx = messageViewers.indexOf(viewer);
			if(idx != -1) {
				selectMessage(idx, false);
			}
		}
	}

	private void setHighlight(MessageViewer mv, boolean value) {
		if(mv != null && !mv.isDisposed()) {
			mv.setHighlighted(value);
		}
	}
	
	private void displayViewer(MessageViewer mv) {
		if(mv == null) {
			return;
		}
		int y = mv.getBounds().y - 5;
		if(y < 0) y = 0;
		scrolled.setOrigin(0, y);
		redraw();
	}

	@Subscribe
	public void onReplyMessage(ReplyMessageEvent event) {
		if(currentIndex < messageViewers.size()) {
			MessageViewer mv = messageViewers.get(currentIndex);
			ComposeWindow compose = new ComposeWindow(getDisplay().getActiveShell(), model, mv.getMessage(), event.isReplyAll());
			compose.open();
		}
	}
	
	@Subscribe
	public void onConversationSelected(ConversationSelectedEvent event) {
		final Conversation c = event.getSelectedConversation();
		if(currentConversation == c) {
			return;
		}
		currentConversation = c;
		
		if(currentTask != null) {
			currentTask.cancel();
		}
		
		if(composite != null) {
			composite.dispose();
			composite = null;
		}
		composite = new Composite(scrolled, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 15;
		composite.setLayout(layout);
		composite.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				recomputeScrolledHeight(composite);
			}
		});
		scrolled.setContent(composite);
		
		currentTask = new DisplayConversationTask(c, composite);
		executor.execute(currentTask);
	}
	
	private class DisplayConversationTask implements Runnable {
		private final Composite composite;
		private final Conversation conversation;
		private volatile boolean finished;
		
		public DisplayConversationTask(Conversation conversation, Composite composite) {
			this.conversation = conversation;
			this.composite = composite;
		}
		
		public void cancel() {
			finished = true;
		}
	
		@Override
		public void run() {
			synchronized(messageViewers) {
				messageViewers.clear();
				currentIndex = 0;
			}
			
			int idx = 0;
			for(StoredMessage m: conversation.getMessages()) {
				if(finished) {
					return;
				}
				if(!m.isFlagSet(StoredMessage.FLAG_DELETED)) {
					addMessageViewer(getMimeMessage(m), idx);
					idx += 1;
				}
			}
			
			getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					recomputeScrolledHeight(composite);
				}
			});
		}

		private MimeMessage getMimeMessage(StoredMessage sm) {
			try {
				return sm.toMimeMessage();
			} catch (MessagingException e) {
				logger.warning("Error converting to MimeMessage: "+ e);
				return null;
			}
		}
	
		private void addMessageViewer(final MimeMessage m, final int idx) {
			if(m == null) {
				return;
			}
			getDisplay().asyncExec(new Runnable() {
				public void run() {
					if(composite.isDisposed()) {
						finished = true;
						return;
					}
					final MessageViewer mv = new MessageViewer(composite, RightPane.this, m, model);
					mv.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
					// Update size for first three and every 10th after that
					if(idx < 3 || (idx % 10) == 0) {
						mv.layout(false);
						recomputeScrolledHeight(composite);
					}
					synchronized (messageViewers) {
						messageViewers.add(mv);
					}
					if(idx == 0) {
						mv.setHighlighted(true);
					}
				}
			});
		}
	
	}
}
