package com.subgraph.sgmail.internal.autoconf;

import java.util.List;

import com.subgraph.sgmail.autoconf.AutoconfigResult;
import com.subgraph.sgmail.autoconf.ServerInformation;

public class AutoconfigResultImpl implements AutoconfigResult {
	private final List<ServerInformation> incomingServers;
	private final List<ServerInformation> outgoingServers;

	public AutoconfigResultImpl(List<ServerInformation> incomingServers, List<ServerInformation> outgoingServers) {
		this.incomingServers = incomingServers;
		this.outgoingServers = outgoingServers;
	}

	@Override
	public List<ServerInformation> getIncomingServers() {
		return incomingServers;
	}

	@Override
	public List<ServerInformation> getOutgoingServers() {
		return outgoingServers;
	}
}
