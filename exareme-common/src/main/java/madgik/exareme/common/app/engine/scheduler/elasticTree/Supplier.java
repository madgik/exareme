package madgik.exareme.common.app.engine.scheduler.elasticTree;

import madgik.exareme.common.optimizer.FinancialProperties;
import madgik.exareme.common.optimizer.RunTimeParameters;

/**
 * @author Konstantinos Tsakalozos <br>
 *         University of Athens / Department of Informatics and Telecommunications.
 * @since 1.0
 */
public abstract class Supplier {
    protected final double MC;
    protected double last_rev;
    protected long last_vms;
    protected double cur_rev;
    protected long cur_vms;
    protected double avg_response_time;

    public Supplier(RunTimeParameters runTime, FinancialProperties fin) {
        MC = fin.timeQuantumCost;
        cur_rev = last_rev = 0.0;
        cur_vms = last_vms = 0;
    }

    public long getSuggestedContainers(double revenue) {
        //    last_vms = lastContainers;
        cur_rev = revenue;
        double MR;
        if (last_vms == cur_vms) {
            // Create instability
            //      MR = MC - 0.00001;
            MR = (last_rev - cur_rev) / (last_vms - cur_vms + 1);
        } else {
            MR = (last_rev - cur_rev) / (last_vms - cur_vms);
        }
        long suggestedVMS = getSuggestedContainersFromRevenue(MR);
        if (suggestedVMS < 1) {
            suggestedVMS = 1;
        }
        last_rev = cur_rev;
        return suggestedVMS;
    }

    public void setCurrentContainers(long currentContainers) {
        last_vms = cur_vms;
        cur_vms = currentContainers;
    }

    abstract protected long getSuggestedContainersFromRevenue(double MR);
}
