package com.utility;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Properties;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import com.entity.MenuBeans;
import com.entity.ProcessBeans;
import com.entity.UserBeans;
import com.factory.DBFactory;

/**
 * @author shiotsuki
 *
 * ユーティリティクラス
 */
public class Utility
{
	public final static String G_STR_START = "稼働";
	public final static String G_STR_STOP = "停止";
	public final static String G_STR_NONE = "未";
	public final static String G_STR_START_VALUE = "start";
	public final static String G_STR_STOP_VALUE = "stop";
	public final static String G_STR_RESTART_VALUE = "restart";

	/**
	 * レート配信Switch
	 * @param value (0 ; 停止、1: 開始)
	 * @return
	 */
	public static String RateCheckProcess(int value)
	{
		Properties props = new Properties();

		String strProcessText = (value == 0 ? "stop" : "start");

		// 定義情報を取得
		try
		{
			props.setProperty("ratechk.allow", String.valueOf(value).toString());
			props.store(new FileOutputStream(Constants.CONF_PROP_PATH), "Comments");
			props.load(new FileInputStream(Constants.CONF_PROP_PATH));

			if (props.getProperty("ratechk.allow").equals(String.valueOf(value).toString()) == false)
			{
				return "rate checker process failed.";
			}

		} catch (IOException e)
		{
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			return e.getMessage().toString();
		}

		return "rate checker " + strProcessText.toString() + " successful.";
	}

	/**
	 * レートチェック状態確認
	 * @return
	 */
	public static String RateCheckStateProcess()
	{
		Properties props = new Properties();

		// 定義情報を取得
		try
		{
			props.load(new FileInputStream(Constants.CONF_PROP_PATH));

			if (props.getProperty("ratechk.allow").equals("0") == true)
				return "stopped state.";

		} catch (IOException e)
		{
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			return e.getCause().getMessage().toString();
		}

		return "in operation.";
	}

	/**
	 * シンボル名存在チェック
	 * @param symbols
	 * @param symbolname
	 * @return
	 */
	public static boolean IsSymbolExists(String[] symbols, String symbolname)
	{
		for (String name: symbols)
		{
			if (name.equals(symbolname) == true)
				return true;
		}


		return false;
	}

	/**
	 * 状態文言を返却
	 *
	 * @param value
	 * @return
	 */
	private static String GetStatusValue(String value)
	{
		if (value.isEmpty() == true) // 空の場合
		{
			return G_STR_NONE;
		}

		return (value.equals("0") == true ? G_STR_STOP : G_STR_START);
	}

	/**
	 * 文字列完全一致判定
	 *
	 * @param from
	 * @param to
	 * @return
	 */
	public static boolean CompareString(String from, String to)
	{
		return (from.toLowerCase().equals(to.toLowerCase()));
	}

	/**
	 * 文字列前方一致判定
	 *
	 * @param from
	 * @param to
	 * @return
	 */
	public static boolean StartsWithString(String from, String to)
	{
		return (from.toLowerCase().startsWith(to.toLowerCase()));
	}

	/**
	 * アプリ一覧メッセージの編集
	 *
	 * @param text
	 * @return
	 */
	public static String EditAppsText(String text)
	{
		StringBuilder sb = new StringBuilder();
		String[] values = text.split("\n");
		for (int i = 0; i < values.length; i++)
		{
			if (i %2 == 0) // 配列位置が偶数の場合
			{
				sb.append(values[i]);
			}
			else
			{
				sb.append(" (");
				sb.append(Utility.GetStatusValue(values[i]));
				sb.append(")");
				sb.append("\n");
			}
		}

		return sb.toString();
	}

	/**
	 * DBコネクションを返却
	 *
	 * @return
	 */
	public static DBConnection GetConn()
	{
		Resource resource = new ClassPathResource(Constants.PROP_PATH);
		Properties props = null;
		DBConnection conn = null;
		try
		{
			// 定義情報を取得
			props = PropertiesLoaderUtils.loadProperties(resource);
			// DBコネクションを取得
			conn = DBFactory.getConnection(props);
		}
		catch (IOException | InstantiationException | IllegalAccessException e)
		{
			System.out.println(e.getCause());
		}
		finally
		{
			if (resource != null) resource = null;
		}

		return (conn);
	}

