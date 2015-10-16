/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.client.compute;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.arm.compute.cluster.ClusterArmComputeInterface;
import madgik.exareme.worker.arm.compute.session.ActiveContainer;

import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * @author Christos
 */
public class Client {

    public static void main(String[] args) throws RemoteException, InterruptedException {

        int i, number_of_usages = 3, number_of_containers = 8;
        int number_of_threads = 2400;
        EntityName entity;
        ArrayList<EntityName> entities = new ArrayList<EntityName>();

        ArrayList<ActiveContainer> containers = new ArrayList<ActiveContainer>();
        for (i = 0; i < number_of_containers; i++) {
            entity = new EntityName("Container" + i, "192.168.2.3");
            entities.add(entity);
            containers.add(new ActiveContainer(i, entity, i));
        }


        ClusterArmComputeInterface manager =
            new ClusterArmComputeInterface(number_of_usages, containers);

        for (i = 0; i < number_of_threads; i++) {
            Thread threads = new Thread(new ClientJob(manager, i, number_of_threads, entities));
            threads.start();
        }


        int number;
        int times = 0;
        while (true) {
            Thread.sleep(8000);
            number = manager.printReport();
            if (number == 0) {
                times++;
            }
            if (times == 3) {
                break;
            }
        }
    }
}
