/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine;

import madgik.exareme.common.art.PlanSessionStatistics;
import madgik.exareme.utils.properties.AdpDBProperties;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Map;

/**
 * The Adp DB query statistics.
 *
 * @author Herald Kllapi <br>
 *         herald@di.uoa.gr /
 *         University of Athens
 * @since 1.0
 */
public class AdpDBStatistics implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DecimalFormat percentage = new DecimalFormat("#.##");
    private final Map<String, String> categoryMessageMap;
    private int operatorsCompleted = 0;
    private int totalOperators = 0;
    private int dataTransferCompleted = 0;
    private int totalDataTransfers = 0;
    private int errors = 0;
    private PlanSessionStatistics adpEngineStatistics = null;
    private int bucketsNum = AdpDBProperties.getAdpDBProps().getInt("db.client.statisticsBuckets");

    public AdpDBStatistics(Map<String, String> categoryMessageMap) {
        this.categoryMessageMap = categoryMessageMap;
    }

    public int getOperatorsCompleted() {
        return operatorsCompleted;
    }

    public void setOperatorsCompleted(int operatorsCompleted) {
        this.operatorsCompleted = operatorsCompleted;
    }

    public int getTotalOperators() {
        return totalOperators;
    }

    public void setTotalOperators(int totalOperators) {
        this.totalOperators = totalOperators;
    }

    public int getDataTransferCompleted() {
        return dataTransferCompleted;
    }

    public void setDataTransferCompleted(int dataTransferCompleted) {
        this.dataTransferCompleted = dataTransferCompleted;
    }

    public int getTotalDataTransfer() {
        return totalDataTransfers;
    }

    public void setTotalDataTransfers(int totalDataTransfers) {
        this.totalDataTransfers = totalDataTransfers;
    }

    public int getErrors() {
        return errors;
    }

    public void setErrors(int errors) {
        this.errors = errors;
    }

    public double getPercentageCreated(String table) {
        return 0.0;
    }

    public PlanSessionStatistics getAdpEngineStatistics() {
        return adpEngineStatistics;
    }

    public void setAdpEngineStatistics(PlanSessionStatistics adpEngineStatistics) {
        this.adpEngineStatistics = adpEngineStatistics;
    }

    //    public String getAdpEngineStringStatistics() {
    //        return ConsoleFactory.getDefaultStatisticsFormat()
    //            .format(adpEngineStatistics, graph, RmiAdpDBSelectScheduler.runTimeParams);
    //    }
    //
    //    public void exportAdpEngineStatistics(QueryScript script, SchedulingResult schedulingResult,
    //        File folder) throws Exception {
    //        ConsoleFactory.getHTMLStatisticsFormat()
    //            .format(schedulingResult, adpEngineStatistics, categoryMessageMap, folder, bucketsNum);
    //    }

    public Map<String, String> getCategoryMessageMap() {
        return categoryMessageMap;
    }

    public int getBucketsNum() {
        return bucketsNum;
    }

    @Override public String toString() {
        StringBuffer stats = new StringBuffer();
        if (totalOperators == 0) {
            return "Initializing ... ";
        }
        stats.append(
            "Proc: " + percentage.format(100.0 * operatorsCompleted / totalOperators) + " % ");
        if (totalDataTransfers == 0)
            stats.append("Data 0% ");
        else
            stats.append(
                "Data: " + percentage.format(100.0 * dataTransferCompleted / totalDataTransfers)
                    + " % ");
        stats.append("Errors: " + errors);
        return stats.toString();
    }

    //    public void exportAdpEngineStatistics(QueryScript script, SchedulingResult schedulingResult) {
    //
    //    }
}
