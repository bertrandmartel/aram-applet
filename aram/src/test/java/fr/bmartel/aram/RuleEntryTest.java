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

import java.lang.reflect.Field;

import static org.junit.Assert.*;

/**
 * Test Rule Entry model class.
 *
 * @author Bertrand Martel
 */
public class RuleEntryTest {

    private RuleEntry entry;

    private final static byte[] AID_BASIC = new byte[]{0x01, 0x02};
    private final static byte[] HASH_BASIC = new byte[]{0x03, 0x04, 0x05};
    private final static byte[] RULE_BASIC = new byte[]{0x05, 0x06, 0x07, 0x08};

    private final static byte[] AID_BASIC1 = new byte[]{0x11, 0x12};
    private final static byte[] HASH_BASIC1 = new byte[]{0x13, 0x14, 0x15};
    private final static byte[] RULE_BASIC1 = new byte[]{0x15, 0x16, 0x17, 0x18};

    private final static byte[] AID_BASIC2 = new byte[]{0x21, 0x22};
    private final static byte[] HASH_BASIC2 = new byte[]{0x23, 0x24, 0x25};
    private final static byte[] RULE_BASIC2 = new byte[]{0x25, 0x26, 0x27, 0x28};

    private final static byte[] AID_BASIC3 = new byte[]{0x31, 0x32};
    private final static byte[] HASH_BASIC3 = new byte[]{0x33, 0x34, 0x35};
    private final static byte[] RULE_BASIC3 = new byte[]{0x35, 0x36, 0x37, 0x38};

    private final static byte[] AID_BASIC4 = new byte[]{0x41, 0x42};

    /**
     * Get static field "first" by reflection
     *
     * @return first RuleEntry
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private RuleEntry getFirst() throws NoSuchFieldException, IllegalAccessException {
        Field f = TestUtils.getField(RuleEntry.class, "first");
        if (f == null)
            throw new NoSuchFieldException();
        return (RuleEntry) f.get(null);
    }

    /**
     * Get static field "deleted" by reflection
     *
     * @return item to recycle
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private RuleEntry getDeleted() throws NoSuchFieldException, IllegalAccessException {
        Field f = TestUtils.getField(RuleEntry.class, "deleted");
        if (f == null)
            throw new NoSuchFieldException();
        return (RuleEntry) f.get(null);
    }

    /**
     * Get the next rule entry for a specific instance
     *
     * @param entry Rule entry instance
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private RuleEntry getNext(RuleEntry entry) throws NoSuchFieldException, IllegalAccessException {
        Field f = TestUtils.getField(entry.getClass(), "next");
        if (f == null)
            throw new NoSuchFieldException();
        return (RuleEntry) f.get(entry);
    }

    /**
     * Get aid length property by reflection.
     *
     * @param entry Rule Entry instance
     * @return aid length
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private byte getAidLength(RuleEntry entry) throws NoSuchFieldException, IllegalAccessException {
        Field f = TestUtils.getField(entry.getClass(), "aidLength");
        if (f == null)
            throw new NoSuchFieldException();
        return f.getByte(entry);
    }

    /**
     * Get hash length property by reflection.
     *
     * @param entry Rule entry instance
     * @return hash length
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private byte getHashLength(RuleEntry entry) throws NoSuchFieldException, IllegalAccessException {
        Field f = TestUtils.getField(entry.getClass(), "hashLength");
        if (f == null)
            throw new NoSuchFieldException();
        return f.getByte(entry);
    }

    /**
     * Get rule length property by reflection.
     *
     * @param entry RuleEntry entry instance
     * @return rule length
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private byte getRuleLength(RuleEntry entry) throws NoSuchFieldException, IllegalAccessException {
        Field f = TestUtils.getField(entry.getClass(), "ruleLength");
        if (f == null)
            throw new NoSuchFieldException();
        return f.getByte(entry);
    }

    /**
     * Get byte array property
     *
     * @param object Object instance
     * @param name   field name
     * @return byte array object matching the specified field name
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     */
    private byte[] getByteArray(Object object, String name) throws IllegalAccessException, NoSuchFieldException {
        Field f = TestUtils.getField(object.getClass(), name);
        if (f == null)
            throw new NoSuchFieldException();
        return (byte[]) f.get(object);
    }

