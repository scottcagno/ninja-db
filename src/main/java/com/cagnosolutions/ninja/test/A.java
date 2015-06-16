package com.cagnosolutions.ninja.test;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class A {

	/**
	 * Representing the databse engine
	 */

	private Map<String, B> mapA = new ConcurrentHashMap<>(16, 0.75f, 2);
	private Map<E, Integer> mapE = new ConcurrentHashMap<>(16, 0.75f, 2);

	public Map<E, Integer> getMapE() {
		return Collections.unmodifiableMap(mapE);
	}

	public void createB(String bid) {
		if(mapA.containsKey(bid))
			return;
		mapA.put(bid, new B(bid));
	}

	public B returnB(String bid) {
		if(mapA.containsKey(bid))
			return mapA.get(bid);
		return null;
	}

	public void insertC(String bid, C c) {
		if(!mapA.containsKey(bid))
			return;
		B b = mapA.get(bid);
		b.insertC(c);
		mapE.put(new E(bid, c.getId()), b.getLine());
	}

	public int getBCount() {
		return mapA.size();
	}

	public void writeSnapshot() {
		D<String> d = new D<>();
		/*for(Map.Entry<E, Integer> entry : Collections.unmodifiableMap(mapE)) {
			E e = entry.getKey();
			d.write(String.format("/tmp/%s.store", e.getBid(), e.getCid()), new Gson);
		}*/

		/**
		 * CONSIDER THAT WRITING EACH INSERT, DELETE, UPDATE, ETC. TO A FILE MIGHT WORK. IF THE FILE IS RE-READ IN-ORDER
		 * JUST AS IT WAS WRITTEN, THEN EVEN THOUGH A HASH-MAP IS NOT ORDERED IT MAY RE-INSERT THE HASH-MAP VALUES IN THE
		 * ORDER OT THE "APPEND ONLY" FILE, THUS LEAVING YOU WITH THE EXACT VALUES IN MEMORY OF THE LAST WRITE. TEST IT
		 * OUT AND SEE WHAT HAPPENS. WARNING: THIS MAY PRODUCE AN UN-NECESSARY AMOUNT OF GC AND CPU CYCLES...
		 *
		 * IT MAY BE BETTER IF THERE IS A WAY TO FIGURE OUT HOW TO KEEP GOOD TRACK OF WHAT WAS UPDATED AND WHAT WAS NOT,
		 * AND ONLY UPDATE CORRESPONDING DATA ON DISK. LET IT BE KNOWN THAT THIS IS EASIER SAID THAN DONE. GOODBYE.
		 */
	}
}
