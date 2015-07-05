package com.cagnosolutions.ninja.util;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class RaceConditionFinder {

	private Executor e = Executors.newFixedThreadPool(2);
	private final Map map;

	public RaceConditionFinder(final Map<Integer, String> map) {
		this.map = map;
		init();
	}

	private void init() {
		for(int i = 0; i < 1000; i++) {
			int kv = i;
			e.execute(new Runnable(){
				public void run(){
					synchronized (this) {
						map.put(kv, String.valueOf(kv));
					}
				}
			});
		}
	}
}
