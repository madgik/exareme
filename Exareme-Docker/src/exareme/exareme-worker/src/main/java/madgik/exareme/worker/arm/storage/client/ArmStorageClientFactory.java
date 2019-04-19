/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package madgik.exareme.worker.arm.storage.client;

import madgik.exareme.utils.properties.AdpProperties;
import madgik.exareme.worker.arm.storage.client.cluster.ClusterArmStorageClient;
import madgik.exareme.worker.arm.storage.client.local.LocalArmStorageClient;

/**
 * Assumptions :
 * + thread-safe
 *
 * @author alexpap
 */
public class ArmStorageClientFactory {

    private static final String defaultFS;
    private static final int buffersize;
    private static final int replication;
    private static final long blocksize;
    private static final boolean type;

    static {
        defaultFS = AdpProperties.getArmProps().getString("arm.storage.client.defaultFS");
        buffersize = AdpProperties.getArmProps().getInt("arm.storage.client.buffersize");
        replication = AdpProperties.getArmProps().getInt("arm.storage.client.replication");
        blocksize = AdpProperties.getArmProps().getInt("arm.storage.client.blocksize");

        type = defaultFS.contains("hdfs://");
    }


    /**
     * @return
     * @throws ArmStorageClientException
     */
    public static ArmStorageClient createArmStorageClient() throws ArmStorageClientException {
        if (!type)
            return new LocalArmStorageClient();
        return new ClusterArmStorageClient(defaultFS, buffersize, replication, blocksize);
    }
}
