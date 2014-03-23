package com.subgraph.sgmail.messages.impl;

import com.db4o.activation.ActivationPurpose;
import com.subgraph.sgmail.messages.IMAPCommand;
import com.subgraph.sgmail.model.AbstractActivatable;

public class IMAPFlagChangedCommand extends AbstractActivatable implements IMAPCommand {
    private final String folderName;
    private final long messageUID;
    private final long flag;
    private final boolean isSet;

    IMAPFlagChangedCommand(String folderName, long messageUID, long flag, boolean isSet) {
        this.folderName = folderName;
        this.messageUID = messageUID;
        this.flag = flag;
        this.isSet = isSet;
    }

    public long getMessageUID() {
        activate(ActivationPurpose.READ);
        return messageUID;
    }

    public long getFlag() {
        activate(ActivationPurpose.READ);
        return flag;
    }

    public boolean isSet() {
        activate(ActivationPurpose.READ);
        return isSet;
    }

    @Override
    public String getFolderName() {
        activate(ActivationPurpose.READ);
        return folderName;
    }
}
