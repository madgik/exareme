/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.arm.compute.simple;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.utils.association.Pair;
import madgik.exareme.worker.arm.compute.cluster.PatternElement;
import madgik.exareme.worker.arm.compute.containerMgr.ContainerManagerInterface;
import madgik.exareme.worker.arm.compute.containerMgr.ContainerManagerStatus;
import madgik.exareme.worker.arm.compute.session.ActiveContainer;
import madgik.exareme.worker.arm.compute.session.ArmComputeSessionID;
import madgik.exareme.worker.arm.compute.trial.DummyContainerManager;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Xristos
 */
public class ContainerManager implements ContainerManagerInterface {
    private static Logger log = Logger.getLogger(DummyContainerManager.class);
    private final Object get_lock = new Object();
    private final Object release_lock = new Object();
    private int total_number_of_containers;
    private int number_of_usages;
    private int number_of_available_containers;
    private int number_of_released_containers = 0;
    private int expected_number_of_containers = 0;
    private boolean multi_usage;
    private boolean get = false;
    private boolean limit_get = false;
    private Thread sleeping_thread = null;
    private HashMap<ArmComputeSessionID, LinkedList<ActiveContainer>> reserved_containers;
    private HashMap<ActiveContainer, Integer> available_containers;
    private ReentrantLock wait_lock = new ReentrantLock();
    private final Condition wait_condition = wait_lock.newCondition();
    private ArrayList<ArmComputeSessionID> aa = new ArrayList<ArmComputeSessionID>();

    public ContainerManager(int number_of_containers, int number_of_usages, boolean multi_usage)
        throws RemoteException {
        int i;
        EntityName entity;

        if (number_of_containers <= 0) {
            throw new RemoteException(exceptionMessage("CreateInvalid"));
        }

        available_containers = new HashMap<ActiveContainer, Integer>();
        reserved_containers = new HashMap<ArmComputeSessionID, LinkedList<ActiveContainer>>();

        this.multi_usage = multi_usage;
        this.total_number_of_containers = number_of_containers;
        this.number_of_available_containers = number_of_containers;
        this.number_of_usages = number_of_usages;

        for (i = 0; i < number_of_containers; i++) {
            entity = new EntityName("Container" + i, "192.168.2.3");
            available_containers.put(new ActiveContainer(i, entity, i), number_of_usages);
        }
    }

    public int getTotalNumberOfContainers() {
        return this.total_number_of_containers;
    }

    public int getNumberOfAvailableContainers() {
        return this.number_of_available_containers;
    }

