/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.analyzer.dbstats;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import madgik.exareme.master.queryProcessor.analyzer.builder.EquiDepth;
import madgik.exareme.master.queryProcessor.analyzer.builder.HistogramBuildMethod;
import madgik.exareme.master.queryProcessor.analyzer.builder.HistogramBuilder;
import madgik.exareme.master.queryProcessor.analyzer.builder.PrimitiveHistogram;
import madgik.exareme.master.queryProcessor.analyzer.stat.Table;
import madgik.exareme.master.queryProcessor.estimator.db.Schema;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jim
 */
public class StatBuilder {

    private String path;
    private String[] db;
    private HistogramBuildMethod method;
    private Map<String, Table> dbStats;
    private String fileName = "NOVAL";

    public StatBuilder(String[] db, HistogramBuildMethod method, Map<String, Table> schema)
        throws Exception {
        this.db = db;
        this.method = method;
        this.dbStats = loadData(schema);
    }

    public StatBuilder(String path, String[] db, HistogramBuildMethod method) throws Exception {
        this.path = path;
        this.db = db;
        this.method = method;
        this.dbStats = loadData();
    }

    public Schema build() throws Exception {
        HistogramBuilder hb = null;
        switch (this.method) {
            case Primitive:
                fileName = "primitive";
                hb = new PrimitiveHistogram();
                break;
            case EquiDepth:
                fileName = "equiDepth";
                hb = new EquiDepth();
                break;
        }
        return hb.build(dbStats);

        //		s.exportToJson("schema_" + fileName);
    }

    /* private - helper methods */
    private Map<String, Table> loadData() throws Exception {

        Map<String, Table> compositeSchema = new HashMap<String, Table>();

        for (String s : this.db) {
            Gson gson = new Gson();
            BufferedReader br = new BufferedReader(new FileReader(path + s + ".json"));
            // convert the json string back to object
            Map<String, Table> schema = gson.fromJson(br, new TypeToken<Map<String, Table>>() {
            }.getType());
            compositeSchema.putAll(schema);
        }

        return compositeSchema;
    }

    private Map<String, Table> loadData(Map<String, Table> schema) throws Exception {

        Map<String, Table> compositeSchema = new HashMap<String, Table>();

        compositeSchema.putAll(schema);


        return compositeSchema;
    }

}
