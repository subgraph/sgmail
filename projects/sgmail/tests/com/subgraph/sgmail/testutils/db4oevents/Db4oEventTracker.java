package com.subgraph.sgmail.testutils.db4oevents;

import com.db4o.ObjectContainer;
import com.db4o.events.*;
import com.db4o.foundation.Iterator4;
import com.db4o.internal.FrozenObjectInfo;
import com.db4o.internal.LazyObjectReference;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * Utility class for constructing test cases which verify that Db4o events occur as expected when storing,
 * modifying and accessing objects stored in a db4o container.
 */
public class Db4oEventTracker {

    /** Record of registered event listeners so that they can later be removed */
    private static class ListenerCollection<T extends EventArgs> {
        private final Multimap<Event4<T>, EventListener4<T>> eventMap = ArrayListMultimap.create();

        private void removeListeners() {
            for(Event4<T> event: eventMap.keySet()) {
                for(EventListener4<T> listener: eventMap.get(event)) {
                    event.removeListener(listener);
                }
            }
            eventMap.clear();
        }

        private void addListener(Event4<T> event, EventListener4<T> listener) {
            eventMap.put(event, listener);
        }
    }


    /** List of stored events */
    private final List<Event> eventList;

    private EnumSet<Event.EventType> eventFilter;

    /** Registered listeners that can later be removed */
    private final ListenerCollection<ObjectInfoEventArgs> objectInfoListeners = new ListenerCollection<>();
    private final ListenerCollection<CommitEventArgs> commitEventListeners = new ListenerCollection<>();

    /** Has dispose() been called on this tracker yet? */
    private boolean isDisposed;

    /** Create a tracker which records all events in EventType */
    public Db4oEventTracker(ObjectContainer db) {
        this(db, EnumSet.allOf(Event.EventType.class));
    }

    public Db4oEventTracker(ObjectContainer db, EnumSet<Event.EventType> eventFilter) {
        this.eventList = new ArrayList<>();
        this.eventFilter = eventFilter;
        registerAllEvents(db);
    }

    public void assertACTIVATE(String... classes) { ExpectedEvents.assertACTIVATE(eventList, classes); clearEvents();}
    public void assertCREATE(String... classes) { ExpectedEvents.assertCREATE(eventList, classes); clearEvents();}
    public void assertDELETE(String... classes) { ExpectedEvents.assertDELETE(eventList, classes); clearEvents();}
    public void assertDEACTIVATE(String... classes) { ExpectedEvents.assertDEACTIVATE(eventList, classes); clearEvents();}
    public void assertINSTANTIATE(String... classes) { ExpectedEvents.assertINSTANTIATE(eventList, classes); clearEvents();}
    public void assertUPDATE(String... classes) { ExpectedEvents.assertUPDATE(eventList, classes); clearEvents(); }

    private void registerAllEvents(ObjectContainer db) {
        final EventRegistry events = EventRegistryFactory.forObjectContainer(db);
        registerEvent(events.activated(), Event.EventType.ACTIVATE);
        registerEvent(events.created(), Event.EventType.CREATE);
        registerEvent(events.deleted(), Event.EventType.DELETE);
        registerEvent(events.deactivated(), Event.EventType.DEACTIVATE);
        registerEvent(events.instantiated(), Event.EventType.INSTANTIATE);
        registerEvent(events.updated(), Event.EventType.UPDATE);
        registerEvent(events.committed());
    }

    public void dispose() {
        checkDisposed();
        isDisposed = true;
        objectInfoListeners.removeListeners();
        commitEventListeners.removeListeners();
    }

    public void setEventFilter(Event.EventType... filterTypes) {
        this.eventFilter = EnumSet.copyOf(Arrays.asList(filterTypes));
    }

    public void setEventFilterAllExcept(Event.EventType... excludeTypes) {
        final EnumSet<Event.EventType> excludeSet = EnumSet.copyOf(Arrays.asList(excludeTypes));
        this.eventFilter = EnumSet.complementOf(excludeSet);
    }

    public void setEventFilter(EnumSet<Event.EventType> eventFilter) {
        this.eventFilter = eventFilter;
    }

    public void clearEvents() {
        checkDisposed();
        eventList.clear();
    }

    public List<Event> getEventList() {
        checkDisposed();
        return ImmutableList.copyOf(eventList);
    }

    public void printEventList() {
        checkDisposed();
        System.out.println("Events: "+ eventList);
    }

    private void registerEvent(Event4<ObjectInfoEventArgs> ev, final Event.EventType eventType) {
        final EventListener4<ObjectInfoEventArgs> listener = new EventListener4<ObjectInfoEventArgs>() {
            @Override
            public void onEvent(Event4<ObjectInfoEventArgs> event, ObjectInfoEventArgs args) {
                if(eventFilter.contains(eventType)) {
                    eventList.add(new BasicEvent(eventType, args.object()));
                }
            }
        };
        objectInfoListeners.addListener(ev, listener);
        ev.addListener(listener);
    }

    private void registerEvent(Event4<CommitEventArgs> ev) {
        final EventListener4<CommitEventArgs> listener = new EventListener4<CommitEventArgs>() {
            @Override
            public void onEvent(Event4<CommitEventArgs> event, CommitEventArgs args) {
                if(eventFilter.contains(Event.EventType.COMMIT)) {
                    onCommit(args);
                }

            }
        };
        commitEventListeners.addListener(ev, listener);
        ev.addListener(listener);
    }

    private void onCommit(CommitEventArgs commitArgs) {
        final List<String> added = new ArrayList<>();
        final List<String> updated = new ArrayList<>();
        final List<String> deleted = new ArrayList<>();

        for(Iterator4 it=commitArgs.added().iterator();it.moveNext();) {
            LazyObjectReference reference = (LazyObjectReference) it.current();
            addName(reference.getObject(), added);
        }
        for(Iterator4 it=commitArgs.updated().iterator();it.moveNext();) {
            LazyObjectReference reference = (LazyObjectReference) it.current();
            addName(reference.getObject(), updated);
        }
        for(Iterator4 it=commitArgs.deleted().iterator();it.moveNext();) {
            FrozenObjectInfo deletedInfo = (FrozenObjectInfo) it.current();
            // the deleted info might doesn't contain the object anymore and
            // return the null.
            addName(deletedInfo.getObject(), deleted);
        }
        eventList.add(new CommitEvent(added, updated, deleted));
    }

    private void addName(Object ob, List<String> names) {
        if(ob != null) {
            names.add(ob.getClass().getSimpleName());
        }
    }

    private void checkDisposed() {
        if(isDisposed) {
            throw new IllegalStateException("Db4oEventTracker has been disposed");
        }
    }
}
