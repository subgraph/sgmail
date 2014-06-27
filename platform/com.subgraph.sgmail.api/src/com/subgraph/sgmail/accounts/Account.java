package com.subgraph.sgmail.accounts;

import ca.odell.glazedlists.EventList;

import com.subgraph.sgmail.database.Preferences;
import com.subgraph.sgmail.messages.StoredFolder;
import com.subgraph.sgmail.messages.StoredMessage;
import com.subgraph.sgmail.messages.StoredMessageLabel;

import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.List;

public interface Account {
    String getLabel();
//    void setIdentity(PrivateIdentity identity);
//    PrivateIdentity getIdentity();
    List<StoredMessageLabel> getMessageLabels();
    StoredMessageLabel getMessageLabelByName(String name);

    List<StoredFolder> getFolders();
    StoredFolder getFolderByName(String name);

    Preferences getPreferences();
    void addMessages(Collection<StoredMessage> messages);
    void addMessage(StoredMessage message);
    void removeDeletedMessages();
    void removeMessage(StoredMessage message);
    void removeMessages(Collection<StoredMessage> messages);
    EventList<StoredMessage> getMessageEventList();
    StoredMessage getMessageById(int messageId);

    void addPropertyChangeListener(PropertyChangeListener listener);
    void removePropertyChangeListener(PropertyChangeListener listener);
}
