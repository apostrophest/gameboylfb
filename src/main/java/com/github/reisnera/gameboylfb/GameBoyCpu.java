/* This file is part of the GameBoyLFB project.
   GameBoyLFB - A Java Game Boy emulator.
   Copyright (C) 2015 Alex Reisner <thearcher at gmail dot com>

   This project is licensed under the GNU GPL v2 license and comes with
   absolutely no warranty of any kind. The full license can be found at:
   http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt */

package com.github.reisnera.gameboylfb;

//import java.util.logging.Logger;
//import java.util.logging.Level

public class GameBoyCpu {
	//private static final Logger log = Logger.getLogger("Main Log");

	private GameBoyMemory mem;
	private CpuRegisters reg = new CpuRegisters();

	public GameBoyCpu(GameBoyMemory memory) {
		this.mem = memory;
	}

	/**
	 * This class implements the Game Boy CPU's registers. NOTE: the setter
	 * methods in this class will truncate their arguments to the appropriate
	 * size (either 16 or 8 bits).
	 */
	static public class CpuRegisters {
		private int AF;
		private int BC;
		private int DE;
		private int HL;
		private int SP;
		private int PC;

		// Getters for 16-bit registers

		public int getAF() {
			return AF;
		}
		public int getBC() {
			return BC;
		}
		public int getDE() {
			return DE;
		}
		public int getHL() {
			return HL;
		}
		public int getSP() {
			return SP;
		}
		public int getPC() {
			return PC;
		}

		// Setters for 16-bit registers

		public void setAF(int num16) {
			// NB: The first 4 bits of the flags portion of AF are cleared no matter what
			AF = num16 & 0xFFF0;
		}
		public void setBC(int num16) {
			BC = num16 & 0xFFFF;
		}
		public void setDE(int num16) {
			DE = num16 & 0xFFFF;
		}
		public void setHL(int num16) {
			HL = num16 & 0xFFFF;
		}
		public void setSP(int num16) {
			SP = num16 & 0xFFFF;
		}
		public void setPC(int num16) {
			PC = num16 & 0xFFFF;
		}

		// Methods to get high portions

		public int getA() {
			return AF >>> 8;
		}
		public int getB() {
			return BC >>> 8;
		}
		public int getD() {
			return DE >>> 8;
		}
		public int getH() {
			return HL >>> 8;
		}

		// Methods to set high portions

		public void setA(int num8) {
			num8 &= 0xFF;
			AF = (AF & 0xFF) | (num8 << 8);
		}
		public void setB(int num8) {
			num8 &= 0xFF;
			BC = (BC & 0xFF) | (num8 << 8);
		}
		public void setD(int num8) {
			num8 &= 0xFF;
			DE = (DE & 0xFF) | (num8 << 8);
		}
		public void setH(int num8) {
			num8 &= 0xFF;
			HL = (HL & 0xFF) | (num8 << 8);
		}

		// Methods to get low portions

		public int getF() {
			return AF & 0xFF;
		}
		public int getC() {
			return BC & 0xFF;
		}
		public int getE() {
			return DE & 0xFF;
		}
		public int getL() {
			return HL & 0xFF;
		}

		// Methods to set low portions

		public void setF(int num8) {
			AF = (AF & 0xFF00) | (num8 & 0xFF);
		}
		public void setC(int num8) {
			BC = (BC & 0xFF00) | (num8 & 0xFF);
		}
		public void setE(int num8) {
			DE = (DE & 0xFF00) | (num8 & 0xFF);
		}
		public void setL(int num8) {
			HL = (HL & 0xFF00) | (num8 & 0xFF);
		}

		// Carry flag related methods

		public boolean isSetCy() {
			if((AF & 0x10) == 0x10)
				return true;
			else
				return false;
		}
		public void setFlagCy() {
			AF |= 0x10;
		}
		public void clearFlagCy() {
			AF &= ~0x10;
		}

		// Half carry (BCD) flag related methods

		public boolean isSetH() {
			if((AF & 0x20) == 0x20)
				return true;
			else
				return false;
		}
		public void setFlagH() {
			AF |= 0x20;
		}
		public void clearFlagH() {
			AF &= ~0x20;
		}

		// Add/Sub (BCD) flag related methods

		public boolean isSetN() {
			if((AF & 0x40) == 0x40)
				return true;
			else
				return false;
		}
		public void setFlagN() {
			AF |= 0x40;
		}
		public void clearFlagN() {
			AF &= ~0x40;
		}

		// Zero flag related methods

		public boolean isSetZ() {
			if((AF & 0x80) == 0x80)
				return true;
			else
				return false;
		}
		public void setFlagZ() {
			AF |= 0x80;
		}
		public void clearFlagZ() {
			AF &= ~0x80;
		}
	}
}
