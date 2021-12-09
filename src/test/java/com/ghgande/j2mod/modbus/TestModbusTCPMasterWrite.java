/*
 * Copyright 2002-2016 jamod & j2mod development teams
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ghgande.j2mod.modbus;

import com.ghgande.j2mod.modbus.procimg.DigitalOut;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleInputRegister;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.ghgande.j2mod.modbus.util.BitVector;
import com.ghgande.j2mod.modbus.utils.AbstractTestModbusTCPMaster;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This class tests the TCP master write features of the library
 */
@SuppressWarnings("ConstantConditions")
public class TestModbusTCPMasterWrite extends AbstractTestModbusTCPMaster {

    @Test
    public void testWriteCoils() {
        try {
            boolean before = master.readCoils(UNIT_ID, 1, 1).getBit(0);
            master.writeCoil(UNIT_ID, 1, !before);
            assertTrue("Incorrect status for coil 1", !before);
            master.writeCoil(UNIT_ID, 1, before);
        }
        catch (Exception e) {
            fail(String.format("Cannot write to coil 1 - %s", e.getMessage()));
        }
    }
    
    @Test
    public void testWriteMultipleCoils() {
    	try {
    		
    		byte[] data = {0b00000000, (byte) 0b11111111, 0b01010101};
    		BitVector bv = BitVector.createBitVector(data);
    		master.writeMultipleCoils(UNIT_ID, 0, bv);
    		
    		DigitalOut[] slaveCoils = slave.getProcessImage(UNIT_ID).getDigitalOutRange(0, 24);
    		
    		assertEquals("Bit 0 of byte 0 is wrong", false, slaveCoils[0].isSet());
    		assertEquals("Bit 1 of byte 0 is wrong", false, slaveCoils[1].isSet());
    		assertEquals("Bit 2 of byte 0 is wrong", false, slaveCoils[2].isSet());
    		assertEquals("Bit 3 of byte 0 is wrong", false, slaveCoils[3].isSet());
    		assertEquals("Bit 4 of byte 0 is wrong", false, slaveCoils[4].isSet());
    		assertEquals("Bit 5 of byte 0 is wrong", false, slaveCoils[5].isSet());
    		assertEquals("Bit 6 of byte 0 is wrong", false, slaveCoils[6].isSet());
    		assertEquals("Bit 7 of byte 0 is wrong", false, slaveCoils[7].isSet());

    		assertEquals("Bit 0 of byte 1 is wrong", true, slaveCoils[8].isSet());
    		assertEquals("Bit 1 of byte 1 is wrong", true, slaveCoils[9].isSet());
    		assertEquals("Bit 2 of byte 1 is wrong", true, slaveCoils[10].isSet());
    		assertEquals("Bit 3 of byte 1 is wrong", true, slaveCoils[11].isSet());
    		assertEquals("Bit 4 of byte 1 is wrong", true, slaveCoils[12].isSet());
    		assertEquals("Bit 5 of byte 1 is wrong", true, slaveCoils[13].isSet());
    		assertEquals("Bit 6 of byte 1 is wrong", true, slaveCoils[14].isSet());
    		assertEquals("Bit 7 of byte 1 is wrong", true, slaveCoils[15].isSet());

    		assertEquals("Bit 0 of byte 2 is wrong", true, slaveCoils[16].isSet());
    		assertEquals("Bit 1 of byte 2 is wrong", false, slaveCoils[17].isSet());
    		assertEquals("Bit 2 of byte 2 is wrong", true, slaveCoils[18].isSet());
    		assertEquals("Bit 3 of byte 2 is wrong", false, slaveCoils[19].isSet());
    		assertEquals("Bit 4 of byte 2 is wrong", true, slaveCoils[20].isSet());
    		assertEquals("Bit 5 of byte 2 is wrong", false, slaveCoils[21].isSet());
    		assertEquals("Bit 6 of byte 2 is wrong", true, slaveCoils[22].isSet());
    		assertEquals("Bit 7 of byte 2 is wrong", false, slaveCoils[23].isSet());
    	}
    	catch(Exception e) {    		
    		fail(String.format("Cannot write to coil 1 - %s", e.getMessage()));
    	}
    }

    @Test
    public void testWriteHoldingRegisters() {
        try {
            int before = master.readInputRegisters(UNIT_ID, 1, 1)[0].getValue();
            int newValue = 9999;

            assertEquals("Incorrect status after write new value for register 1", newValue,
                    master.writeSingleRegister(UNIT_ID, 1, new SimpleInputRegister(newValue)));
            assertEquals("Incorrect status after read new value for register 1", newValue,
                    master.readInputRegisters(UNIT_ID, 1, 1)[0].getValue());
            assertEquals("Incorrect status after write previous value for register 1", before,
                    master.writeSingleRegister(UNIT_ID, 1, new SimpleInputRegister(before)));
        }
        catch (Exception e) {
            fail(String.format("Cannot write to register 1 - %s", e.getMessage()));
        }
    }

    @Test
    public void testWriteMultipleRegisters() {
        try {
            int registerCount = 3;
            Register[] beforeRegisters = master.readMultipleRegisters(UNIT_ID, 1, registerCount);

            Register[] writeRegisters = new Register[registerCount];
            for (int i = 0; i < registerCount; ++i) {
                writeRegisters[i] = new SimpleRegister(Double.valueOf(Math.random()).intValue());
            }

            assertEquals("Incorrect status for register",
                    registerCount, master.writeMultipleRegisters(UNIT_ID, 1, writeRegisters));

            Register[] afterRegisters = master.readMultipleRegisters(UNIT_ID, 1, registerCount);

            for (int i = 0; i < registerCount; ++i) {
                assertEquals("Incorrect status for register",
                        writeRegisters[i].getValue(), afterRegisters[i].getValue());
            }

            master.writeMultipleRegisters(UNIT_ID, 1, beforeRegisters);
        }
        catch (Exception e) {
            fail(String.format("Cannot write to registers - %s", e.getMessage()));
        }
    }

    @Test
    public void testMaskWriteRegister() {
        try {
            int before = master.readMultipleRegisters(UNIT_ID, 1, 1)[0].getValue();
            int andMask = 0xABCD;
            int orMask = 0xBCDA;
            int newValue = (before & andMask) | (orMask & ~andMask);

            assertTrue("Incorrect mask write status for register 1", master.maskWriteRegister(UNIT_ID, 1, andMask, orMask));
            assertEquals("Incorrect status for register 1", newValue, master.readMultipleRegisters(UNIT_ID, 1, 1)[0].getValue());
            master.writeSingleRegister(UNIT_ID, 1, new SimpleInputRegister(before));
        }
        catch (Exception e) {
            fail(String.format("Cannot mask write to register 1 - %s", e.getMessage()));
        }
    }
}
