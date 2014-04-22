package com.subgraph.sgmail.internal.database;

import java.util.List;

import org.osgi.framework.Bundle;
import com.db4o.reflect.jdk.JdkLoader;

public class OsgiLoader implements JdkLoader {
	private final List<Bundle> bundles;
	
	OsgiLoader(List<Bundle> bundles) {
		this.bundles = bundles;
	}
	
	@Override
	public Object deepClone(Object ob) {
		return new OsgiLoader(bundles);
	}

	@Override
	public Class<?> loadClass(String name) {
		for(Bundle b: bundles) {
			try {
				return b.loadClass(name);
			} catch (ClassNotFoundException e) {
			}
		}
		try {
			return getClass().getClassLoader().loadClass(name);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
}
