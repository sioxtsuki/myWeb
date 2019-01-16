package com.utility;

import java.lang.reflect.Field;

public class Reflection {

	/**
	 * フィールドの値を取得します。<br />
	 * public/private おかまいなしです。 getter/setter なくても取得可能
	 *
	 * @param obj
	 *            取得対象オブジェクト
	 * @param name
	 *            取得対象フィールド名
	 * @return フィールドの値
	 */
	public static Object get(Object obj, String name) {

		if (obj == null || name == null) {
			return null;
		}

		Field f = null;
		Object result = null;

		try {
			name = name.replaceAll(" ", "");
			f = obj.getClass().getDeclaredField(name);
			f.setAccessible(true);
			result = f.get(obj);

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return result;

	}

	/**
	 * フィールドの値を設定します。<br />
	 * public/private おかまいなしです。 getter/setter なくても設定可能
	 *
	 * @param obj
	 *            設定対象オブジェクト
	 * @param name
	 *            設定対象フィールド名
	 * @param value
	 *            設定する値
	 */
	public static void put(Object obj, String name, Object value) {

		Field f = null;

		try {
			name = name.replaceAll(" ", "");
			f = obj.getClass().getDeclaredField(name);
			f.setAccessible(true);
			f.set(obj, value);

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

	}
}
