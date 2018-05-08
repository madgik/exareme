/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.client;

import madgik.exareme.common.app.engine.scheduler.elasticTree.client.SLA;
import madgik.exareme.worker.art.container.ContainerProxy;

import java.io.Serializable;

/**
 * Read only client properties.
 *
 * @author alex
 */
public class AdpDBClientProperties implements Serializable {

    // client
    private String database;
    private String username;
    private String password;

    // engine hints
    private boolean tree;
    private boolean useHistory;
    private boolean validate;

    private int maxNumberOfContainers;
    private ContainerProxy[] containerProxies = null;
    private int statisticsUpdateSEC;

    private SLA sla;


    public AdpDBClientProperties(String database, String username, String password, boolean tree,
        boolean useHistory, boolean validate, int maxNumberOfContainers, int statisticsUpdateSEC,
        SLA sla) {
        this.database = database;
        this.username = username;
        this.password = password;

        this.tree = tree;
        this.useHistory = useHistory;
        this.validate = validate;

        this.maxNumberOfContainers = maxNumberOfContainers;
        this.statisticsUpdateSEC = statisticsUpdateSEC;

        this.sla = sla;
    }

    public AdpDBClientProperties(String database) {
        this(database, "", "", false, false, true, -1, 10, null);
    }

    public AdpDBClientProperties(String database, String username, String password) {
        this(database, username, password, false, false, true, -1, 10, null);
    }

    public AdpDBClientProperties(String database, String username, String password,
        boolean useHistory, boolean validate, int maxNumberOfContainers, int statisticsUpdateSEC) {
        this(database, username, password, false, useHistory, validate, maxNumberOfContainers,
            statisticsUpdateSEC, null);
    }

    public String getDatabase() {
        return database;
    }

    public String getUsername() {
        return username;
    }

    public boolean isTreeEnabled() {
        return tree;
    }

    public boolean isHistoryUsed() {
        return useHistory;
    }

    public boolean isValidationEnabled() {
        return validate;
    }

    public int getMaxNumberOfContainers() {
        return maxNumberOfContainers;
    }

    public int getStatisticsUpdateSEC() {
        return statisticsUpdateSEC;
    }

    public SLA getSLA() {
        return this.sla;
    }

    public ContainerProxy[] getContainerProxies() { return containerProxies; }

    public void setContainerProxies(ContainerProxy[] containerProxies) { this.containerProxies = containerProxies; }

}