    /**
     * Check size of byte array properties
     *
     * @param entry Rule entry instance
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     */
    private void checkDataSize(RuleEntry entry) throws IllegalAccessException, NoSuchFieldException {
        assertNotNull("aid array not null", getByteArray(entry, "aid"));
        assertEquals("aid array size check", getByteArray(entry, "aid").length, RuleEntry.SIZE_AID);
        assertNotNull("hash array not null", getByteArray(entry, "hash"));
        assertEquals("hash array size check", getByteArray(entry, "hash").length, RuleEntry.SIZE_HASH);
        assertNotNull("rule array not null", getByteArray(entry, "rule"));
        assertEquals("rule array size check", getByteArray(entry, "rule").length, RuleEntry.SIZE_RULE);
    }

    private void addItem(byte[] aid, byte[] hash, byte[] rule) throws NoSuchFieldException, IllegalAccessException {
        entry = RuleEntry.getInstance();

        assertNotNull("created instance exist", entry);

        RuleEntry firstElement = getFirst();

        assertNotNull("first element exists", firstElement);
        assertEquals("first element is the created element", firstElement, entry);

        assertNull("no item to recycle", getDeleted());

        assertEquals("next getter valid", getNext(entry), entry.getNext());
        assertEquals("first getter valid", getFirst(), RuleEntry.getFirst());

        assertEquals("check aid length getter", 0, entry.getAidLength());

        assertEquals("check empty rule length", 0, entry.getRule(new byte[RuleEntry.SIZE_RULE], (short) 0));
        assertEquals("check empty hash length", 0, entry.getHash(new byte[RuleEntry.SIZE_HASH], (short) 0));
        assertEquals("check empty aid length", 0, entry.getAid(new byte[RuleEntry.SIZE_AID], (short) 0));

        assertEquals("check rule length via reflection", 0, getRuleLength(entry));
        assertEquals("check hash length via reflection", 0, getHashLength(entry));
        assertEquals("check aid length via reflection", 0, getAidLength(entry));

        entry.setAid(aid, (short) 0, (byte) aid.length);
        entry.setHash(hash, (short) 0, (byte) hash.length);
        entry.setRule(rule, (short) 0, (byte) rule.length);

        checkDataSize(entry);
    }

    private void deleteAid(byte[] buff) {
        RuleEntry.deleteAid(buff, (short) 0, (byte) buff.length);
    }

    private void deleteAidHash(byte[] buff, short aidOfs, byte aidLen, short hashOfs, byte hashLen) {
        RuleEntry.deleteAidHash(buff, aidOfs, aidLen, hashOfs, hashLen);
    }

    private void deleteAidHashRule(byte[] buff, short aidOfs, byte aidLen, short hashOfs, byte hashLen, short ruleOfs, byte ruleLen) {
        RuleEntry.deleteAidHashRule(buff, aidOfs, aidLen, hashOfs, hashLen, ruleOfs, ruleLen);
    }

    private void checkData(RuleEntry entry, byte[] expectedAId, byte[] expectedHash, byte[] expectedRule) {
        byte[] ruleOut = new byte[expectedRule.length];
        byte[] hashOut = new byte[expectedHash.length];
        byte[] aidOut = new byte[expectedAId.length];

        assertEquals("check rule length", expectedRule.length, entry.getRule(ruleOut, (short) 0));
        assertEquals("check hash length", expectedHash.length, entry.getHash(hashOut, (short) 0));
        assertEquals("check aid length", expectedAId.length, entry.getAid(aidOut, (short) 0));

        assertArrayEquals("check rule data", expectedRule, ruleOut);
        assertArrayEquals("check hash data", expectedHash, hashOut);
        assertArrayEquals("check aid data", expectedAId, aidOut);
    }

    private int getLength() {
        int length = 0;
        RuleEntry current = RuleEntry.getFirst();
        while (current != null) {
            length++;
            current = current.getNext();
        }
        return length;
    }

    @Before
    public void initTest() throws NoSuchFieldException, IllegalAccessException {
        Field f = TestUtils.getField(RuleEntry.class, "first");
        if (f == null)
            throw new NoSuchFieldException();
        f.set(null, null);
        f = TestUtils.getField(RuleEntry.class, "deleted");
        if (f == null)
            throw new NoSuchFieldException();
        f.set(null, null);
        assertNull("no first element", getFirst());
        assertNull("no item to recycle", getDeleted());
    }

