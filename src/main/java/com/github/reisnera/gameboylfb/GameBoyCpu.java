/* This file is part of the GameBoyLFB project.
   GameBoyLFB - A Java Game Boy emulator.
   Copyright (C) 2015 Alex Reisner <thearcher at gmail dot com>

   This project is licensed under the GNU GPL v2 license and comes with
   absolutely no warranty of any kind. The full license can be found at:
   http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt */

package com.github.reisnera.gameboylfb;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.logging.Logger;

public class GameBoyCpu {
    private static final Logger LOG = Logger.getLogger(GameBoyCpu.class.getName());

    public static final int MASK_HALF_BYTE = 0xF;
    public static final int MASK_HIGH_NIBBLE = 0xF0;
    public static final int MASK_BYTE = 0xFF;
    public static final int MASK_BYTE_PLUS_NIBBLE = 0xFFF;
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
        int tempAddr;
        int priorValue;
        int mostSigBit, leastSigBit;
        int tempResult;

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
                priorValue = reg.getB();
                reg.setB(priorValue + 1);
                cycleCounter += 4;
                // Flags
                checkForZero(reg.getB());
                reg.clearFlagN();
                checkAddForHalfCarry(priorValue, reg.getB(), MASK_HALF_BYTE);
                break;

            case 0x05: // DEC B : 1,4 : Z 1 H -
                priorValue = reg.getB();
                reg.setB(priorValue - 1);
                cycleCounter += 4;
                // Flags
                checkForZero(reg.getB());
                reg.setFlagN();
                checkSubForHalfCarry(priorValue, reg.getB(), MASK_HALF_BYTE);
                break;

            case 0x06: // LD B,d8 : 2,8
                operand = mem.readByte(reg.getThenIncPC());
                reg.setB(operand);
                cycleCounter += 8;
                break;

            case 0x07: // RLCA : 1,4 : 0 0 0 C
                operand = reg.getA();
                mostSigBit = (1 << 7) & operand;
                operand = (operand << 1) & MASK_BYTE;
                operand |= (mostSigBit >>> 7);
                reg.setA(operand);
                cycleCounter += 4;
                // Flags
                reg.clearFlagZ();
                reg.clearFlagN();
                reg.clearFlagH();
                if (mostSigBit == 0) {
                    reg.clearFlagCy();
                } else {
                    reg.setFlagCy();
                }
                break;

            case 0x08: // LD (a16),SP : 3,20
                tempAddr = mem.readWord(reg.getThenIncPC(2));
                mem.writeWord(reg.getSP(), tempAddr);
                cycleCounter += 20;
                break;

            case 0x09: // ADD HL,BC : 1,8 : - 0 H C
                priorValue = reg.getHL();
                reg.setHL(priorValue + reg.getBC());
                cycleCounter += 8;
                // Flags
                reg.clearFlagN();
                checkAddForHalfCarry(priorValue, reg.getHL(), MASK_BYTE_PLUS_NIBBLE);
                checkAddForCarry(priorValue, reg.getHL());
                break;

            case 0x0A: // LD A,(BC) : 1,8
                reg.setA(mem.readByte(reg.getBC()));
                cycleCounter += 8;
                break;

            case 0x0B: // DEC BC : 1,8
                reg.setBC(reg.getBC() - 1);
                cycleCounter += 8;
                break;

            case 0x0C: // INC C : 1,4 : Z 0 H -
                priorValue = reg.getC();
                reg.setC(priorValue + 1);
                cycleCounter += 4;
                // Flags
                checkForZero(reg.getC());
                reg.clearFlagN();
                checkAddForHalfCarry(priorValue, reg.getC(), MASK_HALF_BYTE);
                break;

            case 0x0D: // DEC C : 1,4 : Z 1 H -
                priorValue = reg.getC();
                reg.setC(priorValue - 1);
                cycleCounter += 4;
                // Flags
                checkForZero(reg.getC());
                reg.setFlagN();
                checkSubForHalfCarry(priorValue, reg.getC(), MASK_HALF_BYTE);
                break;

            case 0x0E: // LD C,d8 : 2,8
                reg.setC(mem.readByte(reg.getThenIncPC()));
                cycleCounter += 8;
                break;

            case 0x0F: // RRCA : 1,4 : 0 0 0 C
                operand = reg.getA();
                leastSigBit = operand & 1;
                operand = operand >>> 1;
                operand |= leastSigBit << 7;
                reg.setA(operand);
                cycleCounter += 4;
                // Flags
                reg.clearFlagZ();
                reg.clearFlagN();
                reg.clearFlagH();
                if (leastSigBit == 0) {
                    reg.clearFlagCy();
                } else {
                    reg.setFlagCy();
                }
                break;

            case 0x10: // STOP 0 : 2,4
                // TODO: implement this!
                break;

            case 0x11: // LD DE,d16 : 3,12
                operand = mem.readWord(reg.getThenIncPC(2));
                reg.setDE(operand);
                cycleCounter += 12;
                break;

            case 0x12: // LD (DE),A : 1,8
                mem.writeByte(reg.getA(), reg.getDE());
                cycleCounter += 8;
                break;

            case 0x13: // INC DE : 1,8
                reg.setDE(reg.getDE() + 1);
                cycleCounter += 8;
                break;

            case 0x14: // INC D : 1,4 : Z 0 H -
                priorValue = reg.getD();
                reg.setD(priorValue + 1);
                cycleCounter += 4;
                // Flags
                checkForZero(reg.getD());
                reg.clearFlagN();
                checkAddForHalfCarry(priorValue, reg.getD(), MASK_HALF_BYTE);
                break;

            case 0x15: // DEC D : 1,4 : Z 1 H -
                priorValue = reg.getD();
                reg.setD(priorValue - 1);
                cycleCounter += 4;
                // Flags
                checkForZero(reg.getD());
                reg.setFlagN();
                checkSubForHalfCarry(priorValue, reg.getD(), MASK_HALF_BYTE);
                break;

            case 0x16: // LD D,d8 : 2,8
                reg.setD(mem.readByte(reg.getThenIncPC()));
                cycleCounter += 8;
                break;

            case 0x17: // RLA : 1,4 : 0 0 0 C
                operand = reg.getA();
                mostSigBit = (1 << 7) & operand;
                operand = (operand << 1) & MASK_BYTE;
                if (reg.isSetCy()) {
                    operand |= 1;
                }
                reg.setA(operand);
                cycleCounter += 4;
                // Flags
                reg.clearFlagZ();
                reg.clearFlagN();
                reg.clearFlagH();
                if (mostSigBit == 0) {
                    reg.clearFlagCy();
                } else {
                    reg.setFlagCy();
                }
                break;

