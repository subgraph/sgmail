package com.subgraph.sgmail.ui.dialogs;

import com.google.common.net.InternetDomainName;
import com.subgraph.sgmail.accounts.MailAccount;
import com.subgraph.sgmail.accounts.ServerDetails;
import com.subgraph.sgmail.imap.IMAPAccount;
import com.subgraph.sgmail.model.Model;
import com.subgraph.sgmail.model.Preferences;
import com.subgraph.sgmail.servers.ServerInformation;
import com.subgraph.sgmail.ui.Resources;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

public class AccountDetailsPage extends WizardPage {
    private final static Logger logger = Logger.getLogger(AccountDetailsPage.class.getName());

    private static class TextFieldWithErrorLabel {
        private final static int BASIC_TEXT_FLAGS = SWT.SINGLE | SWT.BORDER;
        private final Text textField;
        private final Label errorLabel;
        private final Color defaultLabelForeground;

        TextFieldWithErrorLabel(Composite parent, String labelText, String textMessage, boolean password) {
            final Label label = new Label(parent, SWT.RIGHT);
            label.setText(labelText);
            label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

            final int flags = (password) ? (BASIC_TEXT_FLAGS | SWT.PASSWORD) : (BASIC_TEXT_FLAGS);
            textField = new Text(parent, flags);
            textField.setMessage(textMessage);
            final GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
            gd.widthHint = 200;
            textField.setLayoutData(gd);

            errorLabel = new Label(parent, SWT.NONE);
            errorLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            defaultLabelForeground = errorLabel.getForeground();
        }

        void addTextFocusListener(FocusListener listener) {
            textField.addFocusListener(listener);
        }

        void addTextModifyListener(ModifyListener listener) {
            textField.addModifyListener(listener);
        }

        String getText() {
            return textField.getText();
        }

        void setErrorText(String message) {
            final Color errorColor = JFaceResources.getColorRegistry().get(Resources.COLOR_ERROR_MESSAGE);
            if(errorColor != null) {
                errorLabel.setForeground(errorColor);
            }
            errorLabel.setText(message);
        }

        void setInfoText(String message) {
            errorLabel.setForeground(defaultLabelForeground);
            errorLabel.setText(message);
        }
    }

    private final Model model;

    private TextFieldWithErrorLabel realnameField;
    private TextFieldWithErrorLabel addressField;
    private TextFieldWithErrorLabel passwordField;
	private IMapServerInfoPanel serverInfoPanel;
	private String previousAddress;
    private boolean isAddressValid;
	
	AccountDetailsPage(Model model) {
		super("details");
        this.model = model;
        setTitle("Email account details");
        setDescription("Enter information about your email account");
		setPageComplete(false);
	}
	
	void setAccountTestError(String message) {
		setErrorMessage(message);
	}

	public String getUsername() {
		return getAddressUsername(addressField.getText());
	}
	
	public String getDomain() {
		return getAddressDomain(addressField.getText());
	}
	
	public String getRealname() {
		return realnameField.getText();
	}
	
	String getPassword() {
		return passwordField.getText();
	}

    String getIncomingLogin() {
        return getUsernameByType(getIncomingServer().getUsernameType());
    }

    String getOutgoingLogin() {
        return getUsernameByType(getOutgoingServer().getUsernameType());
    }

    private String getUsernameByType(ServerInformation.UsernameType type) {
        final String email = addressField.getText();
        switch (type) {
            case USERNAME_EMAILADDRESS:
                return email;

            case USERNAME_LOCALPART:
                return getAddressUsername(email);

            case UNKNOWN:
            default:
                logger.warning("Unknown username type, returning full address");
                return email;
        }
    }

	ServerInformation getIncomingServer() {
		return serverInfoPanel.getIncomingServer();
	}
	
	ServerInformation getOutgoingServer() {
		return serverInfoPanel.getOutgoingServer();
	}

    IMAPAccount createIMAPAccount() {
        final ServerDetails smtpServer = createServerDetails(getOutgoingServer(), "smtps", getOutgoingLogin(), getPassword());
        final String imapProtocol = getIMAPProtocol(getIncomingServer());
        final ServerDetails imapServer = createServerDetails(getIncomingServer(), imapProtocol, getIncomingLogin(), getPassword());
        final MailAccount mailAccount = MailAccount.create(addressField.getText(), addressField.getText(), getRealname(), smtpServer);
        return new IMAPAccount(mailAccount, imapServer);
    }

