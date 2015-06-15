package com.cagnosolutions.ninja.db.store;

import com.cagnosolutions.ninja.db.document.Document;

import java.util.List;
import java.util.UUID;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public interface DatabaseStore {
	public boolean insertDocument(Document document);
	public Document updateDocument(UUID documentId, Document document);
	public boolean deleteDocument(UUID documentId);
	public Document returnDocument(UUID documentId);
	public List<Document> returnDocuments(UUID ...documentIds);
}
