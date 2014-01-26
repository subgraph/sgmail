package com.subgraph.sgmail.model;

import org.eclipse.swt.graphics.Point;

import com.db4o.activation.ActivationPurpose;

public class SavedShellSize extends AbstractActivatable {
	
	private final String name;
	
	private boolean isInitialized;
	private int width;
	private int height;
	
	public SavedShellSize(String name) {
		this.name = name;
		this.isInitialized = false;
	}

	public void setSize(int width, int height) {
		activate(ActivationPurpose.WRITE);
		this.width = width;
		this.height = height;
		this.isInitialized = true;
		model.commit();
	}
	
	public boolean isInitialized() {
		activate(ActivationPurpose.READ);
		return isInitialized;
	}

	public String getName() {
		activate(ActivationPurpose.READ);
		return name;
	}

	public Point getSize() {
		activate(ActivationPurpose.READ);
		return new Point(width, height);
	}
}
