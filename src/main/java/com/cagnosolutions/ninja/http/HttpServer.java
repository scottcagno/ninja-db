package com.cagnosolutions.ninja.http;

import com.cagnosolutions.ninja.database.Database;
import com.cagnosolutions.ninja.database.DocumentSet;
import com.google.gson.Gson;
import spark.Spark;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class HttpServer {

	private static Database _db = null;
	private static int _port = 8080;
	private static final int maxThreads = 8;
	private static final int minThreads = 2;
	private static final int timeOutMillis = 30000;
	private static final Gson gson = new Gson();

	public HttpServer(final Database db) {
		_db = db;
	}

	public HttpServer(final Database db, int port) {
		_db = db;
		_port = port;
	}

	public void run() {

		Spark.port(_port);
		Spark.threadPool(maxThreads, minThreads, timeOutMillis);

		Spark.after((req, res) -> {
			res.header("Access-Control-Allow-Origin", "*");
			res.type("application/json");
		});

		Spark.options("/*", (req, res) -> {
			res.header("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT");
			res.header("Access-Control-Max-Age", "3600");
			res.header("Access-Control-Allow-Headers", "accept, content-type, x-requested-with");
			res.header("Access-Control-Request-Headers", "accept, content-type, x-requested-with");
			return res;
		});

		Spark.get("/db", (req, res) -> {
			Map<String, Object> stats = new LinkedHashMap<>();
			stats.put("ninja-db", "v1.0");
			return stats;
		}, gson::toJson);

		/**
		 * Store endpoints
		 */

		Spark.put("/db/:store", "application/json", (req, res) -> {
			String storeId = req.params(":store");
			return _db.createStore(storeId);
		}, gson::toJson);

		Spark.get("/db/:store", "application/json", (req, res) -> {
			String storeId = req.params(":store");
			if(req.queryMap().toMap().size() > 0) {
				DocumentSet documentSet = _db.findAllIn(storeId);
				Map<String, String[]> queryMap = req.queryMap().toMap();
				for(Map.Entry<String, String[]> entry : queryMap.entrySet())
					documentSet = documentSet.containing(entry.getKey(), entry.getValue()[0]);
				return documentSet.find();
			}
			return _db.returnAllDocuments(storeId);
		}, gson::toJson);

		Spark.delete("/db/:store", "application/json", (req, res) -> {
			String storeId = req.params(":store");
			return _db.deleteStore(storeId);
		}, gson::toJson);

		/**
		 * Document endpoints
		 */

		Spark.post("/db/:store", "application/json", (req, res) -> {
			String storeId = req.params(":store");
			Map data = gson.fromJson(req.body(), Map.class);
			return _db.createDocument(storeId, data);
		}, gson::toJson);

		Spark.get("/db/:store/:doc", "application/json", (req, res) -> {
			String storeId = req.params(":store");
			UUID documentId = UUID.fromString(req.params(":doc"));
			return _db.returnDocument(storeId, documentId);
		}, gson::toJson);

		Spark.put("/db/:store/:doc", "application/json", (req, res) -> {
			String storeId = req.params(":store");
			UUID documentId = UUID.fromString(req.params(":doc"));
			Map data = gson.fromJson(req.body(), Map.class);
			return _db.updateDocument(storeId, documentId, data);
		}, gson::toJson);

		Spark.delete("/:store/:doc", "application/json", (req, res) -> {
			String storeId = req.params(":store");
			UUID documentId = UUID.fromString(req.params(":doc"));
			return _db.deleteDocument(storeId, documentId);
		}, gson::toJson);

		System.err.printf("Listening on %d...\n", _port);

	}

}
