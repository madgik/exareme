/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.net;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author heraldkllapi
 */
public class NetUtilTest extends TestCase {

    private static long ipPart(int part) {
        return (long) Math.pow(256, part - 1);
    }

    public void testIPLong() {
        Assert.assertEquals(0, NetUtil.getIPLongRepresentation("0.0.0.0"));
        Assert.assertEquals(1, NetUtil.getIPLongRepresentation("0.0.0.1"));
        Assert.assertEquals(1 * ipPart(4), NetUtil.getIPLongRepresentation("1.0.0.0"));
        Assert.assertEquals(127 * ipPart(4) + 1 * ipPart(1),
            NetUtil.getIPLongRepresentation("127.0.0.1"));
    }
}
