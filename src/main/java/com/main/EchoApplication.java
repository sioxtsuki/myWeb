package com.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * メインクラス
 *
 * @author shiotsuki
 */

@SpringBootApplication
@EnableScheduling
public class EchoApplication
{
	/**
	 * メインロジック
	 *
	 * @param args
	 */
	public static void main(String[] args)
	{
		SpringApplication.run(EchoApplication.class, args);
	}
}
