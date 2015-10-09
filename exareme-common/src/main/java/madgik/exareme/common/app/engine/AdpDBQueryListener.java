/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine;

/**
 * @author herald
 */
public interface AdpDBQueryListener {

    void statusChanged(AdpDBQueryID queryID, AdpDBStatus status);

    void terminated(AdpDBQueryID queryID, AdpDBStatus status);
}
