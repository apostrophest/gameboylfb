/* This file is part of the GameBoyLFB project.
   GameBoyLFB - A Java Game Boy emulator.
   Copyright (C) 2015 Stephen Thompson <stephen at chomadoma dot net>

   This project is licensed under the GNU GPL v2 license and comes with
   absolutely no warranty of any kind. The full license can be found at:
   http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt */

package com.github.reisnera.gameboylfb;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.function.IntConsumer;
import java.util.function.Supplier;

@Test
public class TestCpuRegisters {

    private GameBoyCpu.CpuRegisters reg;

    @BeforeMethod
    public void beforeMethod() {
        reg = new GameBoyCpu.CpuRegisters();
    }

    private void verifyRegisterGet(Supplier<Integer> getLow, Supplier<Integer> getHigh, IntConsumer setFull) {
        verifyLowGet(getLow, setFull);
        verifyHighGet(getHigh, setFull);
    }

    private void verifyRegisterSet(Supplier<Integer> getFull, IntConsumer setLow, IntConsumer setHigh) {
        verifyLowSet(getFull, setLow);
        verifyHighSet(getFull, setHigh);
    }

    private void verifyLowSet(Supplier<Integer> getFull, IntConsumer setLow) {
        int testValue;
        int preexistingHigh = getFull.get() >>> 8;

        // High result never changes when low bits change
        // Low result always changes when low bits change
        for (testValue = 0x00; testValue <= 0xFF; testValue++) {
            setLow.accept(testValue);
            Assert.assertEquals(getFull.get() >>> 8, preexistingHigh);
            Assert.assertEquals(getFull.get().intValue(), (preexistingHigh << 8) + testValue);
        }
    }

    private void verifyHighSet(Supplier<Integer> getFull, IntConsumer setHigh) {
        int testValue;
        int preexistingLow = getFull.get() & 0xFF;

        // Low result never changes when high bits change
        // High result always changes when high bits change
        for (testValue = 0x00; testValue <= 0xFF; testValue++) {
            setHigh.accept(testValue);
            Assert.assertEquals(getFull.get().intValue(), (testValue << 8) + preexistingLow);
            Assert.assertEquals(getFull.get() & 0xFF, preexistingLow);
        }
    }

    private void verifyLowGet(Supplier<Integer> getLow, IntConsumer setFull) {
        int testValue;

        // Low result never changes when high bits change
        for (testValue = 0x00AB; testValue <= 0xFFAB; testValue += 0x0100) {
            setFull.accept(testValue);
            Assert.assertEquals(getLow.get().intValue(), 0xAB);
        }

        // Low result always changes when low bits change
        for (testValue = 0xFF00; testValue <= 0xFFFF; testValue++) {
            setFull.accept(testValue);
            Assert.assertEquals(getLow.get().intValue(), testValue & 0xFF);
        }
    }

    private void verifyHighGet(Supplier<Integer> getHigh, IntConsumer setFull) {
        int testValue;

        // High result never changes when low bits change
        for (testValue = 0x0000; testValue <= 0x00FF; testValue++) {
            setFull.accept(testValue);
            Assert.assertEquals(getHigh.get().intValue(), 0x00);
        }

        // High result always changes when high bits change
        for (testValue = 0x00AB; testValue <= 0xFFAB; testValue += 0x0100) {
            setFull.accept(testValue);
            Assert.assertEquals(getHigh.get().intValue(), testValue >>> 8);
        }
    }

    /**
     * Verifies the full 16-bit get and set range.
     * @param getFull 16-bit getter
     * @param setFull 16-bit setter
     */
    private void verifyFull(Supplier<Integer> getFull, IntConsumer setFull) {
        for (int testValue = 0x0000; testValue <= 0xFFFF; testValue++) {
            setFull.accept(testValue);
            Assert.assertEquals(getFull.get().intValue(), testValue);
        }
    }

    // Ensure that the lower 4 bits are always cleared when AF is set
    public void verifyAFLowBits() {
        for (int testValue = 0x0000; testValue <= 0x000F; testValue++) {
            reg.setAF(testValue);
            Assert.assertEquals(reg.getAF(), 0x0000);
        }
    }

    // Ensure correct operation at 16-bit boundaries
    public void verifyAFBoundaries() {
        reg.setAF(0x0010);
        Assert.assertEquals(reg.getAF(), 0x0010);

        reg.setAF(0x0011);
        Assert.assertEquals(reg.getAF(), 0x0010);

        reg.setAF(0xFFFF);
        Assert.assertEquals(reg.getAF(), 0xFFF0);
    }

    public void verifyDE() {
        Supplier<Integer> getLow = () -> reg.getE();
        Supplier<Integer> getHigh = () -> reg.getD();
        Supplier<Integer> getFull = () -> reg.getDE();
        IntConsumer setLow = (x) -> reg.setE(x);
        IntConsumer setHigh = (x) -> reg.setD(x);
        IntConsumer setFull = (x) -> reg.setDE(x);

        verifyRegisterGet(getLow, getHigh, setFull);
        verifyRegisterSet(getFull, setLow, setHigh);
        verifyFull(getFull, setFull);
    }

    public void verifyHL() {
        Supplier<Integer> getLow = () -> reg.getL();
        Supplier<Integer> getHigh = () -> reg.getH();
        Supplier<Integer> getFull = () -> reg.getHL();
        IntConsumer setLow = (x) -> reg.setL(x);
        IntConsumer setHigh = (x) -> reg.setH(x);
        IntConsumer setFull = (x) -> reg.setHL(x);

        verifyRegisterGet(getLow, getHigh, setFull);
        verifyRegisterSet(getFull, setLow, setHigh);
        verifyFull(getFull, setFull);
    }

    public void verifySP() {
        Supplier<Integer> getFull = () -> reg.getSP();
        IntConsumer setFull = (x) -> reg.setSP(x);

        verifyFull(getFull, setFull);
    }

    public void verifyPC() {
        Supplier<Integer> getFull = () -> reg.getPC();
        IntConsumer setFull = (x) -> reg.setPC(x);

        verifyFull(getFull, setFull);
    }

}
