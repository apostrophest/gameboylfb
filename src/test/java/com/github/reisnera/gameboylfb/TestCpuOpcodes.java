/* This file is part of the GameBoyLFB project.
   GameBoyLFB - A Java Game Boy emulator.
   Copyright (C) 2015 Alex Reisner <thearcher at gmail dot com>

   This project is licensed under the GNU GPL v2 license and comes with
   absolutely no warranty of any kind. The full license can be found at:
   http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt */

package com.github.reisnera.gameboylfb;

import org.testng.annotations.*;

import static org.testng.Assert.*;
import static org.mockito.Mockito.*;

@Test
public class TestCpuOpcodes {

    private static final int TEST_BYTE = 0x99;
    private static final int TEST_WORD = 0x9999;

    private GameBoyMemory mem;
    private GameBoyCpu cpu;
    private GameBoyCpu.CpuRegisters reg;

    // Test helper functions

    private int buildWord(int high8, int low8) {
        assertTrue(high8 >=0 && high8 <= 0xFF, "Error in test code! High portion of register must be 8 bits!");
        assertTrue(low8 >=0 && low8 <= 0xFF, "Error in test code! Low portion of register must be 8 bits!");
        return (high8 << 8) | low8;
    }

    private void zeroRegisters() {
        setRegisters(0, 0, 0, 0, 0, 0, false, false, false, false);
    }

    private void setRegisters(int A, int BC, int DE, int HL, int SP, int PC,
                              boolean flagZ, boolean flagN, boolean flagH, boolean flagCy) {
        reg.setA(A);
        reg.setBC(BC);
        reg.setDE(DE);
        reg.setHL(HL);
        reg.setSP(SP);
        reg.setPC(PC);

        if (flagZ) {
            reg.setFlagZ();
        } else {
            reg.clearFlagZ();
        }

        if (flagN) {
            reg.setFlagN();
        } else {
            reg.clearFlagN();
        }

        if (flagH) {
            reg.setFlagH();
        } else {
            reg.clearFlagH();
        }

        if (flagCy) {
            reg.setFlagCy();
        } else {
            reg.clearFlagCy();
        }
    }

    private void assertRegisterValues(int A, int BC, int DE, int HL, int SP, int PC,
                                      boolean flagZ, boolean flagN, boolean flagH, boolean flagCy) {
        assertEquals(reg.getA(), A);
        assertEquals(reg.getBC(), BC);
        assertEquals(reg.getDE(), DE);
        assertEquals(reg.getHL(), HL);
        assertEquals(reg.getSP(), SP);
        assertEquals(reg.getPC(), PC);
        assertEquals(reg.isSetZ(), flagZ);
        assertEquals(reg.isSetN(), flagN);
        assertEquals(reg.isSetH(), flagH);
        assertEquals(reg.isSetCy(), flagCy);
    }

    @BeforeClass
    public void beforeClass() {
        mem = mock(GameBoyMemory.class);
        cpu = new GameBoyCpu(mem);
        reg = cpu.reg;
    }

    @BeforeMethod
    public void beforeMethod() {
        reset(mem);
    }

    // Opcode tests:

    // NOP
    public void testOpcode00() {
        int opcode = 0x00;
        zeroRegisters();

        cpu.processOpcode(opcode);

        verifyZeroInteractions(mem);
        assertRegisterValues(0, 0, 0, 0, 0, 0, false, false, false, false);
    }

    // LD BC,d16
    public void testOpcode01() {
        int opcode = 0x01;
        when(mem.readWord(anyInt())).thenReturn(TEST_WORD);
        zeroRegisters();

        cpu.processOpcode(opcode);

        verify(mem).readWord(anyInt());
        verifyNoMoreInteractions(mem);
        assertRegisterValues(0, TEST_WORD, 0, 0, 0, 2, false, false, false, false);
    }

    // LD (BC),A
    public void testOpcode02() {
        int opcode = 0x02;
        setRegisters(TEST_BYTE, TEST_WORD, 0, 0, 0, 0, false, false, false, false);

        cpu.processOpcode(opcode);

        verify(mem).writeByte(TEST_BYTE, TEST_WORD);
        verifyNoMoreInteractions(mem);
        assertRegisterValues(TEST_BYTE, TEST_WORD, 0, 0, 0, 0, false, false, false, false);
    }

    // INC BC
    public void testOpcode03() {
        int opcode = 0x03;
        setRegisters(0, TEST_WORD, 0, 0, 0, 0, false, false, false, false);

        cpu.processOpcode(opcode);

        assertRegisterValues(0, (TEST_WORD + 1), 0, 0, 0, 0, false, false, false, false);
        verifyZeroInteractions(mem);
    }

    // INC B
    public void testOpcode04() {
        int opcode = 0x04;
        int in, expected;

        // set zero, clear sub, set half-carry, leave carry
        in = buildWord(0xFF, TEST_BYTE);
        expected = TEST_BYTE;

        setRegisters(0, in, 0, 0, 0, 0, false, true, false, true);
        cpu.processOpcode(opcode);
        assertRegisterValues(0, expected, 0, 0, 0, 0, true, false, true, true);
        verifyZeroInteractions(mem);

        // clear zero, clear sub, clear half-carry, leave carry
        in = buildWord(TEST_BYTE, TEST_BYTE);
        expected = buildWord(TEST_BYTE + 1, TEST_BYTE);

        setRegisters(0, in, 0, 0, 0, 0, true, true, true, true);
        cpu.processOpcode(opcode);
        assertRegisterValues(0, expected, 0, 0, 0, 0, false, false, false, true);
        verifyZeroInteractions(mem);
    }

    // DEC B
    public void testOpcode05() {
        int opcode = 0x05;
        int in, expected;

        // set zero, set sub, clear half-carry, leave carry
        in = buildWord(0x01, TEST_BYTE);
        expected = TEST_BYTE;

        setRegisters(0, in, 0, 0, 0, 0, false, false, true, true);
        cpu.processOpcode(opcode);
        assertRegisterValues(0, expected, 0, 0, 0, 0, true, true, false, true);
        verifyZeroInteractions(mem);

        // clear zero, set sub, set half-carry, leave carry
        in = buildWord(0, TEST_BYTE);
        expected = buildWord(0xFF, TEST_BYTE);

        setRegisters(0, in, 0, 0, 0, 0, true, false, false, true);
        cpu.processOpcode(opcode);
        assertRegisterValues(0, expected, 0, 0, 0, 0, false, true, true, true);
        verifyZeroInteractions(mem);
    }

    // LD B,d8
    public void testOpcode06() {
        int opcode = 0x06;
        zeroRegisters();
        when(mem.readByte(anyInt())).thenReturn(TEST_BYTE);

        cpu.processOpcode(opcode);

        verify(mem).readByte(anyInt());
        verifyNoMoreInteractions(mem);
        assertRegisterValues(0, buildWord(TEST_BYTE, 0), 0, 0, 0, 1, false, false, false, false);
    }

    // RLCA
    public void testOpcode07() {
        int opcode = 0x07;

        setRegisters(TEST_BYTE, 0, 0, 0, 0, 0, true, true, true, false);
        cpu.processOpcode(opcode);
        assertRegisterValues(0x33, 0, 0, 0, 0, 0, false, false, false, true);
        verifyZeroInteractions(mem);

        setRegisters(0x66, 0, 0, 0, 0, 0, true, true, true, true);
        cpu.processOpcode(opcode);
        assertRegisterValues(0xCC, 0, 0, 0, 0, 0, false, false, false, false);
        verifyZeroInteractions(mem);
    }
}
