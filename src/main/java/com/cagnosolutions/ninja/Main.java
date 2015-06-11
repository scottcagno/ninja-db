package com.cagnosolutions.ninja;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class Main {

	public static void main(String[] args) throws Exception {

		//DiskBackedMap<Integer,String> dm = new DiskBackedMap<>("/tmp/dm");
		//new RaceConditionFinder(dm);
		//System.out.println(dm.get(12345));

		final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		service.scheduleWithFixedDelay(() -> System.out.println(new Date()), 0, 1, TimeUnit.SECONDS);
	}

}
