/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.diskMgr;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;

/**
 * @author herald
 */
public interface DiskSession {

    File requestAccess(String fileName) throws RemoteException;

    File requestAccessRandomFile(String fileName) throws RemoteException;

    File requestAccess(File parent, String fileName) throws RemoteException;

    File requestAccessRandomFile(File parent, String fileName) throws RemoteException;

    InputStream openInputStream(File file) throws RemoteException;

    OutputStream openOutputStream(File file, boolean append) throws RemoteException;

    void delete(File file) throws RemoteException;

    void clean() throws RemoteException;
}
