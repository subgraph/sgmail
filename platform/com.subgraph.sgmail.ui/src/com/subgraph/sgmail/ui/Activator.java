package com.subgraph.sgmail.ui;


import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.subgraph.sgmail.IEventBus;
import com.subgraph.sgmail.JavamailUtils;
import com.subgraph.sgmail.accounts.AccountFactory;
import com.subgraph.sgmail.autoconf.MailserverAutoconfig;
import com.subgraph.sgmail.database.Database;
import com.subgraph.sgmail.database.Model;
import com.subgraph.sgmail.imap.IMAPFactory;
import com.subgraph.sgmail.imap.IMAPSynchronizationManager;
import com.subgraph.sgmail.messages.MessageFactory;
import com.subgraph.sgmail.nyms.NymsAgent;
import com.subgraph.sgmail.search.MessageSearchIndex;

public class Activator implements BundleActivator {

	private static BundleContext context;

	private ServiceTracker<MailserverAutoconfig, MailserverAutoconfig> mailserverAutoconfigTracker;
	private ServiceTracker<IMAPFactory,IMAPFactory> imapFactoryTracker;
	private ServiceTracker<AccountFactory,AccountFactory> accountFactoryTracker;
	private ServiceTracker<MessageFactory,MessageFactory> messageFactoryTracker;
	private ServiceTracker<IMAPSynchronizationManager,IMAPSynchronizationManager> imapSyncTracker;
	private ServiceTracker<Database, Database> databaseTracker;
	private ServiceTracker<Model, Model> modelTracker;
	private ServiceTracker<IEventBus,IEventBus> eventBusTracker;
	private ServiceTracker<ListeningExecutorService,ListeningExecutorService> executorTracker;
	private ServiceTracker<JavamailUtils,JavamailUtils> javamailUtilsTracker;
	private ServiceTracker<MessageSearchIndex,MessageSearchIndex> messageSearchIndexTracker;
	private ServiceTracker<NymsAgent, NymsAgent> nymsAgentTracker;
	
	
	private static Activator _INSTANCE;
	
	public static Activator getInstance() {
		return _INSTANCE;
	}
	
	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		Activator._INSTANCE = this;
		mailserverAutoconfigTracker = openTracker(bundleContext, MailserverAutoconfig.class);
		accountFactoryTracker = openTracker(bundleContext, AccountFactory.class);
		messageFactoryTracker = openTracker(bundleContext, MessageFactory.class);
		imapFactoryTracker = openTracker(bundleContext, IMAPFactory.class);
		imapSyncTracker = openTracker(bundleContext, IMAPSynchronizationManager.class);
		databaseTracker = openTracker(bundleContext, Database.class);
		modelTracker = openTracker(bundleContext, Model.class);
		eventBusTracker = openTracker(bundleContext, IEventBus.class);
		executorTracker = openTracker(bundleContext, ListeningExecutorService.class);
		javamailUtilsTracker = openTracker(bundleContext, JavamailUtils.class);
		messageSearchIndexTracker = openTracker(bundleContext, MessageSearchIndex.class);
		nymsAgentTracker = openTracker(bundleContext, NymsAgent.class);
	}
	
	private <T> ServiceTracker<T,T> openTracker(BundleContext ctx, Class<T> clazz) {
		final ServiceTracker<T,T> st = new ServiceTracker<>(ctx, clazz, null);
		st.open();
		return st;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}
	
	public MailserverAutoconfig getMailserverAutoconfig() {
		return mailserverAutoconfigTracker.getService();
	}
	
	public IMAPFactory getIMAPFactory() {
		return imapFactoryTracker.getService();
	}
	
	public AccountFactory getAccountFactory() {
		return accountFactoryTracker.getService();
	}
	
	public MessageFactory getMessageFactory() {
		return messageFactoryTracker.getService();
	}
	
	public IMAPSynchronizationManager getIMAPSynchronizationManager() {
		return imapSyncTracker.getService();
	}
	
	public Database getDatabase() {
		return databaseTracker.getService();
	}
	
	public Model getModel() {
		return modelTracker.getService();
	}
	
	public IEventBus getEventBus() {
		return eventBusTracker.getService();
	}
	
	public NymsAgent getNymsAgent() {
	  return nymsAgentTracker.getService();
	}
	
	public ListeningExecutorService getGlobalExecutor() {
		return executorTracker.getService();
	}
	
	public JavamailUtils getJavamailUtils() {
		return javamailUtilsTracker.getService();
	}
	
	public MessageSearchIndex getMessageSearchIndex() {
		return messageSearchIndexTracker.getService();
	}
}
