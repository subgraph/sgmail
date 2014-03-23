package com.subgraph.sgmail.ui.panes.middle;

import ca.odell.glazedlists.gui.TableFormat;
import com.subgraph.sgmail.messages.StoredMessage;

import java.util.List;

public class TableFormatter implements TableFormat<List<StoredMessage>> {

    @Override
    public int getColumnCount() {
        return 0;
    }

    @Override
    public String getColumnName(int i) {
        System.out.println("getColumnName should not be called");
        return "";
    }

    @Override
    public Object getColumnValue(List<StoredMessage> storedMessages, int i) {
        System.out.println("getColumnValue should not be called");
        return "";
    }
}
