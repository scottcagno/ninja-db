package com.cagnosolutions.ninja;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class Main {

	public static void main(String[] args) throws Exception {

		final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		AtomicInteger count = new AtomicInteger(1);


		//service.scheduleWithFixedDelay(() -> System.out.println(new Date()), 0, 1, TimeUnit.SECONDS);
		service.scheduleAtFixedRate(new Runnable() {
			public void run() {
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


	}

}
