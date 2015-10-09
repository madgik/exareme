package madgik.exareme.master.queryProcessor.graph;

/**
 * @author herald
 */
public class GraphParameters {
    /**
     * The following are used for DAX graphs
     * data: 1000
     * time: 50
     */
    public double multiply_by_data = 1.0;
    public double multiply_by_time = 1.0;
    public int memory = 40;

    /**
     * Generated graphs pipeline operator percentage
     * Default: no pipeline operators
     */
    public double pipeline_percentage = 0.0;

    public GraphParameters() {
    }
}
