package com.cagnosolutions.ninja;

import com.cagnosolutions.ninja.database.Database;
import com.cagnosolutions.ninja.database.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class Main {

	//GsonBuilder().setPrettyPrinting().create();

	public static void main(String[] args) throws Exception {

		Database db = new Database();
		//createAndSave(db);
		//readAndLoad(db);
		//writeTest(db, 250000);
		//readTest(db);

		db.createStore("foobar");

		Map<String,Object> data1 = new HashMap<>();
		data1.put("id", 123);
		data1.put("email", "foo@bar.com");
		data1.put("active", true);
		db.createDocument("data1", data1);

		Map<String,Object> data2 = new HashMap<>();
		data2.put("id", 456);
		data2.put("email", "bar@baz.com");
		data2.put("active", false);
		db.createDocument("data2", data2);

		db.export();
	}

	public static void writeTest(Database db, int dataSet) {
		System.out.println("Creating users-test store...");
		db.createStore("users-test");
		System.out.printf("Loading users-test store (%d)...\n", dataSet);
		for (int i = 0; i < dataSet; i++) {
			Map<String,Object> testData = new HashMap<>();
			testData.put("id", i);
			testData.put("active", true);
			testData.put("name", "Mr. Number " + i);
			db.createDocument("users-test", testData);
		}
		System.out.println("Finished loading, writing to disk...");
		long start = System.currentTimeMillis();
		db.save();
		System.out.printf("Took %dms to write %d entries to disk\n", System.currentTimeMillis() - start, dataSet);

	}

	public static void readTest(Database db) {
		System.out.println("Reading data into memory from user-test store...");
		long start = System.currentTimeMillis();
		db.load();
		System.out.printf("Took %dms to load %d store(s) and %d documents from disk...\n",
				System.currentTimeMillis() - start, db.getStoreCount(), db.getTotalDocumentCount());
	}

	public static void createAndSave(Database db) {

		db.createStore("users");

		Map<String,Object> user1 = new HashMap<>();
		user1.put("id", 1);
		user1.put("active", true);
		user1.put("name", "Mr. Number One");
		db.createDocument("users", user1);

		Map<String,Object> user2 = new HashMap<>();
		user2.put("id", 2);
		user2.put("active", false);
		user2.put("name", "Mr. Two");
		db.createDocument("users", user2);

		Map<String,Object> user3 = new HashMap<>();
		user3.put("id", 3);
		user3.put("active", true);
		user3.put("name", "Mrs. Three");
		db.createDocument("users", user3);

		db.save();
	}

	public static void readAndLoad(Database db) {

		db.load();
		List<Document> documents = db.returnAllDocuments("users");
		documents.forEach(System.out::println);

	}

}
