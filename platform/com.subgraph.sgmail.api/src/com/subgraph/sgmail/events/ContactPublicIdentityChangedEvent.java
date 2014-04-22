package com.subgraph.sgmail.events;


import com.subgraph.sgmail.identity.Contact;
import com.subgraph.sgmail.identity.PublicIdentity;

public class ContactPublicIdentityChangedEvent {
    private final Contact contact;
    private final PublicIdentity oldIdentity;

    public ContactPublicIdentityChangedEvent(Contact contact, PublicIdentity oldIdentity) {
        this.contact = contact;
        this.oldIdentity = oldIdentity;
    }

    public Contact getContact() {
        return contact;
    }

    public PublicIdentity getOldIdentity() {
        return oldIdentity;
    }
}
