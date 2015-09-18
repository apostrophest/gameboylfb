package com.github.reisnera.gameboylfb;

public class RomInvalidFileException extends Exception {

	private static final long serialVersionUID = 1L;

	public RomInvalidFileException() {
	}

	public RomInvalidFileException(String message) {
		super(message);
	}

	public RomInvalidFileException(Throwable cause) {
		super(cause);
	}

	public RomInvalidFileException(String message, Throwable cause) {
		super(message, cause);
	}

	public RomInvalidFileException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
