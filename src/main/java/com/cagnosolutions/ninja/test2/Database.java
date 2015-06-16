package com.cagnosolutions.ninja.test2;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class Database {

	private Engine engine;
	private DiskQueue diskQueue;

	public Database(String path) {
		this.engine = new Engine();
		this.diskQueue = new DiskQueue(path);
	}

	public Database() {
		this.engine = new Engine();
		this.diskQueue = new DiskQueue("/tmp/ninja.db");
	}

	public void save() {
		diskQueue.write(engine);
	}

	public void load() {
		this.engine.clearAll();
		this.engine = diskQueue.read();
	}

}
