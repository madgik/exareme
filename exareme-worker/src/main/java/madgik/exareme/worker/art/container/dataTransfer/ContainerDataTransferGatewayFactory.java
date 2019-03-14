/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.dataTransfer;

import madgik.exareme.worker.art.container.dataTransfer.rest.RestDataTransferGateway;

/**
 * @author Herald Kllapi
 * @author John Chronis <br>
 * @author Vaggelis Nikolopoulos <br>
 * University of Athens / Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class ContainerDataTransferGatewayFactory {

    public static DataTransferGateway createDataTransferServer(String artRegistry, int port) {
        return new RestDataTransferGateway(artRegistry, port);
    }

    //  public static DataTransferGateway createDataTransferServer(ArtManager manager) {
    //    return new RestDataTransferGateway(manager);
    //  }
}
