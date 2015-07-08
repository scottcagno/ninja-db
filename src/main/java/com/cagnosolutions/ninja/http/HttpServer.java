package com.cagnosolutions.ninja.http;

import com.cagnosolutions.ninja.db.Database;
import com.cagnosolutions.ninja.db.Document;
import com.cagnosolutions.ninja.db.DocumentSet;
import com.google.gson.Gson;
import org.eclipse.jetty.http.HttpStatus;
import spark.Spark;

import java.util.HashMap;
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

		/**
		 * Settings & Control
		 */

		Spark.port(_port);
		Spark.threadPool(maxThreads, minThreads, timeOutMillis);
		Spark.staticFileLocation("/public");

		/**
		 * Routing filters
		 */

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

		/**
		 * Store endpoints
		 */

		Spark.put("/db/:store", "application/json", (req, res) -> {
			String storeId = req.params(":store");
			boolean ok = _db.createStore(storeId);
			res.type("application/json");
			return (ok) ? status(200, true) : status(409, false);
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
			boolean ok = _db.deleteStore(storeId);
			res.type("application/json");
			return (ok) ? status(200, true) : status(404, false);
		}, gson::toJson);

		/**
		 * Document endpoints
		 */

		Spark.post("/db/:store", "application/json", (req, res) -> {
			String storeId = req.params(":store");
			Map data = gson.fromJson(req.body(), Map.class);
			Document document = _db.createDocument(storeId, data);
			res.type("application/json");
			return (document != null) ? document : status(404, false);
		}, gson::toJson);

		Spark.get("/db/:store/:doc", "application/json", (req, res) -> {
			String storeId = req.params(":store");
			UUID documentId = UUID.fromString(req.params(":doc"));
			Document document = _db.returnDocument(storeId, documentId);
			res.type("application/json");
			return (document != null) ? document : status(404, false);
		}, gson::toJson);

		Spark.put("/db/:store/:doc", "application/json", (req, res) -> {
			String storeId = req.params(":store");
			UUID documentId = UUID.fromString(req.params(":doc"));
			Map data = gson.fromJson(req.body(), Map.class);
			boolean ok = _db.updateDocument(storeId, documentId, data);
			res.type("application/json");
			return (ok) ? status(200, true) : status(404, false);
		}, gson::toJson);

		Spark.delete("/:store/:doc", "application/json", (req, res) -> {
			String storeId = req.params(":store");
			UUID documentId = UUID.fromString(req.params(":doc"));
			boolean ok = _db.deleteDocument(storeId, documentId);
			res.type("application/json");
			return (ok) ? status(200, true) : status(404, false);
		}, gson::toJson);

		System.err.printf("Listening on %d...\n", _port);
	}
	
	private Map<String, Object> status(int code, boolean successful) {
		Map<String, Object> status = new HashMap<>(3);
		status.put("code", code);
		status.put("message", HttpStatus.getMessage(code));
		status.put("success", successful);
		return status;
	}
	
}