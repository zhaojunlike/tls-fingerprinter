package de.rub.nds.research.ssl.stack.protocols.handshake;

import de.rub.nds.research.ssl.stack.protocols.commons.ECipherSuite;
import de.rub.nds.research.ssl.stack.protocols.commons.EProtocolVersion;
import de.rub.nds.research.ssl.stack.protocols.handshake.datatypes.CompressionMethod;
import de.rub.nds.research.ssl.stack.protocols.handshake.datatypes.RandomValue;
import de.rub.nds.research.ssl.stack.protocols.handshake.datatypes.SessionId;

/**
 * Defines the ServerHello message of SSL/TLS as defined in RFC 2246
 * 
 * @author  Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1
 *
 * Nov 15, 2011
 */
public final class ServerHello extends AHandshakeRecord {

    /**
     * Minimum length of the encoded form
     */
    public final static int LENGTH_MINIMUM_ENCODED =
            EProtocolVersion.LENGTH_ENCODED
            + RandomValue.LENGTH_ENCODED
            + SessionId.LENGTH_MINIMUM_ENCODED
            + ECipherSuite.LENGTH_ENCODED
            + CompressionMethod.LENGTH_MINIMUM_ENCODED;
    private EProtocolVersion msgProtocolVersion = getProtocolVersion();
    private RandomValue random = new RandomValue();
    private ECipherSuite cipherSuite =
            ECipherSuite.TLS_DH_RSA_WITH_3DES_EDE_CBC_SHA;
    private SessionId sessionID = new SessionId();
    private CompressionMethod compressionMethod = new CompressionMethod();

    /**
     * Initializes a ServerHello message as defined in RFC 2246.
     * 
     * @param protocolVersion Protocol version of this message
     */
    public ServerHello(final EProtocolVersion protocolVersion) {
        super(protocolVersion, EMessageType.SERVER_HELLO);
    }

    /**
     * Initializes a ServerHello message as defined in RFC 2246.
     * 
     * @param message ServerHello message in encoded form
     * @param chained Decode single or chained with underlying frames
     */
    public ServerHello(final byte[] message, final boolean chained) {
        // dummy call - decoding will invoke decoders of the parents if desired
        super();
        this.decode(message, chained);
    }

    /**
     * Get the protocol version of this message. This can be but must not be 
     * equal to the one of the record layer.
     * 
     * @return The protocol version of this message
     */
    public EProtocolVersion getMessageProtocolVersion() {
        // deep copy
        return EProtocolVersion.valueOf(this.msgProtocolVersion.name());
    }

    /**
     * Set the protocol version of this message.
     * 
     * @param protocolVersion The protocol version to be used for this message
     */
    public void setMessageProtocolVersion(
            final EProtocolVersion protocolVersion) {
        if (protocolVersion == null) {
            throw new IllegalArgumentException(
                    "Protocol version must not be null!");
        }

        this.msgProtocolVersion = EProtocolVersion.valueOf(
                protocolVersion.name());
    }

    /**
     * Set the protocol version of this message.
     * 
     * @param protocolVersion The protocol version object to be used for this 
     * message in encoded form
     */
    public void setMessageProtocolVersion(final byte[] protocolVersion) {
        this.msgProtocolVersion = EProtocolVersion.getProtocolVersion(
                protocolVersion);
    }

    /**
     * Get the random of this message. 
     * 
     * @return The random of this message
     */
    public RandomValue getRandom() {
        // deep copy
        return new RandomValue(random.encode(false));
    }

    /**
     * Set the random of this message.
     * 
     * @param randomValue The random value object of this message
     */
    public void setRandom(final RandomValue randomValue) {
        if (randomValue == null) {
            throw new 
                    IllegalArgumentException("Random value must not be null!");
        }
        // deep copy
        this.random = new RandomValue(randomValue.encode(false));
    }

