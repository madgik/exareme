/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.federation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dimitris
 */
public class NamesToAliases {
    private Map<String, List<String>> map;
    private int counter;

    public NamesToAliases() {
        this.map = new HashMap<String, List<String>>();
        this.counter = 0;
    }

    public String getAliasForTable(String tablename) {
        return map.get(tablename).get(0);
    }

    public String getAliasForTableOccurence(String tablename, int occurence) {
        return map.get(tablename).get(occurence - 1);
    }

    public String getGlobalAliasForBaseTable(String basetable, Integer occurence) {
        if (this.map.containsKey(basetable)) {
            List<String> aliases = map.get(basetable);
            if (aliases.size() < occurence + 1) {
                aliases.add(generateNextGlobalAlias());
            }
            return aliases.get(occurence);
        } else {
            List<String> aliases = new ArrayList<String>();
            aliases.add(generateNextGlobalAlias());
            this.map.put(basetable, aliases);
            return aliases.get(occurence);
        }
    }

    public List<String> getAllAliasesForBaseTable(String basetable) {
        return map.get(basetable);
    }

    private String generateNextGlobalAlias() {
        return "alias" + this.counter++;
    }
}
