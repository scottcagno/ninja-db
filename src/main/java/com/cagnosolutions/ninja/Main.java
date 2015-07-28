package com.cagnosolutions.ninja;

import com.cagnosolutions.ninja.db.Database;
import com.cagnosolutions.ninja.db.Document;
import com.cagnosolutions.ninja.http.HttpServer;
import java.util.List;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class Main {

	public static void main(String[] args) {

		Database db = Database.getInstance();

		List<Document> docs = db.findAllIn("dqf")
				.containing("age", "28")
				.containing("email", "foobar")
				.find();

		HttpServer server = new HttpServer(db, 8080);
		server.run();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.printf("... shutting down ninja-db\n");
				db.save();
			}
		});
	}

	/*private static final Database db = Database.getInstance();
	private static final String STORE = "dqf";
	static {
		db.createStore(STORE);
	}

	public List<Document> findAll() {
		return db.returnAllDocuments(STORE);
	}

	public Document findOne(UUID id) {
		return db.returnDocument(STORE, id);
	}*/

}