            case 0x18: // JR r8 : 2,12
                // TODO: if things aren't working AT ALL when we run a ROM, look here and at all the JR instructions.
                operand = mem.readByte(reg.getThenIncPC());
                reg.setPC(reg.getPC() + (byte) operand);
                cycleCounter += 12;
                break;

            case 0x19: // ADD HL,DE : 1,8 : - 0 H C
                priorValue = reg.getHL();
                reg.setHL(priorValue + reg.getDE());
                cycleCounter += 8;
                // Flags
                reg.clearFlagN();
                checkAddForHalfCarry(priorValue, reg.getHL(), MASK_BYTE_PLUS_NIBBLE);
                checkAddForCarry(priorValue, reg.getHL());
                break;

            case 0x1A: // LD A,(DE) : 1,8
                reg.setA(mem.readByte(reg.getDE()));
                cycleCounter += 8;
                break;

            case 0x1B: // DEC DE : 1,8
                reg.setDE(reg.getDE() - 1);
                cycleCounter += 8;
                break;

            case 0x1C: // INC E : 1,4 : Z 0 H -
                priorValue = reg.getE();
                reg.setE(priorValue + 1);
                cycleCounter += 4;
                // Flags
                checkForZero(reg.getE());
                reg.clearFlagN();
                checkAddForHalfCarry(priorValue, reg.getE(), MASK_HALF_BYTE);
                break;

            case 0x1D: // DEC E : 1,4 : Z 1 H -
                priorValue = reg.getE();
                reg.setE(priorValue - 1);
                cycleCounter += 4;
                // Flags
                checkForZero(reg.getE());
                reg.setFlagN();
                checkSubForHalfCarry(priorValue, reg.getE(), MASK_HALF_BYTE);
                break;

            case 0x1E: // LD E,d8 : 2,8
                reg.setE(mem.readByte(reg.getThenIncPC()));
                cycleCounter += 8;
                break;

            case 0x1F: // RRA : 1,4 : 0 0 0 C
                operand = reg.getA();
                leastSigBit = operand & 1;
                operand = operand >>> 1;
                if (reg.isSetCy()) {
                    operand |= 1 << 7;
                }
                reg.setA(operand);
                cycleCounter += 4;
                // Flags
                reg.clearFlagZ();
                reg.clearFlagN();
                reg.clearFlagH();
                if (leastSigBit == 0) {
                    reg.clearFlagCy();
                } else {
                    reg.setFlagCy();
                }
                break;

            case 0x20: // JR NZ,r8 : 2,12/8
                if (reg.isSetZ()) {
                    reg.incPC(); // Move PC past immediate byte data to next instruction
                    cycleCounter += 8;
                } else {
                    operand = mem.readByte(reg.getThenIncPC());
                    reg.setPC(reg.getPC() + (byte) operand);
                    cycleCounter += 12;
                }
                break;

            case 0x21: // LD HL,d16 : 3,12
                reg.setHL(mem.readWord(reg.getThenIncPC(2)));
                cycleCounter += 12;
                break;

            case 0x22: // LD (HL+),A : 1,8
                mem.writeByte(reg.getA(), reg.getHL());
                reg.setHL(reg.getHL() + 1);
                cycleCounter += 8;
                break;

            case 0x23: // INC HL : 1,8
                reg.setHL(reg.getHL() + 1);
                cycleCounter += 8;
                break;

            case 0x24: // INC H : 1,4 : Z 0 H -
                priorValue = reg.getH();
                reg.setH(priorValue + 1);
                cycleCounter += 4;
                // Flags
                checkForZero(reg.getH());
                reg.clearFlagN();
                checkAddForHalfCarry(priorValue, reg.getH(), MASK_HALF_BYTE);
                break;

            case 0x25: // DEC H : 1,4 : Z 1 H -
                priorValue = reg.getH();
                reg.setH(priorValue - 1);
                cycleCounter += 4;
                // Flags
                checkForZero(reg.getH());
                reg.setFlagN();
                checkSubForHalfCarry(priorValue, reg.getH(), MASK_HALF_BYTE);
                break;

            case 0x26: // LD H,d8 : 2,8
                reg.setH(mem.readByte(reg.getThenIncPC()));
                cycleCounter += 8;
                break;

            case 0x27: // DAA : 1,4 : Z - 0 C
                if (reg.isSetH() || (reg.getA() & MASK_HALF_BYTE) > 9) {
                    reg.setA(reg.getA() + 0x06);
                }

                if (reg.isSetCy() || (reg.getA() & MASK_HIGH_NIBBLE) > 9) {
                    reg.setA(reg.getA() + 0x60);
                    reg.setFlagCy();
                } else {
                    reg.clearFlagCy();
                }

                checkForZero(reg.getA());
                reg.clearFlagH();
                cycleCounter += 4;
                break;

            case 0x28: // JR Z,r8 : 2,12/8
                if (reg.isSetZ()) {
                    operand = mem.readByte(reg.getThenIncPC());
                    reg.setPC(reg.getPC() + (byte) operand);
                    cycleCounter += 12;
                } else {
                    reg.incPC(); // Move PC past byte of immediate data
                    cycleCounter += 8;
                }
                break;

            case 0x29: // ADD HL,HL : 1,8 : - 0 H C
                priorValue = reg.getHL();
                reg.setHL(priorValue * 2);
                cycleCounter += 8;
                // Flags
                reg.clearFlagN();
                checkAddForHalfCarry(priorValue, reg.getHL(), MASK_BYTE_PLUS_NIBBLE);
                checkAddForCarry(priorValue, reg.getHL());
                break;

            case 0x2A: // LD A,(HL+) : 1,8
                reg.setA(mem.readByte(reg.getHL()));
                reg.setHL(reg.getHL() + 1);
                cycleCounter += 8;
                break;

            case 0x2B: // DEC HL : 1,8
                reg.setHL(reg.getHL() - 1);
                cycleCounter += 8;
                break;

            case 0x2C: // INC L : 1,4 : Z 0 H -
                priorValue = reg.getL();
                reg.setL(priorValue + 1);
                cycleCounter += 4;
                // Flags
                checkForZero(reg.getL());
                reg.clearFlagN();
                checkAddForHalfCarry(priorValue, reg.getL(), MASK_HALF_BYTE);
                break;

            case 0x30: // JR NC,r8 : 2,12/8
                if (reg.isSetCy()) {
                    reg.incPC();
                    cycleCounter += 8;
                } else {
                    operand = mem.readByte(reg.getThenIncPC());
                    reg.incPC((byte) operand);
                    cycleCounter += 12;
                }
                break;

