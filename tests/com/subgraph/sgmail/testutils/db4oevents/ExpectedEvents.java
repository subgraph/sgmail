package com.subgraph.sgmail.testutils.db4oevents;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

public class ExpectedEvents {


    public static void assertACTIVATE(List<Event> actualEvents, String... classes) { expect(Event.EventType.ACTIVATE, classes).assertExpectation(actualEvents); }
    public static void assertCREATE(List<Event> actualEvents, String... classes) { expect(Event.EventType.CREATE, classes).assertExpectation(actualEvents); }
    public static void assertDELETE(List<Event> actualEvents, String... classes) { expect(Event.EventType.DELETE, classes).assertExpectation(actualEvents); }
    public static void assertDEACTIVATE(List<Event> actualEvents, String... classes) { expect(Event.EventType.DEACTIVATE, classes).assertExpectation(actualEvents); }
    public static void assertINSTANTIATE(List<Event> actualEvents, String... classes) { expect(Event.EventType.INSTANTIATE, classes).assertExpectation(actualEvents); }
    public static void assertUPDATE(List<Event> actualEvents, String... classes) { expect(Event.EventType.UPDATE, classes).assertExpectation(actualEvents); }

    public static ExpectedEvents expect(Event.EventType eventType, String... classes) {
        return (new ExpectedEvents()).addBasicEvents(eventType, classes);
    }

    private final List<Event> expectedEvents = new ArrayList<>();

    public void assertExpectation(List<Event> eventList) {
        if(!testExpectation(eventList)) {
            throw new AssertionError("Expectation failed expected: "+ expectedEvents + " != actual: "+ eventList);
        }
    }

    public boolean testExpectation(List<Event> eventList) {
        if(expectedEvents.size() != eventList.size()) {
            return false;
        }
        for(int i = 0; i < eventList.size(); i++) {
            if(!expectedEvents.get(i).equals(eventList.get(i))) {
                return false;
            }
        }
        return true;
    }

    public ExpectedEvents activates(String ...classes) { return addBasicEvents(Event.EventType.ACTIVATE, classes); }
    public ExpectedEvents creates(String ...classes) { return addBasicEvents(Event.EventType.CREATE, classes); }
    public ExpectedEvents deletes(String ...classes) { return addBasicEvents(Event.EventType.DELETE, classes); }
    public ExpectedEvents deactivates(String ...classes) { return addBasicEvents(Event.EventType.DEACTIVATE, classes); }
    public ExpectedEvents instantiates(String ...classes) { return addBasicEvents(Event.EventType.INSTANTIATE, classes); }
    public ExpectedEvents updates(String ...classes) { return addBasicEvents(Event.EventType.UPDATE, classes); }

    public ExpectedEvents addBasicEvents(Event.EventType eventType, String... classes) {
        for(String s: classes) {
            expectedEvents.add(new BasicEvent(eventType, s));
        }
        return this;
    }

    public ExpectedEvents addCommitEvent(String[] added, String[] updated, String[] deleted) {
        expectedEvents.add(createCommitEvent(added, updated, deleted));
        return this;
    }

    private static CommitEvent createCommitEvent(String[] added, String[] updated, String[] deleted) {
        return new CommitEvent(createListFromArray(added), createListFromArray(updated), createListFromArray(deleted));
    }

    private static List<String> createListFromArray(String[] array) {
        if(array == null) {
            return ImmutableList.of();
        } else {
            return ImmutableList.copyOf(array);
        }
    }

    @Override
    public String toString() {
        return expectedEvents.toString();
    }
}
