/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.arm.compute.cluster;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.utils.association.Pair;
import madgik.exareme.worker.arm.compute.containerMgr.ContainerManagerInterface;
import madgik.exareme.worker.arm.compute.containerMgr.ContainerManagerStatus;
import madgik.exareme.worker.arm.compute.session.ActiveContainer;
import madgik.exareme.worker.arm.compute.session.ArmComputeSessionID;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Christos
 */
public class ClusterArmComputeInterface extends PriorityList implements ContainerManagerInterface {

    //synchronized Objects declaration
    private final Object update_indexes = new Object();
    //variable declaration
    private int weight_factor = 2;
    private int number_of_available_containers;
    private int number_of_currentlyWantedCont;
    private int number_of_active_sessions;
    private int number_of_inactive_sessions;
    private double quantum = 1;
    private Logger log;
    //structure declaration
    private TreeMap<Integer, LinkedList<ActiveContainer>> containers;
    private HashMap<EntityName, Integer> physicalMachines;
    private TreeMap<Integer, LinkedList<ArmComputeSessionID>> onusIndex;
    private HashMap<ArmComputeSessionID, Integer> patternOnus;
    private HashMap<ArmComputeSessionID, Pair<ArrayList<PatternElement>, ActiveContainer[]>>
            sessionPattern;
    private HashMap<ArmComputeSessionID, ArrayList<Integer>> matchingRelativeNames;
    private HashMap<ArmComputeSessionID, String> sessionStartTime;
    private HashMap<ArmComputeSessionID, LinkedList<ActiveContainer>> reservedContainers;
    private HashSet<ArmComputeSessionID> inactiveSessions;
    private LinkedList<ArmComputeSessionID> activeSes;


    public ClusterArmComputeInterface(int usages, ArrayList<ActiveContainer> container_list)
            throws RemoteException {

        super();

        //variable initialization
        number_of_available_containers = container_list.size() * usages;
        number_of_currentlyWantedCont = 0;

        //logger initialization
        //    Logger.getRootLogger().setLevel(Level.DEBUG);
        log = Logger.getLogger(ClusterArmComputeInterface.class);

        //index intilization
        containers = new TreeMap<Integer, LinkedList<ActiveContainer>>(new ReverseComparator());
        physicalMachines = new HashMap<EntityName, Integer>();
        onusIndex = new TreeMap<Integer, LinkedList<ArmComputeSessionID>>(new ReverseComparator());
        patternOnus = new HashMap<ArmComputeSessionID, Integer>();
        sessionPattern =
                new HashMap<ArmComputeSessionID, Pair<ArrayList<PatternElement>, ActiveContainer[]>>();
        matchingRelativeNames = new HashMap<ArmComputeSessionID, ArrayList<Integer>>();
        sessionStartTime = new HashMap<ArmComputeSessionID, String>();
        reservedContainers = new HashMap<ArmComputeSessionID, LinkedList<ActiveContainer>>();
        inactiveSessions = new HashSet<ArmComputeSessionID>();
        activeSes = new LinkedList<ArmComputeSessionID>();

        startManager();

        //procedure creating containers
        containers.put(usages, new LinkedList<ActiveContainer>());

        for (ActiveContainer container : container_list) {
            containers.get(usages).add(container);
            physicalMachines.put(container.containerName, usages);
        }
    }

    public int getWeightFactor() {
        return weight_factor;
    }

    public void setWeightFactor(int factor) {
        weight_factor = factor;
    }

    public double getQuantum() {
        return quantum;
    }

    public void setQuantun(double quantum) {
        this.quantum = quantum;
    }

    public double convertQuantumToSeconds(double quantum) {
        return quantum * this.quantum;
    }

