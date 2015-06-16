package com.cagnosolutions.ninja.test;

import java.io.*;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class D<T> {

	/**
	 * Represeting the disk serializer
	 */

	public T read(String path) {
		T type = null;
		try {
			type = _read(path);
		} catch (IOException | ClassNotFoundException ex) {
			System.err.printf("Error reading from disk: %s\n", path);
			ex.printStackTrace();
		}
		return type;
	}

	public void write(String path, T t) {
		try {
			_write(path, t);
		} catch (IOException ex) {
			System.err.printf("Error writing to disk: %s\n", path);
			ex.printStackTrace();
		}
	}

	private T _read(String path) throws IOException, ClassNotFoundException {
		ObjectInputStream objectInputStream = null;
		try {
			RandomAccessFile randomAccessFile = new RandomAccessFile(path, "r");
			FileInputStream fileInputStream = new FileInputStream(randomAccessFile.getFD());
			objectInputStream = new ObjectInputStream(fileInputStream);
			return (T) objectInputStream.readObject();
		} finally {
			if (objectInputStream != null) {
				objectInputStream.close();
			}
		}
	}

	private void _write(String path, T obj) throws IOException {
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

}
