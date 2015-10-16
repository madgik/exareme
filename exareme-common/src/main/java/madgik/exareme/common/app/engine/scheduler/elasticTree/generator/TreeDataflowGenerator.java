/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine.scheduler.elasticTree.generator;


import madgik.exareme.common.app.engine.scheduler.elasticTree.TreeConstants;
import madgik.exareme.common.app.engine.scheduler.elasticTree.TreeQuery;
import madgik.exareme.common.app.engine.scheduler.elasticTree.client.SLA;
import madgik.exareme.utils.file.FileUtil;
import madgik.exareme.utils.statistics.Poisson;
import madgik.exareme.utils.units.Metrics;

import java.io.File;
import java.io.IOException;

/**
 * @author heraldkllapi
 */
public class TreeDataflowGenerator implements DataflowGenerator {
    private final Poisson gen;
    private final SLA sla;
    private final String queryString;
    private long idCount = 0;

    public TreeDataflowGenerator(File query) throws IOException {
        this.gen = new Poisson(TreeConstants.SETTINGS.QUERY_MEAN_ARRIVAL_TIME_SEC, 0);
        this.sla = TreeConstants.SETTINGS.SLAS[TreeConstants.SETTINGS.NORMAL_SLA];
        this.queryString = FileUtil.readFile(query);
    }

    @Override public TreeQuery getNextDataflow() throws Exception {
        // Wait for the query to arive
        long waitTime = gen.next() * Metrics.MiliSec;
        Thread.sleep(waitTime);
        TreeQuery query = new TreeQuery(idCount, sla, queryString);
        idCount++;
        return query;
    }
}
