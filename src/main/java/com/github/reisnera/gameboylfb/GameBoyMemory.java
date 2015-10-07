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

	// private static final Logger log = Logger.getLogger("Main Log");

	private GameBoyRom rom;
	private byte[] memory = new byte[65536];

	public GameBoyMemory(GameBoyRom rom) throws IOException, MemoryBadDmgRomException {
		this.rom = rom;

		byte[] dmgRom = Files.readAllBytes(Paths.get("../DMG_ROM.bin"));
		if(dmgRom.length != 256) {
			throw new MemoryBadDmgRomException("Invalid DMG ROM length.");
		}

		// Copy 32kB cart ROM to beginning of memory (except first 256 bytes)
		System.arraycopy(rom.getRomData(), 256, memory, 256, 32512);
		// Copy the DMG ROM to the first 256 bytes of memory
		System.arraycopy(dmgRom, 0, memory, 0, 256);

		initialize();
	}

	private void initialize() {
		memory[0xFF05] = (byte) 0x00; // TIMA
		memory[0xFF06] = (byte) 0x00; // TMA
		memory[0xFF07] = (byte) 0x00; // TAC
		memory[0xFF10] = (byte) 0x80; // NR10
		memory[0xFF11] = (byte) 0xBF; // NR11
		memory[0xFF12] = (byte) 0xF3; // NR12
		memory[0xFF14] = (byte) 0xBF; // NR14
		memory[0xFF16] = (byte) 0x3F; // NR21
		memory[0xFF17] = (byte) 0x00; // NR22
		memory[0xFF19] = (byte) 0xBF; // NR24
		memory[0xFF1A] = (byte) 0x7F; // NR30
		memory[0xFF1B] = (byte) 0xFF; // NR31
		memory[0xFF1C] = (byte) 0x9F; // NR32
		memory[0xFF1E] = (byte) 0xBF; // NR33
		memory[0xFF20] = (byte) 0xFF; // NR41
		memory[0xFF21] = (byte) 0x00; // NR42
		memory[0xFF22] = (byte) 0x00; // NR43
		memory[0xFF23] = (byte) 0xBF; // NR30
		memory[0xFF24] = (byte) 0x77; // NR50
		memory[0xFF25] = (byte) 0xF3; // NR51
		memory[0xFF26] = (byte) 0xF1; // NR52
		memory[0xFF40] = (byte) 0x91; // LCDC
		memory[0xFF42] = (byte) 0x00; // SCY
		memory[0xFF43] = (byte) 0x00; // SCX
		memory[0xFF45] = (byte) 0x00; // LYC
		memory[0xFF47] = (byte) 0xFC; // BGP
		memory[0xFF48] = (byte) 0xFF; // OBP0
		memory[0xFF49] = (byte) 0xFF; // OBP1
		memory[0xFF4A] = (byte) 0x00; // WY
		memory[0xFF4B] = (byte) 0x00; // WX
		memory[0xFFFF] = (byte) 0x00; // IE
	}

	public void disableDmgRom() {
		// Overwrite the DMG ROM with the first 256 bytes of the cart ROM
		System.arraycopy(rom.getRomData(), 0, memory, 0, 256);
	}

	public int readByte(int addr) {
		// Take into account the mirrored RAM area
		if(addr >= 0xE000 && addr < 0xFE00) {
			addr -= 0x2000;
		}
		return memory[addr] & GameBoyCpu.MASK_BYTE;
	}

	/**
	 * Read word (two bytes) of little-endian data.
	 */
	public int readWord(int addr) {
		int low, high;
		// Read both bytes and bitwise AND with byte mask to prevent sign
		// extension with int conversion.
		low = readByte(addr) & GameBoyCpu.MASK_BYTE;
		high = (readByte(addr + 1) & GameBoyCpu.MASK_BYTE) << 8;
		return (high | low);
	}

	public void writeByte(int data, int addr) {
		if(addr < 0x8000) {
			// Attempted write to ROM, which will do nothing.
			// At least not until implementing switched memory banks...
		} else if(addr >= 0xE000 && addr < 0xFE00) {
			// Take into account the mirrored RAM area
			addr -= 0x2000;
			memory[addr] = (byte) data;
		} else {
			memory[addr] = (byte) data;
		}
	}

}
