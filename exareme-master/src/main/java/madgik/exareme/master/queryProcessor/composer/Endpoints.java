package madgik.exareme.master.queryProcessor.composer;

import madgik.exareme.utils.embedded.db.DBUtils;
import madgik.exareme.utils.embedded.db.SQLDatabase;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author alex
 */
public class Endpoints {
  private static final Logger log = Logger.getLogger(Endpoints.class);
  private static final String engine = System.getProperty("EXAREME_MADIS");

  private Endpoint[] endpoints;

  public static class Endpoint {
    private String name;
    private String desc;
    private String host;
    private String port;
    private String path;
    private String status;

    public Endpoint() {
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getDesc() {
      return desc;
    }

    public String getPath() {
      return path;
    }

    public void setPath(String path) {
      this.path = path;
    }

    public void setDesc(String desc) {
      this.desc = desc;
    }

    public String getHost() {
      return host;
    }

    public void setHost(String host) {
      this.host = host;
    }

    public String getPort() {
      return port;
    }

    public void setPort(String port) {
      this.port = port;
    }

    public String getStatus() {
      return status;
    }

    public void setStatus(String status) {
      this.status = status;
    }
  }

  public Endpoint[] getEndpoints() {
    return endpoints;
  }

  public void setEndpoints(Endpoint[] endpoints) {
    this.endpoints = endpoints;
  }

  public static Endpoints createEndpoints(String endpointsPropertyFilePath) throws IOException {

    File propertyFile = new File(endpointsPropertyFilePath);
    if (!propertyFile.exists())
      throw new IOException("Endpoints property file does not exits.");

    try {


      SQLDatabase madisDB = DBUtils.createEmbeddedMadisDB(engine);
      ResultSet rs = madisDB.executeAndGetResults(String.format("select name, desc, host, port, path, status  from (file 'dialect:json' '%s');", propertyFile.getCanonicalPath()));

      ArrayList<Endpoint> endpoints = new ArrayList<>();

      while (rs.next()) {
        Endpoint endpoint = new Endpoint();
        endpoint.setName((String) rs.getObject(1));
        endpoint.setDesc((String) rs.getObject(2));
        endpoint.setHost((String) rs.getObject(3));
        endpoint.setPort((String) rs.getObject(4));
        endpoint.setPath((String) rs.getObject(5));
        endpoint.setStatus((String) rs.getObject(6));
        endpoints.add(endpoint);
      }

      Endpoint[] endpointsArr = new Endpoint[endpoints.size()];
      for (int i = 0; i < endpointsArr.length; i++) {
        endpointsArr[i] = endpoints.get(i);
      }

      Endpoints endpointsobj = new Endpoints();
      endpointsobj.setEndpoints(endpointsArr);
      return endpointsobj;
    } catch (SQLException e) {
      throw new IOException("Unbale to parse property file.", e);
    }

  }

}
