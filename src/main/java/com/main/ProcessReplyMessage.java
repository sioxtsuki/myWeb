package com.main;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import com.factory.DBFactory;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.client.LineMessagingClientImpl;
import com.linecorp.bot.client.LineMessagingService;
import com.linecorp.bot.client.LineMessagingServiceBuilder;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.UnfollowEvent;
import com.linecorp.bot.model.event.message.AudioMessageContent;
import com.linecorp.bot.model.event.message.ImageMessageContent;
import com.linecorp.bot.model.event.message.StickerMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.message.VideoMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import com.utility.Constants;
import com.utility.DBConnection;
import com.utility.Utility;

@LineMessageHandler
public class ProcessReplyMessage
{
	@Autowired
	private LineMessagingService lineMessagingService;

	// プライベート文字列
	private String[] strPrivates = {"kerberos", "よし","ケルベロス","おいで",",お手", "お座り","塩月", "ごはん","長谷川"};
	private String[] strPermissions = { "塩月", "ごはん"};


	/**
	 * ディスプレイ名を返却
	 *
	 * @param channelToken
	 * @param user_id
	 * @return
	 */
	private String GetDisplayName(String channelToken, String user_id)
	{
		String displayName = "";
		UserProfileResponse profile;

		try
		{
			final LineMessagingClient lineMessagingClient = new LineMessagingClientImpl(
					LineMessagingServiceBuilder
							.create(channelToken.toString())
							.build()
					);

			// プロフィールを取得
			profile = lineMessagingClient.getProfile(user_id.toString()).get();

			// ディスプレイ名を取得
			displayName = profile.getDisplayName();

		}
		catch (InterruptedException e)
		{
			// TODO 自動生成された catch ブロック
			System.out.println(e.getCause());
		}
		catch (ExecutionException e)
		{
			// TODO 自動生成された catch ブロック
			System.out.println(e.getCause());
		}

		return displayName;
	}


	/**
	 * テキストタイプのメッセージ応答処理
	 *
	 * @param event
	 * @throws Exception
	 */
	@EventMapping
	public void handleTextMessageEvent(MessageEvent<TextMessageContent> event)
	{
		DBConnection conn = null;
		StringBuilder sb = new StringBuilder();

		// 入力文字を取得
		String text = event.getMessage().getText();

		// ユーザIDを取得
		String user_id = event.getSource().getUserId();

		System.out.println("UserId: " + user_id.toString());

		// プロセス結果
		String ret_process = "process successful.";

		// プライベート判定フラグ
		boolean IsPrivate = false;
		boolean IsPermissions = false;

		//---------------------------
		// リプライ処理
		//---------------------------
		try
		{
			// @プライベート文字列判定
			for (String strPrivate : strPrivates)
			{
				if (text.equals(strPrivate))
				{
					for (String strPermission : strPermissions)
					{
						if (text.equals(strPermission))
						{
							IsPermissions = true;
							break;
						}
					}
					IsPrivate = true;
					break;
				}
			}

			if (IsPrivate == true )
			{
				//*********************************
				// プライベート文字列の場合
				//*********************************
				text = "ガルルw";
				if (IsPermissions == true)
					text = "ワン!";
			}
			else
			{
				//*********************************
				// 業務文字列の場合
				//*********************************
				if (Utility.CompareString(text, "symbols") == true)
				{
					// @登録シンボル名情報を取得
					conn = Utility.GetConn();
					text = Utility.GetSymbols(conn);
				}
				else if (Utility.StartsWithString(text, "symbol=") == true)
				{
					// @指定シンボルの現在のTick情報を取得
					String[] symbols = text.split("=");

					if (symbols.length < 2)
					{
						text = "invalid symbol.";
					}
					else
					{
						conn = Utility.GetConn();
			        	text = Utility.GetRate(conn, symbols[1].trim().toString());
					}
				}
				else if (Utility.CompareString(text,"check") == true)
				{
					// @現在の接続状況
					conn = Utility.GetConn();
					text = Utility.IsRate_Proess(conn);

					if (text.isEmpty() == true) // 返却結果が空の場合
						text = "fine.";
				}
				else if (Utility.CompareString(text, "operations") == true)
				{
					// @アプリケーション操作:サーバー選択
					conn = Utility.GetConn();
					sb.append("Which server would you like to select ?\n");
					sb.append("\n");
					sb.append(Utility.GetServer(conn));
					sb.append("\n");
					sb.append("※ An example）server=ID\n");

					text = sb.toString();
				}
				else if (Utility.StartsWithString(text, "server=") == true)
				{
					// @指定サーバーのアプリを制御
					String[] servers = text.split("=");

					if (servers.length < 2)
					{
						text = "invalid server.";
					}
					else
					{
						String server_id = servers[1].trim().toString();
						conn = Utility.GetConn();
			        	text = Utility.GetProcessesByServerId(conn, user_id.toString(), server_id.toString(),
			        			new String[]{"id", "status"});

			        	//
						sb.append("< Operations Recive Commands >\n");
						sb.append("１）run ---> Run specified application.\n");
						sb.append("２）close ---> Activate the specified application.\n");
						sb.append("３）info ---> Return application operation info.\n");
						//sb.append("３）reboot・・・Restart specified application.\n");
						sb.append("\n");
						sb.append("■Apps\n");
						sb.append(Utility.EditAppsText(text));
						sb.append("\n");
						sb.append("※ An example）command=Apps,・・・\n");
						text = sb.toString();
					}
				}
				else if (Utility.StartsWithString(text, "run=") == true)
				{
					// @アプリケーション操作:アプリ実行
					conn = Utility.GetConn();
					if (Utility.ProcessExecute(conn, 0, user_id.toString(), text) == false)
					{
						ret_process = "process failed.";
					}
					text = ret_process;
				}
				else if (Utility.StartsWithString(text, "close=") == true)
				{
					// @アプリケーション操作:アプリ終了
					conn = Utility.GetConn();
					if (Utility.ProcessExecute(conn, 1, user_id.toString(), text) == false)
					{
						ret_process = "process failed.";
					}
					text = ret_process;
				}
				else if (Utility.StartsWithString(text, "info=") == true)
				{
					// @アプリケーション操作:アプリ稼働状態
					conn = Utility.GetConn();
					text = Utility.GetProcessInfo(conn, user_id.toString(), text);
				}
				else
				{
					// @メニュー
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

					text = sb.toString();
					// 上記以外の場合、オウム返し
//					text = event.getMessage().getText();
				}
			}

			// リプライ実行
			final BotApiResponse apiResponse = lineMessagingService
			    .replyMessage(new ReplyMessage(event.getReplyToken(),
			    		Collections.singletonList(new TextMessage(text.toString()))))
			    			.execute().body();

			System.out.println("Sent messages: " + apiResponse);
		}
		catch(Exception e)
		{
			System.out.println(e.getCause());
		}
		finally
		{
			if (conn != null)
			{
				try
				{
					conn.getConnection().close();
					conn = null;
				}
				catch (SQLException e)
				{
					// TODO 自動生成された catch ブロック
					System.out.println(e.getCause());
				}
			}
			sb.delete(0, sb.length());
			sb = null;
		}
	}