    @Override
    public void setPattern(ArrayList<PatternElement> pattern, ArmComputeSessionID sessionID)
            throws RemoteException {

        int i, autoincreament;
        Pair<ArrayList<PatternElement>, ActiveContainer[]> pair;
        ActiveContainer[] containers_restriction;
        ArrayList<PatternElement> sorted_pattern;
        ArrayList<Integer> originalRelativeName;
        LinkedList<Integer> sorted_positions;
        TreeMap<Double, LinkedList<Integer>> start_time_tree;
        TreeMap<Double, LinkedList<Integer>> duration_tree;
        PatternElement element;
        Set set, duration_set;
        Iterator it, duration_it;
        Map.Entry map, duration_map;

        synchronized (update_indexes) {

            //This session has already set partern
            if (sessionPattern.get(sessionID) != null) {
                throw new RemoteException(exceptionMessage("NoEmptyPattern"));
            } else if (pattern.isEmpty()) {
                throw new RemoteException(exceptionMessage("SetEmptyPattern"));
            }

            originalRelativeName = new ArrayList<Integer>();

            sorted_pattern = new ArrayList<PatternElement>();
            sorted_positions = new LinkedList<Integer>();
            start_time_tree = new TreeMap<Double, LinkedList<Integer>>();

            //convert relative time and duration to seconds
            autoincreament = 0;
            for (i = 0; i < pattern.size(); i++) {
                element = pattern.get(i);
                element.relative_start_time = convertQuantumToSeconds(element.relative_start_time);
                element.duration = convertQuantumToSeconds(element.duration);

                //matching a new relative name
                if ((element.relative_name != -1)
                        && originalRelativeName.contains(element.relative_name) == false) {
                    originalRelativeName.add(element.relative_name);
                    autoincreament++;
                }
            }

            //Start of the procedure to sort the demands of a pattern based on the
            //starting relative time and the duration
            for (i = 0; i < pattern.size(); i++) {

                element = pattern.get(i);
                if (start_time_tree.containsKey(element.relative_start_time) == false) {
                    start_time_tree.
                            put(element.relative_start_time, new LinkedList<Integer>());
                }
                start_time_tree.get(element.relative_start_time).add(i);
            }

            set = start_time_tree.entrySet();
            it = set.iterator();

            while (it.hasNext()) {
                map = (Map.Entry) it.next();
                sorted_positions = (LinkedList<Integer>) map.getValue();

                duration_tree =
                        new TreeMap<Double, LinkedList<Integer>>(new ReverseDoubleComparator());

                for (i = 0; i < sorted_positions.size(); i++) {

                    if (duration_tree.containsKey(pattern.get(sorted_positions.get(i)).duration)
                            == false) {

                        duration_tree.put(pattern.get(sorted_positions.get(i)).duration,
                                new LinkedList<Integer>());
                    }
                    duration_tree.get(pattern.get(sorted_positions.get(i)).duration).
                            add(sorted_positions.get(i));
                }
                duration_set = duration_tree.entrySet();
                duration_it = duration_set.iterator();
                while (duration_it.hasNext()) {
                    duration_map = (Map.Entry) duration_it.next();
                    sorted_positions = (LinkedList<Integer>) duration_map.getValue();

                    for (i = 0; i < sorted_positions.size(); i++) {
                        sorted_pattern.add(pattern.get(sorted_positions.get(i)));
                    }
                }
            }
            //The end of the sorting procedure

            containers_restriction = new ActiveContainer[pattern.size()];
            for (i = 0; i < pattern.size(); i++) {
                containers_restriction[i] = null;
            }

            pair = new Pair(sorted_pattern, containers_restriction);
            //Insert the pattern of the session to the Indexes
            sessionPattern.put(sessionID, pair);

            //      if ((number_of_currentlyWantedCont + getDesiredContainers(sessionID))
            //          > number_of_available_containers) {
            //        sessionPattern.remove(sessionID);
            //        throw new RemoteException(exceptionMessage("TooMuchDemand"));
            //      }
            matchingRelativeNames.put(sessionID, originalRelativeName);

            //Consider the session as inactive(no request for containers yet)
            inactiveSessions.add(sessionID);
            number_of_inactive_sessions++;
            number_of_currentlyWantedCont += getDesiredContainers(sessionID);
        }
    }

