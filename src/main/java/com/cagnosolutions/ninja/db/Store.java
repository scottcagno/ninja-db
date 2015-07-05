package com.cagnosolutions.ninja.db;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class Store implements Serializable{

	private static final long serialVersionUID = -11235813213455891L;

	private String id;
	private Map<UUID,Document> documents;

	public Store(String id) {
		this.id = id;
		this.documents = new ConcurrentHashMap<>(16, 0.80f, 4);
	}

	public String getId() {
		return id;
	}

	public Collection<Document> getDocuments() {
		return Collections.unmodifiableCollection(documents.values());
	}

	/**
	 * Document level methods
	 */

	public Document createDocument(Map<String,Object> data) {
		Document document = new Document(data);
		documents.put(document.getId(), document);
		return document;
	}

	public boolean updateDocument(UUID documentId, Map<String, Object> updatedData) {
		if(!documents.containsKey(documentId))
			return false;
		Document document = documents.get(documentId);
		document.setData(updatedData);
		return true;
	}

	public boolean deleteDocument(UUID documentId) {
		if(!documents.containsKey(documentId))
			return false;
		documents.remove(documentId);
		return true;
	}

	public Document returnDocument(UUID documentId) {
		if(!documents.containsKey(documentId))
			return null;
		return documents.get(documentId);
	}

	public List<Document> returnAllDocuments() {
		List<Document> allDocuments = new ArrayList<>(Collections.unmodifiableCollection(documents.values()));
		Collections.sort(allDocuments);
		return allDocuments;
	}

	/**
	 * Serialization overrides, and validators
	 */

	private void writeObject(ObjectOutputStream stream) throws IOException {
		stream.writeObject(id);
		stream.writeObject(documents);
	}

	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		id = (String) stream.readObject();
		documents = (Map<UUID,Document>) stream.readObject();
		validate();
	}

	private void validate() {
		if(id == null || id.length() == 0 || documents == null)
			throw new IllegalArgumentException("ninja-db.store");
	}

	/**
	 * Clear entire database
	 */

	public void clearAll() {
		this.documents.clear();
	}

	/**
	 * Document count method
	 */

	public int getDocumentCount() {
		return this.documents.size();
	}

}
