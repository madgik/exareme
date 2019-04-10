/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.engine.remoteQuery.impl.utility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Christos Mallios <br>
 * University of Athens / Department of Informatics and Telecommunications.
 */
public class Sets {

    public static <T> List<List<T>> powerset(Collection<T> list) {
        List<List<T>> ps = new ArrayList<List<T>>();
        ps.add(new ArrayList<T>());

        for (T item : list) {
            List<List<T>> newPs = new ArrayList<List<T>>();

            for (List<T> subset : ps) {
                newPs.add(subset);

                List<T> newSubset = new ArrayList<T>(subset);
                newSubset.add(item);
                newPs.add(newSubset);
            }

            ps = newPs;
        }
        return ps;
    }
}
