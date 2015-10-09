/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container;

/**
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 *         {herald,paparas,evas}@di.uoa.gr<br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class ContainerLocator {

    private static ContainerProxy localContainerProxy = null;

    private ContainerLocator() {
        throw new RuntimeException("Cannot create instances of this class");
    }

    public static ContainerProxy getLocalContainer() {
        return ContainerLocator.localContainerProxy;
    }

    public static void setLocalContainer(ContainerProxy containerProxy) {
        ContainerLocator.localContainerProxy = containerProxy;
    }
}
