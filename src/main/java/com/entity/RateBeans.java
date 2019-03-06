package com.entity;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RateBeans
{
	String symbol;
	double bid, ask;
	long ctm;
	int interval;

	// 文字列日付を返却
	public String GetStrCtm()
	{
		//Date date1 = new Date(this.ctm); "yyyy年MM月dd日 E曜日 H時mm分"
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

//		return format.format(new Date((this.ctm - 32400) * 1000));
		return format.format(new Date((this.ctm) * 1000));

	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public double getBid() {
		return bid;
	}
	public void setBid(double bid) {
		this.bid = bid;
	}
	public double getAsk() {
		return ask;
	}
	public void setAsk(double ask) {
		this.ask = ask;
	}
	public long getCtm() {
		return ctm;
	}
	public void setCtm(long ctm) {
		this.ctm = ctm;
	}


}
