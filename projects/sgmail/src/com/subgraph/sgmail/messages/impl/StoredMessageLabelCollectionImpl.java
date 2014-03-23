package com.subgraph.sgmail.messages.impl;

import com.db4o.activation.ActivationPurpose;
import com.db4o.collections.ActivatableArrayList;
import com.google.common.collect.ImmutableList;
import com.subgraph.sgmail.accounts.Account;
import com.subgraph.sgmail.messages.StoredMessageLabel;
import com.subgraph.sgmail.messages.StoredMessageLabelCollection;
import com.subgraph.sgmail.model.AbstractActivatable;

import java.util.List;

public class StoredMessageLabelCollectionImpl extends AbstractActivatable implements StoredMessageLabelCollection {

    private final Account account;
    private List<StoredMessageLabel> labels = new ActivatableArrayList<>();

    public StoredMessageLabelCollectionImpl(Account account) {
        this.account = account;
    }

    @Override
    public synchronized List<StoredMessageLabel> getLabels() {
        activate(ActivationPurpose.READ);
        return ImmutableList.copyOf(labels);
    }

    @Override
    public synchronized StoredMessageLabel getLabelByName(String name) {
        activate(ActivationPurpose.READ);
        for(StoredMessageLabel l: labels) {
            if(l.getName().equals(name)) {
                return l;
            }
        }
        return null;
    }

    public synchronized StoredMessageLabel createNewLabel(String name) {
        activate(ActivationPurpose.READ);
        final StoredMessageLabel label = new StoredMessageLabelImpl(account, name);
        model.store(label);
        labels.add(label);
        model.commit();
        return label;
    }
}