            case 0x31: // LD SP,d16 : 3,12
                reg.setSP(mem.readWord(reg.getThenIncPC(2)));
                cycleCounter += 12;
                break;

            case 0x32: // LD (HL-),A : 1,8
                mem.writeByte(reg.getA(), reg.getHL());
                reg.setHL(reg.getHL() - 1);
                cycleCounter += 8;
                break;

            case 0x33: // INC SP : 1,8
                reg.setSP(reg.getSP() + 1);
                cycleCounter += 8;
                break;

            case 0x34: // INC (HL) : 1,12 : Z 0 H -
                priorValue = mem.readWord(reg.getHL());
                mem.writeWord(priorValue + 1, reg.getHL());
                cycleCounter += 12;
                // Flags
                checkForZero(mem.readWord(reg.getHL()));
                reg.clearFlagN();
                checkAddForHalfCarry(priorValue, mem.readWord(reg.getHL()), MASK_BYTE_PLUS_NIBBLE);
                break;

            case 0x35: // DEC (HL) : 1,12 : Z 1 H -
                priorValue = mem.readWord(reg.getHL());
                mem.writeWord(priorValue - 1, reg.getHL());
                cycleCounter += 12;
                // Flags
                checkForZero(mem.readWord(reg.getHL()));
                reg.clearFlagN();
                checkSubForHalfCarry(priorValue, mem.readWord(reg.getHL()), MASK_BYTE_PLUS_NIBBLE);
                break;

            case 0x36: // LD (HL),d8 : 2,12
                mem.writeByte(mem.readByte(reg.getThenIncPC(1)), reg.getHL());
                cycleCounter += 12;
                break;

            case 0x37: // SCF : 1,4 : - 0 0 1
                reg.clearFlagN();
                reg.clearFlagH();
                reg.setFlagCy();
                cycleCounter += 4;
                break;

            case 0x38: // JR C,r8 : 2,12/8
                if (reg.isSetCy()) {
                    operand = mem.readByte(reg.getThenIncPC());
                    reg.incPC((byte) operand);
                    cycleCounter += 12;
                } else {
                    reg.incPC();
                    cycleCounter += 8;
                }
                break;

            case 0x39: // ADD HL,SP : 1,8 : - 0 H C
                priorValue = reg.getHL();
                reg.setHL(priorValue + reg.getSP());
                // Flags
                reg.clearFlagN();
                checkAddForHalfCarry(priorValue, reg.getHL(), MASK_BYTE_PLUS_NIBBLE);
                checkAddForCarry(priorValue, reg.getHL());
                cycleCounter += 8;
                break;

            case 0x3A: // LD A,(HL-) : 1,8
                reg.setA(mem.readByte(reg.getHL()));
                reg.setHL(reg.getHL() + 1);
                cycleCounter += 8;
                break;

            case 0x3B: // DEC SP : 1,8
                reg.setSP(reg.getSP() - 1);
                cycleCounter += 8;
                break;

            case 0x3C: // INC A : 1,4 : Z 0 H -
                priorValue = reg.getA();
                reg.setA(priorValue + 1);
                cycleCounter += 4;
                //Flags
                checkForZero(reg.getA());
                reg.clearFlagN();
                checkAddForHalfCarry(priorValue, reg.getA(), MASK_HALF_BYTE);
                break;

            case 0x3D: // DEC A : 1,4 : Z 1 H -
                priorValue = reg.getA();
                reg.setA(priorValue - 1);
                cycleCounter += 4;
                // Flags
                checkForZero(reg.getA());
                reg.setFlagN();
                checkSubForHalfCarry(priorValue, reg.getA(), MASK_HALF_BYTE);
                break;

            case 0x3E: // LD A,d8 : 2,8
                reg.setA(mem.readByte(reg.getThenIncPC(1)));
                cycleCounter += 8;
                break;

            case 0x3F: // CCF : 1,4 : - 0 0 C
                cycleCounter += 4;
                // Flags
                reg.clearFlagN();
                reg.clearFlagH();
                if (reg.isSetCy()) {
                    reg.clearFlagCy();
                } else {
                    reg.setFlagCy();
                }
                break;

            case 0x40: // LD B,B : 1,4
                // NOP
                cycleCounter += 4;
                break;

            case 0x41: // LD B,C : 1,4
                reg.setB(reg.getC());
                cycleCounter += 4;
                break;

            case 0x42: // LD B,D : 1,4
                reg.setB(reg.getD());
                cycleCounter += 4;
                break;

            case 0x43: // LD B,E : 1,4
                reg.setB(reg.getE());
                cycleCounter += 4;
                break;

            case 0x44: // LD B,H : 1,4
                reg.setB(reg.getH());
                cycleCounter += 4;
                break;

            case 0x45: // LD B,L : 1,4
                reg.setB(reg.getL());
                cycleCounter += 4;
                break;

            case 0x46: // LD B,(HL) : 1,8
                reg.setB(mem.readByte(reg.getHL()));
                cycleCounter += 8;
                break;

            case 0x47: // LD B,A : 1,4
                reg.setB(reg.getA());
                cycleCounter += 4;
                break;

            case 0x48: // LD C,B : 1,4
                reg.setC(reg.getB());
                cycleCounter += 4;
                break;

            case 0x49: // LD C,C : 1,4
                // NOP
                cycleCounter += 4;
                break;

            case 0x4A: // LD C,D : 1,4
                reg.setC(reg.getD());
                cycleCounter += 4;
                break;

            case 0x4B: // LD C,E : 1,4
                reg.setC(reg.getE());
                cycleCounter += 4;
                break;

            case 0x4C: // LD C,E : 1,4
                reg.setC(reg.getE());
                cycleCounter += 4;
                break;

            case 0x4D: // LD C,L : 1,4
                reg.setC(reg.getL());
                cycleCounter += 4;
                break;

            case 0x4E: // LD C,(HL) : 1,8
                reg.setC(mem.readByte(reg.getHL()));
                cycleCounter += 8;
                break;

            case 0x4F: // LD C,A : 1,4
                reg.setC(reg.getA());
                cycleCounter += 4;
                break;

            case 0x50: // LD D,B : 1,4
                reg.setD(reg.getB());
                cycleCounter += 4;
                break;

            case 0x51: // LD D,C : 1,4
                reg.setD(reg.getC());
                cycleCounter += 4;
                break;

            case 0x52: // LD D,D : 1,4
                // NOP
                cycleCounter += 4;
                break;

