package com.subgraph.sgmail.servers;

import java.io.InputStream;

public interface AutoconfRetriever {
    InputStream lookupDomain(String domain);
}
