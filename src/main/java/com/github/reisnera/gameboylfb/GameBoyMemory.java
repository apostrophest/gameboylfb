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
import java.util.logging.Logger;

public class GameBoyMemory {
	private static final Logger log = Logger.getLogger("Main Log");

	private final byte[] dmgRom;

	public GameBoyMemory(GameBoyRom rom) throws IOException, MemoryBadDmgRomException {
		dmgRom = Files.readAllBytes(Paths.get("../DMG_ROM.bin"));
		if(dmgRom.length != 256) {
			throw new MemoryBadDmgRomException("Invalid DMG ROM length.");
		}
	}

}
