package com.cagnosolutions.ninja.mmap;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class MemoryUtil {

	private static final Unsafe THE_UNSAFE;

	static {
		try {
			Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
			theUnsafe.setAccessible(true);
			THE_UNSAFE = (Unsafe) theUnsafe.get(null);
		} catch (Exception e) {
			throw new RuntimeException("Unable to load unsafe", e);
		}
	}

	public static Unsafe getUnsafe() {
		return THE_UNSAFE;
	}
}
