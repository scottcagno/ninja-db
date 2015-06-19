package com.cagnosolutions.ninja;

import com.cagnosolutions.ninja.database.Database;
import com.cagnosolutions.ninja.http.HttpServer;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class Main {

	public static void main(String[] args) throws Exception {
		Database db = Database.getInstance();
		HttpServer server = new HttpServer(db, 8080);
		server.run();
	}

}
