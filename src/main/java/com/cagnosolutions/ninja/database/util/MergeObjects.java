package com.cagnosolutions.ninja.database.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class MergeObjects {

	public Object merge(Object target, Object source) {
		return _toobj(_combine(target, source), target);
	}

	private static SortedMap<String,Object> _combine(Object o1, Object o2) {
		SortedMap<String,Object> target = _tomap(o1);
		SortedMap<String,Object> source = _tomap(o2);
		assert source != null;
		source.keySet().stream()
				.filter(m1Key -> m1Key != null && !Objects.equals(m1Key, ""))
					.filter(m1Key -> source.get(m1Key) != null)
						.filter(target != null ? target::containsKey : null)
							.forEach(m1Key -> {
								assert target != null;
								target.put(m1Key, source.get(m1Key));
							});
		return target;
	}

	private static SortedMap<String, Object> _tomap(Object o) {
		SortedMap<String, Object> result = new TreeMap<>();
		BeanInfo info = null;
		try {
			info = Introspector.getBeanInfo(o.getClass());
		} catch (IntrospectionException e) {
			e.printStackTrace();
		}
		if (info != null) {
			for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
				Method reader = pd.getReadMethod();
				if(reader != null && !pd.getName().equals("class"))
					try {
						result.put(pd.getName(), reader.invoke(o));
					} catch (IllegalAccessException | InvocationTargetException e) {
						e.printStackTrace();
					}
			}
		}
		return result;
	}

	private static Object _toobj(SortedMap<String, Object> map, Object o) {
		Class clazz = o.getClass();
		for(String field : map.keySet()) {
			String methodName = "set" + Character.toUpperCase(field.charAt(0)) + field.substring(1, field.length());
			Method method = null;
			try {
				method = clazz.getDeclaredMethod(methodName, map.get(field).getClass());
			} catch (NoSuchMethodException ex) {
				ex.printStackTrace();
			}
			try {
				if (method != null)
					method.invoke(o, map.get(field));
			} catch (IllegalAccessException | InvocationTargetException ex) {
				ex.printStackTrace();
			}
		}
		return o;
	}
}
