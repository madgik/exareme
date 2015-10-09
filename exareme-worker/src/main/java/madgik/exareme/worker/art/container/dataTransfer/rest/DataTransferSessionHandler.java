/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.dataTransfer.rest;

import org.apache.http.HttpServerConnection;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpService;
import org.apache.log4j.Logger;

/**
 * @author heraldkllapi
 */
public class DataTransferSessionHandler extends Thread {
    private static final Logger log = Logger.getLogger(DataTransferSessionHandler.class);
    private final HttpService httpservice;
    private final HttpServerConnection conn;

    public DataTransferSessionHandler(final HttpService httpservice,
        final HttpServerConnection conn) {
        super();
        this.httpservice = httpservice;
        this.conn = conn;

    }

    @Override public void run() {
        try {
            HttpContext context = new BasicHttpContext();
            this.httpservice.handleRequest(this.conn, context);
        } catch (Exception e) {
            log.error("Worker thread error : " + e.getMessage(), e);
        } finally {
            try {
                this.conn.shutdown();
            } catch (Exception ex) {
                log.error("Worker thread error : " + ex.getMessage(), ex);
            }
        }
    }
}
