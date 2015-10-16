/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.xml;

import madgik.exareme.utils.units.Metrics;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.util.Iterator;

/**
 * @author herald
 */
public class XMLFileStreamIterator implements Iterator<XMLDocument> {

    private BufferedReader input = null;
    private StringBuilder dtd = null;
    private String delimiterLine = null;

    private String currentLine = null;

    private DocumentBuilderFactory builderFactory = null;
    private DocumentBuilder builder = null;

    private String preffix = null;
    private String suffix = null;

    public XMLFileStreamIterator(BufferedReader input, StringBuilder dtd, String delimiterLine,
        String preffix, String suffix) throws Exception {
        this.input = input;
        this.dtd = dtd;
        this.delimiterLine = delimiterLine;
        this.preffix = preffix;
        this.suffix = suffix;

        this.builderFactory = DocumentBuilderFactory.newInstance();
        this.builder = this.builderFactory.newDocumentBuilder();

        this.currentLine = this.input.readLine();
    }

    @Override public boolean hasNext() {
        return (currentLine != null);
    }

    @Override public XMLDocument next() {
        StringBuilder buffer = new StringBuilder(Metrics.KB);
        buffer.append(dtd);

        buffer.append(preffix);
        buffer.append(currentLine);

        try {
            do {
                currentLine = input.readLine();
                buffer.append(currentLine);

                // No more than 1 MB
                if (buffer.length() > Metrics.MB) {
                    break;
                }
            } while (currentLine != null
                && currentLine.trim().equalsIgnoreCase(delimiterLine) != true);

            buffer.append(suffix);

            currentLine = input.readLine();

            ByteArrayInputStream stream = new ByteArrayInputStream(buffer.toString().getBytes());
            Document dom = builder.parse(stream);

            return new XMLDocument(dom, buffer);
        } catch (Exception e) {
            return new XMLDocument(e, buffer);
        }
    }

    @Override public void remove() {
        throw new UnsupportedOperationException("Not supported!");
    }
}
