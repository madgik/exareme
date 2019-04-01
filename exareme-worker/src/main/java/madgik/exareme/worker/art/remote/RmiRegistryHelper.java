/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.remote;

import madgik.exareme.utils.properties.AdpProperties;
import madgik.exareme.worker.art.registry.ArtRegistryLocator;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Semaphore;

/**
 * @author Herald Kllapi <br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class RmiRegistryHelper {

    private static Semaphore waitRegisterThread = null;
    private static RegisterThread registerThread = null;
    private static Semaphore waitUnregisterThread = null;
    private static UnregisterThread unregisterThread = null;

    static {
        int rmiExportPort = AdpProperties.getArtProps().getInt("art.registry.rmi.exportPort");
        waitRegisterThread = new Semaphore(0);
        registerThread = new RegisterThread(rmiExportPort);
        registerThread.setDaemon(true);
        registerThread.start();

        waitUnregisterThread = new Semaphore(0);
        unregisterThread = new UnregisterThread();
        unregisterThread.setDaemon(true);
        unregisterThread.start();
    }

    private RmiRegistryHelper() {
    }

    public static synchronized void bind(RmiRemoteObject remoteObject) throws Exception {

        registerThread.register(remoteObject);
        waitRegisterThread.acquire();
    }

    public static synchronized void unbind(RmiRemoteObject remoteObject) throws Exception {

        unregisterThread.unregister(remoteObject);
        waitUnregisterThread.acquire();
    }


    static class RegisterThread extends Thread {

        private final int rmiExportPort;
        private Semaphore request = null;
        private RmiRemoteObject remoteObject = null;

        public RegisterThread(int rmiExportPort) {
            this.request = new Semaphore(0);
            this.rmiExportPort = rmiExportPort;
            this.setName("RegisterThread");
        }

        public void register(RmiRemoteObject remoteObject) {
            this.remoteObject = remoteObject;
            request.release();
        }

        @Override
        public void run() {
            while (true) {
                try {
                    request.acquire();

                    Remote monitorStub =
                            UnicastRemoteObject.exportObject(remoteObject, rmiExportPort);

                    Registry registry = ArtRegistryLocator.getLocalRmiRegistry();

                    registry.rebind(remoteObject.getRegEntryName(), monitorStub);

                    //                    while (true) {
                    //
                    //
                    //                        try {
                    //                            registry.lookup(remoteObject.getRegEntryName());
                    //                            break;
                    //                        } catch (NotBoundException e) {
                    //                            log.debug("Retrying to bind...");
                    //                        }
                    //                    }

                    waitRegisterThread.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    static class UnregisterThread extends Thread {

        private Semaphore request = null;
        private RmiRemoteObject remoteObject = null;

        public UnregisterThread() {
            this.request = new Semaphore(0);
            this.setName("UnregisterThread");
        }

        public void unregister(RmiRemoteObject remoteObject) {
            this.remoteObject = remoteObject;
            request.release();
        }

        @Override
        public void run() {
            while (true) {
                try {
                    request.acquire();

                    Registry registry = ArtRegistryLocator.getLocalRmiRegistry();

                    try {
                        registry.unbind(remoteObject.getRegEntryName());
                    } catch (NotBoundException e) {
                        //
                    }

                    //                    while (true) {
                    //
                    //
                    //                        try {
                    //                            registry.lookup(remoteObject.getRegEntryName());
                    //                            log.debug("Retrying to unbind...");
                    //                        } catch (NotBoundException e) {
                    //                            break;
                    //                        }
                    //                    }

                    waitUnregisterThread.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
