package madgik.exareme.master.engine.iterations.handler;

import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.entity.HttpAsyncContentProducer;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import madgik.exareme.master.client.AdpDBClientQueryStatus;
import madgik.exareme.master.connector.DataSerialization;
import madgik.exareme.master.engine.iterations.state.IterativeAlgorithmState;

/**
 * @author Christos Aslanoglou <br> caslanoglou@di.uoa.gr <br> University of Athens / Department of
 *         Informatics and Telecommunications.
 */
public class NIterativeAlgorithmResultEntity extends BasicHttpEntity
        implements HttpAsyncContentProducer {
    private static final Logger log = Logger.getLogger(NIterativeAlgorithmResultEntity.class);

    private IterativeAlgorithmState iterativeAlgorithmState;
    private AdpDBClientQueryStatus finalizeQueryStatus;
    // Used for signifying if IOCtrl of response has been registered with IterativeAlgorithmState.
    private boolean haveRegisteredIOCtrl;
    // Serialization format of the response.
    private DataSerialization dataSerialization;

    // Reading response related fields.
    private final ByteBuffer buffer;
    private ReadableByteChannel channel;

    public NIterativeAlgorithmResultEntity(IterativeAlgorithmState iterativeAlgorithmState,
                                           DataSerialization dataSerialization,
                                           int responseBufferSize) {
        this.iterativeAlgorithmState = iterativeAlgorithmState;
        haveRegisteredIOCtrl = false;
        // Buffer is transmitted to response stream, thus fixed size is needed
        buffer = ByteBuffer.allocate(responseBufferSize);
        channel = null;
        this.dataSerialization = dataSerialization;
    }

    @Override
    public void produceContent(ContentEncoder encoder, IOControl ioctrl) throws IOException {
        if (!haveRegisteredIOCtrl) {
            // Registering IOCtrl to Iterative Algorithm State so as to be notified for
            // algorithm completion event.
            try {
                iterativeAlgorithmState.lock();
                iterativeAlgorithmState.setIoctrl(ioctrl);
                ioctrl.suspendOutput();
                haveRegisteredIOCtrl = true;
            }
            finally {
                iterativeAlgorithmState.releaseLock();
            }
        }
        else {
            try {
                // This method will be called after calling ioctrl.requestOutput() from
                // AlgorithmCompletionEventHandler. Thus, the check below is simply for programming
                // errors.
                iterativeAlgorithmState.lock();
                if (!iterativeAlgorithmState.getAlgorithmCompleted()) {
                    String errMsg = "Attempt to produce response while " +
                            iterativeAlgorithmState.toString()
                            + " is still running.";
                    log.error(errMsg);
                }
                else {
                    // Iterative algorithm is complete, read response table and write it to the
                    // communication channel, if no errors, otherwise write query errors.
                    finalizeQueryStatus = iterativeAlgorithmState.getAdpDBClientFinalizeQueryStatus();
                    if (!finalizeQueryStatus.hasError() &&
                            finalizeQueryStatus.hasFinished()) {
                        if (channel == null) {
                            channel = Channels.newChannel(
                                    iterativeAlgorithmState.getAdpDBClientFinalizeQueryStatus()
                                            .getResult(dataSerialization));
                        }
                        // Reading from the channel to the buffer, flip is required by the API
                        channel.read(buffer);
                        buffer.flip();
                        int i = encoder.write(buffer);
                        final boolean buffering = this.buffer.hasRemaining();
                        this.buffer.compact();
                        if (i < 1 && !buffering) {
                            encoder.complete();
                            close();
                        }
                    }
                    else {
                        encoder.write(ByteBuffer.wrap(
                                finalizeQueryStatus.getError().getBytes()));
                        encoder.complete();
                    }
                }
            }
            finally {
                iterativeAlgorithmState.releaseLock();
            }
        }
    }

    @Override
    public void close() throws IOException {
        finalizeQueryStatus.close();
        iterativeAlgorithmState = null;
    }
}
