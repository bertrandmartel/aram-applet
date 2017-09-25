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
package fr.bmartel.aram;

import fr.bmartel.aram.util.TestUtils;
import org.junit.Before;
import org.junit.Test;

import javax.smartcardio.CardException;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Test AramUtils functions (core functions used to generate ber TLV data object and streaming data through for next data cmd).
 *
 * @author Bertrand Martel
 */
public class AramUtilsTest {

    private RuleEntry entry;

    private final static byte[] VALID_REF_AR_DO = TestUtils.concatByteArray(new byte[]{(byte) 0xE2,
            (byte) (AramConstTest.VALID_REF_DO.length + AramConstTest.VALID_AR_DO.length)}, AramConstTest.VALID_REF_DO, AramConstTest.VALID_AR_DO);

    private final static short DEFAULT_DATA_OFFSET = 0;
    private final static short DEFAULT_DATA_OFFSET_MAX = AccessRuleMaster.APDU_CHUNK;
    private final static short DEFAULT_DATA_OFFSET2 = AccessRuleMaster.APDU_CHUNK;
    private final static short DEFAULT_DATA_OFFSET_MAX2 = AccessRuleMaster.APDU_CHUNK * 2;

    enum ObjectType {
        HASH, AID, AR_DO, REF_DO, REF_AR_DO
    }

    @Before
    public void initEntry() {
        entry = RuleEntry.getInstance();

        entry.setAid(AramConstTest.AID, (short) 0, (byte) AramConstTest.AID.length);
        entry.setHash(AramConstTest.HASH, (short) 0, (byte) AramConstTest.HASH.length);
        entry.setRule(AramConstTest.RULE, (short) 0, (byte) AramConstTest.RULE.length);
    }

    @Test
    public void hashRefDoTest() throws CardException {
        byte[] data = new byte[AramConstTest.VALID_HASH_REF_DO.length];
        assertEquals(AramConstTest.VALID_HASH_REF_DO.length, AramUtils.buildHashRefDo(DEFAULT_DATA_OFFSET, DEFAULT_DATA_OFFSET_MAX, data, (short) 0, entry));
        assertArrayEquals(AramConstTest.VALID_HASH_REF_DO, data);
    }

