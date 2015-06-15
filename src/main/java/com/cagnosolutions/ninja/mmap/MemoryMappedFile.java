package com.cagnosolutions.ninja.mmap;

import com.cagnosolutions.ninja.db.store.DataStore;

import java.io.*;
import java.util.concurrent.ConcurrentMap;

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

	public void deleteFile() {
		new File(path).delete();
	}

	public void writeObject(Object obj) throws IOException {
		ObjectOutputStream objectOutputStream = null;
		try {
			RandomAccessFile randomAccessFile = new RandomAccessFile(path, "rw");
			FileOutputStream fileOutputStream = new FileOutputStream(randomAccessFile.getFD());
			objectOutputStream = new ObjectOutputStream(fileOutputStream);
			objectOutputStream.writeObject(obj);
		} finally {
			if (objectOutputStream != null) {
				objectOutputStream.close();
			}
		}
	}

	public Object readObject() throws IOException, ClassNotFoundException {
		ObjectInputStream objectInputStream = null;
		try {
			RandomAccessFile randomAccessFile = new RandomAccessFile(path, "r");
			FileInputStream fileInputStream = new FileInputStream(randomAccessFile.getFD());
			objectInputStream = new ObjectInputStream(fileInputStream);
			return objectInputStream.readObject();
		} finally {
			if (objectInputStream != null) {
				objectInputStream.close();
			}
		}
	}

	public void writeDataStore(ConcurrentMap<String,DataStore> dataStores) throws IOException {
		ObjectOutputStream objectOutputStream = null;
		try {
			RandomAccessFile randomAccessFile = new RandomAccessFile(path, "rw");
			FileOutputStream fileOutputStream = new FileOutputStream(randomAccessFile.getFD());
			objectOutputStream = new ObjectOutputStream(fileOutputStream);
			objectOutputStream.writeObject(dataStores);
		} finally {
			if (objectOutputStream != null) {
				objectOutputStream.close();
			}
		}
	}

	public ConcurrentMap<String,DataStore> readDataStore() throws IOException, ClassNotFoundException {
		ObjectInputStream objectInputStream = null;
		try {
			RandomAccessFile randomAccessFile = new RandomAccessFile(path, "r");
			FileInputStream fileInputStream = new FileInputStream(randomAccessFile.getFD());
			objectInputStream = new ObjectInputStream(fileInputStream);
			return (ConcurrentMap<String,DataStore>) objectInputStream.readObject();
		} finally {
			if (objectInputStream != null) {
				objectInputStream.close();
			}
		}
	}

}
