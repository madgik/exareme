/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.buffer;

import java.io.IOException;

/**
 * @author herald
 */
public interface StreamBuffer {

    void write(byte[] bytes, int offset, int length) throws IOException;

    int read(byte[] bytes, int offset, int length) throws IOException;

    void closeReader() throws IOException;

    void closeWriter() throws IOException;

    int getSize() throws IOException;

    void clear();
}
