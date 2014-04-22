package com.subgraph.sgmail.autoconf;

import java.util.List;

public interface AutoconfigResult {
	List<ServerInformation> getIncomingServers();
	List<ServerInformation> getOutgoingServers();
}
