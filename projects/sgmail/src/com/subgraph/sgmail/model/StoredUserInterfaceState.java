package com.subgraph.sgmail.model;

import java.util.Map;

import org.eclipse.swt.graphics.Point;

import com.db4o.activation.ActivationPurpose;
import com.db4o.collections.ActivatableHashMap;

public class StoredUserInterfaceState extends AbstractActivatable {
	
	private static class ShellSize {
		int width = -1;
		int height = -1;
	}
	
	private final Map<String, ShellSize> savedShellSizes = new ActivatableHashMap<>();
	
	private int[] sashWeights = new int[3];
	
	
	public int[] getSashWeights() {
		activate(ActivationPurpose.READ);
		return sashWeights;
	}
	
	public void setSashWeights(int[] weights) {
		activate(ActivationPurpose.WRITE);
		System.arraycopy(weights, 0, sashWeights, 0, 3);
		model.store(sashWeights);
	}

	public Point getShellSize(String shellName) {
		final ShellSize sz = getShellSizeByName(shellName);
		return new Point(sz.width, sz.height);
	}
	
	public void setShellSize(String shellName, int width, int height) {
		final ShellSize sz = getShellSizeByName(shellName);
		sz.width = width;
		sz.height = height;
		model.store(sz);
	}
	
	
	private ShellSize getShellSizeByName(String name) {
		activate(ActivationPurpose.READ);
		if(!savedShellSizes.containsKey(name)) {
			final ShellSize sz = new ShellSize();
			model.store(sz);
			savedShellSizes.put(name, sz);
			return sz;
		} else {
			return savedShellSizes.get(name);
		}
	}
	
}
