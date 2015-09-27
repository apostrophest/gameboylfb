/* This file is part of the GameBoyLFB project.
   GameBoyLFB - A Java Game Boy emulator.
   Copyright (C) 2015 Alex Reisner <thearcher at gmail dot com>

   This project is licensed under the GNU GPL v2 license and comes with
   absolutely no warranty of any kind. The full license can be found at:
   http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt */

package com.github.reisnera.gameboylfb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
//import java.util.logging.Logger;

public class GameBoyMemory {
	//private static final Logger log = Logger.getLogger("Main Log");

	private GameBoyRom rom;
	private byte[] memory = new byte[65536];

	public GameBoyMemory(GameBoyRom rom) throws IOException, MemoryBadDmgRomException {
		this.rom = rom;

		byte[] dmgRom = Files.readAllBytes(Paths.get("../DMG_ROM.bin"));
		if(dmgRom.length != 256) {
			throw new MemoryBadDmgRomException("Invalid DMG ROM length.");
		}

		// Copy 32kB cart ROM to beginning of memory (except first 256 bytes)
		System.arraycopy(rom.romData, 256, memory, 256, 32512);
		// Copy the DMG ROM to the first 256 bytes of memory
		System.arraycopy(dmgRom, 0, memory, 0, 256);
	}

	public void disableDmgRom() {
		// Overwrite the DMG ROM with the first 256 bytes of the cart ROM
		System.arraycopy(rom.romData, 0, memory, 0, 256);
	}

	public byte readByte(short addr) {
		// Take into account the mirrored RAM area
		if(addr >= 0xE000 && addr < 0xFE00) {
			addr -= 0x2000;
		}
		return memory[addr];
	}

	public void writeByte(byte data, short addr) throws MemoryRomWriteException {
		if(addr < 0x8000) {
			throw new MemoryRomWriteException(addr);
		} else if(addr >= 0xE000 && addr < 0xFE00) {
			// Take into account the mirrored RAM area
			addr -= 0x2000;
		}

		memory[addr] = data;
	}
}
