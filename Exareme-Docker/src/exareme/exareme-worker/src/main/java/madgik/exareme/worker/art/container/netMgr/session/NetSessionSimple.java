/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.netMgr.session;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.utils.stream.StreamUtil;
import madgik.exareme.worker.art.container.buffer.SocketBuffer;
import madgik.exareme.worker.art.container.netMgr.NetSession;
import madgik.exareme.worker.art.remote.RetryPolicy;
import madgik.exareme.worker.art.remote.RetryPolicyFactory;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.rmi.RemoteException;

/**
 * @author herald
 */
public class NetSessionSimple implements NetSession {
    private static final Logger log = Logger.getLogger(NetSessionSimple.class);

    public NetSessionSimple() {
    }

    @Override
    public InputStream openInputStream(EntityName netAddress) throws RemoteException {
        try {
            Socket socket = connect(netAddress);
            return StreamUtil.createZippedInputStream(socket.getInputStream());
        } catch (IOException e) {
            throw new RemoteException("Cannot open stream", e);
        }
    }

    @Override
    public OutputStream openOutputStream(SocketBuffer socket) throws RemoteException {
        try {
            return StreamUtil
                    .createZippedOutputStream(socket.openServerConnection().getOutputStream());
        } catch (IOException e) {
            throw new RemoteException("Cannot open stream", e);
        }
    }

    @Override
    public InputStream openInputStream(SocketBuffer socket) throws RemoteException {
        try {
            return StreamUtil
                    .createZippedInputStream(socket.openServerConnection().getInputStream());
        } catch (IOException e) {
            throw new RemoteException("Cannot open stream", e);
        }
    }

    @Override
    public OutputStream openOutputStream(EntityName netAddress) throws RemoteException {
        try {
            Socket socket = connect(netAddress);
            return StreamUtil.createZippedOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new RemoteException("Cannot open stream", e);
        }
    }

    private Socket connect(EntityName netAddress) throws RemoteException {
        int tries = 0;
        long begin = System.currentTimeMillis();
        try {
            InetSocketAddress client =
                    new InetSocketAddress(netAddress.getIP(), netAddress.getPort());
            log.trace("Connecting to (" + tries + ") " +
                    netAddress.getIP() + ":" + netAddress.getPort() + " ...");
            RetryPolicy retryPolicy = RetryPolicyFactory.socketRetryPolicy();
            while (true) {
                try {
                    // TODO(herald): fix the following.
                    tries++;
                    Socket socket = new Socket();
                    socket.connect(client, 1000);
                    log.debug("ok!");
                    return socket;
                } catch (Exception e) {
                    log.trace("Connecting to (" + tries + ") " +
                            netAddress.getIP() + ":" + netAddress.getPort() + " ...");
                    if (retryPolicy.getRetryTimesPolicy().retry(e, tries) == false) {
                        break;
                    }
                    Thread.sleep(retryPolicy.getRetryTimeInterval().getTime(tries));
                }
            }
        } catch (Exception e) {
            throw new RemoteException("Cannot open stream", e);
        } finally {
            long end = System.currentTimeMillis();
            log.debug("Total time to connect: " + (end - begin));
        }
        throw new RemoteException("Cannot connect to " +
                netAddress.getIP() + ":" + netAddress.getPort() + " ...");
    }
}
