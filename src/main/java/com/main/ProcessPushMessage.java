package com.main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
	 * 処理実行
	 *
	 * @param port
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws SQLException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unused")
	private void Process(String port, String name)throws URISyntaxException, SQLException, InterruptedException, ExecutionException, InstantiationException, IllegalAccessException
	{
    	DBConnection conn = null;
    	PreparedStatement ps = null;
    	ResultSet rs = null;
    	String text = "";
    	StringBuilder sbFindSQL = null;

		try
		{
			conn = DBFactory.getConnection(this.props);

			// MT4からレート情報を取得
			TcpClient client = new TcpClient();
			client.setHost(this.props.getProperty("tcp.server.ip"));
			client.setPort(Integer.parseInt(port)); // props.getProperty("tcp.server.port")));

			// コマンド
			String command = this.props.getProperty("rate.command"); //"RATECHK MASTER=mt4awk113|";

			ArrayList<RateBeans> rates = new ArrayList<RateBeans>();

			ArrayList<String> alInfo = new ArrayList<String>();

			StringBuilder sb = new StringBuilder();

			//-------------------------
			// 返却文字列作成
			//-------------------------
			Constants.PROCESS_TYPE process_type = client.run(command, alInfo, rates); // TCPを実行

			switch (process_type) // 処理タイプを判定
			{
			case PT_SUCCESS: // 処理成功

				int success_count = 0;
				//int beans_count = 0;
				for (RateBeans beans : rates)
				{
					// 該当レコードが存在する場合
					if (success_count == 0) // 最初レコードの場合
					{
						// ヘッダーをセット
						sb.append("■rate alert\r\n");
						// サーバー名をセット
						sb.append("server: " + name.toString() + "\r\n");
						if (alInfo.get(0).equals("1"))
						{
							sb.append("Stop all symbol rates.");
							break;
						}
					}

					//---------------------------
					// ディティールをセット
					//---------------------------
					// 対象シンボルが5件以上の場合、５件目から『...』 表示
					if (success_count == 4)
					{
						sb.append("...");
						break;
					}

					// 対象シンボルが4件以内の場合、シンボル名 + 停止秒数を表示
					sb.append(beans.getSymbol() + " : "
							+ beans.getInterval() + " 秒\r\n");
					success_count++;

				}
				text = sb.toString();
				break;

			case PT_NETWORK_ERROR: // ネットワークエラー
				sb.append("■network disconnection\r\n");
				sb.append("server: " + name.toString());
				text = sb.toString();
				break;

			case PT_EXCEPTION_ERROR: // 例外エラー
				sb.append("■exception error\r\n");
				sb.append("server: " + name.toString());
				text = sb.toString();
				break;

			case PT_ERROR: // 通常エラー
				sb.append("■connection error\r\n");
				sb.append("server: " + name.toString());
				text = sb.toString();
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
				sbFindSQL.append("SELECT user_id, authority FROM ");
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

							//------------------------------------------------------------
							// Add By 2019.04.24
							// アドミン権限以外の場合、通常エラーの場合、処理中断
							int authority = rs.getInt("authority");
							if (authority != 1) // アドミン権限以外の場合
							{
								if (process_type == Constants.PROCESS_TYPE.PT_ERROR)
								{
									continue;
								}
							}
							//------------------------------------------------------------

							//System.out.println(user_id.trim().toString());
				        	//@SuppressWarnings("unused")
							final BotApiResponse response = this.lineMessagingClient
				                                            .pushMessage(new PushMessage(user_id.toString(),
				                                                         new TextMessage(text.toString()
				 //                                                        new ConfirmTemplate("ごみ捨ては終わった？",
				 //                                                        new MessageAction("はい", "はい"),
				 //                                                        new MessageAction("いいえ", "いいえ")
				                                                          ))).get();
				        	System.out.println(response.toString());
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
	 * レートチェック処理
	 *
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws URISyntaxException
	 * @throws SQLException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public void pushMT4RateCheckProcessAlarm() throws FileNotFoundException, IOException, InstantiationException, IllegalAccessException, URISyntaxException, SQLException, InterruptedException, ExecutionException
	{

		// プロパティ情報を取得
    	Properties conf_props = new Properties();
    	conf_props.load(new FileInputStream(Constants.CONF_PROP_PATH));
    	if (conf_props.getProperty("ratechk.allow").toString().equals("0") == true) // 配信を許可しない場合は処理中断
    	{
    		return;
    	}

    	int count = 0;
    	// ポート情報を取得
    	String ports[] = props.getProperty("tcp.server.port").split(",");
    	String names[] = props.getProperty("tcp.server.name").split(",");
    	for (String port : ports) // ポート数分処理実行
    	{
    		if (port.trim().isEmpty()) continue; // 空の場合スルー
    		String name = names[count];
        	this.Process(port, name); // 処理実行
        	count++;
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
				        	//@SuppressWarnings("unused")
							final BotApiResponse response = this.lineMessagingClient
				                                            .pushMessage(new PushMessage(user_id.toString(),
				                                                         new TextMessage(text.toString()
				 //                                                        new ConfirmTemplate("ごみ捨ては終わった？",
				 //                                                        new MessageAction("はい", "はい"),
				 //                                                        new MessageAction("いいえ", "いいえ")
				                                                          ))).get();
				        	System.out.println(response.toString());
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
