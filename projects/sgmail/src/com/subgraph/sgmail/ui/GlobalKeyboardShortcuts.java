package com.subgraph.sgmail.ui;

import java.util.Map;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.google.common.collect.ImmutableMap;
import com.subgraph.sgmail.events.DeleteMessageEvent;
import com.subgraph.sgmail.events.NextMessageEvent;
import com.subgraph.sgmail.events.PreviousMessageEvent;
import com.subgraph.sgmail.events.ReplyMessageEvent;
import com.subgraph.sgmail.model.Model;
import com.subgraph.sgmail.ui.compose.ComposeWindow;

public class GlobalKeyboardShortcuts {

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
	

	public GlobalKeyboardShortcuts(ApplicationWindow mainWindow, Model model) {
		this.mainWindow = mainWindow;
		this.model = model;
	}
	
	public static GlobalKeyboardShortcuts install(ApplicationWindow mainWindow, Model model) {
		final GlobalKeyboardShortcuts gks = new GlobalKeyboardShortcuts(mainWindow, model);
		gks.install();
		return gks;
	}
	
	public void install() {
		final Shell mainWindowShell = mainWindow.getShell();
		final Display display = mainWindowShell.getDisplay();
		display.addFilter(SWT.KeyDown, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if(event.display.getActiveShell() == mainWindowShell) {
					if(handleKey(event.character)) {
						event.doit = false;
					}
				}
			}
		});
	}
	
	private boolean handleKey(char c) {
		if(eventMap.containsKey(c)) {
			model.postEvent(eventMap.get(c));
			return true;
		}
		if(c == 'c') {
            if(!model.getAccounts().isEmpty()) {
                ComposeWindow compose = new ComposeWindow(Display.getDefault().getActiveShell(), model);
                compose.open();
                return true;
            }
		}
		return false;
	}
}
