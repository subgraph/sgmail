package com.subgraph.sgmail.servers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MozillaAutoconfiguration {
	private final static Logger logger = Logger.getLogger(MozillaAutoconfiguration.class.getName());
	private final static String MOZILLA_AUTOCONF_URL = "https://live.mozillamessaging.com/autoconfig/v1.1/";

	private final String domain;
	private final List<ServerInformation> incomingServers;
	private final List<ServerInformation> outgoingServers;

	public MozillaAutoconfiguration(String domain) {
		this.domain = domain;
		this.incomingServers = new ArrayList<>();
		this.outgoingServers = new ArrayList<>();
	}

	public boolean performLookup() {
		final Document document = retrieveDocument();
		if(document == null) {
			return false;
		} else {
			return processDocument(document);
		}
	}

	public List<ServerInformation> getIncomingServers() {
		return incomingServers;
	}

	public List<ServerInformation> getOutgoingServers() {
		return outgoingServers;
	}

	private Document retrieveDocument() {
		final InputStream input = openInputStream();
		if(input == null) {
			return null;
		}
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			final DocumentBuilder builder = factory.newDocumentBuilder();
			return builder.parse(input);
		} catch (ParserConfigurationException e) {
			logger.warning("Error configuring XML parser "+ e);
		} catch (SAXException e) {
			logger.warning("Error parsing response document "+ e);
		} catch (IOException e) {
			logger.warning("IOException reading response "+ e);
		} finally {
			try {
				input.close(); 
			} catch (IOException e) {}
		}
		return null;
	}
	
	private InputStream openInputStream() {
		final URL url = getTargetUrl();
		if(url != null) {
			try {
				return url.openStream();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	private URL getTargetUrl() {
		try {
			return new URL(MOZILLA_AUTOCONF_URL + domain);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	private boolean processDocument(Document document) {
		incomingServers.addAll(processServers(document, "incomingServer"));
		outgoingServers.addAll(processServers(document, "outgoingServer"));
		return true;
	}
	
	
	private List<ServerInformation> processServers(Document document, String tag) {
		final NodeList nodes = document.getElementsByTagName(tag);
		final List<ServerInformation> result = new ArrayList<>();
		for(int i = 0; i < nodes.getLength(); i++) {
			Node n = nodes.item(i);
			if(n.getNodeType() == Node.ELEMENT_NODE) {
				result.add(processServerElement((Element) n));
			}
		}
		return result;
	}

	private ServerInformation processServerElement(Element element) {
		final String type = element.getAttribute("type");
		final String host = getSubElementText(element, "hostname");
		final String port = getSubElementText(element, "port");
		final String socketType = getSubElementText(element, "socketType");
		final String authType = getSubElementText(element, "authentication");
		final String userType = getSubElementText(element, "username");
		final int portVal = Integer.parseInt(port);
		
		return new ServerInformation.Builder()
		.hostname(host)
		.port(portVal)
		.protocol(type)
		.socketType(socketType)
		.authenticationType(authType)
		.usernameType(userType).build();
	}
	
	private String getSubElementText(Element e, String tag) {
		final NodeList nodes = e.getElementsByTagName(tag);
		if(nodes.getLength() != 1) {
			return null;
		}
		return nodes.item(0).getTextContent();
	}
}