    @Test
    public void setPropertiesTest() throws NoSuchFieldException, IllegalAccessException {
        addItem(AID_BASIC, HASH_BASIC, RULE_BASIC);
        assertNull("no next element", entry.getNext());

        entry.setAid(AID_BASIC, (short) 0, (byte) AID_BASIC.length);
        entry.setHash(HASH_BASIC, (short) 0, (byte) HASH_BASIC.length);
        entry.setRule(RULE_BASIC, (short) 0, (byte) RULE_BASIC.length);

        assertEquals("check id length getter", AID_BASIC.length, entry.getAidLength());

        checkData(entry, AID_BASIC, HASH_BASIC, RULE_BASIC);
    }

    @Test
    public void setPropertiesOffsetTest() throws NoSuchFieldException, IllegalAccessException {
        addItem(AID_BASIC, HASH_BASIC, RULE_BASIC);
        assertNull("no next element", entry.getNext());

        short offset = 2;

        entry.setAid(TestUtils.addOffset(offset, AID_BASIC), offset, (byte) AID_BASIC.length);
        entry.setHash(TestUtils.addOffset(offset, HASH_BASIC), offset, (byte) HASH_BASIC.length);
        entry.setRule(TestUtils.addOffset(offset, RULE_BASIC), offset, (byte) RULE_BASIC.length);

        assertEquals("check aid length getter", AID_BASIC.length, entry.getAidLength());

        checkData(entry, AID_BASIC, HASH_BASIC, RULE_BASIC);
    }

    @Test
    public void addMultipleEntries() throws NoSuchFieldException, IllegalAccessException {
        assertEquals(0, getLength());
        addItem(AID_BASIC, HASH_BASIC, RULE_BASIC);
        assertEquals(1, getLength());
        addItem(AID_BASIC, HASH_BASIC, RULE_BASIC);
        assertEquals(2, getLength());
        addItem(AID_BASIC, HASH_BASIC, RULE_BASIC);
        assertEquals(3, getLength());
    }

    private RuleEntry checkSearchedItem(byte[] aid, byte[] hash, byte[] rule) {
        RuleEntry searchEntry = RuleEntry.searchAid(aid, (short) 0, (byte) aid.length);
        assertNotNull("search result not null", searchEntry);
        checkData(searchEntry, aid, hash, rule);
        return searchEntry;
    }

    @Test
    public void searchIdValues() throws NoSuchFieldException, IllegalAccessException {
        addItem(AID_BASIC, HASH_BASIC, RULE_BASIC);
        addItem(AID_BASIC1, HASH_BASIC1, RULE_BASIC1);
        addItem(AID_BASIC2, HASH_BASIC2, RULE_BASIC2);
        addItem(AID_BASIC3, HASH_BASIC3, RULE_BASIC3);

        assertEquals("init length", 4, getLength());

        assertNull("search value check next null (eg first added item)", checkSearchedItem(AID_BASIC, HASH_BASIC, RULE_BASIC).getNext());
        assertNotNull("search value next not null", checkSearchedItem(AID_BASIC1, HASH_BASIC1, RULE_BASIC1).getNext());
        assertNotNull("search value next not null", checkSearchedItem(AID_BASIC2, HASH_BASIC2, RULE_BASIC2).getNext());
        assertNotNull("search value next not null", checkSearchedItem(AID_BASIC3, HASH_BASIC3, RULE_BASIC3).getNext());

        RuleEntry searchEntry = RuleEntry.searchAid(AID_BASIC4, (short) 0, (byte) AID_BASIC4.length);
        assertNull("search value invalid", searchEntry);
    }

    @Test
    public void deleteAidTest() throws NoSuchFieldException, IllegalAccessException {
        assertEquals("init length", 0, getLength());
        addItem(AID_BASIC, HASH_BASIC, RULE_BASIC);
        assertEquals("length after addition", 1, getLength());
        deleteAid(AID_BASIC);
        assertEquals("length after deletion", 0, getLength());
    }

