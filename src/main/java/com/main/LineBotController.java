package com.main;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LineBotController {

	@RequestMapping(value = "/linebot")
	String index(HttpServletRequest request) throws RuntimeException {
		return "OK";
	}
}
