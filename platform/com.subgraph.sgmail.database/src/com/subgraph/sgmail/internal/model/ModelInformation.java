package com.subgraph.sgmail.internal.model;

public class ModelInformation {
	private final static int CURRENT_MODEL_VERSION = 1;
	
	private final int modelVersion = CURRENT_MODEL_VERSION;
	
	int getModelVersion() {
		return modelVersion;
	}

}
