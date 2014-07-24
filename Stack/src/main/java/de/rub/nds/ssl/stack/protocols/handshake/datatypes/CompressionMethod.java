package de.rub.nds.ssl.stack.protocols.handshake.datatypes;

import de.rub.nds.ssl.stack.Utility;
import de.rub.nds.ssl.stack.protocols.commons.APubliclySerializable;
import de.rub.nds.ssl.stack.protocols.commons.ECompressionMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Compression method message part - as defined in RFC-2246.
 *
 * @author Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1
 *
 * Nov 15, 2011
 */
//TODO: create interface + classes for concrete compression methods, instead of using byte[]
public final class CompressionMethod extends APubliclySerializable {

    /**
     * Length of the length field.
     */
    private static final int LENGTH_LENGTH_FIELD = 1;
    /**
     * Minimum length of the encoded form.
     */
    public static final int LENGTH_MINIMUM_ENCODED = LENGTH_LENGTH_FIELD;
    /**
     * Compression methods.
     */
    private List<ECompressionMethod> methods = new ArrayList<>();

    @Override
    public String toString() {
        return methods.toString();
    }

    /**
     * Initializes as defined in RFC 2246. By default contain only ECompressionMethod.NULL
     */
    public CompressionMethod() {
        methods.add(ECompressionMethod.NULL);
    }

    /**
     * Initializes with the given methods
     */
    public CompressionMethod(List<ECompressionMethod> methods) {
        this.methods = new ArrayList<>(methods);
    }

    /**
     * Initializes a compression method object as defined in RFC-2246.
     *
     * @param message Compression method in encoded form
     */
    public CompressionMethod(final byte[] message) {
        this.decode(message, false);
    }

    /**
     * Get the compression method of this message.
     *
     * @return The compression method of this message
     */
    public ECompressionMethod[] getMethods() {
        return methods.toArray(new ECompressionMethod[0]);
    }

    /*
     * @return The compression method of this message
     */
    public List<ECompressionMethod> getCompressionMethods() {
        return methods;
    }

    /**
     * Set the compression methods of this message.
     *
     * @param methods The compression methods to be used for this message
     */
    public final void setMethods(final ECompressionMethod[] methods) {
        if (methods == null) {
            throw new IllegalArgumentException("Compression methods must not be null!");
        }

        this.methods = Utility.deepCopyAsList(methods);
    }

    /**
     * Set the compression methods of this message.
     *
     * @param methods The compression methods to be used for this message
     */
    public final void setMethods(final List<ECompressionMethod> methods) {
        this.methods = new ArrayList<>(methods);
    }

    /**
     * {@inheritDoc}
     *
     * Method parameter will be ignored - no support for chained encoding.
     */
    @Override
    public byte[] encode(final boolean chained) {
        byte[] tmp = new byte[LENGTH_LENGTH_FIELD + methods.size()];
        int index = 0;
        tmp[0] = ((Integer) methods.size()).byteValue();

        // since ECompressionMethod is not encoded as array, don't do encode() or such
        for(ECompressionMethod method : methods) {
            ++index;
            tmp[index] = method.getId();
        }

        return tmp;
    }

    /**
     * {@inheritDoc}
     *
     * Method parameter will be ignored - no support for chained decoding.
     */
    public void decode(final byte[] message, final boolean chained) {
        final int methodsLength;
        List<ECompressionMethod> newMethods;

        // deep copy
        final byte[] methods = new byte[message.length];
        System.arraycopy(message, 0, methods, 0, methods.length);

        // check size
        if (methods.length < LENGTH_MINIMUM_ENCODED) {
            throw new IllegalArgumentException("Compression methods record too short.");
        }

        methodsLength = extractLength(methods, 0, LENGTH_LENGTH_FIELD);

        newMethods = new ArrayList<>();
        for(int i = LENGTH_LENGTH_FIELD; i < methods.length; ++i) {
            ECompressionMethod method = null;
            try {
                method = ECompressionMethod.getCompressionMethod(methods[i]);
            } catch(IllegalArgumentException e) {
                System.out.println(e);
            }
            newMethods.add(method);
        }
        setMethods(newMethods);
    }
}
