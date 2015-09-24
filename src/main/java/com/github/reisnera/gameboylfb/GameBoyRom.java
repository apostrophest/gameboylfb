/* This file is part of the GameBoyLFB project.
   GameBoyLFB - A Java Game Boy emulator.
   Copyright (C) 2015 Alex Reisner <thearcher at gmail dot com>

   This project is licensed under the GNU GPL v2 license and comes with
   absolutely no warranty of any kind. The full license can be found at:
   http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt */

package com.github.reisnera.gameboylfb;

//import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.file.Files;
import java.io.IOException;
import java.io.EOFException;

public class GameBoyRom {
	private static final Logger log = Logger.getLogger("Main Log");

	final byte[] romData;
	final int romLength;
	private int seekPos = 0;

	// Header variables
	final byte[] hNintendoLogo = new byte[0x30];
	final String hGameTitle;
	final byte hCartridgeType;
	final byte hRomSizeCode;
	final byte hRamSizeCode;
	final byte hComplementCheck;

	public GameBoyRom(String fileName) throws IOException, RomInvalidFileException {
		romData = Files.readAllBytes(Paths.get(fileName));
		romLength = romData.length;

		// Process the ROM header
		if(romData[0x143] != 0x00) {
			throw new RomInvalidFileException("ROM may be for CGB.");
		}

		if(romData[0x146] != 0x00) {
			throw new RomInvalidFileException("ROM not for original GB.");
		}

		try {
			System.arraycopy(romData, 0x104, hNintendoLogo, 0, 0x30);

			byte[] temp = new byte[0xF];
			System.arraycopy(romData, 0x134, temp, 0, 0xF);
			hGameTitle = new String(temp, "US-ASCII");

			hCartridgeType = romData[0x147];
			hRomSizeCode = romData[0x148];
			hRamSizeCode = romData[0x149];
			hComplementCheck = romData[0x14d];
		}
		catch(Exception ex) {
			log.log(Level.WARNING, "Unable to set ROM header variables!");
			throw new RomInvalidFileException("Error setting ROM header variables.", ex);
		}

		// Only supported cartridge type is 0x00 (PLAIN ROM)
		if(hCartridgeType != 0x00) {
			throw new RomInvalidFileException("Unsupported cartridge type: " + hCartridgeType);
		}
	}

	public void seek(int newPosition) throws RomInvalidPositionException {
		if(newPosition >= 0 && newPosition < romData.length) {
			seekPos = newPosition;
		}
		else {
			throw new RomInvalidPositionException(newPosition);
		}
	}

	public byte getByte() throws EOFException {
		if(seekPos >= romLength) {
			throw new EOFException();
		}
		try { return romData[seekPos]; }
		finally{ seekPos += 1; }
	}

	public byte[] getBytes(int numBytes) throws EOFException {
		if(numBytes + seekPos > romLength) {
			throw new EOFException();
		}
		else {
			byte[] bytes = new byte[numBytes];
			System.arraycopy(romData, seekPos, bytes, 0, numBytes);
			return bytes;
		}
	}

}
