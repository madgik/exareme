/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package madgik.exareme.master.queryProcessor.analyzer.dbstats;

import java.util.Arrays;

/**
 * @author jim
 */
public class DbStats {

    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        String path = args[0];
        String[] db = Arrays.copyOfRange(args, 1, args.length);
        // gathers stats and stores them in "./files/json/data.json"
        for (String s : db) {
            Gatherer g = new Gatherer(path + s, s);
            g.gather(path + s);
        }

        // builds stats and stores them in "./files/json/"
        //StatBuilder sb = new StatBuilder(path, db,
        //		HistogramBuildMethod.Primitive);
        //sb.build();
    }

}
