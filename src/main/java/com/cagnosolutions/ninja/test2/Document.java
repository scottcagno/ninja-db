package com.cagnosolutions.ninja.test2;

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

public class Document implements Serializable {

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

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.modified = Type1UUID.getTimeUUID();
		this.data = data;
	}

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
}
