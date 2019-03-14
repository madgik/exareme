/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.datetime;

import madgik.exareme.utils.units.Metrics;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author heraldkllapi
 */
public class DateUtils {

    public static double getSeconds(String seconds) throws ParseException {
        DateFormat df = new SimpleDateFormat("hh:mm:ss");
        Date date = df.parse(seconds);
        return (date.getDay() - 4) * Metrics.Day +
                date.getHours() * Metrics.Hour +
                date.getMinutes() * Metrics.Min +
                date.getSeconds();
    }

    public static void main(String[] args) throws Exception {
        System.out.println(getSeconds("0:16:41"));
    }
}
