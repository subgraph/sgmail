package com.subgraph.sgmail.internal.imap;

public interface IMAPCommandListener {
    void commandAdded(LocalIMAPFolderImpl folder, IMAPCommand command);
}
