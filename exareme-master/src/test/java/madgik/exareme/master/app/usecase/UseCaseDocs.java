package madgik.exareme.master.app.usecase;//package madgik.exareme.master.app.usecase;
//
//import madgik.exareme.master.client.AdpDBClient;
//import madgik.exareme.master.client.AdpDBClientProperties;
//import madgik.exareme.master.client.AdpDBClientQueryStatus;
//import madgik.exareme.master.app.cluster.ExaremeCluster;
//import madgik.exareme.master.app.cluster.ExaremeClusterFactory;
//import org.apache.commons.io.IOUtils;
//import org.apache.log4j.Level;
//import org.apache.log4j.Logger;
//import org.junit.Test;
//
///**
// * @author alex
// */
//public class UseCaseDocs {
//    private static final Logger log = Logger.getLogger(UseCaseDocs.class);
//    private boolean load = false;
//    private boolean query = true;
//
//
//
//    @Test public void testDocsUseCase() throws Exception {
//        Logger.getRootLogger().setLevel(Level.INFO);
//        ExaremeCluster miniCluster = ExaremeClusterFactory.createMiniCluster(1099, 8088, 3);
//        miniCluster.start();
//
//        String dbpath ="/home/alex/Desktop/docs-madis/demo";
//        AdpDBClient dbClient =
//            miniCluster.getExaremeClusterClient(new AdpDBClientProperties(dbpath));
//
//        if (load) {
//            String load_query = "distributed create table docs as external "
//                + "select * "
//                + "from ( setschema 'id, text'  select jdictsplit(c1, 'id', 'text') "
//                + "                             from file('/home/alex/Desktop/docs-madis/document_text.txt')) "
//                + "where text <> '' and text not null;";
//            AdpDBClientQueryStatus queryResult = dbClient.query("load", load_query);
//            if (queryResult.hasException()){
//                log.error(queryResult.getException());
//            }
//            log.info("Table docs created!");
//        }
//
//        if (query) {
//            String query = "\ndistributed create table result as \n"
//                + "select jdict('documentId', docid, 'datasetId', dsetID, 'confidenceLevel', sqroot(min(1.49,max(confidence))/1.5)) \n"
//                + "from ( select docid, dsetID, case when length(context) > 100 then round((length(titles) + conf*10)/(length(context)*1.0),2) else round(conf/10.0,2) end as confidence \n"
//                + "       from (  select    docid, dsetID, \n"
//                + "                         regexpcountuniquematches(bag,lower(regexpr('\\W|_',context,' '))) + 2*regexprmatches(creator,lower(context)) + regexprmatches(publisher,lower(context)) as conf, \n"
//                + "                         regexprmatches(titles,lower(context)) as match, titles, context \n"
//                + "               from (  select docid, lower(stripchars(middle,'_')) as mystart, prev||' '||middle||' '||next as context   \n"
//                + "                       from (setschema 'docid,prev,middle,next' select id as docid, textwindow2s(normalizetext(textreferences(text)),15,3,15) from docs \n"
//                + "                     ) \n"
//                + "             ),titlesandtriples \n"
//                + "       where mystart = words and match) where confidence > 0.28 \n"
//                + "       union all \n"
//                + "       select  docid, dsetID, 1 as confidence \n"
//                + "       from ( setschema 'docid,middle' select id as docid, \n"
//                + "              textwindow(comprspaces(filterstopwords(regexpr('(/|:)(n)',text,'\\1'))),0,0,'\\b10.\\d{4}/') from docs \n"
//                + "            ),dois \n"
//                + "       where normalizetext(stripchars(regexpr('(\\b10.\\d{4}/.*)',middle),'.,')) = normaldoi \n"
//                + "    ) \n"
//                + "group by docid, dsetID;\n";
//            log.info(query);
//            AdpDBClientQueryStatus queryResult = dbClient.query("query", query);
//            if (queryResult.hasException()){
//                log.error(queryResult.getException());
//            }
//            log.info("Result table created");
//            log.info(IOUtils.toString(dbClient.readTable("result")));
//
//        }
//
//        miniCluster.stop(false);
//    }
//
//
//}
