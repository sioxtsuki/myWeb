package com.main;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
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
import com.linecorp.bot.model.event.JoinEvent;
import com.linecorp.bot.model.event.LeaveEvent;
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
import com.utility.Crypto;
import com.utility.DBConnection;
import com.utility.PasswordGenerator;

/**
 * @author shiotsuki
 *
 */
@LineMessageHandler
public class ProcessReplyMessage
{
	@Autowired
	private LineMessagingService lineMessagingService;

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
		// 入力文字を取得
		String text = event.getMessage().getText();

		// ユーザIDを取得
		String user_id = event.getSource().getUserId();

		System.out.println("UserId: " + user_id.toString());

		BotApiResponse apiResponse = null;

		try
		{
			// テキスト内容に応じたメッセージの返却
			text = com.process.Process.GetReplyMessage(text, user_id);

			// リプライ実行
			apiResponse = lineMessagingService
			    .replyMessage(new ReplyMessage(event.getReplyToken(),
			    		Collections.singletonList(new TextMessage(text.toString()))))
			    			.execute().body();
			System.out.println("Sent messages: " + apiResponse);
		}
		catch (IOException | SQLException e)
		{
			// TODO 自動生成された catch ブロック
			System.out.println(e.getCause());
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
	public void handleFollowEvent(FollowEvent event)
	{
		// ユーザIDを取得
		String user_id = event.getSource().getUserId().toString();

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
	    	sbAddSQL.append(" VALUES (?,?,?,?,?,?,NOW())");
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

					// パスワード作成
					String passwd = PasswordGenerator.GetPassword();

					// パスワード暗号化
					String crypto_passwd = Crypto.PrintBin(Crypto.Cipher(passwd));

					ps.clearParameters();
					ps.setString(1, user_id.toString()); // ユーザID
					ps.setString(2, displyName.toString()); // ディスプレイ名
					ps.setString(3, bot_id.toString()); // ボットID
					ps.setInt(4, 1); // 有効フラグ
					ps.setInt(5, 0); // 権限（初期値 = user）
					ps.setString(6, crypto_passwd.toString()); // パスワード

					ret = ps.executeUpdate();
					if (ret != 0) // 処理成功の場合
					{
						System.out.println("user add successful. [" + user_id.toString() + "]");
						res = "Welcome.\nThanking you in advance.";
						res += "\nyour password: ";
						res += passwd.toString();
					}
					ps.close();
				}
			}

			// リプライ実行
			final BotApiResponse apiResponse = lineMessagingService
			    .replyMessage(new ReplyMessage(event.getReplyToken(),
			    		Collections.singletonList(new TextMessage(res.toString()))))
			    			.execute().body();


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
				try {
					conn.getConnection().close();
				} catch (SQLException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
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

    	//String replyToken = event.getReplyToken();
		//BotApiResponse apiResponse = null;

	}

	// グループ退会
	@EventMapping
	public void handleLeaveEvent(LeaveEvent event) throws SQLException
	{

		// ユーザIDを取得
		String user_id = event.getSource().getSenderId().toString();

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

	// グループ追加
	@EventMapping
	public void handleJoinEvent(JoinEvent event)
	{
		//System.out.println("event: " + event.getSource().getSenderId());
		// グループIDを取得
		String user_id = event.getSource().getSenderId().toString();

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
	    	sbAddSQL.append(" VALUES (?,?,?,?,?,?,NOW())");
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
					String displyName = "group";
							//GetDisplayName(
							//props.getProperty("line.bot.channelToken"), user_id.toString());

					// パスワード作成
					String passwd = PasswordGenerator.GetPassword();

					// パスワード暗号化
					String crypto_passwd = Crypto.PrintBin(Crypto.Cipher(passwd));

					ps.clearParameters();
					ps.setString(1, user_id.toString()); // ユーザID
					ps.setString(2, displyName.toString()); // ディスプレイ名
					ps.setString(3, bot_id.toString()); // ボットID
					ps.setInt(4, 1); // 有効フラグ
					ps.setInt(5, 0); // 権限（初期値 = user）
					ps.setString(6, crypto_passwd.toString()); // パスワード

					ret = ps.executeUpdate();
					if (ret != 0) // 処理成功の場合
					{
						System.out.println("user add successful. [" + user_id.toString() + "]");
						res = "Welcome.\nThanking you in advance.";
						res += "\nyour password: ";
						res += passwd.toString();
					}
					ps.close();
				}
			}

			/*
			// リプライ実行
			final BotApiResponse apiResponse = lineMessagingService
			    .replyMessage(new ReplyMessage(event.getReplyToken(),
			    		Collections.singletonList(new TextMessage(res.toString()))))
			    			.execute().body();
*/

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
				try {
					conn.getConnection().close();
				} catch (SQLException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
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

    	//String replyToken = event.getReplyToken();
		//BotApiResponse apiResponse = null;
	}

	/**
	 * 上記以外のタイプ<br>
	 * TODO
	 *
	 * @param event
	 */
	@EventMapping
	public void defaultMessageEvent(Event event)
	{
		System.out.println("event: " + event.getSource().getSenderId());
		System.out.println("event: " + event);
	}
}
