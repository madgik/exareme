package madgik.exareme.master.gateway;

import java.io.InputStream;

/**
 * @author alex
 */
public interface ExaremeGatewayClient {

    InputStream query(String database, String query);

}