	/**
	 * 指定したメニュー情報を返却
	 *
	 * @param conn
	 * @param user_id
	 * @return
	 */
	public static ArrayList<MenuBeans> GetMenuInfo(DBConnection conn, int fase_id, int authority_id)
	{
	   	PreparedStatement ps = null;
    	ResultSet rs = null;
    	MenuBeans menu = null;
    	ArrayList<MenuBeans> list = null;

    	if (conn == null)
    		return list;

    	StringBuilder sbFindSQL = new StringBuilder();
    	String tb_menu = conn.GetProps().getProperty("tb.menu");
    	sbFindSQL.append("SELECT * FROM ");
    	sbFindSQL.append(tb_menu.toString());
    	sbFindSQL.append(" WHERE authority_id<=? AND permissions=1");

    	if (fase_id != -1) // フェーズが設定 ?
    	{
    		sbFindSQL.append(" AND fase_id=?");
    	}
		sbFindSQL.append(" ORDER BY fase_id, number ASC");

    	try
        {
			ps = conn.getPreparedStatement(sbFindSQL.toString(), null);
			if (ps != null)
			{
				ps.clearParameters();
				ps.setInt(1, authority_id);

				if (fase_id != -1) // フェーズが設定 ?
		    	{
		    		ps.setInt(1, fase_id);
		    	}

				rs = ps.executeQuery(); // クエリ実行
				if (rs != null)
				{
					rs.last();
					int number_of_row = rs.getRow();
					rs.beforeFirst(); //最初に戻る

					if ((number_of_row > 0) == true) // レコードが存在する場合
					{
				    	list = new ArrayList<MenuBeans>();

				    	while (rs.next())
						{
							menu = new MenuBeans();
							menu.setFase_id(rs.getInt("fase_id"));
							menu.setNumber(rs.getInt("number"));
							menu.setAuthority_id(rs.getInt("authority_id"));
							menu.setType(rs.getInt("type"));
							menu.setName(rs.getString("name"));
							menu.setContents(rs.getString("contents"));
							menu.setPermissions(rs.getInt("permissions"));
							list.add(menu);
						}

						rs.close();

					}
				}

				ps.close();
			}
    	}
        catch ( SQLException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
        	if (rs != null)
        	{
        		rs = null;
        	}
        	if (ps != null)
        	{
        		ps = null;
        	}
        }

    	return list;
	}

	/**
	 * 指定ユーザ情報を返却
	 *
	 * @param conn
	 * @param user_id
	 * @return
	 */
	public static UserBeans GetUserInfo(DBConnection conn, String user_id)
	{
	   	PreparedStatement ps = null;
    	ResultSet rs = null;
    	UserBeans user = null;

    	if (conn == null)
    		return null;

    	//String tb_user = conn.GetProps().getProperty("tb.user");
    	String bot_id = conn.GetProps().getProperty("id");

    	StringBuilder sbFindSQL = new StringBuilder();
    	sbFindSQL.append("SELECT A.user_id, A.display_name, A.bot_id,A.permissions,A.authority,B.name,A.passwd,A.last_login FROM ");
    	sbFindSQL.append("mtb_linebot_user A INNER JOIN mtb_linebot_authority B");
    	sbFindSQL.append(" WHERE A.authority = B.id AND A.user_id =? AND A.bot_id=?");

    	try
        {
			ps = conn.getPreparedStatement(sbFindSQL.toString(), null);
			if (ps != null)
			{
				ps.clearParameters();
				ps.setString(1, user_id.toString());
				ps.setString(2, bot_id.toString());

				rs = ps.executeQuery(); // クエリ実行
				if (rs != null)
				{
					rs.last();
					int number_of_row = rs.getRow();
					rs.beforeFirst(); //最初に戻る

					if ((number_of_row > 0) == true) // レコードが存在する場合
					{
						String passwd = "";
						while (rs.next())
						{
							user = new UserBeans();
							user.setId(rs.getString("user_id"));
							user.setDisplay_name(rs.getString("display_name"));
							user.setBot_id(rs.getString("bot_id"));
							user.setPermissions(rs.getInt("permissions"));
							user.setAuthority(rs.getInt("authority"));
							user.setAuthority_name(rs.getString("name"));
							passwd = rs.getString("passwd");
							user.setPasswd(Crypto.Composite(passwd).toString());
							user.setLast_login(rs.getString("last_login"));
							break;
						}

						rs.close();

					}
				}

				ps.close();
			}
    	}
        catch ( SQLException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
        	if (rs != null)
        	{
        		rs = null;
        	}
        	if (ps != null)
        	{
        		ps = null;
        	}
        }

    	return user;
	}

