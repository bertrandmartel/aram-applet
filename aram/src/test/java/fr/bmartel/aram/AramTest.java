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
import javacard.framework.ISO7816;
import org.junit.Before;
import org.junit.Test;
import pro.javacard.gp.GPDataException;
import pro.javacard.gp.SEAccessControl;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Testing ARAM applet.
 *
 * @author Bertrand Martel
 */
public class AramTest extends JavaCardTest {

    private void sendGetAll(byte[] data, int expectedSw, byte[] expectedResponse) throws CardException {
        TestUtils.sendCmdBatch(this, AramConstTest.CMD_GET_ALL, data, expectedSw, expectedResponse);
    }

    private void sendGetNext(byte[] data, int expectedSw, byte[] expectedResponse) throws CardException {
        TestUtils.sendCmdBatch(this, AramConstTest.CMD_GET_NEXT, data, expectedSw, expectedResponse);
    }

    private ResponseAPDU sendGetRefreshTag(byte[] data, int expectedSw) throws CardException {
        return TestUtils.sendCmdBatch(this, AramConstTest.CMD_GET_REFRESH_TAG, data, expectedSw);
    }

    private void deleteAllRules() throws CardException, GPDataException {
        CommandAPDU commandAPDU = new CommandAPDU(TestUtils.buildApdu(AramConstTest.CMD_GET_ALL, new byte[]{}));
        ResponseAPDU response = this.transmitCommand(commandAPDU);

        SEAccessControl.BerTlvData temp = SEAccessControl.AcrListResponse.getAcrListData(null, response.getData());

        while (temp.getCurrentIndex() < temp.getLength()) {
            commandAPDU = new CommandAPDU(TestUtils.buildApdu(AramConstTest.CMD_GET_NEXT, new byte[]{}));
            response = this.transmitCommand(commandAPDU);
            temp = SEAccessControl.AcrListResponse.getAcrListData(temp, response.getData());
        }

        List<SEAccessControl.RefArDo> resp = SEAccessControl.AcrListResponse.fromBytes(temp.getLength(), temp.getData()).acrList;

        for (int i = 0; i < resp.size(); i++) {
            deleteData(resp.get(i).getBytes());
        }
        sendGetAll(new byte[]{}, 0x9000, AramConstTest.GET_DATA_EMPTY_RESPONSE);
    }


    @Before
    public void initTest() throws NoSuchFieldException, IllegalAccessException, CardException, GPDataException {
        TestSuite.setup();
        deleteAllRules();
    }

    @Test
    public void wrongClass() throws CardException {
        TestUtils.sendCmdBatch(this, AramConstTest.CMD_WRONG_CLA, new byte[]{}, ISO7816.SW_CLA_NOT_SUPPORTED, new byte[]{});
    }

    @Test
    public void wrongInstruction() throws CardException {
        TestUtils.sendCmdBatch(this, AramConstTest.CMD_WRONG_INS, new byte[]{}, ISO7816.SW_INS_NOT_SUPPORTED, new byte[]{});
    }

    @Test
    public void wrongP1P2GetData() throws CardException {
        TestUtils.sendCmdBatch(this, AramConstTest.CMD_WRONG_P1P2_GET_DATA, new byte[]{}, ISO7816.SW_INCORRECT_P1P2, new byte[]{});
    }

    @Test
    public void getAllEmptyTest() throws CardException {
        sendGetAll(new byte[]{}, 0x9000, AramConstTest.GET_DATA_EMPTY_RESPONSE);
    }

    @Test
    public void wrongP1P2StoreData() throws CardException {
        TestUtils.sendCmdBatch(this, AramConstTest.CMD_WRONG_P1P2_STORE_DATA, new byte[]{}, ISO7816.SW_INCORRECT_P1P2, new byte[]{});
    }

