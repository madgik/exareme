/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.estimator;

import madgik.exareme.master.queryProcessor.decomposer.dag.Node;

/**
 * @author dimitris
 */
public interface SelectivityEstimator {

    public void makeEstimationForNode(Node n);

}
