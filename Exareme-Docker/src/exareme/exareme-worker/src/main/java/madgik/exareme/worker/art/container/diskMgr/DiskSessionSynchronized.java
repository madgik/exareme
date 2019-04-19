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
public class DiskSessionSynchronized implements DiskSession {

    private static final long serialVersionUID = 1L;
    private final DiskSession session;

    public DiskSessionSynchronized(DiskSession session) {
        this.session = session;
    }

    public File requestAccess(String fileName) throws RemoteException {
        synchronized (session) {
            return session.requestAccess(fileName);
        }
    }

    public File requestAccessRandomFile(String fileName) throws RemoteException {
        synchronized (session) {
            return session.requestAccessRandomFile(fileName);
        }
    }

    public File requestAccess(File parent, String fileName) throws RemoteException {
        synchronized (session) {
            return session.requestAccess(parent, fileName);
        }
    }

    public File requestAccessRandomFile(File parent, String fileName) throws RemoteException {
        synchronized (session) {
            return session.requestAccessRandomFile(parent, fileName);
        }
    }

    public InputStream openInputStream(File file) throws RemoteException {
        synchronized (session) {
            return session.openInputStream(file);
        }
    }

    public OutputStream openOutputStream(File file, boolean append) throws RemoteException {
        synchronized (session) {
            return session.openOutputStream(file, append);
        }
    }

    public void delete(File file) throws RemoteException {
        synchronized (session) {
            session.delete(file);
        }
    }

    public void clean() throws RemoteException {
        synchronized (session) {
            session.clean();
        }
    }
}
