package madgik.exareme.common.app.engine.scheduler.elasticTree.supplier;


import madgik.exareme.common.app.engine.scheduler.elasticTree.Supplier;
import madgik.exareme.common.optimizer.FinancialProperties;
import madgik.exareme.common.optimizer.RunTimeParameters;

/**
 * @author Konstantinos Tsakalozos <br>
 *         University of Athens / Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class LinearSupplier extends Supplier {
    private final int step_vms = 1;
    private final int maxContainers = 1000;

    public LinearSupplier(RunTimeParameters runTime, FinancialProperties fin) {
        super(runTime, fin);
    }

    @Override protected long getSuggestedContainersFromRevenue(double MR) {
        long suggestion = cur_vms + ((MR > MC) ? step_vms : -step_vms);
        return (suggestion > maxContainers) ? maxContainers : suggestion;
    }
}
