package com.cagnosolutions.ninja.mmap;

import java.io.*;

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

	public void write(Document document) throws IOException {
		ObjectOutputStream objectOutputStream = null;
		try {
			RandomAccessFile randomAccessFile = new RandomAccessFile(path, "rw");
			FileOutputStream fileOutputStream = new FileOutputStream(randomAccessFile.getFD());
			objectOutputStream = new ObjectOutputStream(fileOutputStream);
			objectOutputStream.writeObject(document);
		} finally {
			if (objectOutputStream != null) {
				objectOutputStream.close();
			}
		}
	}

	public Document read(String fileName) throws IOException, ClassNotFoundException {
		ObjectInputStream objectInputStream = null;
		try {
			RandomAccessFile randomAccessFile = new RandomAccessFile(fileName, "r");
			FileInputStream fileInputStream = new FileInputStream(randomAccessFile.getFD());
			objectInputStream = new ObjectInputStream(fileInputStream);
			return (Document) objectInputStream.readObject();
		} finally {
			if (objectInputStream != null) {
				objectInputStream.close();
			}
		}
	}

}
