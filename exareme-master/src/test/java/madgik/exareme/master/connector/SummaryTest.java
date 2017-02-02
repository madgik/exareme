package madgik.exareme.master.connector;

import org.junit.Test;

import java.io.ByteArrayOutputStream;

import madgik.exareme.master.queryProcessor.composer.ComposerConstants;

/**
 * Created by alex on 11/02/16.
 */
public class SummaryTest {

    @Test public void testCategorical() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        AdpDBConnectorUtil.readLocalTablePart(
            "input1",
            0,
            ComposerConstants.demoDbWorkingDirectory ,
            null,
            DataSerialization.summary,
            outputStream);

        System.out.print(outputStream.toString());
    }
}
