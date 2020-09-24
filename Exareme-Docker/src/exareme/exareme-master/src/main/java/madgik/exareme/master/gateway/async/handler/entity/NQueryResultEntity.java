package madgik.exareme.master.gateway.async.handler.entity;

import madgik.exareme.master.client.AdpDBClientQueryStatus;
import madgik.exareme.master.connector.DataSerialization;
import madgik.exareme.master.gateway.async.handler.HBP.HBPQueryHelper;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.entity.HttpAsyncContentProducer;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * TODO flush output before suspend
 */
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
                channel = Channels.newChannel(queryStatus.getResult(format));
            }
            channel.read(buffer);
            buffer.flip();
            int i = encoder.write(buffer);
            final boolean buffering = this.buffer.hasRemaining();
            this.buffer.compact();
            if (i < 1 && !buffering) {
                encoder.complete();
                close();
            }

        } else {
            log.trace("|" + queryStatus.getError() + "|");
            if (queryStatus.getError().contains("ExaremeError:")) {
                String data = queryStatus.getError().substring(queryStatus.getError().lastIndexOf("ExaremeError:") + "ExaremeError:".length()).replaceAll("\\s", " ");
                //type could be error, user_error, warning regarding the error occurred along the process
                String result = HBPQueryHelper.ErrorResponse.createErrorResponse(data, user_error);
                logErrorMessage(result);
                encoder.write(ByteBuffer.wrap(result.getBytes()));
                encoder.complete();
                close();
            } else if (queryStatus.getError().contains("PrivacyError")) {
                String data = "The Experiment could not run with the input provided because there are insufficient data.";
                //type could be error, user_error, warning regarding the error occurred along the process
                String result = HBPQueryHelper.ErrorResponse.createErrorResponse(data, warning);
                logErrorMessage(result);
                encoder.write(ByteBuffer.wrap(result.getBytes()));
                encoder.complete();
                close();
            } else if (queryStatus.getError().contains("java.rmi.RemoteException")) {
                String data = "One or more containers are not responding. Please inform the system administrator.";
                //type could be error, user_error, warning regarding the error occurred along the process
                String result = HBPQueryHelper.ErrorResponse.createErrorResponse(data, error);
                logErrorMessage(result);
                encoder.write(ByteBuffer.wrap(result.getBytes()));
                encoder.complete();
                close();
            } else {
                log.info("Exception from madis: " + queryStatus.getError());
                String data = "Something went wrong. Please inform the system administrator.";
                //type could be error, user_error, warning regarding the error occurred along the process
                String result = HBPQueryHelper.ErrorResponse.createErrorResponse(data, error);
                logErrorMessage(result);
                encoder.write(ByteBuffer.wrap(result.getBytes()));
                encoder.complete();
                close();
            }
        }
    }

    private void logErrorMessage(String error) {
        log.info("Algorithm exited with error and returned:\n " + error);
    }

    @Override
    public void close() throws IOException {
        queryStatus.close();
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }
}
