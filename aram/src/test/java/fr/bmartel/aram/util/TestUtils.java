/*
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2017 Bertrand Martel
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fr.bmartel.aram.util;

import apdu4j.HexUtils;
import fr.bmartel.aram.AccessRuleMaster;
import fr.bmartel.aram.JavaCardTest;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Test utility functions.
 *
 * @author Bertrand Martel
 */
public class TestUtils {

    public static byte[] buildApdu(byte[] command, byte[] data) {
        byte[] apdu = new byte[command.length + data.length + 1];
        System.arraycopy(command, 0, apdu, 0, command.length);
        apdu[command.length] = (byte) data.length;
        System.arraycopy(data, 0, apdu, command.length + 1, data.length);
        return apdu;
    }

    public static void logData(byte[] data) {
        System.out.println(Arrays.toString(data));
    }

    public static int getInt(byte[] data) {
        return (((data[0] & 0xff) << 8) | (data[1] & 0xff));
    }

    public static byte[] getByte(int data) {
        byte[] out = ByteBuffer.allocate(4).putInt(data).array();
        return new byte[]{out[2], out[3]};
    }

    /**
     * Add n x 0x00 offset to a byte array (at the beginning)
     *
     * @param offset number of byte to set to 0x00 before the data
     * @param data   the data
     * @return byte array with offset before data
     */
    public static byte[] addOffset(short offset, byte[] data) {
        byte[] resp = new byte[data.length + offset];
        for (int i = 0; i < offset; i++) {
            resp[i] = 0x00;
        }
        System.arraycopy(resp, offset, data, 0, data.length);
        return resp;
    }

    public static byte[] concatByteArray(byte[]... data) {
        int length = 0;
        for (byte[] item : data) {
            length += item.length;
        }
        byte[] resp = new byte[length];
        int offset = 0;
        for (byte[] item : data) {
            System.arraycopy(item, 0, resp, offset, item.length);
            offset += item.length;
        }
        return resp;
    }

    /**
     * Get class field by reflection.
     *
     * @param object class
     * @param name   field name
     * @return field
     */
    public static Field getField(Class object, String name) {
        for (Field f : object.getDeclaredFields()) {
            f.setAccessible(true);
            if (f != null && f.getName().equals(name)) {
                return f;
            }
        }
        return null;
    }

    /**
     * Send command without expectedResponse.
     *
     * @param card
     * @param cmd
     * @param data
     * @param expectedSw
     * @return
     * @throws CardException
     */
    public static ResponseAPDU sendCmdBatch(JavaCardTest card, byte[] cmd, byte[] data, int expectedSw) throws CardException {
        return sendCmdBatch(card, cmd, data, expectedSw, null);
    }

    /**
     * Send command.
     *
     * @param card
     * @param cmd
     * @param data
     * @param expectedSw
     * @param expectedResponse
     * @return
     * @throws CardException
     */
    public static ResponseAPDU sendCmdBatch(JavaCardTest card, byte[] cmd, byte[] data, int expectedSw, byte[] expectedResponse) throws CardException {
        CommandAPDU commandAPDU = new CommandAPDU(TestUtils.buildApdu(cmd, data));
        System.out.println("> " + HexUtils.bin2hex(commandAPDU.getBytes()));
        ResponseAPDU response = card.transmitCommand(commandAPDU);
        System.out.println("< " + HexUtils.bin2hex(response.getBytes()));
        assertEquals(expectedSw, response.getSW());
        if (expectedResponse != null) {
            assertArrayEquals(expectedResponse, response.getData());
        }
        return response;
    }

    /**
     * return the chunk of data required (with chunk size of AccessRuleMaster.APDU_CHUNK max) from the input index.
     * This is used to check the data expected by the next method for the "index + 1" call.
     *
     * @param index
     * @param resp
     * @return
     */
    public static byte[] checkList(int index, byte[] resp) {
        int length = resp.length;

        int offset = 0;
        if (length < 0xFFFF) {
            offset = 5;
        }

        byte[] expected = new byte[AccessRuleMaster.APDU_CHUNK - offset];

        if (resp.length > AccessRuleMaster.APDU_CHUNK) {
            System.arraycopy(resp, 0, expected, 0, AccessRuleMaster.APDU_CHUNK - offset);
        } else {
            expected = resp;
        }

        if (index == 0) {
            if (length < 0x80) {
                return TestUtils.concatByteArray(new byte[]{
                        (byte) 0xFF, (byte) 0x40, (byte) length
                }, expected);
            } else if (length < 0xFF) {
                return TestUtils.concatByteArray(new byte[]{
                        (byte) 0xFF, (byte) 0x40, (byte) 0x81, (byte) length
                }, expected);
            } else if (length < 0xFFFF) {
                return TestUtils.concatByteArray(new byte[]{
                        (byte) 0xFF, (byte) 0x40, (byte) 0x82, (byte) ((length >> 8) & 0xFF), (byte) (length & 0xFF)
                }, expected);
            }
        } else if (length < 0xFFFF) {
            if (index != (length / AccessRuleMaster.APDU_CHUNK)) {
                expected = new byte[AccessRuleMaster.APDU_CHUNK];
                System.arraycopy(resp, (index * AccessRuleMaster.APDU_CHUNK) - 5, expected, 0, expected.length);
            } else {
                expected = new byte[length % AccessRuleMaster.APDU_CHUNK + 5];
                System.arraycopy(resp, (index * AccessRuleMaster.APDU_CHUNK) - 5, expected, 0, expected.length);
            }
            return expected;
        }
        return null;
    }
}