	/**
	 * 指定したアプリ情報を返却
	 *
	 * @param conn
	 * @param process_value
	 * @param user_id
	 * @param text
	 * @return
	 */
	public static String GetProcessInfo(DBConnection conn, String user_id, String text)
	{
		String res = "invalid application.";
		String[] values = text.split("=");

		if (values.length == 1)
		{
			return res.toString();
		}

		String[] appls = values[1].split(",");
		String server_id = "";
		StringBuilder sb = new StringBuilder();

		try
		{
			// サーバーIDを取得
			server_id = GetMessageByUserrId(conn, user_id);
			if (server_id.isEmpty() == true) // サーバーIDが存在しない場合は処理中断
			{
				return "invalid server.";
			}

			// サーバーIDに該当するアプリを取得
			String strProcess_value = GetProcessesByServerId(conn, user_id, server_id, new String[]{"id"});
			String[] processes = strProcess_value.split("\n");

			// 指定のアプリが対象の場合
			//count = appls.length;
			for (String appl : appls)
			{
				for (String process : processes)
				{
					if (process.compareTo(appl) == 0) // 名称が一致した場合
					{
						res = GetProcessInfoByColumn(conn, process, server_id,
								new String[]{
										"id", "system_id","server_id","status", "last_datetime"});

						String[] datas = res.split("\n");

						// 状態文言値を取得
						String strStatus = Utility.GetStatusValue(datas[3]);

						sb.append("■");
						sb.append(datas[0]);
						sb.append("\n");
						sb.append("system id: ");
						sb.append(datas[1]);
						sb.append("\n");
						sb.append("server id: ");
						sb.append(datas[2]);
						sb.append("\n");
						sb.append("status: ");
						sb.append(strStatus);
						sb.append("\n");
						sb.append("datetime: ");
						sb.append(datas[4]);
						sb.append("\n");

						break;
					}
				}
			}
		}
		catch (SQLException e)
		{
			// TODO 自動生成された catch ブロック
			System.out.println(e.getCause());
		}

		if (sb.length() > 0)
		{
			res = sb.toString();
			sb.delete(0, sb.length());
			sb = null;
		}

		return (res);
	}

