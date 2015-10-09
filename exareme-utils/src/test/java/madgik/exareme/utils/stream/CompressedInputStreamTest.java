///**
// * Copyright MaDgIK Group 2010 - 2015.
// */
//package madgik.exareme.db.util.stream;
//
//import java.io.ByteArrayInputStream;
//import java.io.InputStream;
//import FileUtil;
//import net.jpountz.lz4.CompressedInputStream;
//import org.junit.After;
//import org.junit.AfterClass;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import static org.junit.Assert.*;
//
///**
// *
// * @author Herald Kllapi
// */
//public class CompressedInputStreamTest {
//  
//  public CompressedInputStreamTest() {
//  }
//  
//  @BeforeClass
//  public static void setUpClass() {
//  }
//  
//  @AfterClass
//  public static void tearDownClass() {
//  }
//  
//  @Before
//  public void setUp() {
//  }
//  
//  @After
//  public void tearDown() {
//  }
//
//  @org.junit.Test
//  public void testCompressedStream() throws Exception {
//    // Original data
//    ByteArrayInputStream input = new ByteArrayInputStream("Hello World! How are you doing?!".getBytes());
//    // Compress
//    CompressedInputStream cInput = new CompressedInputStream(input);
//    // Decompress
//    InputStream dInput = StreamUtil.createZippedInputStream(cInput);
//
//    String data = FileUtil.consume(dInput);
//    System.out.println(data);
//  }
//}
