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
import java.util.Random;

/**
 * @author heraldkllapi
 */
public class TreeDataflowMixedGenerator implements DataflowGenerator {
    private final Poisson gen;
    private final SLA[] slas;
    private final String[] queryStrings;
    private final Random rand = new Random();
    private long idCount = 0;

    public TreeDataflowMixedGenerator(File[] queries) throws IOException {
        this.gen = new Poisson(TreeConstants.SETTINGS.QUERY_MEAN_ARRIVAL_TIME_SEC, 0);
        this.slas = new SLA[2];
        this.slas[0] = TreeConstants.SETTINGS.SLAS[TreeConstants.SETTINGS.NORMAL_SLA];
        this.slas[1] = TreeConstants.SETTINGS.SLAS[TreeConstants.SETTINGS.HIGH_PRIORITY_SLA];
        this.queryStrings = new String[queries.length];
        for (int i = 0; i < queries.length; ++i) {
            this.queryStrings[i] = FileUtil.readFile(queries[i]);
        }
    }

    @Override
    public TreeQuery getNextDataflow() throws Exception {
        // Wait for the query to arive
        long waitTime = gen.next() * Metrics.MiliSec;
        Thread.sleep(waitTime);
        String queryString = queryStrings[rand.nextInt(queryStrings.length)];
        TreeQuery query = new TreeQuery(idCount, slas[rand.nextInt(slas.length)], queryString);
        idCount++;
        return query;
    }
}
