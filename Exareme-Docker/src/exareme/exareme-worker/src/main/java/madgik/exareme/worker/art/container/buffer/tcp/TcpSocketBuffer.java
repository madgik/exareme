/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.buffer.tcp;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.utils.net.NetUtil;
import madgik.exareme.worker.art.container.buffer.SocketBuffer;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.ServerException;

/**
 * @author heraldkllapi
 */
public class TcpSocketBuffer implements SocketBuffer {

    private static Logger log = Logger.getLogger(TcpSocketBuffer.class);
    private ServerSocket serverSocket = null;

    public TcpSocketBuffer() {

    }

    private void openSocketAndGetPort() throws RemoteException {
        if (serverSocket != null) {
            return;
        }
        long begin = System.currentTimeMillis();
        try {
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.setSoTimeout(0);
            serverSocket.bind(new InetSocketAddress(NetUtil.getIPv4(), 0));
            log.debug("Buffer listening on port: " + serverSocket.getLocalPort());
        } catch (Exception e) {
            throw new ServerException("Cannot open server", e);
        } finally {
            long end = System.currentTimeMillis();
            log.debug("Total time to open server: " + (end - begin));
        }
    }

    @Override
    public EntityName getNetEntityName() throws RemoteException {
        openSocketAndGetPort();
        return new EntityName("", NetUtil.getIPv4(), serverSocket.getLocalPort());
    }

    @Override
    public void close() throws RemoteException {
        try {
            if (serverSocket != null) {
                log.debug("Buffer closing on port: " + serverSocket.getLocalPort());
                serverSocket.close();
                serverSocket = null;
            }
        } catch (Exception e) {
            throw new ServerException("Cannot close server", e);
        }
    }

    @Override
    public Socket openServerConnection() throws RemoteException {
        try {
            openSocketAndGetPort();
            return serverSocket.accept();
        } catch (Exception e) {
            throw new ServerException("Cannot accept connections", e);
        }
    }
}
