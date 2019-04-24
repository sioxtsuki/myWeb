package com.process;

import java.sql.SQLException;
import java.util.ArrayList;

import com.entity.MenuBeans;
import com.entity.UserBeans;
import com.utility.DBConnection;
import com.utility.Utility;

/**
 * @author shiotsuki
 *
 * 処理クラス
 */
public class Process
{
	// プライベート文字列
	private static final String[] strPrivates = {"kerberos", "よし","ケルベロス","おいで",",お手", "お座り","塩月", "ごはん","長谷川"};
	private static final String[] strPermissions = { "塩月", "ごはん"};

	/**
	 * テキスト内容に応じたメッセージの返却
	 *
	 * @param text
	 * @param user_id
	 * @return
	 * @throws SQLException
	 */
	public static String GetReplyMessageEx(String text, String user_id) throws SQLException
	{
		String res = "";
		DBConnection conn = null;
		ArrayList<MenuBeans> menus = null;

		try
		{
			// ユーザ情報を取得
			UserBeans user = Utility.GetUserInfo(conn, user_id);
			// DBコネクションを取得
			conn = Utility.GetConn();
			// メニューリストを取得
			menus = Utility.GetMenuInfo(conn, -1, user.getAuthority());

			for (MenuBeans menu: menus)
			{
				if (menu.getType() == 1) // ディティールの場合
				{
					if (Utility.StartsWithString(text, menu.getName()) == true)
					{

					}
				}
			}


		}
		catch(Exception e)
		{
			res = e.getCause().getMessage().toString();
		}
		finally
		{
			if (menus != null)
			{
				menus.clear();
				menus = null;
			}

			if (conn != null)
			{
				try
				{
					conn.getConnection().close();
					conn = null;
				}
				catch (SQLException e)
				{
					res = e.getCause().getMessage().toString();
					System.out.println(e.getCause());
				}
			}
		}

		return res;
	}

