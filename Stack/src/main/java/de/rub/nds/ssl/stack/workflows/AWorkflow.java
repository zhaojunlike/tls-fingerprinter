package de.rub.nds.ssl.stack.workflows;

import de.rub.nds.ssl.stack.workflows.commons.WorkflowState;
import de.rub.nds.ssl.stack.workflows.commons.ObservableBridge;
import de.rub.nds.ssl.stack.protocols.ARecordFrame;
import de.rub.nds.ssl.stack.trace.MessageTrace;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observer;

/**
 * Interface to signal a workflow based object.
 *
 * @author Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1 Apr 11, 2012
 */
public abstract class AWorkflow implements Observer {

    /**
     * Current state identifier.
     */
    private int currentState = 0;
    /**
     * Observable bridges for each state.
     */
    private ObservableBridge[] states;
    /**
     * Response fetcher Thread.
     */
    private Thread respFetchThread;
    /**
     * Main thread.
     */
    private Thread mainThread;
    /**
     * Message trace of this workflow.
     */
    private ArrayList<MessageTrace> traceList = new ArrayList<MessageTrace>();
    /**
     * Synchronized trace message trace.
     */
    List<MessageTrace> syncTraceList = Collections.synchronizedList(traceList);
    
    /**
     * Public constructor for a workflow. Assigns an observable bridge to each
     * state for observation reasons.
     *
     * @param workflowStates States of this workflow
     */
    public AWorkflow(final WorkflowState[] workflowStates) {
        if (workflowStates != null) {
            states = new ObservableBridge[workflowStates.length];
            for (int i = workflowStates.length - 1; i >= 0; i--) {
                states[i] = new ObservableBridge(workflowStates[i]);
            }
        }
    }

    /**
     * Start the workflow.
     */
    public abstract void start();

    /**
     * Add an observer for a specific workflow state.
     *
     * @param observer Observer to be registered
     * @param state State for which to register
     */
    public void addObserver(final Observer observer, final WorkflowState state) {
        states[state.getID()].addObserver(observer);
    }

    /**
     * Delete an observer for a specific workflow state.
     *
     * @param observer Observer to be unregistered
     * @param state State for which to unregister
     */
    public void deleteObserver(final Observer observer,
            final WorkflowState state) {
        states[state.getID()].deleteObserver(observer);
    }

    /**
     * Delete all observers for a specific workflow state.
     *
     * @param state State for which to unregister
     */
    public void deleteObservers(final WorkflowState state) {
        states[state.getID()].deleteObservers();
    }

    /**
     * Counts observers registered for a specific state.
     *
     * @param state State for which to count
     * @return Number of observers for this specific state
     */
    public int countObservers(final WorkflowState state) {
        return states[state.getID()].countObservers();
    }

    /**
     * Tests if the changed flag of this state is set.
     *
     * @param state State to test for changes
     * @return True if the changed flag if set for this state.
     */
    public boolean hasChanged(final WorkflowState state) {
        return states[state.getID()].hasChanged();
    }

    /**
     * Notify changes to the the observers and deliver the trace object.
     *
     * @param trace Message trace
     * @param state State for which this notification is valid
     */
    public void notifyObservers(final MessageTrace trace,
            final WorkflowState state) {
        states[state.getID()].notifyObservers(trace);
    }

    /**
     * Notify changes to the the observers of the current state and deliver the
     * trace object.
     *
     * @param trace Message trace
     */
    public void notifyCurrentObservers(final MessageTrace trace) {
        states[currentState].notifyObservers(trace);
    }

    /**
     * Switches to the next state and notifies the observers. Utilizes
     * nextState().
     *
     * @param trace Holds the tracing data
     */
    public void nextStateAndNotify(final MessageTrace trace) {
        nextState();
        notifyCurrentObservers(trace);
    }

    /**
     * Switches to the next state. If the last state is reached the workflow
     * will remain in this last state. A call will automatically set the changed
     * flag of the returned state.
     *
     * @return New current state
     */
    public WorkflowState nextState() {
        this.currentState++;

        // sanity check
        if (this.currentState > this.states.length) {
            this.currentState = this.states.length - 1;
        }


        ObservableBridge newState = this.states[this.currentState];
        newState.setChangedFlag();

        return newState.getState();
    }

    /**
     * Switches to the next state and notifies the observers. Utilizes
     * previousState().
     *
     * @param trace Holds the tracing data
     */
    public void previousStateAndNotify(final MessageTrace trace) {
        previousState();
        notifyCurrentObservers(trace);
    }

    /**
     * Switches to the previous state. If the first state is reached the
     * workflow will remain in this first state. A call will automatically set
     * the changed flag of the returned state.
     *
     * @return New current state
     */
    public WorkflowState previousState() {
        this.currentState--;

        // sanity check
        if (this.currentState < 0) {
            this.currentState = 0;
        }

        ObservableBridge newState = this.states[this.currentState];
        newState.setChangedFlag();

        return newState.getState();
    }

    /**
     * Sets a new state and notifies the observers.
     *
     * @param trace Holds the tracing data
     * @param state The new state
     */
    public void switchToState(final MessageTrace trace,
            final WorkflowState state) {
        setCurrentState(state.getID());
        notifyCurrentObservers(trace);
    }

    /**
     * Get the current state in the handshake.
     *
     * @return Current handshake state
     */
    public int getCurrentState() {
        return this.currentState;
    }

    /**
     * Set the current state in the handshake.
     *
     * @param state Current handshake state
     */
    public void setCurrentState(int state) {
        this.currentState = state;
        ObservableBridge newState = this.states[this.currentState];
        newState.setChangedFlag();
    }

    /**
     * Resets the internal state machine to its initial state.
     */
    protected void resetState() {
        this.currentState = 0;
    }

    /**
     * Get the Thread of the handshake workflow.
     *
     * @return Workflow thread.
     */
    public void wakeUp() {
        this.mainThread.interrupt();
    }

    /**
     * Set the main Thread.
     *
     * @param thread Main Thread
     */
    public void setMainThread(Thread thread) {
        this.mainThread = thread;
    }

    /**
     * Get the main Thread.
     *
     * @return Main thread
     */
    public Thread getMainThread() {
        return this.mainThread;
    }

    /**
     * Set the response Thread.
     *
     * @param thread Response Thread
     */
    public void setResponseThread(Thread thread) {
        this.respFetchThread = thread;
    }

    /**
     * Get the response Thread.
     *
     * @return Response thread
     */
    public Thread getResponseThread() {
        return this.respFetchThread;
    }
    
    /**
     * Sets the current record of a trace and saves the previous one if 
     * present.
     *
     * @param trace MessageTrace to be modified
     * @param record New record to be set
     */
    public void setRecordTrace(final MessageTrace trace,
            final ARecordFrame record) {
        // save the old state
        ARecordFrame oldRecord = trace.getOldRecord();
        trace.setOldRecord(oldRecord);

        //add the newly created message to the trace syncTraceList
        trace.setCurrentRecord(record);
    }

    /**
     * Add a new MessageTrace object to the ArrayList.
     *
     * @param trace MessageTrace object to be added
     */
    public synchronized void addToTraceList(final MessageTrace trace) {
        syncTraceList.add(trace);
    }

    /**
     * Get the trace syncTraceList of the whole handshake.
     *
     * @return MessageTrace syncTraceList
     */
    public ArrayList<MessageTrace> getTraceList() {
        return (ArrayList<MessageTrace>) traceList.clone();
    }
}
