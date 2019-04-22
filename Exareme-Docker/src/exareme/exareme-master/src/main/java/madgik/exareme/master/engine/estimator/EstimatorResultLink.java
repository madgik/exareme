/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.estimator;

import madgik.exareme.master.queryProcessor.graph.Link;

/**
 * @author heraldkllapi
 */
public class EstimatorResultLink extends EstimatorResult {

    public final Link link;

    public EstimatorResultLink(String name, Link link, boolean foundInHistory) {
        super(name, foundInHistory);
        this.link = link;
    }
}
