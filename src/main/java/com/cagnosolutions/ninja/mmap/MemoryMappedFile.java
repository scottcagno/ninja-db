package com.cagnosolutions.ninja.mmap;

import com.cagnosolutions.ninja.db.DataStore;

import java.io.*;
import java.util.Map;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class MemoryMappedFile {

	public static final int KB = 1024;
	public static final int MB = KB*1024;
	public int SIZE = MB;

	private String path;

	public MemoryMappedFile(String path) {
		this.path = path;
	}

	public void write(Map<String, DataStore> dataStore) throws IOException {
		ObjectOutputStream objectOutputStream = null;
		try {
			RandomAccessFile randomAccessFile = new RandomAccessFile(path, "rw");
			FileOutputStream fileOutputStream = new FileOutputStream(randomAccessFile.getFD());
			objectOutputStream = new ObjectOutputStream(fileOutputStream);
			objectOutputStream.writeObject(dataStore);
		} finally {
			if (objectOutputStream != null) {
				objectOutputStream.close();
			}
		}
	}

	public Map<String, DataStore> read(String fileName) throws IOException, ClassNotFoundException {
		ObjectInputStream objectInputStream = null;
		try {
			RandomAccessFile randomAccessFile = new RandomAccessFile(fileName, "r");
			FileInputStream fileInputStream = new FileInputStream(randomAccessFile.getFD());
			objectInputStream = new ObjectInputStream(fileInputStream);
			return (Map<String, DataStore>) objectInputStream.readObject();
		} finally {
			if (objectInputStream != null) {
				objectInputStream.close();
			}
		}
	}

}
