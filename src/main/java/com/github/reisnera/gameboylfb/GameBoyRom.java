/* This file is part of the GameBoyLFB project.
   GameBoyLFB - A Java Game Boy emulator.
   Copyright (C) 2015 Alex Reisner <thearcher at gmail dot com>

   This project is licensed under the GNU GPL v2 license and comes with
   absolutely no warranty of any kind. The full license can be found at:
   http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt */

package com.github.reisnera.gameboylfb;

import java.nio.file.Paths;
//import java.util.logging.Level;
//import java.util.logging.Logger;
import java.nio.file.Files;
import java.io.IOException;

public class GameBoyRom {

    // private static final Logger log = Logger.getLogger("Main Log");

    private byte[] romData;

    // Header variables
    private byte[] headerNintendoLogo = new byte[0x30];
    private String headerGameTitle;
    private byte headerCartridgeType;
    private byte headerRomSizeCode;
    private byte headerRamSizeCode;
    private byte headerComplementCheck;

    public GameBoyRom(String fileName) throws IOException, RomInvalidFileException {
        romData = Files.readAllBytes(Paths.get(fileName));

        // ROM should be exactly 32KB
        if (getRomLength() != 0x8000) {
            throw new RomInvalidFileException("ROM length incorrect.");
        }

        // Process the ROM header
        if (romData[0x143] != 0x00) {
            throw new RomInvalidFileException("ROM may be for CGB.");
        }

        if (romData[0x146] != 0x00) {
            throw new RomInvalidFileException("ROM not for original GB.");
        }

        try {
            System.arraycopy(romData, 0x104, headerNintendoLogo, 0, 0x30);

            byte[] temp = new byte[0xF];
            System.arraycopy(romData, 0x134, temp, 0, 0xF);
            headerGameTitle = new String(temp, "US-ASCII");

            headerCartridgeType = romData[0x147];
            headerRomSizeCode = romData[0x148];
            headerRamSizeCode = romData[0x149];
            headerComplementCheck = romData[0x14d];
        } catch (Exception ex) {
            throw new RomInvalidFileException("Error setting ROM header variables.", ex);
        }

        // Only supported cartridge type is 0x00 (PLAIN ROM)
        if (headerCartridgeType != 0x00) {
            throw new RomInvalidFileException("Unsupported cartridge type: " + headerCartridgeType);
        }
    }

    // Getters

    public byte[] getRomData() {
        return romData;
    }

    public int getRomLength() {
        return romData.length;
    }

    public byte[] getHeaderNintendoLogo() {
        return headerNintendoLogo;
    }

    public String getHeaderGameTitle() {
        return headerGameTitle;
    }

    public byte getHeaderCartridgeType() {
        return headerCartridgeType;
    }

    public byte getHeaderRomSizeCode() {
        return headerRomSizeCode;
    }

    public byte getHeaderRamSizeCode() {
        return headerRamSizeCode;
    }

    public byte getHeaderComplementCheck() {
        return headerComplementCheck;
    }

}