	/**
	 * プロセス処理
	 *
	 * @param conn
	 * @param process_value
	 * @param user_id
	 * @param text
	 * @return
	 */
	public static boolean ProcessExecute(DBConnection conn, int process_value, String user_id, String text)
	{
		int count = 0, process_count = 0;
		String strStatus = "-1";
		boolean IsAll = false;
		String[] values = text.split("=");

		if (values.length == 1)
		{
			return false;
		}

		String[] appls = values[1].split(",");

		if (appls.length == 1)
		{
			// アスタリスクの場合
			IsAll = appls[0].compareTo("*") == 0;
		}

		String server_id = "";

		try
		{
			// サーバーIDを取得
			server_id = GetMessageByUserrId(conn, user_id);
			if (server_id.isEmpty() == true) // サーバーIDが存在しない場合は処理中断
			{
				return false;
			}

			// プロセスエンティティリスト
			ArrayList<ProcessBeans> beansList = new ArrayList<ProcessBeans>();

			// サーバーIDに該当するアプリを取得
			String strProcess = GetProcessesByServerId(conn, user_id, server_id, new String[]{"id"});
			String[] processes = strProcess.split("\n");

			if (IsAll == true) // 全アプリ対象の場合
			{
				count = processes.length;
				for (String process : processes)
				{
					ProcessBeans beans = new ProcessBeans();
					beans.setId(process);
					beans.setProcess(process_value);
					beansList.add(beans);
				}

				// プロセス情報更新
				if (UpdateProcessByProcessId(conn, beansList) == true)
				{
					Thread.sleep(5000);
					for (ProcessBeans beans : beansList)
					{
						process_count++;
						// 処理結果が返却されるまでループ
						while (true)
						{
							strStatus = GetProcessInfoByColumn(conn, beans.getId().toString(), server_id, new String[]{"status"});
							if (Integer.parseInt(strStatus) != process_value)
							{
								break;
							}
						}
					}
				}
			}
			else
			{
				// 指定のアプリが対象の場合
				count = appls.length;
				for (String appl : appls)
				{
					for (String process : processes)
					{
						if (process.compareTo(appl) == 0) // 名称が一致した場合
						{
							ProcessBeans beans = new ProcessBeans();
							beans.setId(process);
							beans.setProcess(process_value);
							beansList.add(beans);
							break;
						}
					}
				}

				// データベース更新処理
				if (UpdateProcessByProcessId(conn, beansList) == true)
				{
					Thread.sleep(5000);
					for (ProcessBeans beans : beansList)
					{
						process_count++;
						// 処理結果が返却されるまでループ
						while (true)
						{
							strStatus = GetProcessInfoByColumn(conn, beans.getId().toString(), server_id, new String[]{"status"});
							if (Integer.parseInt(strStatus) != process_value)
							{
								break;
							}
						}
					}
				}
			}
			beansList.clear();
			beansList = null;
		}
		catch (SQLException | InterruptedException e)
		{
			// TODO 自動生成された catch ブロック
			System.out.println(e.getCause());
		}

		return (process_count == count);
	}

	/**
	 * 指定プロセス情報の指定したカラム値を返却
	 *
	 * @param conn
	 * @param process_id
	 * @param server_id
	 * @return
	 * @throws SQLException
	 */
	public static String GetProcessInfoByColumn(DBConnection conn, String process_id, String server_id, String[] columns) throws SQLException
	{
		String res = "";
	   	PreparedStatement ps = null;
    	ResultSet rs = null;

    	if (conn == null)
    		return "";

    	String tb_process = conn.GetProps().getProperty("tb.process");

    	StringBuilder sbFindSQL = new StringBuilder();
    	sbFindSQL.append("SELECT * FROM ");
    	sbFindSQL.append(tb_process.toString());
    	sbFindSQL.append(" WHERE id= ? AND server_id=?");

    	try
        {
			ps = conn.getPreparedStatement(sbFindSQL.toString(), null);
			if (ps != null)
			{
				ps.clearParameters();
				ps.setString(1, process_id.toString());
				ps.setString(2, server_id.toString());

				rs = ps.executeQuery(); // クエリ実行
				if (rs != null)
				{
					rs.last();
					int number_of_row = rs.getRow();
					rs.beforeFirst();   //最初に戻る

					if ((number_of_row > 0) == true) // レコードが存在する場合
					{
						StringBuilder sbData = new StringBuilder();

						while (rs.next())
						{
							for (String column : columns) // 指定カラム数分ループ
							{
								sbData.append(rs.getString(column.trim().toString()));
								if (columns.length > 2)
								{
									sbData.append("\n");
								}
							}
						}

						rs.close();

						res = sbData.toString(); // 返却変数にセット

						sbData.delete(0, sbData.length());
						sbData = null;
					}
				}
				ps.close();
			}
    	}
        catch ( SQLException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
        	if (rs != null)
        	{
        		rs = null;
        	}
        	if (ps != null)
        	{
        		ps = null;
        	}
        }

    	return res;
	}

