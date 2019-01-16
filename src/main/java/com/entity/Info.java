package com.entity;

/**
 * ライン情報エンティティクラス
 *
 * @author shiotsuki
 *
 */
public class Info {

	private String mId;
	private int toChannel;
	private String eventType;
	private String xLineChannelID;
	private String xLineChannelSecret;
	private String xLineTrustedUserWithACL;

	public String getmId() {
		return mId;
	}

	public void setmId(String mId) {
		this.mId = mId;
	}

	public int getToChannel() {
		return toChannel;
	}

	public void setToChannel(int toChannel) {
		this.toChannel = toChannel;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getxLineChannelID() {
		return xLineChannelID;
	}

	public void setxLineChannelID(String xLineChannelID) {
		this.xLineChannelID = xLineChannelID;
	}

	public String getxLineChannelSecret() {
		return xLineChannelSecret;
	}

	public void setxLineChannelSecret(String xLineChannelSecret) {
		this.xLineChannelSecret = xLineChannelSecret;
	}

	public String getxLineTrustedUserWithACL() {
		return xLineTrustedUserWithACL;
	}

	public void setxLineTrustedUserWithACL(String xLineTrustedUserWithACL) {
		this.xLineTrustedUserWithACL = xLineTrustedUserWithACL;
	}

}
