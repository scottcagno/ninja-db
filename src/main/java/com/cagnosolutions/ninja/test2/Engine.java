package com.cagnosolutions.ninja.test2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class Engine implements Serializable {

	private static final long serialVersionUID = -11235813213455890L;

	private Map<String,Store> stores;

	public Engine() {
		this.stores = new ConcurrentHashMap<>(16, 0.80f, 2);
	}

	private void writeObject(ObjectOutputStream stream) throws IOException {
		stream.writeObject(stores);
	}

	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		stores = (Map<String,Store>) stream.readObject();
		validate();
	}

	private void validate() {
		if(stores == null)
			throw new IllegalArgumentException("ninja-db.engine");
	}
	
	public void clearAll() {
		stores.values().forEach(Store::clearAll);
		this.stores.clear();
	}
}