	/**
	 * データベースのプロセスを更新
	 *
	 * @param conn
	 * @param process_id
	 * @param process_value
	 * @return
	 * @throws SQLException
	 */
	public static boolean UpdateProcessByProcessId(DBConnection conn, ArrayList<ProcessBeans> beansList) throws SQLException
	{
		boolean res = false;
	   	PreparedStatement ps = null;
    	ResultSet rs = null;

    	if (beansList.size() == 0)
    		return false;

    	if (conn == null)
    		return false;

    	String tb_process = conn.GetProps().getProperty("tb.process");
    	//String bot_id = conn.GetProps().getProperty("id");
		SimpleDateFormat sdf = new SimpleDateFormat("YYYY/MM/DD HH:mm:ss");
    	int ret = 0; // 処理結果

    	int count = 0;
    	String strWhen = " case id";
    	String strWhere = " WHERE id IN (";
    	for (ProcessBeans beans : beansList)
    	{
    		count++;
    		strWhen += " WHEN '";
    		strWhen += beans.getId();
    		strWhen += "' THEN ";
    		strWhen += String.valueOf(beans.getProcess());
    		strWhen += " ";

    		strWhere += " '";
    		strWhere += beans.getId();
    		strWhere += "' ";

    		if (count != beansList.size())
    		{
        		strWhere += ",";
    		}
    	}
		strWhen += " END ";
		strWhere += ")";

    	// 追加SQL
    	StringBuilder sbFindSQL = new StringBuilder();
    	sbFindSQL.append("UPDATE ");
    	sbFindSQL.append(tb_process.toString());
    	sbFindSQL.append(" SET process=");
    	sbFindSQL.append(strWhen.toString());
    	sbFindSQL.append(" ,last_datetime=NOW() ");
    	sbFindSQL.append(strWhere.toString());

    	/*
    	sbFindSQL.append("UPDATE ");
    	sbFindSQL.append(tb_process.toString());
    	sbFindSQL.append(" SET process=?, last_datetime=?");
    	sbFindSQL.append(" WHERE id=?");
    	 */
    	try
        {
			// 更新処理
			ps = conn.getPreparedStatement(sbFindSQL.toString(), null);
			if (ps != null)
			{
				//ps.clearParameters();
				//ps.setInt(1, process_value);
				//ps.setString(2, sdf.format(new Date()).toString());
				//ps.setString(3, process_id.toString());

				ret = ps.executeUpdate(); // クエリ実行

				if (ret != 0) // 処理成功の場合
				{
					//System.out.println("message add successful. [" + process_id.toString()+ "]");
					res = true;
				}

				ps.close();
			}
    	}
        catch ( SQLException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
        	sbFindSQL.delete(0, sbFindSQL.length());
        	sbFindSQL =null;

        	if (rs != null)
        	{
        		rs = null;
        	}
        	if (ps != null)
        	{
        		ps = null;
        	}
        }

    	return res;
	}

	/**
	 * データベースから指定ユーザのメッセージを取得
	 *
	 * @param conn
	 * @param user_id
	 * @return
	 * @throws SQLException
	 */
	public static String GetMessageByUserrId(DBConnection conn, String user_id) throws SQLException
	{
		String res = "";
	   	PreparedStatement ps = null;
    	ResultSet rs = null;

    	if (conn == null)
    		return "";

    	String tb_message = conn.GetProps().getProperty("tb.message");
    	String bot_id = conn.GetProps().getProperty("id");

    	StringBuilder sbFindSQL = new StringBuilder();
    	sbFindSQL.append("SELECT message FROM ");
    	sbFindSQL.append(tb_message.toString());
    	sbFindSQL.append(" WHERE user_id= ? AND bot_id=?");

    	try
        {
			ps = conn.getPreparedStatement(sbFindSQL.toString(), null);
			if (ps != null)
			{
				ps.clearParameters();
				ps.setString(1, user_id.toString());
				ps.setString(2, bot_id.toString());

				rs = ps.executeQuery(); // クエリ実行
				if (rs != null)
				{
					rs.last();
					int number_of_row = rs.getRow();
					rs.beforeFirst();   //最初に戻る

					if ((number_of_row > 0) == true) // レコードが存在する場合
					{
						StringBuilder sbData = new StringBuilder();

						while (rs.next())
						{
							sbData.append(rs.getString("message"));
						}

						rs.close();

						res = sbData.toString(); // 返却変数にセット

						sbData.delete(0, sbData.length());
						sbData = null;
					}
				}

				ps.close();
			}
    	}
        catch ( SQLException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
        	if (rs != null)
        	{
        		rs = null;
        	}
        	if (ps != null)
        	{
        		ps = null;
        	}
        }

    	return res;
	}

