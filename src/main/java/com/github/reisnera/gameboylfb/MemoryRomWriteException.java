/* This file is part of the GameBoyLFB project.
   GameBoyLFB - A Java Game Boy emulator.
   Copyright (C) 2015 Alex Reisner <thearcher at gmail dot com>

   This project is licensed under the GNU GPL v2 license and comes with
   absolutely no warranty of any kind. The full license can be found at:
   http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt */

package com.github.reisnera.gameboylfb;

public class MemoryRomWriteException extends Exception {

	private static final long serialVersionUID = 1L;

	private final short romAddress;

	public MemoryRomWriteException(short addr) {
		super();
		romAddress = addr;
	}

	public MemoryRomWriteException(String message, short addr) {
		super(message);
		romAddress = addr;
	}

	public MemoryRomWriteException(Throwable cause, short addr) {
		super(cause);
		romAddress = addr;
	}

	public MemoryRomWriteException(String message, Throwable cause, short addr) {
		super(message, cause);
		romAddress = addr;
	}

	public MemoryRomWriteException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace, short addr) {
		super(message, cause, enableSuppression, writableStackTrace);
		romAddress = addr;
	}

	public short getRequestedRomAddress() {
		return romAddress;
	}
}
