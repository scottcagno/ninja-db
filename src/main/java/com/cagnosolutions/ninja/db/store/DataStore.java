package com.cagnosolutions.ninja.db.store;

import com.cagnosolutions.ninja.db.document.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class DataStore implements DatabaseStore {

	private static final ConcurrentMap<UUID,Document> documents = new ConcurrentHashMap<>(16, 0.80f, 1);

	private boolean isValid(Document document) {
		return (document != null);
	}

	private boolean haveMatchingIds(UUID documentId1, UUID documentId2) {
		return (documentId1.equals(documentId2));
	}

	public void insertDocument(Document document) {
		if(isValid(document))
			documents.putIfAbsent(document.get_id(), document);
	}

	public Document updateDocument(UUID documentId, Document document) {
		if(isValid(document))
			if(haveMatchingIds(documentId, document.get_id()))
				if(documents.containsKey(document.get_id()))
					documents.put(documentId, document);
		return document;
	}

	public void deleteDocument(UUID documentId) {
		documents.remove(documentId);
	}

	public Document returnDocument(UUID documentId) {
		return documents.get(documentId);
	}

	public List<Document> returnDocuments(UUID ...documentIds) {
		List<Document> matchingDocuments = new ArrayList<>(documentIds.length);
		for(UUID documentId : documentIds) {
			Document document = documents.get(documentId);
			if (document != null)
				matchingDocuments.add(document);
		}
		return matchingDocuments;
	}
}