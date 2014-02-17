package com.subgraph.sgmail.model;

import com.db4o.activation.ActivationPurpose;
import com.db4o.collections.ActivatableArrayList;
import com.google.common.collect.ImmutableList;

import javax.mail.NoSuchProviderException;
import javax.mail.Store;
import javax.mail.URLName;
import java.util.List;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class IMAPAccount extends AbstractActivatable implements Account {
	
	private final static Logger logger = Logger.getLogger(IMAPAccount.class.getName());
	
	private final static int DEFAULT_IMAPS_PORT = 993;
	private final static int DEFAULT_IMAP_PORT = 143;

    public static class Builder {
        private boolean isGmail;
        private String label;
        private String login;
        private String username;
        private String domain;
        private String realname;
        private String password;
        private String hostname;
        private String onionHostname;
        private SMTPAccount smtpAccount;
        private int port;

        public Builder label(String s) { label = s; return this; }
        public Builder login(String s) { login = s; return this; }
        public Builder hostname(String s) { hostname = s; return this; }
        public Builder username(String s) { username = s; return this; }
        public Builder domain(String s) { domain = s; return this; }
        public Builder realname(String s) { realname = s; return this; }
        public Builder gmail(boolean b) { isGmail = b; return this; }
        public Builder password(String s) { password = s; return this; }
        public Builder port(int n) { port = n; return this; }
        public Builder onion(String s) { onionHostname = s; return this; }
        public Builder smtp(SMTPAccount smtp) { smtpAccount = smtp; return this; };

        public IMAPAccount build(Model model) {
            if(isGmail) {
                return new GmailIMAPAccount(this, model);
            } else {
                return new IMAPAccount(this, model);
            }
        }
    }

	private final String label;
    private final String login;
	private final String username;
	private final String domain;
	private final String realname;
	private final String password;
    private final String onionHostname;
	private final String hostname;
	private final int port;
    private final SMTPAccount smtpAccount;
	private boolean isSecure;
	private boolean isAutomaticSyncEnabled;
	private List<StoredFolder> folders = new ActivatableArrayList<>();

	private Identity identity;
	
	private final StoredAccountPreferences preferences;
	
	private transient Store remoteStore;

    protected IMAPAccount(Builder builder, Model model) {
        this.model = model;
        this.label = checkNotNull(builder.label);
        this.login = checkNotNull(builder.login);
        this.username = checkNotNull(builder.username);
        this.domain = checkNotNull(builder.domain);
        this.realname = checkNotNull(builder.realname);
        this.password = checkNotNull(builder.password);
        this.hostname = checkNotNull(builder.hostname);
        this.onionHostname = builder.onionHostname;
        this.port = builder.port;
        checkArgument(port > 0 && port <= 0xFFFF, "Port value must be between 1 and 65535");
        this.smtpAccount = checkNotNull(builder.smtpAccount);
        this.isSecure = true;
        this.isAutomaticSyncEnabled = true;

        this.preferences = StoredAccountPreferences.create(this, model);
    }

	public Model getModel() {
		return model;
	}
	
	public StoredAccountPreferences getPreferences() {
		activate(ActivationPurpose.READ);
		return preferences;
	}

	public void setAutomaticSyncEnabled(boolean value) {
		activate(ActivationPurpose.WRITE);
		isAutomaticSyncEnabled = value;
	}

	public boolean isAutomaticSyncEnabled() {
		activate(ActivationPurpose.READ);
		return isAutomaticSyncEnabled;
	}

	public List<StoredFolder> getFolders() {
		activate(ActivationPurpose.READ);
		synchronized (folders) {
			return ImmutableList.copyOf(folders);
		}
	}

	public StoredFolder getFolder(String name) {
		activate(ActivationPurpose.READ);
		synchronized (folders) {
			final StoredFolder folder = findFolderByName(name);
			return (folder != null) ? (folder) : (createNewFolder(name));
		}
	}
	
	private StoredFolder findFolderByName(String name) {
		for(StoredFolder f: folders) {
			if(f.getFullName().equals(name)) {
				return f;
			}
		}
		return null;
	}
	
	private StoredFolder createNewFolder(String name) {
		final StoredFolder newFolder = new StoredFolder(this, name);
		model.store(newFolder);
		folders.add(newFolder);
		return newFolder;
	}

	@Override
	public String getLabel() {
		activate(ActivationPurpose.READ);
		return label;
	}

    public String getLogin() {
        activate(ActivationPurpose.READ);
        return login;
    }

	public String getEmailAddress() {
		return getUsername() + "@" + getDomain();
	}

	public String getUsername() {
		activate(ActivationPurpose.READ);
		return username;
	}
	
	public String getDomain() {
		activate(ActivationPurpose.READ);
		return domain;
	}

	public String getRealname() {
		activate(ActivationPurpose.READ);
		return realname;
	}

	public String getPassword() {
		activate(ActivationPurpose.READ);
		return password;
	}
	
	public String getHostname() {
		activate(ActivationPurpose.READ);
		return hostname;
	}
	
	public int getPort() {
		activate(ActivationPurpose.READ);
		return port;
	}
	
	public boolean isSecure() {
		activate(ActivationPurpose.READ);
		return isSecure;
	}

	public String getSMTPUsername() {
		activate(ActivationPurpose.READ);
		return smtpAccount.getUsername();
	}
	
	public String getSMTPPassword() {
		activate(ActivationPurpose.READ);
		return smtpAccount.getPassword();
	}
	
	public int getSMTPPort() {
		activate(ActivationPurpose.READ);
		return smtpAccount.getPort();
	}
	
	public String getSMTPHostname() {
		activate(ActivationPurpose.READ);
		return smtpAccount.getHostname();
	}

	public URLName getURLName() {
		activate(ActivationPurpose.READ);
		final int portValue = (port == getDefaultPort()) ? -1 : port;
        if(onionHostname != null && model.getRootStoredPreferences().getBoolean(Preferences.TOR_ENABLED)) {
            return new URLName(getProto(), onionHostname, portValue, null, login, password);
        } else {
            return new URLName(getProto(), hostname, portValue, null, login, password);
        }
	}

	private int getDefaultPort() {
		return isSecure ? DEFAULT_IMAPS_PORT : DEFAULT_IMAP_PORT;
	}

    SMTPAccount getSmtpAccount() {
        activate(ActivationPurpose.READ);
        return smtpAccount;
    }

	protected String getProto() {
		return (isSecure) ? "imaps" : "imap";
	}

	@Override
	public Store getRemoteStore() {
		if(remoteStore == null) {
			remoteStore = createRemoteStore();
		}
		return remoteStore;
	}
	
	private Store createRemoteStore() {
		final URLName urlname = getURLName();
        try {
			return model.getSession().getStore(urlname);
		} catch (NoSuchProviderException e) {
			logger.warning("Could not create store for "+ urlname + " : "+ e);
			return null;
		}
	}

	@Override
	public void setIdentity(Identity identity) {
		activate(ActivationPurpose.WRITE);
		this.identity = identity;
	}

	@Override
	public Identity getIdentity() {
		activate(ActivationPurpose.READ);
		return identity;
	}
}