	/**
	 * ユーザメッセージをデータベースへ登録
	 *
	 * @param conn
	 * @param user_id
	 * @param message
	 * @return
	 * @throws SQLException
	 */
	public static boolean AddMessageByUserId(DBConnection conn, String user_id, String message) throws SQLException
	{
		boolean res = false;
	   	PreparedStatement ps = null;
    	ResultSet rs = null;

    	if (conn == null)
    		return false;

    	String tb_message = conn.GetProps().getProperty("tb.message");
    	String bot_id = conn.GetProps().getProperty("id");
		//SimpleDateFormat sdf = new SimpleDateFormat("YYYY/MM/DD HH:mm:ss");
    	int ret = 0; // 処理結果

		// 削除SQL
    	StringBuilder sbDelSQL = new StringBuilder();
    	sbDelSQL.append("DELETE FROM ");
    	sbDelSQL.append(tb_message.toString());
    	sbDelSQL.append(" WHERE user_id=? AND bot_id=?");

    	// 追加SQL
    	StringBuilder sbFindSQL = new StringBuilder();
    	sbFindSQL.append("INSERT INTO ");
    	sbFindSQL.append(tb_message.toString());
    	sbFindSQL.append(" VALUES(?,?,?,NOW())");
    	//Calendar cl = Calendar.getInstance();

    	try
        {
			// 削除処理
			ps = conn.getPreparedStatement(sbDelSQL.toString(), null);
			if (ps != null)
			{
				ps.clearParameters();
				ps.setString(1, user_id.toString());
				ps.setString(2, bot_id.toString());

				ret = ps.executeUpdate();
				if (ret == 1)
					System.out.println("user delete successful. [" + user_id.toString() + "]");
				ps.close();
			}

			// 追加処理
			ps = conn.getPreparedStatement(sbFindSQL.toString(), null);
			if (ps != null)
			{
				ps.clearParameters();
				ps.setString(1, user_id.toString());
				ps.setString(2, bot_id.toString());
				ps.setString(3, message.toString());
				//ps.setString(4, sdf.format(cl.getTime()));

				ret = ps.executeUpdate(); // クエリ実行

				if (ret != 0) // 処理成功の場合
				{
					System.out.println("message add successful. [" + user_id.toString() + "]");
					res = true;
				}

				ps.close();
			}
    	}
        catch ( SQLException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
        	sbDelSQL.delete(0, sbDelSQL.length());
        	sbDelSQL =null;

        	sbFindSQL.delete(0, sbFindSQL.length());
        	sbFindSQL =null;

        	if (rs != null)
        	{
        		rs = null;
        	}
        	if (ps != null)
        	{
        		ps = null;
        	}
        }

    	return res;
	}

