package com.cagnosolutions.ninja.test2;

import java.io.*;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class DiskQueue {

	private String path;

	public DiskQueue(String path) {
		this.path = path;
	}

	public Engine read() {
		Engine engine = null;
		try {
			engine = _read();
		} catch (IOException | ClassNotFoundException ex) {
			System.err.printf("Error reading from disk: %s\n", path);
			ex.printStackTrace();
		}
		return engine;
	}

	public void write(Engine engine) {
		try {
			_write(engine);
		} catch (IOException ex) {
			System.err.printf("Error writing to disk: %s\n", path);
			ex.printStackTrace();
		}
	}

	private Engine _read() throws IOException, ClassNotFoundException {
		ObjectInputStream objectInputStream = null;
		try {
			RandomAccessFile randomAccessFile = new RandomAccessFile(path, "r");
			FileInputStream fileInputStream = new FileInputStream(randomAccessFile.getFD());
			objectInputStream = new ObjectInputStream(fileInputStream);
			return (Engine) objectInputStream.readObject();
		} finally {
			if (objectInputStream != null) {
				objectInputStream.close();
			}
		}
	}

	private void _write(Engine engine) throws IOException {
		ObjectOutputStream objectOutputStream = null;
		try {
			RandomAccessFile randomAccessFile = new RandomAccessFile(path, "rw");
			FileOutputStream fileOutputStream = new FileOutputStream(randomAccessFile.getFD());
			objectOutputStream = new ObjectOutputStream(fileOutputStream);
			objectOutputStream.writeObject(engine);
		} finally {
			if (objectOutputStream != null) {
				objectOutputStream.close();
			}
		}
	}

}
