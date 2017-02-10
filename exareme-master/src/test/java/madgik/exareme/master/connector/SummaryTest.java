//package madgik.exareme.master.connector;
//
//import madgik.exareme.master.client.TestAdpDBClient;
//import madgik.exareme.utils.file.FileUtil;
//import org.junit.Test;
//
//import java.io.BufferedOutputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.OutputStream;
//
///**
// * Created by alex on 11/02/16.
// */
//public class SummaryTest {
//
//    @Test public void testCategorical() throws Exception {
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        AdpDBConnectorUtil.readLocalTablePart(
//            "input1",
//            0,
//            "/tmp/demo/",
//            null,
//            DataSerialization.summary,
//            outputStream);
//
//        System.out.print(outputStream.toString());
//    }
//}
