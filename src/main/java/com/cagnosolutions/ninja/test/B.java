package com.cagnosolutions.ninja.test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class B {

	/**
	 * Representing the database store layer
	 */

	private String id;
	private Map<String, C> mapB = new ConcurrentHashMap<>(16, 0.75f, 2);
	private AtomicInteger lineNo = new AtomicInteger(0);

	public B(String id) {
		this.id = id;
	}

	public void insertC(C c) {
		if(mapB.containsValue(c))
			return;
		mapB.put(c.getId(), c);
	}

	public String toString() {
		return "B{" +
				"id='" + id + '\'' +
				", mapB=" + mapB +
				'}';
	}

	public int getCount() {
		return mapB.size();
	}
	
	public int getLine() {
		return lineNo.getAndAdd(1);
	}
}
