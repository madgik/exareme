/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.string;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author heraldkllapi
 */
public class StringUtilsTest extends TestCase {

    public StringUtilsTest() {
    }

    /**
     * Test of concatenateUnique method, of class StringUtils.
     */
    public void testConcatenateUnique() {
        String[] strings = {"aa", "bbb", "c"};
        String expResult = "2|aa3|bbb1|c";
        String result = StringUtils.concatenateUnique(strings);
        Assert.assertEquals(expResult, result);
    }

    public void testnormalizeSQLQuery() {
        String query = "select  *   from       table;";
        String expResult = "select * from table;";
        String result = StringUtils.normalizeSQLQuery(query);
        Assert.assertEquals(expResult, result);
    }
}
