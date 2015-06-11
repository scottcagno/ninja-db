package com.cagnosolutions.ninja.dm;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

import java.io.Serializable;

class DiskBackedMapEntry<K> implements Serializable, Comparable<DiskBackedMapEntry> {

	private static final long serialVersionUID = 1L;
	private long filePosition = -1;
	private int objectSize = -1;
	private K key = null;

	DiskBackedMapEntry(long pos, int size, K key) {
		this.filePosition = pos;
		this.objectSize = size;
		this.key = key;
	}

	DiskBackedMapEntry(long pos, int size) {
		this(pos, size, null);
	}

	public int compareTo(DiskBackedMapEntry o) {
		return ((Long) this.filePosition).compareTo(o.filePosition);
	}
	
	public String toString() {
		return String.format("DiskBackedMapEntry[filePosition=%d, objectSize=%d, key=%s]\n",
				filePosition, objectSize, (key == null) ? "<null>" : key);
	}

	K getKey() {
		return key;
	}

	void setKey(K newKey) {
		this.key = newKey;
	}

	long getFilePosition() {
		return this.filePosition;
	}

	void setFilePosition(long pos) {
		this.filePosition = pos;
	}

	int getObjectSize() throws IllegalStateException {
		assert (this.objectSize > 0) : "No object stored yet";
		return this.objectSize;
	}

	void setObjectSize(int size) {
		this.objectSize = size;
	}

}
