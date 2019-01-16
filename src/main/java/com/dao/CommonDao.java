package com.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.utility.Constants;
import com.utility.DBConnection;
import com.utility.DBParam;

/**
 * 共通DAOクラス
 *
 * @author shiotsuki
 *
 */
abstract public class CommonDao {

	// パラメータクラス
	DBParam param;

	/**
	 *
	 * @return
	 */
	public DBParam getParam() {
		return param;
	}

	/**
	 *
	 * @param param
	 */
	public void setParam(DBParam param) {
		this.param = param;
	}

	// SQL
	private String sql;

	/**
	 * *
	 *
	 * @param sql
	 */
	public void setSql(String sql) {
		this.sql = sql;
	}

	private DBConnection connection = null;

	/**
	 *
	 * @return
	 */
	public DBConnection getDBConnection() {
		return connection;
	}

	/**
	 *
	 * @param connection
	 */
	public void setDBConnection(DBConnection connection) {
		this.connection = connection;
	}

	/**
	 * パラメータ設定(スケルトン)
	 */
	abstract protected void doSetParameter();

	/**
	 * パラメータ設定
	 */
	public void setParameter() {

		// コネクションからパラメータクラスを取得
		this.setParam(this.getDBConnection().getParam());

		// パラメータ設定
		this.doSetParameter();

	}

	/***
	 * パラメータを設定
	 *
	 * @param key
	 * @param strings
	 */
	protected void paramSet(String key, Object... strings) {

		this.getParam().setParamater(key, strings);
	}

	/**
	 * 検索処理
	 *
	 * @return
	 * @throws SQLException
	 */
	public ResultSet find(Constants.SQL_TYPE type) throws SQLException {

		ResultSet rs = null;

		// パラメータを設定
		this.setParameter();

		// SQLを取得
		String strSql = new String(this.sql);

		// 取得条件：SQLがnull以外の場合
		if (null != strSql) {

			// ----------------------------------
			// SQLタイプに応じて処理を分岐
			// ----------------------------------
			switch (type) {
			case SQL_TYPE_ST:
				// 検索処理実行
				rs = this.connection.getStatement().executeQuery(strSql);

				break;

			case SQL_TYPE_PS:

				ArrayList<Object> resParams = new ArrayList<Object>();

				// キー（バインド変数名）ごとに処理を行う
				for (String key : this.param.keySet()) {
					ArrayList<Object> params = this.param.getParamsByKey(key);
					strSql = this.rePlaceSql(strSql, key, params.size());
					resParams.addAll(params);
				}

				// 検索処理実行
				rs = this.connection.getPreparedStatement(strSql, resParams)
						.executeQuery();

				break;
			}

		}

		return rs;
	}

	/**
	 * バインド変数の部分を書き直し
	 *
	 * @param sql
	 * @param bindVal
	 * @param size
	 * @return
	 */
	private String rePlaceSql(String sql, String bindVal, int size) {

		String value = new String(sql);

		value = value.replaceAll(bindVal,
				this.incStr(size, Constants.SQL_STR_PARAM));

		return value;

	}

	/**
	 * 同じ文字列をインクリメント設定
	 *
	 * @param size
	 * @param setValue
	 * @return
	 */
	private String incStr(int size, String setValue) {

		String value = setValue;

		for (int i = 1; i < size; i++) {
			value += ",".concat(setValue);
		}

		return value;
	}

}