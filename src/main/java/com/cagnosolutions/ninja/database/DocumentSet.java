package com.cagnosolutions.ninja.database;

import java.util.List;
import java.util.Map;

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

	public DocumentSet cointaining(String key, Object value) {
		documents.removeIf(document -> {
			Map<String, Object> data = document.getData();
			return !data.containsKey(key) || !data.containsValue(value);
		});
		if(documents != null || documents.size() > 0);
			setDocuments(documents);
		return this;
	}
	
	public List<Document> find() {
		return documents;
	}

}
