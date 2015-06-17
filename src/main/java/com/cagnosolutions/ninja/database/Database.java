package com.cagnosolutions.ninja.database;

import java.util.List;
import java.util.UUID;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class Database {

	private Engine engine;
	private DiskQueue diskQueue;

	public Database(String path) {
		this.engine = new Engine();
		this.diskQueue = new DiskQueue(path);
	}

	public Database() {
		this.engine = new Engine();
		this.diskQueue = new DiskQueue("/tmp/ninja.db");
	}

	/**
	 * Disk serialization methods
	 */

	public void save() {
		diskQueue.write(engine);
	}

	public void load() {
		this.engine.clearAll();
		this.engine = diskQueue.read();
	}

	/**
	 * Store level methods
	 */

	public void createStore(String storeId) {
		engine.createStore(storeId);
	}

	public void deleteStore(String storeId) {
		engine.deleteStore(storeId);
	}

	public int getStoreCount() {
		return engine.getStoreCount();
	}

	/**
	 * Document level methods
	 */

	public void insertDocument(String storeId, Document document) {
		engine.insertDocument(storeId, document);
	}

	public void updateDocument(String storeId, UUID documentId, Document document) {
		engine.updateDocument(storeId, documentId, document);
	}

	public void deleteDocument(String storeId, UUID documentId) {
		engine.deleteDocument(storeId, documentId);
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

}