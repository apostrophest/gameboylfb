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

public class TestCpuOpcodes {

    private GameBoyMemory mem;
    private GameBoyCpu cpu;

    private static final int TEST_BYTE = 0x99;
    private static final int TEST_WORD = 0xAA99;

    @BeforeMethod
    public void beforeMethod() {
        mem = mock(GameBoyMemory.class);
        cpu = new GameBoyCpu(mem);
        cpu.reg = spy(cpu.reg);
    }

    // NOP
    @Test
    public void testOpcode00() {
        // test
        cpu.processOpcode(0x00);
        // verify
        verifyZeroInteractions(mem, cpu.reg);
    }

    // LD BC,d16
    @Test
    public void testOpcode01() {
        // prepare
        when(mem.readWord(anyInt())).thenReturn(TEST_WORD);
        // test
        cpu.processOpcode(0x01);
        // verify
        verify(mem).readWord(anyInt());
        verify(cpu.reg).getThenIncPC(2);
        verify(cpu.reg).setBC(TEST_WORD);
        verifyNoMoreInteractions(mem, cpu.reg);
    }

    // LD (BC),A
    @Test
    public void testOpcode02() {
        // prepare
        cpu.reg.setA(TEST_BYTE);
        cpu.reg.setBC(TEST_WORD);
        reset(cpu.reg);
        // test
        cpu.processOpcode(0x02);
        // verify
        verify(cpu.reg).getA();
        verify(cpu.reg).getBC();
        verify(mem).writeByte(TEST_BYTE, TEST_WORD);
        verifyNoMoreInteractions(mem, cpu.reg);
    }

    // INC BC
    @Test
    public void testOpcode03() {
        // prepare
        cpu.reg.setBC(TEST_WORD);
        reset(cpu.reg);
        // test
        cpu.processOpcode(0x03);
        // verify
        verify(cpu.reg).getBC();
        verify(cpu.reg).setBC(TEST_WORD + 1);
        verifyNoMoreInteractions(cpu.reg);
        verifyZeroInteractions(mem);
    }

    // INC B - results in zero and half-carry
    @Test
    public void testOpcode04a() {
        // prepare
        cpu.reg.setB(0xFF);
        reset(cpu.reg);
        // test
        cpu.processOpcode(0x04);
        // verify
        verify(cpu.reg, atLeastOnce()).getB();
        verify(cpu.reg).setB(0xFF + 1);
        verify(cpu.reg).clearFlagN();
        verify(cpu.reg).setFlagZ();
        verify(cpu.reg).setFlagH();
        verifyNoMoreInteractions(cpu.reg);
        verifyZeroInteractions(mem);
    }

    // INC B - results in no flags
    @Test
    public void testOpcode04b() {
        // prepare
        cpu.reg.setB(TEST_BYTE);
        reset(cpu.reg);
        // test
        cpu.processOpcode(0x04);
        // verify
        verify(cpu.reg, atLeastOnce()).getB();
        verify(cpu.reg).setB(TEST_BYTE + 1);
        verify(cpu.reg).clearFlagN();
        verify(cpu.reg).clearFlagZ();
        verify(cpu.reg).clearFlagH();
        verifyNoMoreInteractions(cpu.reg);
        verifyZeroInteractions(mem);
    }

    // DEC B - results in half-carry
    @Test
    public void testOpcode05a() {
        // prepare
        cpu.reg.setB(0);
        reset(cpu.reg);
        // test
        cpu.processOpcode(0x05);
        // verify
        verify(cpu.reg, atLeastOnce()).getB();
        verify(cpu.reg).setB(0 - 1);
        verify(cpu.reg).setFlagN();
        verify(cpu.reg).setFlagH();
        verify(cpu.reg).clearFlagZ();
        verifyNoMoreInteractions(cpu.reg);
        verifyZeroInteractions(mem);
    }

    // DEC B - results in zero
    @Test
    public void testOpcode05b() {
        // prepare
        cpu.reg.setB(1);
        reset(cpu.reg);
        // test
        cpu.processOpcode(0x05);
        // verify
        verify(cpu.reg, atLeastOnce()).getB();
        verify(cpu.reg).setB(0);
        verify(cpu.reg).setFlagN();
        verify(cpu.reg).setFlagZ();
        verify(cpu.reg).clearFlagH();
        verifyNoMoreInteractions(cpu.reg);
        verifyZeroInteractions(mem);
    }

    // LD B,d8
    @Test
    public void testOpcode06() {
        // prepare
        when(mem.readByte(anyInt())).thenReturn(TEST_BYTE);
        // test
        cpu.processOpcode(0x06);
        // verify
        verify(mem).readByte(anyInt());
        verify(cpu.reg).getThenIncPC();
        verify(cpu.reg).setB(TEST_BYTE);
        verifyNoMoreInteractions(mem, cpu.reg);
    }
}
