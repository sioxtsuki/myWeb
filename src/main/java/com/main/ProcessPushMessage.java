package com.main;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import com.entity.RateBeans;
import com.factory.DBFactory;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import com.network.TcpClient;
import com.utility.Constants;
import com.utility.DBConnection;
import com.utility.Utility;

@LineMessageHandler
public class ProcessPushMessage
{
	private final LineMessagingClient lineMessagingClient;
	private Properties props;

	/**
	 * コンストラクタ
	 * @param _lineMessagingClient
	 */
	ProcessPushMessage(LineMessagingClient _lineMessagingClient)
	{
		this.lineMessagingClient = _lineMessagingClient;
	}

	/**
	 * @param _props
	 */
	public void SetProps(Properties _props)
	{
		this.props = _props;
	}

	/**
	 * レートチェック処理
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws SQLException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void pushMT4RateCheckProcessAlarm() throws URISyntaxException, IOException, SQLException, InterruptedException, ExecutionException, InstantiationException, IllegalAccessException
	{
    	DBConnection conn = null;
    	PreparedStatement ps = null;
    	ResultSet rs = null;
    	String text = "";
    	StringBuilder sbFindSQL = null;

    	//-------------------------------
    	// 配信を許可しない場合は処理中断
    	//-------------------------------
    	Properties conf_props = new Properties();
    	conf_props.load(new FileInputStream(Constants.CONF_PROP_PATH));
    	if (conf_props.getProperty("ratechk.allow").toString().equals("0") == true)
    	{
    		return;
    	}

		try
		{
			conn = DBFactory.getConnection(this.props);

			// MT4からレート情報を取得
			TcpClient client = new TcpClient();
			client.setHost(this.props.getProperty("tcp.server.ip"));
			client.setPort(Integer.parseInt(props.getProperty("tcp.server.port")));

			// コマンド
			String command = this.props.getProperty("rate.command"); //"RATECHK MASTER=mt4awk113|";

			ArrayList<RateBeans> rates = new ArrayList<RateBeans>();

			//-------------------------
			// 返却文字列作成
			//-------------------------
			switch (client.run(command, rates))
			{
			case PT_SUCCESS: // 処理成功

				StringBuilder sb = new StringBuilder();

				int success_count = 0;
				int beans_count = 0;
				for (RateBeans beans : rates)
				{
					// 該当レコードが存在する場合
					// ヘッダーをセット
					if (success_count == 0) // 最初レコードの場合
						sb.append("■rate alert\r\n");

					if (success_count == 4)
					{
						sb.append("...");
						break;
					}

					// ディティールをセット
					sb.append(beans.getSymbol() + " : "
							+ beans.getInterval() + " 秒\r\n");
					success_count++;

				}
				text = sb.toString();
				break;

			case PT_NETWORK_ERROR: // ネットワークエラー
				text = "network error.";
				break;

			case PT_EXCEPTION_ERROR: // 例外エラー
				text = "exception error.";
				break;

			case PT_ERROR: // 通常エラー
				text = "command error.";
				break;

			default:
				text = ""; // TODO
				break;

			}

			//System.out.println(text);

			//text = Utility.IsRate_Proess(conn);
	    	if (text.isEmpty() == false)
	    	{
		    	// BOT_IDを取得
				String bot_id = this.props.getProperty("id").toString();

				// ユーザ情報を取得
				String tb_user = conn.GetProps().getProperty("tb.user");
		    	sbFindSQL = new StringBuilder();
				sbFindSQL.delete(0, sbFindSQL.length());
				sbFindSQL.append("SELECT user_id FROM ");
				sbFindSQL.append(tb_user.toString());
				sbFindSQL.append(" WHERE permissions =? AND bot_id=?");

				ps = conn.getPreparedStatement(sbFindSQL.toString(), null);
				if (ps != null)
				{
					ps.clearParameters();
					ps.setInt(1, 1);
					ps.setString(2, bot_id.toString());
					rs = ps.executeQuery();
					if (rs != null)
					{
						// ラインへプッシュ
						while (rs.next())
						{
							String user_id = rs.getString("user_id").toString();

							//System.out.println(user_id.trim().toString());
				        	@SuppressWarnings("unused")
							final BotApiResponse response = this.lineMessagingClient
				                                            .pushMessage(new PushMessage(user_id.toString(),
				                                                         new TextMessage(text.toString()
				 //                                                        new ConfirmTemplate("ごみ捨ては終わった？",
				 //                                                        new MessageAction("はい", "はい"),
				 //                                                        new MessageAction("いいえ", "いいえ")
				                                                          ))).get();
						}

						rs.close();
						rs = null;
					}

					ps.close();
					ps = null;
				}
	    	}
		}
        finally
        {
        	if (conn != null)
        	{
        		conn.getConnection().close();
        		conn = null;
        	}

        	if (sbFindSQL != null)
        	{
        		sbFindSQL.delete(0, sbFindSQL.length());
    			sbFindSQL= null;
        	}
        }
	}

    /**
     * @throws URISyntaxException
     * @throws IOException
     * @throws SQLException
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public void pushBurnablesAlarm() throws URISyntaxException, IOException, SQLException, InterruptedException, ExecutionException, InstantiationException, IllegalAccessException
    {
    	DBConnection conn = null;
    	PreparedStatement ps = null;
    	ResultSet rs = null;
    	String text = "";
    	StringBuilder sbFindSQL = null;

		try
		{
			conn = DBFactory.getConnection(this.props);

			text = Utility.IsRate_Proess(conn);
	    	if (text.isEmpty() == false)
	    	{
		    	// BOT_IDを取得
				String bot_id = this.props.getProperty("id").toString();

				// ユーザ情報を取得
				String tb_user = conn.GetProps().getProperty("tb.user");
		    	sbFindSQL = new StringBuilder();
				sbFindSQL.delete(0, sbFindSQL.length());
				sbFindSQL.append("SELECT user_id FROM ");
				sbFindSQL.append(tb_user.toString());
				sbFindSQL.append(" WHERE permissions =? AND bot_id=?");

				ps = conn.getPreparedStatement(sbFindSQL.toString(), null);
				if (ps != null)
				{
					ps.clearParameters();
					ps.setInt(1, 1);
					ps.setString(2, bot_id.toString());
					rs = ps.executeQuery();
					if (rs != null)
					{
						// ラインへプッシュ
						while (rs.next())
						{
							String user_id = rs.getString("user_id").toString();

							//System.out.println(user_id.trim().toString());
				        	@SuppressWarnings("unused")
							final BotApiResponse response = this.lineMessagingClient
				                                            .pushMessage(new PushMessage(user_id.toString(),
				                                                         new TextMessage(text.toString()
				 //                                                        new ConfirmTemplate("ごみ捨ては終わった？",
				 //                                                        new MessageAction("はい", "はい"),
				 //                                                        new MessageAction("いいえ", "いいえ")
				                                                          ))).get();
						}

						rs.close();
						rs = null;
					}

					ps.close();
					ps = null;
				}
	    	}
		}
        finally
        {
        	if (conn != null)
        	{
        		conn.getConnection().close();
        		conn = null;
        	}

        	if (sbFindSQL != null)
        	{
        		sbFindSQL.delete(0, sbFindSQL.length());
    			sbFindSQL= null;
        	}
        }
    }
}
