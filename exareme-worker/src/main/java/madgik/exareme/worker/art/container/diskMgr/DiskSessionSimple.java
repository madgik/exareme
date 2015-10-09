/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.diskMgr;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.ServerException;

/**
 * @author herald
 */
public class DiskSessionSimple implements DiskSession {
    private static Logger log = Logger.getLogger(DiskSessionSimple.class);
    private File rootFile = null;

    public DiskSessionSimple(File root) {
        rootFile = root;
    }

    public File requestAccess(String fileName) throws RemoteException {
        File file = new File(rootFile, fileName);
        log.debug("Granted access to file: " + fileName);
        return file;
    }

    public File requestAccessRandomFile(String fileName) throws RemoteException {
        File file = null;
        try {
            file = File.createTempFile(fileName, "", rootFile);
            file.delete();
        } catch (Exception e) {
            throw new ServerException("Cannot create temp file " + fileName, e);
        }
        log.debug("Granted access to file: " + fileName);
        return file;
    }

    public File requestAccess(File parent, String fileName) throws RemoteException {
        File file = new File(parent, fileName);
        log.debug("Granted access to file: " + fileName);
        return file;
    }

    public File requestAccessRandomFile(File parent, String fileName) throws RemoteException {
        File file = null;
        try {
            file = File.createTempFile(fileName, "", parent);
            file.delete();
        } catch (Exception e) {
            throw new ServerException("Cannot create temp file " + fileName, e);
        }
        log.debug("Granted access to file: " + fileName);
        return file;
    }

    public void delete(File file) throws RemoteException {
        try {
            FileUtils.deleteDirectory(file);
        } catch (Exception e) {
            throw new ServerException("Cannot delete file: " + file.getAbsolutePath(), e);
        }
    }

    public InputStream openInputStream(File file) throws RemoteException {
        log.debug("Open input stream to file: " + file.getName());
        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
        } catch (Exception e) {
            throw new ServerException("Cannot open file input stream: ", e);
        }
        return input;
    }

    public OutputStream openOutputStream(File file, boolean append) throws RemoteException {
        log.debug("Open output stream to file: " + file.getName());
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(file);
        } catch (Exception e) {
            throw new ServerException("Cannot open file input stream: ", e);
        }
        return output;
    }

    public void clean() throws RemoteException {
        log.debug("Cleaning root directory:" + rootFile.getAbsolutePath());
        try {
            FileUtils.deleteDirectory(rootFile);
            rootFile.delete();
        } catch (Exception e) {
            throw new ServerException("Cannot delete root directory", e);
        }
    }
}
