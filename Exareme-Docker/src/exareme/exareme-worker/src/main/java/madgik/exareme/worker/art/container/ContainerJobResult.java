/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container;

import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * TODO(DS): rename to future
 *
 * @author heraldkllapi
 */
public abstract class ContainerJobResult implements Serializable {
    private RemoteException exception = null;
    private long execTime = 0;

    public ContainerJobResult() {
    }

    public void setException(RemoteException e) {
        this.exception = e;
    }

    public boolean hasException() {
        return exception != null;
    }

    public abstract ContainerJobType getType();
}
