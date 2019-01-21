package com.utility;

import java.io.Serializable;

import javax.xml.bind.DatatypeConverter;

/**
 * @author shiotsuki
 *
 */
public class Crypto implements Serializable
{
	/**
	 * 暗号化 TODO
	 *
	 * @param value
	 * @return
	 */
	public static byte[] Cipher(String value)
	{
		return value.getBytes();
    }

	/**
	 * 復元化 TODO
	 *
	 * @param passwd
	 * @return
	 */
	public static String Composite(String passwd)
	{

		byte[] data = DatatypeConverter.parseHexBinary(passwd);
		return new String(data);
	}

	/**
	 * 表示用メソッド
	 *
	 * @param b
	 * @return
	 */
	public static String PrintBin(byte[] b)
	{
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<b.length; i++)
		{
			String h = Integer.toHexString(b[i]&0xFF);
			if((i+1)%8 == 0)
			{
				sb.append(h);
			    System.out.println(h+" ");
			}
			else
			{
				sb.append(h);
			    System.out.print(h+" ");
		   }
		}
		System.out.print("\n");

		return sb.toString();
	}
}
