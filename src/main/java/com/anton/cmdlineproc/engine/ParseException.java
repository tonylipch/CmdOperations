package com.anton.cmdlineproc.engine;

public class ParseException extends RuntimeException {
	public ParseException(String message) {
		super(message);
	}
	public ParseException(String message, Throwable dueTo) {
		super(message, dueTo);
	}

}