    @Test
    public void deleteAidMultipleTest() throws NoSuchFieldException, IllegalAccessException {
        addItem(AID_BASIC, HASH_BASIC, RULE_BASIC);
        addItem(AID_BASIC1, HASH_BASIC1, RULE_BASIC1);
        addItem(AID_BASIC2, HASH_BASIC2, RULE_BASIC2);
        addItem(AID_BASIC3, HASH_BASIC3, RULE_BASIC3);
        assertEquals("init length", 4, getLength());
        deleteAid(AID_BASIC);
        deleteAid(AID_BASIC1);
        deleteAid(AID_BASIC2);
        deleteAid(AID_BASIC3);
        assertEquals("length after deletion", 0, getLength());
    }

    @Test
    public void deleteAidMultipleNonEmptyTest() throws NoSuchFieldException, IllegalAccessException {
        addItem(AID_BASIC1, HASH_BASIC, RULE_BASIC);
        addItem(AID_BASIC, HASH_BASIC1, RULE_BASIC1);
        addItem(AID_BASIC, HASH_BASIC, RULE_BASIC);
        deleteAid(AID_BASIC);
        assertEquals("length after deletion", 1, getLength());
    }

    @Test
    public void deleteAidMultipleSameTest() throws NoSuchFieldException, IllegalAccessException {
        addItem(AID_BASIC, HASH_BASIC, RULE_BASIC);
        addItem(AID_BASIC, HASH_BASIC1, RULE_BASIC1);
        addItem(AID_BASIC, HASH_BASIC2, RULE_BASIC2);
        addItem(AID_BASIC, HASH_BASIC3, RULE_BASIC3);
        assertEquals("init length", 4, getLength());
        deleteAid(AID_BASIC);
        assertEquals("length after deletion", 0, getLength());
    }

    @Test
    public void deleteAidHashTest() throws NoSuchFieldException, IllegalAccessException {
        assertEquals("init length", 0, getLength());
        addItem(AID_BASIC, HASH_BASIC, RULE_BASIC);
        assertEquals("length after addition", 1, getLength());
        deleteAidHash(TestUtils.concatByteArray(AID_BASIC, HASH_BASIC),
                (short) 0, (byte) AID_BASIC.length, (short) AID_BASIC.length, (byte) HASH_BASIC.length);
        assertEquals("length after deletion", 0, getLength());
    }

    @Test
    public void deleteAidHashWrongHashTest() throws NoSuchFieldException, IllegalAccessException {
        assertEquals("init length", 0, getLength());
        addItem(AID_BASIC, HASH_BASIC, RULE_BASIC);
        assertEquals("length after addition", 1, getLength());
        deleteAidHash(TestUtils.concatByteArray(AID_BASIC, HASH_BASIC1),
                (short) 0, (byte) AID_BASIC.length, (short) AID_BASIC.length, (byte) HASH_BASIC1.length);
        assertEquals("length after deletion", 1, getLength());
    }

    @Test
    public void deleteAidHashMultipleTest() throws NoSuchFieldException, IllegalAccessException {
        addItem(AID_BASIC, HASH_BASIC, RULE_BASIC);
        addItem(AID_BASIC1, HASH_BASIC1, RULE_BASIC1);
        addItem(AID_BASIC2, HASH_BASIC2, RULE_BASIC2);
        addItem(AID_BASIC3, HASH_BASIC3, RULE_BASIC3);
        assertEquals("init length", 4, getLength());
        deleteAidHash(TestUtils.concatByteArray(AID_BASIC, HASH_BASIC),
                (short) 0, (byte) AID_BASIC.length, (short) AID_BASIC.length, (byte) HASH_BASIC.length);
        deleteAidHash(TestUtils.concatByteArray(AID_BASIC1, HASH_BASIC1),
                (short) 0, (byte) AID_BASIC1.length, (short) AID_BASIC1.length, (byte) HASH_BASIC1.length);
        deleteAidHash(TestUtils.concatByteArray(AID_BASIC2, HASH_BASIC2),
                (short) 0, (byte) AID_BASIC2.length, (short) AID_BASIC2.length, (byte) HASH_BASIC2.length);
        deleteAidHash(TestUtils.concatByteArray(AID_BASIC3, HASH_BASIC3),
                (short) 0, (byte) AID_BASIC3.length, (short) AID_BASIC3.length, (byte) HASH_BASIC3.length);
        assertEquals("length after deletion", 0, getLength());
    }

