package com.subgraph.sgmail.database;

import java.io.File;
import java.util.List;

import com.db4o.query.Predicate;


public interface Database {
	boolean open(File directory);
	void close();
	<T> List<T> getAll(Class<T> clazz);
	<T> T getSingleton(Class<T> clazz);
	void store(Object ob);
	void delete(Object ob);
	void commit();
	<T> T getSingleByPredicate(Predicate<T> predicate);
	<T> List<T> getByPredicate(Predicate<T> predicate);
}
