package de.rub.nds.research.ssl.stack.tests.attacks;

import de.rub.nds.research.ssl.stack.protocols.ARecordFrame;
import de.rub.nds.research.ssl.stack.protocols.alert.Alert;
import de.rub.nds.research.ssl.stack.protocols.commons.ECipherSuite;
import de.rub.nds.research.ssl.stack.protocols.commons.EProtocolVersion;
import de.rub.nds.research.ssl.stack.protocols.commons.KeyExchangeParams;
import de.rub.nds.research.ssl.stack.protocols.handshake.ClientHello;
import de.rub.nds.research.ssl.stack.protocols.handshake.ClientKeyExchange;
import de.rub.nds.research.ssl.stack.protocols.handshake.datatypes.CipherSuites;
import de.rub.nds.research.ssl.stack.protocols.handshake.datatypes.EncryptedPreMasterSecret;
import de.rub.nds.research.ssl.stack.protocols.handshake.datatypes.PreMasterSecret;
import de.rub.nds.research.ssl.stack.protocols.handshake.datatypes.RandomValue;
import de.rub.nds.research.ssl.stack.protocols.msgs.TLSCiphertext;
import de.rub.nds.research.ssl.stack.tests.common.MessageBuilder;
import de.rub.nds.research.ssl.stack.tests.common.SSLHandshakeWorkflow;
import de.rub.nds.research.ssl.stack.tests.common.SSLHandshakeWorkflow.States;
import de.rub.nds.research.ssl.stack.tests.common.SSLServer;
import de.rub.nds.research.ssl.stack.tests.common.SSLTestUtils;
import de.rub.nds.research.ssl.stack.tests.trace.Trace;
import de.rub.nds.research.ssl.stack.tests.workflows.ObservableBridge;
import java.io.IOException;
import java.math.BigInteger;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import javax.crypto.BadPaddingException;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import sun.security.rsa.RSACore;

/**
 * Test for Bleichenbacher attack.
 *
 * @author Eugen Weiss - eugen.weiss@ruhr-uni-bochum.de
 * @version 0.1 Apr 12, 2012
 */
public class BleichenbacherTimingTest implements Observer {

    /**
     * Client hello message.
     */
    private ClientHello clientHello;
    /**
     * Handshake workflow to observe.
     */
    private SSLHandshakeWorkflow workflow;
    /**
     * Help utilities for testing.
     */
    private SSLTestUtils utils = new SSLTestUtils();
    /**
     * TLS protocol version.
     */
    private EProtocolVersion protocolVersion = EProtocolVersion.TLS_1_0;
    /**
     * Protocol short name.
     */
    private String protocolShortName = "TLS";
    /**
     * Test host.
     */
    private static final String HOST = "localhost";
    /**
     * Test port.
     */
    private static final int PORT = 10443;
    /**
     * Separate byte between padding and data in PKCS#1 message.
     */
    private byte[] separate;
    /**
     * Protocol version.
     */
    private EProtocolVersion version;
    /**
     * First two bytes of PKCS#1 message which defines the op-mode.
     */
    private byte[] mode;
    /**
     * Signalizes if padding should be changed.
     */
    private boolean changePadding;
    /**
     * Position in padding to change.
     */
    private int position;
    /**
     * First position in padding string.
     */
    public static final int FIRST_POSITION = 0;
    /**
     * Mid-position of the padding string.
     */
    public static final int MID_POSITION = 1;
    /**
     * Last position of the padding string.
     */
    public static final int LAST_POSITION = 2;
    /**
     * Test Server Thread.
     */
    private Thread sslServerThread;
    /**
     * Test SSL Server.
     */
    private SSLServer sslServer;
    /**
     * Server key store.
     */
    private static final String PATH_TO_JKS = "server.jks";
    /**
     * Pass word for server key store.
     */
    private static final String JKS_PASSWORD = "server";

    /**
     * Test parameters for the Bleichenbacher Tests.
     *
     * @return List of parameters
     */
    @DataProvider(name = "bleichenbacher")
    public Object[][] createData1() {
        return new Object[][]{
                    //                      ok case
                    {new byte[]{0x00, 0x02}, new byte[]{0x00}, protocolVersion,
                        false, 0},
                    //                    wrong protocol version in PreMasterSecret
                    {new byte[]{0x00, 0x02}, new byte[]{0x00},
                        EProtocolVersion.SSL_3_0, false, 0},
                    //                    seperate byte is not 0x00
                    {new byte[]{0x00, 0x02}, new byte[]{0x01}, protocolVersion,
                        false, 0},
                    //                    mode changed
                    {new byte[]{0x00, 0x01}, new byte[]{0x00}, protocolVersion,
                        false, 0},
                    //                    zero byte at the first position of the padding
                    {new byte[]{0x00, 0x02}, new byte[]{0x00}, protocolVersion,
                        true, 0},
                    //                    zero byte in the middle of the padding string
                    {new byte[]{0x00, 0x02}, new byte[]{0x00}, protocolVersion,
                        true, 1},
                    //                    zero byte at the end of the padding string
                    {new byte[]{0x00, 0x02}, new byte[]{0x00}, protocolVersion,
                        true, 2}
                };
    }

