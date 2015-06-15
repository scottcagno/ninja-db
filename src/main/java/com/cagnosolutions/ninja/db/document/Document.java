package com.cagnosolutions.ninja.db.document;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class Document implements Serializable {

	private static final long serialVersionUID = 46315782001L;

	private UUID _id;
	private UUID _modified;
	private Map<String, Object> data;

	public Document() {
		this._id = UUID.randomUUID();
		this._modified = UUID.randomUUID();
	}

	public UUID get_id() {
		return _id;
	}

	public UUID get_modified() {
		return _modified;
	}

	public void updateModified() {
		this._modified = UUID.randomUUID();
	}

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}
}
