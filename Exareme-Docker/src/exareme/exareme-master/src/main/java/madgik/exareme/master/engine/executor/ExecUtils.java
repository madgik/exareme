/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.executor;

import com.google.gson.JsonObject;
import madgik.exareme.utils.association.Pair;
import madgik.exareme.utils.properties.AdpProperties;
import madgik.exareme.worker.art.concreteOperator.manager.MadisWebAPICaller;
import madgik.exareme.worker.art.concreteOperator.manager.ProcessManager;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author heraldkllapi
 */
public class ExecUtils {
    private static final String python =
            AdpProperties.getSystemProperties().getString("EXAREME_PYTHON");
    private static final String engine =
            AdpProperties.getSystemProperties().getString("EXAREME_MADIS");
    private static Logger log = Logger.getLogger(ExecUtils.class);

    public static void copyPartition(String tableLocation, String tempTableLocation, File directory,
                                     ProcessManager procManager) throws RemoteException {
        log.debug("Copying partition to temp file ...");
        try {
            Pair<String, String> stdOutErr =
                    procManager.createAndRunProcess(directory, "cp", tableLocation, tempTableLocation);

            if (stdOutErr.b.trim().isEmpty() == false) {
                throw new ServerException("Cannot execute cp: " + stdOutErr.b);
            }

            log.debug(stdOutErr.a);
        } catch (Exception e) {
            throw new ServerException("Cannot copy table: " + tempTableLocation, e);
        }
    }

    public static String runQueryOnTable(StringBuilder query, String madisMainDB, File directory,
                                         ProcessManager procManager) throws RemoteException {

        return runQueryOnTable(query, madisMainDB, directory);

        /*
        log.debug("Process Directory: " + directory.getAbsolutePath());
        log.debug("(ExecUtils::runQueryOnTable) running on MTERM process. query="+query.toString());
        try {
            Process p = procManager.createProcess(directory, python, engine, madisMainDB);
            p.getOutputStream().write(query.toString().getBytes());
            p.getOutputStream().flush();
            p.getOutputStream().close();

            InputStreamConsumerThread stdout =
                    new InputStreamConsumerThread(p.getInputStream(), false);
            stdout.start();

            InputStreamConsumerThread stderr =
                    new InputStreamConsumerThread(p.getErrorStream(), false);
            stderr.start();

            int exitCode = p.waitFor();
            stdout.join();
            stderr.join();

            if (exitCode != 0 || stderr.getOutput().trim().isEmpty() == false) {
                log.error(stderr.getOutput());
                throw new ServerException(
                        "Cannot execute db (code: " + exitCode + "): " + stderr.getOutput());
            }
            String output = stdout.getOutput();

            return output;
        } catch (Exception e) {
            throw new ServerException("Cannot run query", e);
        }
        */

    }

    private static String runQueryOnTable(StringBuilder query, String madisMainDB, File directory) throws RemoteException {

        String theLog = "";

        theLog += "(ExecuteUtils::runQueryOnTable) directory=" + directory.getAbsolutePath();
        theLog += "\n(ExecuteUtils::runQueryOnTable) madisMainDB=" + madisMainDB;

        String databaseFullPath = Paths.get(directory.getAbsolutePath(), madisMainDB).toString();
        String queryString = processQueryString(query.toString());

        MadisWebAPICaller madisWebAPICaller = new MadisWebAPICaller();
        String reply = "";
        try {
            reply = madisWebAPICaller.postRequest(databaseFullPath, queryString);
            theLog += "\n(ExecuteUtils::runQueryOnTable) just after post request. reply: ->\n\n" + reply + "\n\n<-";
        } catch (MadisWebAPICaller.MadisServerException | IOException e) {
            if (e instanceof MadisWebAPICaller.MadisServerException)
                theLog += "\n(ExecuteUtils::runQueryOnTable) MadisServerException: " + e.getMessage();
            else if (e instanceof IOException)
                theLog += "\n(ExecuteUtils::runQueryOnTable) IOException: " + e.getMessage();

            //this abomination of a line is here to replicate the pipeline of error handling that the previous solution
            //with the mterm process was using
            throw new ServerException("Cannot run query", new ServerException("Cannot execute db (code: " + 1 + "): " + e.getMessage()));
        } finally {
            log.debug(theLog);
        }

        return reply;

        /*
        catch (MadisWebAPICaller.MadisServerException mse){
            theLog+="\n(ExecuteUtils::runQueryOnTable) MadisServerException: "+mse.getMessage();
            //return mse.getMessage();
            throw new ServerException("Cannot run query", mse);
        }catch (IOException ioe){
            theLog+="\n(ExecuteUtils::runQueryOnTable) IOException: "+ioe.getMessage();
            throw new ServerException("Cannot run query", ioe);
            //return ioe.getMessage();
        }
        */
    }

