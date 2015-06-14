package com.cagnosolutions.ninja.db;

import com.cagnosolutions.ninja.mmap.MemoryMappedFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class DatabaseEngine {

	private static final DatabaseEngine INSTANCE = new DatabaseEngine();
	private static final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
	private static final ConcurrentMap<String,DataStore> dataStores = new ConcurrentHashMap<>(16, 0.80f, 1);
	private static final MemoryMappedFile snapshot = new MemoryMappedFile("/opt/ninja-db/snapshot.db");
	private static final AtomicInteger previousCount = new AtomicInteger(0);
	private static final AtomicInteger currentCount = new AtomicInteger(0);

	service.scheduleAtFixedRate(new Runnable() {
		public void run() {
			for (DataStore dataStore : Collections.unmodifiableList((List<DataStore>) dataStores.values())) {
				if(dataStore.hasChanged())
					try {
						snapshot.write(dataStores);
					} catch (IOException ex) {
						System.err.println("Error: Could not write snapshot to disk!");
						ex.printStackTrace();
					}
			}
			int n = count.addAndGet(1);
			boolean ok = ((n*2)%10 == 0) ? true : false;
			//System.out.printf("[%d*2=%d, mod of %d is %d] outcome is: %b\n", n, n*2, n*2, ((n*2)%10), ok);
			if(ok)
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					System.err.println("DANG: something bad happened...");
				}
		}
	}, 0, 5, TimeUnit.SECONDS);



	private DatabaseEngine() {}

	public static DatabaseEngine getInstance() {
		return INSTANCE;
	}

}
