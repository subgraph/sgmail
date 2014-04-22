package com.subgraph.sgmail.internal;

import com.google.common.eventbus.EventBus;
import com.subgraph.sgmail.IEventBus;

public class EventBusService implements IEventBus {
	private final EventBus eventBus = new EventBus();

	@Override
	public void register(Object object) {
		eventBus.register(object);
	}

	@Override
	public void unregister(Object object) {
		eventBus.unregister(object);
	}

	@Override
	public void post(Object event) {
		eventBus.post(event);
	}
}
