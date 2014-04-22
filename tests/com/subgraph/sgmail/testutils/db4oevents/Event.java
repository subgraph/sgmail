package com.subgraph.sgmail.testutils.db4oevents;

public interface Event {
    public enum EventType {
        ACTIVATE, CREATE, UPDATE, DELETE, DEACTIVATE, INSTANTIATE, COMMIT;
    }
    EventType getEventType();
}
