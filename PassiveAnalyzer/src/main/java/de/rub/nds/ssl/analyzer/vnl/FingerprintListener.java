package de.rub.nds.ssl.analyzer.vnl;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.SortedSetMultimap;
import de.rub.nds.ssl.analyzer.vnl.fingerprint.TLSFingerprint;
import de.rub.nds.ssl.analyzer.vnl.fingerprint.serialization.Serializer;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FingerprintListener {
    private static Logger logger = Logger.getLogger(TLSFingerprint.class);

    //TODO: get back insertion-order, store it in TLSFingerprint & use SortedSetMultimap (TreeMultimap) here
    private SetMultimap<SessionIdentifier, TLSFingerprint> fingerprints =
            HashMultimap.create();
    /**
     * {@link FingerprintReporter}s to notify about reported fingerprints
     */
    private final Collection<FingerprintReporter> reporters = new LinkedList<>();

    //statistics / counts
    private int fingerprintsNew;
    private int fingerprintsUpdates;
    private int fingerprintsChanges;
    private int fingerprintsArtificial;

    public FingerprintListener() {
        this.fingerprintsChanges = 0;
    }

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
     */
    boolean insertFingerprint(SessionIdentifier sessionId, TLSFingerprint tlsFingerprint) {
        if (fingerprints.containsEntry(sessionId, tlsFingerprint)) {
            return false;
        }
        reportFingerprintArtificial(sessionId, tlsFingerprint);
        fingerprints.put(sessionId, tlsFingerprint);
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
            reportFingerprintChange(sessionIdentifier, tlsFingerprint,
                    fingerprints.get(sessionIdentifier));
            fingerprints.put(sessionIdentifier, tlsFingerprint);
        }
        else {
            // the SessionIdentifier is not yet in fingerprints, add it
            reportFingerprintNew(sessionIdentifier, tlsFingerprint);
            fingerprints.put(sessionIdentifier, tlsFingerprint);
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
            ++fingerprintsNew;
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
            ++fingerprintsUpdates;
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
            ++fingerprintsChanges;
        }
    }
    /**
     * tlsFingerprint [and possibly sessionIdentifier] was generated by code
     */
    private void reportFingerprintArtificial(SessionIdentifier sessionIdentifier,
            TLSFingerprint tlsFingerprint) {
        synchronized (reporters) {
            for (FingerprintReporter fingerprintReporter : reporters) {
                fingerprintReporter.reportArtificial(sessionIdentifier, tlsFingerprint);
            }
            ++fingerprintsArtificial;
        }
    }

    public String toString() {
        return String.format(
                "Endpoints: %d; Fingerprints: New %d, Updates %d, Changes %d, Generated %d",
                fingerprints.size(),
                fingerprintsNew,
                fingerprintsUpdates,
                fingerprintsChanges,
                fingerprintsArtificial);
        //TODO: detailed statistics, here or (completely) elsewhere (in reporter?)
    }

    /**
     * read all fingerprints in saveFile to the internal store.
     * <p>
     * <b>NOTE</b>: currently this overrides everything already in the internal store
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
    }

    public ImmutableSetMultimap<SessionIdentifier, TLSFingerprint> getFingerprints() {
        return ImmutableSetMultimap.copyOf(fingerprints);
    }
}
