/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.worker.arm.compute.trial;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.arm.compute.containerMgr.ContainerManagerStatus;
import madgik.exareme.worker.arm.compute.session.ActiveContainer;
import madgik.exareme.worker.arm.compute.session.ArmComputeSessionID;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Χρήστος
 */
public class DummyContainerManager implements ContainerManagerInterface {

    private static Logger log = Logger.getLogger(DummyContainerManager.class);
    private final Object get_lock = new Object();
    private final Object release_lock = new Object();
    boolean get = false;
    boolean souper_get = false;
    private int total_number_of_containers;
    private int number_of_available_containers;
    private int number_of_released_containers = 0;
    private int expected_number_of_containers = 0;
    private List<ActiveContainer> available_containers;
    private List<ActiveContainer> reserved_containers;
    private HashMap<ArmComputeSessionID, LinkedList<ActiveContainer>> releasedContainers;
    private Thread sleeping_thread = null;
    private ReentrantLock wait_lock = new ReentrantLock();
    private final Condition wait_condition = wait_lock.newCondition();

    public DummyContainerManager(int number_of_containers) throws RemoteException {
        int i;
        EntityName entity;

        if (number_of_containers <= 0) {
            throw new RemoteException(exceptionMessage("CreateInvalid"));
        }

        available_containers = new LinkedList<ActiveContainer>();
        reserved_containers = new LinkedList<ActiveContainer>();
        releasedContainers = new HashMap<ArmComputeSessionID, LinkedList<ActiveContainer>>();
        this.total_number_of_containers = number_of_containers;
        this.number_of_available_containers = number_of_containers;

        for (i = 0; i < number_of_containers; i++) {
            entity = new EntityName("Container" + i, "192.168.2.3");
            available_containers.add(new ActiveContainer(i, entity, i));
        }
    }

    public int getTotalNumberOfContainers() {
        return this.total_number_of_containers;
    }

    public int getNumberOfAvailableContainers() {
        return this.number_of_available_containers;
    }

    @Override
    public void startManager() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void stopManager() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ActiveContainer[] getContainers(int number_of_containers, ArmComputeSessionID sessionID)
            throws RemoteException {
        int i;
        LinkedList<ActiveContainer> personal_containers;
        synchronized (get_lock) {

            ActiveContainer[] containers = new ActiveContainer[number_of_containers];

            if (number_of_containers > total_number_of_containers) {
                containers = null;
                throw new RemoteException(exceptionMessage("ManyCont"));
            } else if (number_of_containers <= 0) {
                containers = null;
                throw new RemoteException(exceptionMessage("InvalidCont"));
            } else {
                log.debug("Requested " + number_of_containers + " containers");
            }

            synchronized (this) {
                if (number_of_containers > number_of_available_containers) {
                    expected_number_of_containers = number_of_containers;
                }
            }

            wait_lock.lock();
            get = true;
            try {
                while (number_of_containers > number_of_available_containers) {
                    wait_condition.await();
                }
            } catch (InterruptedException ex) {
                containers = null;
                return null;
            } finally {
                wait_lock.unlock();
            }


            synchronized (this) {
                number_of_released_containers += number_of_containers;
                number_of_available_containers -= number_of_containers;
                personal_containers = releasedContainers.get(sessionID);
                if (personal_containers == null) {
                    releasedContainers.put(sessionID, new LinkedList<ActiveContainer>());
                }

                for (i = 0; i < number_of_containers; i++) {
                    containers[i] = available_containers.get(0);
                    reserved_containers.add(available_containers.get(0));
                    releasedContainers.get(sessionID).add(available_containers.get(0));
                    available_containers.remove(0);
                }
            }

            log.debug("The client acquired " + number_of_containers + " containers");
            return containers;
        }
    }

    @Override
    public ActiveContainer[] getContainers(int number_of_containers, ArmComputeSessionID sessionID,
                                           long duration_time) throws RemoteException {

        int i;
        LinkedList<ActiveContainer> personal_containers;
        synchronized (get_lock) {

            ActiveContainer[] containers = new ActiveContainer[number_of_containers];

            if (number_of_containers > total_number_of_containers) {
                containers = null;
                throw new RemoteException(exceptionMessage("ManyCont"));
            } else if (number_of_containers <= 0) {
                containers = null;
                throw new RemoteException(exceptionMessage("InvalidCont"));
            } else {
                log.debug("Requested " + number_of_containers + " containers");
            }

            while (number_of_containers > number_of_available_containers) {
                try {
                    souper_get = true;
                    sleeping_thread = Thread.currentThread();
                    Thread.sleep(duration_time);
                    souper_get = false;
                    sleeping_thread = null;
                    break;
                } catch (InterruptedException ex) {
                    break;
                }
            }

            if (number_of_containers <= number_of_available_containers) {
                synchronized (this) {
                    number_of_released_containers += number_of_containers;
                    number_of_available_containers -= number_of_containers;
                    personal_containers = releasedContainers.get(sessionID);
                    if (personal_containers == null) {
                        releasedContainers.put(sessionID, new LinkedList<ActiveContainer>());
                    }

                    for (i = 0; i < number_of_containers; i++) {
                        containers[i] = available_containers.get(0);
                        reserved_containers.add(available_containers.get(0));
                        releasedContainers.get(sessionID).add(available_containers.get(0));
                        available_containers.remove(0);
                    }
                }
            } else {
                containers = null;
                return null;
            }

            log.debug("The client acquired " + number_of_containers + " containers");
            return containers;
        }
    }

