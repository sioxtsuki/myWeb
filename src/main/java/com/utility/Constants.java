package com.utility;

/**
 * 定義情報
 *
 * @author shiotsuki
 *
 */
public interface Constants
{
	// -----------------------------
	// 処理タイプ
	// -----------------------------
	enum PROCESS_TYPE
	{
		PT_SUCCESS,
		PT_NETWORK_ERROR,
		PT_EXCEPTION_ERROR,
		PT_ERROR;
	}

	// -----------------------------
	// SQL関連
	// -----------------------------
	/**
	 * SQLタイプ
	 *
	 * @author shiotsuki
	 *
	 */
	public static enum SQL_TYPE {
		SQL_TYPE_ST, SQL_TYPE_PS
	}

	public static final String SQL_STR_BIND = ":";
	public static final String SQL_STR_PARAM = "?";

	public static final String SQL_STR_SELECT = "SELECT";
	public static final String SQL_STR_FROM = "FROM";
	public static final String SQL_STR_WHERE = "WHERE";

	public static final String USER_KEY = "db.user";
	public static final String PASS_KEY = "db.password";
	public static final String URL_KEY = "db.url";
	public static final String SCHEMA_KEY = "db.schema";


	public static final String PROP_PATH = "/application.properties";
	public static final String CONF_PROP_PATH = "conf.properties";

	// -----------------------------
	// メッセージ関連
	// -----------------------------
	public static final String STRING_CODE = "UTF-8";
	public static final String CONTENTS = "application/json; charset=UTF-8";
	public static final String URL = "https://trialbot-api.line.me/v1/events";
}
