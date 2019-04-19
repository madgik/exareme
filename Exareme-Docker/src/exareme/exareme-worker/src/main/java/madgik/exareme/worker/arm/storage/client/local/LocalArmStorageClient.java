/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package madgik.exareme.worker.arm.storage.client.local;

import madgik.exareme.worker.arm.storage.client.ArmStorageClient;
import madgik.exareme.worker.arm.storage.client.ArmStorageClientException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @author alexpap
 * @see ArmStorageClient
 */
public class LocalArmStorageClient implements ArmStorageClient {

    /**
     *
     */
    public LocalArmStorageClient() {
    }

    /**
     * @throws ArmStorageClientException
     */
    public void connect() throws ArmStorageClientException {
    }

    /**
     * Makes a hard link between files.
     *
     * @param src
     * @param dest
     * @throws ArmStorageClientException
     */
    @Override
    public void put(String src, String dest) throws ArmStorageClientException {

        // validate parameters
        if (src == null || src.isEmpty())
            throw new ArmStorageClientException("No valid src file path!");
        if (dest == null || dest.isEmpty())
            throw new ArmStorageClientException("No valid dest file path!");

        // src == remote -> do nothing!
        if (src.equals(dest))
            return;

        // already exist?
        File srcFile = new File(src);
        if (!srcFile.exists() || !srcFile.isFile() || srcFile.length() == 0)
            throw new ArmStorageClientException("src file does not exist!");

        File destFile = new File(dest);
        if (destFile.exists())
            throw new ArmStorageClientException("dest file already exists!");

        destFile.getParentFile().mkdirs();

        // hard link!
        try {
            Files.createLink(destFile.toPath(), srcFile.toPath());
        } catch (IOException x) {
            throw new ArmStorageClientException("Hard link failure : ", x);
        } catch (UnsupportedOperationException x) {
            throw new ArmStorageClientException("Hard links does not supported : ", x);
        }
    }

    /**
     * @param src
     * @param dest
     * @throws ArmStorageClientException
     */
    @Override
    public void fetch(String src, String dest) throws ArmStorageClientException {
        put(dest, src);
    }


    /**
     * @throws ArmStorageClientException
     */
    public void disconnect() throws ArmStorageClientException {
    }

}