    @Override
    public ActiveContainer[] getAtMostContainers(int number_of_containers,
                                                 ArmComputeSessionID sessionID) throws RemoteException {

        int i;
        LinkedList<ActiveContainer> personal_containers;
        ActiveContainer[] containers;
        synchronized (get_lock) {

            if (number_of_containers > total_number_of_containers) {
                log.debug("Requested " + number_of_containers + " containers");
                number_of_containers = total_number_of_containers;
            } else if (number_of_containers <= 0) {
                throw new RemoteException(exceptionMessage("InvalidCont"));
            } else {
                log.debug("Requested " + number_of_containers + " containers");
            }

            synchronized (this) {

                if (number_of_containers > number_of_available_containers) {
                    number_of_containers = number_of_available_containers;
                }

                if (number_of_containers == 0) {
                    return null;
                } else {
                    containers = new ActiveContainer[number_of_containers];
                }

                number_of_released_containers += number_of_containers;
                number_of_available_containers -= number_of_containers;
                personal_containers = releasedContainers.get(sessionID);
                if (personal_containers == null) {
                    releasedContainers.put(sessionID, new LinkedList<ActiveContainer>());
                }

                for (i = 0; i < number_of_containers; i++) {
                    containers[i] = available_containers.get(0);
                    reserved_containers.add(available_containers.get(0));
                    releasedContainers.get(sessionID).add(available_containers.get(0));
                    available_containers.remove(0);
                }
            }

            log.debug("The client acquired " + number_of_containers + " containers");
            return containers;
        }
    }

    @Override
    public ActiveContainer[] tryGetContainers(int number_of_containers,
                                              ArmComputeSessionID sessionID) throws RemoteException {

        int i;
        LinkedList<ActiveContainer> personal_containers;
        synchronized (get_lock) {

            ActiveContainer[] containers = new ActiveContainer[number_of_containers];

            if (number_of_containers > total_number_of_containers) {
                containers = null;
                throw new RemoteException(exceptionMessage("ManyCont"));
            } else if (number_of_containers <= 0) {
                containers = null;
                throw new RemoteException(exceptionMessage("InvalidCont"));
            } else {
                log.debug("Requested " + number_of_containers + " containers");
            }

            synchronized (this) {
                if (number_of_containers > number_of_available_containers) {
                    containers = null;
                    return null;
                } else {
                    number_of_released_containers += number_of_containers;
                    number_of_available_containers -= number_of_containers;
                    personal_containers = releasedContainers.get(sessionID);
                    if (personal_containers == null) {
                        releasedContainers.put(sessionID, new LinkedList<ActiveContainer>());
                    }

                    for (i = 0; i < number_of_containers; i++) {
                        containers[i] = available_containers.get(0);
                        reserved_containers.add(available_containers.get(0));
                        releasedContainers.get(sessionID).add(available_containers.get(0));
                        available_containers.remove(0);
                    }
                }
            }

            log.debug("The client acquired " + number_of_containers + " containers");
            return containers;
        }
    }

    @Override
    public void releaseContainers(ActiveContainer[] containers, ArmComputeSessionID sessionID)
            throws RemoteException {

        synchronized (release_lock) {

            int i, j, released_containers, index;

            released_containers = containers.length;
            if (released_containers == 0) {
                throw new RemoteException(exceptionMessage("ZeroCont"));
            }

            synchronized (this) {

                for (i = 0; i < released_containers; i++) {
                    for (ActiveContainer container : reserved_containers) {
                        if (container.containerID == containers[i].containerID) {
                            number_of_available_containers++;
                            number_of_released_containers--;
                            available_containers.add(container);
                            reserved_containers.remove(container);
                            releasedContainers.get(sessionID).remove(container);
                            break;
                        }
                    }
                }
            }

            if (get == true) {
                wait_lock.lock();
                try {
                    if ((number_of_available_containers >= expected_number_of_containers) && (
                            expected_number_of_containers != 0)) {
                        expected_number_of_containers = 0;
                        get = false;
                        wait_condition.signalAll();
                    }
                } finally {
                    wait_lock.unlock();
                }
            } else if (souper_get == true) {
                souper_get = false;
                if (sleeping_thread != null) {
                    sleeping_thread.interrupt();
                    sleeping_thread = null;
                }
            }

            if (released_containers > 0) {
                log.debug(released_containers + " were dismissed");
            }
        }
    }

    @Override
    public void stopContainer(ActiveContainer container, ArmComputeSessionID sessionID)
            throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void closeSession(ArmComputeSessionID sessionID) throws RemoteException {

        LinkedList<ActiveContainer> personal_containers = releasedContainers.get(sessionID);

        for (ActiveContainer container : personal_containers) {
            reserved_containers.remove(container);
        }
        releasedContainers.remove(sessionID);
    }

    @Override
    public ContainerManagerStatus getStatus() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private String exceptionMessage(String message) {
        return message;

        // TODO(mallios): the following is not allowed in java 1.6!
        //    switch (message) {
        //      case "CreateInvalid":
        //        return "Invalid number of containers was given!!!";
        //      case "ManyCont":
        //        return "Requested too large ammount of containers!!!";
        //      case "InvalidCont":
        //        return "Requested invalid number of containers!!!";
        //      case "ZeroCont":
        //        return "Requested to released 0 containers";
        //      default:
        //        return "Not supported yet";
        //    }
    }
}
