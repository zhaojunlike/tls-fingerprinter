package de.rub.nds.ssl.analyzer.vnl.fingerprint;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import de.rub.nds.ssl.analyzer.vnl.Connection;
import de.rub.nds.ssl.analyzer.vnl.fingerprint.SignatureDifference.SignDifference;
import de.rub.nds.ssl.analyzer.vnl.fingerprint.SignatureDifference.SignatureIdentifier;
import de.rub.nds.ssl.analyzer.vnl.fingerprint.serialization.Serializer;
import de.rub.nds.virtualnetworklayer.fingerprint.MtuFingerprint;
import de.rub.nds.virtualnetworklayer.fingerprint.TcpFingerprint;
import org.apache.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Collection of Fingerprint signatures identifying a TLS endpoint
 *
 * @author jBiegert azrdev@qrdn.de
 */
public class TLSFingerprint {
    private static final Logger logger = Logger.getLogger(TLSFingerprint.class);

    // static Fingerprint instances for signature creation
    private static TcpFingerprint serverTcpFingerprint = new TcpFingerprint();
    private static MtuFingerprint serverMtuFingerprint = new MtuFingerprint();

    // non-static Signature instances
    private HandshakeFingerprint handshakeSignature;
    private ServerHelloFingerprint serverHelloSignature;
    private de.rub.nds.virtualnetworklayer.fingerprint.Fingerprint.Signature serverTcpSignature;
    private de.rub.nds.virtualnetworklayer.fingerprint.Fingerprint.Signature serverMtuSignature;

    // additional info, only used (i.e. may be true) for connection-generated fps, not deserialized ones
    private boolean _hasRetransmissions = false;
    private boolean _hasIpFragmentation = false;

    public <T extends de.rub.nds.virtualnetworklayer.fingerprint.Fingerprint>
    TLSFingerprint(HandshakeFingerprint handshakeSignature,
                   ServerHelloFingerprint serverHelloSignature,
                   T.Signature serverTcpSignature,
                   T.Signature serverMtuSignature) {
        this.handshakeSignature = handshakeSignature;
        this.serverHelloSignature = serverHelloSignature;
        this.serverTcpSignature = serverTcpSignature;
        this.serverMtuSignature = serverMtuSignature;

        if(serverTcpSignature != null) serverTcpSignature.setFuzzy(true);
        if(serverMtuSignature != null) serverMtuSignature.setFuzzy(true);
    }

    /**
     * initialize all signatures from connection
     */
    public TLSFingerprint(@Nonnull Connection connection) {
        _hasRetransmissions = connection.hasRetransmissions();
        _hasIpFragmentation = connection.hasIPv4Fragmentation();

        try {
            serverHelloSignature = ServerHelloFingerprint.create(connection);
        } catch(RuntimeException e) {
            logger.debug("Error creating ServerHelloFingerprint: " + e);
        }
        handshakeSignature = HandshakeFingerprint.create(connection.getFrameList());

        serverTcpSignature = connection.getServerTcpSignature();
        serverMtuSignature = connection.getServerMtuSignature();
        if(serverTcpSignature != null) serverTcpSignature.setFuzzy(true);
        if(serverMtuSignature != null) serverMtuSignature.setFuzzy(true);
    }

    public HandshakeFingerprint getHandshakeSignature() {
        return handshakeSignature;
    }

    public ServerHelloFingerprint getServerHelloSignature() {
        return serverHelloSignature;
    }

    public de.rub.nds.virtualnetworklayer.fingerprint.Fingerprint.Signature getServerTcpSignature() {
        return serverTcpSignature;
    }

    public de.rub.nds.virtualnetworklayer.fingerprint.Fingerprint.Signature getServerMtuSignature() {
        return serverMtuSignature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || !(o instanceof TLSFingerprint))
            return false;

        TLSFingerprint that = (TLSFingerprint) o;

        if (handshakeSignature != null?
                !handshakeSignature.equals(that.handshakeSignature) :
                that.handshakeSignature != null)
            return false;
        if (serverHelloSignature != null ?
                !serverHelloSignature.equals(that.serverHelloSignature) :
                that.serverHelloSignature != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 0;
        result = prime * result +
                (handshakeSignature != null ? handshakeSignature.hashCode() : 0);
        result = prime * result +
                (serverHelloSignature != null ? serverHelloSignature.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TLSFingerprint: {");
        sb.append("\nHandshake: {\n").append(handshakeSignature).append("\n}");
        sb.append("\nServer Hello: {\n").append(serverHelloSignature).append("\n}");
        sb.append("\nServer TCP: {\n").append(serverTcpSignature).append("\n}");
        sb.append("\nServer MTU: {\n").append(serverMtuSignature).append("\n}");
        sb.append("\n").append(additionalInfo());
        sb.append("\n}");
        return sb.toString();
    }

    public @Nonnull String additionalInfo() {
        return Joiner.on("\n").skipNulls().join(
                _hasRetransmissions? "some TCP packets were retransmitted" : null,
                _hasIpFragmentation? "some Ipv4 packets were fragmented" : null);
    }

    /**
     * @return a String representation of everything that has changed w.r.t. other
     * @param otherName text to represent other
     */
    public String differenceString(TLSFingerprint other, String otherName) {
        return getClass().getSimpleName() + " difference to " + otherName + ":\n" +
                Joiner.on("\n").join(difference(other));
    }

    /**
     * @return Set describing all the changed signs w.r.t. <code>other</code>
     */
    public Set<SignDifference> difference(TLSFingerprint other) {
        final Set<SignDifference> differences = Sets.newHashSet();

        differences.addAll(SignatureDifference.fromGenericFingerprints(
                SignatureIdentifier.create("Handshake"),
                handshakeSignature,
                other.getHandshakeSignature()));

        differences.addAll(SignatureDifference.fromGenericFingerprints(
                SignatureIdentifier.create("ServerHello"),
                serverHelloSignature,
                other.getServerHelloSignature()));

        differences.addAll(SignatureDifference.fromVnlFingerprints(
                SignatureIdentifier.create("ServerTCP"),
                serverTcpSignature,
                other.getServerTcpSignature()));

        differences.addAll(SignatureDifference.fromVnlFingerprints(
                SignatureIdentifier.create("ServerMTU"),
                serverMtuSignature,
                other.getServerMtuSignature()));

        return ImmutableSet.copyOf(differences);
    }

    public String serialize() {
        return Serializer.serialize(this);
    }

    /**
     * Additional info, if generated from connection (and not deserialized)
     * @return If the connection contained TCP retransmissions
     */
    public boolean hasRetransmissions() {
        return _hasRetransmissions;
    }

    /**
     * Additional info, if generated from connection (and not deserialized)
     * @return If the connection contained fragmented IPv4 packets
     */
    public boolean hasIpFragmentation() {
        return _hasIpFragmentation;
    }
}