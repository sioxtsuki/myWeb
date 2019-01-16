package com.dto;

public class LineInfoDto {

	//
	private String mId;
	private String toChannel;
	private String eventType;
	private String text;
	private String XLineChannelID;
	private String XLineChannelSecret;
	private String XLineTrustedUserWithACL;

	public String getmId() {
		return mId;
	}

	public void setmId(String mId) {
		this.mId = mId;
	}

	public String getToChannel() {
		return toChannel;
	}

	public void setToChannel(String toChannel) {
		this.toChannel = toChannel;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getXLineChannelID() {
		return XLineChannelID;
	}

	public void setXLineChannelID(String xLineChannelID) {
		XLineChannelID = xLineChannelID;
	}

	public String getXLineChannelSecret() {
		return XLineChannelSecret;
	}

	public void setXLineChannelSecret(String xLineChannelSecret) {
		XLineChannelSecret = xLineChannelSecret;
	}

	public String getXLineTrustedUserWithACL() {
		return XLineTrustedUserWithACL;
	}

	public void setXLineTrustedUserWithACL(String xLineTrustedUserWithACL) {
		XLineTrustedUserWithACL = xLineTrustedUserWithACL;
	}

}
