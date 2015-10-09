/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.locking.file;

import madgik.exareme.master.engine.locking.AdpDBLocking;
import madgik.exareme.master.engine.locking.AdpDBTablePartKey;
import madgik.exareme.utils.properties.AdpDBProperties;
import madgik.exareme.utils.properties.AdpProperties;

import java.io.File;
import java.io.RandomAccessFile;
import java.rmi.RemoteException;
import java.rmi.ServerException;

/**
 * @author herald
 */
public class AdpDBFileLocking implements AdpDBLocking {

    private File adpDBRootFile = null;

    public AdpDBFileLocking() {
        String adpDBRootFileName =
            AdpProperties.getArtProps().getString("art.container.diskRoot") + AdpDBProperties
                .getAdpDBProps().getString("db.scheduler.pathRoot");

        adpDBRootFile = new File(adpDBRootFileName);
        adpDBRootFile.mkdirs();
    }

    public AdpDBTablePartKey getSharedKey(String database, String tableName, int part)
        throws RemoteException {
        try {
            File databaseDir = new File(adpDBRootFile, database.replaceAll("/", "_"));
            databaseDir.mkdirs();
            RandomAccessFile fileLock =
                new RandomAccessFile(new File(databaseDir, tableName + "_" + part), "rw");

            return new AdpDBTablePartFileLock(fileLock.getChannel().lock(0, Long.MAX_VALUE, true));
        } catch (Exception e) {
            throw new ServerException("Cannot lock part: " + tableName + "@" + part, e);
        }
    }

    public AdpDBTablePartKey getExclusiveKey(String database, String tableName, int part)
        throws RemoteException {
        try {
            File databaseDir = new File(adpDBRootFile, database.replaceAll("/", "_"));
            databaseDir.mkdirs();
            RandomAccessFile fileLock =
                new RandomAccessFile(new File(databaseDir, tableName + "_" + part), "rw");

            return new AdpDBTablePartFileLock(fileLock.getChannel().lock(0, Long.MAX_VALUE, false));
        } catch (Exception e) {
            throw new ServerException("Cannot lock part: " + tableName + "@" + part, e);
        }
    }
}
