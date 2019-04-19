/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package madgik.exareme.worker.arm.storage.client;

import java.rmi.RemoteException;

/**
 * @author alexpap
 */
public class ArmStorageClientException extends RemoteException {

    public ArmStorageClientException(String message) {
        super(message);
    }

    public ArmStorageClientException(String message, Throwable cause) {
        super(message, cause);
    }

}
