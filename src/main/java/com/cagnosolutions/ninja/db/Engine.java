package com.cagnosolutions.ninja.db;

import com.cagnosolutions.ninja.db.document.Document;
import com.cagnosolutions.ninja.db.store.DataStore;
import com.cagnosolutions.ninja.mmap.MemoryMappedFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class Engine implements DatabaseEngine {

	private static final Engine INSTANCE = new Engine();
	private static final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
	private static final ConcurrentMap<String,DataStore> dataStores = new ConcurrentHashMap<>(16, 0.80f, 1);
	private static AtomicLong dataStoreChanges = new AtomicLong(0);
	private static final MemoryMappedFile snapshot = new MemoryMappedFile("/tmp/snapshot.db");

	protected void scheduledSnapshots() {
		service.scheduleAtFixedRate(() -> {
			System.out.println("Running scheduled snapshots...");
			if(dataStoreChanges.get() >= 5) {
				System.out.println("Store changes are greater or equal to threshold, attempting to write snapshot...");
				try {
					snapshot.write(dataStores);
					dataStoreChanges.set(0);
				} catch (IOException ex) {
					System.err.println("Error: Could not write snapshot to disk!");
					ex.printStackTrace();
				}
			} else {
				System.out.println("Store changes are less than threshold, not writing a snapshot...");
			}
		}, 0, 5, TimeUnit.SECONDS);
	}

	private Engine() {
		scheduledSnapshots();
	}

	public static Engine getInstance() {
		return INSTANCE;
	}

	private boolean storeExists(String storeId) {
		return dataStores.containsKey(storeId);
	}

	/**
	 * DatabaseEngine level actions
	 * @param storeId
	 */

	public void createStore(String storeId) {
		dataStores.putIfAbsent(storeId, new DataStore());
	}

	public void deleteStore(String storeId) {
		dataStores.remove(storeId);
	}

	public DataStore returnStore(String storeId) {
		return dataStores.computeIfPresent(storeId, (k, v) -> (v == null) ? new DataStore() : v);
	}

	/**
	 * DataStore level actions
	 * @param document
	 */

	public void insertDocument(String storeId, Document document) {
		if(storeExists(storeId))
			dataStores.get(storeId).insertDocument(document);
	}

	public Document updateDocument(String storeId, UUID documentId, Document document) {
		Document doc = null;
		if(storeExists(storeId))
			doc = dataStores.get(storeId).updateDocument(documentId, document);
		return doc;
	}

	public void deleteDocument(String storeId, UUID documentId) {
		if(storeExists(storeId))
			dataStores.get(storeId).deleteDocument(documentId);
	}

	public Document returnDocument(String storeId, UUID documentId) {
		if(storeExists(storeId))
			return dataStores.get(storeId).returnDocument(documentId);
		return null;
	}

	public List<Document> returnDocuments(String storeId, UUID... documentIds) {
		if(storeExists(storeId))
			return dataStores.get(storeId).returnDocuments(documentIds);
		return null;
	}

}
