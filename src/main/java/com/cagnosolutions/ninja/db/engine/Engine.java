package com.cagnosolutions.ninja.db.engine;

import com.cagnosolutions.ninja.db.document.Document;
import com.cagnosolutions.ninja.db.store.DataStore;
import com.cagnosolutions.ninja.mmap.MemoryMappedFile;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class Engine implements DatabaseEngine, Serializable {

	private static final long serialVersionUID = 46315782003L;

	private static final Engine INSTANCE = new Engine();

	private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

	private static final String SNAPSHOT_PATH = "/tmp/snapshot.db";

	private static final MemoryMappedFile fd = new MemoryMappedFile(SNAPSHOT_PATH);

	private static long changes = 0L;

	private static ConcurrentMap<String,DataStore> dataStores = new ConcurrentHashMap<>(16, 0.80f, 1);

	private static final Runnable snapshot = () -> {
		System.out.println("Running scheduled snapshots...");
		if (changes >= 5) {
			System.out.println("Store changes are greater or equal to threshold, attempting to write snapshot...");
			INSTANCE.saveSnapshot();
		} else {
			System.out.println("Store changes are less than threshold, not writing a snapshot...");
		}
	};

	public void saveSnapshot() {
		try {
			fd.writeDataStore(dataStores);
			changes = 0L;
		} catch (IOException ex) {
			System.err.printf("Error: Could not write snapshot file: %s\n", SNAPSHOT_PATH);
			ex.printStackTrace();
		}
	}

	public void loadSnapshot() {
		try {
			dataStores = fd.readDataStore();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException ex) {
			System.err.printf("Error: Could not read snapshot file: %s\n", SNAPSHOT_PATH);
			ex.printStackTrace();
		}
	}

	public void eraseSnapshot() {
		fd.deleteFile();
	}

	private static ScheduledFuture<?> snapshotHandler = null;

	public void enableSnapshots() {
		snapshotHandler = scheduler.scheduleAtFixedRate(snapshot, 0, 5, TimeUnit.SECONDS);
	}

	public void disableSnapshots() {
		if(!snapshotHandler.isCancelled())
			snapshotHandler.cancel(true);
	}

	private Engine() {}

	public static Engine getInstance() {
		return INSTANCE;
	}

	/**
	 * DatabaseEngine level actions
	 * @param storeId
	 */

	public void createStore(String storeId) {
		if(dataStores.containsKey(storeId))
			return; // store exists, no need to create store
		dataStores.put(storeId, new DataStore(storeId));
		changes++;
	}

	public void deleteStore(String storeId) {
		if(!dataStores.containsKey(storeId))
			return; // store does not exist, no need to remove
		dataStores.remove(storeId);
		changes++;
	}

	public DataStore returnStore(String storeId) {
		return dataStores.computeIfPresent(storeId, (k, v) -> (v == null) ? new DataStore("null") : v);
	}

	/**
	 * DataStore level actions
	 * @param document
	 */

	public void insertDocument(String storeId, Document document) {
		if(!dataStores.containsKey(storeId))
			return; // store does note exist, cannot insert document
		boolean inserted = dataStores.get(storeId).insertDocument(document);
		if(inserted)
			changes++;
	}

	public Document updateDocument(String storeId, UUID documentId, Document document) {
		Document doc = null;
		if(dataStores.containsKey(storeId))
			doc = dataStores.get(storeId).updateDocument(documentId, document);
		if(doc != null)
			changes++;
		return doc;
	}

	public void deleteDocument(String storeId, UUID documentId) {
		if(!dataStores.containsKey(storeId))
			return; // store does not exist, no need to remove document
		boolean deleted = dataStores.get(storeId).deleteDocument(documentId);
		if(deleted)
			changes++;
	}

	public Document returnDocument(String storeId, UUID documentId) {
		if(dataStores.containsKey(storeId))
			return dataStores.get(storeId).returnDocument(documentId);
		return null;
	}

	public List<Document> returnDocuments(String storeId, UUID... documentIds) {
		if(dataStores.containsKey(storeId))
			return dataStores.get(storeId).returnDocuments(documentIds);
		return null;
	}
	
	public int getStoreCount() {
		return dataStores.size();
	}
}