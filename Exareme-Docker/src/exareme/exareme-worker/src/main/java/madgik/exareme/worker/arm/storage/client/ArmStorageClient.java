/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package madgik.exareme.worker.arm.storage.client;

/**
 * Encapsulates all the storage operations.
 * TODO - interact with Partitions
 * <p/>
 * Assumptions
 * + thread-safe
 * + lightweight
 *
 * @author alexpap
 * @see ArmStorageClientFactory
 */
public interface ArmStorageClient {

    /**
     * @throws ArmStorageClientException
     */
    void connect() throws ArmStorageClientException;

    /**
     * Places src into the ArmStorage space as dest.
     *
     * @param src
     * @param dest
     * @throws ArmStorageClientException
     */
    void put(String src, String dest) throws ArmStorageClientException;

    /**
     * Places src from the ArmStorage space as dest.
     *
     * @param src
     * @param dest
     * @throws ArmStorageClientException
     */
    void fetch(String src, String dest) throws ArmStorageClientException;

    /**
     * @throws ArmStorageClientException
     */
    void disconnect() throws ArmStorageClientException;

}
