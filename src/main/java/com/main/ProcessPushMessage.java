package com.main;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import com.factory.DBFactory;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
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
