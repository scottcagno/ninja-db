package com.cagnosolutions.ninja.test2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
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
		this.documents = new ConcurrentHashMap<>(16, 0.80f, 2);
	}

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
	
	public void clearAll() {
		this.documents.clear();
	}
}
