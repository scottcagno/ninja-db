package com.cagnosolutions.ninja.db;

import java.util.List;
import java.util.UUID;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public interface DatabaseEngineStore {
		public void insertDocument(Document document);
		public Document updateDocument(UUID documentId, Document document);
		public void deleteDocument(UUID documentId);
		public Document returnDocument(UUID documentId);
		public List<Document> returnDocuments(UUID ...documentIds);
}
