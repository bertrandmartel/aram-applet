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

/**
 * Functions used to generate ARAM data object data from a Rule Entry object, from Global Platform spec : Secure Element Access Control Version 1.0.
 * <p>
 * This includes the management of streaming used for GET NEXT request
 */
public class AramUtils {

    /**
     * Convert a RuleEntry object to a REF-AR-DO data object.
     * <p>
     * reference : Secure Element Access control spec p46
     *
     * @param buf   apdu buffer
     * @param ofs   offset
     * @param entry rule entry
     * @return length of REF-AR-DO payload
     */
    public static byte buildRefArDo(short dataOffset, short dataOffsetMax, byte[] buf, short ofs, RuleEntry entry) {
        if (ofs >= dataOffset && ofs < dataOffsetMax) {
            buf[(short) (ofs - dataOffset)] = (byte) 0xE2;
        }
        ofs++;
        byte refDoLen = buildRefDo(dataOffset, dataOffsetMax, buf, (short) (ofs + 1), entry);
        byte arDoLen = buildArDo(dataOffset, dataOffsetMax, buf, (short) (ofs + refDoLen + 1), entry);
        if (ofs >= dataOffset && ofs < dataOffsetMax) {
            buf[(short) (ofs - dataOffset)] = (byte) (refDoLen + arDoLen);
        }
        return (byte) (refDoLen + arDoLen + 2);
    }


    /**
     * Convert a RuleEntry object to a REF-DO data object.
     * <p>
     * reference : Secure Element Access control spec p46
     *
     * @param buf   apdu buffer
     * @param ofs   offset
     * @param entry rule entry
     * @return length of REF-DO payload
     */
    public static byte buildRefDo(short dataOffset, short dataOffsetMax, byte[] buf, short ofs, RuleEntry entry) {
        if (ofs >= dataOffset && ofs < dataOffsetMax) {
            buf[(short) (ofs - dataOffset)] = (byte) 0xE1;
        }
        ofs++;
        byte aidRefDoLen = buildAidRefDo(dataOffset, dataOffsetMax, buf, (short) (ofs + 1), entry);
        byte hashRefDoLen = buildHashRefDo(dataOffset, dataOffsetMax, buf, (short) (ofs + aidRefDoLen + 1), entry);
        if (ofs >= dataOffset && ofs < dataOffsetMax) {
            buf[(short) (ofs - dataOffset)] = (byte) (aidRefDoLen + hashRefDoLen);
        }
        return (byte) (aidRefDoLen + hashRefDoLen + 2);
    }

    /**
     * Convert a RuleEntry object to a AR-DO data object.
     * <p>
     * reference : Secure Element Access control spec p47
     *
     * @param buf   apdu buffer
     * @param ofs   offset
     * @param entry rule entry
     * @return length of AR-DO payload
     */
    public static byte buildArDo(short dataOffset, short dataOffsetMax, byte[] buf, short ofs, RuleEntry entry) {
        if (ofs >= dataOffset && ofs < dataOffsetMax) {
            buf[(short) (ofs - dataOffset)] = (byte) 0xE3;
        }
        ofs++;
        if (ofs >= dataOffset && ofs < dataOffsetMax) {
            buf[(short) (ofs - dataOffset)] = entry.getRuleLength();
        }
        ofs++;
        entry.getRule(buf, ofs, dataOffset, dataOffsetMax);
        return (byte) (entry.getRuleLength() + 2);
    }

    /**
     * Convert a RuleEntry object to a AID-REF-DO data object.
     * <p>
     * reference : Secure Element Access control spec p45
     *
     * @param buf   apdu buffer
     * @param ofs   offset
     * @param entry rule entry
     * @return length of AID-REF-DO payload
     */
    public static byte buildAidRefDo(short dataOffset, short dataOffsetMax, byte[] buf, short ofs, RuleEntry entry) {
        if (ofs >= dataOffset && ofs < dataOffsetMax) {
            buf[(short) (ofs - dataOffset)] = (byte) 0x4F;
        }
        ofs++;
        if (ofs >= dataOffset && ofs < dataOffsetMax) {
            buf[(short) (ofs - dataOffset)] = entry.getAidLength();
        }
        ofs++;
        entry.getAid(buf, ofs, dataOffset, dataOffsetMax);
        return (byte) (entry.getAidLength() + 2);
    }

    /**
     * Convert a RuleEntry object to a HASH-REF-DO data object.
     * <p>
     * reference : Secure Element Access control spec p46
     *
     * @param buf   apdu buffer
     * @param ofs   offset
     * @param entry rule entry
     * @return length of HASH-REF-DO payload
     */
    public static byte buildHashRefDo(short dataOffset, short dataOffsetMax, byte[] buf, short ofs, RuleEntry entry) {
        if (ofs >= dataOffset && ofs < dataOffsetMax) {
            buf[(short) (ofs - dataOffset)] = (byte) 0xC1;
        }
        ofs++;
        if (ofs >= dataOffset && ofs < dataOffsetMax) {
            buf[(short) (ofs - dataOffset)] = entry.getHashLength();
        }
        ofs++;
        entry.getHash(buf, ofs, dataOffset, dataOffsetMax);
        return (byte) (entry.getHashLength() + 2);
    }
}
