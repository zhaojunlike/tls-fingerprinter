package de.rub.nds.ssl.analyzer.parameters;

import de.rub.nds.ssl.stack.Utility;
import de.rub.nds.ssl.stack.protocols.commons.ECipherSuite;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.log4j.Logger;

/**
 * Defines the ClientKeyExchange parameters for tests.
 *
 * @author Eugen Weiss - eugen.weiss@ruhr-uni-bochum
 * @version 0.1 Jun. 21, 2012
 */
public final class ClientKeyExchangeParams extends AParameters {

    /**
     * Log4j logger initialization.
     */
    private static Logger logger = Logger.getRootLogger();
    /**
     * Cipher suite for tests
     */
    private ECipherSuite[] cipherSuite;
    /**
     * ClientKeyExchange payload
     */
    private byte[] payload;

    public ECipherSuite[] getCipherSuite() {
        ECipherSuite[] result = null;
        if (this.cipherSuite != null) {
            result = new ECipherSuite[this.cipherSuite.length];
            System.arraycopy(this.cipherSuite, 0, result, 0, result.length);
        }

        return result;
    }

    public void setCipherSuite(final ECipherSuite[] cipherSuite) {
        if (cipherSuite != null) {
            this.cipherSuite = new ECipherSuite[cipherSuite.length];
            System.arraycopy(cipherSuite, 0, this.cipherSuite, 0,
                    this.cipherSuite.length);
        }
        else
        	this.cipherSuite = null;
    }

    /**
     * Get the value of the ChangeCipherSpec payload.
     *
     * @return ChangeCipherSpec payload
     */
    public byte[] getPayload() {
        byte[] result = null;
        if (this.payload != null) {
            result = new byte[this.payload.length];
            System.arraycopy(this.payload, 0, result, 0, result.length);
        }

        return result;
    }

    /**
     * Set the value of the ChangeCipherSpec payload.
     *
     * @param payload ChangeCipherSpec payload
     */
    public void setPayload(final byte[] payload) {
        if (payload != null) {
            this.payload = new byte[payload.length];
            System.arraycopy(payload, 0, this.payload, 0,
                    this.payload.length);
        }
        else
        	this.payload = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String computeHash() {
        MessageDigest sha1 = null;
        try {
            sha1 = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            logger.error("Wrong algorithm.", e);
        }
        updateHash(sha1, getIdentifier().name().getBytes());
        updateHash(sha1, getDescription().getBytes());
        updateHash(sha1, getPayload());
        byte[] hash = sha1.digest();
        String hashValue = Utility.bytesToHex(hash);
        hashValue = hashValue.replace(" ", "");
        return hashValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateHash(final MessageDigest md, final byte[] input) {
        if (input != null) {
            md.update(input);
        }
    }
}
