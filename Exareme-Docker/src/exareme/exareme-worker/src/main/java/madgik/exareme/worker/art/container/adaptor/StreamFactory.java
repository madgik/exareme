/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptor;

import madgik.exareme.worker.art.container.buffer.StreamBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author herald
 */
public class StreamFactory {

    //  private static boolean ZIP_STREAM = false;
    //  private static int ZIP_LEVEL = 2;

    private StreamFactory() {
    }

    public static OutputStream createOutputStream(final WriteRmiStreamAdaptor buf)
            throws IOException {
        OutputStream out = new OutputStream() {

            @Override
            public synchronized void write(int b) throws IOException {
                buf.write(new byte[]{(byte) (b & 0xFF)}, 0, 1);
            }

            @Override
            public synchronized void write(byte[] b) throws IOException {
                buf.write(b, 0, b.length);
            }

            @Override
            public synchronized void write(byte[] bytes, int off, int len)
                    throws IOException {
                buf.write(bytes, off, len);
            }

            @Override
            public void close() throws IOException {
                super.close();
                buf.close();
            }
        };

        //    if (ZIP_STREAM) {
        //      return new DeflaterOutputStream(
        //              out, new Deflater(ZIP_LEVEL), Constants.BUFFER_SIZE);
        //    } else {
        return out;
        //    }
    }

    public static OutputStream createOutputStream(final StreamBuffer buf) throws IOException {
        OutputStream out = new OutputStream() {

            @Override
            public synchronized void write(int b) throws IOException {
                buf.write(new byte[]{(byte) (b & 0xFF)}, 0, 1);
            }

            @Override
            public synchronized void write(byte[] b) throws IOException {
                buf.write(b, 0, b.length);
            }

            @Override
            public synchronized void write(byte[] bytes, int off, int len)
                    throws IOException {
                buf.write(bytes, off, len);
            }

            @Override
            public void close() throws IOException {
                super.close();
                buf.closeWriter();
            }
        };

        //    if (ZIP_STREAM) {
        //      return new DeflaterOutputStream(
        //              out, new Deflater(ZIP_LEVEL), Constants.BUFFER_SIZE);
        //    } else {
        return out;
        //    }
    }

    public static InputStream createInputStream(final ReadRmiStreamAdaptor buf) throws IOException {
        InputStream in = new InputStream() {

            @Override
            public synchronized int read() throws IOException {
                byte[] b = new byte[1];
                int bytes = this.read(b, 0, b.length);

                if (bytes < 0) {
                    return bytes;
                }

                return ((int) b[0] & 0xFF);
            }

            @Override
            public synchronized int read(byte[] b) throws IOException {
                return this.read(b, 0, b.length);
            }

            @Override
            public synchronized int read(byte[] bytes, int off, int len)
                    throws IOException {
                try {
                    byte[] ret = buf.read(len);
                    if (ret == null) {
                        return -1;
                    }

                    System.arraycopy(ret, 0, bytes, off, ret.length);

                    return ret.length;
                } catch (Exception e) {
                    //		    e.printStackTrace();
                    return -1;
                }
            }

            @Override
            public void close() throws IOException {
                super.close();
                buf.close();
            }
        };

        //    if (ZIP_STREAM) {
        //      return new InflaterInputStream(in, new Inflater(), Constants.BUFFER_SIZE);
        //    } else {
        return in;
        //    }
    }

    public static InputStream createInputStream(final StreamBuffer buf) throws IOException {
        InputStream in = new InputStream() {

            @Override
            public synchronized int read() throws IOException {
                byte[] b = new byte[1];
                int bytes = this.read(b, 0, b.length);

                if (bytes < 0) {
                    return bytes;
                }

                return ((int) b[0] & 0xFF);
            }

            @Override
            public synchronized int read(byte[] b) throws IOException {
                return this.read(b, 0, b.length);
            }

            @Override
            public synchronized int read(byte[] bytes, int off, int len)
                    throws IOException {

                try {
                    return buf.read(bytes, off, len);
                } catch (Exception e) {
                    //          e.printStackTrace();
                    return -1;
                }
            }

            @Override
            public void close() throws IOException {
                super.close();
                buf.closeReader();
            }
        };

        //    if (ZIP_STREAM) {
        //      return new InflaterInputStream(in, new Inflater(), Constants.BUFFER_SIZE);
        //    } else {
        return in;
        //    }
    }
}
