package madgik.exareme.master.queryProcessor.analyzer.fanalyzer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 * @author jim
 */
public class Demo {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here

        /* analyze all tables */
        /*
         * FederatedAnalyzer fa = new FederatedAnalyzer(); fa.analyzeAll();
         */

        /* analyze specific table */
        FederatedAnalyzer fa = new FederatedAnalyzer();
        Set<String> tableNames = new HashSet<String>();

        // This txt file contains all the tables we need to analyze
        try (BufferedReader br = new BufferedReader(new FileReader("./files/input/slegge.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                // process the line.
                tableNames.add(line);
            }
        }

        for (String s : tableNames) {
            fa.analyzeTable(s);
        }
        fa.closeConnection();

    }

}