	/**
	 * テキストタイプのメッセージ応答処理
	 *
	 * @param event
	 * @throws Exception
	 */
	/*
	@EventMapping
	public Message handleTextMessageEvent(MessageEvent<TextMessageContent> event) {

		System.out.println("event: " + event);
		System.out.println(new TextMessage(event.getMessage().getText()));
		return new TextMessage(event.getMessage().getText());
	}
*/
	/**
	 * スタンプタイプのメッセージ応答処理<br>
	 * TODO
	 *
	 * @param event
	 * @return
	 */
	@EventMapping
	public Message handleStickerMessage(MessageEvent<StickerMessageContent> event) {
		return new TextMessage("スタンプ送信");
	}

	/**
	 * 画像タイプのメッセージ応答処理<br>
	 * TODO
	 *
	 * @param event
	 * @return
	 */
	@EventMapping
	public Message handleImageMessage(MessageEvent<ImageMessageContent> event) {
		return new TextMessage("画像送信");
	}

	/**
	 * 動画タイプのメッセージ応答処理<br>
	 * TODO
	 *
	 * @param event
	 * @return
	 */
	@EventMapping
	public Message handleVideoMessage(MessageEvent<VideoMessageContent> event) {
		return new TextMessage("動画送信");
	}

	/**
	 * 音声タイプのメッセージ応答処理<br>
	 * TODO
	 *
	 * @param event
	 * @return
	 */
	@EventMapping
	public Message handleAudioMessage(MessageEvent<AudioMessageContent> event) {
		return new TextMessage("音声送信");
	}

