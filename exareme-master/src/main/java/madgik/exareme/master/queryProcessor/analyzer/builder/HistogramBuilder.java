/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package madgik.exareme.master.queryProcessor.analyzer.builder;

import madgik.exareme.master.queryProcessor.analyzer.stat.Table;
import madgik.exareme.master.queryProcessor.estimator.db.Schema;

import java.util.Map;

/**
 * @author jim
 */
public interface HistogramBuilder {
    public Schema build(Map<String, Table> dbStats);
}
