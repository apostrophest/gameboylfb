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
    private static final Logger LOG = Logger.getLogger(GameBoyAppLauncher.class.getName());

    public static void main(String[] args) throws Exception {
        configureLogging();

        GameBoyRom rom;
        GameBoyMemory mem;
        GameBoyCpu cpu;
        App app;

        // Load a Game Boy ROM
        try {
            rom = new GameBoyRom("Alleyway.gb");
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.toString(), ex);
            throw new Exception(ex);
        }

        System.out.println(rom.getHeaderGameTitle());

        try {
            mem = new GameBoyMemory(rom);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.toString(), ex);
            throw new Exception(ex);
        }

        cpu = new GameBoyCpu(mem);

        run();

        // testing...
        System.out.println(String.format("%x", mem.readByte(0)));
        mem.disableDmgRom();
        System.out.println(String.format("%x", mem.readByte(0)));

        app = new App();

        while (app.tick() == 0) {
            // TODO: More
            continue;
        }
    }

    private static void configureLogging() {
        LOG.setLevel(Level.INFO);
        try {
            FileHandler fh = new FileHandler("./LogFile.txt");
            LOG.addHandler(fh);
            fh.setFormatter(new SimpleFormatter());
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Unable to open log file for writing!");
        }
    }

    private static void run() {
        // main loop goes here
    }
}
