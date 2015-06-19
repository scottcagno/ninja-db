package com.cagnosolutions.ninja.database;

import java.util.List;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class DocumentSet {

	private List<Document> documents;
	
	public DocumentSet(final List<Document> documents) {
		this.documents = documents;
	}

	public void setDocuments(List<Document> documents) {
		this.documents = documents;
	}

	public DocumentSet containing(String key, String value) {
		documents.removeIf(document -> !document.hasDataContaining(key, value));
		if(documents != null || documents.size() > 0);
			setDocuments(documents);
		return this;
	}
	
	public List<Document> find() {
		return documents;
	}

}
