package com.subgraph.sgmail.identity.client;

import com.google.common.net.InetAddresses;
import com.subgraph.sgmail.identity.protocol.Message;
import com.subgraph.sgmail.identity.protocol.MessageReader;
import com.subgraph.sgmail.identity.protocol.MessageWriter;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class IdentityServerConnection {
    private final String serverAddress;

    private Socket socket;
    private MessageReader messageReader;
    private MessageWriter messageWriter;

    private boolean isConnected;
    private boolean isClosed;

    IdentityServerConnection(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public synchronized void connect() throws IOException {
        if(isConnected) {
            throw new IllegalStateException("Already connected");
        }
        if(isClosed) {
            throw new IllegalStateException("Closed");
        }

        socket = openSocket();
        messageReader = new MessageReader(socket.getInputStream());
        messageWriter = new MessageWriter(socket.getOutputStream());
        isConnected = true;
    }

    public boolean isConnected() {
        if(!isConnected) {
            return false;
        } else {
            return socket.isConnected();
        }
    }
    public synchronized void close() {
        checkConnected();
        quietlyClose(messageReader);
        quietlyClose(messageWriter);
        isClosed = true;
        isConnected = false;
    }

    public <T extends Message> T transact(Message message, Class<T> expect) throws IOException {
        checkConnected();
        writeMessage(message);
        return expectMessage(expect);
    }

    public Message readMessage() throws IOException {
        checkConnected();
        return messageReader.readMessage();
    }

    public <T extends Message> T expectMessage(Class<T> clazz) throws IOException {
        checkConnected();
        return messageReader.expectMessage(clazz);
    }

    public void writeMessage(Message message) throws IOException {
        checkConnected();
        messageWriter.writeMessage(message);
    }

    private synchronized  void checkConnected() {
        if(!isConnected) {
            throw new IllegalStateException("Not connected");
        }
    }
    private void quietlyClose(Closeable c) {
        try {
            c.close();
        } catch (IOException e) { }
    }

    private Socket openSocket() throws IOException {
        final Socket s = new Socket();
        s.connect(getSocketAddress());
        return s;
    }

    private SocketAddress getSocketAddress() {
        final String[] parts = serverAddress.split(":");
        if(parts.length != 2) {
            throw new IllegalArgumentException("Server address is incorrectly formed: "+ serverAddress);
        }
        try {
            final int port = Integer.parseInt(parts[1]);
            if(InetAddresses.isInetAddress(parts[0])) {
                return new InetSocketAddress(InetAddresses.forString(parts[0]), port);
            } else {
                return InetSocketAddress.createUnresolved(parts[0], port);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Could not parse port section of server address: "+ serverAddress);
        }
    }

}