	/**
	 * 友達解除処理<br>
	 * TODO
	 *
	 * @param event
	 * @return
	 * @throws SQLException
	 */
	@EventMapping
	public void handleUnfollowEvent(UnfollowEvent event) throws SQLException
	{
		// ユーザIDを取得
		String user_id = event.getSource().getUserId().toString();

    	DBConnection conn = null;
		PreparedStatement ps = null;
		Resource resource = null;
		Properties props = null;

    	try
    	{
    		// データベースへ追加
        	resource = new ClassPathResource(Constants.PROP_PATH);
			props = PropertiesLoaderUtils.loadProperties(resource);
			conn = DBFactory.getConnection(props);

			// BOT_IDを取得
			String bot_id = props.getProperty("id").toString();

			// 削除SQL
			String tb_user = props.getProperty("tb.user");

	    	StringBuilder sbDelSQL = new StringBuilder();
	    	sbDelSQL.append("DELETE FROM ");
	    	sbDelSQL.append(tb_user.toString());
	    	sbDelSQL.append(" WHERE user_id=? AND bot_id=?");
	    	//sbDelSQL.append("user_id='" + user_id.toString() + "'");
	    	//sbDelSQL.append(" AND ");
	    	//sbDelSQL.append("bot_id='" + bot_id.toString() + "'");

			if (conn != null)
			{
				// 削除処理
				ps = conn.getPreparedStatement(sbDelSQL.toString(), null);
				if (ps != null)
				{
					ps.clearParameters();
					ps.setString(1, user_id.toString());
					ps.setString(2, bot_id.toString());
					int ret = ps.executeUpdate();
					if (ret == 1)
					{
						System.out.println("user delete successful. [" + user_id.toString() + "]");
					}
					ps.close();
				}
			}

    	} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			System.out.println(e.getCause());
		} catch (InstantiationException e) {
			// TODO 自動生成された catch ブロック
			System.out.println(e.getCause());
		} catch (IllegalAccessException e) {
			// TODO 自動生成された catch ブロック
			System.out.println(e.getCause());
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			System.out.println(e.getCause());
		} finally{
			if (conn != null)
			{
				conn.getConnection().close();
				conn = null;
			}
			if (ps != null)
			{
				ps = null;
			}
			if (resource != null)
			{
				resource = null;
			}
			if (props != null)
			{
				props = null;
			}
		}
	}

	/**
	 * 友達追加のメッセージ応答処理<br>
	 * TODO
	 *
	 * @param event
	 * @return
	 * @throws SQLException
	 */
	@EventMapping
	public Message handleFollowEvent(FollowEvent event) throws SQLException
	{
		// ユーザIDを取得
		String user_id = event.getSource().getUserId().toString();

		SimpleDateFormat sdf = new SimpleDateFormat("YYYY/MM/DD HH:mm:ss");


    	String res = "Signup failed.\nPlease apply again.";
    	DBConnection conn = null;
    	Resource resource = null;
    	Properties props = null;
		PreparedStatement ps = null;

    	try
    	{
        	resource = new ClassPathResource(Constants.PROP_PATH);
			props = PropertiesLoaderUtils.loadProperties(resource);

    		// データベースへ追加
			conn = DBFactory.getConnection(props);
			int ret = 0; // 処理結果

			// BOT_IDを取得
			String bot_id = props.getProperty("id").toString();

			// 削除SQL
			String tb_user = props.getProperty("tb.user");
	    	StringBuilder sbDelSQL = new StringBuilder();
	    	sbDelSQL.append("DELETE FROM ");
	    	sbDelSQL.append(tb_user.toString());
	    	sbDelSQL.append(" WHERE user_id=? AND bot_id=?");


			// 追加SQL
	    	StringBuilder sbAddSQL = new StringBuilder();
	    	sbAddSQL.append("INSERT INTO ");
	    	sbAddSQL.append(tb_user.toString());
	    	sbAddSQL.append(" VALUES (?,?,?,?,?)");
	    	//sbAddSQL.append("'" + user_id.toString() + "',");
	    	//sbAddSQL.append("'" + bot_id.toString() + "',");
	    	//sbAddSQL.append("1,");
	    	//sbAddSQL.append("'" + sdf.format(new Date()).toString() + "')");


			if (conn != null)
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
				ps = conn.getPreparedStatement(sbAddSQL.toString(), null);
				if (ps != null)
				{
					// ディスプレイ名を取得
					String displyName = GetDisplayName(
							props.getProperty("line.bot.channelToken"), user_id.toString());

					ps.clearParameters();
					ps.setString(1, user_id.toString());
					ps.setString(2, displyName.toString());
					ps.setString(3, bot_id.toString());
					ps.setInt(4, 1);
					ps.setString(5, sdf.format(new Date()).toString());
					ret = ps.executeUpdate();
					if (ret != 0) // 処理成功の場合
					{
						System.out.println("user add successful. [" + user_id.toString() + "]");
						res = "Welcome.\nThanking you in advance.";
					}
					ps.close();
				}
			}

    	} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			System.out.println(e.getCause());
		} catch (InstantiationException e) {
			// TODO 自動生成された catch ブロック
			System.out.println(e.getCause());
		} catch (IllegalAccessException e) {
			// TODO 自動生成された catch ブロック
			System.out.println(e.getCause());
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			System.out.println(e.getCause());
		} finally{
			if (conn != null)
			{
				conn.getConnection().close();
				conn = null;
			}
			if (ps != null)
			{
				ps = null;
			}
			if (resource != null)
			{
				resource = null;
			}
			if (props != null)
			{
				props = null;
			}
		}


		return new TextMessage(res.toString());
	}

	/**
	 * 上記以外のタイプ<br>
	 * TODO
	 *
	 * @param event
	 */
	@EventMapping
	public void defaultMessageEvent(Event event) {
		System.out.println("event: " + event);
	}
}
