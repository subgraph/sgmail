package com.subgraph.sgmail.model;

import static com.google.common.base.Preconditions.checkState;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import com.db4o.activation.ActivationPurpose;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.sun.mail.gimap.GmailMessage;
import com.sun.mail.imap.IMAPMessage;

public class StoredMessage extends AbstractActivatable implements Conversation {
	
	public static StoredMessage createFromJavamailMessage(IMAPAccount account, Message message, long uid) throws MessagingException {
		if(message instanceof GmailMessage) {
			return createStoredGmailMessage(account, (GmailMessage) message, uid);
		} else {
			return createStoredMessage(message, uid);
		}
	}
	
	private static byte[] getRawMessageBody(Message message) throws MessagingException {
		if(message instanceof IMAPMessage) {
			((IMAPMessage) message).setPeek(true);
		}
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			message.writeTo(output);
			return output.toByteArray();
		} catch (IOException e) {
			throw new MessagingException("IOException reading message body "+ e, e);
		}
	}

	public static long getFlagsFromMessage(Message message) throws MessagingException {
		return getFlagBitsFromFlags(message.getFlags());
	}
	
	public static long getFlagBitFromFlag(Flag flag) {
		if(flagMap.containsKey(flag)) {
			return flagMap.get(flag);
		} else {
			return 0;
		}
	}
	public static long getFlagBitsFromFlags(Flags flags) {
		long flagBits = 0;
		for(Flag f: flagMap.keySet()) {
			if(flags.contains(f)) {
				flagBits |= flagMap.get(f);
			}
		}
		return flagBits;
	}
	
	private static StoredMessage createStoredMessage(Message message, long uid) throws MessagingException {
		final byte[] body = getRawMessageBody(message);
		final long flags = getFlagsFromMessage(message);
		return new StoredMessage(uid, body, flags);
	}
	
	private static StoredGmailMessage createStoredGmailMessage(IMAPAccount account, GmailMessage message, long uid) throws MessagingException {
		final byte[] body = getRawMessageBody(message);
		final long flags = getFlagsFromMessage(message);
		final GmailIMAPAccount gmailAccount = (GmailIMAPAccount) account;
		final StoredGmailMessage gmsg = new StoredGmailMessage(uid, body, flags, message.getMsgId(), message.getThrId());
		for(String label: message.getLabels()) {
			if(!label.isEmpty()) {
				gmsg.addLabel(gmailAccount.getLabelByName(label));
			}
		}
		return gmsg;
	}
	
	public void mergeFlags(Message message) throws MessagingException {
		activate(ActivationPurpose.READ);
		if(offlineUpdatedFlags == 0) {
			return;
		}
		for(Entry<Flag, Long> entry: flagMap.entrySet()) {
			mergeSingleFlag(message, entry.getKey(), entry.getValue());
		}
	}

	private void mergeSingleFlag(Message remoteMessage, Flag f, long bit) throws MessagingException {
		if(!isFlagBitSet(offlineUpdatedFlags, bit)) {
			return;
		}
		if(isFlagBitSet(flags, bit)) {
			remoteMessage.setFlag(f, true);
		} else {
			remoteMessage.setFlag(f, false);
		}
	}

	private static boolean isFlagBitSet(long flagBits, long flag) {
		return((flagBits & flag) == flag);
	}
	
	private final static BiMap<Flag, Long> flagMap = new ImmutableBiMap.Builder<Flags.Flag, Long>()
			.put(Flag.ANSWERED, StoredMessage.FLAG_ANSWERED)
			.put(Flag.DELETED, StoredMessage.FLAG_DELETED)
			.put(Flag.DRAFT, StoredMessage.FLAG_DRAFT)
			.put(Flag.FLAGGED, StoredMessage.FLAG_FLAGGED)
			.put(Flag.RECENT, StoredMessage.FLAG_RECENT)
			.put(Flag.SEEN, StoredMessage.FLAG_SEEN)
			.build();

	public final static long FLAG_ANSWERED           = 0x01;
	public final static long FLAG_DELETED            = 0x02;
	public final static long FLAG_DRAFT              = 0x04;
	public final static long FLAG_FLAGGED            = 0x08;
	public final static long FLAG_RECENT             = 0x10;
	public final static long FLAG_SEEN               = 0x20;
	
	private StoredFolder folder;
	private int messageNumber;
	private final long messageUID;
	private final byte[] messageData;
	
	private long flags;
	private long offlineUpdatedFlags;
	
	private boolean deleted;
	
	public StoredMessage(long uid, byte[] messageData, long flags) {
		this.messageUID = uid;
		this.messageData = messageData;
		this.flags = flags;
	}
	

	private transient MimeMessage cachedMimeMessage;
	
	public MimeMessage toMimeMessage() throws MessagingException {
		activate(ActivationPurpose.READ);
		if(cachedMimeMessage == null) {
			cachedMimeMessage = new LocalMimeMessage(this, model.getSession(), new ByteArrayInputStream(messageData));
		}
		return cachedMimeMessage;
	}
	
	public long getFlags() {
		activate(ActivationPurpose.READ);
		return flags;
	}
	
	public void setFlags(long value) {
		activate(ActivationPurpose.WRITE);
		this.flags = value;
	}
	
	public void addFlag(long flag) {
		activate(ActivationPurpose.WRITE);
		processChangedFlag(flag, true);
		this.flags |= flag;
	}
	
	public void removeFlag(long flag) {
		activate(ActivationPurpose.WRITE);
		processChangedFlag(flag, false);
		this.flags &= ~flag;
	}

	public long getOfflineUpdatedFlags() {
		activate(ActivationPurpose.READ);
		return offlineUpdatedFlags;
	}

	public void addOfflineUpdatedFlag(long flag) {
		activate(ActivationPurpose.WRITE);
		this.offlineUpdatedFlags |= flag;
	}
	
	public void clearOfflineUpdatedFlags() {
		activate(ActivationPurpose.WRITE);
		this.offlineUpdatedFlags = 0;
	}

	public boolean isFlagSet(long flag) {
		activate(ActivationPurpose.READ);
		return (flags & flag) == flag;
	}

	public void setFolder(StoredFolder folder) {
		activate(ActivationPurpose.WRITE);
		this.folder = folder;
	}
	
	public StoredFolder getFolder() {
		activate(ActivationPurpose.READ);
		return folder;
	}

	public void setMessageNumber(int value) {
		activate(ActivationPurpose.WRITE);
		messageNumber = value;
	}
	public int getMessageNumber() {
		activate(ActivationPurpose.READ);
		return messageNumber;
	}
	
	public long getMessageUID() {
		activate(ActivationPurpose.READ);
		return messageUID;
	}
	
	public void setDeleted(boolean flag) {
		activate(ActivationPurpose.WRITE);
		deleted = flag;
	}
	
	public boolean getDeleted() {
		activate(ActivationPurpose.READ);
		return deleted;
	}
	
	public boolean isNewMessage() {
		activate(ActivationPurpose.READ);
		return !isFlagSet(FLAG_SEEN);
	}

	
	private void processChangedFlag(long flagBit, boolean isSet) {
		activate(ActivationPurpose.READ);
		final Flag flag = flagMap.inverse().get(flagBit);
		checkState(folder != null && (folder.getAccount() instanceof IMAPAccount));
		final IMAPAccount account = (IMAPAccount) folder.getAccount();
		model.getSynchronizationManager().updateFlag(account, this, flag, isSet);
	}

	@Override
	public boolean hasUndeletedMessages() {
		return !isFlagSet(FLAG_DELETED);
	}

	@Override
	public int getMessageCount() {
		return 1;
	}

	@Override
	public List<StoredMessage> getMessages() {
		return Arrays.asList(this);
	}

	@Override
	public StoredMessage getLeadMessage() {
		return this;
	}

	@Override
	public int getNewMessageCount() {
		return isNewMessage() ? 1 : 0;
	}
}