	/**
	 * データベースから指定サーバーのアプリ情報を取得
	 *
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static String GetProcessesByServerId(DBConnection conn, String user_id, String server_id, String[] columns) throws SQLException
	{
		String res = "";
	   	PreparedStatement ps = null;
    	ResultSet rs = null;

    	if (conn == null)
    		return "database connect error.";

    	String tb_process = conn.GetProps().getProperty("tb.process");

    	StringBuilder sbFindSQL = new StringBuilder();
    	sbFindSQL.append("SELECT * FROM ");
    	sbFindSQL.append(tb_process.toString());
    	sbFindSQL.append(" WHERE server_id= ? AND permissions=?");

    	try
        {
			ps = conn.getPreparedStatement(sbFindSQL.toString(), null);
			if (ps != null)
			{
				ps.clearParameters();
				ps.setString(1, server_id.toString());
				ps.setInt(2, 1);

				rs = ps.executeQuery(); // クエリ実行
				if (rs != null)
				{
					rs.last();
					int number_of_row = rs.getRow();
					rs.beforeFirst(); //最初に戻る

					if ((number_of_row > 0) == true) // レコードが存在する場合
					{
						StringBuilder sbData = new StringBuilder();

						while (rs.next())
						{
							for (String column : columns)
							{
								sbData.append(rs.getString(column.trim().toString()));
								sbData.append("\n");
							}
						}

						rs.close();

						res = sbData.toString(); // 返却変数にセット

						sbData.delete(0, sbData.length());
						sbData = null;
					}
				}

				ps.close();
			}
    	}
        catch ( SQLException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
        	if (rs != null)
        	{
        		rs = null;
        	}
        	if (ps != null)
        	{
        		ps = null;
        	}
        }

    	return res;
	}

	/**
	 * データベースからサーバー情報を取得
	 *
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static String GetServer(DBConnection conn) throws SQLException
	{
		String res = "";
	   	PreparedStatement ps = null;
    	ResultSet rs = null;

    	if (conn == null)
    		return "database connect error.";

    	String tb_server = conn.GetProps().getProperty("tb.server");

    	StringBuilder sbFindSQL = new StringBuilder();
    	sbFindSQL.append("SELECT id, name FROM ");
    	sbFindSQL.append(tb_server.toString());
    	sbFindSQL.append(" WHERE type=? OR type=? ");

    	try
        {
			ps = conn.getPreparedStatement(sbFindSQL.toString(), null);
			if (ps != null)
			{
				ps.clearParameters();
				ps.setInt(1, 0);
				ps.setInt(2, 2);

				rs = ps.executeQuery(); // クエリ実行
				if (rs != null)
				{
					rs.last();
					int number_of_row = rs.getRow();
					rs.beforeFirst();   //最初に戻る

					if ((number_of_row > 0) == true) // レコードが存在する場合
					{
						StringBuilder sbData = new StringBuilder();

						while (rs.next())
						{
							sbData.append("ID : ");
							sbData.append(rs.getString("id"));
							sbData.append("\t(");
							sbData.append(rs.getString("name"));
							sbData.append(")\n");
						}

						rs.close();

						res = sbData.toString(); // 返却変数にセット

						sbData.delete(0, sbData.length());
						sbData = null;
					}
				}

				ps.close();
			}
    	}
        catch ( SQLException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
        	if (rs != null)
        	{
        		rs = null;
        	}
        	if (ps != null)
        	{
        		ps = null;
        	}
        }

    	return res;
	}

	/**
	 * データベースからシンボル情報を返却
	 *
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static String GetSymbols(DBConnection conn) throws SQLException
	{
		String res = "invalid symbols.";
    	PreparedStatement ps = null;
    	ResultSet rs = null;

    	if (conn == null)
    		return "database connect error.";

    	String tb_rate = conn.GetProps().getProperty("tb.rate");
    	String tb_exchange = conn.GetProps().getProperty("tb.exchange");

    	StringBuilder sbFindSQL = new StringBuilder();
    	sbFindSQL.append("SELECT B.name name, A.symbol symbol FROM ");
    	sbFindSQL.append(tb_rate.toString());
		sbFindSQL.append(" A JOIN ");
    	sbFindSQL.append(tb_exchange.toString());
		sbFindSQL.append(" B ON A.exgcode = B.code ORDER BY A.symbol ASC");

    	try
        {
			ps = conn.getPreparedStatement(sbFindSQL.toString(), null);
			if (ps != null)
			{
				rs = ps.executeQuery();
				if (rs != null)
				{
					rs.last();
					int number_of_row = rs.getRow();
					rs.beforeFirst();   //最初に戻る

					if ((number_of_row > 0) == true) // レコードが存在する場合
					{
						StringBuilder sbData = new StringBuilder();

						while (rs.next())
						{
							sbData.append(rs.getString("symbol"));
							sbData.append("\t");
							sbData.append("：");
							sbData.append(rs.getString("name"));
							sbData.append("\n");
						}

						rs.close();

						res = sbData.toString(); // 返却変数にセット

						sbData.delete(0, sbData.length());
						sbData = null;
					}
				}

				ps.close();
			}
    	}
        catch ( SQLException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
        	if (rs != null)
        	{
        		rs = null;
        	}
        	if (ps != null)
        	{
        		ps = null;
        	}
        }

    	return res;
    }

	/**
	 * データベースから指定のシンボルTick情報を返却
	 *
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static String GetRate(DBConnection conn, String symbol) throws SQLException
	{
		String res = "invalid symbol.";
    	PreparedStatement ps = null;
    	ResultSet rs = null;

    	if (conn == null)
    		return "database connect error.";

    	String tb_rate = conn.GetProps().getProperty("tb.rate");
    	String tb_exchange = conn.GetProps().getProperty("tb.exchange");

    	StringBuilder sbFindSQL = new StringBuilder();
    	sbFindSQL.append("SELECT B.name name, A.bid bid, A.ask ask, A.last_price last_price, A.datetime datetime FROM ");
    	sbFindSQL.append(tb_rate.toString());
    	sbFindSQL.append(" A JOIN ");
    	sbFindSQL.append(tb_exchange.toString());
    	sbFindSQL.append(" B ON A.exgcode = B.code WHERE A.symbol=?");
    	//sbFindSQL.append("'");
    	//sbFindSQL.append(symbol.toUpperCase().toString());
    	//sbFindSQL.append("'");

    	try
        {
			ps = conn.getPreparedStatement(sbFindSQL.toString(), null);
			if (ps != null)
			{
				ps.clearParameters();
				ps.setString(1, symbol.toUpperCase().toString());
				rs = ps.executeQuery();
				if (rs != null)
				{
					rs.last();
					int number_of_row = rs.getRow();
					rs.beforeFirst();   //最初に戻る

					if ((number_of_row > 0) == true) // レコードが存在する場合
					{
						StringBuilder sbData = new StringBuilder();
						sbData.append("■" + symbol.toUpperCase().toString());

						while (rs.next())
						{
							sbData.append("\n");
							sbData.append(rs.getString("name"));
							sbData.append("\n");
							sbData.append("bid：");
							sbData.append(rs.getString("bid"));
							sbData.append("\n");
							sbData.append("ask：");
							sbData.append(rs.getString("ask"));
							sbData.append("\n");
							sbData.append("last price：");
							sbData.append(rs.getString("last_price"));
							sbData.append("\n");
							sbData.append("datetime：");
							sbData.append(rs.getString("datetime"));
							sbData.append("\n");
						}

						rs.close();

						res = sbData.toString();

						sbData.delete(0, sbData.length());
						sbData = null;
					}
				}

				ps.close();
			}
    	}
        catch ( SQLException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
        	if (rs != null)
        	{
        		rs = null;
        	}
        	if (ps != null)
        	{
        		ps = null;
        	}
        }

    	return res;
	}


	/**
	 * データベースからレート状況を返却
	 *
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static String IsRate_Proess(DBConnection conn) throws SQLException
	{
		String res = "";
    	PreparedStatement ps = null;
    	ResultSet rs = null;

    	if (conn == null)
    		return "database connect error.";

    	String tb_rate_process = conn.GetProps().getProperty("tb.rate_process");
    	String tb_exchange = conn.GetProps().getProperty("tb.exchange");

    	StringBuilder sbFindSQL = null;

    	try
        {
        	sbFindSQL = new StringBuilder();
        	sbFindSQL.append("SELECT B.name name, A.contents contents, A.datetime datetime FROM ");
        	sbFindSQL.append(tb_rate_process.toString());
        	sbFindSQL.append(" A JOIN ");
        	sbFindSQL.append(tb_exchange.toString());
        	sbFindSQL.append(" B ON A.exgcode = B.code");

        	ps = conn.getPreparedStatement(sbFindSQL.toString(), null);
			if (ps != null)
			{
				rs = ps.executeQuery();
				if (rs != null)
				{
					rs.last();
					int number_of_row = rs.getRow();
					rs.beforeFirst();   //最初に戻る

					if ((number_of_row > 0) == true) // エラーレコードが存在する場合
					{
						StringBuilder sbRes = new StringBuilder();
						sbRes.append("【Error】");

						while (rs.next())
						{
							sbRes.append("\n");
							sbRes.append("■");
							sbRes.append(rs.getString("name"));
							sbRes.append("\n");
							sbRes.append(rs.getString("contents"));
							sbRes.append("\n");
							sbRes.append(rs.getString("datetime"));
						}

						res = sbRes.toString();
						sbRes.delete(0, sbRes.length());
					}

					rs.close();
				}

				ps.close();
			}
    	}
        catch ( SQLException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
        	if (sbFindSQL != null)
        	{
        		sbFindSQL.delete(0, sbFindSQL.length());
        		sbFindSQL = null;
        	}
        	if (rs != null)
        	{
        		rs = null;
        	}
        	if (ps != null)
        	{
        		ps = null;
        	}
       }

    	return res;
	}
}
