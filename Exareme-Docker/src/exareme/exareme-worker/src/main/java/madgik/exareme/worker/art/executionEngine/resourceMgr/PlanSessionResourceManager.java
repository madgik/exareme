/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.resourceMgr;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.active.Resources;
import org.apache.log4j.Logger;

import java.util.HashMap;

/**
 * @author heraldkllapi
 */
public class PlanSessionResourceManager {
    private static final Logger log = Logger.getLogger(PlanSessionResourceManager.class);
    private HashMap<String, Resources> containerResources = new HashMap<String, Resources>();

    public PlanSessionResourceManager() {
    }

    public Resources getAvailableResources(EntityName name) {
        Resources r = containerResources.get(name.getName());
        if (r == null) {
            r = new Resources();
            containerResources.put(name.getName(), r);
        }
        return r;
    }

    public void printUsage(String msg) {
        StringBuilder sb = new StringBuilder();
        sb.append(msg).append(": ");
        for (Resources r : containerResources.values()) {
            sb.append(r.toString()).append(" ");
        }
        log.debug(sb.toString());
    }

    //  public void reset() {
    //    for (Map.Entry<String, Resources> e : containerResources.entrySet()) {
    //      e.getValue().reset();
    //    }
    //  }
}
