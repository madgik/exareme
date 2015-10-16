/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container;

import madgik.exareme.utils.net.NetUtil;
import org.apache.log4j.Logger;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * University of Athens /
 * Department of Informatics and Telecommunications.
 *
 * @since 1.0
 */
public class ContainerClientDemo {

    private static Logger log = Logger.getLogger(ContainerClientDemo.class);

    public static void main(String[] args) throws Exception {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        Registry registry = LocateRegistry.getRegistry();
        String name = NetUtil.getIPv4() + "_container";
        Container container = (Container) registry.lookup(name);
        ContainerStatus cmp = container.getStatus();
        for (int i = 0; i < 100; i++) {
            log.debug(cmp.toString());
        }
    }
}
