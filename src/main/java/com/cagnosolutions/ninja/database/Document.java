package com.cagnosolutions.ninja.database;

import com.cagnosolutions.ninja.database.util.Type1UUID;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class Document implements Serializable, Comparable<Document> {

	private static final long serialVersionUID = -11235813213455892L;

	private UUID id;
	private UUID modified;
	private Map<String, Object> data;

	public Document() {
		UUID time = Type1UUID.getTimeUUID();
		this.id = time;
		this.modified = time;
		this.data = new HashMap<>();
	}

	public Document(Map<String, Object> data) {
		UUID time = Type1UUID.getTimeUUID();
		this.id = time;
		this.modified = time;
		this.data = data;
	}

	public UUID getId() {
		return id;
	}

	public void updateModified() {
		this.modified = Type1UUID.getTimeUUID();
	}

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		updateModified();
		this.data = data;
	}

	/**
	 * Comparable<T> implementation
	 */

	public int compareTo(Document that) {
		return this.id.compareTo(that.getId());
	}


	/**
	 * Serialization overrides, and validators
	 */

	private void writeObject(ObjectOutputStream stream) throws IOException {
		stream.writeObject(id);
		stream.writeObject(modified);
		stream.writeObject(data);
	}

	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		id = (UUID) stream.readObject();
		modified = (UUID) stream.readObject();
		data = (Map<String, Object>) stream.readObject();
		validate();
	}

	private void validate() {
		if(id == null || modified == null || data == null)
			throw new IllegalArgumentException("ninja-db.document");
	}

	/**
	 * Custom toString() method
	 */

	public String toString() {
		return "Document{" +
				"id=" + id +
				", modified=" + modified +
				", data=" + data +
				'}';
	}
}