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
 *
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
            if (queryStatus.getError().contains("\n" + "Operator VARIABLE:")) {
                String result = "{\"Error\":\"Please provide a variable that exists.\"}";
                encoder.write(ByteBuffer.wrap(result.getBytes()));
                encoder.complete();
                close();
            } else if (queryStatus.getError().contains("\n" + "Operator DATASET:")) {
                String result = "{\"Error\":\"Please provide a dataset that exists.\"}";
                encoder.write(ByteBuffer.wrap(result.getBytes()));
                encoder.complete();
                close();
            } else if (queryStatus.getError().contains("\n" + "Operator NULLTABLE:")) {
                String result = "{\"Error\":\"The input you provided gives an empty table. Please check your input.\"}";
                encoder.write(ByteBuffer.wrap(result.getBytes()));
                encoder.complete();
                close();
            } else if (queryStatus.getError().matches("java.rmi.RemoteException: Containers:.*not responding")) {
                String result = "{\"Error\":\"One or more containers are not responding. Please inform your system administrator\"}";
                encoder.write(ByteBuffer.wrap(result.getBytes()));
                encoder.complete();
                close();
            } else if (queryStatus.getError().contains("\n" + "Operator TYPE:")) {
                String result = "{\"Error\":\"Each variable's type must be Real,Integer or Float.\"}";
                encoder.write(ByteBuffer.wrap(result.getBytes()));
                encoder.complete();
                close();
            } else if (queryStatus.getError().contains("\n" + "Operator FILTER:")) {
                String result = "{\"Error\":\"Privacy issues.\"}";
                encoder.write(ByteBuffer.wrap(result.getBytes()));
                encoder.complete();
                close();
            } else if (queryStatus.getError().contains("\n" + "Operator MINIMUMREC:")) {
                String result = "{\"Error\":\"Privacy issues.Less than 10 patients.\"}";
                encoder.write(ByteBuffer.wrap(result.getBytes()));
                encoder.complete();
                close();
            } else if (queryStatus.getError().contains("\n" + "Operator LARGEBUCKET:")) {
                String result = "{\"Error\":\"Bucket size too big.\"}";
                encoder.write(ByteBuffer.wrap(result.getBytes()));
                encoder.complete();
                close();
            } else if (queryStatus.getError().contains("\n" + "Operator EMPTYFIELD:")) {
                String result = "{\"Error\":\"Fields should not be empty. Please provide a name.\"}";
                encoder.write(ByteBuffer.wrap(result.getBytes()));
                encoder.complete();
                close();
            } else if (queryStatus.getError().contains("\n" + "Operator EMPTYSET:")) {
                String result = "{\"Error\":\"Dataset should not be empty. Please provide one or more dataset(s).\"}";
                encoder.write(ByteBuffer.wrap(result.getBytes()));
                encoder.complete();
                close();
            } else if (queryStatus.getError().contains("\n" + "Operator TYPEBUCKET:")) {
                String result = "{\"Error\":\"Bucket field should be type: Integer.\"}";
                encoder.write(ByteBuffer.wrap(result.getBytes()));
                encoder.complete();
                close();
            } else if (queryStatus.getError().contains("\n" + "Operator TYPEY:")) {
                String result = "{\"Error\":\"Dependent variable should not be type text. Please provide type: Real,Integer or Float.\"}";
                encoder.write(ByteBuffer.wrap(result.getBytes()));
                encoder.complete();
                close();
            } else if (queryStatus.getError().contains("\n" + "Operator TYPEHISTOGRAM:")) {
                String result = "{\"Error\":\"First variable should be type: Real,Integer or Float. Second variable should be empty or type: Text.\"}";
                encoder.write(ByteBuffer.wrap(result.getBytes()));
                encoder.complete();
                close();
            } else if (queryStatus.getError().contains("\n" + "Operator TYPECOLUMNS:")) {
                String result = "{\"Error\":\"Column's type should not be Text. Please provide type: Real,Integer and(or) Float.\"}";
                encoder.write(ByteBuffer.wrap(result.getBytes()));
                encoder.complete();
                close();
            } else if (queryStatus.getError().contains("\n" + "Operator TYPEK:")) {
                String result = "{\"Error\":\"K should be type: Integer.\"}";
                encoder.write(ByteBuffer.wrap(result.getBytes()));
                encoder.complete();
                close();
            } else {
                String result = "{\"Error\":\"Something went wrong.Please inform your system administrator " +
                        "to consult the logs.\"}";
                encoder.write(ByteBuffer.wrap(result.getBytes()));
                encoder.complete();
                close();
            }
        }
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
