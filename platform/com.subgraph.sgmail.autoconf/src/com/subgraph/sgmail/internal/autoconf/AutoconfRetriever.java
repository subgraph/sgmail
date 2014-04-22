package com.subgraph.sgmail.internal.autoconf;

import java.io.InputStream;

public interface AutoconfRetriever {
    InputStream lookupDomain(String domain);
}
