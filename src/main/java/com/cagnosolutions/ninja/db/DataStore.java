package com.cagnosolutions.ninja.db;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class DataStore implements DatabaseEngineStore {

	private static final ConcurrentMap<UUID,Document> documents = new ConcurrentHashMap<>(16, 0.80f, 1);
	private boolean hasChanged = false;

	public int count() {
		return documents.size();
	}
	
	public boolean hasChanged() {
		return false;
	}

	public void insertDocument(Document document) {
		if(document.get_id() == null)
			return;
		documents.putIfAbsent(document.get_id(), document);
		hasChanged = true;
	}

	public Document updateDocument(UUID documentId, Document document) {
		return null;
	}

	public void deleteDocument(UUID documentId) {

	}

	public Document returnDocument(UUID documentId) {
		return null;
	}

	public List<Document> returnDocuments(UUID ...documentIds) {
		return null;
	}
}
