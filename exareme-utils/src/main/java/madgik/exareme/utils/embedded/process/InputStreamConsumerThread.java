package madgik.exareme.utils.embedded.process;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author herald
 */
public class InputStreamConsumerThread extends Thread {
  private static final int ioBufferSize = 4096;
  private static final Logger log = Logger.getLogger(MadisProcess.class);
  private final InputStream inputStream;
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
    StringBuilder getMsg = new StringBuilder();
    try {
      byte[] buffer = new byte[ioBufferSize];
      int length;
      while ((length = inputStream.read(buffer)) > 0) {
        if (print == false) {
          continue;
        }
        for (int i = 0; i < length; i++) {
          getMsg.append((char) buffer[i]);
        }
        if (length > 0) {
          exception = new Exception(getMsg.toString());
        }
      }
      inputStream.close();
    } catch (IOException e) {
      exception = e;
      log.error(e.toString(), e);
    }
  }

  public Exception getException() {
    return exception;
  }
}
