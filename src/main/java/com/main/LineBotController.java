package com.main;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author shiotsuki
 *
 */
@RestController
public class LineBotController {

	//@Autowired
	//private LineMessagingClient lineMessagingClient;


	@RequestMapping(value = "/linebot")
	void index(HttpServletRequest request) throws RuntimeException {

		System.out.println("request: " + request.getParameter("text").toString());
/*
		@SuppressWarnings("unused")
		BotApiResponse response;
		try {
			response = this.lineMessagingClient
			        .pushMessage(new PushMessage("Ud6e699869888126beedab30c5b3d484e".toString(),
			                     new TextMessage(request.getParameter("text").toString()
			                      ))).get();

		} catch (InterruptedException | ExecutionException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		*/
	}
}
