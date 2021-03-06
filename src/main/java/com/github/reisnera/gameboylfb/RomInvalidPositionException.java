/* This file is part of the GameBoyLFB project.
   GameBoyLFB - A Java Game Boy emulator.
   Copyright (C) 2015 Alex Reisner <thearcher at gmail dot com>

   This project is licensed under the GNU GPL v2 license and comes with
   absolutely no warranty of any kind. The full license can be found at:
   http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt */

package com.github.reisnera.gameboylfb;

public class RomInvalidPositionException extends Exception {

    private static final long serialVersionUID = 1L;

    private final int requestedRomPosition;

    public RomInvalidPositionException(int requestedRomPosition) {
        super();
        this.requestedRomPosition = requestedRomPosition;
    }

    public RomInvalidPositionException(int requestedRomPosition, String message) {
        super(message);
        this.requestedRomPosition = requestedRomPosition;
    }

    public RomInvalidPositionException(int requestedRomPosition, Throwable cause) {
        super(cause);
        this.requestedRomPosition = requestedRomPosition;
    }

    public RomInvalidPositionException(int requestedRomPosition, String message, Throwable cause) {
        super(message, cause);
        this.requestedRomPosition = requestedRomPosition;
    }

    public RomInvalidPositionException(int requestedRomPosition, String message, Throwable cause,
                                       boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.requestedRomPosition = requestedRomPosition;
    }

    public int getRequestedRomPosition() {
        return requestedRomPosition;
    }
}
