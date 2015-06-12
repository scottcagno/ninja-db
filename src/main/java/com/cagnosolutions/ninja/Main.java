package com.cagnosolutions.ninja;

import com.cagnosolutions.ninja.mmap.Document;
import com.cagnosolutions.ninja.mmap.MemoryMappedFile;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class Main {

	public static void main(String[] args) throws Exception {

		//DiskBackedMap<Integer,String> dm = new DiskBackedMap<>("/tmp/dm");
		//new RaceConditionFinder(dm);
		//System.out.println(dm.get(12345));

		//final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		//service.scheduleWithFixedDelay(() -> System.out.println(new Date()), 0, 1, TimeUnit.SECONDS);

		/*Map<String,Object> data = new HashMap<>();
		data.put("foo", true);
		data.put("bar", false);
		data.put("done", "Yes we sure are");
		data.put("nested", new Document(new HashMap<String,Object>(){{
			put("one", 1);
			put("two", 2);
			put("three", 3);
			put("alive", true);
		}}));

		Document document = new Document(data);*/

		MemoryMappedFile mmap = new MemoryMappedFile("/tmp/fd123");
		//mmap.write(document);

		Document doc = mmap.read("/tmp/fd123");
		System.out.printf("%s\n", doc);
	}

}
