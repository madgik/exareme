/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.worker.arm.storage.client.cluster;

import madgik.exareme.worker.arm.storage.client.ArmStorageClient;
import madgik.exareme.worker.arm.storage.client.ArmStorageClientException;
import madgik.exareme.worker.arm.storage.client.utils.ArmStorageClientUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.io.IOUtils;

import java.io.*;
import java.net.URI;

/**
 * @author alex
 */
public class HDFSArmStorageClient implements ArmStorageClient {

    private String armStorageURI;
    private DistributedFileSystem fs;
    private long blocksize;
    private int replication;
    private int buffersize;


    public HDFSArmStorageClient(String armStorageURI, long blocksize, int replication,
                                int buffersize) {
        this.fs = null;
        this.armStorageURI = armStorageURI;
        this.blocksize = blocksize;
        this.replication = replication;
        this.buffersize = buffersize;
    }


    /**
     * @throws ArmStorageClientException
     */
    @Override
    public void connect() throws ArmStorageClientException {

        if (this.fs != null)
            this.disconnect();

        Configuration configuration = new Configuration();
        try {
            URI uri = URI.create(armStorageURI);
            this.fs = (DistributedFileSystem) FileSystem
                    .newInstance(uri, configuration, uri.getUserInfo());
        } catch (IOException ex) {
            throw new ArmStorageClientException("Connect failure : ", ex);
        } catch (InterruptedException ex) {
            throw new ArmStorageClientException("Connect failure : ", ex);
        }
    }

    private void put(String src, String dest, int buffersize, short replication, long blocksize)
            throws ArmStorageClientException {

    }

    /**
     * @param src
     * @param dest
     * @throws ArmStorageClientException
     */
    @Override
    public void put(String src, String dest) throws ArmStorageClientException {

        // connected ?
        if (this.fs == null)
            throw new ArmStorageClientException("Not connected.");

        // validate parameters
        if (src == null || src.isEmpty())
            throw new ArmStorageClientException("No valid src file path!");
        if (dest == null || dest.isEmpty())
            throw new ArmStorageClientException("No valid dest file path!");

        // src file exist?
        File srcFile = new File(src);
        if (!srcFile.exists() || !srcFile.isFile() || srcFile.length() == 0)
            throw new ArmStorageClientException("src file does not exist!");


        // copy !
        FSDataOutputStream out = null;
        InputStream in = null;
        try {
            in = new FileInputStream(srcFile);

            Path destPath = new Path(dest);
            fs.mkdirs(destPath.getParent());

            if (this.fs.exists(destPath))
                throw new ArmStorageClientException("dest file allready exist!");

            //long blocksize = ArmStorageClientUtils.roundFileLength(srcFile.length());
            long currentBlockSize = blocksize == -1 ?
                    ArmStorageClientUtils.roundFileLength(srcFile.length()) :
                    blocksize;
            out = this.fs.create(destPath, true, buffersize, (short) replication, currentBlockSize);
            IOUtils.copyBytes(in, out, buffersize);

        } catch (IOException ex) {
            throw new ArmStorageClientException("Put failure : ", ex);
        } finally {
            IOUtils.closeStream(in);
            IOUtils.closeStream(out);
        }
    }

    /**
     * @param src
     * @param dest
     * @throws ArmStorageClientException
     */
    @Override
    public void fetch(String src, String dest) throws ArmStorageClientException {

        // connected ?
        if (this.fs == null)
            throw new ArmStorageClientException("Not connected.");

        // validate parameters
        if (src == null || src.isEmpty())
            throw new ArmStorageClientException("No valid src file path!");
        if (dest == null || dest.isEmpty())
            throw new ArmStorageClientException("No valid remote file path!");

        // already exists? TODO use /tmp/adp/ location
        File destFile = new File(dest);
        if (destFile.exists())
            return;
        //throw new ArmStorageClientException("dest file allready exists!");

        // copy ! TODO create a link
        OutputStream out = null;
        FSDataInputStream in = null;
        try {
            out = new FileOutputStream(dest);
            in = this.fs.open(new Path(src));
            IOUtils.copyBytes(in, out, buffersize);

        } catch (IOException ex) {
            throw new ArmStorageClientException("Fetch failure : ", ex);
        } finally {
            IOUtils.closeStream(in);
            IOUtils.closeStream(out);
        }
    }

    /**
     * @throws ArmStorageClientException
     */
    public void disconnect() throws ArmStorageClientException {

        // connected ?
        if (this.fs == null)
            return;

        try {
            this.fs.close();
        } catch (IOException ex) {
            throw new ArmStorageClientException("Disconnect failure : ", ex);
        }
    }
}