    @Test
    public void storeDataWrongStore() throws CardException {
        TestUtils.sendCmdBatch(this,
                TestUtils.concatByteArray(AramConstTest.CMD_STORE_HEADER,
                        new byte[]{(byte) AramConstTest.CMD_STORE_DATA_INVALID_CMD_STORE.length},
                        AramConstTest.CMD_STORE_DATA_INVALID_CMD_STORE),
                new byte[]{},
                ISO7816.SW_DATA_INVALID,
                new byte[]{});
    }

    @Test
    public void storeDataWrongRefArDo() throws CardException {
        TestUtils.sendCmdBatch(this,
                TestUtils.concatByteArray(AramConstTest.CMD_STORE_HEADER,
                        new byte[]{(byte) AramConstTest.CMD_STORE_DATA_INVALID_REFARDO.length},
                        AramConstTest.CMD_STORE_DATA_INVALID_REFARDO),
                new byte[]{},
                ISO7816.SW_DATA_INVALID,
                new byte[]{});
    }

    @Test
    public void storeDataWrongRefDo() throws CardException {
        TestUtils.sendCmdBatch(this,
                TestUtils.concatByteArray(AramConstTest.CMD_STORE_HEADER,
                        new byte[]{(byte) AramConstTest.CMD_STORE_DATA_INVALID_REFDO.length},
                        AramConstTest.CMD_STORE_DATA_INVALID_REFDO),
                new byte[]{},
                ISO7816.SW_DATA_INVALID,
                new byte[]{});
    }

    @Test
    public void storeDataWrongAidRefDo() throws CardException {
        TestUtils.sendCmdBatch(this,
                TestUtils.concatByteArray(AramConstTest.CMD_STORE_HEADER,
                        new byte[]{(byte) AramConstTest.CMD_STORE_DATA_INVALID_AIDREFDO.length},
                        AramConstTest.CMD_STORE_DATA_INVALID_AIDREFDO),
                new byte[]{},
                ISO7816.SW_DATA_INVALID,
                new byte[]{});
    }

    @Test
    public void storeDataWrongHashRefDo() throws CardException {
        TestUtils.sendCmdBatch(this,
                TestUtils.concatByteArray(AramConstTest.CMD_STORE_HEADER,
                        new byte[]{(byte) AramConstTest.CMD_STORE_DATA_INVALID_HASHREFDO.length},
                        AramConstTest.CMD_STORE_DATA_INVALID_HASHREFDO),
                new byte[]{},
                ISO7816.SW_DATA_INVALID,
                new byte[]{});
    }

    @Test
    public void storeDataWrongArDo() throws CardException {
        TestUtils.sendCmdBatch(this,
                TestUtils.concatByteArray(AramConstTest.CMD_STORE_HEADER,
                        new byte[]{(byte) AramConstTest.CMD_STORE_DATA_INVALID_ARDO.length},
                        AramConstTest.CMD_STORE_DATA_INVALID_ARDO),
                new byte[]{},
                ISO7816.SW_DATA_INVALID,
                new byte[]{});
    }

    private void storeData(byte[] refArDo) throws CardException {

        byte[] request = TestUtils.concatByteArray(new byte[]{
                //F0 = Command-Store-AR-DO
                (byte) 0xF0, (byte) refArDo.length
        }, refArDo);

        TestUtils.sendCmdBatch(this,
                TestUtils.concatByteArray(AramConstTest.CMD_STORE_HEADER,
                        new byte[]{(byte) request.length},
                        request),
                new byte[]{},
                0x9000,
                new byte[]{});
    }

    private void updateRefreshTag() throws CardException {

        byte[] request = new byte[]{
                //F0 = Command-Store-AR-DO
                (byte) 0xF2, (byte) 0
        };

        TestUtils.sendCmdBatch(this,
                TestUtils.concatByteArray(AramConstTest.CMD_STORE_HEADER,
                        new byte[]{(byte) request.length},
                        request),
                new byte[]{},
                0x9000,
                new byte[]{});
    }

