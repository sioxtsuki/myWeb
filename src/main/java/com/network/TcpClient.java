package com.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import com.entity.RateBeans;
import com.utility.Constants;


public class TcpClient// implements Runnable
{
    //接続先ホスト名(今回はローカルホスト)
    //ここにサーバのIPアドレスを指定します．普通はハードコーディングせずに外部入力から設定します．
    //例)　192.xxx.yyy.z --> "192.xxx.yyy.z"
    public String host;
    public void setHost(String host) {
		this.host = host;
	}
    //接続先ポート番号(サーバー側で設定したものと同じもの)
    //ポート番号をクライアントとサーバ間で一致させてないと通信できませんよ！
    public int port;
    public void setPort(int port) {
		this.port = port;
	}

    //+------------------------------------------
	//| Runメソッドの実装
    //+------------------------------------------
    public Constants.PROCESS_TYPE run(String command, ArrayList<String> info, ArrayList<RateBeans> rates)
    {
    	//ArrayList<RateBeans> rates = new ArrayList<RateBeans>();
		Socket socket = null;//ソケット

		Constants.PROCESS_TYPE  pt_type = Constants.PROCESS_TYPE .PT_SUCCESS;
		int count = 0;

		try {
			ArrayList<String> list = new ArrayList<String>();

			socket = new Socket(this.host , this.port); //接続

			OutputStream os = socket.getOutputStream();

			InputStream sok_in = socket.getInputStream();
			InputStreamReader sok_isr = new InputStreamReader(sok_in);
			BufferedReader sok_br = new BufferedReader(sok_isr);

			//String send = "RATECHK MASTER=mt4awk113|";	//キー1行入力
			os.write(command.getBytes());//送信

			while(true)
			{
				String recive = sok_br.readLine();

				if (recive == null) break;

				if (count == 0) // 最初のデータの場合
				{
					if (recive.equals("Success") == false) // エラーの場合は処理中断
					{
						pt_type = Constants.PROCESS_TYPE.PT_ERROR;
						break;
					}
					count++;
					continue;
				}
				/*
				 * 削除
				if (count == 1) // ２件目のデータの場合（サーバー名）
				{
					info.add(recive);
					count++;
					continue;
				}
				*/
				if (count == 1) // ３件目のデータの場合（種別）0: 個別、1：全体
				{
					info.add(recive);
					count++;
					continue;
				}

				// ４件目以降のデータの場合（シンボル）
				String values[] = recive.split("\\|");

				if (values.length >= 4)
				{
					// ブランクが存在するため、リストに洗い替え
					list.clear();
					for (String val : values)
					{
						if (val.toString().trim().isEmpty() == true) continue;
						list.add(val);
					}

					// オブジェクトへ洗い替え
					RateBeans rate = new RateBeans();
					rate.setSymbol(list.get(0).toString());
					rate.setBid(Double.parseDouble(list.get(1).toString()));
					rate.setAsk(Double.parseDouble(list.get(2).toString()));
					rate.setCtm(Long.parseLong(list.get(3).toString()));
					rate.setInterval(Integer.parseInt(list.get(4).toString()));

					rates.add(rate);
				}

				count++;

//				text = sok_br.readLine();//受信データ取得
//				System.out.println("受信『" + recive + "』");
			}
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
			pt_type = Constants.PROCESS_TYPE.PT_NETWORK_ERROR;
		}

		finally
		{
			try
			{
				if (socket != null)
					socket.close();
			}
			catch (IOException e)
			{
				// TODO 自動生成された catch ブロック
				System.out.println(e.toString());
				pt_type = Constants.PROCESS_TYPE.PT_EXCEPTION_ERROR;
			}
		}

    	return pt_type;
    }
}