/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.xml;

import madgik.exareme.utils.association.Triple;
import org.apache.log4j.Logger;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author herald
 */
public class XMLDemo {

    private static Logger log = Logger.getLogger(XMLDemo.class);

    public static void main(String[] args) throws Exception {
        XMLFileStreamParser parser = new XMLFileStreamParser("");

        Iterator<XMLDocument> iterator = parser.parseFile("/home/herald/work/datasets/dblp.xml",
            "/home/herald/work/datasets/dblp_bht.dtd", "<dblp>", "</dblp>");

        Exporter exporter = new Exporter("/tmp/dblp/", 100000);

        int correctCount = 0;
        int errorCount = 0;
        int entityCount = 0;

        HashMap<String, Integer> entities = new HashMap<String, Integer>();

        long dataSize = 0;
        while (iterator.hasNext()) {
            XMLDocument dom = iterator.next();
            if (dom.hasError()) {
                log.debug(dom.getException().toString());
                //        log.debug(dom.getSource().toString());

                errorCount++;

                //        if (errorCount > 5) {
                //          break;
                //        }
            } else {
                correctCount++;

                NodeList nodes = dom.getDom().getDocumentElement().getChildNodes();
                entityCount += nodes.getLength();

                for (int i = 0; i < nodes.getLength(); ++i) {
                    Node n = nodes.item(i);
                    String nodeName = new String(n.getNodeName());

                    Integer count = entities.get(nodeName);
                    if (count == null) {
                        count = 0;
                    }

                    ++count;
                    entities.put(nodeName, count);
                }
                //        log.debug(dom.getDom().getDocumentElement().getNodeName());

                exporter.export(dom);
            }

            dataSize += dom.getSource().length();
            log.debug(entityCount + " / " + correctCount + " : " + errorCount);

            log.debug(entities);
        }

        log.debug("dataSize = " + dataSize);
        exporter.close();
    }
}


class Exporter {
    HashMap<String, Triple<Integer, Integer, BufferedWriter>> outMap = null;
    private File root = null;
    private int maxFileRecords = 0;

    public Exporter(String folder, int maxFileRecords) {
        root = new File(folder);
        root.mkdirs();

        this.maxFileRecords = maxFileRecords;
        outMap = new HashMap<String, Triple<Integer, Integer, BufferedWriter>>();
    }

    public void export(XMLDocument dom) throws IOException {
        NodeList nodes = dom.getDom().getDocumentElement().getChildNodes();

        for (int i = 0; i < nodes.getLength(); ++i) {
            Node n = nodes.item(i);
            String nodeName = new String(n.getNodeName());

            Triple<Integer, Integer, BufferedWriter> out = outMap.get(nodeName);
            if (out == null) {
                out = new Triple<Integer, Integer, BufferedWriter>(0, 0,
                    new BufferedWriter(new FileWriter(new File(root, nodeName + "." + 0))));
                outMap.put(nodeName, out);
            }

            NamedNodeMap attrList = n.getAttributes();
            if (attrList != null) {
                for (int a = 0; a < attrList.getLength(); ++a) {
                    try {
                        Node attr = attrList.item(a);
                        out.c.write(
                            attr.getNodeName() + "=" + attr.getTextContent().replaceAll("\t", " ")
                                + "\t");
                    } catch (Exception e) {
                    }
                }
            }

            NodeList attrs = n.getChildNodes();
            if (attrs != null) {
                for (int a = 0; a < attrs.getLength(); ++a) {
                    try {
                        Node attr = attrs.item(a);
                        out.c.write(
                            attr.getNodeName() + "=" + attr.getTextContent().replaceAll("\t", " ")
                                + "\t");
                    } catch (Exception e) {
                    }
                }
            }

            out.c.newLine();
            out.b++;

            if (out.b >= maxFileRecords) {
                out.c.close();

                out.a++;
                out.b = 0;
                out.c = new BufferedWriter(new FileWriter(new File(root, nodeName + "." + out.a)));
            }
        }
    }

    public void close() throws IOException {
        for (Triple<Integer, Integer, BufferedWriter> w : outMap.values()) {
            w.c.close();
        }
    }
}
