/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine.scheduler.elasticTree.generator;


import madgik.exareme.common.app.engine.scheduler.elasticTree.TreeConstants;
import madgik.exareme.common.app.engine.scheduler.elasticTree.TreeQuery;
import madgik.exareme.common.app.engine.scheduler.elasticTree.client.SLA;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.GlobalTime;
import madgik.exareme.utils.file.FileUtil;
import madgik.exareme.utils.statistics.Poisson;
import madgik.exareme.utils.units.Metrics;

import java.io.File;
import java.io.IOException;

/**
 * @author heraldkllapi
 */
public class TreeDataflowGeneratorPhase implements DataflowGenerator {
    private final Poisson gen1;
    private final Poisson gen2;
    private final String query1;
    private final String query2;
    private final SLA sla1;
    private final SLA sla2;

    // 1 -> change generator's rate
    // 2 -> change SLA
    // 3 -> change query
    private final int experiment;

    private long idCount = 0;

    public TreeDataflowGeneratorPhase(File q1, File q2, int experiment) throws IOException {
        this.gen1 = new Poisson(TreeConstants.SETTINGS.QUERY_MEAN_ARRIVAL_TIME_SEC, 0);
        this.gen2 = new Poisson(0.5 * TreeConstants.SETTINGS.QUERY_MEAN_ARRIVAL_TIME_SEC, 0);
        this.query1 = FileUtil.readFile(q1);
        this.query2 = FileUtil.readFile(q2);
        this.sla1 = TreeConstants.SETTINGS.SLAS[TreeConstants.SETTINGS.NORMAL_SLA];
        this.sla2 = TreeConstants.SETTINGS.SLAS[TreeConstants.SETTINGS.HIGH_PRIORITY_SLA];
        this.experiment = experiment;
    }

    @Override
    public TreeQuery getNextDataflow() throws Exception {
        Poisson gen = gen1;
        SLA sla = sla1;
        String queryString = query1;

        int phase = 0;
        if (GlobalTime.getCurrentSec() < TreeConstants.SETTINGS.PHASE_TRANSITION_TIME) {
            phase = 0;
        } else if (GlobalTime.getCurrentSec() < 2 * TreeConstants.SETTINGS.PHASE_TRANSITION_TIME) {
            phase = 1;
        } else {
            phase = 2;
        }

        // Phase 1 is different
        if (phase == 1) {
            switch (experiment) {
                case 0:
                    gen = gen2;
                    break;
                case 1:
                    sla = sla2;
                    break;
                case 2:
                    queryString = query2;
                    break;
            }
        }

        // Wait for the query to arive
        long waitTime = gen.next() * Metrics.MiliSec;
        Thread.sleep(waitTime);
        TreeQuery query = new TreeQuery(idCount, sla, queryString);
        idCount++;
        return query;
    }
}
