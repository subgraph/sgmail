package com.subgraph.sgmail.identity.protocol;

import com.subgraph.sgmail.identity.protocol.Protocol.ProtocolMessage;
import com.subgraph.sgmail.identity.protocol.Protocol.ProtocolMessage.Type;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class MessageReader implements Closeable {
	private final static int MAXIMUM_MESSAGE_LENGTH = 256 * 1024;
	
	private final InputStream input;
	
	public MessageReader(InputStream input) {
		this.input = input;
	}
	
	public InputStream getStream() {
		return input;
	}

	public Message readMessage() throws IOException {
		byte[] msgBytes = readMessageBytes();
		if(msgBytes == null) {
			return null;
		}
		final ProtocolMessage pm = Protocol.ProtocolMessage.parseFrom( msgBytes );
		Type type = pm.getType();
		switch(type.getNumber()) {
		case Type.KEY_LOOKUP_REQUEST_VALUE:
			return KeyLookupRequest.fromProtocolMessage(pm.getLookupRequest());
		case Type.KEY_LOOKUP_RESPONSE_VALUE:
			return KeyLookupResponse.fromProtocolMessage(pm.getLookupResponse());
		case Type.KEY_PUBLISH_REQUEST_VALUE:
			return KeyPublishRequest.fromProtocolMessage(pm.getPublishRequest());
		case Type.KEY_PUBLISH_RESPONSE_VALUE:
			return KeyPublishResponse.fromProtocolMessage(pm.getPublishResponse());
        case Type.KEY_REGISTRATION_REQUEST_VALUE:
            return KeyRegistrationRequest.fromProtocolMessage(pm.getRegistrationRequest());
        case Type.KEY_REGISTRATION_RESPONSE_VALUE:
            return KeyRegistrationResponse.fromProtocolMessage(pm.getRegistrationResponse());
        case Type.KEY_REGISTRATION_FINALIZE_REQUEST_VALUE:
            return KeyRegistrationFinalizeRequest.fromProtocolMessage(pm.getRegistrationFinalizeRequest());
        case Type.KEY_REGISTRATION_FINALIZE_RESPONSE_VALUE:
            return KeyRegistrationFinalizeResponse.fromProtocolMessage(pm.getRegistrationFinalizeResponse());
		default:
			throw new IllegalArgumentException("Unknown message type "+ type.getNumber());
		}
	}
	
	public <T extends Message> T expectMessage(Class<T> clazz) throws IOException {
		final Message message = readMessage();
		if(message == null) {
			throw new IOException("Input stream closed while expecting message of type "+ clazz.getName());
		}
		if(clazz.isInstance(message)) {
			return clazz.cast(message);
		}
		throw new IOException("Message not expected type.  Expecting "+ clazz.getName() + " and got "+ message.getClass().getName());
	}

	@Override
	public void close() throws IOException {
		input.close();
	}
	
	private byte[] readMessageBytes() throws IOException {
		final byte[] lengthBytes = readBytes(4);

		if(lengthBytes == null) {
			return null;
		}
		final long length = unpackLengthBytes(lengthBytes);
		if(length > MAXIMUM_MESSAGE_LENGTH) {
			throw new IOException("Message recieved which exceeds maximum length. length = "+ length);
		}
		return readBytes((int) length);
	}

	private long unpackLengthBytes(byte[] lengthBytes) {
		long length = 0;
		for(byte b: lengthBytes) {
			length <<= 8;
			int octet = (b & 0xFF);
			length |= octet;
		}
		return length;
	}

	private byte[] readBytes(int count) throws IOException {
		final byte[] buffer = new byte[count];
		int remaining = count;
		int off = 0;
		while(remaining > 0) {
			int n = input.read(buffer, off, remaining);
			if(n == -1) {
				return null;
			}
			off += n;
			remaining -= n;
		}
		return buffer;
	}
}
