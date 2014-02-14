package com.subgraph.sgmail.identity.server;

import org.bouncycastle.openpgp.PGPPublicKeyRing;

import java.util.Date;

public class KeyRegistrationState {

    enum State {STATE_REQUEST_RECEIVED, STATE_MAIL_SENT };

    private State state;
    private final String emailAddress;
    private final PGPPublicKeyRing publicKeyRing;
    private final long requestId;
    private final long mailId;
    private final Date requestReceived;

    public KeyRegistrationState(String emailAddress, PGPPublicKeyRing publicKeyRing, long requestId, long mailId, Date requestRecieved) {
        this.emailAddress = emailAddress;
        this.publicKeyRing = publicKeyRing;
        this.requestId = requestId;
        this.mailId = mailId;
        this.requestReceived = requestRecieved;
        this.state = State.STATE_REQUEST_RECEIVED;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public PGPPublicKeyRing getPublicKeyRing() {
        return publicKeyRing;
    }

    public long getRequestId() {
        return requestId;
    }

    public long getMailId() {
        return mailId;
    }

    public State getCurrentState() {
        return state;
    }

    public void setCurrentState(State newState) {
        this.state = newState;
    }
}
