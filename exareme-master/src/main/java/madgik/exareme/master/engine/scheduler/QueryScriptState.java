/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.scheduler;

/**
 * @author herald
 */
public enum QueryScriptState {
    queued,
    initializing,
    optimizing,
    ready,
    running,
    success,
    error
}
