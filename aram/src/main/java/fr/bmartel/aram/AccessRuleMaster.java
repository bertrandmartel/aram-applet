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

import javacard.framework.*;
import javacard.security.RandomData;
import org.globalplatform.Application;

/**
 * Access Rule Application Master Applet.
 * <p>
 * reference document : Global Platform Secure Element Access Control Version 1.0
 *
 * @author Bertrand Martel
 */
public class AccessRuleMaster extends Applet implements Application {

    public final static byte INS_STORE_DATA = (byte) 0xE2;
    public final static byte INS_GET_DATA = (byte) 0xCA;

    /**
     * APDU data size.
     */
    public final static short APDU_CHUNK = (short) 255;

    /**
     * the random refresh tag.
     */
    private byte[] refreshTag;

    /**
     * current offset for GET ALL/NEXT.
     */
    private short dataOffset;
    /**
     * total length of data for GET ALL/NEXT.
     */
    private short nextLength;
    /**
     * current chunk index to send for next GET NEXT command.
     */
    private short currentNext;

    private AccessRuleMaster() {
        refreshTag = new byte[8];
    }

    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new AccessRuleMaster().register();
    }

    public void process(APDU apdu) {
        if (selectingApplet()) {
            return;
        }

        byte[] buffer = apdu.getBuffer();

        if (((byte) (buffer[ISO7816.OFFSET_CLA] & (byte) 0xFC)) != (byte) 0x80 ) {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }

        switch (buffer[ISO7816.OFFSET_INS]) {
            case INS_STORE_DATA:
                if (apdu.setIncomingAndReceive() != (short) (buffer[ISO7816.OFFSET_LC] & 0xFF))
                    ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

                processCmdStoreData(APDU.getCurrentAPDUBuffer());
                break;
            case INS_GET_DATA:
                processCmdGetData();
                break;
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }

    /**
     * process GET DATA command (p23 Secure Element Access Control Version 1.0).
     */
    private void processCmdGetData() {

        byte[] buf = APDU.getCurrentAPDUBuffer();

        if (buf[ISO7816.OFFSET_P1] == (byte) 0xFF && buf[ISO7816.OFFSET_P2] == (byte) 0x40) {
            //get all
            processGetAll();
        } else if (buf[ISO7816.OFFSET_P1] == (byte) 0xFF && buf[ISO7816.OFFSET_P2] == (byte) 0x50) {
            // get specific
            processGetSpecific();
        } else if (buf[ISO7816.OFFSET_P1] == (byte) 0xDF && buf[ISO7816.OFFSET_P2] == (byte) 0x20) {
            //get refresh tag
            processGetRefreshTag();
        } else if (buf[ISO7816.OFFSET_P1] == (byte) 0xFF && buf[ISO7816.OFFSET_P2] == (byte) 0x60) {
            //get next
            processGetNext();
        } else {
            ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
        }
    }

    /**
     * process GET DATA ALL (p23 & p25 Secure Element Access Control Version 1.0).
     */
    private void processGetAll() {

        byte[] buf = APDU.getCurrentAPDUBuffer();

        buf[0] = (byte) 0xFF;
        buf[1] = (byte) 0x40;

        RuleEntry entry = RuleEntry.getFirst();

        short offset = 2;
        if (entry == null) {
            buf[offset++] = 0;
            APDU.getCurrentAPDU().setOutgoingAndSend((short) 0, (short) 3);
        } else {
            dataOffset = 0;
            while (entry != null) {
                short len = AramUtils.buildRefArDo(dataOffset, (short) (dataOffset + APDU_CHUNK), buf, (short) (offset + 1), entry);
                offset += len;
                entry = entry.getNext();
            }
            offset++;

            short length = (short) (offset - 3);

            currentNext = 1;
            nextLength = length;

            if (length < (short) 0x80) {
                buf[2] = (byte) length;
                dataOffset = -3;
                APDU.getCurrentAPDU().setOutgoingAndSend((short) 0, offset);
            } else if (length < (short) 0xFF) {
                Util.arrayCopy(buf, (short) 3, buf, (short) 4, (short) (buf.length - 4));
                buf[2] = (byte) 0x81;
                buf[3] = (byte) length;
                dataOffset = -4;
                APDU.getCurrentAPDU().setOutgoingAndSend((short) 0, (short) (offset + 1));
            } else if (length < (short) 0x7FFF) {
                Util.arrayCopy(buf, (short) 2, buf, (short) 4, (short) (buf.length - 4));
                buf[2] = (byte) 0x82;
                buf[3] = (byte) ((length >> 8) & 0xFF);
                buf[4] = (byte) (length & 0xFF);
                dataOffset = -5;
                APDU.getCurrentAPDU().setOutgoingAndSend((short) 0, APDU_CHUNK);
            } else {
                ISOException.throwIt(ISO7816.SW_DATA_INVALID);
            }
        }
    }

    /**
     * process GET DATA SPECIFIC (p23 & p26 Secure Element Access Control Version 1.0).
     */
    private void processGetSpecific() {

        byte[] buf = APDU.getCurrentAPDUBuffer();

        APDU apdu = APDU.getCurrentAPDU();

        if (apdu.setIncomingAndReceive() !=
                (short) (buf[ISO7816.OFFSET_LC] & 0xFF))
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

        short ofs = ISO7816.OFFSET_CDATA;

        checkTLV(buf, (short) (ofs + 1), (byte) 0xE1, (short) (4 + RuleEntry.SIZE_AID + RuleEntry.SIZE_HASH));
        short ofsAidRefDo = (short) (ofs + 3);
        short ofsHashRefDo = checkTLV(buf, (short) (ofs + 3), (byte) 0x4F, (RuleEntry.SIZE_AID));
        checkTLV(buf, ofsHashRefDo, (byte) 0xC1, (RuleEntry.SIZE_HASH));

        RuleEntry re = RuleEntry.searchAidHash(buf,
                (short) (ofsAidRefDo + 2), buf[(short) (ofsAidRefDo + 1)],
                (short) (ofsHashRefDo + 2), buf[(short) (ofsHashRefDo + 1)]);

        if (re == null)
            ISOException.throwIt((short) 0x6A88);

        buf[0] = (byte) 0xFF;
        buf[1] = (byte) 0x50;

        short len = AramUtils.buildRefArDo((short) 0, APDU_CHUNK, buf, (short) 3, re);
        buf[2] = (byte) len;

        APDU.getCurrentAPDU().setOutgoingAndSend((short) 0, (short) (len + 3));
    }

    /**
     * process GET DATA REFRESH TAG (p23 & p26 Secure Element Access Control Version 1.0).
     */
    private void processGetRefreshTag() {

        byte[] buf = APDU.getCurrentAPDUBuffer();

        buf[0] = (byte) 0xDF;
        buf[1] = (byte) 0x20;
        buf[2] = (byte) 8;

        Util.arrayCopy(refreshTag, (short) 0, buf, (short) 3, (short) 8);
        APDU.getCurrentAPDU().setOutgoingAndSend((short) 0, (short) 11);
    }

    /**
     * process GET DATA NEXT (p23 Secure Element Access Control Version 1.0).
     */
    private void processGetNext() {

        short next = (short) (nextLength / APDU_CHUNK);
        if ((nextLength % APDU_CHUNK) != 0) {
            next++;
        }

        if (currentNext < next) {

            byte[] buf = APDU.getCurrentAPDUBuffer();

            RuleEntry entry = RuleEntry.getFirst();

            short offset = 0;
            dataOffset += APDU_CHUNK;

            short currentLength = 0;

            short diff = (short) (nextLength - currentNext * APDU_CHUNK);

            if (diff > APDU_CHUNK) {
                currentLength = APDU_CHUNK;
            } else {
                short header = (short) (APDU_CHUNK - ((short) (dataOffset % APDU_CHUNK)));
                currentLength = (short) (diff + header);
            }

            while (entry != null) {
                short len = AramUtils.buildRefArDo(dataOffset, (short) (dataOffset + APDU_CHUNK), buf, offset, entry);
                offset += len;
                entry = entry.getNext();
            }
            currentNext++;

            APDU.getCurrentAPDU().setOutgoingAndSend((short) 0, currentLength);
        } else {
            ISOException.throwIt((short) 0x6A88);
        }
    }

    /**
     * process STORE DATA command (p36 Secure Element Access Control Version 1.0).
     */
    private void processCmdStoreData(byte[] buf) {

        if (buf[ISO7816.OFFSET_P1] == (byte) 0x90 && buf[ISO7816.OFFSET_P2] == (byte) 0x00) {

            short ofs = ISO7816.OFFSET_CDATA;

            if (buf[ofs] == (byte) 0xF0) {
                //Command-Store-AR-DO
                storeArDo(buf);
            } else if (buf[ofs] == (byte) 0xF1) {
                //Command-Delete-AR-DO
                deleteArDo(buf);
            } else if (buf[ofs] == (byte) 0xF2) {
                //Command-UpdateRefreshTag-DO
                updateRefreshTag();
            } else {
                ISOException.throwIt(ISO7816.SW_DATA_INVALID);
            }
        } else {
            ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
        }
    }

    /**
     * Command-Store-AR-DO (p36 & p38 Secure Element Access Control Version 1.0).
     */
    private void storeArDo(byte[] buf) {

        short ofs = ISO7816.OFFSET_CDATA;

        checkTLV(buf, ofs, (byte) 0xF0, (short) (8 + RuleEntry.SIZE_AID + RuleEntry.SIZE_HASH + RuleEntry.SIZE_RULE));
        checkTLV(buf, (short) (ofs + 2), (byte) 0xE2, (short) (6 + RuleEntry.SIZE_AID + RuleEntry.SIZE_HASH + RuleEntry.SIZE_RULE));
        checkTLV(buf, (short) (ofs + 4), (byte) 0xE1, (short) (4 + RuleEntry.SIZE_AID + RuleEntry.SIZE_HASH));
        short ofsAidRefDo = (short) (ofs + 6);
        short ofsHashRefDo = checkTLV(buf, (short) (ofs + 6), (byte) 0x4F, (RuleEntry.SIZE_AID));
        short ofsArDo = checkTLV(buf, ofsHashRefDo, (byte) 0xC1, (RuleEntry.SIZE_HASH));
        checkTLV(buf, ofsArDo, (byte) 0xE3, (RuleEntry.SIZE_RULE));

        JCSystem.beginTransaction();
        RuleEntry pe = RuleEntry.getInstance();
        pe.setAid(buf, (short) (ofsAidRefDo + 2), buf[(short) (ofsAidRefDo + 1)]);
        pe.setHash(buf, (short) (ofsHashRefDo + 2), buf[(short) (ofsHashRefDo + 1)]);
        pe.setRule(buf, (short) (ofsArDo + 2), buf[(short) (ofsArDo + 1)]);
        JCSystem.commitTransaction();
    }

    /**
     * Command-Delete-AR-DO (p36 & p39 Secure Element Access Control Version 1.0).
     */
    private void deleteArDo(byte[] buf) {

        short ofs = ISO7816.OFFSET_CDATA;

        checkTLV(buf, ofs, (byte) 0xF1, (short) (6 + RuleEntry.SIZE_AID + RuleEntry.SIZE_HASH + RuleEntry.SIZE_RULE));
        if (buf[(short) (ofs + 1)] == 0) {
            //delete all rules if length == 0
            RuleEntry.deleteAll();
        } else if (buf[(short) (ofs + 2)] == (byte) 0x4F) {
            //delete AID-REF-DO
            checkTLV(buf, (short) (ofs + 2), (byte) 0x4F, RuleEntry.SIZE_AID);
            short ofsAidRefDo = (short) (ofs + 2);

            RuleEntry re = RuleEntry.searchAid(buf, (short) (ofsAidRefDo + 2), buf[(short) (ofsAidRefDo + 1)]);
            if (re == null)
                ISOException.throwIt((short) 0x6A88);

            RuleEntry.deleteAid(buf, (short) (ofsAidRefDo + 2), buf[(short) (ofsAidRefDo + 1)]);
        } else if (buf[(short) (ofs + 2)] == (byte) 0xE1) {
            checkTLV(buf, (short) (ofs + 2), (byte) 0xE1, (short) (4 + RuleEntry.SIZE_AID + RuleEntry.SIZE_HASH));
            //delete REF-DO
            short ofsAidRefDo = (short) (ofs + 4);
            short ofsHashRefDo = checkTLV(buf, (short) (ofs + 4), (byte) 0x4F, (RuleEntry.SIZE_AID));
            checkTLV(buf, ofsHashRefDo, (byte) 0xC1, (RuleEntry.SIZE_HASH));

            RuleEntry re = RuleEntry.searchAidHash(buf,
                    (short) (ofsAidRefDo + 2), buf[(short) (ofsAidRefDo + 1)],
                    (short) (ofsHashRefDo + 2), buf[(short) (ofsHashRefDo + 1)]);

            if (re == null)
                ISOException.throwIt((short) 0x6A88);

            RuleEntry.deleteAidHash(buf,
                    (short) (ofsAidRefDo + 2), buf[(short) (ofsAidRefDo + 1)],
                    (short) (ofsHashRefDo + 2), buf[(short) (ofsHashRefDo + 1)]);
        } else if (buf[(short) (ofs + 2)] == (byte) 0xE2) {
            //delete REF-AR-DO
            checkTLV(buf, (short) (ofs + 2), (byte) 0xE2, (short) (6 + RuleEntry.SIZE_AID + RuleEntry.SIZE_HASH + RuleEntry.SIZE_RULE));
            checkTLV(buf, (short) (ofs + 4), (byte) 0xE1, (short) (4 + RuleEntry.SIZE_AID + RuleEntry.SIZE_HASH));
            short ofsAidRefDo = (short) (ofs + 6);
            short ofsHashRefDo = checkTLV(buf, (short) (ofs + 6), (byte) 0x4F, (RuleEntry.SIZE_AID));
            short ofsArDo = checkTLV(buf, ofsHashRefDo, (byte) 0xC1, (RuleEntry.SIZE_HASH));
            checkTLV(buf, ofsArDo, (byte) 0xE3, (RuleEntry.SIZE_RULE));

            if (buf[(short) (ofsArDo + 1)] > 2) {
                RuleEntry re = RuleEntry.searchAidHashRule(buf,
                        (short) (ofsAidRefDo + 2), buf[(short) (ofsAidRefDo + 1)],
                        (short) (ofsHashRefDo + 2), buf[(short) (ofsHashRefDo + 1)],
                        (short) (ofsArDo + 2), buf[(short) (ofsArDo + 1)]);

                if (re == null)
                    ISOException.throwIt((short) 0x6A88);

                RuleEntry.deleteAidHashRule(buf,
                        (short) (ofsAidRefDo + 2), buf[(short) (ofsAidRefDo + 1)],
                        (short) (ofsHashRefDo + 2), buf[(short) (ofsHashRefDo + 1)],
                        (short) (ofsArDo + 2), buf[(short) (ofsArDo + 1)]);

            } else {
                RuleEntry re = RuleEntry.searchAidHash(buf,
                        (short) (ofsAidRefDo + 2), buf[(short) (ofsAidRefDo + 1)],
                        (short) (ofsHashRefDo + 2), buf[(short) (ofsHashRefDo + 1)]);

                if (re == null)
                    ISOException.throwIt((short) 0x6A88);

                RuleEntry.deleteAidHash(buf,
                        (short) (ofsAidRefDo + 2), buf[(short) (ofsAidRefDo + 1)],
                        (short) (ofsHashRefDo + 2), buf[(short) (ofsHashRefDo + 1)]);
            }
        }
    }

    /**
     * Command-UpdateRefreshTag-DO (p36 & p39 Secure Element Access Control Version 1.0).
     */
    private void updateRefreshTag() {
        RandomData rnd = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);
        rnd.generateData(refreshTag, (short) 0, (short) 8);
    }

    /**
     * Check tag & length for tag length value.
     *
     * @param buffer apdu buffer
     * @param ofs    buffer offset
     * @param tag    tag to check
     * @param maxLen max length for this TLV
     * @return apdu data length after this TLV
     */
    short checkTLV(byte[] buffer, short ofs, byte tag, short maxLen) {
        if (buffer[ofs++] != tag)
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
        short len = buffer[ofs++];
        if (len > maxLen)
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
        return (short) (ofs + len);
    }

    /**
     * Process data from install for personalization.
     *
     * @param data input data
     * @param ofs  data offset
     * @param len  data length
     */
    public void processData(byte[] data, short ofs, short len) {
        processCmdStoreData(data);
    }
}
