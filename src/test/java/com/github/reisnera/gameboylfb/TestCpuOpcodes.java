/* This file is part of the GameBoyLFB project.
   GameBoyLFB - A Java Game Boy emulator.
   Copyright (C) 2015 Alex Reisner <thearcher at gmail dot com>

   This project is licensed under the GNU GPL v2 license and comes with
   absolutely no warranty of any kind. The full license can be found at:
   http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt */

package com.github.reisnera.gameboylfb;

import org.testng.annotations.*;
//import static org.testng.Assert.*;
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

    // INC B
    @Test
    public void testOpcode04() {
        // TODO: this test needs to test flags!
        // prepare
        cpu.reg.setB(TEST_BYTE);
        reset(cpu.reg);
        // test
        cpu.processOpcode(0x04);
        // verify
        verify(cpu.reg, atLeastOnce()).getB();
        verify(cpu.reg).setB(TEST_BYTE + 1);
        verify(cpu.reg).clearFlagN();
        verifyNoMoreInteractions(cpu.reg);
        verifyZeroInteractions(mem);
    }

    // DEC B
    @Test
    public void testOpcode05() {
        // TODO: this test needs to test flags!
        // prepare
        cpu.reg.setB(TEST_BYTE);
        reset(cpu.reg);
        // test
        cpu.processOpcode(0x05);
        // verify
        verify(cpu.reg, atLeastOnce()).getB();
        verify(cpu.reg).setB(TEST_BYTE - 1);
        verify(cpu.reg).setFlagN();
        verifyNoMoreInteractions(cpu.reg);
        verifyZeroInteractions(mem);
    }
}