            case 0x53: // LD D,E : 1,4
                reg.setD(reg.getE());
                cycleCounter += 4;
                break;

            case 0x54: // LD D,H : 1,4
                reg.setD(reg.getH());
                cycleCounter += 4;
                break;

            case 0x55: // LD D,L : 1,4
                reg.setD(reg.getL());
                cycleCounter += 4;
                break;

            case 0x56: // LD D,(HL) : 1,8
                reg.setD(mem.readByte(reg.getHL()));
                cycleCounter += 8;
                break;

            case 0x57: // LD D,A : 1,4
                reg.setD(reg.getA());
                cycleCounter += 4;
                break;

            case 0x58: // LD E,B : 1,4
                reg.setE(reg.getB());
                cycleCounter += 4;
                break;

            case 0x59: // LD E,C : 1,4
                reg.setE(reg.getC());
                cycleCounter += 4;
                break;

            case 0x5A: // LD E,D : 1,4
                reg.setE(reg.getD());
                cycleCounter += 4;
                break;

            case 0x5B: // LD E,E : 1,4
                // NOP
                cycleCounter += 4;
                break;

            case 0x5C: // LD E,H : 1,4
                reg.setE(reg.getH());
                cycleCounter += 4;
                break;

            case 0x5D: // LD E,L : 1,4
                reg.setE(reg.getL());
                cycleCounter += 4;
                break;

            case 0x5E: // LD E,(HL) : 1,8
                reg.setE(mem.readByte(reg.getHL()));
                cycleCounter += 8;
                break;

            case 0x5F: // LD E,A : 1,4
                reg.setE(reg.getA());
                cycleCounter += 4;
                break;

            case 0x60: // LD H,B : 1,4
                reg.setH(reg.getB());
                cycleCounter += 4;
                break;

            case 0x61: // LD H,C : 1,4
                reg.setH(reg.getC());
                cycleCounter += 4;
                break;

            case 0x62: // LD H,D : 1,4
                reg.setH(reg.getD());
                cycleCounter += 4;
                break;

            case 0x63: // LD H,E : 1,4
                reg.setH(reg.getE());
                cycleCounter += 4;
                break;

            case 0x64: // LD H,H : 1,4
                // NOP
                cycleCounter += 4;
                break;

            case 0x65: // LD H,L : 1,4
                reg.setH(reg.getL());
                cycleCounter += 4;
                break;

            case 0x66: // LD H,(HL) : 1,8
                reg.setH(mem.readByte(reg.getHL()));
                cycleCounter += 8;
                break;

            case 0x67: // LD H,A : 1,4
                reg.setH(reg.getA());
                cycleCounter += 4;
                break;

            case 0x68: // LD L,B : 1,4
                reg.setL(reg.getB());
                cycleCounter += 4;
                break;

            case 0x69: // LD L,C : 1,4
                reg.setL(reg.getC());
                cycleCounter += 4;
                break;

            case 0x6A: // LD L,D : 1,4
                reg.setL(reg.getD());
                cycleCounter += 4;
                break;

            case 0x6B: // LD L,E : 1,4
                reg.setL(reg.getE());
                cycleCounter += 4;
                break;

            case 0x6C: // LD L,H : 1,4
                reg.setL(reg.getH());
                cycleCounter += 4;
                break;

            case 0x6D: // LD L,L : 1,4
                // NOP
                cycleCounter += 4;
                break;

            case 0x6E: // LD L,(HL) : 1,8
                reg.setL(mem.readByte(reg.getHL()));
                cycleCounter += 8;
                break;

            case 0x6F: // LD L,A : 1,4
                reg.setL(reg.getA());
                cycleCounter += 4;
                break;

            case 0x70: // LD (HL),B : 1,8
                mem.writeByte(reg.getB(), reg.getHL());
                cycleCounter += 8;
                break;

            case 0x71: // LD (HL),C : 1,8
                mem.writeByte(reg.getC(), reg.getHL());
                cycleCounter += 8;
                break;

            case 0x72: // LD (HL),D : 1,8
                mem.writeByte(reg.getD(), reg.getHL());
                cycleCounter += 8;
                break;

            case 0x73: // LD (HL),E : 1,8
                mem.writeByte(reg.getE(), reg.getHL());
                cycleCounter += 8;
                break;

            case 0x74: // LD (HL),H : 1,8
                mem.writeByte(reg.getH(), reg.getHL());
                cycleCounter += 8;
                break;

            case 0x75: // LD (HL),L : 1,8
                mem.writeByte(reg.getL(), reg.getHL());
                cycleCounter += 8;
                break;

            case 0x76: // HALT : 1,4
                // TODO: Figure out how to implement
                cycleCounter += 4;
                break;

            case 0x77: // LD (HL),A : 1,8
                mem.writeByte(reg.getA(), reg.getHL());
                cycleCounter += 8;
                break;

            case 0x78: // LD A,B : 1,4
                reg.setA(reg.getB());
                cycleCounter += 4;
                break;

            case 0x79: // LD A,C : 1,4
                reg.setA(reg.getC());
                cycleCounter += 4;
                break;

            case 0x7A: // LD A,D : 1,4
                reg.setA(reg.getD());
                cycleCounter += 4;
                break;

            case 0x7B: // LD A,E : 1,4
                reg.setA(reg.getE());
                cycleCounter += 4;
                break;

            case 0x7C: // LD A,H : 1,4
                reg.setA(reg.getH());
                cycleCounter += 4;
                break;

            case 0x7D: // LD A,L : 1,4
                reg.setA(reg.getL());
                cycleCounter += 4;
                break;

            case 0x7E: // LD A,(HL) : 1,8
                mem.writeByte(reg.getA(), reg.getHL());
                cycleCounter += 8;
                break;

            case 0x7F: // LD A,A : 1,4
                // NOP
                cycleCounter += 4;
                break;

            case 0x80: // ADD A,B : 1,4 : Z 0 H C
                addByteToByte(reg::getA, reg::setA, reg::getB, false);
                cycleCounter += 4;
                break;

            case 0x81: // ADD A,C : 1,4 : Z 0 H C
                addByteToByte(reg::getA, reg::setA, reg::getC, false);
                cycleCounter += 4;
                break;

            case 0x82: // ADD A,D : 1,4 : Z 0 H C
                addByteToByte(reg::getA, reg::setA, reg::getD, false);
                cycleCounter += 4;
                break;

            case 0x83: // ADD A,E : 1,4 : Z 0 H C
                addByteToByte(reg::getA, reg::setA, reg::getE, false);
                cycleCounter += 4;
                break;