    private ResponseAPDU getSpecific(byte[] refDo, byte[] expected) throws CardException {
        byte[] request = TestUtils.concatByteArray(new byte[]{(byte) refDo.length}, refDo);

        return TestUtils.sendCmdBatch(this,
                TestUtils.concatByteArray(AramConstTest.CMD_GET_SPECIFIC,
                        new byte[]{(byte) request.length},
                        request),
                new byte[]{}, 0x9000, expected);
    }

    private void deleteData(byte[] refArDo) throws CardException {

        byte[] request = TestUtils.concatByteArray(new byte[]{
                //F1 = Command-Delete-AR-DO
                (byte) 0xF1, (byte) refArDo.length
        }, refArDo);

        TestUtils.sendCmdBatch(this,
                TestUtils.concatByteArray(AramConstTest.CMD_STORE_HEADER,
                        new byte[]{(byte) request.length},
                        request),
                new byte[]{},
                0x9000,
                new byte[]{});
    }

    @Test
    public void storeDataValid() throws CardException {
        storeData(AramConstTest.VALID_REF_AR_DO);
        sendGetAll(new byte[]{}, 0x9000, TestUtils.checkList(0, AramConstTest.VALID_REF_AR_DO));
    }

    @Test
    public void storeDataMultiple() throws CardException {
        storeData(AramConstTest.VALID_REF_AR_DO);
        storeData(AramConstTest.VALID_REF_AR_DO1);
        sendGetAll(new byte[]{}, 0x9000, TestUtils.checkList(0, TestUtils.concatByteArray(AramConstTest.VALID_REF_AR_DO1, AramConstTest.VALID_REF_AR_DO)));
    }

    @Test
    public void storeDataMultipleNextLength2() throws CardException {
        storeData(AramConstTest.VALID_REF_AR_DO);
        storeData(AramConstTest.VALID_REF_AR_DO1);
        storeData(AramConstTest.VALID_REF_AR_DO2);
        storeData(AramConstTest.VALID_REF_AR_DO3);
        storeData(AramConstTest.VALID_REF_AR_DO4);
        sendGetAll(new byte[]{}, 0x9000, TestUtils.checkList(0, TestUtils.concatByteArray(AramConstTest.VALID_REF_AR_DO4, AramConstTest.VALID_REF_AR_DO3, AramConstTest.VALID_REF_AR_DO2, AramConstTest.VALID_REF_AR_DO1, AramConstTest.VALID_REF_AR_DO)));
    }

    @Test
    public void storeDataMultipleNextLength3() throws CardException {
        storeData(AramConstTest.VALID_REF_AR_DO);
        storeData(AramConstTest.VALID_REF_AR_DO1);
        storeData(AramConstTest.VALID_REF_AR_DO2);
        storeData(AramConstTest.VALID_REF_AR_DO3);
        storeData(AramConstTest.VALID_REF_AR_DO4);
        storeData(AramConstTest.VALID_REF_AR_DO5);
        sendGetAll(new byte[]{}, 0x9000, TestUtils.checkList(0, TestUtils.concatByteArray(AramConstTest.VALID_REF_AR_DO5, AramConstTest.VALID_REF_AR_DO4, AramConstTest.VALID_REF_AR_DO3, AramConstTest.VALID_REF_AR_DO2, AramConstTest.VALID_REF_AR_DO1, AramConstTest.VALID_REF_AR_DO)));
    }

