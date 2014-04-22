package com.subgraph.sgmail.internal.directory;


import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import com.subgraph.sgmail.directory.protocol.Message;
import com.subgraph.sgmail.directory.protocol.MessageReader;
import com.subgraph.sgmail.directory.protocol.MessageWriter;

public class ConnectionTask implements Runnable {
	private final static Logger logger = Logger.getLogger(ConnectionTask.class.getName());

    private final Server server;
	private final MessageReader reader;
	private final MessageWriter writer;

	ConnectionTask(Server server, InputStream input, OutputStream output) {
        this.server = server;
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
			} else {
                server.getMessageDispatcher().handleMessage(m, this);
            }
        }
	}

    public void writeMessage(Message message) {
        try {
            writer.writeMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
