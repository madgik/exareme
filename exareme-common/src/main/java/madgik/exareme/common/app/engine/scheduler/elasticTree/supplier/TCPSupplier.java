package madgik.exareme.common.app.engine.scheduler.elasticTree.supplier;


import madgik.exareme.common.app.engine.scheduler.elasticTree.Supplier;
import madgik.exareme.common.optimizer.FinancialProperties;
import madgik.exareme.common.optimizer.RunTimeParameters;

/**
 * @author Konstantinos Tsakalozos <br>
 *         University of Athens / Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class TCPSupplier extends Supplier {
    private final int window_threshold = 1000;
    private final int start_exp_move_after = 5;
    private final double exponent = 1.4;
    private final long defaultWindowIncr = 1;

    private long window_size = 1;
    private long window_inc = defaultWindowIncr;
    private int same_direction = 0;
    private boolean going_up = true;

    public TCPSupplier(RunTimeParameters runTime, FinancialProperties fin) {
        super(runTime, fin);
    }

    @Override protected long getSuggestedContainersFromRevenue(double MR) {
        window_size = cur_vms;
        //Decide on slow start or congestion avoidance
        if (MR > MC) {
            if (going_up == false) {
                going_up = true;
                same_direction = 0;
            } else {
                same_direction++;
            }
        } else {
            if (going_up == true) {
                going_up = false;
                same_direction = 0;
            } else {
                same_direction--;
            }
        }

        if (Math.abs(same_direction) > start_exp_move_after) {
            window_inc = (long) Math.ceil((double) window_inc * exponent);
        } else {
            window_inc = defaultWindowIncr;
        }

        if (MR > MC) {
            if (window_size + window_inc > window_threshold) {
                window_size = (long) Math.ceil((double) (window_size + window_inc) / exponent);
                same_direction = 0;
                //        throw new RuntimeException("WINDOW SIZE LIMIT!");
            } else {
                window_size += window_inc;
            }
        } else {
            window_size -= window_inc;
        }
        //    System.out.println("MR: " + MR + "\t" + "MC: " + MC + " -> " + window_size);
        return window_size;
    }
}
