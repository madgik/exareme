/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.concreteOperator;

import madgik.exareme.common.art.entity.DataSourceEntity;
import madgik.exareme.worker.art.concreteOperator.manager.AdaptorManager;

/**
 * This operator represents a source operator.
 * All source operators must extend this class.
 *
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 *         {herald,paparas,evas}@di.uoa.gr<br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public abstract class AbstractNiSo extends AbstractOperatorImpl {

    private String initialization = null;
    private DataSourceEntity datasource = null;

    /**
     * Get the initialization.
     *
     * @return the initialization.
     */
    protected String getInitialization() {
        return this.initialization;
    }

    /**
     * Set the initialization.
     * <p/>
     * <pre>
     * Example:
     *
     * sql.query { ... } {
     *      queryString // -> Initialization
     * };
     *
     * </pre>
     *
     * @param initialization The operator initialization.
     */
    public void setInitialization(String initialization) {
        if (this.initialization == null) {
            this.initialization = initialization;
        }
    }

    protected DataSourceEntity getDatasource() {
        return this.datasource;
    }

    public void setDatasource(DataSourceEntity ds) {
        this.datasource = ds;
    }

    @Override protected AdaptorManager createAdaptorManager() {
        return new AdaptorManager(1, 0, this.getSessionManager());
    }
}
