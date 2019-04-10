package madgik.exareme.master.engine.executor.remote.operator.OptiqueStreamQueryEndPoint;

import com.google.gson.Gson;
import madgik.exareme.utils.association.SimplePair;
import org.apache.http.entity.ContentProducer;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class StreamDataStreamer implements ContentProducer {
    private SimplePair<List<String[]>, ArrayDeque<Object[]>> buffer;
    Output output;

    private static final Logger log = Logger.getLogger(StreamDataStreamer.class);

    public StreamDataStreamer(SimplePair<List<String[]>, ArrayDeque<Object[]>> buffer,
                              Output output) {
        this.buffer = buffer;
        this.output = output;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        if (output == Output.JSON) {
            jsonWriteTo(out);
        } else if (output == Output.CSV) {
            csvWriteTo(out);
        } else {
            mixedWriteTo(out);
        }
    }

    private void jsonWriteTo(OutputStream out) throws IOException {
        log.debug("Starting Producing Stream Data In Json Format Thread ...");
        if (out != null) {
            Gson gson = new Gson();
            ArrayList<HashMap<String, Object>> jsonTuples =
                    new ArrayList<HashMap<String, Object>>();
            for (Object[] tuple : buffer.second) {
                HashMap<String, Object> tupleMap = new HashMap<String, Object>();
                for (int i = 0; i < buffer.first.size(); ++i) {
                    tupleMap.put(buffer.first.get(i)[0], tuple[i]);
                }
                jsonTuples.add(tupleMap);
            }
            out.write(gson.toJson(jsonTuples).getBytes());
        }

        log.debug("Ending Producing Stream Data Thread ...");
    }

    private void csvWriteTo(OutputStream out) throws IOException {
        log.debug("Starting Producing Stream Data In Csv Format Thread ...");
        if (out != null) {
            // Write Schema
            for (int i = 0; i < buffer.first.size(); ++i) {
                String schema = (i == 0) ?
                        buffer.first.get(i)[0].toString() :
                        "," + buffer.first.get(i)[0].toString();
                out.write(schema.getBytes());
            }
            out.write("\n".getBytes());

            // Write Lines
            for (Object[] tuple : buffer.second) {
                out.write(ParseUtils.ArrayToCsv(tuple).getBytes());
            }
        }

        log.debug("Ending Producing Stream Data Thread ...");
    }

    private void mixedWriteTo(OutputStream out) throws IOException {
        log.debug("Starting Producing Stream Data In Mixed Format Thread ...");

        if (out != null) {
            Gson gson = new Gson();
            LinkedHashMap<String, List<String[]>> headerMap =
                    new LinkedHashMap<String, List<String[]>>();
            headerMap.put("schema", buffer.first);
            out.write((gson.toJson(headerMap) + "\n").getBytes());

            for (Object[] tuple : buffer.second) {
                out.write((gson.toJson(tuple) + "\n").getBytes());
            }
        }

        log.debug("Ending Producing Stream Data Thread ...");
    }
}
