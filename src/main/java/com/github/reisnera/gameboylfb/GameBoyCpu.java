/* This file is part of the GameBoyLFB project.
   GameBoyLFB - A Java Game Boy emulator.
   Copyright (C) 2015 Alex Reisner <thearcher at gmail dot com>

   This project is licensed under the GNU GPL v2 license and comes with
   absolutely no warranty of any kind. The full license can be found at:
   http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt */

package com.github.reisnera.gameboylfb;

import java.util.logging.Logger;
import java.util.logging.Level;

public class GameBoyCpu {
    private static final Logger LOG = Logger.getLogger(GameBoyCpu.class.getName());

    public static final int MASK_HALF_BYTE = 0xF;
    public static final int MASK_BYTE = 0xFF;
    public static final int MASK_WORD = 0xFFFF;
    public static final int MASK_HIGH_BYTE = 0xFF00;

    private static final int CLOCK_FREQ_HZ = 4194304;
    private static final double VBLANK_FREQ_HZ = 59.73;
    private static final int CPU_CYCLES_PER_VBLANK = (int) (CLOCK_FREQ_HZ / VBLANK_FREQ_HZ);

    private GameBoyMemory mem;
    CpuRegisters reg = new CpuRegisters();
    private boolean interruptMasterEnableFlag; // TODO: initial value?

    private int cycleCounter;

    public GameBoyCpu(GameBoyMemory memory) {
        this.mem = memory;
        initialize();
    }

    private void initialize() {
        // These magic numbers are the initialization values of the GB.
        reg.setAF(0x01B0);
        reg.setBC(0x0013);
        reg.setDE(0x00D8);
        reg.setHL(0x014D);
        reg.setSP(0xFFFE);
        reg.setPC(0);
    }

    public boolean isReadyForVblank() {
        if (cycleCounter > CPU_CYCLES_PER_VBLANK)
            return true;
        else
            return false;
    }

    public void getAndProcessNextOpcode() {
        if (interruptMasterEnableFlag == true) {
            doInterrupts();
        }

        int opcode;
        opcode = mem.readByte(reg.getThenIncPC());
        cycleCounter += 0; // how many cycles does fetch take?????????

        processOpcode(opcode);

        // TODO: update timers/counters, update input/output "ports", interrupt
        // flags, LCD, sound ?
    }

    void processOpcode(int opcode) {
        int operand;

        // Opcode reference:
        // http://www.pastraiser.com/cpu/gameboy/gameboy_opcodes.html
        // Comment format: opcode mnemonic : bytes, cycles : Z N H C

        switch (opcode) {
            case 0x00: // NOP : 1,4
                cycleCounter += 4;
                break;

            case 0x01: // LD BC,d16 : 3,12
                operand = mem.readWord(reg.getThenIncPC(2));
                reg.setBC(operand);
                cycleCounter += 12;
                break;

            case 0x02: // LD (BC),A : 1,8
                mem.writeByte(reg.getA(), reg.getBC());
                cycleCounter += 8;
                break;

            case 0x03: // INC BC : 1,8
                reg.setBC(reg.getBC() + 1);
                cycleCounter += 8;
                break;

            case 0x04: // INC B : 1,4 : Z 0 H -
                reg.setB(reg.getB() + 1);
                cycleCounter += 4;
                // Flags
                reg.clearFlagN();
                if (reg.getB() == 0) {
                    reg.setFlagZ();
                    reg.setFlagH();
                } else if ((reg.getB() & MASK_HALF_BYTE) == 0) {
                    reg.clearFlagZ();
                    reg.setFlagH();
                } else {
                    reg.clearFlagZ();
                    reg.clearFlagH();
                }
                break;

            case 0x05: // DEC B : 1,4 : Z 1 H -
                reg.setB(reg.getB() - 1);
                cycleCounter += 4;
                // Flags
                reg.setFlagN();
                if (reg.getB() == 0) {
                    reg.setFlagZ();
                    reg.clearFlagH();
                } else if ((reg.getB() & MASK_HALF_BYTE) == MASK_HALF_BYTE) {
                    reg.clearFlagZ();
                    reg.setFlagH();
                } else {
                    reg.clearFlagZ();
                    reg.clearFlagH();
                }
                break;

            case 0x06: // LD B,d8 : 2,8
                operand = mem.readByte(reg.getThenIncPC());
                reg.setB(operand);
                cycleCounter += 8;
                break;

            case 0x07: // RLCA : 1,4 : 0 0 0 C
                operand = rotateLeftAndSetFlags(reg.getA());
                reg.setA(operand);
                cycleCounter += 4;
                break;

            default: // Unimplemented opcode
                LOG.severe("Opcode " + Integer.toHexString(opcode) + " is not implemented.");
                System.exit(1);
                break;
        }
    }

    private void doInterrupts() {
        // process any interrupts
    }

    // Opcode helper functions

