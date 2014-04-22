package com.subgraph.sgmail.imap;

import com.db4o.activation.ActivationPurpose;
import com.db4o.collections.ActivatableArrayList;
import com.google.common.collect.ImmutableList;
import com.subgraph.sgmail.model.AbstractActivatable;

import java.util.List;

public class IMAPAccountList extends AbstractActivatable {

    private final List<IMAPAccount> accountList = new ActivatableArrayList<>();

    public void addAccount(IMAPAccount account) {
        activate(ActivationPurpose.READ);
        model.store(account);
        accountList.add(account);
        model.commit();
    }

    public List<IMAPAccount> getAccounts() {
        activate(ActivationPurpose.READ);
        return ImmutableList.copyOf(accountList);
    }
}