    @Override
    public ArrayList<Pair<PatternElement, ActiveContainer>> getAtMostContainers(
            ArmComputeSessionID sessionID) throws RemoteException {

        int max_number_of_containers;
        boolean first_entry = true;
        LinkedList<ArmComputeSessionID> prioritySessions;
        ArrayList<Pair<PatternElement, ActiveContainer>> containers_array = null;

        synchronized (update_indexes) {

            if (activeSes.contains(sessionID)) {
                throw new RemoteException("AlreadyHaveActiveRequest");
            }

            activeSes.add(sessionID);
            number_of_active_sessions++;

            if (sessionPattern.containsKey(sessionID) == false) {
                throw new RemoteException(exceptionMessage("EmptyPattern"));
            } else if (sessionPattern.get(sessionID).a.isEmpty()) {
                throw new RemoteException(exceptionMessage("PatternCompleted"));
            }

            //update the onus of sessions
            updateOnus();
            //Session was inactive until now
            if (inactiveSessions.contains(sessionID)) {
                onusInitialization(sessionID); //initialize session's onus
            }
        }

        synchronized (update_indexes) {

            while ((number_of_available_containers == 0) || (checkPriority(sessionID, onusIndex)
                    == false)) {
                try {
                    update_indexes.wait();
                } catch (InterruptedException ex) {
                    throw new RemoteException("InterruptedException");
                }
            }

            //Find the maximum number of containers that the Session can obtain
            max_number_of_containers = getDesiredContainers(sessionID);

            do {
                if (first_entry == false) {
                    try {
                        update_indexes.wait();
                    } catch (InterruptedException ex) {
                        throw new RemoteException("InterruptedException");
                    }
                }
                if (number_of_available_containers != 0) {
                    containers_array = obtainContainers(sessionID, max_number_of_containers);
                }
                first_entry = false;
            } while (containers_array.isEmpty());

            number_of_active_sessions--;
            activeSes.remove(sessionID);
        }
        return containers_array;
    }

    @Override
    public void closeSession(ArmComputeSessionID sessionID) throws RemoteException {

        int i;
        ActiveContainer container;
        LinkedList<ActiveContainer> container_list;

        synchronized (update_indexes) {
            activeSes.add(sessionID);
            number_of_active_sessions++;

            if (sessionPattern.containsKey(sessionID) == false) {
                throw new RemoteException(exceptionMessage("EmptyPattern"));
            }

            container_list = reservedContainers.get(sessionID);
            for (i = 0; i < container_list.size(); i++) {
                container = container_list.get(i);
                releaseContainer(container, sessionID);
            }

            number_of_available_containers += container_list.size();

            reservedContainers.remove(sessionID);
            patternOnus.remove(sessionID);
            sessionPattern.remove(sessionID);
            sessionStartTime.remove(sessionID);

            update_indexes.notifyAll();

            number_of_active_sessions--;
            activeSes.remove(sessionID);
        }
    }

    @Override
    public void stopContainer(ActiveContainer container, ArmComputeSessionID sessionID)
            throws RemoteException {

        synchronized (update_indexes) {
            activeSes.add(sessionID);
            number_of_active_sessions++;

            if (sessionPattern.containsKey(sessionID) == false) {
                throw new RemoteException(exceptionMessage("EmptyPattern"));
            }

            releaseContainer(container, sessionID);

            number_of_available_containers++;
            update_indexes.notifyAll();

            number_of_active_sessions--;
            activeSes.remove(sessionID);
        }

    }

    //function which reports the status of the Container Manager
    public int printReport() {
        synchronized (update_indexes) {
            System.out.println("System report!!!");
            System.out.println("There are " + number_of_active_sessions + " sessions active");
            System.out
                    .println("There are " + number_of_available_containers + " available containers");
            System.out.println("There are " + number_of_inactive_sessions + " inactive sessions");
            System.out.println("System report is done!!!\n");
            return number_of_active_sessions;
        }
    }

