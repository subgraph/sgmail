package com.subgraph.sgmail.internal.autoconf;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

public class MozillaAutoconfRetriever implements AutoconfRetriever {
    private final static Logger logger = Logger.getLogger(MozillaAutoconfRetriever.class.getName());

    private final static String MOZILLA_AUTOCONF_URL = "https://live.mozillamessaging.com/autoconfig/v1.1/";

    @Override
    public InputStream lookupDomain(String domain) {
        try {
            final URL url = getTargetURL(domain);
            return url.openStream();
        } catch (MalformedURLException e) {
            logger.warning("Error building URL for domain "+ domain);
            return null;
        } catch(FileNotFoundException e) {
            return null;

        } catch (IOException e) {
            logger.warning("IO exception accessing mozilla autoconfiguration service: "+ e.getMessage());
            return null;
        }
    }

    private URL getTargetURL(String domain) throws MalformedURLException {
        return new URL(MOZILLA_AUTOCONF_URL + domain);
    }
}
