package com.cagnosolutions.ninja.db.document;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class Document implements Serializable {

	private static final long serialVersionUID = 46315782001L;

	private UUID id;
	private UUID modified;
	private String data;

	public Document() {
		this.id = UUID.randomUUID();
		this.modified = UUID.randomUUID();
		this.data = null;
	}

	public Document(String data) {
		this.id = UUID.randomUUID();
		this.modified = UUID.randomUUID();
		this.data = data;
	}

	public UUID getId() {
		return id;
	}

	public UUID getModified() {
		return modified;
	}

	public void updateModified() {
		this.modified = UUID.randomUUID();
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String toString() {
		return "Document{" +
				"id=" + id +
				", modified=" + modified +
				", data=" + data +
				'}';
	}
}
