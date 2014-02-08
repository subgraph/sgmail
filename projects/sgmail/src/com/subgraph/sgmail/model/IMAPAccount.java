package com.subgraph.sgmail.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.logging.Logger;

import javax.mail.NoSuchProviderException;
import javax.mail.Store;
import javax.mail.URLName;

import com.db4o.activation.ActivationPurpose;
import com.db4o.collections.ActivatableArrayList;
import com.google.common.collect.ImmutableList;

public class IMAPAccount extends AbstractActivatable implements Account {
	
	private final static Logger logger = Logger.getLogger(IMAPAccount.class.getName());
	
	private final static int DEFAULT_IMAPS_PORT = 993;
	private final static int DEFAULT_IMAP_PORT = 143;
	
	private String label;
	private String username;
	private String domain;
	private String realname;
	private String password;
	private String hostname;
	private int port;
	private boolean isSecure;
	private boolean isAutomaticSyncEnabled;
	private List<StoredFolder> folders = new ActivatableArrayList<>();
	private SMTPAccount smtpAccount;
	private Identity identity;
	
	private final StoredAccountPreferences preferences;
	
	private transient Store remoteStore;
	
	public IMAPAccount(Model model, String label, String username, String domain, String realname, String password, String hostname, SMTPAccount smtpAccount) {
		this(model, label, username, domain, realname, password, hostname, DEFAULT_IMAPS_PORT, smtpAccount);
	}

	public IMAPAccount(Model model, String label, String username, String domain, String realname, String password, String hostname, int port, SMTPAccount smtpAccount) {
		this.model = model;
		this.label = checkNotNull(label);
		this.username = checkNotNull(username);
		this.domain = checkNotNull(domain);
		this.realname = checkNotNull(realname);
		this.password = checkNotNull(password);
		this.hostname = checkNotNull(hostname);
		checkArgument(port > 0 && port <= 0xFFFF, "Port value must be between 1 and 65535");
		this.port = port; 
		this.isSecure = true;
		this.isAutomaticSyncEnabled = true;
		this.smtpAccount = smtpAccount;
		this.preferences = new StoredAccountPreferences(this, model);
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
		return new URLName(getProto(), hostname, portValue, null, username, password);
	}

	private int getDefaultPort() {
		return isSecure ? DEFAULT_IMAPS_PORT : DEFAULT_IMAP_PORT;
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
