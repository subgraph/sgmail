package com.subgraph.sgmail.model;

import com.db4o.activation.ActivationPurpose;

public class UniqueIdGenerator extends AbstractActivatable {

    private int currentId;

    public synchronized int next() {
        activate(ActivationPurpose.WRITE);
        currentId += 1;
        model.commit();
        return currentId;
    }
}