            case 0x84: // ADD A,H : 1,4 : Z 0 H C
                addByteToByte(reg::getA, reg::setA, reg::getH, false);
                cycleCounter += 4;
                break;

            case 0x85: // ADD A,L : 1,4 : Z 0 H C
                addByteToByte(reg::getA, reg::setA, reg::getL, false);
                cycleCounter += 4;
                break;

            case 0x87: // ADD A,A : 1,4 : Z 0 H C
                addByteToByte(reg::getA, reg::setA, reg::getA, false);
                cycleCounter += 4;
                break;

            case 0x88: // ADC A,B : 1,4 : Z 0 H C
                addByteToByte(reg::getA, reg::setA, reg::getB, true);
                cycleCounter += 4;
                break;

            case 0x89: // ADC A,C : 1,4 : Z 0 H C
                addByteToByte(reg::getA, reg::setA, reg::getC, true);
                cycleCounter += 4;
                break;

            case 0x8A: // ADC A,D : 1,4 : Z 0 H C
                addByteToByte(reg::getA, reg::setA, reg::getD, true);
                cycleCounter += 4;
                break;

            case 0x8B: // ADC A,E : 1,4 : Z 0 H C
                addByteToByte(reg::getA, reg::setA, reg::getE, true);
                cycleCounter += 4;
                break;

            case 0x8C: // ADC A,H : 1,4 : Z 0 H C
                addByteToByte(reg::getA, reg::setA, reg::getH, true);
                cycleCounter += 4;
                break;

            case 0x8D: // ADC A,L : 1,4 : Z 0 H C
                addByteToByte(reg::getA, reg::setA, reg::getL, true);
                cycleCounter += 4;
                break;

            case 0x8F: // ADC A,A : 1,4 : Z 0 H C
                addByteToByte(reg::getA, reg::setA, reg::getA, true);
                cycleCounter += 4;
                break;

            case 0x90: // SUB B : 1,4 : Z 1 H C
                subtractByteFromByte(reg::getA, reg::setA, reg::getB, false);
                cycleCounter += 4;
                break;

            case 0x91: // SUB C : 1,4 : Z 1 H C
                subtractByteFromByte(reg::getA, reg::setA, reg::getC, false);
                cycleCounter += 4;
                break;

            case 0x92: // SUB D : 1,4 : Z 1 H C
                subtractByteFromByte(reg::getA, reg::setA, reg::getD, false);
                cycleCounter += 4;
                break;

            case 0x93: // SUB E : 1,4 : Z 1 H C
                subtractByteFromByte(reg::getA, reg::setA, reg::getE, false);
                cycleCounter += 4;
                break;

            case 0x94: // SUB H : 1,4 : Z 1 H C
                subtractByteFromByte(reg::getA, reg::setA, reg::getH, false);
                cycleCounter += 4;
                break;

            case 0x95: // SUB L : 1,4 : Z 1 H C
                subtractByteFromByte(reg::getA, reg::setA, reg::getL, false);
                cycleCounter += 4;
                break;

            case 0x97: // SUB A : 1,4 : Z 1 H C
                subtractByteFromByte(reg::getA, reg::setA, reg::getA, false);
                cycleCounter += 4;
                break;

            case 0x98: // SBC A,B : 1,4 : Z 1 H C
                subtractByteFromByte(reg::getA, reg::setA, reg::getB, true);
                cycleCounter += 4;
                break;

            case 0x99: // SBC A,C : 1,4 : Z 1 H C
                subtractByteFromByte(reg::getA, reg::setA, reg::getC, true);
                cycleCounter += 4;
                break;

            case 0x9A: // SBC A,D : 1,4 : Z 1 H C
                subtractByteFromByte(reg::getA, reg::setA, reg::getD, true);
                cycleCounter += 4;
                break;

            case 0x9B: // SBC A,E : 1,4 : Z 1 H C
                subtractByteFromByte(reg::getA, reg::setA, reg::getE, true);
                cycleCounter += 4;
                break;

            case 0x9C: // SBC A,H : 1,4 : Z 1 H C
                subtractByteFromByte(reg::getA, reg::setA, reg::getH, true);
                cycleCounter += 4;
                break;

            case 0x9D: // SBC A,L : 1,4 : Z 1 H C
                subtractByteFromByte(reg::getA, reg::setA, reg::getL, true);
                cycleCounter += 4;
                break;

            case 0x9F: // SBC A,A : 1,4 : Z 1 H C
                subtractByteFromByte(reg::getA, reg::setA, reg::getA, true);
                cycleCounter += 4;
                break;

            case 0xA0: // AND B : 1,4 : Z 0 1 0
                bitwiseAndByteWithAccumulator(reg::getB, 4);
                break;

            case 0xA1: // AND C : 1,4 : Z 0 1 0
                bitwiseAndByteWithAccumulator(reg::getC, 4);
                break;

            case 0xA2: // AND D : 1,4 : Z 0 1 0
                bitwiseAndByteWithAccumulator(reg::getD, 4);
                break;

            case 0xA3: // AND E : 1,4 : Z 0 1 0
                bitwiseAndByteWithAccumulator(reg::getE, 4);
                break;

            case 0xA4: // AND H : 1,4 : Z 0 1 0
                bitwiseAndByteWithAccumulator(reg::getH, 4);
                break;

            case 0xA5: // AND L : 1,4 : Z 0 1 0
                bitwiseAndByteWithAccumulator(reg::getL, 4);
                break;

            case 0xA6: // AND (HL) : 1,8 : Z 0 1 0
                bitwiseAndByteWithAccumulator(() -> mem.readByte(reg.getHL()), 8);
                break;

            case 0xA7: // AND A : 1,4 : Z 0 1 0
                bitwiseAndByteWithAccumulator(reg::getA, 4);
                break;

            case 0xA8: // XOR B : 1,4 : Z 0 0 0
                bitwiseXorByteWithAccumulator(reg::getB, 4);
                break;

            case 0xA9: // XOR C : 1,4 : Z 0 0 0
                bitwiseXorByteWithAccumulator(reg::getC, 4);
                break;

            case 0xAA: // XOR D : 1,4 : Z 0 0 0
                bitwiseXorByteWithAccumulator(reg::getD, 4);
                break;

            case 0xAB: // XOR E : 1,4 : Z 0 0 0
                bitwiseXorByteWithAccumulator(reg::getE, 4);
                break;

            case 0xAC: // XOR H : 1,4 : Z 0 0 0
                bitwiseXorByteWithAccumulator(reg::getH, 4);
                break;

