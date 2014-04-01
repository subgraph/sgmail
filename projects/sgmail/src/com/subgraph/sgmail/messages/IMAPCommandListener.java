package com.subgraph.sgmail.messages;

import com.subgraph.sgmail.imap.LocalIMAPFolder;

public interface IMAPCommandListener {
    void commandAdded(LocalIMAPFolder folder, IMAPCommand command);
}
