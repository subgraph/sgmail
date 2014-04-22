package com.subgraph.sgmail.testutils.db4oevents;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;

import java.util.List;

public class CommitEvent implements Event {
    private final Multiset<String> added;
    private final Multiset<String> updated;
    private final Multiset<String> deleted;

    CommitEvent(List<String> addedObjects, List<String> updatedObjects, List<String> deletedObjects) {
        this.added = ImmutableMultiset.copyOf(addedObjects);
        this.updated = ImmutableMultiset.copyOf(updatedObjects);
        this.deleted = ImmutableMultiset.copyOf(deletedObjects);
    }

    public EventType getEventType() {
        return EventType.COMMIT;
    }

    public Multiset<String> getAdded() {
        return added;
    }

    public Multiset<String> getUpdated() {
        return updated;
    }

    public Multiset<String> getDeleted() {
        return deleted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommitEvent that = (CommitEvent) o;

        if (!added.equals(that.added)) return false;
        if (!deleted.equals(that.deleted)) return false;
        if (!updated.equals(that.updated)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = added.hashCode();
        result = 31 * result + updated.hashCode();
        result = 31 * result + deleted.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "(COMMIT added: "+ added +
                " updated: "+ updated +
                " deleted: "+ deleted +" )";
    }
}
