package com.cagnosolutions.ninja.mmap;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class Document implements Serializable {

	private final static long serialVersionUID = 52167356721L;

	private String _id;
	private String _modified;
	private Map<String,Object> data;

	public Document(Map<String,Object> data) {
		this._id = UUID.randomUUID().toString();
		this._modified = UUID.randomUUID().toString();
		this.data = data;
	}

	public String toString() {
		return String.format("Document{ _id='%s', _modified='%s', data=%s}\n", _id, _modified, data);
	}
/*public void write(MemoryBuffer buffer) {

		// handle writing id
		boolean idExists = _id != null;
		buffer.putBoolean(idExists);
		if(idExists)
			buffer.putCharArray(_id.toCharArray());

		// handle writing modified
		boolean modifiedExists = _modified != null;
		buffer.putBoolean(modifiedExists);
		if(modifiedExists)
			buffer.putCharArray(_modified.toCharArray());

		// handle writing hashmap
		boolean dataExists = data != null;
		buffer.putBoolean(dataExists);
		if(dataExists)
			buffer.putObject(data);
	}

	public static Document read(MemoryBuffer unsafeBuffer) {
		*//*final Document result = new Document();
		result.longVariable = unsafeBuffer.getLong();
		result.longArray = unsafeBuffer.getLongArray();
		boolean objectExists = unsafeBuffer.getBoolean();
		if (objectExists) {
			result.stringObject = String.valueOf(unsafeBuffer.getCharArray());
		}
		objectExists = unsafeBuffer.getBoolean();
		if (objectExists) {
			result.secondStringObject = String.valueOf(unsafeBuffer.getCharArray());
		}
		return result;*//*

		return null;
	}*/


}
