/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.geometry;

import java.awt.geom.Line2D;

/**
 * @author heraldkllapi
 */
public class GeoDemo {

    public static void main(String[] args) {
        double minTime = 3.0;
        double maxTime = 3.0;
        double minMoney = 3.0;
        double maxMoney = 3.0;

        Line2D.Double lineMoneyMax =
            new Line2D.Double(minTime, maxMoney, minTime, Double.MAX_VALUE);
        Line2D.Double lineTimeMax = new Line2D.Double(maxTime, minMoney, 100000.0, minMoney);

        System.out.println(lineMoneyMax.ptLineDist(8.5, 16.0));
        System.out.println(lineTimeMax.ptSegDist(8.5, 16.0));

        //    java.rmi.RemoteException: [(8.537117875108763,16.0)]
        //[(2.9228272224073972,3.0)]
    }
}
