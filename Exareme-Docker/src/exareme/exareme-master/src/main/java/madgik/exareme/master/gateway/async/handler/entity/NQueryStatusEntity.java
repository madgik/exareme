package madgik.exareme.master.gateway.async.handler.entity;

import madgik.exareme.common.app.engine.AdpDBQueryID;
import madgik.exareme.common.app.engine.AdpDBQueryListener;
import madgik.exareme.common.app.engine.AdpDBStatus;
import madgik.exareme.master.client.AdpDBClientQueryStatus;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.entity.HttpAsyncContentProducer;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * TODO flush output before suspend
 *
 * @author alex
 * @since 0.1
 */
public class NQueryStatusEntity extends BasicHttpEntity implements HttpAsyncContentProducer {

    private static final Logger log = Logger.getLogger(NQueryStatusEntity.class);
    private static final String msg = "[\"Successfully executed in %s.\"]\n";

    private final AdpDBClientQueryStatus queryStatus;
    private boolean schemaWritten = false;


    public NQueryStatusEntity(AdpDBClientQueryStatus queryStatus) {
        super();
        this.queryStatus = queryStatus;
    }

    @Override
    public void produceContent(ContentEncoder encoder, IOControl ioctrl)
            throws IOException {
        // header
        if (!schemaWritten) {
            encoder.write(ByteBuffer.wrap("{\"schema\":[[\"status\", \"null\"]]}\n".getBytes()));
            schemaWritten = true;

            queryStatus.registerListener(new QueryStatusListener(ioctrl));
            ioctrl.suspendOutput();
            return;
        }

        // status changed
        if (queryStatus.hasFinished() == false && queryStatus.hasError() == false) {
            encoder.write(
                    ByteBuffer.wrap(String.format("[\"%s\"]\n", queryStatus.getStatus()).getBytes()));
            ioctrl.suspendOutput();
            return;
        }

        // terminated
        if (queryStatus.hasError() == false) {
            encoder.write(
                    ByteBuffer.wrap(String.format("[\"%s\"]\n", queryStatus.getStatus()).getBytes()));
            encoder.write(
                    ByteBuffer.wrap(String.format(msg, queryStatus.getExecutionTime()).getBytes()));
        } else {
            encoder.write(
                    ByteBuffer.wrap(String.format("[\"%s\"]\n", queryStatus.getError()).getBytes()));
        }
        encoder.complete();

    }

    @Override
    public void close() {

    }

    public static class QueryStatusListener implements AdpDBQueryListener {
        private static final Logger log = Logger.getLogger(QueryStatusListener.class);
        private IOControl ioctl = null;

        public QueryStatusListener(IOControl ioctrl) {
            this.ioctl = ioctrl;
        }

        @Override
        public void statusChanged(AdpDBQueryID queryID, AdpDBStatus status) {
            ioctl.requestOutput();
        }

        @Override
        public void terminated(AdpDBQueryID queryID, AdpDBStatus status) {
            ioctl.requestOutput();
        }
    }
}
