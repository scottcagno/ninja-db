package com.cagnosolutions.ninja.database;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class Engine implements Serializable {

	private static final long serialVersionUID = -11235813213455890L;

	private Map<String,Store> stores;

	public Engine() {
		this.stores = new ConcurrentHashMap<>(16, 0.80f, 4);
	}

	public Collection<Store> getStores() {
		return Collections.unmodifiableCollection(stores.values());
	}

	/**
	 * Store level methods
	 */

	public void createStore(String storeId) {
		stores.putIfAbsent(storeId, new Store(storeId));
	}

	public void deleteStore(String storeId) {
		if(!stores.containsKey(storeId))
			return;
		Store store = stores.get(storeId);
		store.clearAll();
		stores.remove(storeId);
	}

	/**
	 * Document level methods
	 */

	public Document createDocument(String storeId, Map<String,Object> data) {
		if(!stores.containsKey(storeId))
			return null;
		Store store = stores.get(storeId);
		return store.createDocument(data);
	}

	public boolean updateDocument(String storeId, UUID documentId, Map<String, Object> updatedData) {
		if(!stores.containsKey(storeId))
			return false;
		Store store = stores.get(storeId);
		return store.updateDocument(documentId, updatedData);
	}

	public boolean deleteDocument(String storeId, UUID documentId) {
		if(!stores.containsKey(storeId))
			return false;
		Store store = stores.get(storeId);
		return store.deleteDocument(documentId);
	}

	public Document returnDocument(String storeId, UUID documentId) {
		if(!stores.containsKey(storeId))
			return null;
		Store store = stores.get(storeId);
		return store.returnDocument(documentId);
	}

	public List<Document> returnAllDocuments(String storeId) {
		if(!stores.containsKey(storeId))
			return null;
		Store store = stores.get(storeId);
		return store.returnAllDocuments();
	}

	/**
	 * Serialization overrides, and validators
	 */

	private void writeObject(ObjectOutputStream stream) throws IOException {
		stream.writeObject(stores);
	}

	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		stores = (Map<String,Store>) stream.readObject();
		validate();
	}

	private void validate() {
		if(stores == null)
			throw new IllegalArgumentException("ninja-db.engine");
	}

	/**
	 * Clear entire database
	 */

	public void clearAll() {
		stores.values().forEach(Store::clearAll);
		this.stores.clear();
	}

	/**
	 * Store and document count methods
	 */

	public int getStoreCount() {
		return stores.size();
	}

	public int getDocumentCount(String storeId) {
		if(!stores.containsKey(storeId))
			return 0;
		Store store = stores.get(storeId);
		return store.getDocumentCount();
	}

	public int getTotalDocumentCount() {
		if(stores.size() < 1)
			return 0;
		int totalDocuments = 0;
		for(Store store : stores.values())
			totalDocuments += store.getDocumentCount();
		return totalDocuments;
	}
}
