/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.parser;

/**
 * @author herald
 */
public class AdpDBParserStatistics {

    private int numOfQueries = 0;
    private long lastParseTimeMS = 0;

    public AdpDBParserStatistics() {

    }

    public void addQuery(long parseTimeMS) {
        this.lastParseTimeMS = parseTimeMS;
    }

    public long getLastQueryParseTime() {
        return lastParseTimeMS;
    }
}
