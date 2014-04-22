package com.subgraph.sgmail.internal.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import com.db4o.activation.ActivationPurpose;
import com.db4o.activation.Activator;
import com.db4o.collections.ActivatableHashMap;
import com.db4o.ta.Activatable;
import com.subgraph.sgmail.database.Database;
import com.subgraph.sgmail.database.Storeable;
import com.subgraph.sgmail.database.StoredUserInterfaceState;

public class StoredUserInterfaceStateImpl implements StoredUserInterfaceState, Storeable, Activatable {
	
	private static class ShellSize {
		int width = -1;
		int height = -1;
	}
	
	private final Map<String, ShellSize> savedShellSizes = new ActivatableHashMap<>();
	
	private int[] sashWeights = new int[3];
	
	private transient Activator activator;
	private transient Database database;
	
	public int[] getSashWeights() {
		activate(ActivationPurpose.READ);
		return sashWeights;
	}
	
	public void setSashWeights(int[] weights) {
		activate(ActivationPurpose.WRITE);
		System.arraycopy(weights, 0, sashWeights, 0, 3);
		database.store(sashWeights);
	}

	public int getShellWidth(String shellName) {
		return getShellSizeByName(shellName).width;
	}
	
	public int getShellHeight(String shellName) {
		return getShellSizeByName(shellName).height;
	}
	
	public void setShellSize(String shellName, int width, int height) {
		final ShellSize sz = getShellSizeByName(shellName);
		sz.width = width;
		sz.height = height;
		database.store(sz);
	}
	
	
	private ShellSize getShellSizeByName(String name) {
		activate(ActivationPurpose.READ);
		if(!savedShellSizes.containsKey(name)) {
			final ShellSize sz = new ShellSize();
			database.store(sz);
			savedShellSizes.put(name, sz);
			return sz;
		} else {
			return savedShellSizes.get(name);
		}
	}
	
	@Override
	public void activate(ActivationPurpose activationPurpose) {
		if(activator != null) {
			activator.activate(activationPurpose);
		}
	}

	@Override
	public void bind(Activator activator) {
		if(this.activator == activator) {
			return;
		}
		if(activator != null  && this.activator != null) {
			throw new IllegalStateException("Object can only be bound one to an activator");
		}
		this.activator = activator;
	}

	@Override
	public void setDatabase(Database database) {
		this.database = checkNotNull(database);
	}
	
}
