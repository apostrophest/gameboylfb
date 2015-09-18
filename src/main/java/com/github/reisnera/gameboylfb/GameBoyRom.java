/* This file is part of the GameBoyLFB project.
   GameBoyLFB - A Java Game Boy emulator.
   Copyright (C) 2015 Alex Reisner <thearcher at gmail dot com>

   This project is licensed under the GNU GPL v2 license and comes with
   absolutely no warranty of any kind. The full license can be found at:
   http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt */

package com.github.reisnera.gameboylfb;

import java.io.IOException;
//import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

public class GameBoyRom {

	private final byte[] romData;
	private final int romLength;
	private int seekPos = 0;
	private final byte[] romHeader;

	public GameBoyRom(String fileName) throws IOException, RomInvalidFileException {
		romData = Files.readAllBytes(Paths.get(fileName));
		romLength = romData.length;

		romHeader = new byte[80];
		try { System.arraycopy(romData, 0x100, romHeader, 0, 0x50); }
		catch(Exception ex) { throw new RomInvalidFileException(); }
	}

	public void seek(int newPosition) throws RomInvalidPositionException {
		if(newPosition >= 0 && newPosition < romData.length) {
			seekPos = newPosition;
		}
		else {
			throw new RomInvalidPositionException();
		}
	}

	public byte getByte() throws RomEndOfFileException {
		if(seekPos >= romLength) {
			throw new RomEndOfFileException();
		}
		try { return romData[seekPos]; }
		finally{ seekPos += 1; }
	}

	public byte[] getBytes(int numBytes) throws RomEndOfFileException {
		if(numBytes + seekPos > romLength) {
			throw new RomEndOfFileException();
		}
		else {
			byte[] bytes = new byte[numBytes];
			System.arraycopy(romData, seekPos, bytes, 0, numBytes);
			return bytes;
		}
	}

}