    private static String processQueryString(String query) {

        String theLog = "(ExecuteUtils::processQueryString) UNPROCESSED query: " + query;

        query = query.replaceAll("-- Script BEGIN ", "");
        query = query.replaceAll("-- Optimization Pragmas", "");
        query = query.replaceAll("-- Additional Commands", "");
        query = query.replaceAll("-- Run Query", "");
        query = query.replaceAll("-- Mappings ", "");
        query = query.replaceAll("-- Attach databases", "");
        query = query.replaceAll("-- Create tables", "");
        query = query.replaceAll("-- Cleanup", "");
        query = query.replaceAll("-- Script END", "");

        query = query.replaceAll("  ", " ");
        query = query.replaceAll("(?m)^[ \t]*\r?\n", "");

        try {
            query = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            theLog += "(ExecuteUtils::processQueryString) UnsupportedEncodingException: " + ex;
            throw new RuntimeException(ex.getCause());
        } finally {
            theLog += "(ExecuteUtils::processQueryString) PROCESSED query: " + query;
            log.debug(theLog);
        }
        return query;

    }

    public static String runQueryOnTable(StringBuilder query, String madisMainDB, File directory,
                                         String Port) throws RemoteException {
        return runQueryOnTable(query, madisMainDB, directory, "localhost", Port);

    }

    public static String runQueryOnTable(StringBuilder query, String madisMainDB, File directory,
                                         String IP, String Port) throws RemoteException {
        log.debug("Process Directory: " + directory.getAbsolutePath());
        try {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("path", directory.getAbsolutePath() + "/" + madisMainDB);
            requestBody.addProperty("query", query.toString());
            String result = "";
            CloseableHttpClient httpclient = HttpClients.createDefault();
            List<BasicNameValuePair> params = new LinkedList<>();
            params.add(new BasicNameValuePair("Content-Type", "application/json"));

            HttpPost httpPost = null;
            try {
                httpPost = new HttpPost("http://" + IP + ":" + Port + "/runsql");
                httpPost.setHeader(new BasicHeader("Content-Type", "application/json"));


                httpPost.setEntity(new StringEntity(requestBody.toString()));
                CloseableHttpResponse response = null;
                try {
                    response = httpclient.execute(httpPost);
                    if (response.getStatusLine().getStatusCode() != 200) {
                        throw new ServerException(
                                "Cannot run query", new Exception(EntityUtils.toString(response.getEntity())));
                    } else {
                        result = EntityUtils.toString(response.getEntity());
                    }
                } finally {
                    response.close();
                }
            } finally {
                httpPost.releaseConnection();
                httpclient.close();

            }

            log.debug(result);
            return result;
        } catch (Exception e) {
            throw new ServerException("Cannot run query", e);
        }
    }

    public static void enforceNoStatsOnTable(String tableName, String madisMainDB, File directory,
                                             ProcessManager procManager) throws RemoteException {
        ExecUtils.runQueryOnTable(new StringBuilder("create table dummy(id); \n" +
                        "analyze dummy; \n" +
                        "drop table dummy; \n" +
                        "delete from sqlite_stat1 where tbl = '" + tableName + "'; \n" +
                        "insert into sqlite_stat1 values('" + tableName + "', null, 1000000); \n"),
                madisMainDB, directory, procManager);
    }
}
