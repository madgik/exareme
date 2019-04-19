package madgik.exareme.master.engine.iterations.scheduler;

import madgik.exareme.master.engine.iterations.scheduler.events.algorithmCompletion.AlgorithmCompletionEventHandler;
import madgik.exareme.master.engine.iterations.scheduler.events.newAlgorithm.NewAlgorithmEventHandler;
import madgik.exareme.master.engine.iterations.scheduler.events.newAlgorithm.NewAlgorithmEventListener;
import madgik.exareme.master.engine.iterations.scheduler.events.phaseCompletion.PhaseCompletionEventHandler;
import madgik.exareme.master.engine.iterations.scheduler.events.phaseCompletion.PhaseCompletionEventListener;
import madgik.exareme.master.engine.iterations.state.IterationsStateManager;
import madgik.exareme.master.engine.iterations.state.IterationsStateManagerImpl;
import madgik.exareme.utils.eventProcessor.EventProcessor;

/**
 * @author Christos Aslanoglou <br> caslanoglou@di.uoa.gr <br> University of Athens / Department of
 * Informatics and Telecommunications.
 */
class IterationsSchedulerState {
    // Instance fields --------------------------------------------------------------------------
    private final EventProcessor eventProcessor;
    private final IterationsStateManager iterationsStateManager;
    private final IterationsDispatcher iterationsDispatcher;
    // Listeners & Handlers =============================
    private NewAlgorithmEventHandler newAlgorithmEventHandler;
    private NewAlgorithmEventListener newAlgorithmEventListener;
    private PhaseCompletionEventHandler phaseCompletionEventHandler;
    private PhaseCompletionEventListener phaseCompletionEventListener;
    private AlgorithmCompletionEventHandler algorithmCompletionEventHandler;

    IterationsSchedulerState(IterationsScheduler iterationsScheduler) {
        iterationsStateManager = IterationsStateManagerImpl.getInstance();
        iterationsDispatcher = IterationsDispatcher.getInstance(iterationsScheduler);
        eventProcessor = new EventProcessor(1);
        eventProcessor.start();
        createHandlers();
        createListeners();
    }

    private void createHandlers() {
        newAlgorithmEventHandler = new NewAlgorithmEventHandler(
                iterationsStateManager, iterationsDispatcher);
        phaseCompletionEventHandler = new PhaseCompletionEventHandler(
                iterationsStateManager, iterationsDispatcher);
        algorithmCompletionEventHandler = new AlgorithmCompletionEventHandler(
                iterationsStateManager, iterationsDispatcher);
    }

    private void createListeners() {
        newAlgorithmEventListener = new NewAlgorithmEventListener();
        phaseCompletionEventListener = new PhaseCompletionEventListener();
    }

    // Getters ----------------------------------------------------------------------------------
    EventProcessor getEventProcessor() {
        return eventProcessor;
    }

    NewAlgorithmEventHandler getNewAlgorithmEventHandler() {
        return newAlgorithmEventHandler;
    }

    NewAlgorithmEventListener getNewAlgorithmEventListener() {
        return newAlgorithmEventListener;
    }

    PhaseCompletionEventHandler getPhaseCompletionEventHandler() {
        return phaseCompletionEventHandler;
    }

    PhaseCompletionEventListener getPhaseCompletionEventListener() {
        return phaseCompletionEventListener;
    }

    AlgorithmCompletionEventHandler getAlgorithmCompletionEventHandler() {
        return algorithmCompletionEventHandler;
    }
}
