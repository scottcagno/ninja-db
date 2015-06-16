package com.cagnosolutions.ninja.test1;

import java.util.UUID;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class C {

	/**
	 * Representing a document
	 */

	private String id;
	private String info;

	public C(String info) {
		this.id = UUID.randomUUID().toString();
		this.info = info;
	}

	public String getId() {
		return id;
	}

	public String toString() {
		return "C{" +
				"id='" + id + '\'' +
				", info='" + info + '\'' +
				'}';
	}
}
