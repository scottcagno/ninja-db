package com.cagnosolutions.ninja;

import com.cagnosolutions.ninja.db.document.Document;
import com.cagnosolutions.ninja.db.engine.Engine;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class Main {

	public static void main(String[] args) throws Exception {

		Engine db = Engine.getInstance();
		db.createStore("users");
		db.insertDocument("users", new Document("user1"));
		db.insertDocument("users", new Document("user2"));
		db.insertDocument("users", new Document("user3"));
		db.insertDocument("users", new Document("user4"));
		db.insertDocument("users", new Document("user5"));
		System.out.printf("users document count: %d\n", db.returnStore("users").getDocumentCount());
		System.out.println(db.returnStore("users"));

		Gson gson = new Gson();

	}

}
