package com.cagnosolutions.ninja.db.engine;

import com.cagnosolutions.ninja.db.store.DataStore;
import com.cagnosolutions.ninja.db.document.Document;

import java.util.List;
import java.util.UUID;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public interface DatabaseEngine {
	public void createStore(String storeId);
	public void deleteStore(String storeId);
	public DataStore returnStore(String storeId);

	public void insertDocument(String storeId, Document document);
	public Document updateDocument(String storeId, UUID documentId, Document document);
	public void deleteDocument(String storeId, UUID documentId);
	public Document returnDocument(String storeId, UUID documentId);
	public List<Document> returnDocuments(String storeId, UUID ...documentIds);
}
