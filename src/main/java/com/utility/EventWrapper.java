package com.utility;

import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.message.Message;

/**
 * Eventにおけるユーティリティインターフェース
 *
 * @author shiotsuki
 *
 */
public interface EventWrapper {

	/**
	 * eventからメッセージの返却
	 *
	 * @return
	 */
	public TextMessageContent getTextMessageContent();

	/**
	 * eventからソースの返却
	 *
	 * @return
	 */
	public Source getSource();

	/**
	 * eventからユーザIDの返却
	 *
	 * @return
	 */
	public String getUserId();

	/**
	 * eventからメッセージの返却
	 *
	 * @param strMessage
	 * @return
	 */
	public Message getMessage(String message);

	/**
	 * リプライメッセージの返却
	 */
	public ReplyMessage getReplyMessage(String message);

	/**
	 * メッセージ作成<br>
	 * TODO
	 */
	public void create();
}
