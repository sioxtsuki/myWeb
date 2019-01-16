package com.factory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import com.utility.Constants;
import com.utility.DBConnection;

/**
 * DBファクトリクラス
 *
 * @author shiotsuki
 *
 */

public class DBFactory {

	/**
	 * コネクション返却
	 *
	 * @return
	 */
	public static DBConnection getConnection(Properties configuration) throws InstantiationException, IllegalAccessException {

		DBConnection dbConnection = null;

		try
		{
			// ----------------------------------
			// プロパティファイル読み込み
			// ----------------------------------
			// Properties configuration = new Properties();
			// InputStream inputStream = new FileInputStream(new File(
			// "System.properties"));

			// configuration.load(inputStream);

			// URL
			String url = configuration.getProperty(Constants.URL_KEY);

			// スキーマ名
			url = url.concat(configuration.getProperty(Constants.SCHEMA_KEY));

			// ユーザID
			String id = configuration.getProperty(Constants.USER_KEY);

			// パスワード
			String pw = configuration.getProperty(Constants.PASS_KEY);

/*
        	System.out.println(url);
        	System.out.println(id);
        	System.out.println(pw);
*/

			Connection connection = DriverManager.getConnection(url, id, pw);

			// ----------------------------------
			// 接続処理実行
			// ----------------------------------
			dbConnection = new DBConnection();
			dbConnection.setConnection(connection);
			dbConnection.SetProps(configuration);
		}
		catch (SQLException e)
		{
	        System.out.println("MySQLに接続できませんでした。");
	        throw new InstantiationException();
		}
		return dbConnection;
	}

}