    /**
     * Set the random of this message.
     * 
     * @param randomValue The random value object of this message in encoded 
     * form
     */
    public void setRandom(final byte[] randomValue) {
        if (randomValue == null) {
            throw new 
                    IllegalArgumentException("Random value must not be null!");
        }
        // deep copy
        this.random = new RandomValue(randomValue);
    }

    /**
     * Get the session ID of this message.
     * 
     * @return The session ID of this message
     */
    public SessionId getSessionID() {
        // deep copy
        return new SessionId(sessionID.encode(false));
    }

    /**
     * Set the session ID of this message.
     * 
     * @param sessionID  The session id object to be used for this message in 
     * encoded form
     */
    public void setSessionID(final byte[] sessionID) {
        if (sessionID == null) {
            throw new IllegalArgumentException("Session ID must not be null!");
        }
        // deep copy
        this.sessionID = new SessionId(sessionID);
    }

    /**
     * Set the session ID of this message.
     * 
     * @param sessionID  The session id to be used for this message
     */
    public void setSessionID(final SessionId sessionID) {
        if (sessionID == null) {
            throw new IllegalArgumentException("Session ID must not be null!");
        }

        // deep copy
        this.sessionID = new SessionId(sessionID.encode(false));
    }

    /**
     * Get the compression method of this message. 
     * 
     * @return The compression method of this message
     */
    public byte[] getCompressionMethod() {
        return compressionMethod.getMethods();
    }

    /**
     * Set the compression method of this message.
     * 
     * @param compressionMethod  The compression method object to be used for 
     * this message in encoded form
     */
    public void setCompressionMethod(final byte[] compressionMethod) {
        this.compressionMethod.setMethods(compressionMethod);
    }

    /**
     * Set the compression method of this message.
     * 
     * @param compressionMethod  The compression method to be used for this 
     * message
     */
    public void setCompressionMethod(final CompressionMethod compressionMethod) {
        if (compressionMethod == null) {
            throw new IllegalArgumentException(
                    "Compression method must not be null!");
        }

        // deep copy
        this.compressionMethod = new CompressionMethod(
                compressionMethod.encode(false));
    }

    /**
     * Set the protocol version at the record layer level. This will NOT change
     * the protocol version of this message.
     * 
     * @param version Protocol version for the record Layer
     */
    public void setRecordLayerProtocolVersion(final EProtocolVersion version) {
        this.setProtocolVersion(version);
    }

    /**
     * Set the protocol version at the record layer level. This will NOT change
     * the protocol version of this message.
     * 
     * @param version Protocol version for the record Layer
     */
    public void setRecordLayerProtocolVersion(final byte[] version) {
        this.setProtocolVersion(version);
    }

    /**
     * {@inheritDoc}
     * 
     * ServerHello representation
     *      2 bytes Protocol version
     *     32 bytes Random value
     *  1 + x bytes Session id
     *      2 bytes Cipher suites
     *  1 + x bytes Compression method
     */
    @Override
    public byte[] encode(final boolean chained) {
        int pointer = 0;
        byte[] tmp;
        byte[] encSessionID = sessionID.encode(false);
        byte[] encCompressionMethod = compressionMethod.encode(false);

        // putting the pieces together
        byte[] serverHelloMsg = new byte[EProtocolVersion.LENGTH_ENCODED
                + RandomValue.LENGTH_ENCODED
                + encSessionID.length
                + ECipherSuite.LENGTH_ENCODED
                + encCompressionMethod.length];

        /*
         * Prepre ServerHello message
         */
        // 1. add protocol version
        tmp = this.getMessageProtocolVersion().getId();
        System.arraycopy(tmp, 0, serverHelloMsg, pointer, tmp.length);
        pointer += tmp.length;

        // 2. add random part
        tmp = random.encode(false);
        System.arraycopy(tmp, 0, serverHelloMsg, pointer, tmp.length);
        pointer += tmp.length;

        // 3. add session id
        System.arraycopy(encSessionID, 0, serverHelloMsg, pointer,
                encSessionID.length);
        pointer += encSessionID.length;

        // 4. add cipher suite
        System.arraycopy(cipherSuite.getId(), 0, serverHelloMsg, pointer,
                ECipherSuite.LENGTH_ENCODED);
        pointer += ECipherSuite.LENGTH_ENCODED;

        // 5. add compression method
        System.arraycopy(encCompressionMethod, 0, serverHelloMsg, pointer,
                encCompressionMethod.length);

        super.setPayload(serverHelloMsg);
        return chained ? super.encode(true) : serverHelloMsg;
    }

