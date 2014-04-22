package com.subgraph.sgmail.messages;

import java.util.List;

public interface StoredMessageLabelCollection {
    List<StoredMessageLabel> getLabels();
    StoredMessageLabel getLabelByName(String name);
    StoredMessageLabel createNewLabel(String name);
}
