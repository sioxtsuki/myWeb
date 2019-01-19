package com.utility;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import com.factory.DBFactory;

public class Utility
{
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

			// サーバーIDに該当するアプリを取得
			String strProcess = GetProcessesByServerId(conn, user_id, server_id);
			String[] processes = strProcess.split("\n");

			if (IsAll == true) // 全アプリ対象の場合
			{
				count = processes.length;
				for (String process : processes)
				{
					if (UpdateProcessByProcessId(conn, process.toString(), process_value) == true)
					{
						while (true)
						{
							Thread.sleep(1000);
							strStatus = GetStatusByProcess(conn, process, server_id);
							if (Integer.parseInt(strStatus) != process_value)
							{
								break;
							}
						}
						process_count++;
					}
				}
			}
			else
			{
				count = appls.length;
				// 指定のアプリが対象の場合
				for (String appl : appls)
				{
					for (String process : processes)
					{
						if (process.compareTo(appl) == 0)
						{
							if (UpdateProcessByProcessId(conn, process.toString(), process_value) == true)
							{
								while (true)
								{
									Thread.sleep(1);
									strStatus = GetStatusByProcess(conn, process, server_id);
									if (Integer.parseInt(strStatus) != process_value)
									{
										break;
									}
								}
								process_count++;
							}

							break;
						}
					}
				}
			}
		}
		catch (SQLException | InterruptedException e)
		{
			// TODO 自動生成された catch ブロック
			System.out.println(e.getCause());
		}

		return (process_count == count);
	}

	/**
	 * 指定のプロセス状態を返却
	 *
	 * @param conn
	 * @param process_id
	 * @param server_id
	 * @return
	 * @throws SQLException
	 */
	public static String GetStatusByProcess(DBConnection conn, String process_id, String server_id) throws SQLException
	{
		String res = "";
	   	PreparedStatement ps = null;
    	ResultSet rs = null;

    	if (conn == null)
    		return "";

    	String tb_process = conn.GetProps().getProperty("tb.process");

    	StringBuilder sbFindSQL = new StringBuilder();
    	sbFindSQL.append("SELECT status FROM ");
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
							sbData.append(rs.getString("status"));
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
	public static boolean UpdateProcessByProcessId(DBConnection conn, String process_id, int process_value) throws SQLException
	{
		boolean res = false;
	   	PreparedStatement ps = null;
    	ResultSet rs = null;

    	if (conn == null)
    		return false;

    	String tb_process = conn.GetProps().getProperty("tb.process");
    	//String bot_id = conn.GetProps().getProperty("id");
		SimpleDateFormat sdf = new SimpleDateFormat("YYYY/MM/DD HH:mm:ss");
    	int ret = 0; // 処理結果

    	// 追加SQL
    	StringBuilder sbFindSQL = new StringBuilder();
    	sbFindSQL.append("UPDATE ");
    	sbFindSQL.append(tb_process.toString());
    	sbFindSQL.append(" SET process=?, last_datetime=?");
    	sbFindSQL.append(" WHERE id=?");

    	try
        {
			// 更新処理
			ps = conn.getPreparedStatement(sbFindSQL.toString(), null);
			if (ps != null)
			{
				ps.clearParameters();
				ps.setInt(1, process_value);
				ps.setString(2, sdf.format(new Date()).toString());
				ps.setString(3, process_id.toString());

				ret = ps.executeUpdate(); // クエリ実行

				if (ret != 0) // 処理成功の場合
				{
					System.out.println("message add successful. [" + process_id.toString()+ "]");
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
		SimpleDateFormat sdf = new SimpleDateFormat("YYYY/MM/DD HH:mm:ss");
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
    	sbFindSQL.append(" VALUES(?,?,?,?)");

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
				ps.setString(4, sdf.format(new Date()).toString());

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
	public static String GetProcessesByServerId(DBConnection conn, String user_id, String server_id) throws SQLException
	{
		String res = "";
	   	PreparedStatement ps = null;
    	ResultSet rs = null;

    	if (conn == null)
    		return "database connect error.";

    	String tb_process = conn.GetProps().getProperty("tb.process");

    	StringBuilder sbFindSQL = new StringBuilder();
    	sbFindSQL.append("SELECT id, system_id, permissions, process FROM ");
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
					rs.beforeFirst();   //最初に戻る

					if ((number_of_row > 0) == true) // レコードが存在する場合
					{
						StringBuilder sbData = new StringBuilder();

						while (rs.next())
						{
							sbData.append(rs.getString("id"));
							sbData.append("\n");
						}

						rs.close();

						res = sbData.toString(); // 返却変数にセット

						sbData.delete(0, sbData.length());
						sbData = null;
					}
				}

				ps.close();

	        	if (AddMessageByUserId(conn, user_id, server_id) == false)
	        	{
	        		// TODO
	        	}

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
