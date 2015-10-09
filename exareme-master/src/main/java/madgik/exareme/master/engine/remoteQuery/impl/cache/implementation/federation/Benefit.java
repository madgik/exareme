/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.engine.remoteQuery.impl.cache.implementation.federation;

import madgik.exareme.master.engine.remoteQuery.impl.cache.QueryRequests;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static madgik.exareme.master.engine.remoteQuery.impl.utility.Sets.powerset;

/**
 * @author Christos Mallios <br>
 *         University of Athens / Department of Informatics and Telecommunications.
 */
public class Benefit {

    private static HashMap<String, Double> benefitMap;

    public static void setBenefitMap(HashMap<String, Double> benefitMap) {
        Benefit.benefitMap = benefitMap;
    }

    public static List<String> maximizeBenefit(List<List<String>> powerSet, FederatedCache cache,
        String newQuery) {

        double maxBenefit = 0, benefit;
        List<String> maxSet = null;

        for (List<String> set : powerSet) {
            benefit = 0;
            for (String query : set) {
                benefit += benefitMap.get(query);
            }
            if (benefit >= maxBenefit) {
                if (cache.fitInCache(set, newQuery)) {
                    maxBenefit = benefit;
                    maxSet = set;
                }
            }
        }

        if (maxSet == null) {
            return powerSet.get(powerSet.size() - 1);
        }

        List<String> removedQueries = new LinkedList<String>();
        for (String query : benefitMap.keySet()) {
            if (!maxSet.contains(query)) {
                removedQueries.add(query);
            }
        }

        return removedQueries;
    }

    public static double computeBenefit(double a, QueryRequests request) {

        return a * request.numberOfRequests * request.queryResponseTime
            / request.numberOfTotalRequests;
    }

    public static void main(String[] args) {
        Collection<String> list = new LinkedList<String>();
        list.add("a");
        list.add("b");
        list.add("c");

        List<List<String>> set = powerset(list);
        set.remove(0);
        System.out.println(set);

        HashMap<String, Double> map = new HashMap<String, Double>();
        map.put("a", -3.0);
        map.put("b", 1.0);
        map.put("c", -2.0);

        Benefit.setBenefitMap(map);
        List<String> maxSet = Benefit.maximizeBenefit(set, null, null);

    }

}