    //function which dismiss a container
    private void releaseContainer(ActiveContainer container, ArmComputeSessionID sessionID)
            throws RemoteException {

        int usage = 0;
        Set set;
        Iterator it;
        Map.Entry map;
        LinkedList<ActiveContainer> list;

        if (reservedContainers.get(sessionID).remove(container) == false) {
            throw new RemoteException(exceptionMessage("NoRightToTheContainer"));
        }

        set = containers.entrySet();
        it = set.iterator();

        while (it.hasNext()) {
            map = (Map.Entry) it.next();
            usage = (Integer) map.getKey();
            list = (LinkedList<ActiveContainer>) map.getValue();

            if (list.contains(container) == true) {
                break;
            }
        }

        containers.get(usage).remove(container);
        if (containers.get(usage).size() == 0) {
            containers.remove(usage);
        }
        if (containers.containsKey(usage + 1) == false) {
            containers.put(usage + 1, new LinkedList<ActiveContainer>());
        }
        containers.get(usage + 1).add(container);

        physicalMachines.put(container.containerName, usage + 1);
    }

    //function which returns containers for a Session
    private ArrayList<Pair<PatternElement, ActiveContainer>> obtainContainers(
            ArmComputeSessionID sessionID, int max_number_of_containers) throws RemoteException {

        int counter, usages = 0, i, j, current_onus, key_value, position;
        int relativeName, matchedName = -1;
        boolean not_available_container;
        ActiveContainer restrictedContainer = null;
        LinkedList<ActiveContainer> container_list;
        ArrayList<Pair<PatternElement, ActiveContainer>> array =
                new ArrayList<Pair<PatternElement, ActiveContainer>>();
        Pair<PatternElement, ActiveContainer> pair;
        PatternElement element;

        Set set;
        Iterator it;
        Map.Entry map;
        LinkedList<ActiveContainer> list;
        ActiveContainer container = null;
        LinkedList<Integer> keys;

        counter = 0;
        position = 0;
        while (counter < max_number_of_containers) {

            try {
                element = sessionPattern.get(sessionID).a.get(position);
            } catch (IndexOutOfBoundsException e) {
                break;
            }

            relativeName = element.relative_name;

            //find for possible container restriction
            if (relativeName != -1) {
                matchedName = matchingRelativeNames.get(sessionID).get(relativeName);
                restrictedContainer = sessionPattern.get(sessionID).b[matchedName];
                if (restrictedContainer != null) {
                    usages = physicalMachines.get(restrictedContainer.containerName);
                }
            } else {
                usages = physicalMachines.get(element.machine);
                container_list = containers.get(usages);
                for (i = 0; i < container_list.size(); i++) {
                    container = container_list.get(i);
                    if (container.containerName == element.machine) {
                        break;
                    }
                }
                restrictedContainer = container;
            }

            if (restrictedContainer == null) {
                usages = containers.firstKey();
                if (usages == 0) {
                    break;
                }
                list = new LinkedList<ActiveContainer>(containers.get(usages));
                container = list.get(0);
                sessionPattern.get(sessionID).b[matchedName] = container;
            } else {
                //if the restricted container is not available, then skip it
                if (usages == 0) {
                    position++;
                    continue;
                }
                container = restrictedContainer;
            }

            containers.get(usages).remove(container);
            if (containers.get(usages).size() == 0) {
                containers.remove(usages);
            }

            if (containers.containsKey(usages - 1) == false) {
                containers.put(usages - 1, new LinkedList<ActiveContainer>());
            }
            containers.get(usages - 1).add(container);

            physicalMachines.put(container.containerName, usages - 1);

            pair = new Pair(element, container);
            array.add(pair);

            //remove from session's pattern
            sessionPattern.get(sessionID).getA().remove(position);

            //update that the session has obtained this container
            if (reservedContainers.containsKey(sessionID) == false) {
                reservedContainers.put(sessionID, new LinkedList<ActiveContainer>());
            }
            reservedContainers.get(sessionID).add(container);

            counter++;
        }

        //Session has a new starting relative time from now
        if (counter == max_number_of_containers) {
            current_onus = patternOnus.get(sessionID);
            patternOnus.remove(sessionID);  //delete the old onus of the session
            onusIndex.get(current_onus).remove(sessionID);  //update the onus index
            if (onusIndex.get(current_onus).size() == 0) {
                onusIndex.remove(current_onus);
            }

            if (!sessionPattern.get(sessionID).getA().isEmpty()) {
                onusInitialization(sessionID);
                number_of_currentlyWantedCont += getDesiredContainers(sessionID);
            }
        }

        number_of_currentlyWantedCont -= counter;
        number_of_available_containers -= counter;
        return array;
    }

