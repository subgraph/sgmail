package com.subgraph.sgmail.ui.panes.left;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GroupingList;
import ca.odell.glazedlists.matchers.Matcher;
import com.subgraph.sgmail.messages.StoredMessage;
import com.subgraph.sgmail.messages.StoredMessageLabel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EventListStack {

    private final EventList<StoredMessage> baseList;

    private List<EventList<StoredMessage>> stack = new ArrayList<>();
    private GroupingList<StoredMessage> groupingList;

    public EventListStack(EventList<StoredMessage> baseList) {
        this.baseList = baseList;
    }

    public void dispose() {
        baseList.getReadWriteLock().writeLock().lock();
        try {
            if (groupingList != null) {
                groupingList.dispose();
            }
            while (!stack.isEmpty()) {
                int lastIndex = stack.size() - 1;
                stack.get(lastIndex).dispose();
                stack.remove(lastIndex);
            }
        } finally {
            baseList.getReadWriteLock().writeLock().unlock();
        }
    }

    private EventList<StoredMessage> getTopList() {
        if(stack.isEmpty()) {
            return baseList;
        } else {
            return stack.get(stack.size() - 1);
        }
    }

    public void addLabelFilter(StoredMessageLabel label) {
        stack.add(new FilterList<>(getTopList(), new LabelMatcher(label)));
    }

    public void addSearchFilter(SearchMatcherEditor searchMatcherEditor) {
        stack.add(new FilterList<>(getTopList(), searchMatcherEditor));
    }

    public GroupingList<StoredMessage> addGroupingList() {
        if(groupingList == null) {
            groupingList = new GroupingList<>(getTopList(), new ConversationGroupingComparator());
        }
        return groupingList;
    }

    private static class LabelMatcher implements Matcher<StoredMessage> {
        private final StoredMessageLabel label;
        LabelMatcher(StoredMessageLabel label) {
            this.label = label;
        }
        @Override
        public boolean matches(StoredMessage message) {
            return message.getLabels().contains(label);
        }
    }

    private static class ConversationGroupingComparator implements Comparator<StoredMessage> {
        @Override
        public int compare(StoredMessage m1, StoredMessage m2) {
            final long c1 = m1.getConversationId();
            final long c2 = m2.getConversationId();
            if(c1 == c2) {
                return 0;
            } else if (c1 < c2) {
                return -1;
            } else {
                return 1;
            }
        }
    }
}
