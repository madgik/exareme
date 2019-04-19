/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.arm.compute;

/**
 * @author herald
 */
public class ArmComputeLocator {

    private static ArmComputeProxy computeProxy = null;

    public static void setArmCompute(ArmComputeProxy compute) {
        ArmComputeLocator.computeProxy = compute;
    }

    public static ArmComputeProxy getArmComputeProxy() {
        return computeProxy;
    }
}
