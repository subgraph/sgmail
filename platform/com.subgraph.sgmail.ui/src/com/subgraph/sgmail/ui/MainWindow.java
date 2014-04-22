package com.subgraph.sgmail.ui;

import com.google.common.primitives.Ints;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.subgraph.sgmail.IEventBus;
import com.subgraph.sgmail.JavamailUtils;
import com.subgraph.sgmail.database.Database;
import com.subgraph.sgmail.database.Model;
import com.subgraph.sgmail.database.StoredUserInterfaceState;
import com.subgraph.sgmail.identity.IdentityManager;
import com.subgraph.sgmail.imap.IMAPSynchronizationManager;
import com.subgraph.sgmail.openpgp.MessageProcessor;
import com.subgraph.sgmail.search.MessageSearchIndex;
import com.subgraph.sgmail.ui.actions.ComposeMessageAction;
import com.subgraph.sgmail.ui.actions.NewAccountAction;
import com.subgraph.sgmail.ui.actions.OpenPreferencesAction;
import com.subgraph.sgmail.ui.actions.RunSynchronizeAction;
import com.subgraph.sgmail.ui.panes.left.LeftPane;
import com.subgraph.sgmail.ui.panes.middle.MiddlePane;
import com.subgraph.sgmail.ui.panes.right.RightPane;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.security.Security;
import java.util.Properties;

public class MainWindow extends ApplicationWindow {
	
	private final Model model;
	private final IEventBus eventBus;
	private final IdentityManager identityManger;
	private final JavamailUtils javamailUtils;
	private final MessageProcessor messageProcessor;
	private final ListeningExecutorService globalExecutor;
	private final MessageSearchIndex messageSearchIndex;
	
	private final TorSupport torSupport;

	private SashForm sashForm;

	private final StoredUserInterfaceState savedState;
	
	
	public MainWindow(Model model, IEventBus eventBus, IdentityManager identityManager, JavamailUtils javamailUtils, MessageProcessor messageProcessor, ListeningExecutorService globalExecutor, MessageSearchIndex messageSearchIndex) {
		super(null);
		this.model = model;
		this.eventBus = eventBus;
		this.identityManger = identityManager;
		this.javamailUtils = javamailUtils;
		this.messageProcessor = messageProcessor;
		this.globalExecutor = globalExecutor;
		this.messageSearchIndex = messageSearchIndex;
		
        this.torSupport = TorSupport.create(eventBus, model);
        
		this.savedState = model.getStoredUserInterfaceState();
		
		setBlockOnOpen(true);
		addToolBar( SWT.WRAP | SWT.FLAT);
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
				final IMAPSynchronizationManager syncManager = Activator.getInstance().getIMAPSynchronizationManager();
				if(isChecked()) {
					syncManager.start();
				} else {
					syncManager.stop();
				}
			}
		};
	}

	protected Control createContents(Composite parent) {
		sashForm = new SashForm(parent, SWT.HORIZONTAL | SWT.BORDER);
		sashForm.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
        sashForm.setSashWidth(1);

		final Composite left = new LeftPane(sashForm, eventBus, model);
		left.setFocus();
		
		final Composite middle = new MiddlePane(sashForm, eventBus);
		final Composite right = new RightPane(sashForm, messageProcessor, model, globalExecutor, eventBus, identityManger, javamailUtils);
	    
	    
		sashForm.setWeights(getInitialSashWeights());
		
	    final ControlListener listener = createSashResizeListener();
	    left.addControlListener(listener);
	    middle.addControlListener(listener);
	    right.addControlListener(listener);

	    GlobalKeyboardShortcuts.install(this, model, javamailUtils, eventBus, messageProcessor, identityManger);

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
        SearchBarContribution search = new SearchBarContribution(eventBus, messageSearchIndex, globalExecutor);
        toolBarManager.add(new ComposeMessageAction());
        //toolBarManager.add(new NewIdentityAction(model));
        toolBarManager.add(new SpacerContribution(30));
        toolBarManager.add(search);
		return toolBarManager;
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Subgraph Mail");
		shell.addControlListener(createControlListener());
	}
	
	private ControlListener createControlListener() {
		return new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
                getToolBarManager().update(true);
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
		fileMenu.add(new NewAccountAction());
		fileMenu.add(new OpenPreferencesAction());
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

		
		final Activator a = Activator.getInstance();
		final Database database = a.getDatabase();
		final Model model = a.getModel();
		final IEventBus eventBus = a.getEventBus();
		final IdentityManager identityManager = a.getIdentityManager();
		final JavamailUtils javamailUtils = a.getJavamailUtils();
		final MessageProcessor messageProcessor = a.getMessageProcessor();
		final ListeningExecutorService globalExecutor = a.getGlobalExecutor();
		final MessageSearchIndex messageSearchIndex = a.getMessageSearchIndex();
				
		final File home = new File(System.getProperty("user.home"));
		final File sgos = new File(home, ".sgos");
		final File index = new File(sgos, "index");
		database.open(sgos);
		messageSearchIndex.setIndexDirectory(index);
		
		final MainWindow w = new MainWindow(model, eventBus, identityManager, javamailUtils, messageProcessor, globalExecutor, messageSearchIndex);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown(w.model.getDatabase())));
        Resources.initialize();
		w.setBlockOnOpen(true);
		w.create();
		w.open();
        shutdown(w.model.getDatabase());

		final Display d = Display.getCurrent();
		if(d != null) {
			d.dispose();
		}
		
	}

    private final static Object shutdownLock = new Object();
    private static boolean isShutdown = false;

    private static void shutdown(Database database) {
        synchronized (shutdownLock) {
            if(isShutdown) {
                return;
            }
            database.close();
            LoggingConfiguration.close();
            isShutdown = true;
        }

    }

    private static void startServer(String propertyFile) {
        try {
            final Properties properties = loadProperties(propertyFile);
            //Server s = new Server(properties);
            //s.start();
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
    /*
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
	*/

	protected Point getInitialSize() {
		final int width = savedState.getShellWidth("main");
		final int height = savedState.getShellHeight("main");
		final Point p = new Point(width, height);
		
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
