package com.subgraph.sgmail.messages.impl;

import com.db4o.activation.ActivationPurpose;
import com.subgraph.sgmail.accounts.Account;
import com.subgraph.sgmail.messages.StoredMessageLabel;
import com.subgraph.sgmail.model.AbstractActivatable;

public class StoredMessageLabelImpl extends AbstractActivatable implements StoredMessageLabel {

    private final Account account;
    private final String name;

    public StoredMessageLabelImpl(Account account, String name) {
        this.account = account;
        this.name = name;
    }

    @Override
    public Account getAccount() {
        activate(ActivationPurpose.READ);
        return account;
    }

    @Override
    public String getName() {
        activate(ActivationPurpose.READ);
        return name;
    }
}