    private void overflowTest(short offset, ObjectType type) {
        byte[] data = new byte[AccessRuleMaster.APDU_CHUNK];
        byte[] expected = new byte[AccessRuleMaster.APDU_CHUNK];

        switch (type) {
            case HASH:
                assertEquals(AramConstTest.VALID_HASH_REF_DO.length, AramUtils.buildHashRefDo(DEFAULT_DATA_OFFSET, DEFAULT_DATA_OFFSET_MAX, data, offset, entry));
                System.arraycopy(AramConstTest.VALID_HASH_REF_DO, 0, expected, offset, AccessRuleMaster.APDU_CHUNK - offset);
                break;
            case AID:
                assertEquals(AramConstTest.VALID_AID_REF_DO.length, AramUtils.buildAidRefDo(DEFAULT_DATA_OFFSET, DEFAULT_DATA_OFFSET_MAX, data, offset, entry));
                System.arraycopy(AramConstTest.VALID_AID_REF_DO, 0, expected, offset, AccessRuleMaster.APDU_CHUNK - offset);
                break;
            case AR_DO:
                assertEquals(AramConstTest.VALID_AR_DO.length, AramUtils.buildArDo(DEFAULT_DATA_OFFSET, DEFAULT_DATA_OFFSET_MAX, data, offset, entry));
                System.arraycopy(AramConstTest.VALID_AR_DO, 0, expected, offset, AccessRuleMaster.APDU_CHUNK - offset);
                break;
            case REF_DO:
                assertEquals(AramConstTest.VALID_REF_DO.length, AramUtils.buildRefDo(DEFAULT_DATA_OFFSET, DEFAULT_DATA_OFFSET_MAX, data, offset, entry));
                System.arraycopy(AramConstTest.VALID_REF_DO, 0, expected, offset, AccessRuleMaster.APDU_CHUNK - offset);
                break;
            case REF_AR_DO:
                assertEquals(VALID_REF_AR_DO.length, AramUtils.buildRefArDo(DEFAULT_DATA_OFFSET, DEFAULT_DATA_OFFSET_MAX, data, offset, entry));
                System.arraycopy(VALID_REF_AR_DO, 0, expected, offset, AccessRuleMaster.APDU_CHUNK - offset);
                break;
        }
        assertArrayEquals(expected, data);

        switch (type) {
            case HASH:
                assertEquals(AramConstTest.VALID_HASH_REF_DO.length, AramUtils.buildHashRefDo(DEFAULT_DATA_OFFSET2, DEFAULT_DATA_OFFSET_MAX2, data, offset, entry));
                assertArrayEquals(Arrays.copyOfRange(AramConstTest.VALID_HASH_REF_DO,
                        AccessRuleMaster.APDU_CHUNK - offset,
                        AramConstTest.VALID_HASH_REF_DO.length),
                        Arrays.copyOfRange(data, 0, AramConstTest.VALID_HASH_REF_DO.length - (AccessRuleMaster.APDU_CHUNK - offset)));
                break;
            case AID:
                assertEquals(AramConstTest.VALID_AID_REF_DO.length, AramUtils.buildAidRefDo(DEFAULT_DATA_OFFSET2, DEFAULT_DATA_OFFSET_MAX2, data, offset, entry));
                assertArrayEquals(Arrays.copyOfRange(AramConstTest.VALID_AID_REF_DO,
                        AccessRuleMaster.APDU_CHUNK - offset,
                        AramConstTest.VALID_AID_REF_DO.length),
                        Arrays.copyOfRange(data, 0, AramConstTest.VALID_AID_REF_DO.length - (AccessRuleMaster.APDU_CHUNK - offset)));
                break;
            case AR_DO:
                assertEquals(AramConstTest.VALID_AR_DO.length, AramUtils.buildArDo(DEFAULT_DATA_OFFSET2, DEFAULT_DATA_OFFSET_MAX2, data, offset, entry));
                assertArrayEquals(Arrays.copyOfRange(AramConstTest.VALID_AR_DO,
                        AccessRuleMaster.APDU_CHUNK - offset,
                        AramConstTest.VALID_AR_DO.length),
                        Arrays.copyOfRange(data, 0, AramConstTest.VALID_AR_DO.length - (AccessRuleMaster.APDU_CHUNK - offset)));
                break;
            case REF_DO:
                assertEquals(AramConstTest.VALID_REF_DO.length, AramUtils.buildRefDo(DEFAULT_DATA_OFFSET2, DEFAULT_DATA_OFFSET_MAX2, data, offset, entry));
                assertArrayEquals(Arrays.copyOfRange(AramConstTest.VALID_REF_DO,
                        AccessRuleMaster.APDU_CHUNK - offset,
                        AramConstTest.VALID_REF_DO.length),
                        Arrays.copyOfRange(data, 0, AramConstTest.VALID_REF_DO.length - (AccessRuleMaster.APDU_CHUNK - offset)));
                break;
            case REF_AR_DO:
                assertEquals(VALID_REF_AR_DO.length, AramUtils.buildRefArDo(DEFAULT_DATA_OFFSET2, DEFAULT_DATA_OFFSET_MAX2, data, offset, entry));
                assertArrayEquals(Arrays.copyOfRange(VALID_REF_AR_DO,
                        AccessRuleMaster.APDU_CHUNK - offset,
                        VALID_REF_AR_DO.length),
                        Arrays.copyOfRange(data, 0, VALID_REF_AR_DO.length - (AccessRuleMaster.APDU_CHUNK - offset)));
                break;
        }
    }

    @Test
    public void hashRefDoOverflow1Test() throws CardException {
        overflowTest(AccessRuleMaster.APDU_CHUNK, ObjectType.HASH);
    }

    @Test
    public void hashRefDoOverflow2Test() throws CardException {
        overflowTest((short) (AccessRuleMaster.APDU_CHUNK - 1), ObjectType.HASH);
    }

