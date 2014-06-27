package com.subgraph.sgmail.ui.panes.right;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

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
import com.google.common.util.concurrent.ListeningExecutorService;
import com.subgraph.sgmail.IEventBus;
import com.subgraph.sgmail.JavamailUtils;
import com.subgraph.sgmail.database.Model;
import com.subgraph.sgmail.events.ConversationSelectedEvent;
import com.subgraph.sgmail.events.DeleteMessageEvent;
import com.subgraph.sgmail.events.MessageStateChangedEvent;
import com.subgraph.sgmail.events.NextConversationEvent;
import com.subgraph.sgmail.events.NextMessageEvent;
import com.subgraph.sgmail.events.PreviousMessageEvent;
import com.subgraph.sgmail.events.ReplyMessageEvent;
import com.subgraph.sgmail.events.SearchQueryChangedEvent;
import com.subgraph.sgmail.messages.StoredMessage;
import com.subgraph.sgmail.nyms.NymsAgent;
import com.subgraph.sgmail.ui.compose.ComposeWindow;

public class RightPane extends Composite {
	private final static Logger logger = Logger.getLogger(RightPane.class.getName());
	
	private final NymsAgent nymsAgent;
	private final Model model;
	private final ListeningExecutorService globalExecutor;
	private final IEventBus eventBus;
	private final JavamailUtils javamailUtils;
	
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private Composite composite;
	private ScrolledComposite scrolled;
	
	private List<StoredMessage> currentConversation;
	private DisplayConversationTask currentTask;
	
	private List<MessageViewer> messageViewers = new ArrayList<MessageViewer>();
	private int currentIndex = 0;
    private List<String> currentHighlightTerms;
	
	public RightPane(Composite parent, NymsAgent nymsAgent, Model model, ListeningExecutorService executor, IEventBus eventBus, JavamailUtils javamailUtils) {
		super(parent, SWT.NONE);
		this.nymsAgent = nymsAgent;
		this.model = model;
		this.globalExecutor = executor;
		this.eventBus = eventBus;
		this.javamailUtils = javamailUtils;
		
		setLayout(new FillLayout());

		scrolled = new ScrolledComposite(this, SWT.V_SCROLL);

		scrolled.setExpandHorizontal(true);
		scrolled.setExpandVertical(true);
		
		eventBus.register(this);

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
			final StoredMessage m = mv.getMessage();
			m.addFlag(StoredMessage.FLAG_DELETED);
			model.getDatabase().commit();

			messageViewers.remove(mv);
			mv.dispose();
			recomputeScrolledHeight(composite);
			eventBus.post(new MessageStateChangedEvent(m));
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
				eventBus.post(new NextConversationEvent(isNewOnly));
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

    @Subscribe
    public void onSearchQueryChanged(SearchQueryChangedEvent event) {
       synchronized (messageViewers) {
           currentHighlightTerms = extractSearchTerms(event);
           for (MessageViewer mv : messageViewers) {
               mv.highlightTerms(currentHighlightTerms);
           }
       }
    }

    private List<String> extractSearchTerms(SearchQueryChangedEvent event) {
        if(event.getSearchQuery() == null || event.getSearchQuery().isEmpty()) {
            return Collections.emptyList();
        }
        final String[] parts = event.getSearchQuery().split("[\\s\\W]+");
        final List<String> termList = new ArrayList<>();
        for (String term : parts) {
            if(!term.isEmpty()) {
                termList.add(term);
            }
        }
        return termList;
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
			ComposeWindow compose = new ComposeWindow(getDisplay().getActiveShell(), javamailUtils, eventBus, nymsAgent, model, mv.getMessage(), event.isReplyAll());
			compose.open();
		}
	}
	
	@Subscribe
	public void onConversationSelected(ConversationSelectedEvent event) {
        final List<StoredMessage> c = event.getSelectedConversation();
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
		private final List<StoredMessage> conversation;
		private volatile boolean finished;
		
		public DisplayConversationTask(List<StoredMessage> conversation, Composite composite) {
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
			for(StoredMessage m: conversation) {
				if(finished) {
					return;
				}
				if(!m.isFlagSet(StoredMessage.FLAG_DELETED)) {
					if(m.isFlagSet(StoredMessage.FLAG_ENCRYPTED)) {
//						maybeDecryptMessage(m);
					}
					addMessageViewer(m, idx);
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
		/*

		private PrivateIdentity findDecryptIdentity(List<Long> keyIds) {

            for(PrivateIdentity p: identityManager.getLocalPrivateIdentities()) {
                if(testPrivateIdentity(p, keyIds)) {
                    return p;
                }
            }

            final AccountList accountList = model.getAccountList();
            
            for(Account account: accountList.getAccounts()) {
                if(account.getIdentity() != null) {
                    PrivateIdentity privateIdentity = account.getIdentity();
                    if(testPrivateIdentity(privateIdentity, keyIds)) {
                        return privateIdentity;
                    }
                }
            }
			return null;
		}

        private boolean testPrivateIdentity(PrivateIdentity privateIdentity, List<Long> keyIds) {
            if(privateIdentity == null) {
                return false;
            }
            for(long id: keyIds) {
                if(privateIdentity.containsKeyId(id)) {
                    if(privateIdentity.getPassphrase() != null) {
                        return true;
                    } else if(showPassphraseDialog(privateIdentity)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean showPassphraseDialog(final PrivateIdentity identity) {
            final int[] result = new int[1];
            getDisplay().syncExec(new Runnable() {
                @Override
                public void run() {
                    PassphraseDialog dialog = new PassphraseDialog(getShell(), identity);
                    result[0] = dialog.open();
                }
            });

            return result[0] == Window.OK;
        }
	
		private void maybeDecryptMessage(StoredMessage message) {
			if(!messageProcessor.isEncrypted(message)) {
				return;
			}
			try {
				final PrivateIdentity decryptIdentity = findDecryptIdentity(messageProcessor.getDecryptionKeyIds(message));
				if(decryptIdentity == null) {
					return;
				} else {
					messageProcessor.decryptMessage(message, decryptIdentity);
				}
			} catch (IOException | MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (PGPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OpenPGPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		*/

		private void addMessageViewer(final StoredMessage message, final int idx) {
			getDisplay().asyncExec(new Runnable() {
				public void run() {
					if(composite.isDisposed()) {
						finished = true;
						return;
					}
					final MessageViewer mv = new MessageViewer(composite, RightPane.this, message, model, globalExecutor, eventBus, nymsAgent, javamailUtils);
					mv.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
					// Update size for first three and every 10th after that
					if(idx < 3 || (idx % 10) == 0) {
						mv.layout(false);
						recomputeScrolledHeight(composite);
					}
					synchronized (messageViewers) {
						messageViewers.add(mv);
                        if(currentHighlightTerms != null) {
                            mv.highlightTerms(currentHighlightTerms);
                        }
					}
					if(idx == 0) {
						mv.setHighlighted(true);
					}
				}
			});
		}
	
	}
}