    /**
     * Test if Bleichenbacher attack is possible.
     *
     * @param mode First two bytes of PKCS#1 message which defines the op-mode
     * @param separate Separate byte between padding and data in PKCS#1 message
     * @param version Protocol version
     * @param changePadding True if padding should be changed
     * @param position Position where padding is changed
     * @throws IOException
     */
    @Test(enabled = true, dataProvider = "bleichenbacher")
    public final void testBleichenbacherPossible(final byte[] mode,
            final byte[] separate, final EProtocolVersion version,
            final boolean changePadding, final int position)
            throws IOException {
        workflow = new SSLHandshakeWorkflow();
        workflow.connectToTestServer(HOST, PORT);
        workflow.addObserver(this, States.CLIENT_HELLO);
        workflow.addObserver(this, States.CLIENT_KEY_EXCHANGE);

        this.mode = mode;
        this.separate = separate;
        this.version = version;
        this.changePadding = changePadding;
        this.position = position;

        workflow.start();

        ArrayList<Trace> traceList = workflow.getTraceList();
        ARecordFrame frame = traceList.get(traceList.size() - 1).
                getCurrentRecord();
        if (frame instanceof Alert) {
            Alert alert = (Alert) frame;
            Assert.fail("Test failed with an SSL-Alert: " + alert.getAlertLevel() + " " + alert.
                    getAlertDescription());
        }
        if ((frame instanceof TLSCiphertext) == false) {
            Assert.fail("Last message not Encrypted finished message");
        }
    }

    /**
     * Update observed object.
     *
     * @param o Observed object
     * @param arg Arguments
     */
    @Override
    public void update(final Observable o, final Object arg) {
        Trace trace = null;
        States states = null;
        ObservableBridge obs;
        if (o instanceof ObservableBridge) {
            obs = (ObservableBridge) o;
            states = (States) obs.getState();
            trace = (Trace) arg;
        }
        switch (states) {
            case CLIENT_HELLO:
                MessageBuilder builder = new MessageBuilder();
                CipherSuites suites = new CipherSuites();
                RandomValue random = new RandomValue();
                suites.setSuites(new ECipherSuite[]{
                            ECipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA});
                clientHello = builder.createClientHello(EProtocolVersion.TLS_1_0.
                        getId(),
                        random.encode(false),
                        suites.encode(false), new byte[]{0x00});

                trace.setCurrentRecord(clientHello);
                break;
            case CLIENT_KEY_EXCHANGE:
                KeyExchangeParams keyParams = KeyExchangeParams.getInstance();
                PublicKey pk = keyParams.getPublicKey();
                ClientKeyExchange cke = new ClientKeyExchange(protocolVersion,
                        keyParams.getKeyExchangeAlgorithm());
                PreMasterSecret pms = new PreMasterSecret(protocolVersion);
                workflow.setPreMasterSecret(pms);
                pms.setProtocolVersion(this.version);
                byte[] encodedPMS = pms.encode(false);

                //encrypt the PreMasterSecret
                EncryptedPreMasterSecret encPMS = new EncryptedPreMasterSecret(
                        pk);
                BigInteger mod = null;
                RSAPublicKey rsaPK = null;
                if (pk instanceof RSAPublicKey) {
                    rsaPK = (RSAPublicKey) pk;
                    mod = rsaPK.getModulus();
                }
                int modLength = mod.bitLength() / 8;

                /*
                 * set the padding length of the PKCS#1 padding string (it is
                 * [<Modulus length> - <Data length> -3])
                 */
                utils.setPaddingLength((modLength - encodedPMS.length - 3));
                utils.setSeperateByte(this.separate);
                utils.setMode(this.mode);
                //generate the PKCS#1 padding string
                byte[] padding = utils.createPaddingString(utils.
                        getPaddingLength());
                if (this.changePadding) {
                    Assert.assertFalse(this.position > utils.getPaddingLength(), 
                            "Position to large - padding length is "
                            + utils.getPaddingLength());
                    utils.changePadding(padding, this.position);
                }
                //put the PKCS#1 pieces together
                byte[] clear = utils.buildPKCS1Msg(encodedPMS);

                //compute c = m^e mod n (RSA encryption)
                byte[] ciphertext = null;
                try {
                    ciphertext = RSACore.rsa(clear, rsaPK);
                } catch (BadPaddingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                encPMS.setEncryptedPreMasterSecret(ciphertext);
                cke.setExchangeKeys(encPMS);

                trace.setCurrentRecord(cke);
                break;
            default:
                break;
        }

    }

    /**
     * Close the Socket after the test run.
     */
    @AfterMethod
    public void tearDown() {
        try {
            System.out.println("sslServer shutdown: " + sslServer);
            workflow.getSocket().close();
            sslServer.shutdown();
            sslServer = null;
            sslServerThread.interrupt();
            sslServerThread = null;

            Thread.currentThread().sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Start the target SSL Server.
     */
    @BeforeMethod
    public void setUp() {
        try {
            System.setProperty("javax.net.debug", "ssl");
            sslServer = new SSLServer(PATH_TO_JKS, JKS_PASSWORD,
                    protocolShortName, PORT);
            sslServerThread = new Thread(sslServer);
            sslServerThread.start();
            System.out.println("sslServer startup: " + sslServer);
            Thread.currentThread().sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
