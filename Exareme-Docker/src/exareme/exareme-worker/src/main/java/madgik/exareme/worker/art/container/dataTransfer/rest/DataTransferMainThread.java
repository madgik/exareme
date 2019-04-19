/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.dataTransfer.rest;

import madgik.exareme.utils.units.Metrics;
import madgik.exareme.worker.art.manager.ArtManager;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.SyncBasicHttpParams;
import org.apache.http.protocol.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * @author John Chronis <br>
 * @author Vaggelis Nikolopoulos <br>
 * University of Athens / Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class DataTransferMainThread extends Thread {
    private static final Logger log = Logger.getLogger(DataTransferMainThread.class);
    private final ServerSocket serversocket;
    private final HttpParams params;
    private final HttpService httpService;
    private final ExecutorService threadPool;
    private int bufferSizeKB = 16;//TODO(jc) check
    private boolean acceptConnections = true;

    public DataTransferMainThread(int port, ExecutorService threadPool, String artRegistry,
                                  ArtManager artManager) throws IOException {
        this.serversocket = new ServerSocket(port);
        this.threadPool = threadPool;
        this.params = new SyncBasicHttpParams();
        this.params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, Integer.MAX_VALUE)
                .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, bufferSizeKB * Metrics.KB)
                .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
                .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, false)
                .setParameter(CoreProtocolPNames.ORIGIN_SERVER, "HttpComponents/1.1");
        // Set up the HTTP protocol processor
        HttpProcessor httpproc =
                new ImmutableHttpProcessor(new ResponseDate(), new ResponseServer(),
                        new ResponseContent(), new ResponseConnControl());
        // Set up request handlers
        HttpRequestHandlerRegistry reqistry = new HttpRequestHandlerRegistry();
        reqistry.register("*", new DataTransferRequestHandler(port));
        // Set up the HTTP service
        this.httpService = new HttpService(httpproc, new DefaultConnectionReuseStrategy(),
                new DefaultHttpResponseFactory(), reqistry, this.params);
    }


    public void stopAcceptingConnections() {
        acceptConnections = false;
    }

    @Override
    public void run() {
        log.debug("Listening on port " + this.serversocket.getLocalPort());
        while (!Thread.interrupted() && acceptConnections) {
            try {
                Socket socket = this.serversocket.accept();
                log.debug("Incoming connection from " + socket.getInetAddress());
                if (acceptConnections == false) {
                    log.debug("Cannot accept more connections ...");
                    break;
                }
                DefaultHttpServerConnection conn = new DefaultHttpServerConnection();
                conn.bind(socket, this.params);
                conn.setSocketTimeout(0);
                threadPool.submit(new DataTransferSessionHandler(this.httpService, conn));
            } catch (InterruptedIOException _) {
                break;
            } catch (Exception e) {
                log.debug("Error initialising connection thread: ", e);
                break;
            }
        }
    }

    void close() throws IOException {
        this.serversocket.close();
    }
}
