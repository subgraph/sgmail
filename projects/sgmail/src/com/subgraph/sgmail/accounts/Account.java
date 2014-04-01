package com.subgraph.sgmail.accounts;

import ca.odell.glazedlists.EventList;
import com.subgraph.sgmail.messages.StoredFolder;
import com.subgraph.sgmail.messages.StoredMessage;
import com.subgraph.sgmail.messages.StoredMessageLabel;
import com.subgraph.sgmail.model.Identity;
import com.subgraph.sgmail.model.StoredAccountPreferences;

import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.List;

public interface Account {
    String getLabel();
    void setIdentity(Identity identity);
    Identity getIdentity();
    List<StoredMessageLabel> getMessageLabels();
    StoredMessageLabel getMessageLabelByName(String name);

    List<StoredFolder> getFolders();
    StoredFolder getFolderByName(String name);

    StoredAccountPreferences getPreferences();
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
