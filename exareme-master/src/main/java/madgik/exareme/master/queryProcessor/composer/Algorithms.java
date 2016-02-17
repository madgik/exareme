package madgik.exareme.master.queryProcessor.composer;

import com.google.gson.Gson;

import java.io.*;
import java.util.ArrayList;

/**
 * @author alex
 */
public class Algorithms {

    private String input_local_tbl;
    private String host;
    private String port;
    private String username;
    private String password;
    private String query;

    public String getInput_local_tbl() {
        return input_local_tbl;
    }

    public void setInput_local_tbl(String input_local_tbl) {
        this.input_local_tbl = input_local_tbl;
    }

    private Endpoint[] endpoints;

    public Endpoint[] getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Endpoint[] endpoints) {
        this.endpoints = endpoints;
    }


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

    private Algorithm[] algorithms;

    public static class Algorithm {
        public static class AlgorithmParameter {
            private String name;
            private String desc;
            private String value;

            public AlgorithmParameter() {
            }

            public String getDesc() {
                return desc;
            }

            public void setDesc(String desc) {
                this.desc = desc;
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }

            public String getName() { return name; }

            public void setName(String name) {
                this.name = name;
            }
        }
        private String name;
        private String desc;
        private AlgorithmType type;
        private AlgorithmStatus status;
        private AlgorithmVType VType;
        private AlgorithmParameter[] parameters;

        public Algorithm() {}

        public String getName() {
          return name;
        }

        public void setName(String name) {
          this.name = name;
        }

        public AlgorithmStatus getStatus() {
          return status;
        }

        public void setStatus(AlgorithmStatus status) {
          this.status = status;
        }

        public void setVType(AlgorithmVType VType) {
          this.VType = VType;
        }

        public AlgorithmVType getVType() {
      return VType;
    }

        public AlgorithmType getType() {  return type; }

        public void setType(AlgorithmType type) {  this.type = type;}

        public String getDesc() {  return desc;}

        public void setDesc(String desc) {  this.desc = desc; }

        public AlgorithmParameter[] getParameters() {
            return parameters;
        }

        public void setParameters(AlgorithmParameter[] parameters) {
            this.parameters = parameters;
        }
  }

    public Algorithm[] getAlgorithms() {
        return algorithms;
    }

    public void setAlgorithms(Algorithm[] algorithms) {
        this.algorithms = algorithms;
    }


    public static Algorithms createAlgorithms(String repoPath) throws IOException {
      File repoFile = new File(repoPath);
      if (!repoFile.exists()) throw new IOException("Unable to locate property file.");

      // read property.json
      Gson gson = new Gson();
      Algorithms algorithms = gson.fromJson(new BufferedReader(new FileReader(repoPath + "/properties.json")), Algorithms.class);

      // read per algorithm property.json
        ArrayList<Algorithm> algs = new ArrayList<Algorithm>();
      for (File file : repoFile.listFiles(new FileFilter() {
          @Override
          public boolean accept(File pathname) {
              return pathname.isDirectory() && !pathname.getName().startsWith(".") ? true : false;
          }
      })) {
          Algorithm algorithm = gson.fromJson(new BufferedReader(new FileReader(file.getAbsolutePath() + "/properties.json")), Algorithm.class);
          algs.add(algorithm);
      }
    algorithms.setAlgorithms( algs.toArray(new Algorithm[algs.size()]));
    return algorithms;
  }
    public static void main(String[] args) throws IOException, ComposerException {

        Algorithms algorithms = Algorithms.createAlgorithms("/home/alex/Projects/madgik/hbp-algorithms");
        Gson gson = new Gson();
        String s = gson.toJson(algorithms, Algorithms.class);
        System.out.println(s);
        System.out.println(Composer.getInstance().getEndpoints());
        System.out.println(Composer.getInstance().getAlgorithms());



    }
}
