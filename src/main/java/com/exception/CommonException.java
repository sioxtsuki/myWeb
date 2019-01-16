package com.exception;

/**
 *
 * @author shiotsuki
 *
 */
abstract public class CommonException extends RuntimeException {

	/**
	 *
	 * @param cause
	 */
	CommonException(Throwable cause) {
		super(cause);
	}
}
