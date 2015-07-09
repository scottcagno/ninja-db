package com.cagnosolutions.ninja;

import com.cagnosolutions.ninja.db.Database;
import com.cagnosolutions.ninja.http.HttpServer;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class Main {

	public static void main(String[] args) {

		Database db = Database.getInstance();
		HttpServer server = new HttpServer(db, 8080);
		server.run();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() { db.save(); }
		});
	}
}
