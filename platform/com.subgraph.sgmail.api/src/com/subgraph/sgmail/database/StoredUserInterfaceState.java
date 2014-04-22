package com.subgraph.sgmail.database;

public interface StoredUserInterfaceState {
	int[] getSashWeights();
	void setSashWeights(int[] weights);
	int getShellWidth(String shellName);
	int getShellHeight(String shellName);
	void setShellSize(String shellName, int width, int height);
}
