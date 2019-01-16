package com.utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

import com.exception.SysException;

/**
 * DBコネクションクラス
 *
 * @author shiotsuki
 *
 */
public class DBConnection {

	// DBパラメータ
	DBParam param = new DBParam();

	Properties props;

	/**
	 * @return
	 */
	public Properties GetProps()
	{
		return this.props;
	}

	/**
	 * @param _props
	 */
	public void SetProps(Properties _props)
	{
		this.props = _props;
	}

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

	/**
	 *
	 * @param isAutoCommit
	 * @throws SQLException
	 */
	public void setAutoCommit(boolean isAutoCommit) {

		try {

			if (null != this.connection) {
				this.connection.setAutoCommit(isAutoCommit);
			}

		} catch (SQLException e) {
			throw new SysException(e.getCause());
		}

	}

	// コネクション
	private Connection connection;

	/**
	 *
	 * @return
	 */
	public Connection getConnection() {
		return connection;
	}

	/**
	 *
	 * @param connection
	 */
	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	/**
	 *
	 * @return
	 * @throws SQLException
	 */
	public Statement getStatement() throws SQLException {

		Statement statement = null;

		if (null != this.connection) {
			statement = this.connection.createStatement();
		}

		return statement;
	}

	/**
	 *
	 * @param sql
	 * @param parameters
	 * @return
	 * @throws SQLException
	 */
	public PreparedStatement getPreparedStatement(String sql,
			ArrayList<Object> parameters) throws SQLException {

		PreparedStatement pstmt = null;

		if (null != this.connection) {
			pstmt = this.connection.prepareStatement(sql);

			if (parameters != null)
				// パラメータをセット
				for (int count = 0; count < parameters.size(); count++) {
					this.setParamByTypeValue(pstmt, parameters.get(count),
							count + 1);
				}
		}

		return pstmt;
	}

	/**
	 * 型に応じた値に変換してパラメータオブジェクトへ追加
	 *
	 * @throws SQLException
	 */
	private void setParamByTypeValue(PreparedStatement pstmt, Object value,
			int count) throws SQLException {

		// 文字列へ変換
		String strValue = String.valueOf(value);

		if (value instanceof Integer) {
			// 整数型の場合
			pstmt.setInt(count, Integer.valueOf(strValue));

		} else if (value instanceof Double) {
			// 浮動整数型の場合
			pstmt.setDouble(count, Double.valueOf(strValue));

		} else {
			// 文字列型の場合
			pstmt.setString(count, strValue);

		}

	}

}
