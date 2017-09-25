package fr.bmartel.aram;

import apdu4j.LoggingCardTerminal;
import apdu4j.TerminalManager;
import com.licel.jcardsim.smartcardio.CardSimulator;
import com.licel.jcardsim.utils.AIDUtil;
import javacard.framework.AID;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import javax.smartcardio.*;
import javax.smartcardio.CardTerminals.State;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(Suite.class)
@SuiteClasses({RuleEntryTest.class,
        AramTest.class,
        AramUtilsTest.class})
public class TestSuite {

    private final static String APPLET_AID = "A00000015141434C00";

    private static CardSimulator mSimulator;

    private static boolean mInitialized;

    private static Card mCard;

    private static Card initGp() {

        try {
            TerminalFactory tf = TerminalManager.getTerminalFactory(null);

            CardTerminals terminals = tf.terminals();

            System.out.println("# Detected readers from " + tf.getProvider().getName());
            for (CardTerminal term : terminals.list()) {
                System.out.println((term.isCardPresent() ? "[*] " : "[ ] ") + term.getName());
            }

            // Select terminal(s) to work on
            List<CardTerminal> do_readers;
            do_readers = terminals.list(State.CARD_PRESENT);

            if (do_readers.size() == 0) {
                fail("No smart card readers with a card found");
            }
            // Work all readers
            for (CardTerminal reader : do_readers) {
                if (do_readers.size() > 1) {
                    System.out.println("# " + reader.getName());
                }

                OutputStream o = null;
                reader = LoggingCardTerminal.getInstance(reader, o);

                Card card = null;
                // Establish connection
                try {
                    card = reader.connect("*");
                    // Use use apdu4j which by default uses jnasmartcardio
                    // which uses real SCardBeginTransaction
                    card.beginExclusive();

                    return card;

                } catch (CardException e) {
                    System.err.println("Could not connect to " + reader.getName() + ": " + TerminalManager.getExceptionMessage(e));
                    continue;
                }
            }
        } catch (CardException e) {
            // Sensible wrapper for the different PC/SC exceptions
            if (TerminalManager.getExceptionMessage(e) != null) {
                System.out.println("PC/SC failure: " + TerminalManager.getExceptionMessage(e));
            } else {
                e.printStackTrace(); // TODO: remove
                fail("CardException, terminating");
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    @BeforeClass
    public static void setup() throws CardException {
        if (mInitialized) {
            return;
        }
        if (System.getProperty("testMode") != null && System.getProperty("testMode").equals("smartcard")) {
            mCard = initGp();
            CommandAPDU c = new CommandAPDU(AIDUtil.select(APPLET_AID));
            ResponseAPDU response = mCard.getBasicChannel().transmit(c);
            assertEquals(0x9000, response.getSW());
        } else {
            mSimulator = new CardSimulator();
            AID appletAID = AIDUtil.create(APPLET_AID);
            mSimulator.installApplet(appletAID, AccessRuleMaster.class);
            mSimulator.selectApplet(appletAID);
        }
        mInitialized = true;
    }

    public static Card getCard() {
        return mCard;
    }

    public static CardSimulator getSimulator() {
        return mSimulator;
    }

    @AfterClass
    public static void close() throws CardException {
        if (mCard != null) {
            mCard.endExclusive();
            mCard.disconnect(true);
            mCard = null;
        }
    }
}