	/**
	 * テキスト内容に応じたメッセージの返却
	 *
	 * @param tmpText
	 * @param user_id
	 * @return
	 * @throws SQLException
	 */
	public static String GetReplyMessage(String text, String user_id) throws SQLException
	{
		// 返却結果
		String tmpText = text.trim().toString();

		// スペースを除去
		tmpText = tmpText.replaceAll(" ", "");
		tmpText = tmpText.replaceAll("　", "");

		// 返却結果
		String res = "";
		// プロセス結果
		String ret_process = "process successful.";
		// プライベート判定フラグ
		boolean IsPrivate = false;
		// プライベート許容判定フラグ
		boolean IsPermissions = false;
		// DBコネクション
		DBConnection conn = null;

		//----------------------------
		// プライベート文字列判定
		//----------------------------
		for (String strPrivate : strPrivates)
		{
			if (tmpText.equals(strPrivate))
			{
				for (String strPermission : strPermissions)
				{
					if (tmpText.equals(strPermission))
					{
						IsPermissions = true;
						break;
					}
				}
				IsPrivate = true;
				break;
			}
		}

		StringBuilder sb = new StringBuilder();

		try
		{
			/*
			if (IsPrivate == true )
			{
				//*********************************
				// プライベート文字列の場合
				//*********************************
				res = "ガルルw";
				if (IsPermissions == true)
					res = "ワン!";
			}
			else
			{
				//*********************************
				// 業務文字列の場合
				//*********************************
				if (Utility.CompareString(tmpText, "symbols") == true)
				{
					// @登録シンボル名情報を取得
					conn = Utility.GetConn();
					res = Utility.GetSymbols(conn);
				}
				*/
				if (Utility.CompareString(tmpText, "stop") == true) // レートチェック停止
				{
					res = Utility.RateCheckProcess(0);
				}
				else if (Utility.CompareString(tmpText, "start") == true) // レートチェック開始
				{
					res = Utility.RateCheckProcess(1);
				}
				else if (Utility.CompareString(tmpText, "state") == true) // レートチェック稼働状況確認
				{
					// レートチェック状態確認
					res = Utility.RateCheckStateProcess();
				}
				/*
				else if (Utility.CompareString(tmpText, "check") == true) // レート配信確認
				{
					// レートチェック状態確認
					res = Utility.RateCheckStateProcess();
				}
				else if (Utility.StartsWithString(tmpText, "symbol=") == true)
				{
					//*********************************
					// @指定シンボルの現在のTick情報を取得
					//*********************************
					String[] symbols = tmpText.split("=");

					if (symbols.length < 2)
					{
						res = "invalid symbol.";
					}
					else
					{
						conn = Utility.GetConn();
						res = Utility.GetRate(conn, symbols[1].trim().toString());
					}
				}
				else if (Utility.CompareString(tmpText,"check") == true)
				{
					//*********************************
					// @現在の接続状況
					//*********************************
					conn = Utility.GetConn();
					res = Utility.IsRate_Proess(conn);

					if (res.isEmpty() == true) // 返却結果が空の場合
						res = "fine.";
				}
				else if (Utility.CompareString(tmpText, "operations") == true)
				{
					//*********************************
					// @アプリケーション操作:サーバー選択
					//*********************************
					conn = Utility.GetConn();
					sb.append("Which server would you like to select ?\n");
					sb.append("\n");
					sb.append(Utility.GetServer(conn));
					sb.append("\n");
					sb.append("※ An example）server=ID\n");

					res = sb.toString();
				}
				else if (Utility.StartsWithString(tmpText, "server=") == true)
				{
					//*********************************
					// @指定サーバーのアプリを制御
					//*********************************
					String[] servers = tmpText.split("=");

					if (servers.length < 2)
					{
						res = "invalid server.";
					}
					else
					{
						String server_id = servers[1].trim().toString();
						conn = Utility.GetConn();
						res = Utility.GetProcessesByServerId(conn, user_id.toString(), server_id.toString(),
			        			new String[]{"id", "status"});

						// データベースへメッセージを登録
			        	if (Utility.AddMessageByUserId(conn, user_id, server_id) == true)
			        	{
							sb.append("< Operations Recive Commands >\n");
							sb.append("１）start\n");
							sb.append("    ---> Run specified application.\n");
							sb.append("\n");
							sb.append("２）stop\n");
							sb.append("    ---> Activate the specified application.\n");
							sb.append("\n");
							sb.append("３）restart\n");
							sb.append("    ---> Restart the application.\n");
							sb.append("\n");
							sb.append("４）info\n");
							sb.append("    ---> Return application operation info.\n");
							sb.append("\n");
							sb.append("■Apps\n");
							sb.append(Utility.EditAppsText(res));
							sb.append("\n");
							sb.append("※ An example）command=Apps,・・・\n");
							res = sb.toString();
			        	}

					}
				}
				else if (Utility.StartsWithString(tmpText, "start=") == true)
				{
					//*********************************
					// @アプリケーション操作:アプリ実行
					//*********************************
					conn = Utility.GetConn();
					if (Utility.ProcessExecute(conn, 0, user_id.toString(), tmpText) == false)
					{
						ret_process = "process failed.";
					}
					res += Utility.G_STR_START_VALUE;
					res += " ";
					res += ret_process;
				}
				else if (Utility.StartsWithString(tmpText, "stop=") == true)
				{
					//*********************************
					// @アプリケーション操作:アプリ終了
					//*********************************
					conn = Utility.GetConn();
					if (Utility.ProcessExecute(conn, 1, user_id.toString(), tmpText) == false)
					{
						ret_process = "process failed.";
					}
					res += Utility.G_STR_STOP_VALUE;
					res += " ";
					res += ret_process;
				}
				else if (Utility.StartsWithString(tmpText, "restart=") == true)
				{
					//*********************************
					// @アプリケーション操作:アプリ再起動
					//*********************************
					conn = Utility.GetConn();
					if (Utility.ProcessExecute(conn, 1, user_id.toString(), tmpText) == false) // アプリ停止
					{
						ret_process = "process failed.";
					}
					if (ret_process.compareTo("process successful.") == 0) // 成功の場合
					{
						if (Utility.ProcessExecute(conn, 0, user_id.toString(), tmpText) == false) // アプリ起動
						{
							ret_process = "process failed.";
						}
					}
					res += Utility.G_STR_RESTART_VALUE;
					res += " ";
					res += ret_process;
				}
				else if (Utility.StartsWithString(tmpText, "info=") == true)
				{
					//*********************************
					// @アプリケーション操作:アプリ情報
					//*********************************
					conn = Utility.GetConn();
					res = Utility.GetProcessInfo(conn, user_id.toString(), tmpText);
				}
				else if (Utility.CompareString(tmpText, "user") == true)
				{
					//*********************************
					// @ユーザ情報
					//*********************************
					conn = Utility.GetConn();
					UserBeans user = Utility.GetUserInfo(conn, user_id.toString());

					sb.append("■Information");
					sb.append("\npasswd: ");
					sb.append(user.getPasswd());
					sb.append("\nauthority: ");
					sb.append(user.getAuthority_name());

					res = sb.toString();
				}
				else
				{
					//*********************************
					// @メニュー
					//*********************************
					sb.append("< Recive Commands >\n");
					sb.append("１）symbols\n");
					sb.append("    ---> Return the registered symbol list.\n");
					sb.append("\n");
					sb.append("２）symbol=Symbol name\n");
					sb.append("    ---> Return tick information of designated symbol.\n");
					sb.append("\n");
					sb.append("３）check\n");
					sb.append("    ---> Return check contents of communication status.\n");
					sb.append("\n");
					sb.append("４）operations\n");
					sb.append("    ---> Perform application operations.\n");
					sb.append("\n");
					sb.append("５）user\n");
					sb.append("    ---> Return user information.\n");

					res = sb.toString();
					// 上記以外の場合、オウム返し
	//				text = event.getMessage().getText();
				}
	//		}
	 */
		}
		catch(Exception e)
		{
			res = e.getCause().getMessage().toString();
			System.out.println(e.getCause());
		}
		finally
		{
			sb.delete(0, sb.length());
			sb = null;
			if (conn != null)
			{
				try
				{
					conn.getConnection().close();
					conn = null;
				}
				catch (SQLException e)
				{
					res = e.getCause().getMessage().toString();
					System.out.println(e.getCause());
				}
			}
		}

		return res.toString();
	}

}
