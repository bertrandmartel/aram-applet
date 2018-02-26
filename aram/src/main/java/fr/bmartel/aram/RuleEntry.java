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

import javacard.framework.JCSystem;
import javacard.framework.Util;

/**
 * Rule Entry model used to store aid, hash and rule.
 *
 * @author Bertrand Martel
 */
public class RuleEntry {

    public static short SIZE_AID = 16;
    public static short SIZE_HASH = 20;
    public static short SIZE_RULE = (short) (2 + (20 * 8));

    private RuleEntry next;
    private static RuleEntry first;
    private static RuleEntry deleted;

    private byte[] aid;
    private byte[] hash;
    private byte[] rule;

    private byte aidLength;
    private byte hashLength;
    private byte ruleLength;

    /**
     * init properties.
     */
    private RuleEntry() {
        aid = new byte[SIZE_AID];
        hash = new byte[SIZE_HASH];
        rule = new byte[SIZE_RULE];
        next = first;
        first = this;
    }

    /**
     * add a new entry
     *
     * @return new rule entry
     */
    static RuleEntry getInstance() {
        if (deleted == null) {
            return new RuleEntry();
        } else {
            RuleEntry instance = deleted;
            deleted = instance.next;
            instance.next = first;
            first = instance;
            return instance;
        }
    }

    /**
     * Search by aid.
     *
     * @param buf apdu buffer
     * @param ofs offset
     * @param len length of aid to search
     * @return rune entry or null if not found
     */
    static RuleEntry searchAid(byte[] buf, short ofs, byte len) {
        for (RuleEntry re = first; re != null; re = re.next) {
            if (re.aidLength != len) continue;
            if (Util.arrayCompare(re.aid, (short) 0, buf, ofs, len) == 0)
                return re;
        }
        return null;
    }

    /**
     * Search by aid & hash.
     *
     * @param buf     apdu buffer
     * @param aidOfs  offset for the start of aid
     * @param aidLen  length of the aid to search
     * @param hashOfs offset for the start of hash
     * @param hashLen length of the hash to search
     * @return rule entry or null if not found
     */
    static RuleEntry searchAidHash(byte[] buf, short aidOfs, byte aidLen, short hashOfs, byte hashLen) {
        for (RuleEntry re = first; re != null; re = re.next) {
            if (re.aidLength != aidLen || re.hashLength != hashLen) continue;
            if (Util.arrayCompare(re.aid, (short) 0, buf, aidOfs, aidLen) == 0 &&
                    Util.arrayCompare(re.hash, (short) 0, buf, hashOfs, hashLen) == 0)
                return re;
        }
        return null;
    }

    /**
     * Search by aid & hash & rule.
     *
     * @param buf     apdu buffer
     * @param aidOfs  offset for the start of aid
     * @param aidLen  length of aid to search
     * @param hashOfs offset for the start of hash
     * @param hashLen length of hash to search
     * @param ruleOfs offset for start of rule
     * @param ruleLen length of rule to search
     * @return rule entry or null if not found
     */
    static RuleEntry searchAidHashRule(byte[] buf, short aidOfs, byte aidLen, short hashOfs, byte hashLen, short ruleOfs, byte ruleLen) {
        for (RuleEntry re = first; re != null; re = re.next) {
            if (re.aidLength != aidLen || re.hashLength != hashLen || re.ruleLength != ruleLen) continue;
            if (Util.arrayCompare(re.aid, (short) 0, buf, aidOfs, aidLen) == 0 &&
                    Util.arrayCompare(re.hash, (short) 0, buf, hashOfs, hashLen) == 0 &&
                    Util.arrayCompare(re.rule, (short) 0, buf, ruleOfs, ruleLen) == 0)
                return re;
        }
        return null;
    }

    /**
     * get the first entry.
     *
     * @return
     */
    public static RuleEntry getFirst() {
        return first;
    }

    /**
     * remove this entry.
     */
    private void remove() {
        if (first == this) {
            first = next;
        } else {
            for (RuleEntry re = first; re != null; re = re.next)
                if (re.next == this)
                    re.next = next;
        }
    }

    /**
     * recycle current entry.
     */
    private void recycle() {
        next = deleted;
        aidLength = 0;
        hashLength = 0;
        ruleLength = 0;
        deleted = this;
    }

    /**
     * delete all rules
     *
     */
    static void deleteAll() {
        JCSystem.beginTransaction();

        RuleEntry re = first;
        while (re != null) {
            re.remove();
            re.recycle();
            re = first;
        }
        JCSystem.commitTransaction();
    }

    /**
     * delete by aid.
     *
     * @param buf apdu buffer
     * @param ofs offset for the aid
     * @param len length of aid
     */
    static void deleteAid(byte[] buf, short ofs, byte len) {
        JCSystem.beginTransaction();
        RuleEntry re = searchAid(buf, ofs, len);
        while (re != null) {
            re.remove();
            re.recycle();
            re = searchAid(buf, ofs, len);
        }
        JCSystem.commitTransaction();
    }

