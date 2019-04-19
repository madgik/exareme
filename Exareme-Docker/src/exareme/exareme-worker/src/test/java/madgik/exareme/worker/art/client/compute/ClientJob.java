/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.client.compute;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.utils.association.Pair;
import madgik.exareme.worker.arm.compute.cluster.ClusterArmComputeInterface;
import madgik.exareme.worker.arm.compute.cluster.PatternElement;
import madgik.exareme.worker.arm.compute.session.ActiveContainer;
import madgik.exareme.worker.arm.compute.session.ArmComputeSessionID;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Christos
 */
public class ClientJob implements Runnable {

    private static final Object client_lock = new Object();
    private static int number_of_threads;
    private static int counter5 = 0;
    private static int whole_counter = 0;
    private ArrayList<EntityName> entities;
    private int sleeping_time = 30;
    private ClusterArmComputeInterface manager;
    private int number;
    private boolean interrupted = false;
    private boolean createPattern = true;


    public ClientJob(ClusterArmComputeInterface manager, int number, int number_of_threads,
                     ArrayList<EntityName> entities) {
        this.number = number + 1;
        this.manager = manager;
        this.entities = entities;
        ClientJob.number_of_threads = number_of_threads;
    }


    private boolean checkRelativeConstraints(
            ArrayList<Pair<PatternElement, ActiveContainer>> results, ArrayList<EntityName> entities) {

        ActiveContainer[] array = new ActiveContainer[results.size()];
        int i, current_position, max_position = 0, relativeName;
        ActiveContainer container;
        EntityName entity;
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
                    if (container != array[current_position]) {
                        return false;
                    }
                } else {
                    arrayPosition.add(relativeName);
                    array[max_position] = container;
                    max_position++;
                }
            } else {
                entity = container.containerName;
                if (entities.contains(entity)) {
                    entities.remove(entity);
                } else {
                    return false;
                }
            }
        }

        return entities.isEmpty();
    }


    @Override
    public void run() {

        ArmComputeSessionID id = new ArmComputeSessionID(number);
        ArrayList<PatternElement> array = new ArrayList<PatternElement>();
        ArrayList<Pair<PatternElement, ActiveContainer>> result = null, totalResults;
        ArrayList<EntityName> constrainedEntities = new ArrayList<EntityName>();
        PatternElement element;
        int i, duration_time, relative_start_time, name, position, entityNumber;
        Random rn;
        int counter = 0;
        int max_counter = 7;

        Random random = new Random();
        entityNumber = random.nextInt(max_counter - 1);
        for (i = 0; i < max_counter; i++) {
            name = random.nextInt(max_counter - 1) + 1;
            duration_time = random.nextInt(10) + 1;
            relative_start_time = random.nextInt(5);

            element = new PatternElement();
            if (name == entityNumber) {
                position = random.nextInt(entities.size() - 1) % entities.size();
                element.setParameters(entities.get(position), duration_time, relative_start_time);
                constrainedEntities.add(entities.get(position));
            } else {
                element.setParameters(name, duration_time, relative_start_time);
            }
            array.add(element);
        }

        try {
            manager.setPattern(array, id);
        } catch (RemoteException ex) {
            interrupted = true;
            createPattern = false;
        }

        totalResults = new ArrayList<Pair<PatternElement, ActiveContainer>>();
        try {

            while (counter < max_counter && interrupted != true) {

                try {
                    result = manager.getAtMostContainers(id);
                    totalResults.addAll(result);
                } catch (Exception e) {
                    interrupted = true;
                    break;
                }

                Thread.sleep(1);
                for (i = 0; i < result.size(); i++) {
                    manager.stopContainer(result.get(i).getB(), id);
                }
                counter += result.size();
                whole_counter += result.size();

                Thread.sleep(sleeping_time);
            }
            if (counter != max_counter && interrupted == false) {
                throw new Exception(id + " didn't acquired all " + "the containers that wanted");
            }
        } catch (Exception e) {
            Logger.getLogger(ClientJob.class.getName()).log(Level.SEVERE, null, e);
        }
        if (createPattern != false && !interrupted) {
            try {
                manager.closeSession(id);
            } catch (RemoteException ex) {
                Logger.getLogger(ClientJob.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (createPattern != false && !interrupted) {
            if (checkRelativeConstraints(totalResults, constrainedEntities) == false) {
                try {
                    throw new Exception("Constraints don't workproperly !!!");
                } catch (Exception ex) {
                    Logger.getLogger(ClientJob.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
