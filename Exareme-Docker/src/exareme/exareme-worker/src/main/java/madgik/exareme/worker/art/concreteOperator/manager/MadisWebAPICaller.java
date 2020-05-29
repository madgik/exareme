package madgik.exareme.worker.art.concreteOperator.manager;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MadisWebAPICaller {
    private final static Logger log = Logger.getLogger(MadisWebAPICaller.class);


    public String postRequest(String dbFilename, String query) throws MadisServerException, IOException {
        HttpURLConnection connection;
        String url_str = "http://localhost:8888";
        String parameters = "dbfilename="+dbFilename+"&"+"query="+query;
        log.debug("(MadisWebAPICaller::postRequest) parameters: ->\n\n"+parameters+"\n\n<-");
        byte[] postData = parameters.getBytes(StandardCharsets.UTF_8);
        int postDataLength = postData.length;
        String reply="";
        URL url = new URL(url_str);
        connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("charset", "utf-8");
        connection.setRequestProperty("Content-Length", Integer.toString(postDataLength));
        connection.setUseCaches(false);
        connection.setRequestProperty("User-Agent", "Java client");
        
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.write(postData);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder content;            
            String line;
            content = new StringBuilder();
            while ((line = br.readLine()) != null) {
                content.append(line);
                content.append(System.lineSeparator());
            }
            reply = content.toString();
        } catch (Exception e) {
            BufferedReader errorStreamReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            String errorMessage = "";
            String line;
            while ((line = errorStreamReader.readLine()) != null) {
                errorMessage += line + "\n";
            }
            throw new MadisServerException(errorMessage);
        }
        return reply;
    }

    public class MadisServerException extends Exception{
        MadisServerException(String errorMessage){
            super(errorMessage);
        }
    }

}

