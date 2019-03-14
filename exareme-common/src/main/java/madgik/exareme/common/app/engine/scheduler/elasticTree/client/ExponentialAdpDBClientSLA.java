package madgik.exareme.common.app.engine.scheduler.elasticTree.client;


import madgik.exareme.common.app.client.AdpDBClientSLA;

/**
 * Herald
 */
public class ExponentialAdpDBClientSLA implements AdpDBClientSLA {
    private final int id;
    private final double alpha;
    private final double gamma;

    public ExponentialAdpDBClientSLA(int id, double alpha, double gamma) {
        this.id = id;
        this.alpha = alpha;
        this.gamma = gamma;
    }

    public double getAlpha() {
        return alpha;
    }

    public double getGamma() {
        return gamma;
    }

    @Override
    public double getBudget(double time) {
        return alpha * Math.pow(Math.E, -time / gamma);
    }

    @Override
    public int getId() {
        return id;
    }
}
