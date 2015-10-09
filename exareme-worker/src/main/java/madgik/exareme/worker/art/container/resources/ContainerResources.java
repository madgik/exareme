/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.resources;

import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import org.apache.log4j.Logger;

import java.util.HashMap;

/**
 * @author John Chronis<br>
 * @author Vaggelis Nikolopoulos<br>
 */
public class ContainerResources extends Resources {

    private static final Logger log = Logger.getLogger(ContainerResources.class);
    HashMap<ConcreteOperatorID, Resources> resourceMap =
        new HashMap<ConcreteOperatorID, Resources>();

    public ContainerResources(int memory) {
        super(memory);
    }

    public boolean canRun(Resources jobResources) {
        double[] jobResourcesArray = jobResources.getResourceArray();
        synchronized (resourceArray) {
            for (int i = 0; i < ResourceName.values().length; ++i) {
                if (jobResourcesArray[i] > resourceArray[i]) {
                    return false;
                }
            }
        }
        return true;
    }

    //use only after canrun returns true TODO(DSC)
    public void allocateResources(Resources jobResources) {

        double[] jobResourcesArray = jobResources.getResourceArray();
        for (int i = 0; i < ResourceName.values().length; ++i) {
            resourceArray[i] -= jobResourcesArray[i];
        }
    }

    //use only after canrun returns true TODO(DSC)
    public void allocateResources(Resources jobResources, ConcreteOperatorID opID) {
        double[] jobResourcesArray = jobResources.getResourceArray();
        synchronized (resourceArray) {
            for (int i = 0; i < ResourceName.values().length; ++i) {
                resourceArray[i] -= jobResourcesArray[i];
            }
        }
        synchronized (resourceMap) {
            resourceMap.put(opID, jobResources);
        }
    }

    public void freeResources(ConcreteOperatorID opID) {
        Resources res;
        synchronized (resourceMap) {
            res = resourceMap.remove(opID);
        }
        if (res != null) {
            freeResources(res);
        }

    }

    //use only after canrun returns true TODO(DSC)
    private void freeResources(Resources jobResources) {
        double[] jobResourcesArray = jobResources.getResourceArray();
        synchronized (resourceArray) {
            for (int i = 0; i < ResourceName.values().length; ++i) {
                resourceArray[i] += jobResourcesArray[i];
            }
            log.info("freeResources. availmem: " + resourceArray[0]);
        }
    }
}
