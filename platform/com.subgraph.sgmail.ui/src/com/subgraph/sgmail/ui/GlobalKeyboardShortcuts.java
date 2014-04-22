package com.subgraph.sgmail.ui;

import com.google.common.collect.ImmutableMap;
import com.subgraph.sgmail.IEventBus;
import com.subgraph.sgmail.JavamailUtils;
import com.subgraph.sgmail.accounts.AccountList;
import com.subgraph.sgmail.database.Model;
import com.subgraph.sgmail.events.DeleteMessageEvent;
import com.subgraph.sgmail.events.NextMessageEvent;
import com.subgraph.sgmail.events.PreviousMessageEvent;
import com.subgraph.sgmail.events.ReplyMessageEvent;
import com.subgraph.sgmail.identity.IdentityManager;
import com.subgraph.sgmail.openpgp.MessageProcessor;
import com.subgraph.sgmail.ui.compose.ComposeWindow;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

import java.util.Map;

public class GlobalKeyboardShortcuts {


    public final static String DISABLE_KEYS_WHEN_FOCUSED = "com.subgraph.sgmail.disableShortcuts";

	private final static Map<Character, Object> eventMap = createEventMap();
	
	private static Map<Character, Object> createEventMap() {
		return ImmutableMap.<Character, Object>builder()
				.put('n', new NextMessageEvent(true, true))
				.put('j', new NextMessageEvent(false, false))
				.put('k', new PreviousMessageEvent())
				.put('r', new ReplyMessageEvent(false))
				.put('a', new ReplyMessageEvent(true))
				.put('d', new DeleteMessageEvent())
				.build();
	}

	private final ApplicationWindow mainWindow;
	private final Model model;
	private final JavamailUtils javamailUtils;
	private final IEventBus eventBus;
	private final MessageProcessor messageProcessor;
	private final IdentityManager identityManager;
	

	public GlobalKeyboardShortcuts(ApplicationWindow mainWindow, Model model, JavamailUtils javamailUtils, IEventBus eventBus, MessageProcessor messageProcessor, IdentityManager identityManager) {
		this.mainWindow = mainWindow;
		this.model = model;
		this.javamailUtils = javamailUtils;
		this.eventBus = eventBus;
		this.messageProcessor = messageProcessor;
		this.identityManager = identityManager;
	}
	
	public static GlobalKeyboardShortcuts install(ApplicationWindow mainWindow, Model model, JavamailUtils javamailUtils, IEventBus eventBus, MessageProcessor messageProcessor, IdentityManager identityManager) {
		final GlobalKeyboardShortcuts gks = new GlobalKeyboardShortcuts(mainWindow, model, javamailUtils, eventBus, messageProcessor, identityManager);
		gks.install();
		return gks;
	}
	
	public void install() {
		final Shell mainWindowShell = mainWindow.getShell();
		final Display display = mainWindowShell.getDisplay();
		display.addFilter(SWT.KeyDown, new Listener() {
			@Override
			public void handleEvent(Event event) {
                if(!shouldProcessKeyEvent(event)) {
                    return;
                }
                if(handleKey(event.character)) {
                    event.doit = false;
                }
			}
		});
	}

    private boolean shouldProcessKeyEvent(Event event) {
        if(event.display.getActiveShell() != mainWindow.getShell()) {
            return false;
        }
        return !doesControlDisableShortcuts(event.display.getFocusControl());
    }

    private boolean doesControlDisableShortcuts(Control c) {
        if(c == null) {
            return false;
        }
        final Object ob = c.getData(DISABLE_KEYS_WHEN_FOCUSED);
        return Boolean.TRUE.equals(ob);
    }
	
	private boolean handleKey(char c) {
		if(eventMap.containsKey(c)) {
			eventBus.post(eventMap.get(c));
			return true;
		}
		if(c == 'c') {
			final AccountList accountList = model.getAccountList();
            if(!accountList.getAccounts().isEmpty()) {
                ComposeWindow compose = new ComposeWindow(Display.getDefault().getActiveShell(), javamailUtils, eventBus, messageProcessor, identityManager, model);
                compose.open();
                return true;
            }
		}
		return false;
	}
}
