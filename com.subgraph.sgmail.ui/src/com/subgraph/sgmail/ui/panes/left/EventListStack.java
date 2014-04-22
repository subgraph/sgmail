package com.subgraph.sgmail.ui.panes.left;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GroupingList;
import ca.odell.glazedlists.calculation.Calculation;
import ca.odell.glazedlists.calculation.Calculations;
import ca.odell.glazedlists.matchers.Matcher;
import com.subgraph.sgmail.accounts.Account;
import com.subgraph.sgmail.messages.StoredFolder;
import com.subgraph.sgmail.messages.StoredMessage;
import com.subgraph.sgmail.messages.StoredMessageLabel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EventListStack {

    public static EventListStack createForAccount(Account imapAccount, SearchMatcherEditor searchMatcherEditor) {
        return create(imapAccount.getMessageEventList(), searchMatcherEditor, null);
    }

    public static EventListStack createForFolder(StoredFolder folder, SearchMatcherEditor searchMatcherEditor) {
        return create(folder.getMessageEventList(), searchMatcherEditor, null);
    }

    public static EventListStack createForLabel(StoredMessageLabel label, SearchMatcherEditor searchMatcherEditor) {
        return create(label.getAccount().getMessageEventList(), searchMatcherEditor, label);
    }

    private static EventListStack create(EventList<StoredMessage> messages, SearchMatcherEditor searchMatcherEditor, StoredMessageLabel label) {
        final EventListStack els = new EventListStack(messages);
        if(label != null) {
            els.addLabelFilter(label);
        }
        els.addSearchFilter(searchMatcherEditor);
        els.addNewMessageCounter();
        els.addGroupingList();
        return els;
    }

    private final EventList<StoredMessage> baseList;

    private List<EventList<StoredMessage>> stack = new ArrayList<>();
    private GroupingList<StoredMessage> groupingList;
    private Calculation<Integer> newMessageCounter;
    private Calculation<Integer> searchMatchCounter;

    public EventListStack(EventList<StoredMessage> baseList) {
        this.baseList = baseList;
    }

    public void dispose() {
        baseList.getReadWriteLock().writeLock().lock();
        try {
            if (groupingList != null) {
                groupingList.dispose();
            }
            if(newMessageCounter != null) {
                newMessageCounter.dispose();
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

    public void addNewMessageCounter() {
        newMessageCounter = Calculations.count(getTopList(), m -> (m.getFlags() & StoredMessage.FLAG_SEEN) == 0);
    }

    public Calculation<Integer> getNewMessageCounter() {
        return newMessageCounter;
    }

    public GroupingList<StoredMessage> getGroupingList() {
        return groupingList;
    }

    public void addLabelFilter(StoredMessageLabel label) {
        stack.add(new FilterList<>(getTopList(), new LabelMatcher(label)));
    }

    public void addSearchFilter(SearchMatcherEditor searchMatcherEditor) {
        EventList<StoredMessage> searchFiltered = new FilterList<>(getTopList(), searchMatcherEditor);
        searchMatchCounter = Calculations.count(searchFiltered);
        stack.add(searchFiltered);
    }

    public Calculation<Integer> getSearchMatchCounter() { return searchMatchCounter; }

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
            return message.containsLabel(label);
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
