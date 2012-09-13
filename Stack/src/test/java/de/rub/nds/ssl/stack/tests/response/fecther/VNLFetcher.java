package de.rub.nds.ssl.stack.tests.response.fecther;

import de.rub.nds.ssl.stack.tests.workflows.AWorkflow;
import de.rub.nds.virtualnetworklayer.connection.pcap.FragmentSequence;
import de.rub.nds.virtualnetworklayer.connection.pcap.PcapConnection;
import de.rub.nds.virtualnetworklayer.connection.pcap.PcapTrace;
import de.rub.nds.virtualnetworklayer.socket.VNLSocket;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * Fetches the responses from the socket.
 *
 * @author Christopher Meyer - christopher.meyer@ruhr-uni-bochum.de
 * @version 0.1 Aug 30, 2012
 */
public class VNLFetcher extends AResponseFetcher {

    /**
     * Initialize the fetcher to get responses from the socket and notify the
     * observer.
     *
     * @param so Socket to work with
     * @param workflow Workflow to notify
     */
    public VNLFetcher(final VNLSocket so, final AWorkflow workflow) {
        super(so, workflow);
    }

    /**
     * Run the thread. Fetch bytes from the socket and notify the observer when
     * a complete record is present.
     */
    @Override
    public void run() {
        
Logger logger = Logger.getRootLogger();
        PcapConnection connection = ((VNLSocket) socket).getConnection();
        PcapTrace trace = connection.getTrace();
        List<FragmentSequence> sequences = trace.getFragmentSequences();
        Response response;
        while (continueFetching()) {
logger.debug("============> Will Elvis leave the building?");
            try {
                while (sequences.isEmpty() || !sequences.get(0).isComplete()) {
                    synchronized (connection) {
                        connection.wait();
                    }
                }
            } catch (InterruptedException e) {
                // TODO silently ignore
            }

logger.debug("===========> Elvis has left the building");

            //set changed Flag and notify the observer
            this.setChanged();
            response = new Response(trace);
            this.notifyObservers(response);
            workflow.wakeUp();
        }
    }
}