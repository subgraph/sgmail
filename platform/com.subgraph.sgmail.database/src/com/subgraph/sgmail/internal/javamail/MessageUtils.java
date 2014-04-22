package com.subgraph.sgmail.internal.javamail;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.subgraph.sgmail.messages.StoredMessage;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class MessageUtils {
	private final static Logger logger = Logger.getLogger(MessageUtils.class.getName());
	
	private final static long TIME_24_HOURS = (24 * 60 * 60 * 1000);
	
	private final static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
	private final static SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd");

    public static String getSubject(StoredMessage message, Session session) {
        try {
            return getSubject(message.toMimeMessage(session));
        } catch (MessagingException e) {
            logger.warning("Error converting to Mime message "+ e);
            return "";
        }
    }

	public static String getSubject(Message message) {
		
		try {
			final String subject = message.getSubject();
			if(subject == null) {
				return "No Subject";
			} else {
				return subject;
			}
			
		} catch (MessagingException e) {
			logger.warning("Error reading message subject "+ e);
			return "Subject error";
		}
	}
	
	
	public  static String getSentDate(Message message) {
		final Date sentDate = getSentDateFromMessage(message);

		if(sentDate == null) {
			return "No sent date";
		}
		final long ts = sentDate.getTime();
		if(ts < TIME_24_HOURS) {
			return timeFormat.format(sentDate);
		} else if (ts < (2 * TIME_24_HOURS)) {
			return "Yesterday";
		} else {
			return dateFormat.format(sentDate);
		}
	}
	
	private static Date getSentDateFromMessage(Message message) {
		try {
			return message.getSentDate();
		} catch (MessagingException e) {
			logger.warning("Error getting sent date from message "+ e);
			return null;
		}
	}

	public static String getSender(Message message) {
		return getSender(message, false);
	}

	public static String getSender(Message message, boolean verbose) {
		final InternetAddress address = getSenderAddress(message);
		return renderAddress(address, verbose);
	}

	public static String getRecipient(Message message, boolean verbose) {
		try {
			Address[] allRecipients = message.getRecipients(RecipientType.TO);
			if(allRecipients != null && allRecipients[0] != null) {
				return renderAddress((InternetAddress)allRecipients[0], verbose);
			}
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		return "";
		
		
	}
	
	private static String renderAddress(InternetAddress address, boolean verbose) {
		if(address == null || (address.getPersonal() == null && address.getAddress() == null)) {
			return "(none)";
		}
		
		if(address.getPersonal() != null) {
			if(verbose && address.getAddress() != null) {
				return address.getPersonal() + " " + address.getAddress();
			} else {
				return address.getPersonal();
			}
		} 
		return address.getAddress();
	}

	public static InternetAddress getSenderAddress(Message message) {
		try {
			final Address[] as = message.getFrom();
			if(as == null || as.length == 0) {
				return null;
			}
			if(as.length > 1) {
				logger.warning("Message has "+ as.length + " senders");
			}
			if(!(as[0] instanceof InternetAddress)) {
				logger.warning("Message sender is not InternetAddress as expected");
				return null;
			}
			return (InternetAddress) as[0];
		} catch (MessagingException e) {
			logger.warning("Error reading sender for message "+ e);
			return null;
		}
		
	}

	public static String trimToMaxLength(String str, int maxLength) {
		if(str.length() <= maxLength) {
			return str;
		} else {
			return str.substring(0, maxLength - 3) + "...";
		}
	}
	
	/*
	public static String trimToMaxWidth(GC gc, String str, int maxWidth) {
		if(maxWidth < 0) {
			return "";
		}
		if(getWidth(gc, str) < maxWidth) {
			return str;
		}
		for(int i = 1; i < str.length(); i++) {
			if(getWidth(gc, str.substring(0, i) + "...") > maxWidth) {
				return str.substring(0, i - 1) + "...";
			}
		}
		return str;
	}
	
	private static int getWidth(GC gc, String s) {
		return gc.textExtent(s).x;
	}
	*/
	
	public static String readContent(Part message) {
		final Reader r = openPartReader(message);
		if(r == null) {
			return "";
		}
		try {
			return CharStreams.toString(r);
		} catch (IOException e) {
			logger.warning("Error reading part content "+ e);
			return "";
		} finally {
			closeQuietly(r);
		}
	}
	
	private static Reader openPartReader(Part part) {
		try {
			return new InputStreamReader(part.getInputStream(), Charsets.UTF_8);
		} catch (IOException | MessagingException e) {
			logger.warning("Exception opening part "+ e);
			return null;
		}
	}
	
	private static void closeQuietly(Closeable c) {
		try {
			c.close();
		} catch (IOException e) { }
	}

}