            case 0xAD: // XOR L : 1,4 : Z 0 0 0
                bitwiseXorByteWithAccumulator(reg::getL, 4);
                break;

            case 0xAE: // XOR (HL) : 1,8 : Z 0 0 0
                bitwiseXorByteWithAccumulator(() -> mem.readByte(reg.getHL()), 8);
                break;

            case 0xAF: // XOR A : 1,4 : Z 0 0 0
                bitwiseXorByteWithAccumulator(reg::getA, 4);
                break;

            case 0xB0: // OR B : 1,4 : Z 0 0 0
                orByteWithA(reg::getB);
                break;

            case 0xB1: // OR C : 1,4 : Z 0 0 0
                orByteWithA(reg::getC);
                break;

            case 0xB2: // OR D : 1,4 : Z 0 0 0
                orByteWithA(reg::getD);
                break;

            case 0xB3: // OR E : 1,4 : Z 0 0 0
                orByteWithA(reg::getE);
                break;

            case 0xB4: // OR H : 1,4 : Z 0 0 0
                orByteWithA(reg::getH);
                break;

            case 0xB5: // OR L : 1,4 : Z 0 0 0
                orByteWithA(reg::getL);
                break;

            case 0xB6: // OR (HL) : 1,8 : Z 0 0 0
                orByteWithA(() -> mem.readWord(reg.getHL()));
                // add 4 cycles for memory load to 4 cycles for OR
                cycleCounter += 4;
                break;

            case 0xB7: // OR A : 1,4 : Z 0 0 0
                orByteWithA(reg::getA);
                break;

            case 0xC0: // RET NZ : 1,20/8
                if (reg.isSetZ()) {
                    cycleCounter += 8;
                } else {
                    reg.setPC(mem.readWord(reg.getSP()));
                    reg.incSP(2);
                    cycleCounter += 20;
                }
                break;

            case 0xC1: // POP BC : 1,12
                reg.setBC(mem.readWord(reg.getSP()));
                reg.incSP(2);
                cycleCounter += 12;
                break;

            case 0xC2: // JP NZ,a16 : 3,16/12
                tempAddr = mem.readWord(reg.getThenIncPC(2));
                if (reg.isSetZ()) {
                    cycleCounter += 12;
                } else {
                    reg.setPC(tempAddr);
                    cycleCounter += 16;
                }
                break;

            case 0xC3: // JP a16 : 3,16
                reg.setPC(mem.readWord(reg.getThenIncPC(2)));
                cycleCounter += 16;
                break;

            case 0xC4: // CALL NZ,a16 : 3,24/12
                tempAddr = mem.readWord(reg.getThenIncPC(2));
                if (reg.isSetZ()) {
                    cycleCounter += 12;
                } else {
                    reg.decSP(2);
                    mem.writeWord(reg.getPC(), reg.getSP());
                    reg.setPC(tempAddr);
                    cycleCounter += 24;
                }
                break;

            case 0xC5: // PUSH BC : 1,16
                reg.decSP(2);
                mem.writeWord(reg.getBC(), reg.getSP());
                cycleCounter += 16;
                break;

            case 0xC6: // ADD A,d8 : 2,8 : Z 0 H C
                operand = mem.readByte(reg.getThenIncPC(1));
                priorValue = reg.getA();
                reg.setA(priorValue + operand);
                cycleCounter += 8;
                // Flags
                checkForZero(reg.getA());
                reg.clearFlagN();
                checkAddForHalfCarry(priorValue, reg.getA(), MASK_HALF_BYTE);
                checkAddForCarry(priorValue, reg.getA());
                break;

            case 0xC7: // RST 00H : 1,16
                rstHelper(0x00);
                break;

            case 0xC8: // RET Z : 1,20/8
                if (reg.isSetZ()) {
                    reg.setPC(mem.readWord(reg.getSP()));
                    reg.incSP(2);
                    cycleCounter += 20;
                } else {
                    cycleCounter += 8;
                }
                break;

            case 0xC9: // RET : 1,16
                reg.setPC(mem.readWord(reg.getSP()));
                reg.incSP(2);
                cycleCounter += 16;
                break;

            case 0xCA: // JP Z,a16 : 3,16/12
                tempAddr = mem.readWord(reg.getThenIncPC(2));
                if (reg.isSetZ()) {
                    reg.setPC(tempAddr);
                    cycleCounter += 16;
                } else {
                    cycleCounter += 12;
                }
                break;

            // TODO: Process CB prefix opcodes here.

            case 0xCC: // CALL Z,a16 : 3,24/12
                tempAddr = mem.readWord(reg.getThenIncPC(2));
                if (reg.isSetZ()) {
                    reg.decSP(2);
                    mem.writeWord(reg.getPC(), reg.getSP());
                    reg.setPC(tempAddr);
                    cycleCounter += 24;
                } else {
                    cycleCounter += 12;
                }
                break;

            case 0xCD: // CALL a16 : 3,24
                tempAddr = mem.readWord(reg.getThenIncPC(2));
                reg.decSP(2);
                mem.writeWord(reg.getPC(), reg.getSP());
                reg.setPC(tempAddr);
                cycleCounter += 24;
                break;

            case 0xCE: // ADC A,d8 : 2,8 : Z 0 H C
                addByteToByte(reg::getA, reg::setA, () -> mem.readByte(reg.getPC()), true);
                reg.incPC(1);
                cycleCounter += 8;
                break;
            
            case 0xCF: // RST 08H : 1,16
                rstHelper(0x08);
                break;

            case 0xD0: // RET NC : 1,20/8
                if (reg.isSetCy()) {
                    cycleCounter += 8;
                } else {
                    reg.setPC(mem.readWord(reg.getSP()));
                    reg.incSP(2);
                    cycleCounter += 20;
                }
                break;

            case 0xD1: // POP DE : 1,12
                reg.setDE(mem.readWord(reg.getSP()));
                reg.incSP(2);
                cycleCounter += 12;
                break;

            case 0xD2: // JP NC,a16 : 3,16/12
                tempAddr = mem.readWord(reg.getThenIncPC(2));
                if (reg.isSetCy()) {
                    cycleCounter += 12;
                } else {
                    reg.setPC(tempAddr);
                    cycleCounter += 16;
                }
                break;

            // TODO: do something with opcodes that don't exist (e.g. 0xD3)

            case 0xD4: // CALL NC,a16 : 3,24/12
                tempAddr = mem.readWord(reg.getThenIncPC(2));
                if (reg.isSetCy()) {
                    cycleCounter += 12;
                } else {
                    reg.decSP(2);
                    mem.writeWord(reg.getPC(), reg.getSP());
                    reg.setPC(tempAddr);
                    cycleCounter += 24;
                }
                break;

