package com.utility;

import java.util.Collections;

import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;

/**
 * eventラッパークラス
 *
 * @author shiotsuki
 *
 *         [--------Messaging APIリファレンス--------]<br>
 *         <br>
 *         < replyToken > <br>
 *         <br>
 *         一定秒間以内に使用しないと無効になりますので、<br>
 *         受信してから可能な限り速く返信してください。 <br>
 *         使用回数の上限は1トークンにつき１回までです。
 */
public class EventWrapperImpl implements EventWrapper {

	MessageEvent<TextMessageContent> myEvent;

	/**
	 * コンストラクタ
	 */
	public EventWrapperImpl(MessageEvent<TextMessageContent> event) {
		this.myEvent = event;
	}

	@Override
	public TextMessageContent getTextMessageContent() {
		// TODO 自動生成されたメソッド・スタブ
		return this.myEvent.getMessage();
	}

	@Override
	public Source getSource() {
		// TODO 自動生成されたメソッド・スタブ
		return this.myEvent.getSource();
	}

	@Override
	public String getUserId() {
		// TODO 自動生成されたメソッド・スタブ
		return this.myEvent.getSource().getUserId();
	}

	@Override
	public Message getMessage(String message) {
		// TODO 自動生成されたメソッド・スタブ
		return (Message) Collections.singletonList(new TextMessage(message));
	}

	@Override
	public ReplyMessage getReplyMessage(String message) {
		/**
		 * ↓ 元ネタ<br>
		 * final BotApiResponse apiResponse = lineMessagingService
		 * .replyMessage(new ReplyMessage(event.getReplyToken(), <br>
		 * (Message) Collections.singletonList(<br>
		 * new TextMessage(event.getSource().getUserId())))) .execute().body();
		 */

		// TODO 自動生成されたメソッド・スタブ
		return new ReplyMessage(this.myEvent.getReplyToken(), getMessage(message));
	}

	/**
	 * 未実装<br>
	 * 今後、検索文字に紐づくワードを返す
	 */
	@Override
	public void create() {
		// TODO 自動生成されたメソッド・スタブ
	}

}
