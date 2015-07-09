package com.cagnosolutions.ninja.db;

import com.google.gson.Gson;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class Database {

	private static Engine engine;
	private static DiskQueue diskQueue;
	private static ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
	private static final Database INSTANCE = new Database();
	private static final String SNAPSHOT_PATH = "/tmp/ninja.db";

	private Database() {
		engine = new Engine();
		diskQueue = new DiskQueue(SNAPSHOT_PATH);
		service.scheduleAtFixedRate(snapshot, 0, 15, TimeUnit.MINUTES);
		if(Files.exists(new File(SNAPSHOT_PATH).toPath()))
			engine = diskQueue.read();
	}

	Runnable snapshot = () -> {
		INSTANCE.writeSnapshot();
	};

	private void writeSnapshot() {
		diskQueue.write(engine);
	}
	
	public static Database getInstance() {
		return INSTANCE;
	}

	/**
	 * Disk serialization methods
	 */

	public void save() {
		diskQueue.write(engine);
	}

	public void load() {
		engine.clearAll();
		engine = diskQueue.read();
	}

	/**
	 * Export JSON method
	 */

	public void export() {
		for(Store store : engine.getStores())
			diskQueue.export(String.format("/tmp/ninja-%s.store",
					store.getId()),
						new Gson().toJson(store.getDocuments()));
	}

	/**
	 * Store level methods
	 */

	public boolean createStore(String storeId) {
		return engine.createStore(storeId);
	}

	public boolean deleteStore(String storeId) {
		return engine.deleteStore(storeId);
	}

	public int getStoreCount() {
		return engine.getStoreCount();
	}

	/**
	 * Document level methods
	 */

	public Document createDocument(String storeId, Map<String, Object> data) {
		return engine.createDocument(storeId, data);
	}

	public boolean updateDocument(String storeId, UUID documentId, Map<String, Object> updatedData) {
		return engine.updateDocument(storeId, documentId, updatedData);
	}

	public boolean deleteDocument(String storeId, UUID documentId) {
		return engine.deleteDocument(storeId, documentId);
	}

	public Document returnDocument(String storeId, UUID documentId) {
		return engine.returnDocument(storeId, documentId);
	}

	public List<Document> returnAllDocuments(String storeId) {
		return engine.returnAllDocuments(storeId);
	}

	public int getDocumentCount(String storeId) {
		return engine.getDocumentCount(storeId);
	}

	public int getTotalDocumentCount() {
		return engine.getTotalDocumentCount();
	}

	/**
	 * Search & query methods
	 */

	public DocumentSet findAllIn(String storeId) {
		return engine.findAllIn(storeId);
	}
}