/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.gateway;

/**
 * Exareme Gateway interface provided
 * for blocking and non blocking implementations.
 *
 * @author alex
 * @since 0.1
 */
public interface ExaremeGateway {

    String getName();

    int getPort();

    void start() throws Exception;

    boolean isUp();

    void stop();
}
