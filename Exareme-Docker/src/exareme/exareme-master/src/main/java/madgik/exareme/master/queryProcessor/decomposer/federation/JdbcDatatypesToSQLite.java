package madgik.exareme.master.queryProcessor.decomposer.federation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class JdbcDatatypesToSQLite {

    public static final Set<Integer> textList = new HashSet<Integer>(Arrays
            .asList(java.sql.Types.CHAR, java.sql.Types.LONGNVARCHAR, java.sql.Types.LONGVARCHAR,
                    java.sql.Types.NVARCHAR, java.sql.Types.DATE, java.sql.Types.VARCHAR));

    public static final Set<Integer> intList = new HashSet<Integer>(Arrays
            .asList(java.sql.Types.BIGINT, java.sql.Types.INTEGER, java.sql.Types.SMALLINT,
                    java.sql.Types.TINYINT, java.sql.Types.TIMESTAMP));

    public static final int BLOB = java.sql.Types.BLOB;

    public static final Set<Integer> realList = new HashSet<Integer>(
            Arrays.asList(java.sql.Types.REAL, java.sql.Types.DOUBLE, java.sql.Types.FLOAT));

    public static final Set<Integer> numericList = new HashSet<Integer>(Arrays
            .asList(java.sql.Types.NUMERIC, java.sql.Types.BOOLEAN, java.sql.Types.DECIMAL,
                    java.sql.Types.TIME));

}
