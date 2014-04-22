package com.subgraph.sgmail.ui;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

public class Application implements IApplication {

	@Override
	public Object start(IApplicationContext arg0) throws Exception {
		MainWindow.main(new String[0]);
		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
	}
}
