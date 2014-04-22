package com.subgraph.sgmail.testutils.db4oevents;

public class BasicEvent implements Event {

    private final EventType eventType;
    private final String simpleClassName;
    private final int objectIdentity;

    BasicEvent(EventType eventType, Object object) {
        this.eventType = eventType;
        this.simpleClassName = object.getClass().getSimpleName();
        this.objectIdentity = System.identityHashCode(object);
    }

    BasicEvent(EventType eventType, String simpleClassName) {
        this.eventType = eventType;
        this.simpleClassName = simpleClassName;
        this.objectIdentity = 0;
    }

    public EventType getEventType() {
        return eventType;
    }

    public String getSimpleClassName() {
        return simpleClassName;

    }

    public int getObjectIdentity() {
        return objectIdentity;
    }

    @Override
    public String toString() {
        return "("+ eventType + " "+ simpleClassName + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final BasicEvent that = (BasicEvent) o;

        if (eventType != that.eventType) return false;
        if (!simpleClassName.equals(that.simpleClassName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = eventType.hashCode();
        result = 31 * result + simpleClassName.hashCode();
        return result;
    }
}
