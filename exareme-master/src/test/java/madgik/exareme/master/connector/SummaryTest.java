package madgik.exareme.master.connector;

import org.junit.Test;

import java.io.ByteArrayOutputStream;

import madgik.exareme.common.consts.HBPConstants;

/**
 * Created by alex on 11/02/16.
 */
public class SummaryTest {

    @Test
    public void testCategorical() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        AdpDBConnectorUtil.readLocalTablePart(
                "input1",
                0,
                HBPConstants.DEMO_DB_WORKING_DIRECTORY,
                null,
                DataSerialization.summary,
                outputStream);

        System.out.print(outputStream.toString());
    }
}
