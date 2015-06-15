package com.cagnosolutions.ninja.db.store;

import com.cagnosolutions.ninja.db.document.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class DataStore implements DatabaseStore, Serializable {

	private static final long serialVersionUID = 46315782002L;

	private static ConcurrentMap<UUID,Document> documents = new ConcurrentHashMap<>(16, 0.80f, 1);

	private String storeId;

	public DataStore(String storeId) {
		this.storeId = storeId;
	}

	public boolean insertDocument(Document document) {
		UUID documentId = document.get_id();
		if(documents.containsKey(documentId))
			return false; // document exists, no need to insert
		documents.put(documentId, document);
		return true; // document did not exist, inserted
	}

	public Document updateDocument(UUID documentId, Document document) {
		if (documentId.equals(document.get_id())) {
			if (documents.containsKey(documentId)) {
				document.updateModified();
				documents.put(documentId, document);
				return document; // document successfully updated
			}
		}
		return null; // document not updated, return null so Engine can check for null
	}

	public boolean deleteDocument(UUID documentId) {
		if(!documents.containsKey(documentId))
			return false; // document does not exist, no need to remove
		documents.remove(documentId);
		return true; // document existed, and removed
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
		if(matchingDocuments.size() > 0)
			return matchingDocuments;
		return null;
	}

	public int getDocumentCount() {
		return documents.size();
	}

	public String toString() {
		String s = "DataStore{\n\tstoreId:" + storeId + ",\n\tdocuments=[\n";
		for(Document document : documents.values())
			s+= "\t\t" + document.toString() + "\n";
		s+="]}";
		return s;
	}
}