            case 0xD5: // PUSH DE : 1,16
                reg.decSP(2);
                mem.writeWord(reg.getDE(), reg.getSP());
                cycleCounter += 16;
                break;

            case 0xD6: // SUB d8 : 2,8 : Z 1 H C
                priorValue = reg.getA();
                operand = mem.readByte(reg.getThenIncPC(1));
                reg.setA(priorValue - operand);
                cycleCounter += 8;
                // Flags
                checkForZero(reg.getA());
                reg.setFlagN();
                checkSubForHalfCarry(priorValue, reg.getA(), MASK_HALF_BYTE);
                checkSubForCarry(priorValue, reg.getA());
                break;
            
            case 0xD7: // RST 10H : 1,16
                rstHelper(0x10);
                break;

            case 0xD8: // RET C : 1,20/8
                if (reg.isSetCy()) {
                    reg.setPC(mem.readWord(reg.getSP()));
                    reg.incSP(2);
                    cycleCounter += 20;
                } else {
                    cycleCounter += 8;
                }
                break;

            case 0xD9: // RETI : 1,16
                reg.setPC(mem.readWord(reg.getSP()));
                reg.incSP(2);
                cycleCounter += 16;
                interruptMasterEnableFlag = true;
                break;

            case 0xDA: // JP C,a16 : 3,16/12
                tempAddr = mem.readWord(reg.getThenIncPC(2));
                if (reg.isSetCy()) {
                    reg.setPC(tempAddr);
                    cycleCounter += 16;
                } else {
                    cycleCounter += 12;
                }
                break;

            case 0xDC: // CALL C,a16 : 3,24/12
                tempAddr = mem.readWord(reg.getThenIncPC(2));
                if (reg.isSetCy()) {
                    reg.decSP(2);
                    mem.writeWord(reg.getPC(), reg.getSP());
                    reg.setPC(tempAddr);
                    cycleCounter += 24;
                } else {
                    cycleCounter += 12;
                }
                break;

            case 0xDE: // SBC A,d8 : 2,8
                subtractByteFromByte(reg::getA, reg::setA, () -> mem.readByte(reg.getPC()), true);
                reg.incPC(1);
                cycleCounter += 8;
                break;
            
            case 0xDF: // RST 18H : 1,16
                rstHelper(0x18);
                break;

            case 0xE0: // LDH (a8),A : 2,12
                operand = mem.readByte(reg.getThenIncPC(1));
                mem.writeByte(reg.getA(), MASK_HIGH_BYTE + operand);
                cycleCounter += 12;
                break;

            case 0xE1: // POP HL : 1,12
                reg.setHL(mem.readWord(reg.getSP()));
                reg.incSP(2);
                cycleCounter += 12;
                break;

            case 0xE2: // LD (C),A : 2,8
                tempAddr = MASK_HIGH_BYTE + reg.getC();
                mem.writeByte(reg.getA(), tempAddr);
                cycleCounter += 8;
                break;

            case 0xE5: // PUSH HL : 1,16
                reg.decSP(2);
                mem.writeWord(reg.getHL(), reg.getSP());
                cycleCounter += 16;
                break;

            case 0xE6: // AND d8 : 2,8 : Z 0 1 0
                bitwiseAndByteWithAccumulator(() -> mem.readByte(reg.getPC()), 8);
                reg.incPC(1);
                break;

            case 0xE7: // RST 20H : 1,16
                rstHelper(0x20);
                break;

            case 0xE8: // ADD SP,r8 : 2,16 : 0 0 H C
                operand = mem.readByte(reg.getThenIncPC(1));
                reg.setSP(reg.getSP() + (byte) operand);
                cycleCounter += 16;
                break;

            case 0xE9: // JP (HL) : 1,4
                reg.setPC(mem.readWord(reg.getHL()));
                cycleCounter += 4;
                break;

            case 0xEA: // LD (a16),A : 3,16
                tempAddr = mem.readWord(reg.getThenIncPC(2));
                mem.writeByte(reg.getA(), tempAddr);
                cycleCounter += 16;
                break;

            case 0xEE: // XOR d8 : 2,8 : Z 0 0 0
                bitwiseXorByteWithAccumulator(() -> mem.readByte(reg.getPC()), 8);
                reg.incPC(1);
                break;

            case 0xEF: // RST 28H : 1,16
                rstHelper(0x28);
                break;

            case 0xF0: // LDH A,(a8) : 2,12
                operand = mem.readByte(reg.getThenIncPC(1));
                reg.setA(mem.readByte(MASK_HIGH_BYTE + operand));
                cycleCounter += 12;
                break;

            case 0xF1: // POP AF : 1,12 : Z N H C
                // Note, we do not have to manually process the flags. They are altered
                // because the F register is being restored!
                reg.setAF(mem.readWord(reg.getSP()));
                reg.incSP(2);
                cycleCounter += 12;
                break;

            case 0xF2: // LD A,(C) : 1,8
                tempAddr = MASK_HIGH_BYTE + reg.getC();
                reg.setA(mem.readByte(tempAddr));
                cycleCounter += 8;
                break;

            case 0xF3: // DI : 1,4
                // TODO: crap... enabling and disabling interrupts happens after the NEXT inst...
                interruptMasterEnableFlag = false;
                cycleCounter += 4;
                break;

            case 0xF5: // PUSH AF : 1,16
                reg.decSP(2);
                mem.writeWord(reg.getAF(), reg.getSP());
                cycleCounter += 16;
                break;

            // TODO: opcode 0xF6

            case 0xF7: // RST 30H : 1,16
                rstHelper(0x30);
                break;

            case 0xF8: // LD HL,SP+r8 : 2,12 : 0 0 H C
                operand = mem.readByte(reg.getThenIncPC(1));
                priorValue = reg.getSP();
                reg.setHL(priorValue + (byte) operand);
                cycleCounter += 12;
                // Flags
                // TODO: what are the half-carry and carry flags even based on here???
                reg.clearFlagZ();
                reg.clearFlagN();
                checkAddForHalfCarry(priorValue, reg.getHL(), MASK_BYTE_PLUS_NIBBLE);
                checkAddForCarry(priorValue, reg.getHL());
                break;

            case 0xF9: // LD SP,HL : 1,8
                reg.setSP(reg.getHL());
                cycleCounter += 8;
                break;

