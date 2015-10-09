/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.demo;

import com.google.gson.Gson;
import madgik.exareme.master.queryProcessor.estimator.db.Schema;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * @author jim
 */
public class LoadStatDemo {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here


        Schema s = loadStats();
        System.out.println(s);
    }



    private static Schema loadStats() throws Exception {

        Gson gson = new Gson();

        BufferedReader br = new BufferedReader(new FileReader("./files/schema_primitive.json"));

        Schema schema = gson.fromJson(br, Schema.class);

        return schema;
    }

}