    private String getIMAPProtocol(ServerInformation imapServer) {
        final String hostname = imapServer.getHostname();
        if(hostname.endsWith("gmail.com") || hostname.endsWith("googlemail.com")) {
            return "gimaps";
        } else {
            return "imaps";
        }
    }

    private ServerDetails createServerDetails(ServerInformation info, String protocol, String login, String password) {
        return ServerDetails.create(protocol, info.getHostname(), info.getOnionHostname(), info.getPort(), login, password);
    }
	
	@Override
	public void createControl(Composite parent) {
		final Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new GridLayout());
		createAccountDetailsGroup(c);
        boolean useTor = model.getRootStoredPreferences().getBoolean(Preferences.TOR_ENABLED);
		serverInfoPanel = new IMapServerInfoPanel(c, useTor);
		serverInfoPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		setControl(c);
	}

	private Group createAccountDetailsGroup(Composite parent) {
		final Group g = new Group(parent, SWT.NONE);
		final GridLayout layout = new GridLayout(3, false);
		layout.verticalSpacing = 8;
		g.setLayout(layout);
		g.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        realnameField = new TextFieldWithErrorLabel(g, "Your Name:", "enter your name", false);
        realnameField.addTextModifyListener(createTextModifyListener());
        addressField = new TextFieldWithErrorLabel(g, "Address:", "enter email address", false);
        addressField.addTextFocusListener(createAddressFocusListener());
        passwordField = new TextFieldWithErrorLabel(g, "Password:", "enter password", true);
        passwordField.addTextModifyListener(createTextModifyListener());
		return g;
	}
	
	private FocusListener createAddressFocusListener() {
		return new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent event) {
				onAddressChange(addressField.getText());
                testPageComplete();
			}
		};
	}

    private ModifyListener createTextModifyListener() {
        return new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                testPageComplete();
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
        isAddressValid = false;

		if(isValidAddress(address)) {
            final String domain = getAddressDomain(address);
			try {
                addressField.setInfoText("Searching provider information...");
				final AccountLookupTask task = new AccountLookupTask(this, domain);
				getContainer().run(false, true, task);
				if(task.getLookupSucceeded()) {
                    addressField.setInfoText("");
                    isAddressValid = true;
				} else {
                    addressField.setErrorText("No info found for "+ domain);
                    serverInfoPanel.clearServerInfo();
                }
				
			} catch (InvocationTargetException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
            addressField.setErrorText("Invalid email address");
			serverInfoPanel.clearServerInfo();
		}	
	}

    private void testPageComplete() {
        final boolean complete = (isAddressValid && !getRealname().isEmpty() && !getPassword().isEmpty());
        setPageComplete(complete);
    }

	void setServerInfo(final ServerInformation incoming, final ServerInformation outgoing) {
		getControl().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				serverInfoPanel.setServerInfo(incoming, outgoing);
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
		if(!verifyAccountDetails()) {
			return null;
		}
		return super.getNextPage();
    }
	
	private boolean verifyAccountDetails() {
		final AccountTestLoginTask task = createAccountLoginTest();
		if(task == null) {
            testPageComplete();
			return false;
		}
		try {
            passwordField.setInfoText("Verifying login details");
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
            passwordField.setInfoText("");
			return true;
		} else {
            passwordField.setErrorText(task.getErrorMessage());
            return false;
        }
	}
	
	private AccountTestLoginTask createAccountLoginTest() {
		final ServerInformation incoming = getIncomingServer();
		if(incoming == null) {
			setAccountTestError("No incoming server information");
			return null;
		}
		final String login = getIncomingLogin();
		if(login == null) {
			setAccountTestError("No username");
		}
		final String password = getPassword();
		if(password == null) {
			setAccountTestError("No password");
		}

        final boolean useTor = model.getRootStoredPreferences().getBoolean(Preferences.TOR_ENABLED);
        final boolean debug = model.getRootStoredPreferences().getBoolean(Preferences.IMAP_DEBUG_OUTPUT);
		return new AccountTestLoginTask(incoming, login, password, useTor, debug);
	}
}
