package de.rub.nds.ssl.analyzer.vnl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import de.rub.nds.ssl.analyzer.vnl.fingerprint.ClientHelloFingerprint;
import de.rub.nds.ssl.analyzer.vnl.fingerprint.HandshakeFingerprint;
import de.rub.nds.ssl.analyzer.vnl.fingerprint.ServerHelloFingerprint;
import de.rub.nds.ssl.analyzer.vnl.fingerprint.TLSFingerprint;
import de.rub.nds.ssl.stack.protocols.commons.EContentType;
import de.rub.nds.ssl.stack.protocols.commons.EProtocolVersion;
import de.rub.nds.ssl.stack.protocols.commons.Id;
import de.rub.nds.ssl.stack.protocols.handshake.datatypes.EMessageType;
import de.rub.nds.ssl.stack.protocols.handshake.extensions.datatypes.EExtensionType;
import de.rub.nds.virtualnetworklayer.fingerprint.Fingerprint;
import org.apache.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Set;

import static de.rub.nds.ssl.analyzer.vnl.FingerprintReporter.*;

/**
 * Listens for fingerprints of "normal" handshakes and guesses how the fingerprints of
 * the corresponding session resumption(s) may look like, mimicking normal browser &
 * server behaviour. Can inject the guess back to the {@link FingerprintListener}, so
 * they become part of the "already seen" fingerprints.
 *
 * @author jBiegert azrdev@qrdn.de
 */
public class ResumptionFingerprintGuesser extends FingerprintReporterAdapter {
    private static final Logger logger = Logger.getLogger(ResumptionFingerprintGuesser.class);

    private FingerprintListener listener;

    /**
     * @param listener Used to inject guessed fingerprints. Pass null to disable injection.
     */
    public ResumptionFingerprintGuesser(FingerprintListener listener) {
        this.listener = listener;
    }

    @Override
    public void reportNew(SessionIdentifier sessionIdentifier, TLSFingerprint tlsFingerprint) {
        if(tlsFingerprint instanceof GuessedResumptionFingerprint) {
            //shouldn't happen
            logger.warn("guessed fingerprint seen in reportNew(). sessionId: " +
                    sessionIdentifier);
            return;
        }

        if(listener == null)
            return;

        final GuessedSessionIdentifier guessedSessionIdentifier =
                GuessedSessionIdentifier.create(sessionIdentifier,
                        tlsFingerprint.getServerHelloSignature());
        for (TLSFingerprint fingerprint :
                GuessedResumptionFingerprint.create(tlsFingerprint)) {
            logger.debug("now reporting guessed fingerprint");
            listener.insertFingerprint(guessedSessionIdentifier, fingerprint);
        }
    }

    public static class GuessedSessionIdentifier extends SessionIdentifier {
        private GuessedSessionIdentifier(String serverHostName,
                                         ClientHelloFingerprint clientHelloSignature) {
            super(serverHostName, clientHelloSignature);
        }

        public static GuessedSessionIdentifier create(
                @Nonnull SessionIdentifier original,
                ServerHelloFingerprint originalServerHelloSignature) {
            final ClientHelloFingerprint chf = GuessedClientHelloFingerprint.create(
                    original.getClientHelloSignature(),
                    originalServerHelloSignature);
            return new GuessedSessionIdentifier(original.getServerHostName(), chf);
        }
    }

    public static class GuessedClientHelloFingerprint extends ClientHelloFingerprint {
        private static final Id pointFormats = new Id(EExtensionType.EC_POINT_FORMATS.getId());

        private GuessedClientHelloFingerprint(ClientHelloFingerprint original,
                                              ServerHelloFingerprint originalServerHello) {
            super(original); //copy

            // overwrite signs
            boolean originalHandshakeHasTLS1_2 = false;

            final Object messageVersion = originalServerHello.getSign("message-version");
            if(Objects.equals(messageVersion, EProtocolVersion.TLS_1_2) ||
                    Objects.equals(messageVersion, EProtocolVersion.TLS_1_3))
                originalHandshakeHasTLS1_2 = true;

            if(! originalHandshakeHasTLS1_2)
                signs.remove("supported-point-formats");

            // assemble extensions-layout
            try {
                final List<Id> origExtensions = getSign("extensions-layout");
                final List<Id> extensions = new ArrayList<>(origExtensions);
                if(! originalHandshakeHasTLS1_2)
                    extensions.remove(pointFormats);
                signs.put("extensions-layout", extensions);
            } catch (ClassCastException|NullPointerException e) {
                logger.debug("Could not properly guess extensions-layout: " + e);
                signs.put("extensions-layout", Collections.emptyList());
            }
        }

        public static GuessedClientHelloFingerprint create(
                ClientHelloFingerprint original, ServerHelloFingerprint originalServerHello) {
            return new GuessedClientHelloFingerprint(original, originalServerHello);
        }
    }

    public static class GuessedResumptionFingerprint extends TLSFingerprint {

