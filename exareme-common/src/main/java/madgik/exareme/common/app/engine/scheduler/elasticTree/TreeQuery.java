/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine.scheduler.elasticTree;

import madgik.exareme.common.app.engine.scheduler.elasticTree.client.SLA;

/**
 * @author heraldkllapi
 */
public class TreeQuery {
    public final long id;
    private final SLA sla;
    private final String query;

    public TreeQuery(long id, SLA sla, String query) {
        this.id = id;
        this.sla = sla;
        this.query = query;
    }

    public SLA getSLA() {
        return sla;
    }

    public String getQuery() {
        return query;
    }
}
