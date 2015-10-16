/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.dataTransfer.rest;

import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.worker.art.container.diskMgr.DiskManagerInterface;
import madgik.exareme.worker.art.container.diskMgr.DiskSession;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author John Chronis <br>
 * @author Vaggelis Nikolopoulos <br>
 *         University of Athens / Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class DataTransferClient {

    private static final Logger log = Logger.getLogger(DataTransferClient.class);


    public static void registerFileByID(String receiverIP, int fid, String filename, String IP,
        String FromPort, String ToPort, PlanSessionID pid) throws IOException {
        log.debug(
            "Register File: " + filename + " to :" + receiverIP + ":" + ToPort + " with FileID:"
                + fid + " From:" + IP + ":" + ToPort);
        CloseableHttpClient httpclient = HttpClients.createDefault();
        List<BasicNameValuePair> params = new LinkedList<>();
        params.add(new BasicNameValuePair(DataTransferConstants.FILE_ID, String.valueOf(fid)));
        params.add(new BasicNameValuePair(DataTransferConstants.URI_FILENAME, filename));
        params.add(new BasicNameValuePair(DataTransferConstants.WORKER_IP, IP));
        params.add(new BasicNameValuePair(DataTransferConstants.WORKER_PORT, FromPort));
        params.add(new BasicNameValuePair(DataTransferConstants.PLANSESSIONID,
            String.valueOf(pid.getLongId())));
        params.add(new BasicNameValuePair(DataTransferConstants.MODE, "register"));
        HttpPost httpPost = null;
        try {
            httpPost = new HttpPost("http://" + receiverIP + ":" + ToPort);
            //String paramString = URLEncodedUtils.format(params, "utf-8");

            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            CloseableHttpResponse response = null;
            try {
                response = httpclient.execute(httpPost);

                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new ClientProtocolException(
                        "Something went wrong with the registration of file");
                } else {
                    log.debug("File Registered: " + filename);
                }
            } catch (IOException e) {
                log.error(
                    "Error occurred while registering file by id (" + receiverIP + ":" + FromPort
                        + "):", e);
                throw e;
            } finally {
                if (response != null)
                    response.close();
            }
        } catch (NumberFormatException | IOException e) {
            log.error(e);
            throw e;
        } finally {
            httpPost.releaseConnection();
            try {
                httpclient.close();
            } catch (IOException ex) {
                log.error(ex);
            }
        }
    }

    public static void requestByID(String receiverIP, int fid, String filename, String port,
        String pid, DiskManagerInterface diskManagerInterface) {
        log.debug(
            "Request File: " + filename + " From: " + receiverIP + ":" + port + "  with FileID:"
                + fid);
        CloseableHttpClient httpclient = HttpClients.createDefault();
        boolean error = false;
        List<BasicNameValuePair> params = new LinkedList<>();
        params.add(new BasicNameValuePair(DataTransferConstants.FILE_ID, String.valueOf(fid)));
        params.add(new BasicNameValuePair(DataTransferConstants.URI_FILENAME, filename));
        HttpGet httpGet = null;
        try {
            httpGet = new HttpGet("http://" + receiverIP + ":" + port);
            String paramString = URLEncodedUtils.format(params, "utf-8");
            String newURI = httpGet.getURI() + "?" + paramString;
            httpGet.setURI(new URI(newURI));
            CloseableHttpResponse response = null;
            try {
                response = httpclient.execute(httpGet);
                HttpEntity entity = response.getEntity();

                //    DiskManager dskmang= new DiskManager(null);
                PlanSessionID pid2 = new PlanSessionID(Long.parseLong(pid));
                DiskSession disksession = diskManagerInterface.getGlobalSession(pid2);
                File filepath = disksession.requestAccess(filename);
                File filedata = disksession.requestAccessRandomFile("InputFile");
                //TODO JV
                ObjectOutputStream sessionFileStream =
                    new ObjectOutputStream(disksession.openOutputStream(filepath, false));

                sessionFileStream.writeObject(filedata.getAbsolutePath());
                sessionFileStream.close();

                //read from contenct to filedata and done :)
                if (entity != null) {
                    FileOutputStream fileOutputStream =
                        new FileOutputStream(filedata.getAbsolutePath());
                    try {
                        IOUtils.copy(entity.getContent(), fileOutputStream);
                        //EntityUtils.consumeQuietly(entity);
                        log.debug("File created: " + filepath.getAbsoluteFile());
                        //DTThreadPool.submit(new CompletedThread(receiverIP, fid, filename, port, pid));
                    } catch (IOException | IllegalStateException e) {
                        log.error(e);
                        error = true;
                    } finally {
                        fileOutputStream.close();
                    }
                }
            } catch (IOException e) {
                log.error(e);
                error = true;
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        } catch (URISyntaxException | NumberFormatException | IOException e) {
            log.error(e);
            error = true;
        } finally {
            httpGet.releaseConnection();
            try {
                httpclient.close();
            } catch (IOException ex) {
                log.error(ex);
            }
        }

        if (!error) {
            datatranferCompletedByID(receiverIP, fid, filename, port, pid);
        } else {
            log.error("Data Transfer Error!");
        }
    }

    public static void datatranferCompletedByID(String receiverIP, int fid, String filename,
        String port, String pid) {
        log.debug("File transfer completed: " + filename + " (FileID" + fid + ")" + " inform: "
            + receiverIP + ":" + port);
        CloseableHttpClient httpclient = HttpClients.createDefault();

        List<BasicNameValuePair> params = new LinkedList<>();
        params.add(new BasicNameValuePair(DataTransferConstants.FILE_ID, String.valueOf(fid)));
        params.add(new BasicNameValuePair(DataTransferConstants.URI_FILENAME, filename));
        params.add(new BasicNameValuePair(DataTransferConstants.WORKER_IP, ""));
        params.add(new BasicNameValuePair(DataTransferConstants.WORKER_PORT, ""));
        params.add(new BasicNameValuePair(DataTransferConstants.PLANSESSIONID, pid));
        params.add(new BasicNameValuePair(DataTransferConstants.MODE, "completed"));
        HttpPost httpPost = null;
        try {
            httpPost = new HttpPost("http://" + receiverIP + ":" + port);
            //String paramString = URLEncodedUtils.format(params, "utf-8");

            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            CloseableHttpResponse response = null;
            try {
                response = httpclient.execute(httpPost);
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new ClientProtocolException(
                        "Something went wrong with the registration of file");
                } else {
                    log.debug(
                        "File transfer completed: " + filename + ", " + receiverIP + ":" + port
                            + " informed");
                }
            } catch (IOException e) {
                log.error(e);
            } finally {
                response.close();
            }
        } catch (NumberFormatException | IOException e) {
            log.error(e);
        } finally {
            httpPost.releaseConnection();
            try {
                httpclient.close();
            } catch (IOException ex) {
                log.error(ex);
            }
        }
    }

}
