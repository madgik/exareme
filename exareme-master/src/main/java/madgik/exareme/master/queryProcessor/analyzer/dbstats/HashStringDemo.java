/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package madgik.exareme.master.queryProcessor.analyzer.dbstats;

import madgik.exareme.master.queryProcessor.analyzer.stat.StatUtils;

/**
 * @author jim
 */
public class HashStringDemo {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here

        String str1 = "aaa";
        String str2 = "z";

        double str1h = StatUtils.hashString(str1);
        double str2h = StatUtils.hashString(str2);

        System.out.println(str1h);
        System.out.println(str2h);

        if (str1h >= str2h)
            System.out.println("str1 > str2");
        else
            System.out.println("str1 < str2");
    }

}
