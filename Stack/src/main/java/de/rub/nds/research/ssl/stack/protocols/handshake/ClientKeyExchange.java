package de.rub.nds.research.ssl.stack.protocols.handshake;

import de.rub.nds.research.ssl.stack.protocols.handshake.datatypes.EKeyExchangeAlgorithm;
import de.rub.nds.research.ssl.stack.protocols.commons.EProtocolVersion;
import de.rub.nds.research.ssl.stack.protocols.handshake.datatypes.ClientDHPublic;
import de.rub.nds.research.ssl.stack.protocols.handshake.datatypes.EncryptedPreMasterSecret;
import de.rub.nds.research.ssl.stack.protocols.handshake.datatypes.IExchangeKeys;
import de.rub.nds.research.ssl.stack.protocols.handshake.datatypes.PreMasterSecret;

/**
 * Defines the ClientKeyExchange message of SSL/TLS as defined in RFC 2246
 *
 * @author Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1
 *
 * Jan 10, 2011
 */
public final class ClientKeyExchange extends AHandshakeRecord {

    /**
     * Minimum length of the encoded form
     */
    public final static int LENGTH_MINIMUM_ENCODED = 0;
    private IExchangeKeys exchangeKeys;
    private EKeyExchangeAlgorithm keyExchangeAlgorithm = null;
    
    /**
	* Length bytes
	*/
    public final static int LENGTH_BYTES = 2;

    /**
     * Initializes a ClientKeyExchange message as defined in RFC 2246.
     *
     * @param message ClientKeyExchange message in encoded form
     * @param exchangeAlgorithm Key exchange algorithm to be used
     * @param chained Decode single or chained with underlying frames
     */
    public ClientKeyExchange(final byte[] message,
            final EKeyExchangeAlgorithm exchangeAlgorithm,
            final boolean chained) {
        // dummy call - decoding will invoke decoders of the parents if desired
        super();
        this.keyExchangeAlgorithm =
                EKeyExchangeAlgorithm.valueOf(exchangeAlgorithm.name());
        this.decode(message, chained);
    }

    /**
     * Initializes a ClientKeyExchange message as defined in RFC 2246.
     *
     * @param protocolVersion Protocol version of this message
     * @param exchangeAlgorithm Key exchange algorithm to be used
     */
    public ClientKeyExchange(final EProtocolVersion protocolVersion,
            final EKeyExchangeAlgorithm exchangeAlgorithm) {
        super(protocolVersion, EMessageType.CLIENT_KEY_EXCHANGE);
        this.keyExchangeAlgorithm =
                EKeyExchangeAlgorithm.valueOf(exchangeAlgorithm.name());
    }

    /**
     * Set the key exchange algorithm (internal state only).
     *
     * @param algo Key exchange algorithm to be used.
     */
    private void setKeyExchangeAlgorithm(EKeyExchangeAlgorithm algo) {
        if (algo == null) {
            throw new IllegalArgumentException(
                    "Key exchange algorithm MUST NOT be NULL.");
        }

        this.keyExchangeAlgorithm = EKeyExchangeAlgorithm.valueOf(algo.name());
    }

    /**
     * Set the protocol version at the record layer level.
     *
     * @param version Protocol version for the record Layer
     */
    public void setRecordLayerProtocolVersion(final EProtocolVersion version) {
        this.setProtocolVersion(version);
    }

    /**
     * Set the protocol version at the record layer level.
     *
     * @param version Protocol version for the record Layer
     */
    public void setRecordLayerProtocolVersion(final byte[] version) {
        this.setProtocolVersion(version);
    }

    /**
     * Get the exchange keys
     *
     * @return Key exchange keys of this message
     */
    public IExchangeKeys getExchangeKeys() {
        IExchangeKeys keys = null;
        byte[] tmp;

        tmp = this.exchangeKeys.encode(false);
        switch (this.keyExchangeAlgorithm) {
            case DIFFIE_HELLMAN:
                keys = new ClientDHPublic(tmp);
                break;
            case RSA:
//                keys = new PreMasterSecret(tmp);
                keys = new EncryptedPreMasterSecret(tmp);
                break;
            default:
                break;
        }

        return keys;
    }

    /**
     * Set exchange keys of this message.
     *
     * @param keys Exchange keys of this message
     */
    public void setExchangeKeys(IExchangeKeys keys) {
        if (keys == null) {
            throw new IllegalArgumentException("Keys muste not be NULL!");
        }

        setExchangeKeys(keys.encode(false));
    }

    /**
     * Set exchange keys of this message.
     *
     * @param keys Exchange keys of this message
     */
    public void setExchangeKeys(byte[] keys) {
        if (keys == null) {
            throw new IllegalArgumentException("Keys muste not be NULL!");
        }

        byte[] tmp = new byte[keys.length];
        System.arraycopy(keys, 0, tmp, 0, tmp.length);

        switch (this.keyExchangeAlgorithm) {
            case DIFFIE_HELLMAN:
                this.exchangeKeys = new ClientDHPublic(tmp);
                break;
            case RSA:
//            	this.exchangeKeys = new PreMasterSecret(tmp);
//            	RSA needs an encrypted PreMasterSecret
                this.exchangeKeys = new EncryptedPreMasterSecret(tmp);
                break;
            default:
                break;
        }
    }

    /**
     * {@inheritDoc}
     *
     * ClientKeyExchange representation 0 bytes
     */
    @Override
    public byte[] encode(final boolean chained) {
        byte[] encodedExchangeKeys = this.exchangeKeys.encode(false);

        super.setPayload(encodedExchangeKeys);
        return chained ? super.encode(true) : encodedExchangeKeys;
    }

    /**
     * {@inheritDoc}
     */
    public void decode(final byte[] message, final boolean chained) {
        byte[] payloadCopy;

        if (chained) {
            super.decode(message, true);
        } else {
            setPayload(message);
        }

        // payload already deep copied
        payloadCopy = getPayload();

        // check size
        switch (keyExchangeAlgorithm) {
            case DIFFIE_HELLMAN:
                if (payloadCopy.length < ClientDHPublic.LENGTH_MINIMUM_ENCODED) {
                    throw new IllegalArgumentException(
                            "ClientKeyExchange message too short.");
                }
                exchangeKeys = new ClientDHPublic(payloadCopy);
                break;
            case RSA:
                if (payloadCopy.length < PreMasterSecret.LENGTH_MINIMUM_ENCODED) {
                    throw new IllegalArgumentException(
                            "ClientKeyExchange message too short.");
                }
                exchangeKeys = new PreMasterSecret(payloadCopy);
                break;
            default:
                break;
        }

    }
 
}