package com.subgraph.sgmail.identity.server;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import com.subgraph.sgmail.identity.protocol.KeyLookupRequest;
import com.subgraph.sgmail.identity.protocol.Message;
import com.subgraph.sgmail.identity.protocol.MessageReader;
import com.subgraph.sgmail.identity.protocol.MessageWriter;

public class ConnectionTask implements Runnable {
	private final static Logger logger = Logger.getLogger(ConnectionTask.class.getName());

	private final MessageReader reader;
	private final MessageWriter writer;


	
	ConnectionTask(InputStream input, OutputStream output) {
		this.reader = new MessageReader(input);
		this.writer = new MessageWriter(output);
	}

	@Override
	public void run() {
		try {
			runConnection();
		} catch (IOException e) {
			logger.warning("IOException running connection "+ e);
		} finally {
			close(reader);
			close(writer);
		}
	}
	
	private void close(Closeable c) {
		try {
			c.close();
		} catch (IOException e) {
			logger.warning("Error closing stream "+ e);
		}
	}
	
	private void runConnection() throws IOException {
		
		while(true) {
			Message m = reader.readMessage();
			if(m == null) {
				return;
			}
			if(m instanceof KeyLookupRequest) {
				processKeyLookupRequest((KeyLookupRequest) m);
			} else {
				logger.warning("Unhandled message type: "+ m);
			}
		}
	}
	
	private void processKeyLookupRequest(KeyLookupRequest msg) {
		System.out.println("got key lookup for "+ msg.getEmailAddress());
		System.out.println("fp: "+ msg.getFingerprint());
	}
	
	

}
