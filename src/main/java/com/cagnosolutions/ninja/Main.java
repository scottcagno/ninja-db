package com.cagnosolutions.ninja;

import com.cagnosolutions.ninja.db.document.Document;
import com.cagnosolutions.ninja.db.engine.Engine;

import java.util.HashMap;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class Main {

	public static void main(String[] args) throws Exception {

		Engine db = Engine.getInstance();

		db.createStore("users");
		db.insertDocument("users", new Document(new HashMap<String, Object>(){{ put("user", "u1"); }}));
		db.insertDocument("users", new Document(new HashMap<String, Object>(){{ put("user", "u2"); }}));
		db.insertDocument("users", new Document(new HashMap<String, Object>(){{ put("user", "u3"); }}));
		db.createStore("orders");
		db.insertDocument("orders", new Document(new HashMap<String, Object>(){{ put("order", "o1"); }}));

		//db.loadSnapshot();
		//System.out.printf("store count: %d\n", db.getStoreCount());
		System.out.printf("users document count: %d\n", db.returnStore("users").getDocumentCount());
		System.out.println(db.returnStore("users"));
	}

}
