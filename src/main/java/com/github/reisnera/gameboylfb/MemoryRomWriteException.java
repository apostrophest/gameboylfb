/* This file is part of the GameBoyLFB project.
   GameBoyLFB - A Java Game Boy emulator.
   Copyright (C) 2015 Alex Reisner <thearcher at gmail dot com>

   This project is licensed under the GNU GPL v2 license and comes with
   absolutely no warranty of any kind. The full license can be found at:
   http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt */

package com.github.reisnera.gameboylfb;

public class MemoryRomWriteException extends Exception {

	private static final long serialVersionUID = 1L;

	private final int romAddress;

	public MemoryRomWriteException(int addr) {
		super();
		romAddress = addr;
	}

	public MemoryRomWriteException(String message, int addr) {
		super(message);
		romAddress = addr;
	}

	public MemoryRomWriteException(Throwable cause, int addr) {
		super(cause);
		romAddress = addr;
	}

	public MemoryRomWriteException(String message, Throwable cause, int addr) {
		super(message, cause);
		romAddress = addr;
	}

	public MemoryRomWriteException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace, int addr) {
		super(message, cause, enableSuppression, writableStackTrace);
		romAddress = addr;
	}

	public int getRequestedRomAddress() {
		return romAddress;
	}
}