    /**
     * delete by aid & hash.
     *
     * @param buf     apdu buffer
     * @param aidOfs  offset for the aid
     * @param aidLen  length of aid
     * @param hashOfs offset for the hash
     * @param hashLen length of the hash
     */
    static void deleteAidHash(byte[] buf, short aidOfs, byte aidLen, short hashOfs, byte hashLen) {
        JCSystem.beginTransaction();
        RuleEntry re = searchAidHash(buf, aidOfs, aidLen, hashOfs, hashLen);
        while (re != null) {
            re.remove();
            re.recycle();
            re = searchAidHash(buf, aidOfs, aidLen, hashOfs, hashLen);
        }
        JCSystem.commitTransaction();
    }

    /**
     * delete by aid & hash & rule.
     *
     * @param buf     apdu buffer
     * @param aidOfs  offset for the aid
     * @param aidLen  length of aid
     * @param hashOfs offset for the hash
     * @param hashLen length of hash
     * @param ruleOfs offset for the rule
     * @param ruleLen length of tule
     */
    static void deleteAidHashRule(byte[] buf, short aidOfs, byte aidLen, short hashOfs, byte hashLen, short ruleOfs, byte ruleLen) {
        JCSystem.beginTransaction();
        RuleEntry re = searchAidHashRule(buf, aidOfs, aidLen, hashOfs, hashLen, ruleOfs, ruleLen);
        while (re != null) {
            re.remove();
            re.recycle();
            re = searchAidHashRule(buf, aidOfs, aidLen, hashOfs, hashLen, ruleOfs, ruleLen);
        }
        JCSystem.commitTransaction();
    }

    /**
     * get this entry's aid while putting it in input apdu buffer
     *
     * @param buf apdu buffer
     * @param ofs start offset for this buffer
     * @return total length of data in apdu buffer
     */
    short getAid(byte[] buf, short ofs) {
        Util.arrayCopy(aid, (short) 0, buf, ofs, aidLength);
        return (short) (ofs + aidLength);
    }

    short getAid(byte[] buf, short ofs, short dataOffset, short dataOffsetMax) {
        return getOffsetData(aidLength, aid, buf, ofs, dataOffset, dataOffsetMax);
    }

    /**
     * get this entry's hash while putting it in input apdu buffer
     *
     * @param buf apdu buffer
     * @param ofs start offset for this buffer
     * @return total length of data in apdu buffer
     */
    short getHash(byte[] buf, short ofs) {
        Util.arrayCopy(hash, (short) 0, buf, ofs, hashLength);
        return (short) (ofs + hashLength);
    }

    short getHash(byte[] buf, short ofs, short dataOffset, short dataOffsetMax) {
        return getOffsetData(hashLength, hash, buf, ofs, dataOffset, dataOffsetMax);
    }

    /**
     * get this entry's rule while putting it in input apdu buffer
     *
     * @param buf apdu buffer
     * @param ofs start offset for this buffer
     * @return total length of data in apdu buffer
     */
    short getRule(byte[] buf, short ofs) {
        Util.arrayCopy(rule, (short) 0, buf, ofs, ruleLength);
        return (short) (ofs + ruleLength);
    }

    short getRule(byte[] buf, short ofs, short dataOffset, short dataOffsetMax) {
        return getOffsetData(ruleLength, rule, buf, ofs, dataOffset, dataOffsetMax);
    }

    /**
     * get this entry's rule while putting it in input apdu buffer
     *
     * @param buf apdu buffer
     * @param ofs start offset for this buffer
     * @return total length of data in apdu buffer
     */
    short getOffsetData(byte dataLength, byte[] data, byte[] buf, short ofs, short dataOffset, short dataOffsetMax) {
        if (ofs >= dataOffset && ofs < dataOffsetMax) {
            if (dataLength <= (short) (dataOffsetMax - ofs)) {
                Util.arrayCopy(data, (short) 0, buf, (short) (ofs - dataOffset), dataLength);
            } else {
                Util.arrayCopy(data, (short) 0, buf, (short) (ofs - dataOffset), (short) (dataOffsetMax - ofs));
            }
        } else {
            if ((short) (ofs + dataLength) > dataOffset && (short) (ofs + dataLength) < dataOffsetMax) {
                Util.arrayCopy(data,
                        (short) (dataOffset - ofs),
                        buf,
                        (short) 0,
                        (short) (dataLength - (dataOffset - ofs)));
            }
        }
        return (short) (ofs + dataLength);
    }

    public byte getAidLength() {
        return aidLength;
    }

    public byte getHashLength() {
        return hashLength;
    }

    public byte getRuleLength() {
        return ruleLength;
    }

    public RuleEntry getNext() {
        return next;
    }

    /**
     * set the aid value for this entry from apdu buffer.
     *
     * @param buf apdu buffer
     * @param ofs offset for the aid
     * @param len length of aid
     */
    public void setAid(byte[] buf, short ofs, byte len) {
        Util.arrayCopy(buf, ofs, aid, (short) 0, len);
        aidLength = len;
    }

    /**
     * set the hash value for this entry from apdu buffer.
     *
     * @param buf apdu buffer
     * @param ofs offset for the hash
     * @param len length of hash
     */
    public void setHash(byte[] buf, short ofs, byte len) {
        Util.arrayCopy(buf, ofs, hash, (short) 0, len);
        hashLength = len;
    }

    /**
     * set the rule value for this entry from apdu buffer.
     *
     * @param buf apdu buffer
     * @param ofs offset for the rule
     * @param len length of rule
     */
    public void setRule(byte[] buf, short ofs, byte len) {
        Util.arrayCopy(buf, ofs, rule, (short) 0, len);
        ruleLength = len;
    }
}
