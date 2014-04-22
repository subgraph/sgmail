package com.subgraph.sgmail.directory.protocol;

import com.subgraph.sgmail.directory.protocol.Protocol.ProtocolMessage.Type;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

public class MessageWriter implements Closeable {
	
	private final OutputStream output;
	
	public MessageWriter(OutputStream output) {
		this.output = output;
	}
	
	public OutputStream getStream() {
		return output;
	}

	public void writeMessage(Message message) throws IOException {
		final byte[] bytes = serializeMessage(message);
		writeLength(bytes.length);
		output.write(bytes);
	}
	
	private void writeLength(int length) throws IOException {
		output.write(serializeLength(length));
	}
	
	private byte[] serializeLength(int length) {
		final byte[] buffer = new byte[4];
		for(int i = 3; i >= 0; i--) {
			buffer[i] = (byte) (length & 0xFF);
			length >>= 8;
		}
		return buffer;
	}

	private byte[] serializeMessage(Message message) throws IOException {
		if (message instanceof KeyLookupRequest)    { return serializeKeyLookupRequest((KeyLookupRequest) message); }   else 
		if (message instanceof KeyLookupResponse)   { return serializeKeyLookupResponse((KeyLookupResponse) message); } else
		if (message instanceof KeyPublishRequest)   { return serializeKeyPublishRequest((KeyPublishRequest) message); }   else
        if (message instanceof KeyPublishResponse)  { return serializeKeyPublishResponse((KeyPublishResponse) message); } else
        if (message instanceof KeyRegistrationRequest)  { return serializeKeyRegistrationRequest((KeyRegistrationRequest) message); } else
        if (message instanceof KeyRegistrationResponse)  { return serializeKeyRegistrationResponse((KeyRegistrationResponse) message); } else
        if (message instanceof KeyRegistrationFinalizeRequest)  { return serializeKeyRegistrationFinalizeRequest((KeyRegistrationFinalizeRequest) message); } else
        if (message instanceof KeyRegistrationFinalizeResponse)  { return serializeKeyRegistrationFinalizeResponse((KeyRegistrationFinalizeResponse) message); }

		throw new IllegalArgumentException("Unknown message type "+ message);
	}
	
	private byte[] serializeKeyLookupRequest(KeyLookupRequest msg) {
		return Protocol.ProtocolMessage.newBuilder()
				.setType(Type.KEY_LOOKUP_REQUEST)
				.setLookupRequest(msg.toProtocolMessage())
				.build().toByteArray();
	}
	
	private byte[] serializeKeyLookupResponse(KeyLookupResponse msg) {
		return Protocol.ProtocolMessage.newBuilder()
				.setType(Type.KEY_LOOKUP_RESPONSE)
				.setLookupResponse(msg.toProtocolMessage())
				.build().toByteArray();
	}
	
	private byte[] serializeKeyPublishRequest(KeyPublishRequest msg) {
		return Protocol.ProtocolMessage.newBuilder()
				.setType(Type.KEY_PUBLISH_REQUEST)
				.setPublishRequest(msg.toProtocolMessage())
				.build().toByteArray();
	}
	
	private byte[] serializeKeyPublishResponse(KeyPublishResponse msg) {
		return Protocol.ProtocolMessage.newBuilder()
				.setType(Type.KEY_PUBLISH_RESPONSE)
				.setPublishResponse(msg.toProtocolMessage())
				.build().toByteArray();
	}

    private byte[] serializeKeyRegistrationRequest(KeyRegistrationRequest msg) {
        return Protocol.ProtocolMessage.newBuilder()
                .setType(Type.KEY_REGISTRATION_REQUEST)
                .setRegistrationRequest(msg.toProtocolMessage())
                .build().toByteArray();
    }

    private byte[] serializeKeyRegistrationResponse(KeyRegistrationResponse msg) {
        return Protocol.ProtocolMessage.newBuilder()
                .setType(Type.KEY_REGISTRATION_RESPONSE)
                .setRegistrationResponse(msg.toProtocolMessage())
                .build().toByteArray();
    }

    private byte[] serializeKeyRegistrationFinalizeRequest(KeyRegistrationFinalizeRequest msg) {
        return Protocol.ProtocolMessage.newBuilder()
                .setType(Type.KEY_REGISTRATION_FINALIZE_REQUEST)
                .setRegistrationFinalizeRequest(msg.toProtocolMessage())
                .build().toByteArray();
    }

    private byte[] serializeKeyRegistrationFinalizeResponse(KeyRegistrationFinalizeResponse msg) {
        return Protocol.ProtocolMessage.newBuilder()
                .setType(Type.KEY_REGISTRATION_FINALIZE_RESPONSE)
                .setRegistrationFinalizeResponse(msg.toProtocolMessage())
                .build().toByteArray();
    }
	
	@Override
	public void close() throws IOException {
		output.close();
	}
}