    @Test
    public void storeDataMultipleNextLength1next() throws CardException {
        storeData(AramConstTest.VALID_REF_AR_DO);
        storeData(AramConstTest.VALID_REF_AR_DO1);
        storeData(AramConstTest.VALID_REF_AR_DO2);
        storeData(AramConstTest.VALID_REF_AR_DO3);
        storeData(AramConstTest.VALID_REF_AR_DO4);
        storeData(AramConstTest.VALID_REF_AR_DO5);
        storeData(AramConstTest.VALID_REF_AR_DO6);
        storeData(AramConstTest.VALID_REF_AR_DO7);

        byte[] expectedData = TestUtils.concatByteArray(AramConstTest.VALID_REF_AR_DO7, AramConstTest.VALID_REF_AR_DO6, AramConstTest.VALID_REF_AR_DO5, AramConstTest.VALID_REF_AR_DO4, AramConstTest.VALID_REF_AR_DO3, AramConstTest.VALID_REF_AR_DO2, AramConstTest.VALID_REF_AR_DO1, AramConstTest.VALID_REF_AR_DO);

        sendGetAll(new byte[]{}, 0x9000, TestUtils.checkList(0, expectedData));
        sendGetNext(new byte[]{}, 0x9000, TestUtils.checkList(1, expectedData));
        sendGetNext(new byte[]{}, 0x6A88, new byte[]{});
    }

    @Test
    public void storeDataMultipleNextLength2next() throws CardException {
        storeData(AramConstTest.VALID_REF_AR_DO);
        storeData(AramConstTest.VALID_REF_AR_DO1);
        storeData(AramConstTest.VALID_REF_AR_DO2);
        storeData(AramConstTest.VALID_REF_AR_DO3);
        storeData(AramConstTest.VALID_REF_AR_DO4);
        storeData(AramConstTest.VALID_REF_AR_DO5);
        storeData(AramConstTest.VALID_REF_AR_DO6);
        storeData(AramConstTest.VALID_REF_AR_DO7);
        storeData(AramConstTest.VALID_REF_AR_DO8);
        storeData(AramConstTest.VALID_REF_AR_DO9);
        storeData(AramConstTest.VALID_REF_AR_DO10);

        byte[] expectedData = TestUtils.concatByteArray(AramConstTest.VALID_REF_AR_DO10, AramConstTest.VALID_REF_AR_DO9, AramConstTest.VALID_REF_AR_DO8, AramConstTest.VALID_REF_AR_DO7, AramConstTest.VALID_REF_AR_DO6, AramConstTest.VALID_REF_AR_DO5, AramConstTest.VALID_REF_AR_DO4, AramConstTest.VALID_REF_AR_DO3, AramConstTest.VALID_REF_AR_DO2, AramConstTest.VALID_REF_AR_DO1, AramConstTest.VALID_REF_AR_DO);

        sendGetAll(new byte[]{}, 0x9000, TestUtils.checkList(0, expectedData));
        sendGetNext(new byte[]{}, 0x9000, TestUtils.checkList(1, expectedData));
        sendGetNext(new byte[]{}, 0x9000, TestUtils.checkList(2, expectedData));
        sendGetNext(new byte[]{}, 0x6A88, new byte[]{});
    }

    @Test
    public void nextDataNotFound() throws CardException {
        sendGetNext(new byte[]{}, 0x6A88, new byte[]{});
    }

    @Test
    public void nextDataSingleNextLengthRequest() throws CardException, IllegalAccessException, NoSuchFieldException, GPDataException {
        storeData(AramConstTest.VALID_REF_AR_DO);
        storeData(AramConstTest.VALID_REF_AR_DO1);
        storeData(AramConstTest.VALID_REF_AR_DO2);
        storeData(AramConstTest.VALID_REF_AR_DO3);
        storeData(AramConstTest.VALID_REF_AR_DO4);
        storeData(AramConstTest.VALID_REF_AR_DO5);

        byte[] expectedData = TestUtils.concatByteArray(AramConstTest.VALID_REF_AR_DO5, AramConstTest.VALID_REF_AR_DO4, AramConstTest.VALID_REF_AR_DO3, AramConstTest.VALID_REF_AR_DO2, AramConstTest.VALID_REF_AR_DO1, AramConstTest.VALID_REF_AR_DO);

        sendGetAll(new byte[]{}, 0x9000, TestUtils.checkList(0, expectedData));
        sendGetNext(new byte[]{}, 0x9000, TestUtils.checkList(1, expectedData));
        sendGetNext(new byte[]{}, 0x6A88, new byte[]{});
    }

