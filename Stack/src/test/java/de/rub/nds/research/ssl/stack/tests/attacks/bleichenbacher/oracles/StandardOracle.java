package de.rub.nds.research.ssl.stack.tests.attacks.bleichenbacher.oracles;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Standard Bleichenbacher oracle.
 *
 * @author Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1
 *
 * May 18, 2012
 */
public class StandardOracle implements IOracle {

    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;
    private Cipher cipher;
    private long numberOfQueries;

    public StandardOracle(final PrivateKey privKey, final PublicKey pubKey) throws
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException {
        this.privateKey = (RSAPrivateKey) privKey;
        this.publicKey = (RSAPublicKey) pubKey;
        cipher = Cipher.getInstance("RSA/None/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, this.privateKey);
    }

    public byte[] encrypt(byte[] toEncrypt) {
        byte[] result = new byte[0];

        try {
            cipher = Cipher.getInstance("RSA/None/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, this.publicKey);
            result = cipher.doFinal(toEncrypt);
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        } catch (NoSuchPaddingException ex) {
            ex.printStackTrace();
        } catch (InvalidKeyException ex) {
            ex.printStackTrace();
        } catch (BadPaddingException ex) {
            ex.printStackTrace();
        } catch (IllegalBlockSizeException ex) {
            ex.printStackTrace();
        }

        return result;
    }
    
    @Override
    public long getNumberOfQueries() {
        return numberOfQueries;
    }
    
    public byte[] decrypt(final byte[] msg) {
        byte[] result = new byte[0];
        try {
            cipher = Cipher.getInstance("RSA/None/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, this.privateKey);
            result = cipher.doFinal(msg);
            
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        } catch (NoSuchPaddingException ex) {
            ex.printStackTrace();
        } catch (InvalidKeyException ex) {
            ex.printStackTrace();
        } catch (IllegalBlockSizeException ex) {
            ex.printStackTrace();
        } catch (BadPaddingException ex) {
            // only valid fail!
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException ex) {
            ex.printStackTrace();
        }
        return result;

    }

    @Override
    public boolean checkPKCSConformity(final byte[] msg) {
        boolean result = false;
        numberOfQueries++;

        // fresh init
        try {
            cipher = Cipher.getInstance("RSA/None/PKCS1Padding");
//            cipher = Cipher.getInstance("RSA/None/NoPadding");
            
            cipher.init(Cipher.DECRYPT_MODE, this.privateKey);
            byte[] toCheck = cipher.doFinal(msg);
            
//            if(toCheck[0] == 0x02 && toCheck.length == (cipher.getBlockSize()-1)) {
                result = true;
//            }
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
            result = false;
        } catch (NoSuchPaddingException ex) {
            ex.printStackTrace();
            result = false;
        } catch (InvalidKeyException ex) {
            ex.printStackTrace();
            result = false;
        } catch (IllegalBlockSizeException ex) {
            ex.printStackTrace();
            result = false;
        } catch (BadPaddingException ex) {
            // only valid fail!
            result = false;
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            result = false;
        } catch (ArrayIndexOutOfBoundsException ex) {
            ex.printStackTrace();
            result = false;
        }

        return result;
    }

    @Override
    public int getBlockSize() {
        return cipher.getBlockSize();
    }

    @Override
    public PublicKey getPublicKey() {
        return this.publicKey;
    }
}
