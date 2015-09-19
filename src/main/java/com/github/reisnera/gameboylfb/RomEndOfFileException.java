/* This file is part of the GameBoyLFB project.
   GameBoyLFB - A Java Game Boy emulator.
   Copyright (C) 2015 Alex Reisner <thearcher at gmail dot com>

   This project is licensed under the GNU GPL v2 license and comes with
   absolutely no warranty of any kind. The full license can be found at:
   http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt */

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
