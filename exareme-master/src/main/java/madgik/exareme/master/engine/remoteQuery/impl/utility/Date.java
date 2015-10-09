/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.remoteQuery.impl.utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author Christos Mallios <br>
 *         University of Athens / Department of Informatics and Telecommunications.
 */
public class Date {

    /*
     * Function which returns the current date-time
     */
    public static String getCurrentDateTime() {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.util.Date date = new java.util.Date();

        return dateFormat.format(date);
    }

    /*
     * Function which returns the difference of the current date time to 
     * the given date time in seconds
     */
    public static double getDifferenceInSec(String date) throws ParseException {

        double difference;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        java.util.Date give_date = dateFormat.parse(date);

        java.util.Date current_date = new java.util.Date();

        Calendar given_calendar = Calendar.getInstance();
        given_calendar.setTime(give_date);

        Calendar current_calendar = Calendar.getInstance();
        current_calendar.setTime(current_date);

        long diffInMil = current_calendar.getTimeInMillis() - given_calendar.getTimeInMillis();

        difference = diffInMil / 1000;

        return difference;
    }
}
