/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.file;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * @author herald
 */
public class FileUtilTest extends TestCase {

    private static Logger log = Logger.getLogger(FileUtilTest.class);

    public FileUtilTest() {
    }

    /**
     * Test of writeToStream method, of class FileUtil.
     */
    public void testWriteToANDReadFromStream() throws Exception {
        // Create a 3MB string.
        StringBuilder contents = new StringBuilder();
        for (int i = 0; i < 1024; i++) {
            contents.append("abc");
        }

        File file = File.createTempFile("Test", "txt");
        //    file.deleteOnExit();

        // Write the contents
        FileUtil.writeFile(contents.toString(), file);

        // Write the contents of the file to the stream
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        FileUtil.writeToStream(file, out);

        log.debug("Bytes are : " + out.toByteArray().length);

        // Read
        File file2 = File.createTempFile("Test2", "txt");
        //    file2.deleteOnExit();

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        FileUtil.readFromStream(in, file2);

        String after = FileUtil.readFile(file2).trim();

        Assert.assertEquals(contents.toString(), after);
    }
}
