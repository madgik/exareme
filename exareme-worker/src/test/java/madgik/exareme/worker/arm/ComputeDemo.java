/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.arm;

import madgik.exareme.worker.arm.compute.ArmComputeLocator;
import madgik.exareme.worker.arm.compute.ArmComputeProxy;
import madgik.exareme.worker.arm.compute.ComputeSessionContainerManagerProxy;
import madgik.exareme.worker.arm.compute.session.ActiveContainer;
import madgik.exareme.worker.arm.compute.session.ArmComputeSession;
import madgik.exareme.worker.arm.manager.ArmManager;
import madgik.exareme.worker.arm.manager.ArmManagerFactory;
import madgik.exareme.worker.art.manager.ArtManager;
import madgik.exareme.worker.art.manager.ArtManagerFactory;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

/**
 * @author herald
 */
public class ComputeDemo {

    private static ArtManager manager = null;
    private static ArmManager armManager = null;
    private static Logger log = Logger.getLogger(ComputeDemo.class);

    public static void main(String[] args) throws Exception {

        BasicConfigurator.configure();

        manager = ArtManagerFactory.createRmiArtManager();
        manager.getRegistryManager().startArtRegistry();

        armManager = ArmManagerFactory.createRmiArtManager();
        armManager.getComputeManager().startCompute();

        ArmComputeProxy computeProxy = ArmComputeLocator.getArmComputeProxy();

        for (int i = 0; i < 10; i++) {
            ArmComputeSession computeSession = computeProxy.createSession();

            ComputeSessionContainerManagerProxy containerManagerProxy =
                computeSession.getComputeSessionContainerManagerProxy();

            ActiveContainer ac = containerManagerProxy.getContainers(1)[0];
            log.debug(ac.containerName);
            containerManagerProxy.stopContainer(ac);

            computeSession.close();
        }

        log.debug("ok!");
    }
}
