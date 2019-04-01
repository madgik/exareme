/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.manager;

import madgik.exareme.utils.net.NetUtil;
import madgik.exareme.utils.properties.AdpProperties;

/**
 * This is the ArtManagerProperties interface.
 *
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class ArtManagerProperties {
    private final String containerName;
    private final int localRegistryPort;
    private final int dataTransferPort;

    public ArtManagerProperties() {
        containerName =
                AdpProperties.getArtProps().getString("art.container.rmi.RmiContainer.defaultID");
        localRegistryPort = AdpProperties.getArtProps().getInt("art.registry.rmi.defaultPort");
        dataTransferPort = AdpProperties.getArtProps().getInt("art.container.data.port");
    }

    public ArtManagerProperties(String containerID, int localRegistryPort, int dataTransferPort) {
        this.containerName = containerID;
        this.localRegistryPort = localRegistryPort;
        this.dataTransferPort = dataTransferPort;
    }

    public String getContainerName() {
        return containerName;
    }

    public int getLocalRegistryPort() {
        return localRegistryPort;
    }

    public long getContainerID() {
        // Compute the id from the container IP.
        long ipNum = NetUtil.getIPLongRepresentation(NetUtil.getIPv4());
        long port = getLocalRegistryPort();
        // The port is 16 bits
        return (ipNum << 16) + port;
    }

    public int getDataTransferPort() {
        return dataTransferPort;
    }
}
