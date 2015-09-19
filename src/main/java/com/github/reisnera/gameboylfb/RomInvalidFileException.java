/* This file is part of the GameBoyLFB project.
   GameBoyLFB - A Java Game Boy emulator.
   Copyright (C) 2015 Alex Reisner <thearcher at gmail dot com>

   This project is licensed under the GNU GPL v2 license and comes with
   absolutely no warranty of any kind. The full license can be found at:
   http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt */

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
