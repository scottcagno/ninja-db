package com.cagnosolutions.ninja;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

import java.io.*;

class SerializationBox implements Serializable {
	private byte serializableProp = 10;
	public byte getSerializableProp() {
		return serializableProp;
	}
}

public class SerializationSample {

	public static void main(String args[]) throws IOException, ClassNotFoundException {
		SerializationBox serialB = new SerializationBox();
		serialize("serial.out", serialB);
		SerializationBox sb = (SerializationBox) deSerialize("serial.out");
		System.out.println(sb.getSerializableProp());
	}

	public static void serialize(String outFile, Object serializableObject) throws IOException {
		FileOutputStream fos = new FileOutputStream(outFile);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(serializableObject);
	}

	public static Object deSerialize(String serilizedObject) throws IOException, ClassNotFoundException {
		FileInputStream fis = new FileInputStream(serilizedObject);
		ObjectInputStream ois = new ObjectInputStream(fis);
		return ois.readObject();
	}

}
