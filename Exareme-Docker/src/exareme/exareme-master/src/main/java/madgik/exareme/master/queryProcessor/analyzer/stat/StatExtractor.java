/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package madgik.exareme.master.queryProcessor.analyzer.stat;

import java.util.Map;

/**
 * @author jim
 */
public interface StatExtractor {

    public Map<String, Table> extractStats() throws Exception;

}
