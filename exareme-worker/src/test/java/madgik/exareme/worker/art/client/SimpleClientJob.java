/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.client;

import madgik.exareme.utils.association.Pair;
import madgik.exareme.worker.arm.compute.ArmCompute;
import madgik.exareme.worker.arm.compute.cluster.PatternElement;
import madgik.exareme.worker.arm.compute.session.ActiveContainer;
import madgik.exareme.worker.arm.compute.session.ArmComputeSession;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;

/**
 * @author heraldkllapi
 */
public class SimpleClientJob implements Runnable {
    private static final Logger log = Logger.getLogger(SimpleClientJob.class);
    private ArmCompute compute = null;
    private int numContainers = 0;
    private long keepFor_ms = 0;
    private long release_ms = 0;

    public SimpleClientJob(ArmCompute compute, int numContainers, long keepFor_ms,
        long release_ms) {
        this.compute = compute;
        this.numContainers = numContainers;
        this.keepFor_ms = keepFor_ms;
        this.release_ms = release_ms;
    }

    private boolean checkRelativeConstraints(
        ArrayList<Pair<PatternElement, ActiveContainer>> results) {

        ActiveContainer[] array = new ActiveContainer[results.size()];
        int i, current_position, max_position = 0, relativeName;
        ActiveContainer container;
        ArrayList<Integer> arrayPosition = new ArrayList();
        Pair<PatternElement, ActiveContainer> pair;

        for (i = 0; i < results.size(); i++) {
            array[i] = null;
        }

        for (i = 0; i < results.size(); i++) {
            pair = results.get(i);
            relativeName = pair.a.relative_name;
            container = pair.b;

            if (relativeName != -1) {
                if (arrayPosition.contains(relativeName)) {
                    current_position = arrayPosition.indexOf(relativeName);
                    if (container.ID != array[current_position].ID) {
                        return false;
                    }
                } else {
                    arrayPosition.add(relativeName);
                    array[max_position] = container;
                    max_position++;
                }
            }
        }

        return true;
    }

    @Override public void run() {
        try {
            ArmComputeSession session = compute.createProxy().createSession();
            ArrayList<PatternElement> array = new ArrayList<PatternElement>();
            PatternElement element;
            Random rn;
            int i, duration_time, relative_start_time, name, counter = 0;
            int maxCounter = numContainers;
            boolean interrupted = false;

            Random random = new Random();
            for (i = 0; i < maxCounter; i++) {
                name = random.nextInt(maxCounter - 1) + 1;
                duration_time = random.nextInt(10);
                relative_start_time = random.nextInt(5);
                element = new PatternElement();
                element.setParameters(name, duration_time, relative_start_time);
                array.add(element);
            }
            try {
                session.getComputeSessionContainerManagerProxy().setPattern(array);
            } catch (RemoteException ex) {
                log.error("Cannot set pattern", ex);
                interrupted = true;
                return;
            }
            Thread.sleep(keepFor_ms);
            ArrayList<Pair<PatternElement, ActiveContainer>> containerArray =
                new ArrayList<Pair<PatternElement, ActiveContainer>>();
            while (true) {
                ArrayList<Pair<PatternElement, ActiveContainer>> containers;
                try {
                    containers =
                        session.getComputeSessionContainerManagerProxy().getAtMostContainers();
                    Thread.sleep(release_ms);
                    for (Pair<PatternElement, ActiveContainer> container : containers) {
                        containerArray.add(container);
                        session.getComputeSessionContainerManagerProxy().
                            stopContainer(container.b);
                    }
                    if (containerArray.size() == numContainers) {
                        break;
                    }
                } catch (Exception ex) {
                    log.error("", ex);
                    break;
                }
            }

            session.close();
            log.info("");
            if (containerArray.size() != numContainers && interrupted == false) {
                throw new RemoteException(
                    "The session didn't aqcuire all the " + "containers, it was needed!!!");
            }

            if (checkRelativeConstraints(containerArray) == false && interrupted == false) {
                try {
                    throw new Exception("Constraint doesn't work");
                } catch (Exception ex) {
                    java.util.logging.Logger.getLogger(SimpleClientJob.class.getName()).
                        log(Level.SEVERE, null, ex);
                }
            }

        } catch (Exception e) {
            log.error("Client error", e);
        }
    }
}
