package de.rub.nds.ssl.analyzer.vnl.fingerprint;

import de.rub.nds.ssl.analyzer.vnl.Connection;
import de.rub.nds.ssl.analyzer.vnl.fingerprint.serialization.SerializationException;
import de.rub.nds.ssl.analyzer.vnl.fingerprint.serialization.Serializer;
import de.rub.nds.ssl.stack.Utility;
import de.rub.nds.ssl.stack.protocols.commons.ECipherSuite;
import de.rub.nds.ssl.stack.protocols.commons.ECompressionMethod;
import de.rub.nds.ssl.stack.protocols.commons.EProtocolVersion;
import de.rub.nds.ssl.stack.protocols.commons.Id;
import de.rub.nds.ssl.stack.protocols.handshake.ServerHello;
import de.rub.nds.ssl.stack.protocols.handshake.datatypes.Extensions;
import de.rub.nds.ssl.stack.protocols.handshake.extensions.SupportedPointFormats;
import de.rub.nds.ssl.stack.protocols.handshake.extensions.datatypes.EExtensionType;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;

public class ServerHelloFingerprint extends Fingerprint<ServerHelloFingerprint> {
    private static Logger logger = Logger.getLogger(ServerHelloFingerprint.class);

    protected ServerHelloFingerprint(ServerHelloFingerprint original) {
        super(original);
    }

    private ServerHelloFingerprint() {
        super();
    }

    private ServerHelloFingerprint(Connection connection) {
        ServerHello serverHello = connection.getServerHello();
        if(serverHello == null)
            throw new NotMatchingException();

        addSign("version", serverHello.getProtocolVersion());
        addSign("message-version", serverHello.getMessageProtocolVersion());
        addSign("cipher-suite", serverHello.getCipherSuite());
        addSign("compression-method", serverHello.getCompressionMethod());
        addSign("session-id-empty", serverHello.getSessionID().isEmpty());

        Extensions extensions = serverHello.getExtensions();
        if(extensions == null)
            return;
        addSign("extensions-layout", extensions.getRawExtensionTypes());

        // below are handled specific extensions, if present

        SupportedPointFormats supportedPointFormats =
                extensions.getExtension(EExtensionType.EC_POINT_FORMATS);
        if(supportedPointFormats != null) {
            addSign("supported-point-formats",
                    supportedPointFormats.getRawPointFormats());
        }
    }


    public static ServerHelloFingerprint copy(ServerHelloFingerprint original) {
        return new ServerHelloFingerprint(original);
    }

    @Deprecated
    public static ServerHelloFingerprint deserializeFingerprint(String serialized) {
        return new ServerHelloFingerprint().deserialize(serialized);
    }

    public static ServerHelloFingerprint deserializeFingerprint(List<String> signs) {
        return new ServerHelloFingerprint().deserialize(signs);
    }

    public static ServerHelloFingerprint create(Connection connection) {
        return new ServerHelloFingerprint(connection);
    }

    @Override
    public List<String> serializationSigns() {
        return Arrays.asList(
                "version",
                "message-version",
                "cipher-suite",
                "compression-method",
                "session-id-empty",
                "extensions-layout",
                "supported-point-formats"
        );
    }

    @Override
    public ServerHelloFingerprint deserialize(final List<String> signs) {
        if(signs.size() < 5) {
            throw new IllegalArgumentException("Serialized form of fingerprint invalid: "
                    + "Wrong sign count " + signs.size());
        }

        byte[] bytes;
        bytes = Utility.hexToBytes(signs.get(0).trim());
        addSign("version", EProtocolVersion.getProtocolVersion(bytes));

        bytes = Utility.hexToBytes(signs.get(1).trim());
        addSign("message-version", EProtocolVersion.getProtocolVersion(bytes));

        bytes = Utility.hexToBytes(signs.get(2).trim());
        addSign("cipher-suite", ECipherSuite.getCipherSuite(bytes));

        bytes = Utility.hexToBytes(signs.get(3).trim());
        addSign("compression-method", ECompressionMethod.getCompressionMethod(bytes[0]));

        try {
            addSign("session-id-empty", Serializer.deserializeBoolean(signs.get(4)));
        } catch (SerializationException e) {
            // omit sign
        }

        List<Id> extensionLayout = Serializer.deserializeList(signs.get(5).trim());
        if(extensionLayout != null)
            addSign("extensions-layout", extensionLayout);

        if(signs.size() >= 6) {
            List<Id> supportedPointFormats = Serializer.deserializeList(signs.get(6).trim());
            if (supportedPointFormats != null)
                addSign("supported-point-formats", supportedPointFormats);
        }

        return this;
    }
}