    @Test
    public void deleteByAid() throws CardException {
        storeData(AramConstTest.VALID_REF_AR_DO);
        sendGetAll(new byte[]{}, 0x9000, TestUtils.checkList(0, AramConstTest.VALID_REF_AR_DO));
        deleteData(AramConstTest.AID_REF_DO);
        sendGetAll(new byte[]{}, 0x9000, AramConstTest.GET_DATA_EMPTY_RESPONSE);
    }

    @Test
    public void deleteMultipleSameAid() throws CardException {
        storeData(AramConstTest.VALID_REF_AR_DO);
        storeData(AramConstTest.VALID_REF_AR_DO_CLONE);
        sendGetAll(new byte[]{}, 0x9000, TestUtils.checkList(0, TestUtils.concatByteArray(AramConstTest.VALID_REF_AR_DO_CLONE, AramConstTest.VALID_REF_AR_DO)));
        deleteData(AramConstTest.AID_REF_DO);
        sendGetAll(new byte[]{}, 0x9000, AramConstTest.GET_DATA_EMPTY_RESPONSE);
    }

    @Test
    public void deleteByAidHash() throws CardException {
        storeData(AramConstTest.VALID_REF_AR_DO);
        sendGetAll(new byte[]{}, 0x9000, TestUtils.checkList(0, AramConstTest.VALID_REF_AR_DO));
        deleteData(AramConstTest.REF_DO);
        sendGetAll(new byte[]{}, 0x9000, AramConstTest.GET_DATA_EMPTY_RESPONSE);
    }

    @Test
    public void deleteByAidHashRule() throws CardException {
        storeData(AramConstTest.VALID_REF_AR_DO);
        sendGetAll(new byte[]{}, 0x9000, TestUtils.checkList(0, AramConstTest.VALID_REF_AR_DO));
        deleteData(AramConstTest.VALID_REF_AR_DO);
        sendGetAll(new byte[]{}, 0x9000, AramConstTest.GET_DATA_EMPTY_RESPONSE);
    }

    @Test
    public void deleteByAidHashRuleEmpty() throws CardException {
        storeData(AramConstTest.VALID_REF_AR_DO);
        sendGetAll(new byte[]{}, 0x9000, TestUtils.checkList(0, AramConstTest.VALID_REF_AR_DO));
        deleteData(AramConstTest.VALID_REF_AR_DO_EMPTY);
        sendGetAll(new byte[]{}, 0x9000, AramConstTest.GET_DATA_EMPTY_RESPONSE);
    }

    @Test
    public void refreshTag() throws CardException {
        byte[] req1 = sendGetRefreshTag(new byte[]{}, 0x9000).getData();
        assertEquals(11, req1.length);
        assertArrayEquals(new byte[]{(byte) 0xDF,0x20}, new byte[]{req1[0],req1[1]});
        assertEquals(8, req1[2]);
        byte[] req2 = sendGetRefreshTag(new byte[]{}, 0x9000).getData();
        assertArrayEquals(req1, req2);
        updateRefreshTag();
        byte[] req3 = sendGetRefreshTag(new byte[]{}, 0x9000).getData();
        assertNotEquals(req3, req2);
    }

    @Test
    public void getSpecificArDo() throws CardException {
        storeData(AramConstTest.VALID_REF_AR_DO);
        sendGetAll(new byte[]{}, 0x9000, TestUtils.checkList(0, AramConstTest.VALID_REF_AR_DO));
        getSpecific(AramConstTest.REF_DO, TestUtils.concatByteArray(new byte[]{(byte) 0xFF, (byte) 0x50, (byte) AramConstTest.VALID_REF_AR_DO.length}, AramConstTest.VALID_REF_AR_DO));
    }
}