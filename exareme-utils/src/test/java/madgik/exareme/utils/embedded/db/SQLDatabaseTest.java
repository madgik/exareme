/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.embedded.db;

import junit.framework.Assert;
import junit.framework.TestCase;
import madgik.exareme.utils.file.FileUtil;

import java.util.*;

/**
 * @author herald
 */
public class SQLDatabaseTest extends TestCase {

    private List<SQLDatabase> sqlDatabaseImpls = new ArrayList<SQLDatabase>();

    private static <T> void assertEqualCollections(Collection<T> expectedCol,
        Collection<T> actualCol) {
        Object[] expected = expectedCol.toArray();
        Object[] actual = actualCol.toArray();
        Arrays.sort(expected);
        Arrays.sort(actual);
        //    assertArrayEquals(expected, actual);
    }

    private static <K, V> void assertEqualMaps(Map<K, V> expectedMap, Map<K, V> actualMap) {
        assertEqualCollections(expectedMap.keySet(), actualMap.keySet());

        for (K key : expectedMap.keySet()) {
            Collection<V> expValue = (Collection<V>) expectedMap.get(key);
            Collection<V> actualValue = (Collection<V>) actualMap.get(key);

            assertEqualCollections(expValue, actualValue);
        }
    }

    public void createDatabases(boolean useEmbeded) throws Exception {
        sqlDatabaseImpls.add(DBUtils.createEmbeddedSqliteDB());
    }

    public void loadSchema() throws Exception {
        String script = FileUtil.consume(this.getClass().getResourceAsStream("schema.sql"));

        for (SQLDatabase db : sqlDatabaseImpls) {
            db.execute(script);
        }
    }

    public void closeDatabases() throws Exception {
        for (SQLDatabase db : sqlDatabaseImpls) {
            db.close();
        }
        sqlDatabaseImpls.clear();
    }

    public void execute() throws Exception {
        createDatabases(true);
        loadSchema();
        closeDatabases();
    }

    public void getQueryInfo() throws Exception {
        createDatabases(false);
        loadSchema();

        String query = FileUtil.consume(this.getClass().getResourceAsStream("q.sql"));
        List<SQLQueryInfo> infos = new ArrayList<SQLQueryInfo>();
        for (SQLDatabase db : sqlDatabaseImpls) {
            SQLQueryInfo info = db.getQueryInfo(query);
            infos.add(info);
        }

        SQLQueryInfo expected = new SQLQueryInfo(query);
        expected.addInputTable("a");
        expected.addInputTable("b");
        expected.addInputTable("c");
        expected.addUsedColumn("a", "a1", false, true);
        expected.addUsedColumn("b", "b1", false, true);
        expected.addUsedColumn("b", "b2", false, true);
        expected.addUsedColumn("c", "c1", false, true);
        expected.addUsedColumn("c", "c2", true, false);

        for (SQLQueryInfo sqi : infos) {
            Assert.assertEquals(expected.getQuery().toLowerCase(), sqi.getQuery().toLowerCase());
            assertEqualCollections(expected.getInputTables(), sqi.getInputTables());
            assertEqualMaps(expected.getUsedColumns(), sqi.getUsedColumns());
        }

        closeDatabases();
    }

    public void getTableInfo() throws Exception {
        createDatabases(true);
        loadSchema();

        String[] tableNames = new String[] {"a", "b", "c"};
        String[] tableDefs =
            new String[] {"create table a (a1, a2, a3)", "create table b (\n" + "b1,\n" + "b2)",
                "create table c (c1, c2, c3)"};

        for (int t = 0; t < tableNames.length; ++t) {
            List<TableInfo> infos = new ArrayList<TableInfo>();
            for (SQLDatabase db : sqlDatabaseImpls) {
                TableInfo info = db.getTableInfo(tableNames[t]);
                infos.add(info);
            }

            TableInfo expected = new TableInfo(tableNames[t]);
            expected.setSqlDefinition(tableDefs[t]);

            for (TableInfo ti : infos) {
                Assert.assertEquals(expected.getTableName().toLowerCase(),
                    ti.getTableName().toLowerCase());
                Assert.assertEquals(expected.getSQLDefinition().toLowerCase(),
                    ti.getSQLDefinition().toLowerCase());
            }
        }

        closeDatabases();
    }
}
