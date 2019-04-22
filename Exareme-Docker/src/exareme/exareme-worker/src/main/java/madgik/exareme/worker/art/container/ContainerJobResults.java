/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container;

import madgik.exareme.utils.collections.ReadOnlyViewList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author heraldkllapi
 */
public class ContainerJobResults implements Serializable {
    private ReadOnlyViewList<ContainerJobResult> results = null;

    public ContainerJobResults() {
        results = new ReadOnlyViewList<>(new ArrayList<ContainerJobResult>());
    }

    public void addJobResult(ContainerJobResult result) {
        results.getList().add(result);
    }

    public List<ContainerJobResult> getJobResults() {
        return results.getReadOnlyView();
    }
}
