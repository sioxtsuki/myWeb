package com.utility;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Utility
{
	/**
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

    	return res;	}

	/**
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
    		return "";

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
