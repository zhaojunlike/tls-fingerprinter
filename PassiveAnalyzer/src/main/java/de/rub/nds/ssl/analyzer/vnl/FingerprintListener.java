package de.rub.nds.ssl.analyzer.vnl;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import de.rub.nds.ssl.analyzer.vnl.fingerprint.TLSFingerprint;
import de.rub.nds.ssl.analyzer.vnl.fingerprint.serialization.Serializer;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class FingerprintListener {
    private static Logger logger = Logger.getLogger(TLSFingerprint.class);

    //TODO: get back insertion-order, store it in TLSFingerprint & use SortedSetMultimap (TreeMultimap) here
    private SetMultimap<SessionIdentifier, TLSFingerprint> fingerprints =
            HashMultimap.create();
    /**
     * {@link FingerprintReporter}s to notify about reported fingerprints
     */
    private final Collection<FingerprintReporter> reporters = new LinkedList<>();

    public boolean addFingerprintReporter(FingerprintReporter fr) {
        synchronized (reporters) {
            return reporters.add(fr);
        }
    }

    public boolean removeFingerprintReporter(FingerprintReporter fr) {
        synchronized (reporters) {
            return reporters.remove(fr);
        }
    }

    public void clearFingerprintReporters() {
        synchronized (reporters) {
            reporters.clear();
            logger.info("Cleared all fingerprint reporters");
        }
    }

    /**
     * Insert a given fingerprint into the database, bypassing normal "report"
     * notifications. Use when guessing fingerprints.
     * @return False if the tlsFingerprint was already known, true otherwise.
     * @see FingerprintReporter#reportArtificial(SessionIdentifier, TLSFingerprint)
     */
    boolean insertFingerprint(SessionIdentifier sessionId, TLSFingerprint tlsFingerprint) {
        if (fingerprints.containsEntry(sessionId, tlsFingerprint)) {
            return false;
        }
        fingerprints.put(sessionId, tlsFingerprint);
        reportFingerprintArtificial(sessionId, tlsFingerprint);
        return true;
    }

    public void reportConnection(SessionIdentifier sessionIdentifier,
            TLSFingerprint tlsFingerprint) {
        if(fingerprints.containsEntry(sessionIdentifier, tlsFingerprint)) {
            // We have seen this!
            reportFingerprintUpdate(sessionIdentifier, tlsFingerprint);
            //TODO: store seen count
        }
        else if(fingerprints.containsKey(sessionIdentifier)) {
            // A new different fingerprint for this SessionIdentifier

            final Set<TLSFingerprint> previousFingerprints =
                    ImmutableSet.copyOf(fingerprints.get(sessionIdentifier));
            //TODO: make configurable if changed fingerprints should be added to store
            fingerprints.put(sessionIdentifier, tlsFingerprint);
            reportFingerprintChange(sessionIdentifier, tlsFingerprint, previousFingerprints);
        }
        else {
            // the SessionIdentifier is not yet in fingerprints, add it
            fingerprints.put(sessionIdentifier, tlsFingerprint);
            reportFingerprintNew(sessionIdentifier, tlsFingerprint);
        }
    }

    /**
     * first occurrence of sessionIdentifier, with accompanying tlsFingerprint
     */
    private void reportFingerprintNew(SessionIdentifier sessionIdentifier,
            TLSFingerprint tlsFingerprint) {
        synchronized (reporters) {
            for (FingerprintReporter fingerprintReporter : reporters) {
                fingerprintReporter.reportNew(sessionIdentifier, tlsFingerprint);
            }
        }
    }

    /**
     * sessionIdentifier + tlsFingerprint have already been seen in this combination
     */
    private void reportFingerprintUpdate(SessionIdentifier sessionIdentifier,
            TLSFingerprint tlsFingerprint) {
        synchronized (reporters) {
            for (FingerprintReporter fingerprintReporter : reporters) {
                fingerprintReporter.reportUpdate(sessionIdentifier, tlsFingerprint);
            }
        }
    }

    /**
     * we know a different tlsFingerprint for this sessionIdentifier ! Might be MITM!
     */
    private void reportFingerprintChange(SessionIdentifier sessionIdentifier,
            TLSFingerprint tlsFingerprint,
            Set<TLSFingerprint> previousFingerprints) {
        synchronized (reporters) {
            for (FingerprintReporter fingerprintReporter : reporters) {
                fingerprintReporter.reportChange(sessionIdentifier,
                        tlsFingerprint,
                        previousFingerprints);
            }
        }
    }
    /**
     * TLSFingerprint [and possibly sessionIdentifier] was generated by code
     */
    private void reportFingerprintArtificial(SessionIdentifier sessionIdentifier,
            TLSFingerprint tlsFingerprint) {
        synchronized (reporters) {
            for (FingerprintReporter fingerprintReporter : reporters) {
                fingerprintReporter.reportArtificial(sessionIdentifier, tlsFingerprint);
            }
        }
    }

    public String toString() {
        return fingerprints.size() + " known endpoints; " +
                reporters.size() + " attached reporters";
    }

    /**
     * read all fingerprints in saveFile to the internal store.
     * <p>
     * @param overrideExisting Clear the currently known fingerprints before loading
     * @throws IOException
     */
    public void loadFingerprintSaveFile(Path saveFile, boolean overrideExisting)
            throws IOException {
        logger.info("loading from " + saveFile);

        BufferedReader br = Files.newBufferedReader(saveFile, Charset.forName("UTF8"));
        SetMultimap<SessionIdentifier, TLSFingerprint> fingerprints =
                Serializer.deserialize(br);

        if (overrideExisting) {
            logger.info("clearing previously stored fingerprints");
            this.fingerprints = fingerprints;
        } else {
            for (Map.Entry<SessionIdentifier, TLSFingerprint> e : fingerprints.entries()) {
                if (! this.fingerprints.put(e.getKey(), e.getValue())) {
                    logger.warn("fingerprint in file already known: " + e.getKey());
                    logger.trace("fingerprint: " + e.getValue());
                }
            }
        }

        logger.info("load fingerprints done. " +
                "New # of known endpoints: " + fingerprints.size());
    }

    /** @return An unmodifiable view of the stored fingerprints. It is reflecting all
     * subsequent changes to these. */
    public SetMultimap<SessionIdentifier, TLSFingerprint> getFingerprints() {
        return Multimaps.unmodifiableSetMultimap(fingerprints);
    }
}
