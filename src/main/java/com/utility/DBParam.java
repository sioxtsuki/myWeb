package com.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author shiotsuki
 *
 */
public class DBParam {

	// パラメータデータ・マップ
	Map<String, ArrayList<Object>> map;

	/**
	 * コンストラクタ
	 */
	DBParam() {
		map = new HashMap<String, ArrayList<Object>>();
	}

	/**
	 *
	 * @param key
	 * @param values
	 */
	@SuppressWarnings("unchecked")
	public void setParamater(String key, Object... values) {

		// リスト方に変換
		ArrayList<?> list = new ArrayList<Object>(Arrays.asList(values));

		// データ・マップへ登録
		this.map.put(Constants.SQL_STR_BIND.concat(key),
				(ArrayList<Object>) list);
	}

	/**
	 *
	 * @return
	 */
	public ArrayList<Object> getParamsByKey(String key) {
		return this.map.get(key);
	}

	/**
	 * キー情報を返却
	 *
	 * @return
	 */
	public Set<String> keySet() {
		return this.map.keySet();
	}

	/**
	 *
	 */
	public void clear() {
		this.map.clear();
	}

	/**
	 *
	 * @param key
	 */
	public void clear(String key) {
		this.map.remove(key);
	}

}
