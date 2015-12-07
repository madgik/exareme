/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.federation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    
    public Set<String> getTables() {
    	return map.keySet();
    }

	public void addTable(String t, String[] aliases) {
		//for(String a:aliases){
			this.map.put(t,  new ArrayList<String>(Arrays.asList(aliases)));
		//}
	}

	@Override
	public String toString() {
		return "NamesToAliases [map=" + map + ", counter=" + counter + "]";
	}

	public void setCounter(int c) {
		this.counter=c;
		
	}
	
	public String getOriginalName(String alias){
		for (String k:map.keySet()){
			if(map.get(k).contains(alias)){
				return k;
			}
		}
		return null;
	}
	
}