    @Override public void startManager() throws RemoteException {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public void stopManager() throws RemoteException {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ActiveContainer[] getContainers(int number_of_containers, ArmComputeSessionID sessionID)
        throws RemoteException {

        ActiveContainer[] containers;
        System.out.println("Uparxoun " + this.number_of_available_containers);

        synchronized (get_lock) {

            if (aa.contains(sessionID) == true) {
                System.out.println("Yparxei");
            } else {
                System.out.println("Den uparxei");
                aa.add(sessionID);
            }

            if (number_of_containers > total_number_of_containers) {
                throw new RemoteException(exceptionMessage("ManyCont"));
            } else if (number_of_containers <= 0) {
                throw new RemoteException(exceptionMessage("InvalidCont"));
            } else {
                System.out.println("Requested " + number_of_containers + " containers");
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
                containers = findContainers(number_of_containers, sessionID);
            }

            System.out.println("The client acquired " + number_of_containers + " containers");
            return containers;
        }
    }

    //@Override
    public ActiveContainer[] getContainers(int number_of_containers, ArmComputeSessionID sessionID,
        long duration_time) throws RemoteException {

        ActiveContainer[] containers;

        synchronized (get_lock) {

            if (number_of_containers > total_number_of_containers) {
                containers = null;
                throw new RemoteException(exceptionMessage("ManyCont"));
            } else if (number_of_containers <= 0) {
                containers = null;
                throw new RemoteException(exceptionMessage("InvalidCont"));
            } else {
                System.out.println("Requested " + number_of_containers + " containers");
            }

            if (number_of_containers > number_of_available_containers) {
                try {
                    limit_get = true;
                    sleeping_thread = Thread.currentThread();
                    expected_number_of_containers = number_of_containers;
                    Thread.sleep(duration_time);
                    expected_number_of_containers = 0;
                    limit_get = false;
                    sleeping_thread = null;
                } catch (InterruptedException ex) {  //To thread ginete awaked
                }
            }

            if (number_of_containers <= number_of_available_containers) {
                synchronized (this) {

                    containers = findContainers(number_of_containers, sessionID);
                }
            } else {
                containers = null;
                return null;
            }

            System.out.println("The client acquired " + number_of_containers + " containers");
            return containers;
        }
    }

    @Override public ActiveContainer[] getAtMostContainers(int number_of_containers,
        ArmComputeSessionID sessionID) throws RemoteException {

        ActiveContainer[] containers;

        synchronized (get_lock) {

            if (number_of_containers > total_number_of_containers) {
                System.out.println("Requested " + number_of_containers + " containers");
                number_of_containers = total_number_of_containers;
            } else if (number_of_containers <= 0) {
                throw new RemoteException(exceptionMessage("InvalidCont"));
            } else {
                System.out.println("Requested " + number_of_containers + " containers");
            }

            synchronized (this) {

                if (number_of_containers > number_of_available_containers) {
                    number_of_containers = number_of_available_containers;
                }

                if (number_of_containers == 0) {
                    return null;
                }

                containers = findContainers(number_of_containers, sessionID);
            }

            System.out.println("The client acquired " + number_of_containers + " containers");
            return containers;
        }
    }

    @Override public ActiveContainer[] tryGetContainers(int number_of_containers,
        ArmComputeSessionID sessionID) throws RemoteException {

        ActiveContainer[] containers;

        synchronized (get_lock) {

            if (number_of_containers > total_number_of_containers) {
                containers = null;
                throw new RemoteException(exceptionMessage("ManyCont"));
            } else if (number_of_containers <= 0) {
                containers = null;
                throw new RemoteException(exceptionMessage("InvalidCont"));
            } else {
                System.out.println("Requested " + number_of_containers + " containers");
            }

            synchronized (this) {
                if (number_of_containers > number_of_available_containers) {
                    containers = null;
                    return null;
                } else {
                    containers = findContainers(number_of_containers, sessionID);
                }
            }

            System.out.println("The client acquired " + number_of_containers + " containers");
            return containers;
        }
    }

    //@Override
    public void releaseContainers(ActiveContainer[] containers, ArmComputeSessionID sessionID)
        throws RemoteException {

        synchronized (release_lock) {

            int i, j, released_containers, usages;
            LinkedList<ActiveContainer> personal_containers;
            personal_containers = reserved_containers.get(sessionID);

            if (personal_containers == null) {
                throw new RemoteException("EmptySession");
            }

            released_containers = containers.length;
            if (released_containers == 0) {
                throw new RemoteException(exceptionMessage("ZeroCont"));
            }

            synchronized (this) {
                int in = 0;
                for (i = 0; i < released_containers; i++) {
                    if (personal_containers.contains(containers[i]) == true) {
                        in++;
                        if (available_containers.containsKey(containers[i]) == false) {
                            number_of_available_containers++;
                            number_of_released_containers--;
                            usages = 0;
                        } else {
                            usages = available_containers.get(containers[i]);
                            available_containers.remove(containers[i]);
                        }
                        usages++;
                        available_containers.put(containers[i], usages);
                        reserved_containers.get(sessionID).remove(containers[i]);
                    }
                }
                if (in != released_containers) {
                    throw new RemoteException(in + " " + released_containers);
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
            } else if (limit_get == true) {
                if ((number_of_available_containers >= expected_number_of_containers) && (
                    expected_number_of_containers != 0)) {
                    expected_number_of_containers = 0;
                    limit_get = false;
                    if (sleeping_thread != null) {
                        sleeping_thread.interrupt();
                    }
                }
            }

            if (released_containers > 0) {
                System.out.println(released_containers + " were dismissed");
            }
        }
    }

    @Override public void stopContainer(ActiveContainer container, ArmComputeSessionID sessionID)
        throws RemoteException {

        synchronized (release_lock) {

            int i, j, released_containers, usages;
            LinkedList<ActiveContainer> personal_containers;
            personal_containers = reserved_containers.get(sessionID);

            if (personal_containers == null) {
                throw new RemoteException("EmptySession");
            }

            synchronized (this) {

                if (personal_containers.contains(container) == true) {

                    if (available_containers.containsKey(container) == false) {
                        number_of_available_containers++;
                        number_of_released_containers--;
                        usages = 0;
                    } else {
                        usages = available_containers.get(container);
                        available_containers.remove(container);
                    }
                    usages++;
                    available_containers.put(container, usages);
                    reserved_containers.get(sessionID).remove(container);
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
            } else if (limit_get == true) {
                if ((number_of_available_containers >= expected_number_of_containers) && (
                    expected_number_of_containers != 0)) {
                    expected_number_of_containers = 0;
                    limit_get = false;
                    if (sleeping_thread != null) {
                        sleeping_thread.interrupt();
                    }
                }
            }
        }
    }

    @Override public void closeSession(ArmComputeSessionID sessionID) throws RemoteException {

        int usages;

        synchronized (release_lock) {

            LinkedList<ActiveContainer> personal_containers = reserved_containers.get(sessionID);

            if (personal_containers != null) {
                System.out.println("session closed " + sessionID);
                synchronized (this) {
                    for (ActiveContainer container : personal_containers) {

                        if (available_containers.containsKey(container)) {
                            usages = available_containers.get(container);
                            usages++;
                            available_containers.put(container, usages);
                        } else {
                            available_containers.put(container, 1);
                        }
                    }
                    reserved_containers.remove(sessionID);
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
                } else if (limit_get == true) {
                    if ((number_of_available_containers >= expected_number_of_containers) && (
                        expected_number_of_containers != 0)) {
                        expected_number_of_containers = 0;
                        limit_get = false;
                        if (sleeping_thread != null) {
                            sleeping_thread.interrupt();
                        }
                    }
                }
            } else {
                System.err.println("session close failed " + sessionID);
            }
        }
    }

    @Override public ContainerManagerStatus getStatus() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private ActiveContainer[] findContainers(int number_of_containers,
        ArmComputeSessionID sessionID) {

        int i, usages, temp_usage;
        boolean update = true;
        ActiveContainer container = null;
        LinkedList<ActiveContainer> personal_containers;
        ActiveContainer[] containers = new ActiveContainer[number_of_containers];
        LinkedList<ActiveContainer> temp = new LinkedList<ActiveContainer>();
        LinkedList<ActiveContainer> available = new LinkedList<ActiveContainer>();

        personal_containers = reserved_containers.get(sessionID);
        if (personal_containers == null) {
            System.err.println("dhmiourgia neou key" + sessionID);
            reserved_containers.put(sessionID, new LinkedList<ActiveContainer>());
            if (reserved_containers.containsKey(sessionID) == true) {
                System.out.println("Uparxei to key meta th dimiourgia " + sessionID);
            } else {
                System.out.println("Den uparxei to key meta th dimiourgia " + sessionID);
            }
        } else {
            System.out.println("den xreiastike dhmiorgia");
        }

        Set set = available_containers.entrySet();
        Iterator iterator = set.iterator();
        i = 0;

        if (multi_usage == false) {

            while (iterator.hasNext()) {
                Map.Entry index = (Map.Entry) iterator.next();
                container = (ActiveContainer) index.getKey();

                temp.add(container);
                containers[i] = container;
                if (reserved_containers.containsKey(sessionID) == true) {
                    System.out.println("Ontws uparxei to key " + sessionID);
                } else {
                    System.out.println("Eprepe na upirxe to key " + sessionID);
                }
                reserved_containers.get(sessionID).add(container);

                LinkedList<ActiveContainer> exist = reserved_containers.get(sessionID);
                if (exist == null) {
                    System.out.println("Den petuxe " + sessionID);
                } else {
                    System.out.println("Petuxe " + sessionID);
                }

                i++;
                if (i == number_of_containers) {
                    break;
                }
            }
        } else {

            temp_usage = 0;
            while (i < number_of_containers) {

                if (update == true) {
                    Map.Entry index = (Map.Entry) iterator.next();
                    container = (ActiveContainer) index.getKey();
                    temp_usage = (Integer) index.getValue();
                    update = false;
                }

                temp.add(container);
                containers[i] = container;
                reserved_containers.get(sessionID).add(container);
                temp_usage--;

                if (temp_usage <= 0) {
                    update = true;
                }
                i++;
            }
        }

        for (ActiveContainer element : temp) {  //To temp ginete gt tha skasei an kanoume update sto iterator loop
            usages = available_containers.get(element);
            usages--;
            available_containers.remove(element);
            if (usages > 0) {
                available_containers.put(element, usages);
            } else {
                number_of_released_containers++;
                number_of_available_containers--;
            }
        }

        return containers;
    }

    private String exceptionMessage(String message) {
        if (message.equals("CreateInvalid")) {
            return "Invalid number of containers was given!!!";
        } else if (message.equals("ManyCont")) {
            return "Requested too large ammount of containers!!!";
        } else if (message.equals("InvalidCont")) {
            return "Requested invalid number of containers!!!";
        } else if (message.equals("ZeroCont")) {
            return "Requested to released 0 containers";
        } else if (message.equals("EmptySession")) {
            return "This session has not acquired any container";
        } else {
            return "Not supported yet";
        }
    }

    @Override
    public void setPattern(ArrayList<PatternElement> pattern, ArmComputeSessionID sessionID)
        throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public ArrayList<Pair<PatternElement, ActiveContainer>> getAtMostContainers(
        ArmComputeSessionID sessionID) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
