/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package madgik.exareme.master.queryProcessor.analyzer.builder;

import java.util.Map;

/**
 * @author jim
 */
public class BuildUtil {

    public static double computeMeanVal(Map<String, Integer> valMap) {
        int sum = 0;

        for (String s : valMap.keySet())
            sum += valMap.get(s);

        // return mean val
        return (double) sum / (double) valMap.size();
    }

}
