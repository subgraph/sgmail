package com.subgraph.sgmail.ui;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.security.Security;
import java.util.Properties;

import com.subgraph.sgmail.identity.server.Server;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.google.common.primitives.Ints;
import com.subgraph.sgmail.model.Model;
import com.subgraph.sgmail.model.Preferences;
import com.subgraph.sgmail.model.StoredUserInterfaceState;
import com.subgraph.sgmail.ui.actions.ComposeMessageAction;
import com.subgraph.sgmail.ui.actions.NewAccountAction;
import com.subgraph.sgmail.ui.actions.NewIdentityAction;
import com.subgraph.sgmail.ui.actions.OpenPreferencesAction;
import com.subgraph.sgmail.ui.actions.RunSynchronizeAction;
import com.subgraph.sgmail.ui.panes.left.LeftPane;
import com.subgraph.sgmail.ui.panes.middle.MiddlePane;
import com.subgraph.sgmail.ui.panes.right.RightPane;

public class MainWindow extends ApplicationWindow {
	
	private final Model model;
	private final TorSupport torSupport;

	private SashForm sashForm;

	private final StoredUserInterfaceState savedState;
	
	public MainWindow(Model model) {
		super(null);
		
		this.model = model;
        this.torSupport = TorSupport.create(model);
		this.savedState = model.getStoredUserInterfaceState();
		
		setBlockOnOpen(true);
		//addToolBar( SWT.WRAP | SWT.FLAT);
		addMenuBar();
		
		setDefaultImage(ImageCache.getInstance().getDisabledImage(ImageCache.USER_IMAGE));
	}
	
	private Action createExitAction() {
		return new Action("Exit") {
			@Override
			public void run() {
				close();
			}
		};
	}
	
	private Action createSyncAction() {
		return new RunSynchronizeAction() {
			@Override
			public void run() {
				if(isChecked()) {
					model.getSynchronizationManager().start();
				} else {
					model.getSynchronizationManager().stop();
				}
			}
		};
	}

	protected Control createContents(Composite parent) {
		sashForm = new SashForm(parent, SWT.HORIZONTAL | SWT.BORDER);
		sashForm.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
		
		final Composite left = new LeftPane(sashForm, model);
		left.setFocus();
		
		final Composite middle = new MiddlePane(sashForm, this, model);
		final Composite right = new RightPane(sashForm, model);
	    
	    
		sashForm.setWeights(getInitialSashWeights());
		
	    final ControlListener listener = createSashResizeListener();
	    left.addControlListener(listener);
	    middle.addControlListener(listener);
	    right.addControlListener(listener);

	    GlobalKeyboardShortcuts.install(this, model);

	    return parent;
	}
	
	private int[] getInitialSashWeights() {
		final int[] savedWeights = savedState.getSashWeights();
		if(Ints.min(savedWeights) <= 0) {
			return new int[] { 150, 350, 500 };
		} else {
			return savedWeights;
		}
	}

	private ControlListener createSashResizeListener() {
		return new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent event) {
				savedState.setSashWeights(sashForm.getWeights());
			}
		};
		
	}

	@Override
	protected ToolBarManager createToolBarManager(int style) { 		
		ToolBarManager toolBarManager = new ToolBarManager(style);
		//toolBarManager.add(new ComposeMessageAction(model));
		//toolBarManager.add(new NewIdentityAction(model));
		return toolBarManager; 	
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("E-Mail Client");
		shell.addControlListener(createControlListener());
	}
	
	private ControlListener createControlListener() {
		return new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				final Rectangle bounds = getShell().getBounds();
				savedState.setShellSize("main", bounds.width, bounds.height);
			}
		};
	}
	
	protected MenuManager createMenuManager() {
		final MenuManager menuManager = new MenuManager();
		
		final MenuManager fileMenu = new MenuManager("File");
		menuManager.add(fileMenu);
		
		//fileMenu.add(new NewIdentityAction(model));
		fileMenu.add(new NewAccountAction(model));
		fileMenu.add(new OpenPreferencesAction(model));
		fileMenu.add(createSyncAction());
		fileMenu.add(createExitAction());
		
		return menuManager;
	}

	public static void main(String[] args) {
        LoggingConfiguration.configure();

        if(args.length == 2 && args[0].equals("--server")) {
            startServer(args[1]);
            return;
        }

		Security.addProvider(new BouncyCastleProvider());
		Display.setAppName("Mail");

		final MainWindow w = new MainWindow(createModel());
		w.setBlockOnOpen(true);
		w.create();
        setupColors();
		w.open();
		w.model.close();

		final Display d = Display.getCurrent();
		if(d != null) {
			d.dispose();
		}
		
	}

    private static void setupColors() {
        JFaceResources.getColorRegistry().put(Resources.COLOR_ERROR_MESSAGE, StringConverter.asRGB("255,0,0"));
    }

    private static void startServer(String propertyFile) {
        try {
            final Properties properties = loadProperties(propertyFile);
            Server s = new Server(properties);
            s.start();
        } catch (IOException e) {
            System.err.println("Error loading properties file: "+ e);
        }
    }
    private static Properties loadProperties(String path) throws IOException {
        final File propertiesFile = new File(path);
        try(Reader reader = new FileReader(propertiesFile)) {
            final Properties p = new Properties();
            p.load(reader);
            return p;
        }
    }
	private static Model createModel() {
		final File home = new File(System.getProperty("user.home"));
		final Model model = new Model(new File(home, ".sgos"));
		model.open();
		if(model.getRootStoredPreferences().getBoolean(Preferences.IMAP_DEBUG_OUTPUT)) {
			model.enableSessionDebug();
		}
		ImageCache.getInstance().setModel(model);
		return model;
	}

	protected Point getInitialSize() {
		final Point p = savedState.getShellSize("main");
		if(p.x != -1 && p.y != -1) {
			return p;
		}
		final Point defaultSize = calculateDefaultInitialSize();
		savedState.setShellSize("main", defaultSize.x, defaultSize.y);
		return defaultSize;
	}
	
	private Point calculateDefaultInitialSize() {
		final Rectangle displayBounds = Display.getDefault().getBounds();
		final int width = displayBounds.width * 80 / 100;
		final int height = displayBounds.height * 75 / 100;
		return new Point(width, height);
	}
}
