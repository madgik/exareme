/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.stream;

import java.util.zip.Checksum;

/**
 * @author heraldkllapi
 */
public class NoChecksum implements Checksum {
    @Override
    public void update(int b) {
    }

    @Override
    public void update(byte[] b, int off, int len) {
    }

    @Override
    public long getValue() {
        return 0;
    }

    @Override
    public void reset() {
    }
}
