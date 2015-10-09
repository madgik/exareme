/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.util;

import madgik.exareme.common.schema.PhysicalTable;
import madgik.exareme.utils.file.FileUtil;
import madgik.exareme.worker.art.container.ContainerProxy;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.*;

/**
 * @author heraldkllapi
 */
public class SchemaUtil {

    private static Logger log = Logger.getLogger(SchemaUtil.class);

    public static void getLocationsOfPartitions(ContainerProxy[] containerProxies,
        PhysicalTable pTable, int part, BitSet filter) {
        filter.clear();
        List<String> locations = pTable.getPartition(part).getLocations();
        log.debug("Locations of " + pTable.getName() + "/" + part + " are " + locations.toString());
        for (int p = 0; p < containerProxies.length; ++p) {
            for (String loc : locations) {

                if (containerProxies[p].getEntityName().getName().startsWith(loc + "_")) {
                    filter.set(p);
                    break;
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        HashMap<String, LinkedList<TablePart>> tables =
            new HashMap<String, LinkedList<TablePart>>();
        String data = FileUtil.readFile(new File("demo/data.location"));
        String[] parts = data.split("[\n]");
        String currentServer = null;
        for (String p : parts) {
            if (p.startsWith("Server")) {
                currentServer = p.split("  ")[1];
                if (currentServer.equals("192.168.10.1")) {
                    currentServer = "88.197.53.125";
                }
                continue;
            }
            TablePart tp = extractTableNamePart(currentServer, p);
            LinkedList<TablePart> tableParts = tables.get(tp.table);
            if (tableParts == null) {
                tableParts = new LinkedList<TablePart>();
                tables.put(tp.table, tableParts);
            }
            tableParts.add(tp);
        }
        for (Map.Entry<String, LinkedList<TablePart>> e : tables.entrySet()) {
            System.out.print(e.getKey() + " = ");
            Collections.sort(e.getValue(), new Comparator<TablePart>() {

                @Override public int compare(TablePart o1, TablePart o2) {
                    return (o1.part < o2.part) ? -1 : ((o1.part == o2.part) ? 0 : 1);
                }
            });
            for (TablePart tp : e.getValue()) {
                System.out.print(tp + " ");
            }
            System.out.println("");
        }
    }

    private static TablePart extractTableNamePart(String location, String data) {
        String[] parts = data.split("\\.");
        return new TablePart(parts[0], Integer.parseInt(parts[1]), location);
    }


    static class TablePart {
        public String table;
        public int part;
        public String location;

        public TablePart(String table, int part, String location) {
            this.table = table;
            this.part = part;
            this.location = location;
        }

        @Override public String toString() {
            return part + ":" + location;
        }
    }
}