    private int rotateLeftAndSetFlags(int num8) {
        int mostSigBit = (1 << 7) & num8;
        num8 = (num8 << 1) & MASK_BYTE;
        num8 |= (mostSigBit >>> 7);
        reg.clearFlagZ();
        reg.clearFlagN();
        reg.clearFlagH();

        if (mostSigBit == 0)
            reg.clearFlagCy();
        else if (mostSigBit == (1 << 7))
            reg.setFlagCy();

        return num8;
    }

    /**
     * This class implements the Game Boy CPU's registers. NOTE: the setter
     * methods in this class will truncate their arguments to the appropriate
     * size (either 16 or 8 bits).
     */
    static public class CpuRegisters {

        private static final int MASK_REG_AF = 0xFFF0;
        private static final int MASK_FLAG_CY_BIT = 0x10;
        private static final int MASK_FLAG_H_BIT = 0x20;
        private static final int MASK_FLAG_N_BIT = 0x40;
        private static final int MASK_FLAG_Z_BIT = 0x80;

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

        public int getThenIncPC() {
            int retVal = PC;
            PC += 1;
            PC &= MASK_WORD;
            return retVal;
        }

        public int getThenIncPC(int n) {
            int retVal = PC;
            PC += n;
            PC &= MASK_WORD;
            return retVal;
        }

        // Setters for 16-bit registers

        public void setAF(int num16) {
            // NB: The first 4 bits of the flags portion of AF are cleared no
            // matter what
            AF = num16 & MASK_REG_AF;
        }

        public void setBC(int num16) {
            BC = num16 & MASK_WORD;
        }

        public void setDE(int num16) {
            DE = num16 & MASK_WORD;
        }

        public void setHL(int num16) {
            HL = num16 & MASK_WORD;
        }

        public void setSP(int num16) {
            SP = num16 & MASK_WORD;
        }

        public void setPC(int num16) {
            PC = num16 & MASK_WORD;
        }

        public void incPC() {
            PC += 1;
            PC &= MASK_WORD;
        }

        public void incPC(int n) {
            PC += n;
            PC &= MASK_WORD;
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
            num8 &= MASK_BYTE;
            AF = (AF & MASK_BYTE) | (num8 << 8);
        }

        public void setB(int num8) {
            num8 &= MASK_BYTE;
            BC = (BC & MASK_BYTE) | (num8 << 8);
        }

        public void setD(int num8) {
            num8 &= MASK_BYTE;
            DE = (DE & MASK_BYTE) | (num8 << 8);
        }

        public void setH(int num8) {
            num8 &= MASK_BYTE;
            HL = (HL & MASK_BYTE) | (num8 << 8);
        }

        // Methods to get low portions

        public int getF() {
            return AF & MASK_BYTE;
        }

        public int getC() {
            return BC & MASK_BYTE;
        }

        public int getE() {
            return DE & MASK_BYTE;
        }

        public int getL() {
            return HL & MASK_BYTE;
        }

        // Methods to set low portions

        public void setF(int num8) {
            AF = (AF & MASK_HIGH_BYTE) | (num8 & MASK_BYTE);
        }

        public void setC(int num8) {
            BC = (BC & MASK_HIGH_BYTE) | (num8 & MASK_BYTE);
        }

        public void setE(int num8) {
            DE = (DE & MASK_HIGH_BYTE) | (num8 & MASK_BYTE);
        }

        public void setL(int num8) {
            HL = (HL & MASK_HIGH_BYTE) | (num8 & MASK_BYTE);
        }

        // Carry flag related methods

        public boolean isSetCy() {
            if ((AF & MASK_FLAG_CY_BIT) == 0)
                return false;
            else
                return true;
        }

        public void setFlagCy() {
            AF |= MASK_FLAG_CY_BIT;
        }

        public void clearFlagCy() {
            AF &= ~MASK_FLAG_CY_BIT;
        }

        // Half carry (BCD) flag related methods

        public boolean isSetH() {
            if ((AF & MASK_FLAG_H_BIT) == 0)
                return false;
            else
                return true;
        }

        public void setFlagH() {
            AF |= MASK_FLAG_H_BIT;
        }

        public void clearFlagH() {
            AF &= ~MASK_FLAG_H_BIT;
        }

        // Add/Sub (BCD) flag related methods

        public boolean isSetN() {
            if ((AF & MASK_FLAG_N_BIT) == 0)
                return false;
            else
                return true;
        }

        public void setFlagN() {
            AF |= MASK_FLAG_N_BIT;
        }

        public void clearFlagN() {
            AF &= ~MASK_FLAG_N_BIT;
        }

        // Zero flag related methods

        public boolean isSetZ() {
            if ((AF & MASK_FLAG_Z_BIT) == 0)
                return false;
            else
                return true;
        }

        public void setFlagZ() {
            AF |= MASK_FLAG_Z_BIT;
        }

        public void clearFlagZ() {
            AF &= ~MASK_FLAG_Z_BIT;
        }
    }
}
