package com.subgraph.sgmail.ui.dialogs;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.google.common.net.InternetDomainName;
import com.subgraph.sgmail.servers.ServerInformation;

public class AccountDetailsPage extends WizardPage {

	private Text realnameText;
	private Text addressText;
	private Text passwordText;
	private Label errorMessageLabel;
	private IMapServerInfoPanel serverInfoPanel;
	private String previousAddress;
	
	AccountDetailsPage() {
		super("");
		setPageComplete(false);
	}
	
	void setAccountTestError(String message) {
		setErrorMessage(message);
	}

	String getUsername() {
		return getAddressUsername(addressText.getText());
	}
	
	String getDomain() {
		return getAddressDomain(addressText.getText());
	}
	
	String getRealname() {
		return realnameText.getText();
	}
	
	String getPassword() {
		return passwordText.getText();
	}

	ServerInformation getIncomingServer() {
		return serverInfoPanel.getIncomingServer();
	}
	
	ServerInformation getOutgoingServer() {
		return serverInfoPanel.getOutgoingServer();
	}
	
	
	@Override
	public void createControl(Composite parent) {
		final Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new GridLayout());
		createAccountDetailsGroup(c);
		errorMessageLabel = createErrorMessageLabel(c);
		serverInfoPanel = new IMapServerInfoPanel(c);
		serverInfoPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		setControl(c);
	}

	private Group createAccountDetailsGroup(Composite parent) {
		final Group g = new Group(parent, SWT.NONE);
		final GridLayout layout = new GridLayout(2, false);
		layout.verticalSpacing = 8;
		g.setLayout(layout);
		g.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		realnameText = createTextWithLabel(g, "Your Name:", "enter your name", SWT.NONE);
		addressText = createTextWithLabel(g, "Address:", "enter email address", SWT.NONE);
		passwordText = createTextWithLabel(g, "Password:", "enter password", SWT.PASSWORD);
		
		addressText.addFocusListener(createAddressFocusListener());
			
		return g;
		
	}
	
	private Label createErrorMessageLabel(Composite parent) {
		final Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		label.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_RED));
		return label;
	}

	private FocusListener createAddressFocusListener() {
		return new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent event) {
				onAddressChange(addressText.getText());
			}
		};
	}

	private Text createTextWithLabel(Composite parent, String labelText,
			String textMessage, int style) {
		createLabel(parent, labelText);
		return createText(parent, textMessage, style);
	}

	private Label createLabel(Composite parent, String text) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText(text);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		return label;
	}

	private Text createText(Composite parent, String message, int style) {
		final Text text = new Text(parent, SWT.SINGLE | SWT.BORDER | style);
		text.setMessage(message);
		final GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		gd.widthHint = 200;
		text.setLayoutData(gd);
		return text;
	}
	
	private void onAddressChange(String address) {
		if(address == null || address.equals(previousAddress)) {
			return;
		}
		previousAddress = address;
		System.out.println("setting false");
		setPageComplete(false);
		if(isValidAddress(address)) {
			String domain = getAddressDomain(address);
			try {
				getContainer().run(true, true, new AccountLookupTask(this, domain));
			} catch (InvocationTargetException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//new Thread(new AccountLookupTask(this, domain)).start();
		} else {
			serverInfoPanel.clearServerInfo();
		}	
	}

	void setServerInfo(final ServerInformation incoming, final ServerInformation outgoing) {
		getControl().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				serverInfoPanel.setServerInfo(incoming, outgoing);
				System.out.println("setting true (is current "+ isCurrentPage() + ")");
				setPageComplete(true);
			}
		});
	}

	private boolean isValidAddress(String address) {
		final String parts[] = address.split("@");
		if(parts.length != 2) {
			return false;
		}
		if(!InternetDomainName.isValid(parts[1])) {
			return false;
		}
		
		InternetDomainName idn = InternetDomainName.from(parts[1]);
		return idn.hasPublicSuffix();
	}
	
	private String getAddressUsername(String address) {
		return address.split("@")[0];
	}
	private String getAddressDomain(String address) {
		return address.split("@")[1];
	}
	
	public boolean canFlipToNextPage() {
	        return isPageComplete();
	}

	
	public IWizardPage getNextPage() {
		System.out.println("Get next page...");
		if(!verifyAccountDetails()) {
			System.out.println("setting false");
			setPageComplete(false);
			return null;
		}
		return super.getNextPage();
    }
	
	private boolean verifyAccountDetails() {
		final AccountTestLoginTask task = createAccountLoginTest();
		if(task == null) {
			return false;
		}
		try {
			getContainer().run(false, true, task);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			return false;
			// TODO Auto-generated catch block
			
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
			// TODO Auto-generated catch block

		}
		if(task.isSuccess()) {
			return true;
		}
		setErrorMessage(task.getErrorMessage());
		return false;
	}
	
	private AccountTestLoginTask createAccountLoginTest() {
		final ServerInformation incoming = getIncomingServer();
		if(incoming == null) {
			setAccountTestError("No incoming server information");
			return null;
		}
		final String username = getUsername();
		if(username == null) {
			setAccountTestError("No username");
		}
		final String password = getPassword();
		if(password == null) {
			setAccountTestError("No password");
		}
		return new AccountTestLoginTask(null, incoming, username, password);
		
	}
}
