package madgik.exareme.worker.art.security;

import java.io.FileDescriptor;
import java.net.InetAddress;
import java.security.Permission;

/**
 * @author herald
 */
public class ContainerSecurityManager extends SecurityManager {

    public ContainerSecurityManager() {

    }

    @Override public void checkExit(int status) {
        throw new SecurityException("Cannot Shutdown Containers!");
    }

    @Override public void checkExec(String cmd) {
        throw new SecurityException("Cannot Execute Processes!");
    }

    @Override public void checkAccept(String host, int port) {
        throw new SecurityException("Cannot use sockets!");
    }

    @Override public void checkAccess(Thread t) {
        throw new SecurityException("Cannot ...");
    }

    @Override public void checkAccess(ThreadGroup g) {
        throw new SecurityException("Cannot ...");
    }

    @Override public void checkAwtEventQueueAccess() {
        throw new SecurityException("Cannot ...");
    }

    @Override public void checkConnect(String host, int port) {
        throw new SecurityException("Cannot ...");
    }

    @Override public void checkConnect(String host, int port, Object context) {
        throw new SecurityException("Cannot ...");
    }

    @Override public void checkCreateClassLoader() {
        throw new SecurityException("Cannot ...");
    }

    @Override public void checkDelete(String file) {
        throw new SecurityException("Cannot ...");
    }

    @Override public void checkLink(String lib) {
        throw new SecurityException("Cannot ...");
    }

    @Override public void checkListen(int port) {
        throw new SecurityException("Cannot ...");
    }

    @Override public void checkMemberAccess(Class<?> clazz, int which) {
        throw new SecurityException("Cannot ...");
    }

    @Override public void checkMulticast(InetAddress maddr) {
        throw new SecurityException("Cannot ...");
    }

    @Override public void checkMulticast(InetAddress maddr, byte ttl) {
        throw new SecurityException("Cannot ...");
    }

    @Override public void checkPackageAccess(String pkg) {
        throw new SecurityException("Cannot ...");
    }

    @Override public void checkPackageDefinition(String pkg) {
        throw new SecurityException("Cannot ...");
    }

    @Override public void checkPermission(Permission perm) {
        throw new SecurityException("Cannot ...");
    }

    @Override public void checkPermission(Permission perm, Object context) {
        throw new SecurityException("Cannot ...");
    }

    @Override public void checkPrintJobAccess() {
        throw new SecurityException("Cannot ...");
    }

    @Override public void checkPropertiesAccess() {
        throw new SecurityException("Cannot ...");
    }

    @Override public void checkPropertyAccess(String key) {
        throw new SecurityException("Cannot ...");
    }

    @Override public void checkRead(FileDescriptor fd) {
        throw new SecurityException("Cannot ...");
    }

    @Override public void checkRead(String file) {
        throw new SecurityException("Cannot ...");
    }

    @Override public void checkRead(String file, Object context) {
        throw new SecurityException("Cannot ...");
    }

    @Override public void checkSecurityAccess(String target) {
        throw new SecurityException("Cannot ...");
    }

    @Override public void checkSetFactory() {
        throw new SecurityException("Cannot ...");
    }

    @Override public void checkSystemClipboardAccess() {
        throw new SecurityException("Cannot ...");
    }

    @Override public boolean checkTopLevelWindow(Object window) {
        throw new SecurityException("Cannot ...");
    }

    @Override public void checkWrite(FileDescriptor fd) {
        throw new SecurityException("Cannot ...");
    }

    @Override public void checkWrite(String file) {
        throw new SecurityException("Cannot ...");
    }
}