        private GuessedResumptionFingerprint(
                @Nullable HandshakeFingerprint handshake,
                @Nullable ServerHelloFingerprint serverHello,
                @Nullable Fingerprint.Signature serverTcp,
                @Nullable Fingerprint.Signature serverMtu) {
            super(handshake, serverHello, serverTcp, serverMtu);
        }

        public static List<TLSFingerprint> create(@Nonnull TLSFingerprint original) {

            GuessedHandshakeFingerprint handshakeFingerprint = null;
            if(original.getHandshakeSignature() != null)
                handshakeFingerprint =
                        GuessedHandshakeFingerprint.create(original.getHandshakeSignature());

            List<GuessedServerHelloFingerprint> serverHelloFingerprints = new LinkedList<>();
            if (original.getServerHelloSignature() != null) {
                serverHelloFingerprints.add(
                        GuessedServerHelloFingerprint.create(
                                original.getServerHelloSignature()
                        ));
                serverHelloFingerprints.add(
                        GuessedServerHelloFingerprint.createIncludingExtensions(
                                original.getServerHelloSignature()
                        ));
            }

            LinkedList<TLSFingerprint> guesses = new LinkedList<>();
            for (GuessedServerHelloFingerprint serverHelloFingerprint : serverHelloFingerprints) {
                guesses.add(new GuessedResumptionFingerprint(
                        handshakeFingerprint,
                        serverHelloFingerprint,
                        original.getServerTcpSignature(),
                        original.getServerMtuSignature()));
            }
            return guesses;
        }
    }

    public static class GuessedHandshakeFingerprint extends HandshakeFingerprint {
        /** the "message-types" sign content to be assumed for every resumption */
        private static final List<MessageTypes> MESSAGE_TYPES =
                ImmutableList.<MessageTypes>of(
                    new MessageTypeSubtype(new Id(EContentType.HANDSHAKE.getId()),
                            new Id(EMessageType.CLIENT_HELLO.getId())),
                    new MessageTypeSubtype(new Id(EContentType.HANDSHAKE.getId()),
                            new Id(EMessageType.SERVER_HELLO.getId())),
                    new MessageType(new Id(EContentType.CHANGE_CIPHER_SPEC.getId())),
                    new MessageType(new Id(EContentType.CHANGE_CIPHER_SPEC.getId()))
                );
        /** the "ssl-fragment-layout" sign content to be assumed for every resumption */
        private static final List<String> FRAGMENT_LAYOUT = Arrays.asList("0", "1");

        private GuessedHandshakeFingerprint(@Nonnull HandshakeFingerprint original) {
            super(original); // copy

            // overwrite signs
            signs.put("message-types", MESSAGE_TYPES);
            signs.put("ssl-fragment-layout", FRAGMENT_LAYOUT);
            signs.put("session-ids-match", true);
        }

        public static GuessedHandshakeFingerprint create(@Nonnull HandshakeFingerprint original) {
            return new GuessedHandshakeFingerprint(original);
        }
    }

    public static class GuessedServerHelloFingerprint extends ServerHelloFingerprint {
        private static final Id reneg = new Id(EExtensionType.RENEGOTIATION_INFO.getId());
        private static final Set<Id> KEEP_EXTENSIONS = Sets.newHashSet(
                new Id(EExtensionType.STATUS_REQUEST.getId()),
                new Id(EExtensionType.SERVER_NAME.getId()));

        /**
         * guess a {@link ServerHelloFingerprint}
         * @param includeExtensions Whether to include some of the extensions from
         *                          original into the guess
         */
        private GuessedServerHelloFingerprint(@Nonnull ServerHelloFingerprint original,
                                              boolean includeExtensions) {
            super(original); // copy

            // overwrite signs
            signs.put("session-id-empty", false);
            signs.remove("supported-point-formats");

            // assemble extensions-layout
            try {
                final List<Id> origExtensions = getSign("extensions-layout");

                final List<Id> newExtensionsLayout = new ArrayList<>(origExtensions);
                for(ListIterator<Id> it = newExtensionsLayout.listIterator(); it.hasNext(); ) {
                    final Id extension = it.next();
                    if(reneg.equals(extension))
                        continue; //keep
                    if(includeExtensions && KEEP_EXTENSIONS.contains(extension))
                        continue; //keep
                    it.remove(); // don't keep
                }

                signs.put("extensions-layout", newExtensionsLayout);
            } catch (ClassCastException|NullPointerException e) {
                logger.debug("Could not properly guess extensions-layout: " + e);
                signs.put("extensions-layout", Collections.emptyList());
            }
        }

        public static GuessedServerHelloFingerprint create(
                @Nonnull ServerHelloFingerprint original) {
            return new GuessedServerHelloFingerprint(original, false);
        }

        public static GuessedServerHelloFingerprint createIncludingExtensions(
                @Nonnull ServerHelloFingerprint original) {
            return new GuessedServerHelloFingerprint(original, true);
        }
    }
}
