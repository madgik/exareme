/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.graph;

import madgik.exareme.common.optimizer.OperatorBehavior;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.*;

/**
 * @author herald
 * @since 1.0
 */
public class PegasusDaxParser {

    /**
     * A mapping of AbstractOperator names and their Concrete Operator names
     */
    public final static Map<String, List<String>> abstractConcreteOperatorsMap =
            Collections.unmodifiableMap(new HashMap<String, List<String>>() {

                {
                    put("mAdd", Collections.unmodifiableList(Arrays.asList("mAdd")));
                    put("mBackground", Collections.unmodifiableList(Arrays.asList("mBackground")));
                    put("mBgModel", Collections.unmodifiableList(Arrays.asList("mBgModel")));
                    put("mConcatFit", Collections.unmodifiableList(Arrays.asList("mConcatFit")));
                    put("mDiffFit", Collections.unmodifiableList(Arrays.asList("mDiffFit")));
                    put("mImgTbl", Collections.unmodifiableList(Arrays.asList("mImgTbl")));
                    put("mJPEG", Collections.unmodifiableList(Arrays.asList("mJPEG")));
                    put("mProjectPP", Collections.unmodifiableList(Arrays.asList("mProjectPP")));
                    put("mShrink", Collections.unmodifiableList(Arrays.asList("mShrink")));
                }
            });
    private static final Logger LOG = Logger.getLogger(PegasusDaxParser.class);

    static {
    }

    private PegasusDaxParser() {
    }

    public static ConcreteQueryGraph parseDax(String url, GraphParameters params) throws Exception {
        ConcreteQueryGraph graph = new ConcreteQueryGraph();

        LinkedHashMap<String, ConcreteOperator> operatorNameMap = new LinkedHashMap<>(16);
        LinkedHashMap<String, LinkData> operatorDataMap = new LinkedHashMap<>(16);

        LinkedHashMap<String, LinkedHashMap<String, Integer>> operatorDataInputMap =
                new LinkedHashMap<>(16);
        LinkedHashMap<String, LinkedHashMap<String, Integer>> operatorDataOutputMap =
                new LinkedHashMap<>(16);

        HashMap<String, LinkData> inFileDataMap = new HashMap<>();
        HashMap<String, LinkData> outFileDataMap = new HashMap<>();
        HashMap<String, String> inDataOpMap = new HashMap<>();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(url);
        doc.getDocumentElement().normalize();

        Random rand = new Random();

        NodeList jobList = doc.getElementsByTagName("job");
        for (int s = 0; s < jobList.getLength(); s++) {
            double runTimeValue = 0;
            double cpuUtilizationValue = 1.0;
            int memoryValue = params.memory;

            Node job = jobList.item(s);
            Element jobElement = (Element) job;

            String name = jobElement.getAttribute("id");
            runTimeValue =
                    Double.parseDouble(jobElement.getAttribute("runtime")) * params.multiply_by_time;

            LinkedHashMap<String, Integer> inMap = new LinkedHashMap<>(16);
            LinkedHashMap<String, Integer> outMap = new LinkedHashMap<>(16);

            NodeList useElementList = jobElement.getElementsByTagName("uses");
            for (int i = 0; i < useElementList.getLength(); i++) {
                Node use = useElementList.item(i);
                Element useElement = (Element) use;

                long dataSize = (long) Double.parseDouble(useElement.getAttribute("size"));
                LinkData data = new LinkData(useElement.getAttribute("file"),
                        (dataSize * params.multiply_by_data) / (1024 * 1024));
                operatorDataMap.put(data.name, data);

                if (useElement.getAttribute("link").equals("output")) {
                    outMap.put(useElement.getAttribute("file"), outMap.size());
                    outFileDataMap.put(useElement.getAttribute("file"), data);
                } else {
                    inMap.put(useElement.getAttribute("file"), inMap.size());
                    inFileDataMap.put(useElement.getAttribute("file"), data);
                    inDataOpMap.put(data.name, name);
                }

                // Find the inputs
                //        if (useElement.getAttribute("link").equals("input")) {
                //          System.out.println("...: " + dataSize);
                //        }
            }
            operatorDataInputMap.put(name, inMap);
            operatorDataOutputMap.put(name, outMap);

            // Set behavior
            OperatorBehavior behavior;
            if (rand.nextDouble() < params.pipeline_percentage) {
                behavior = OperatorBehavior.pipeline;
            } else {
                behavior = OperatorBehavior.store_and_forward;
            }

            ConcreteOperator op =
                    new ConcreteOperator(name, runTimeValue, cpuUtilizationValue, memoryValue,
                            behavior);

            graph.addOperator(op);
            operatorNameMap.put(name, op);
        }

        // Find the dataflow inputs
        for (String out : outFileDataMap.keySet()) {
            inFileDataMap.remove(out);
        }
        for (Map.Entry<String, LinkData> entry : inFileDataMap.entrySet()) {
            String opName = inDataOpMap.get(entry.getKey());
            ConcreteOperator op = operatorNameMap.get(opName);
            LinkData data = entry.getValue();
            op.addDataflowInputFile(new LocalFileData(data.name, data.size_MB));
        }

        // Create the links
        NodeList childList = doc.getElementsByTagName("child");
        for (int c = 0; c < childList.getLength(); c++) {
            Node child = childList.item(c);
            Element childElement = (Element) child;

            String to = childElement.getAttribute("ref");
            ConcreteOperator toOp = operatorNameMap.get(to);
            NodeList parentList = childElement.getElementsByTagName("parent");

            /* Input port names */
            LinkedHashMap<String, Integer> inMap = operatorDataInputMap.get(to);
            for (int p = 0; p < parentList.getLength(); p++) {
                Node parent = parentList.item(p);
                Element parentElement = (Element) parent;

                String from = parentElement.getAttribute("ref");
                ConcreteOperator fromOp = operatorNameMap.get(from);

                /* Output port names */
                LinkedHashMap<String, Integer> outMap = operatorDataOutputMap.get(from);
                LinkedHashMap<String, Link> fromToLinkMap = new LinkedHashMap<>(16);
                for (String in : inMap.keySet()) {
                    if (outMap.containsKey(in)) {
                        String id = fromOp.opID + ":" + toOp.opID;
                        Link link = fromToLinkMap.get(id);
                        LinkData d = operatorDataMap.get(in);
                        LinkData data = new LinkData(d.name + "-" + id, d.size_MB);
                        /* Create a new link if it does not exist */
                        if (link == null) {
                            link = new Link(fromOp, toOp, data);
                            data.updateLinks(link);
                            fromOp.addOutputData(data);
                            toOp.addInputData(data);
                            fromToLinkMap.put(id, link);
                            graph.addLink(link);
                        } else {
                            /* Update the existing link */
                            link.data.size_MB += data.size_MB;
                        }
                        LocalFileData lfd = new LocalFileData(data.name + ".file", data.size_MB);
                        fromOp.addFileOutputData(lfd);
                        toOp.addFileInputData(lfd);
                    }
                }
            }
        }
        return graph;
    }
}
