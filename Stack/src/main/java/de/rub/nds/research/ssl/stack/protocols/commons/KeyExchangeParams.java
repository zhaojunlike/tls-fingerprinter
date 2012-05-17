package de.rub.nds.research.ssl.stack.protocols.commons;

import de.rub.nds.research.ssl.stack.protocols.handshake.
datatypes.EKeyExchangeAlgorithm;
import de.rub.nds.research.ssl.stack.protocols.handshake.
datatypes.ESignatureAlgorithm;
import java.security.PublicKey;

/**
 * A singleton to save data like the
 * key exchange algorithm or the public key.
 * @author Eugen Weiss - eugen.weiss@ruhr-uni-bochum.de
 * @version 0.1
 * Apr 26, 2012
 */
public final class KeyExchangeParams {

    /**Instance of KeyExchangeParams.*/
    private static volatile KeyExchangeParams keyExParam = null;
    /**Public key.*/
    private PublicKey pk = null;
    /**Key exchange algorithm - RSA/Diffie-Hellman.*/
    private EKeyExchangeAlgorithm algorithm;
    /**Signature algorithm - RSA/DSA.*/
    private ESignatureAlgorithm sigAlg;
    /**Diffie-Hellman generator.*/
    private byte[] dhGen;
    /**Diffie-Hellman prime modulus.*/
    private byte[] dhPrime;
    /**Diffie-Hellman public value.*/
    private byte[] dhPublic;

    /**
     * Private constructor for singleton.
     */
    private KeyExchangeParams() {
        dhGen = new byte[0];
        dhPrime = new byte[0];
        dhPublic = new byte[0];
    }

    /**
     * Public method to create instance.
     *
     * @return Instance of KeyExchangeParam
     */
    public static KeyExchangeParams getInstance() {
        if (keyExParam == null) {
            keyExParam = new KeyExchangeParams();
        }
        return keyExParam;
    }

    /**
     * Set the PublicKey which was extracted from the Certificate.
     * @param publicKey Public key
     */
    public void setPublicKey(final PublicKey publicKey) {
        this.pk = publicKey;
    }

    /**
     * Get the PublicKey which was extracted from the Certificate.
     * @return PublicKey
     */
    public PublicKey getPublicKey() {
        return this.pk;
    }

    /**
     * Set the key exchange algorithm defined in the cipher suite.
     * @param alg Key exchange algorithm
     */
    public void setKeyExchangeAlgorithm(
            final EKeyExchangeAlgorithm alg) {
        this.algorithm = alg;
    }

    /**
     * Get the key exchange algorithm defined in the cipher suite.
     * @return Key exchange algorithm
     */
    public EKeyExchangeAlgorithm getKeyExchangeAlgorithm() {
        return this.algorithm;
    }

    /**
     * Set the signature algorithm defined in the cipher suite.
     * @param sig Signature algorithm
     */
    public void setSignatureAlgorithm(
            final ESignatureAlgorithm sig) {
        this.sigAlg = sig;
    }

    /**
     * Get the signature algorithm defined in the cipher suite.
     * @return Signature algorithm
     */
    public ESignatureAlgorithm getSignatureAlgorithm() {
        return this.sigAlg;
    }

    /**
     * Get the Diffie-Hellman generator.
     * @return Diffie-Hellman generator
     */
    public byte[] getDHGenerator() {
        return this.dhGen.clone();
    }

    /**
     * Set the Diffie-Hellman generator.
     * @param gen Diffie-Hellman generator
     */
    public void setDHGenerator(final byte[] gen) {
        this.dhGen = gen.clone();
    }

    /**
     * Get the Diffie-Hellman prime modulus.
     * @return Diffie-Hellman prime modulus
     */
    public byte[] getDHPrime() {
        return dhPrime.clone();
    }

    /**
     * Set the Diffie-Hellman prime modulus.
     * @param mod Diffie-Hellman prime modulus
     */
    public void setDHPrime(final byte[] mod) {
        this.dhPrime = mod.clone();
    }

    /**
     * Get the Diffie-Hellman public value.
     * @return Diffie-Hellman public value
     */
    public byte[] getDhPublic() {
        return dhPublic.clone();
    }

    /**
     * Set the Diffie-Hellman public value.
     * @param pub Diffie-Hellman public value
     */
    public void setDhPublic(final byte[] pub) {
        this.dhPublic = pub.clone();
    }
}
