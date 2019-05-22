package madgik.exareme.master.gateway.async.handler.entity;

import madgik.exareme.master.client.AdpDBClientQueryStatus;
import madgik.exareme.master.connector.DataSerialization;
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
    private DataSerialization format;

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
    public void produceContent(ContentEncoder encoder, IOControl ioctrl)
            throws IOException {

        if (!queryStatus.hasFinished() && !queryStatus.hasError()) {
            if (l == null) {
                l = new NQueryStatusEntity.QueryStatusListener(ioctrl);
                queryStatus.registerListener(l);
            }
            ioctrl.suspendOutput();
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
            if (queryStatus.getError().contains("\n" + "Operator EXAREMEERROR:")) {
                String result = queryStatus.getError();
                result = result.substring(result.lastIndexOf("EXAREMEERROR:") + "EXAREMEERROR:".length()).replaceAll("\\s"," ");
                encoder.write(ByteBuffer.wrap(createErrorMessage(result).getBytes()));
                encoder.complete();
                close();
            }
            else if (queryStatus.getError().contains("\n" + "Operator PRIVACYERROR:")) {
                String result = createErrorMessage("The data you provided can not generate a result for the selected Experiment.");
                encoder.write(ByteBuffer.wrap(result.getBytes()));
                encoder.complete();
                close();
            }
            else if (queryStatus.getError().matches("java.rmi.RemoteException: Containers:.*not responding")) {
                String result = createErrorMessage("One or more containers are not responding. Please inform the system administrator.");
                encoder.write(ByteBuffer.wrap(result.getBytes()));
                encoder.complete();
                close();
            } else {
                String result = createErrorMessage("Something went wrong. Please inform your system administrator to consult the logs.");
                encoder.write(ByteBuffer.wrap(result.getBytes()));
                encoder.complete();
                close();
            }
        }
    }

    private String createErrorMessage(String error) {
        return "{\"error\" : \"" + error + "\"}";
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
