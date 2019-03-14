/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.connector.rmi;

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
public class AdpDBNetReaderThread extends Thread {
    private final String tabName;
    private final Map<String, Object> alsoIncludeProps;
    private final AdpDBClientProperties props;
    private final PipedOutputStream out;
    private Logger log = Logger.getLogger(AdpDBNetReaderThread.class);

    public AdpDBNetReaderThread(String tabName, Map<String, Object> alsoIncludeProps,
                                AdpDBClientProperties props, PipedOutputStream out) {
        this.tabName = tabName;
        this.alsoIncludeProps = alsoIncludeProps;
        this.props = props;
        this.out = out;
    }

    @Override
    public void run() {
        try {
            log.info("table " + tabName);
            Registry registry = Registry.getInstance(props.getDatabase());
            PhysicalTable table = registry.getSchema().getPhysicalTable(tabName);
            if (table == null) {
                log.warn("table not found in registry");
                ArrayList<Object> errors = (ArrayList<Object>) alsoIncludeProps.get("errors");
                errors.add("Cannot read result table");
                Gson g = new Gson();
                out.write((g.toJson(alsoIncludeProps) + "\n").getBytes());
                return;
            }

            Map<String, Object> includeProps = alsoIncludeProps;
            if (alsoIncludeProps != null
                    && alsoIncludeProps.containsKey("skipSchema")
                    && (Boolean) alsoIncludeProps.get("skipSchema") == true) includeProps = null;

            for (Partition p : table.getPartitions()) {
                log.debug("Reading : " + p.getTable() + "/" + p.getpNum());
                AdpDBConnectorUtil.readRemoteTablePart(registry, table, p, includeProps, out);
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
