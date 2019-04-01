/*
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.remoteQuery.impl.utility;

import madgik.exareme.master.engine.remoteQuery.impl.cache.CachedDataInfo;
import madgik.exareme.master.engine.remoteQuery.impl.cache.implementation.lru.ListNode;
import madgik.exareme.master.engine.remoteQuery.impl.doublyLinkedList.Node;
import madgik.exareme.master.engine.remoteQuery.impl.metadata.Metadata;
import madgik.exareme.utils.association.Pair;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * @author Christos Mallios <br>
 * University of Athens / Department of Informatics and Telecommunications.
 */
public class Files {

    private static final Logger log = Logger.getLogger(Files.class);

    /*
     * Function which deletes the file which contains a database
     * along with it's soft link
     */
    public static void deleteDB(Node lruNode, Metadata metadata) throws IOException, SQLException {

        File file = new File(((ListNode) lruNode.value).cacheInfo.storagePath);
        file.delete();

        String command = "unlink " + ((ListNode) lruNode.value).cacheInfo.getCachePath();

        Process process = Runtime.getRuntime().exec(command);

        metadata.deleteCacheRecord(((ListNode) lruNode.value).cacheInfo.database);
    }

    /*
     * Function which deletes the file which contains a database
     * along with it's soft link
     */
    public static void deleteDB(CachedDataInfo info, Metadata metadata)
            throws IOException, SQLException {

        File file = new File(info.storagePath);
        file.delete();

        String command = "unlink " + info.getCachePath();

        Process process = Runtime.getRuntime().exec(command);

        metadata.deleteCacheRecord(info.database);
    }

    /*
     * Function which deletes the file which contains a database
     * along with it's soft link
     */
    public static void deleteDBFile(String storageDir, String cacheDir, String filename)
            throws IOException, SQLException {

        File file = new File(storageDir + "/" + filename);
        file.delete();

        String command = "unlink " + cacheDir + "/" + filename;

        Process process = Runtime.getRuntime().exec(command);
    }

    public static void deleteFile(String directory, String fileName) {

        File file = new File(directory + "/" + fileName);
        file.delete();
    }

    /*
     * Function which creates a new directory
     */
    public static File createDir(String directory) throws Exception {

        boolean created;

        //Creation of storage directory
        File fileDirectory = new File(directory);
        if (!fileDirectory.exists()) {

            created = fileDirectory.mkdir();
            if (!created) {
                throw new Exception("The directory's creation was failed");
            }
        }
        return fileDirectory;
    }

    /*
     * function which returns the files of the given directory
     */
    public static File[] getFilesOfdirectory(String directory) {

        try {
            File dir = new File(directory);
            if (!dir.isDirectory()) {
                return null;
            }
            return dir.listFiles();
        } catch (Exception ex) {
            return null;
        }
    }

    public static Pair<String, String> getDirAndFile(String fullPath) {

        int pos = fullPath.lastIndexOf('/');

        String dir = fullPath.substring(0, pos);
        String file = fullPath.substring(pos + 1, fullPath.length());

        return new Pair(dir, file);
    }

    public static void main(String[] args) {

        Pair<String, String> pair = getDirAndFile("/home/christos/demo/my.sql");

        System.out.println("dir " + pair.a);
        System.out.println("file " + pair.b);

    }

}
