package com.subgraph.sgmail;


public interface IEventBus {
	void register(Object object);
	void unregister(Object object);
	void post(Object event);
}