    @Test
    public void hashRefDoOverflow3Test() throws CardException {
        overflowTest((short) (AccessRuleMaster.APDU_CHUNK - 10), ObjectType.HASH);
    }

    @Test
    public void aidRefDoTest() throws CardException {
        byte[] data = new byte[AramConstTest.VALID_AID_REF_DO.length];
        assertEquals(AramConstTest.VALID_AID_REF_DO.length, AramUtils.buildAidRefDo(DEFAULT_DATA_OFFSET, DEFAULT_DATA_OFFSET_MAX, data, (short) 0, entry));
        assertArrayEquals(AramConstTest.VALID_AID_REF_DO, data);
    }

    @Test
    public void aidRefDoOverflow1Test() throws CardException {
        overflowTest(AccessRuleMaster.APDU_CHUNK, ObjectType.AID);
    }

    @Test
    public void aidRefDoOverflow2Test() throws CardException {
        overflowTest((short) (AccessRuleMaster.APDU_CHUNK - 1), ObjectType.AID);
    }

    @Test
    public void aidRefDoOverflow3Test() throws CardException {
        overflowTest((short) (AccessRuleMaster.APDU_CHUNK - 10), ObjectType.AID);
    }

    @Test
    public void arDoTest() throws CardException {
        byte[] data = new byte[AramConstTest.VALID_AR_DO.length];
        assertEquals(AramConstTest.VALID_AR_DO.length, AramUtils.buildArDo(DEFAULT_DATA_OFFSET, DEFAULT_DATA_OFFSET_MAX, data, (short) 0, entry));
        assertArrayEquals(AramConstTest.VALID_AR_DO, data);
    }

    @Test
    public void arDoOverflowTest1() throws CardException {
        overflowTest(AccessRuleMaster.APDU_CHUNK, ObjectType.AR_DO);
    }

    @Test
    public void arDoOverflowTest2() throws CardException {
        overflowTest((short) (AccessRuleMaster.APDU_CHUNK - 1), ObjectType.AR_DO);
    }

    @Test
    public void arDoOverflowTest3() throws CardException {
        overflowTest((short) (AccessRuleMaster.APDU_CHUNK - 10), ObjectType.AR_DO);
    }

    @Test
    public void refDoTest() throws CardException {
        byte[] data = new byte[AramConstTest.VALID_REF_DO.length];
        assertEquals(AramConstTest.VALID_REF_DO.length, AramUtils.buildRefDo(DEFAULT_DATA_OFFSET, DEFAULT_DATA_OFFSET_MAX, data, (short) 0, entry));
        assertArrayEquals(AramConstTest.VALID_REF_DO, data);
    }

    @Test
    public void refDoOverflow1Test() throws CardException {
        overflowTest(AccessRuleMaster.APDU_CHUNK, ObjectType.REF_DO);
    }

    @Test
    public void refDoOverflow2Test() throws CardException {
        overflowTest((short) (AccessRuleMaster.APDU_CHUNK - 1), ObjectType.REF_DO);
    }

    @Test
    public void refDoOverflow3Test() throws CardException {
        overflowTest((short) (AccessRuleMaster.APDU_CHUNK - 10), ObjectType.REF_DO);
    }

    @Test
    public void refArDoTest() throws CardException {
        byte[] data = new byte[VALID_REF_AR_DO.length];
        assertEquals(VALID_REF_AR_DO.length, AramUtils.buildRefArDo(DEFAULT_DATA_OFFSET, DEFAULT_DATA_OFFSET_MAX, data, (short) 0, entry));
        assertArrayEquals(VALID_REF_AR_DO, data);
    }

    @Test
    public void refArDoOverflow1Test() throws CardException {
        overflowTest(AccessRuleMaster.APDU_CHUNK, ObjectType.REF_AR_DO);
    }

    @Test
    public void refArDoOverflow2Test() throws CardException {
        overflowTest((short) (AccessRuleMaster.APDU_CHUNK - 1), ObjectType.REF_AR_DO);
    }

    @Test
    public void refArDoOverflow3Test() throws CardException {
        overflowTest((short) (AccessRuleMaster.APDU_CHUNK - 10), ObjectType.REF_AR_DO);
    }
}