            case 0xFA: // LD A,(a16) : 3,16
                tempAddr = mem.readWord(reg.getThenIncPC(2));
                reg.setA(mem.readByte(tempAddr));
                cycleCounter += 16;
                break;

            case 0xFB: // EI : 1,4
                // TODO: crap... enabling and disabling interrupts happens after the NEXT inst...
                interruptMasterEnableFlag = true;
                cycleCounter += 4;
                break;

            case 0xFE: // CP d8 : 2,8 : Z 1 H C
                operand = mem.readByte(reg.getThenIncPC(1));
                tempResult = reg.getA() - operand;
                cycleCounter += 8;
                // Flags
                checkForZero(tempResult);
                reg.setFlagN();
                checkSubForHalfCarry(reg.getA(), tempResult, MASK_HALF_BYTE);
                checkSubForCarry(reg.getA(), tempResult);
                break;

            case 0xFF: // RST 38H : 1,16
                rstHelper(0x38);
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

    // Opcode helper methods

    private void checkForZero(int newValue) {
        if (newValue == 0) {
            reg.setFlagZ();
        } else {
            reg.clearFlagZ();
        }
    }

    /**
     * Evaluate for a half-carry after an add or increment and set the flag as appropriate.
     * @param byteMask The appropriate mask for the half-carry. I.e. MASK_HALF_BYTE (0xF) for 8-bit register/data and
     *                 MASK_BYTE_PLUS_NIBBLE (0xFFF) for 16-bit register/data.
     */
    private void checkAddForHalfCarry(int priorValue, int newValue, int byteMask) {
        priorValue &= byteMask;
        newValue &= byteMask;
        if (newValue < priorValue) {
            reg.setFlagH();
        } else {
            reg.clearFlagH();
        }
    }

    private void checkAddForCarry(int priorValue, int newValue) {
        if (newValue < priorValue) {
            reg.setFlagCy();
        } else {
            reg.clearFlagCy();
        }
    }

    /**
     * Evaluate for a half-carry after a subtract or decrement and set the flag as appropriate.
     * @param byteMask The appropriate mask for the half-carry. I.e. MASK_HALF_BYTE (0xF) for 8-bit register/data and
     *                 MASK_BYTE_PLUS_NIBBLE (0xFFF) for 16-bit register/data.
     */
    private void checkSubForHalfCarry(int priorValue, int newValue, int byteMask) {
        priorValue &= byteMask;
        newValue &= byteMask;
        if (newValue > priorValue) {
            reg.setFlagH();
        } else {
            reg.clearFlagH();
        }
    }

    private void checkSubForCarry(int priorValue, int newValue) {
        if (newValue > priorValue) {
            reg.setFlagCy();
        } else {
            reg.clearFlagCy();
        }
    }

    /**
     *
     * @param getSrc Byte register getter to compare with A
     */
    private void orByteWithA(IntSupplier getSrc) {
        // flags
        checkForZero(reg.getA() | getSrc.getAsInt());
        reg.clearFlagN();
        reg.clearFlagH();
        reg.clearFlagCy();
        cycleCounter += 4;
    }

    /**
     * ADD [byte],[byte] : 1,4 : Z 0 H C
     * ADC [byte],[byte] : 1,4 : Z 0 H C
     * @param getDest Byte register getter
     * @param setDest Byte register setter
     * @param getSrc Byte register getter
     * @param withCarry Whether to add with carry
     */
    private void addByteToByte(IntSupplier getDest, IntConsumer setDest, IntSupplier getSrc, boolean withCarry) {
        int priorValue = getDest.getAsInt();
        int carryModifier = 0;

        if (withCarry && reg.isSetCy()) {
            carryModifier = 1;
        }

        setDest.accept(priorValue + getSrc.getAsInt() + carryModifier);
        // Flags
        checkForZero(getDest.getAsInt());
        reg.clearFlagN();
        checkAddForHalfCarry(priorValue, getDest.getAsInt(), MASK_HALF_BYTE);
        checkAddForCarry(priorValue, getDest.getAsInt());
    }

    /**
     * SUB [byte],[byte] : 1,4 : Z 1 H C
     * SBC [byte],[byte] : 1,4 : Z 1 H C
     * @param getDest Byte register getter
     * @param setDest Byte register setter
     * @param getSrc Byte register getter
     * @param withCarry Whether to subtract with carry
     */
    private void subtractByteFromByte(IntSupplier getDest, IntConsumer setDest, IntSupplier getSrc, boolean withCarry) {
        int priorValue = getDest.getAsInt();
        int carryModifier = 0;

        if (withCarry && reg.isSetCy()) {
            carryModifier = 1;
        }

        setDest.accept(priorValue - getSrc.getAsInt() - carryModifier);
        // Flags
        checkForZero(getDest.getAsInt());
        reg.setFlagN();
        checkSubForHalfCarry(priorValue, getDest.getAsInt(), MASK_HALF_BYTE);
        checkSubForCarry(priorValue, getDest.getAsInt());
    }

    /**
     * AND [8-bit register or immediate] : _ : Z 0 1 0
     * @param byteGetter Method reference or lambda that supplies 1 byte.
     * @param cycles Number of cycles required for this operation.
     */
    private void bitwiseAndByteWithAccumulator(IntSupplier byteGetter, int cycles) {
        reg.setA(reg.getA() & byteGetter.getAsInt());
        cycleCounter += cycles;
        // Flags
        checkForZero(reg.getA());
        reg.clearFlagN();
        reg.setFlagH();
        reg.clearFlagCy();
    }

    /**
     * XOR [8-bit register or immediate] : _ : Z 0 0 0
     * @param byteGetter Method reference or lambda that supplies 1 byte.
     * @param cycles Number of cycles required for this operation.
     */
    private void bitwiseXorByteWithAccumulator(IntSupplier byteGetter, int cycles) {
        reg.setA(reg.getA() ^ byteGetter.getAsInt());
        cycleCounter += cycles;
        // Flags
        checkForZero(reg.getA());
        reg.clearFlagN();
        reg.clearFlagH();
        reg.clearFlagCy();
    }

    /**
     * RST [predefined 8-bit immediate] : 1,16
     * Note that the immediate is not actually an additional byte in the program.
     */
    private void rstHelper(int dest8) {
        reg.decSP(2);
        mem.writeWord(reg.getPC(), reg.getSP());
        reg.setPC(dest8);
        cycleCounter += 16; // TODO: find out if this is actually 32... sources disagree
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

        public void incSP(int n) {
            SP += n;
            SP &= MASK_WORD;
        }

        public void decSP(int n) {
            SP -= n;
            SP &= MASK_WORD;
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