    @Test
    public void deleteAidHashMultipleNonEmptyTest() throws NoSuchFieldException, IllegalAccessException {
        addItem(AID_BASIC1, HASH_BASIC, RULE_BASIC);
        addItem(AID_BASIC, HASH_BASIC, RULE_BASIC1);
        addItem(AID_BASIC, HASH_BASIC, RULE_BASIC);
        deleteAidHash(TestUtils.concatByteArray(AID_BASIC, HASH_BASIC),
                (short) 0, (byte) AID_BASIC.length, (short) AID_BASIC.length, (byte) HASH_BASIC.length);
        assertEquals("length after deletion", 1, getLength());
    }

    @Test
    public void deleteAidHashMultipleSameTest() throws NoSuchFieldException, IllegalAccessException {
        addItem(AID_BASIC, HASH_BASIC, RULE_BASIC);
        addItem(AID_BASIC, HASH_BASIC, RULE_BASIC1);
        addItem(AID_BASIC, HASH_BASIC, RULE_BASIC2);
        addItem(AID_BASIC, HASH_BASIC, RULE_BASIC3);
        assertEquals("init length", 4, getLength());
        deleteAidHash(TestUtils.concatByteArray(AID_BASIC, HASH_BASIC),
                (short) 0, (byte) AID_BASIC.length, (short) AID_BASIC.length, (byte) HASH_BASIC.length);
        assertEquals("length after deletion", 0, getLength());
    }

    @Test
    public void deleteAidHashRuleTest() throws NoSuchFieldException, IllegalAccessException {
        assertEquals("init length", 0, getLength());
        addItem(AID_BASIC, HASH_BASIC, RULE_BASIC);
        assertEquals("length after addition", 1, getLength());
        deleteAidHashRule(TestUtils.concatByteArray(AID_BASIC, HASH_BASIC, RULE_BASIC),
                (short) 0, (byte) AID_BASIC.length, (short) AID_BASIC.length, (byte) HASH_BASIC.length,
                (short) (AID_BASIC.length + HASH_BASIC.length), (byte) RULE_BASIC.length);
        assertEquals("length after deletion", 0, getLength());
    }

    @Test
    public void deleteAidHashRuleWrongHashTest() throws NoSuchFieldException, IllegalAccessException {
        assertEquals("init length", 0, getLength());
        addItem(AID_BASIC, HASH_BASIC, RULE_BASIC);
        assertEquals("length after addition", 1, getLength());
        deleteAidHashRule(TestUtils.concatByteArray(AID_BASIC, HASH_BASIC, RULE_BASIC1),
                (short) 0, (byte) AID_BASIC.length, (short) AID_BASIC.length, (byte) HASH_BASIC.length,
                (short) (AID_BASIC.length + HASH_BASIC.length), (byte) RULE_BASIC1.length);
        assertEquals("length after deletion", 1, getLength());
    }

    @Test
    public void deleteAidHashRuleMultipleTest() throws NoSuchFieldException, IllegalAccessException {
        addItem(AID_BASIC, HASH_BASIC, RULE_BASIC);
        addItem(AID_BASIC1, HASH_BASIC1, RULE_BASIC1);
        addItem(AID_BASIC2, HASH_BASIC2, RULE_BASIC2);
        addItem(AID_BASIC3, HASH_BASIC3, RULE_BASIC3);
        assertEquals("init length", 4, getLength());
        deleteAidHashRule(TestUtils.concatByteArray(AID_BASIC, HASH_BASIC, RULE_BASIC),
                (short) 0, (byte) AID_BASIC.length, (short) AID_BASIC.length, (byte) HASH_BASIC.length,
                (short) (AID_BASIC.length + HASH_BASIC.length), (byte) RULE_BASIC.length);
        deleteAidHashRule(TestUtils.concatByteArray(AID_BASIC1, HASH_BASIC1, RULE_BASIC1),
                (short) 0, (byte) AID_BASIC1.length, (short) AID_BASIC1.length, (byte) HASH_BASIC1.length,
                (short) (AID_BASIC1.length + HASH_BASIC1.length), (byte) RULE_BASIC1.length);
        deleteAidHashRule(TestUtils.concatByteArray(AID_BASIC2, HASH_BASIC2, RULE_BASIC2),
                (short) 0, (byte) AID_BASIC2.length, (short) AID_BASIC2.length, (byte) HASH_BASIC2.length,
                (short) (AID_BASIC2.length + HASH_BASIC2.length), (byte) RULE_BASIC2.length);
        deleteAidHashRule(TestUtils.concatByteArray(AID_BASIC3, HASH_BASIC3, RULE_BASIC3),
                (short) 0, (byte) AID_BASIC3.length, (short) AID_BASIC3.length, (byte) HASH_BASIC3.length,
                (short) (AID_BASIC3.length + HASH_BASIC3.length), (byte) RULE_BASIC3.length);
        assertEquals("length after deletion", 0, getLength());
    }

