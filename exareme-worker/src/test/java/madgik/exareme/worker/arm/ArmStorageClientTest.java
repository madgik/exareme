package madgik.exareme.worker.arm;//package madgik.exareme.worker.arm;
//
//import org.apache.hadoop.conf.Configuration;
//import org.apache.hadoop.fs.FSDataOutputStream;
//import org.apache.hadoop.fs.FileSystem;
//import org.apache.hadoop.fs.Path;
//import org.apache.hadoop.fs.permission.FsPermission;
//import org.apache.hadoop.hdfs.DistributedFileSystem;
//import org.apache.hadoop.io.IOUtils;
//import org.junit.*;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.net.URI;
//
///**
// * @author alex
// */
//public class ArmStorageClientTest {
//
//    public ArmStorageClientTest() {
//    }
//
//    @BeforeClass public static void setUpClass() {
//    }
//
//    @AfterClass public static void tearDownClass() {
//    }
//
//    @Before public void setUp() {
//    }
//
//    @After public void tearDown() {
//    }
//
//    @Test public void test() {
//
//        String uri = "hdfs://83.212.85.247:8020";
//        File srcFile = new File("/tmp/2K-data.random");
//
//        System.out.println("URI : " + uri);
//        DistributedFileSystem fileSystem = null;
//        FileInputStream in = null;
//        FSDataOutputStream out = null;
//
//        try {
//            // get fs
//            fileSystem = (DistributedFileSystem) FileSystem
//                .get(URI.create(uri), new Configuration(), "hadoop");
//            System.out.println("DFS used : " + fileSystem.getUsed());
//            long l = System.currentTimeMillis();
//
//            // upload
//            String filename = srcFile.getAbsolutePath() + "." + String.valueOf(l);
//
//            out = fileSystem
//                .create(new Path(filename), FsPermission.getDefault(), true, 4096, (short) 3, 4096L,
//                    null, null);
//            in = new FileInputStream(srcFile);
//            IOUtils.copyBytes(in, out, 4096);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } finally {
//            IOUtils.closeStream(in);
//            IOUtils.closeStream(out);
//        }
//
//    }
//}
