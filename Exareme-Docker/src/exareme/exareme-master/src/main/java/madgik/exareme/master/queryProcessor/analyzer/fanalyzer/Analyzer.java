package madgik.exareme.master.queryProcessor.analyzer.fanalyzer;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.Set;

/**
 * @author jim
 */
public interface Analyzer {
    public void analyzeAll() throws Exception;

    public void analyzeTable(String tableName) throws Exception;

    public void analyzeAttrs(String tableName, Set<String> attrs) throws Exception;
}
