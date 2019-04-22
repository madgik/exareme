/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine.scheduler.elasticTree.generator;


import madgik.exareme.common.app.engine.scheduler.elasticTree.TreeQuery;

/**
 * @author heraldkllapi
 */
public interface DataflowGenerator {

    // The thread that call this method is blocked until the next dataflow arrives
    TreeQuery getNextDataflow() throws Exception;
}
