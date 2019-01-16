package com.bl;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import com.dao.CommonDao;
import com.exception.SysException;
import com.utility.Constants;

/**
 * 共通ロジッククラス
 *
 * @author shiotsuki
 *
 */
abstract public class CommonBl {

	// カラム名リスト
	private ArrayList<String> columns;

	/**
	 *
	 * @return
	 */
	public ArrayList<String> getColumns() {
		return columns;
	}

	/**
	 *
	 * @param columns
	 */
	public void setColumns(ArrayList<String> columns) {
		this.columns = columns;
	}

	// DAOオブジェクト
	private CommonDao dao;

	/**
	 * DAOを返却
	 *
	 * @return
	 */
	public CommonDao getDao() {
		return dao;
	}

	/**
	 * DAOを設定
	 *
	 * @param dao
	 */
	public void setDao(CommonDao dao) {
		this.dao = dao;
	}

	/**
	 * スケルトン実装
	 *
	 * @param rs
	 * @return
	 */
	abstract protected Object getObject(ResultSet rs) throws SQLException;

	/**
	 *
	 * @param rs
	 * @throws SQLException
	 */
	private void makeMetaDataByResultSet(ResultSet rs) throws SQLException {

		ArrayList<String> columns = null;

		// メタデータを取得
		ResultSetMetaData metaData = rs.getMetaData();

		if (null != metaData) {
			// カラム名リストを生成
			columns = new ArrayList<String>();

			// カラム件数を取得
			int count = metaData.getColumnCount();

			for (int i = 1; i <= count; i++) {
				String column = metaData.getColumnName(i);
				columns.add(column);
			}
		}

		this.setColumns(columns);

	}

	/**
	 * 処理実行
	 *
	 * @return
	 */
	public Object execute(Constants.SQL_TYPE type) {

		Object obj = null;

		try {

			// 検索結果を取得する。
			ResultSet rs = this.getDao().find(type);

			if (null != rs) {
				// カラム名を取得
				this.makeMetaDataByResultSet(rs);

				// 検索結果からDTOを取得
				obj = this.getObject(rs);
			}

		} catch (SQLException e) {
			throw new SysException(e.getCause());
		}

		return obj;
	}
}