    //function which returns the number of containers that a session needs atm
    private int getDesiredContainers(ArmComputeSessionID sessionID) {

        int i, number_of_containers = 0;
        double start_relative_time = 0;
        ArrayList<PatternElement> desirableCont;
        PatternElement element;

        desirableCont = sessionPattern.get(sessionID).getA();

        if (desirableCont.isEmpty()) {
            return 0;
        }

        for (i = 0; i < desirableCont.size(); i++) {
            element = desirableCont.get(i);
            if (i == 0) {
                start_relative_time = element.relative_start_time;
            }
            if (element.relative_start_time == start_relative_time) {
                number_of_containers++;
            } else {
                break;
            }
        }

        return number_of_containers;
    }

    //function which initializa the onus of a session
    private void onusInitialization(ArmComputeSessionID sessionID) throws RemoteException {

        int onus;
        double max_duration;
        String current_hour;

        current_hour = getCurrentTimeStamp();
        if ((sessionStartTime).containsKey(sessionID)) {
            sessionStartTime.remove(sessionID);
        }
        sessionStartTime.put(sessionID, current_hour);

        max_duration = sessionPattern.get(sessionID).getA().get(0).duration;
        //calculate the onus of the session
        onus = calculateOnus(max_duration, current_hour, true);
        if (onusIndex.containsKey(onus) == false) {
            onusIndex.put(onus, new LinkedList<ArmComputeSessionID>());
        }

        //Insert the onus of the session to the indexes
        onusIndex.get(onus).add(sessionID);
        patternOnus.put(sessionID, onus);

        if (inactiveSessions.contains(sessionID)) {
            number_of_inactive_sessions--;
            inactiveSessions.remove(sessionID); //session is no longer inactive
        }

    }

    //function which updates an existing onus
    private void updateOnus() throws RemoteException {

        int previous_onus, updated_onus;
        double max_duration;
        String relative_start_time;
        ArmComputeSessionID current_sessionID;
        Set set;
        Iterator it;
        Map.Entry map;

        set = patternOnus.entrySet();
        it = set.iterator();

        while (it.hasNext()) {
            map = (Map.Entry) it.next();
            current_sessionID = (ArmComputeSessionID) map.getKey();  //current session
            previous_onus = (Integer) map.getValue();  //onus of the current session

            //find the maximum duration of the currently needed containers
            if (sessionPattern.get(current_sessionID).getA().isEmpty()) {
                continue;
            }
            max_duration = sessionPattern.get(current_sessionID).getA().get(0).duration;
            //find the time that the containers were started to be requested
            relative_start_time = sessionStartTime.get(current_sessionID);
            //find the updated onus of the session
            updated_onus = calculateOnus(max_duration, relative_start_time, false);

            //replace the old onus with the new one
            patternOnus.put(current_sessionID, updated_onus);

            //remove the session for the list of the old onus
            onusIndex.get(previous_onus).remove(current_sessionID);
            if (onusIndex.get(previous_onus).size() == 0) {
                onusIndex.remove(previous_onus);
            }

            //Update the onusIndex with the new pair of Onus-Session
            if (onusIndex.containsKey(updated_onus) == false) {
                onusIndex.put(updated_onus, new LinkedList<ArmComputeSessionID>());
            }
            onusIndex.get(updated_onus).add(current_sessionID);
        }
    }

