/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.connector.local;

import com.google.gson.Gson;
import madgik.exareme.common.schema.Partition;
import madgik.exareme.common.schema.PhysicalTable;
import madgik.exareme.master.client.AdpDBClientProperties;
import madgik.exareme.master.connector.AdpDBConnectorUtil;
import madgik.exareme.master.registry.Registry;
import org.apache.log4j.Logger;

import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author heraldkllapi
 */
public class AdpDBTableReaderThread extends Thread {
    private final String tabName;
    private final Map<String, Object> alsoIncludeProps;
    private final AdpDBClientProperties props;
    private final PipedOutputStream out;
    private Logger log = Logger.getLogger(AdpDBTableReaderThread.class);

    public AdpDBTableReaderThread(String tabName, Map<String, Object> alsoIncludeProps,
        AdpDBClientProperties props, PipedOutputStream out) {
        this.tabName = tabName;
        this.alsoIncludeProps = alsoIncludeProps;
        this.props = props;
        this.out = out;
    }

    @Override public void run() {
        try {
            Registry registry = Registry.getInstance(props.getDatabase());
            PhysicalTable table = registry.getSchema().getPhysicalTable(tabName);
            Map<String, Object> includeProps = alsoIncludeProps;
            if (table == null) {
                ArrayList<Object> errors = (ArrayList<Object>) alsoIncludeProps.get("errors");
                errors.add("Cannot read result table");
                Gson g = new Gson();
                out.write((g.toJson(alsoIncludeProps) + "\n").getBytes());
                return;
            }
            for (Partition p : table.getPartitions()) {
                AdpDBConnectorUtil
                    .readLocalTablePart(tabName, p.getpNum(), props.getDatabase(), includeProps,
                        out);
                includeProps = null;  // include props only in the first part
            }
        } catch (Exception e) {
            log.error("Cannot get results", e);
        } finally {
            try {
                out.flush();
                out.close();
            } catch (Exception e) {
                log.error("Cannot close output", e);
            }
        }
    }
}
