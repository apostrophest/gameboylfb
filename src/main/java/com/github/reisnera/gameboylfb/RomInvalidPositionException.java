package com.github.reisnera.gameboylfb;

public class RomInvalidPositionException extends Exception {

	private static final long serialVersionUID = 1L;

	public RomInvalidPositionException() {
		super();
	}

	public RomInvalidPositionException(String message) {
		super(message);
	}

	public RomInvalidPositionException(Throwable cause) {
		super(cause);
	}

	public RomInvalidPositionException(String message, Throwable cause) {
		super(message, cause);
	}

	public RomInvalidPositionException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
