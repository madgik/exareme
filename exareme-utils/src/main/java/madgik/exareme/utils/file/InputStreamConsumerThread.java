package madgik.exareme.utils.file;

import madgik.exareme.utils.properties.AdpProperties;
import madgik.exareme.utils.units.Metrics;

import java.io.InputStream;

/**
 * @author herald
 */
public class InputStreamConsumerThread extends Thread {

    private static final int ioBufferSize =
            AdpProperties.getArtProps().getInt("art.container.ioBufferSize_kb") * Metrics.KB;
    private final InputStream inputStream;
    private StringBuffer output = new StringBuffer();
    private boolean print = false;
    private Exception exception = null;

    public InputStreamConsumerThread(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public InputStreamConsumerThread(InputStream inputStream, boolean print) {
        this.inputStream = inputStream;
        this.print = print;
    }

    @Override
    public void run() {
        try {
            byte[] buffer = new byte[ioBufferSize];
            int length = 0;
            while ((length = inputStream.read(buffer)) > 0) {
                for (int i = 0; i < length; i++) {
                    synchronized (inputStream) {
                        output.append((char) buffer[i]);
                    }

                    if (print) {
                        System.err.print((char) buffer[i]);
                    }
                }
            }

            inputStream.close();
        } catch (Exception e) {
            exception = e;
        }
    }

    public String getOutput() {
        synchronized (inputStream) {
            return output.toString();
        }
    }

    public Exception getException() {
        return exception;
    }
}