    //function which calculates the onus of a session
    private int calculateOnus(double duration, String startTime, boolean initialization)
            throws RemoteException {

        String current_time;
        int difference;
        double onus;

        if (initialization == true) { //The session will set a new onus
            onus = duration;
        } else { //The session will update an existing onus
            current_time = getCurrentTimeStamp();
            difference = calculateHourDifference(startTime, current_time);
            onus = difference * weight_factor + duration;
        }

        return (int) Math.round(onus);
    }

    //function which returns the current hour
    private String getCurrentTimeStamp() {

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();

        String strDate = sdfDate.format(now);
        return strDate;
    }

    //function which calculates the difference of two time-dates in seconds
    private int calculateHourDifference(String previous_start_time, String current_time)
            throws RemoteException {

        long difference;

        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            Date date1 = formatter.parse(previous_start_time);
            Date date2 = formatter.parse(current_time);

            // Get msec from each, and subtract.
            difference = date2.getTime() - date1.getTime();
        } catch (ParseException ex) {
            throw new RemoteException("ParseException");
        }

        return (int) difference / 1000;
    }

    //function which returns the messages of the Exceptions
    private String exceptionMessage(String message) {

        if (message.equals("CreateInvalid")) {
            return "Invalid number of containers was given!!!";
        } else if (message.equals("ManyCont")) {
            return "Requested too large ammount of containers!!!";
        } else if (message.equals("InvalidCont")) {
            return "Requested invalid number of containers!!!";
        } else if (message.equals("ZeroCont")) {
            return "Requested to released 0 containers!!!";
        } else if (message.equals("EmptySession")) {
            return "This session has not acquired any container!!!";
        } else if (message.equals("NoEmptyPattern")) {
            return "You have already set a pattern!!!";
        } else if (message.equals("EmptyPattern")) {
            return "There is no pattern for this session!!!";
        } else if (message.equals("PatternCompleted")) {
            return "This session has already completed his pattern!!!";
        } else if (message.equals("TooMuchDemand")) {
            return "There is too much demand in order to fulfil your needs!!!";
        } else if (message.equals("NoRightToTheContainer")) {
            return "The Session doesn't own this container!!!";
        } else if (message.equals("SetEmptyPattern")) {
            return "You cannot set an empty pattern";
        } else if (message.equals("InterruptedException")) {
            return "Interrupted Exception";
        } else if (message.equals("ParseException")) {
            return "Parse Exception";
        } else {
            return "Not supported yet(apo exceptionMessage function)!!!";
        }
    }

    @Override
    public final void startManager() throws RemoteException {


    }

    @Override
    public void stopManager() throws RemoteException {
        // TODO(mallios): clean up resources here.
    }

    @Override
    public ActiveContainer[] getContainers(int numOfContainers, ArmComputeSessionID sessionID)
            throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ActiveContainer[] tryGetContainers(int numOfContainers, ArmComputeSessionID sessionID)
            throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ActiveContainer[] getAtMostContainers(int numOfContainers, ArmComputeSessionID sessionID)
            throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ContainerManagerStatus getStatus() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    //Comparator which sort integer in descending order
    class ReverseComparator implements Comparator {
        @Override
        public int compare(Object firstObject, Object secondObject) {
            Integer first = (Integer) firstObject;
            Integer second = (Integer) secondObject;
            return second.compareTo(first);
        }
    }


    //Comparator for double descending order
    class ReverseDoubleComparator implements Comparator {
        @Override
        public int compare(Object firstObject, Object secondObject) {
            Double first = (Double) firstObject;
            Double second = (Double) secondObject;
            return second.compareTo(first);
        }
    }
}
