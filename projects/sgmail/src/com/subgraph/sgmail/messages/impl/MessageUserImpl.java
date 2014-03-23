package com.subgraph.sgmail.messages.impl;

import com.db4o.activation.ActivationPurpose;
import com.subgraph.sgmail.messages.MessageUser;
import com.subgraph.sgmail.model.AbstractActivatable;

import static com.google.common.base.Preconditions.checkNotNull;

public class MessageUserImpl extends AbstractActivatable implements MessageUser {

    private final String username;
    private final String address;
    private final UserType usertype;

    public MessageUserImpl(String username, String address, UserType usertype) {
        this.username = username;
        this.address = checkNotNull(address);
        this.usertype = checkNotNull(usertype);
    }

    @Override
    public String getUsername() {
        activate(ActivationPurpose.READ);
        return username;
    }

    @Override
    public String getAddress() {
        activate(ActivationPurpose.READ);
        return address;
    }

    @Override
    public UserType getType() {
        activate(ActivationPurpose.READ);
        return usertype;
    }
}
