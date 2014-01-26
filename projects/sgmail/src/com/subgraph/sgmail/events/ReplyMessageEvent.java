package com.subgraph.sgmail.events;

public class ReplyMessageEvent {
	
	private final boolean isReplyAll;
	
	public ReplyMessageEvent(boolean isReplyAll) {
		this.isReplyAll = isReplyAll;
	}
	
	public boolean isReplyAll() {
		return isReplyAll;
	}

}
