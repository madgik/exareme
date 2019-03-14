/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.dataTransfer.rest;

import madgik.exareme.worker.art.container.dataTrasferMgr.DataTransferMgrLocator;
import madgik.exareme.worker.art.container.diskMgr.DiskManagerInterface;
import org.apache.http.*;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

/**
 * @author John Chronis <br>
 * @author Vaggelis Nikolopoulos <br>
 * University of Athens / Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class DataTransferRequestHandler implements HttpRequestHandler {
    private static final Logger log = Logger.getLogger(DataTransferRequestHandler.class);
    public static DiskManagerInterface diskManagerInterface = null;
    public static ExecutorService DTThreadPool = null;
    private int port;

    public DataTransferRequestHandler(int port) {
        super();
        this.port = port;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void handle(final HttpRequest request, final HttpResponse response,
                       final HttpContext context) throws HttpException, IOException {
        String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
        if (!method.equals("GET") && !method.equals("POST")) {
            throw new MethodNotSupportedException("Not supported:" + method);
        }

        if (method.equals("POST")) {
            log.debug("POST handler ");

            String workerIP = null;
            Integer fid = null;
            String fileName = null;
            String workerPort = null;
            String pid = null;
            String mode = null;
            // TODO: register a file download request!
            try {
                HttpEntity entity = null;
                if (request instanceof HttpEntityEnclosingRequest) {
                    entity = ((HttpEntityEnclosingRequest) request).getEntity();
                }
                byte[] data;
                if (entity == null) {
                    data = new byte[0];
                } else {
                    data = EntityUtils.toByteArray(entity);
                }

                List<NameValuePair> params =
                        URLEncodedUtils.parse(new String(data), Charset.forName("UTF-8"));
                fileName = params.get(1).getValue();
                fid = Integer.parseInt(params.get(0).getValue());
                workerIP = params.get(2).getValue();
                workerPort = params.get(3).getValue();
                pid = params.get(4).getValue();
                mode = params.get(5).getValue();
                response.setStatusCode(200);

            } catch (IOException | NumberFormatException | IllegalStateException e) {
                log.error(e);
                response.setStatusCode(500);
            }
            if (mode.equals("register")) {

                DTThreadPool.submit(new RequestFileThread(workerIP, fid, fileName, workerPort, pid,
                        diskManagerInterface));

                log.debug("Registered file by " + workerIP + "file: " + fileName);
            } else if (mode.equals("completed")) {
                log.debug("Completed file transfer with id " + fid);
                try {
                    DataTransferMgrLocator.getDataTransferManager(this.port)
                            .addSuccesfulTransfer(fid);
                } catch (Exception ex) {
                    log.error(ex);
                }
            }

            return;
        }
        if (method.equals("GET")) {
            int fid = -1;
            log.debug("GET handler ");
            try {
                URI uri = new URI(request.getRequestLine().getUri());
                log.debug("URI: " + uri);
                List<NameValuePair> params = URLEncodedUtils.parse(uri, "UTF-8");
                String fileName = params.get(0).getValue();//fid
                //retrieve filename from datamanagerDTP
                String filepath = DataTransferMgrLocator.getDataTransferManager(this.port)
                        .getFileNamefromRegID(Integer.parseInt(fileName));
                String tableFile = DataTransferMgrLocator.getDataTransferManager(this.port)
                        .getTableFilefromRegID(Integer.parseInt(fileName));
                log.debug("Get handler " + filepath);
                File tf = new File(tableFile);


                FileEntity fe = new FileEntity(tf); //delete me
                response.setEntity(fe);
                response.setStatusCode(200);

            } catch (Exception e) {
                response.setEntity(new StringEntity("Cannot read file" + e));
                response.setStatusCode(404);
            }

            return;
        }
        response.setEntity(new StringEntity("Not supported method!"));
        response.setStatusCode(400);

        //
        // jv add success to dtp manager
        //
    }

    private InputStream getDataStream(String fileName) throws IOException {
        System.out.println("Reading file: " + fileName);
        final File file = new File(fileName);
        return new FileInputStream(file);
    }
}
