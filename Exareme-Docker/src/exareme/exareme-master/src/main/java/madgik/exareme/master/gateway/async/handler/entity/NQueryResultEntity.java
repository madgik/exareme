package madgik.exareme.master.gateway.async.handler.entity;

import madgik.exareme.master.client.AdpDBClientQueryStatus;
import madgik.exareme.master.connector.DataSerialization;
import madgik.exareme.master.gateway.async.handler.HBP.HBPQueryHelper;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.entity.HttpAsyncContentProducer;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;

public class NQueryResultEntity extends BasicHttpEntity implements HttpAsyncContentProducer {

    private static final Logger log = Logger.getLogger(NQueryResultEntity.class);

    private final AdpDBClientQueryStatus queryStatus;
    private final ByteBuffer buffer;
    private ReadableByteChannel channel;
    private NQueryStatusEntity.QueryStatusListener l;
    private final DataSerialization format;
    private final static String user_error = "text/plain+user_error";
    private final static String error = "text/plain+error";
    private final static String warning = "text/plain+warning";

    public NQueryResultEntity(AdpDBClientQueryStatus status, DataSerialization ds,
                              int bufferSize) {
        super();
        queryStatus = status;
        buffer = ByteBuffer.allocate(bufferSize);
        channel = null;
        l = null;
        format = ds;
    }

    @Override
    public void produceContent(ContentEncoder encoder, IOControl iocontrol)
            throws IOException {

        if (!queryStatus.hasFinished() && !queryStatus.hasError()) {
            if (l == null) {
                l = new NQueryStatusEntity.QueryStatusListener(iocontrol);
                queryStatus.registerListener(l);
            }
            iocontrol.suspendOutput();
            return;
        }

        if (!queryStatus.hasError()) {
            if (channel == null) {
                String result = queryStatus.getResult(format);
                log.info("Algorithm with queryId " + queryStatus.getQueryID().getQueryID()
                        + " terminated. Result: \n " + result);
                channel = Channels.newChannel(
                        new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8)));
            }
            channel.read(buffer);
            buffer.flip();
            int i = encoder.write(buffer);
            final boolean buffering = this.buffer.hasRemaining();
            this.buffer.compact();
            if (i < 1 && !buffering) {
                encoder.complete();
                closeQuery();
                close();
            }
        } else {
            if (queryStatus.getError().contains("ExaremeError:")) {
                String data = queryStatus.getError().substring(queryStatus.getError().lastIndexOf("ExaremeError:") + "ExaremeError:".length()).replaceAll("\\s", " ");
                String result = HBPQueryHelper.ErrorResponse.createErrorResponse(data, user_error);
                logErrorMessage(result);
                encoder.write(ByteBuffer.wrap(result.getBytes()));
                encoder.complete();
            } else if (queryStatus.getError().contains("PrivacyError")) {
                String data = "The Experiment could not run with the input provided because there are insufficient data.";
                String result = HBPQueryHelper.ErrorResponse.createErrorResponse(data, warning);
                logErrorMessage(result);
                encoder.write(ByteBuffer.wrap(result.getBytes()));
                encoder.complete();
            } else if (queryStatus.getError().contains("java.rmi.RemoteException")) {
                String data = "One or more containers are not responding. Please inform the system administrator.";
                String result = HBPQueryHelper.ErrorResponse.createErrorResponse(data, error);
                logErrorMessage(result);
                encoder.write(ByteBuffer.wrap(result.getBytes()));
                encoder.complete();
            } else {
                log.info("Exception when running the query: " + queryStatus.getError());
                String data = "Something went wrong. Please inform the system administrator.";
                String result = HBPQueryHelper.ErrorResponse.createErrorResponse(data, error);
                logErrorMessage(result);
                encoder.write(ByteBuffer.wrap(result.getBytes()));
                encoder.complete();
            }
            closeQuery();
            close();
        }
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }

    public void closeQuery() throws IOException {
        queryStatus.close();
    }

    @Override
    public void close() {

    }

    private void logErrorMessage(String error) {
        log.info("Algorithm exited with error and returned:\n " + error);
    }
}
