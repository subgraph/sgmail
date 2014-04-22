package com.subgraph.sgmail.internal.imap;

public interface IMAPCommandQueue {
    int getPendingCommandCount();
    IMAPCommand peekNextCommand();
    void removeCommand(IMAPCommand command);
    void queueCommand(IMAPCommand command);
    void addCommandListener(IMAPCommandListener listener);
    void removeCommandListener(IMAPCommandListener listener);
}
