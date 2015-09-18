package com.github.reisnera.gameboylfb;

public class RomEndOfFileException extends Exception {

	private static final long serialVersionUID = 1L;

	public RomEndOfFileException() {
	}

	public RomEndOfFileException(String message) {
		super(message);
	}

	public RomEndOfFileException(Throwable cause) {
		super(cause);
	}

	public RomEndOfFileException(String message, Throwable cause) {
		super(message, cause);
	}

	public RomEndOfFileException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