    @Test
    public void deleteAidHashRuleMultipleNonEmptyTest() throws NoSuchFieldException, IllegalAccessException {
        addItem(AID_BASIC1, HASH_BASIC, RULE_BASIC);
        addItem(AID_BASIC, HASH_BASIC, RULE_BASIC);
        addItem(AID_BASIC, HASH_BASIC, RULE_BASIC);
        deleteAidHashRule(TestUtils.concatByteArray(AID_BASIC, HASH_BASIC, RULE_BASIC),
                (short) 0, (byte) AID_BASIC.length, (short) AID_BASIC.length, (byte) HASH_BASIC.length,
                (short) (AID_BASIC.length + HASH_BASIC.length), (byte) RULE_BASIC.length);
        assertEquals("length after deletion", 1, getLength());
    }

    @Test
    public void deleteAidHashRuleMultipleSameTest() throws NoSuchFieldException, IllegalAccessException {
        addItem(AID_BASIC, HASH_BASIC, RULE_BASIC);
        addItem(AID_BASIC, HASH_BASIC, RULE_BASIC);
        addItem(AID_BASIC, HASH_BASIC, RULE_BASIC);
        addItem(AID_BASIC, HASH_BASIC, RULE_BASIC);
        assertEquals("init length", 4, getLength());
        deleteAidHashRule(TestUtils.concatByteArray(AID_BASIC, HASH_BASIC, RULE_BASIC),
                (short) 0, (byte) AID_BASIC.length, (short) AID_BASIC.length, (byte) HASH_BASIC.length,
                (short) (AID_BASIC.length + HASH_BASIC.length), (byte) RULE_BASIC.length);
        assertEquals("length after deletion", 0, getLength());
    }

    @Test
    public void checkRecycledItem() throws NoSuchFieldException, IllegalAccessException {
        assertNull("no item to recycle", getDeleted());
        addItem(AID_BASIC, HASH_BASIC, RULE_BASIC);
        deleteAid(AID_BASIC);
        assertNotNull("1 item to recycle", getDeleted());
    }

    @Test
    public void interTwinedAddDelete() throws NoSuchFieldException, IllegalAccessException {
        for (int i = 0; i < 10; i++) {
            assertEquals("init length, iteration n°" + i, 0, getLength());
            addItem(AID_BASIC, HASH_BASIC, RULE_BASIC);
            assertEquals("length after addition, iteration n°" + i, 1, getLength());
            deleteAid(AID_BASIC);
            assertEquals("length after deletion, iteration n°" + i, 0, getLength());
        }
    }

    @Test
    public void searchAidHash() throws NoSuchFieldException, IllegalAccessException {
        addItem(AID_BASIC, HASH_BASIC, RULE_BASIC);
        RuleEntry re = RuleEntry.searchAidHash(TestUtils.concatByteArray(AID_BASIC, HASH_BASIC),
                (short) 0, (byte) AID_BASIC.length, (short) AID_BASIC.length, (byte) HASH_BASIC.length);
        assertNotNull(re);
        assertEquals(re.getAidLength(), AID_BASIC.length);
        byte[] aidData = new byte[AID_BASIC.length];
        byte[] hashData = new byte[HASH_BASIC.length];
        byte[] ruleData = new byte[RULE_BASIC.length];
        assertEquals(AID_BASIC.length, re.getAid(aidData, (short) 0));
        assertEquals(HASH_BASIC.length, re.getHash(hashData, (short) 0));
        assertEquals(RULE_BASIC.length, re.getRule(ruleData, (short) 0));
        assertArrayEquals(AID_BASIC, aidData);
        assertArrayEquals(HASH_BASIC, hashData);
        assertArrayEquals(RULE_BASIC, ruleData);
    }
}
