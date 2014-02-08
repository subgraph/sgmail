package com.subgraph.sgmail.ui.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.google.common.net.InternetDomainName;
import com.subgraph.sgmail.servers.ServerInformation;

public class NewAccountDialog extends TitleAreaDialog {

	private String username;
	private String domain;
	private String realname;
	private String password;
	
	private Text realnameText;
	private Text addressText;
	private Text passwordText;

	private String previousAddress;
	
	private Label errorMessageLabel;
	private IMapServerInfoPanel serverInfoPanel;
	private ServerInformation serverInfo;

	public NewAccountDialog(Shell parentShell) {
		super(parentShell);
	}

	public ServerInformation getIncomingServer() {
		return serverInfoPanel.getIncomingServer();
	}
	
	public ServerInformation getOutgoingServer() {
		return serverInfoPanel.getOutgoingServer();
	}

	public String getPassword() {
		return password;
	}
	public String getUsername() {
		return username;
	}
	public String getDomain() {
		return domain;
		
	}
	
	public String getRealname() {
		return realname;
	}

	public ServerInformation getServerInformation() {
		return serverInfo;
	}

	public void accountVerificationSucceeded() {
		getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				setReturnCode(OK);
				close();
			}
		});
	}
	
	public void accountVerificationFailed(final String message, final boolean isLoginFailure) {
		getShell().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				getButton(IDialogConstants.OK_ID).setEnabled(true);
				errorMessageLabel.setText(message);
				if(isLoginFailure) {
					passwordText.setText("");
					passwordText.setFocus();
				}
			}
		});
		
	}
	
	protected void okPressed() {
		username = getAddressUsername(addressText.getText());
		domain = getAddressDomain(addressText.getText());
		realname = realnameText.getText();
		password = passwordText.getText();
		errorMessageLabel.setText("");
		getButton(IDialogConstants.OK_ID).setEnabled(false);
		
		//final Runnable r = new AccountTestLoginTask(this, serverInfoPanel.getIncomingServer(), username, password);
		//new Thread(r).start();
	}

	protected Control createContents(Composite parent) {
		final Control contents = super.createContents(parent);
		setMessage("Create a new account");
		setTitle("Account creation");
		enableOkButton(false);
		getButton(IDialogConstants.OK_ID).setText("Create");
		return contents;
	}
	
	private void enableOkButton(boolean enabled) {
		final Button button = getButton(IDialogConstants.OK_ID);
		if(button != null) {
			button.setEnabled(enabled);
		}
	}

	protected Control createDialogArea(Composite parent) {
		final Composite composite = new Composite(
				(Composite) super.createDialogArea(parent), SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout());
		
		Group g = new Group(composite, SWT.NONE);
		final GridLayout layout = new GridLayout(2, false);
		layout.verticalSpacing = 8;
		g.setLayout(layout);
		g.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		
		realnameText = createTextWithLabel(g, "Your Name:", "enter your name", SWT.NONE);

		addressText = createTextWithLabel(g, "Address:","enter email address", SWT.NONE);
		addressText.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				onAddressChange(addressText.getText());
			}
		});
		passwordText = createTextWithLabel(g, "Password:",
				"enter password", SWT.PASSWORD);
		
		errorMessageLabel = new Label(composite, SWT.NONE);
		errorMessageLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		errorMessageLabel.setForeground(composite.getDisplay().getSystemColor(SWT.COLOR_RED));
		
		serverInfoPanel = new IMapServerInfoPanel(composite);
		final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		gd.heightHint = 200;
		serverInfoPanel.setLayoutData(gd);
				

		return composite;
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
		enableOkButton(false);
		if(isValidAddress(address)) {
			String domain = getAddressDomain(address);
			//new Thread(new AccountLookupTask(this, domain)).start();
		} else {
			serverInfoPanel.clearServerInfo();
		}
	}
	
	void setServerInfo(final ServerInformation incoming, final ServerInformation outgoing) {
		getParentShell().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				serverInfoPanel.setServerInfo(incoming, outgoing);
				enableOkButton(true);
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
}
