package com.cagnosolutions.ninja.mmap;

import sun.misc.Unsafe;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class MemoryBuffer {

	public static final Unsafe unsafe = MemoryUtil.getUnsafe();

	private final byte[] buffer;

	private static final long byteArrayOffset = unsafe.arrayBaseOffset(byte[].class);
	private static final long charArrayOffset = unsafe.arrayBaseOffset(char[].class);
	private static final long longArrayOffset = unsafe.arrayBaseOffset(long[].class);

	private static final int SIZE_OF_BOOLEAN = 1;
	private static final int SIZE_OF_BYTE = 1;
	private static final int SIZE_OF_INT = 4;
	private static final int SIZE_OF_LONG = 8;

	private long pos = 0;

	public MemoryBuffer(int bufferSize) {
		this.buffer = new byte[bufferSize];
	}

	public final void putLong(long value) {
		unsafe.putLong(buffer, byteArrayOffset + pos, value);
		pos += SIZE_OF_LONG;
	}

	public final long getLong() {
		long result = unsafe.getLong(buffer, byteArrayOffset + pos);
		pos += SIZE_OF_LONG;
		return result;
	}

	public final void putObject(Object value) {
		unsafe.putObject(buffer, byteArrayOffset + pos, value);
		pos += sizeOf(value);
	}

	public final Object getObject() {
		Object result = unsafe.getObject(buffer, byteArrayOffset + pos);
		pos += sizeOf(result);
		return result;
	}

	public final void putInt(int value) {
		unsafe.putInt(buffer, byteArrayOffset + pos, value);
		pos += SIZE_OF_INT;
	}

	public final int getInt() {
		int result = unsafe.getInt(buffer, byteArrayOffset + pos);
		pos += SIZE_OF_INT;
		return result;
	}

	public final void putLongArray(final long[] values) {
		putInt(values.length);
		long bytesToCopy = values.length << 3;
		unsafe.copyMemory(values, longArrayOffset, buffer, byteArrayOffset + pos, bytesToCopy);
		pos += bytesToCopy;
	}

	public final long[] getLongArray() {
		int arraySize = getInt();
		long[] values = new long[arraySize];
		long bytesToCopy = values.length << 3;
		unsafe.copyMemory(buffer, byteArrayOffset + pos, values, longArrayOffset, bytesToCopy);
		pos += bytesToCopy;
		return values;
	}

	public final void putCharArray(final char[] values) {
		putInt(values.length);
		long bytesToCopy = values.length << 1;
		unsafe.copyMemory(values, charArrayOffset, buffer, byteArrayOffset + pos, bytesToCopy);
		pos += bytesToCopy;
	}

	public final char[] getCharArray() {
		int arraySize = getInt();
		char[] values = new char[arraySize];
		long bytesToCopy = values.length << 1;
		unsafe.copyMemory(buffer, byteArrayOffset + pos, values, charArrayOffset, bytesToCopy);
		pos += bytesToCopy;
		return values;
	}

	public final void putBoolean(boolean value) {
		unsafe.putBoolean(buffer, byteArrayOffset + pos, value);
		pos += SIZE_OF_BOOLEAN;
	}

	public final boolean getBoolean() {
		boolean result = unsafe.getBoolean(buffer, byteArrayOffset + pos);
		pos += SIZE_OF_BOOLEAN;
		return result;
	}

	public final byte[] getBuffer() {
		return buffer;
	}

	private static long normalize(int value) {
		if(value >= 0) return value;
		return (~0L >>> 32) & value;
	}

	public static long sizeOf(Object obj){
		return unsafe.getAddress(normalize(unsafe.getInt(obj, 4L)) + 12L);
	}
}
