package com.subgraph.sgmail.identity.server;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class Server {
	private final static Logger logger = Logger.getLogger(Server.class.getName());
	
	private final ExecutorService executor = Executors.newCachedThreadPool();
	
	private final int listeningPort;
	
	Server(int listeningPort) {
		this.listeningPort = listeningPort;
	}
	
	public void start() throws IOException {
		try(ServerSocket listeningSocket = new ServerSocket(listeningPort)) {
			while(true) {
				Socket newSocket = listeningSocket.accept();
				launchConnectionTask(newSocket);
			}
		} 
	}
	
	private void launchConnectionTask(Socket s) {
		try {
			final InputStream in = s.getInputStream();
			final OutputStream out = s.getOutputStream();
			executor.execute(new ConnectionTask(in, out));
		} catch (IOException e) {
			logger.warning("IOException getting streams from socket "+ e);
			quietlyClose(s);
		}
	}
	
	private void quietlyClose(Closeable c) {
		try {
			c.close();
		} catch (IOException e) { }
	}
	
	public static void main(String[] args) {
		final Server server = new Server(12345);
		try {
			server.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
