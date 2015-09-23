/* This file is part of the GameBoyLFB project.
   GameBoyLFB - A Java Game Boy emulator.
   Copyright (C) 2015 Alex Reisner <thearcher at gmail dot com>

   This project is licensed under the GNU GPL v2 license and comes with
   absolutely no warranty of any kind. The full license can be found at:
   http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt */

package com.github.reisnera.gameboylfb;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.io.IOException;

public class GameBoyAppLauncher {
	private static final Logger log = Logger.getLogger("Main Log");

	public static void main(String[] args) {
		// Set up logging
		log.setLevel(Level.INFO);
		try {
			FileHandler fh = new FileHandler("./LogFile.txt");
			log.addHandler(fh);
			fh.setFormatter(new SimpleFormatter());
		}
		catch(IOException ex) {
			log.log(Level.WARNING, "Unable to open log file for writing!");
		}

		// Test GameBoyRom class
		try {
			GameBoyRom rom = new GameBoyRom("../test.txt");
		}
		catch(Exception ex) {
			log.log(Level.SEVERE, ex.toString(), ex);
		}

		GameBoyVideo video = new GameBoyVideo();
		System.out.println("Goodbye!");
	}
}