    /**
     * {@inheritDoc}
     */
    public void decode(final byte[] message, final boolean chained) {
        byte[] tmpBytes;
        byte[] payloadCopy;
        int pointer;
        int extractedLength;

        if(chained) {
            super.decode(message, true);
        } else {
            setPayload(message);
        }
        
        // payload already deep copied
        payloadCopy = getPayload();

        // check size
        if (payloadCopy.length < LENGTH_MINIMUM_ENCODED) {
            throw new IllegalArgumentException("ServerHello message too short.");
        }

        pointer = 0;
        // 1. extract protocol version
        tmpBytes = new byte[EProtocolVersion.LENGTH_ENCODED];
        System.arraycopy(payloadCopy, pointer, tmpBytes, 0, tmpBytes.length);
        setProtocolVersion(tmpBytes);
        pointer += tmpBytes.length;
        
        // 2. extract random value
        tmpBytes = new byte[RandomValue.LENGTH_ENCODED];
        System.arraycopy(payloadCopy, pointer, tmpBytes, 0, tmpBytes.length);
        setRandom(tmpBytes);
        pointer += tmpBytes.length;

        // 3. extract session id
        extractedLength = SessionId.LENGTH_MINIMUM_ENCODED
                + extractLength(payloadCopy, pointer,
                SessionId.LENGTH_MINIMUM_ENCODED);
        if (pointer + extractedLength > payloadCopy.length) {
            throw new IllegalArgumentException("Session id length invalid.");
        }
        tmpBytes = new byte[extractedLength];
        System.arraycopy(payloadCopy, pointer, tmpBytes, 0, tmpBytes.length);
        setSessionID(tmpBytes);
        pointer += tmpBytes.length;

        // 4. extract cipher suite 
        tmpBytes = new byte[ECipherSuite.LENGTH_ENCODED];
        System.arraycopy(payloadCopy, pointer, tmpBytes, 0, tmpBytes.length);
        setCipherSuite(tmpBytes);
        pointer += tmpBytes.length;

        // 5. extract compression method
        extractedLength = CompressionMethod.LENGTH_MINIMUM_ENCODED
                + extractLength(payloadCopy, pointer,
                CompressionMethod.LENGTH_MINIMUM_ENCODED);
        if (pointer + extractedLength > payloadCopy.length) {
            throw new IllegalArgumentException(
                    "Compression method length invalid.");
        }
        tmpBytes = new byte[extractedLength];
        System.arraycopy(payloadCopy, pointer, tmpBytes, 0, tmpBytes.length);
        setCompressionMethod(tmpBytes);

        // ignore additional fields if any
    }

    /**
     * Get the cipher suite of this message.
     * 
     * @return The cipher suite of this message
     */
    public ECipherSuite getCipherSuite() {
        return ECipherSuite.valueOf(cipherSuite.name());
    }

    /**
     * Set the cipher suite of this message.
     * 
     * @param suite The cipher suite to be used for this message
     */
    public void setCipherSuite(final ECipherSuite suite) {
        this.cipherSuite = ECipherSuite.valueOf(suite.name());
    }

    /**
     * Set the cipher suite of this message.
     * 
     * @param suite The cipher suite to be used for this message
     */
    public void setCipherSuite(final byte[] suite) {
        this.cipherSuite = ECipherSuite.getCipherSuite(suite);
    }
}