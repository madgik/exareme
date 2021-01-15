/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.dataTransfer.rest;

import madgik.exareme.worker.art.container.dataTransfer.DataTransferGateway;
import madgik.exareme.worker.art.manager.ArtManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * @author John Chronis <br>
 * @author Vaggelis Nikolopoulos <br>
 * University of Athens / Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class RestDataTransferGateway implements DataTransferGateway {

    private static final Logger log = Logger.getLogger(RestDataTransferGateway.class);
    // TODO(herald): this looks like a magic number!
    private final int threads = 20;
    private final int secondsToWait = 30;
    private String artRegistry = null;
    private ArtManager artManager = null;
    private DataTransferMainThread mainThread = null;
    private ExecutorService threadPool = null;
    private ExecutorService DTThreadPool = null;
    private int port;// = AdpProperties.getArtProps().getInt("art.container.data.port");

    {
        Logger.getLogger("org.apache.http").setLevel(org.apache.log4j.Level.OFF);

    }

    public RestDataTransferGateway(String artRegistry, int port) {
        this.artRegistry = artRegistry;
        this.port = port;
    }

    public RestDataTransferGateway(ArtManager manager) {
        this.artManager = manager;
    }

    @Override
    public void start() throws RemoteException {
        try {
            log.debug("Starting server ...");
            threadPool = Executors.newFixedThreadPool(threads);
            DTThreadPool = Executors.newFixedThreadPool(threads);
            mainThread = new DataTransferMainThread(port, threadPool, artRegistry, artManager);
            mainThread.setDaemon(false);
            mainThread.start();
            DataTransferRequestHandler.DTThreadPool = DTThreadPool;
        } catch (IOException e) {
            throw new RemoteException("Cannot create http server", e);
        }
    }

    @Override
    public void stop() {

        mainThread.stopAcceptingConnections();
        mainThread.interrupt();
        threadPool.shutdown();
        DTThreadPool.shutdown();
        try {
            log.info("Waiting termination ...");
            threadPool.awaitTermination(secondsToWait, TimeUnit.SECONDS);
            DTThreadPool.awaitTermination(secondsToWait, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
        } finally {
            // TODO(herald): do something with the remaining jobs
            List<Runnable> remaining = threadPool.shutdownNow();
            List<Runnable> remainingDT = DTThreadPool.shutdownNow();

            log.info("Jobs remaining: " + remaining.size());
            mainThread.stop();
            log.info("Stopping server ...");
            try {
                mainThread.close();
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(RestDataTransferGateway.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }

    }


}
