/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.worker.arm;

import junit.framework.TestCase;
import madgik.exareme.worker.arm.compute.session.ActiveContainer;
import madgik.exareme.worker.arm.compute.session.ArmComputeSessionID;
import madgik.exareme.worker.arm.compute.trial.DummyContainerManager;

import java.rmi.RemoteException;

/**
 * @author Χρήστος
 */
public class DummyContainerManagerTest extends TestCase {

    DummyContainerManager manager;
    int NumberOfTotalContainers = 10;

    public DummyContainerManagerTest() throws RemoteException {
        manager = new DummyContainerManager(NumberOfTotalContainers);

    }

    /**
     * Test of getTotalNumberOfContainers method, of class DummyContainerManager.
     */
    public void testGetTotalNumberOfContainers() {
        System.out.println("getTotalNumberOfContainers");
        DummyContainerManager instance = manager;
        int expResult = NumberOfTotalContainers;
        int result = instance.getTotalNumberOfContainers();

        assertEquals(expResult, result);
    }

    /**
     * Test of getNumberOfAvailableContainers method, of class DummyContainerManager.
     */
    public void testGetNumberOfAvailableContainers() {
        System.out.println("getNumberOfAvailableContainers");
        DummyContainerManager instance = manager;
        int expResult = NumberOfTotalContainers;
        int result = instance.getNumberOfAvailableContainers();

        assertEquals(expResult, result);
    }

    /**
     * Test of startManager method, of class DummyContainerManager.
     */
/*  @Test
  public void testStartManager() throws Exception {
    System.out.println("startManager");
    DummyContainerManager instance = null;
    instance.startManager();
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }
*/
    /**
     * Test of stopManager method, of class DummyContainerManager.
     */
/*  @Test
  public void testStopManager() throws Exception {
    System.out.println("stopManager");
    DummyContainerManager instance = null;
    instance.stopManager();
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }
*/

    /**
     * Test of getContainers method, of class DummyContainerManager.
     */
    public void testGetContainers_int_ArmComputeSessionID() throws Exception {
        System.out.println("getContainers without limit time");
        int number_of_containers = NumberOfTotalContainers - 1;
        int acquired, requested;

        ArmComputeSessionID sessionID = null;
        long duration_time = 0L;
        DummyContainerManager instance = manager;
        ActiveContainer[] result =
            instance.getContainers(number_of_containers, sessionID, duration_time);
        acquired = number_of_containers;
        requested = result.length;
        assertEquals(requested, acquired);
    }

    /**
     * Test of getContainers method, of class DummyContainerManager.
     */
    public void testGetContainers_3args() throws Exception {
        System.out.println("getContainers with limit time");
        int first_request = NumberOfTotalContainers - 1, second_request = 2;
        int acquired, requested;

        ArmComputeSessionID sessionID = null;
        long duration_time = 100L;
        DummyContainerManager instance = manager;
        ActiveContainer[] result = instance.getContainers(first_request, sessionID, duration_time);
        ActiveContainer[] acquired_containers =
            instance.getContainers(second_request, sessionID, duration_time);
        if (acquired_containers == null) {
            acquired = 0;
        } else {
            acquired = acquired_containers.length;
        }
        requested = 0;
        assertEquals(requested, acquired);
    }

    /**
     * Test of getAtMostContainers method, of class DummyContainerManager.
     */
    public void testGetAtMostContainers() throws Exception {
        System.out.println("getAtMostContainers");
        ArmComputeSessionID sessionID = null;
        DummyContainerManager instance = manager;
        int number_of_containers = NumberOfTotalContainers - 1;
        int requested, acquired;

        ActiveContainer[] result1 = instance.getAtMostContainers(number_of_containers, sessionID);
        if (result1 != null) {
            acquired = result1.length;
        } else {
            acquired = 0;
        }
        requested = NumberOfTotalContainers - 1;
        assertEquals(requested, acquired);

        ActiveContainer[] result2 = instance.getAtMostContainers(number_of_containers, sessionID);
        if (result2 != null) {
            acquired = result2.length;
        } else {
            acquired = 0;
        }
        requested = 1;
        assertEquals(requested, acquired);

        ActiveContainer[] result3 = instance.getAtMostContainers(number_of_containers, sessionID);
        if (result3 != null) {
            acquired = result3.length;
        } else {
            acquired = 0;
        }
        requested = 0;
        assertEquals(requested, acquired);
    }

    /**
     * Test of tryGetContainers method, of class DummyContainerManager.
     */
    public void testTryGetContainers() throws Exception {
        System.out.println("tryGetContainers");
        ArmComputeSessionID sessionID = null;
        DummyContainerManager instance = manager;
        int number_of_containers = NumberOfTotalContainers - 1;
        int requested, acquired;

        ActiveContainer[] first_result = instance.tryGetContainers(number_of_containers, sessionID);

        if (first_result != null) {
            acquired = first_result.length;
        } else {
            acquired = 0;
        }
        requested = NumberOfTotalContainers - 1;
        assertEquals(requested, acquired);

        number_of_containers = 2;
        ActiveContainer[] second_result =
            instance.tryGetContainers(number_of_containers, sessionID);

        if (second_result != null) {
            acquired = second_result.length;
        } else {
            acquired = 0;
        }
        requested = 0;
        assertEquals(requested, acquired);
    }

    /**
     * Test of releaseContainers method, of class DummyContainerManager.
     */
/*  @Test
  public void testReleaseContainers() throws Exception {
    System.out.println("releaseContainers");
    ActiveContainer[] containers = null;
    ArmComputeSessionID sessionID = null;
    DummyContainerManager instance = null;
    instance.releaseContainers(containers, sessionID);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }
*/
    /**
     * Test of closeSession method, of class DummyContainerManager.
     */
/*  @Test
  public void testCloseSession() throws Exception {
    System.out.println("closeSession");
    ArmComputeSessionID sessionID = null;
    DummyContainerManager instance = null;
    instance.closeSession(sessionID);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }
*/
    /**
     * Test of getStatus method, of class DummyContainerManager.
     */
/*  @Test
  public void testGetStatus() throws Exception {
    System.out.println("getStatus");
    DummyContainerManager instance = null;
    ContainerManagerStatus expResult = null;
    ContainerManagerStatus result = instance.getStatus();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }
*/
    /**
     * Test of stopContainer method, of class DummyContainerManager.
     */
/*  @Test
  public void testStopContainer() throws Exception {
    System.out.println("stopContainer");
    ActiveContainer container = null;
    ArmComputeSessionID sessionID = null;
    DummyContainerManager instance = null;
    instance.stopContainer(container, sessionID);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }
*/
}
