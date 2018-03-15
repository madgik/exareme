package madgik.exareme.master.gateway.async;

import madgik.exareme.master.engine.AdpDBManager;
import madgik.exareme.master.gateway.ExaremeGateway;
import madgik.exareme.master.gateway.ExaremeGatewayUtils;
import madgik.exareme.master.gateway.async.handler.*;
import madgik.exareme.master.gateway.control.handler.HttpAsyncRemoveWorkerHandler;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.nio.DefaultHttpServerIODispatch;
import org.apache.http.impl.nio.reactor.DefaultListeningIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.protocol.HttpAsyncService;
import org.apache.http.nio.protocol.UriHttpAsyncRequestHandlerMapper;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.ListenerEndpoint;
import org.apache.http.nio.reactor.ListeningIOReactor;
import org.apache.http.protocol.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Simple Non-Blocking Streaming HTTP Server.
 *
 * @author alex
 * @since 0.1
 */
public class HttpAsyncExaremeGateway implements ExaremeGateway {

  private static final Logger log = Logger.getLogger(HttpAsyncExaremeGateway.class);
  private final AsyncHttpListener listener;
  private final AsyncHttpListener controlListener;

  public HttpAsyncExaremeGateway(AdpDBManager manager) throws Exception {

    final IOReactorConfig reactorConfig = IOReactorConfig.custom()
        .setIoThreadCount(1)
        .setSoKeepAlive(true)
        .setSoReuseAddress(true)
        .setTcpNoDelay(ExaremeGatewayUtils.GW_NODELAY)
        .build();

    final ListeningIOReactor ioReactor = new DefaultListeningIOReactor(reactorConfig);
    final ConnectionConfig connectionConfig = ConnectionConfig.custom()
        .setBufferSize(ExaremeGatewayUtils.GW_BUFFERSIZE_KB)
        .setFragmentSizeHint(ExaremeGatewayUtils.GW_BUFFERSIZE_KB)
        .build();

    final HttpProcessor httpproc = new ImmutableHttpProcessor(
        new ResponseDate(),
        new ResponseServer("ExaremeGateway/0.1"),
        new ResponseContent(),
        new ResponseConnControl()
    );

    final UriHttpAsyncRequestHandlerMapper registry = new UriHttpAsyncRequestHandlerMapper();
    registry.register(ExaremeGatewayUtils.GW_API_FILE, new HttpAsyncFileHandler());
    registry.register(ExaremeGatewayUtils.GW_API_QUERY, new HttpAsyncQueryHandler());
    registry.register(ExaremeGatewayUtils.GW_API_TABLE, new HttpAsyncTableHandler());
    registry.register(ExaremeGatewayUtils.GW_API_MINING_ALGORITHMS, new HttpAsyncMiningAlgorithmsHandler());
    registry.register(ExaremeGatewayUtils.GW_API_MINING_QUERY, new HttpAsyncMiningQueryHandler());
    registry.register("/v1/mining/*", new HttpAsyncMiningOldHandler());

    final HttpAsyncService handler = new HttpAsyncService(httpproc, null, null, registry, null, null);

    final IOEventDispatch ioEventDispatch =
        new DefaultHttpServerIODispatch(handler, connectionConfig);

    this.listener = new AsyncHttpListener(ioReactor, ioEventDispatch);

    //Control Listener
    final IOReactorConfig reactorConfigC = IOReactorConfig.custom()
            .setIoThreadCount(1)
            .setSoKeepAlive(true)
            .setSoReuseAddress(true)
            .setTcpNoDelay(ExaremeGatewayUtils.GW_NODELAY)
            .build();

    final ListeningIOReactor ioReactorC = new DefaultListeningIOReactor(reactorConfig);
    final HttpProcessor httpprocControl = new ImmutableHttpProcessor(
            new ResponseDate(),
            new ResponseServer("ExaremeControlGateway/0.1"),
            new ResponseContent(),
            new ResponseConnControl()
    );

    final UriHttpAsyncRequestHandlerMapper controlRegistry = new UriHttpAsyncRequestHandlerMapper();
    controlRegistry.register("/remove/worker", new HttpAsyncRemoveWorkerHandler());
    final HttpAsyncService controlHandler = new HttpAsyncService(httpprocControl, null, null,
            controlRegistry, null, null);

    final IOEventDispatch ioEventDispatchC =
            new DefaultHttpServerIODispatch(controlHandler, connectionConfig);

    this.controlListener = new AsyncHttpListener(ioReactorC, ioEventDispatchC);

  }

  @Override
  public String getName() {
    return HttpAsyncExaremeGateway.class.getSimpleName();
  }

  @Override
  public int getPort() {
    return ExaremeGatewayUtils.GW_PORT;
  }

  @Override
  public void start() throws Exception {
    log.trace("Starting...");
    this.listener.start();
    this.listener.listen(new InetSocketAddress(ExaremeGatewayUtils.GW_PORT));
    log.trace("listens on " + ExaremeGatewayUtils.GW_PORT);
    this.controlListener.start();
    this.controlListener.listen(new InetSocketAddress(ExaremeGatewayUtils.GW_CONTROL_PORT));
    log.trace("Control listens on "+ ExaremeGatewayUtils.GW_CONTROL_PORT);
  }

  @Override
  public boolean isUp() {
    if (listener == null)
      return false;
    return listener.isAlive();
  }

  @Override
  public void stop() {
    log.trace("Terminating...");
    // stop gateway
    this.listener.terminate();
    try {
      this.listener.awaitTermination(ExaremeGatewayUtils.GW_WAIT_TERM_SEC * 1000);
    } catch (final InterruptedException e) {
      log.error("Unable to stop gateway.", e);
    }

    if (this.listener.getException() != null) {
      log.error("Gateway exception.", this.listener.getException());
    }
    this.controlListener.terminate();
    try {
      this.controlListener.awaitTermination(ExaremeGatewayUtils.GW_WAIT_TERM_SEC * 1000);
    } catch (final InterruptedException e) {
      log.error("Unable to stop gateway.", e);
    }

    if (this.controlListener.getException() != null) {
      log.error("Gateway exception.", this.listener.getException());
    }
  }


  /**
   * Main listener Thread.
   *
   * @author alex
   * @since 0.1
   */
  private class AsyncHttpListener extends Thread {
    private final Logger log = Logger.getLogger(AsyncHttpListener.class);

    private final ListeningIOReactor ioreactor;
    private final IOEventDispatch ioEventDispatch;
    private volatile Exception exception;

    public AsyncHttpListener(final ListeningIOReactor ioreactor,
                             final IOEventDispatch ioEventDispatch) {
      super();
      this.ioreactor = ioreactor;
      this.ioEventDispatch = ioEventDispatch;
    }

    @Override
    public void run() {
      try {
        this.ioreactor.execute(this.ioEventDispatch);
      } catch (final Exception ex) {
        this.exception = ex;
      }
    }

    public void listen(final InetSocketAddress address) throws InterruptedException {
      final ListenerEndpoint endpoint = this.ioreactor.listen(address);
      endpoint.waitFor();
    }

    public void terminate() {
      try {
        this.ioreactor.shutdown();
      } catch (final IOException e) {
        log.error("Unable to terminate listener.", e);
      }
    }

    public Exception getException() {
      return this.exception;
    }

    public void awaitTermination(final long millis) throws InterruptedException {
      this.join(millis);
    }
  }

}
