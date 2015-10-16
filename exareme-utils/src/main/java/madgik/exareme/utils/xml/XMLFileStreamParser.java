/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.xml;

import madgik.exareme.utils.properties.AdpProperties;
import madgik.exareme.utils.units.Metrics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Iterator;

/**
 * @author herald
 */
public class XMLFileStreamParser {

    private final int ioBufferSize;
    private String delimiterLine = null;

    public XMLFileStreamParser(String delimiterLine) {
        this.delimiterLine = delimiterLine;
        ioBufferSize =
            AdpProperties.getArtProps().getInt("art.container.ioBufferSize_kb") * Metrics.KB;
    }

    public Iterator<XMLDocument> parseFile(String file, String dtd, String preffix, String suffix)
        throws Exception {
        BufferedReader input = new BufferedReader(new FileReader(file), ioBufferSize);

        // Read the dtd
        //    BufferedReader dtdInput = new BufferedReader(
        //            new FileReader(dtd), 1024 * 1024);

        StringBuilder dtdBuffer = new StringBuilder();
        dtdBuffer.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
        dtdBuffer.append("<!DOCTYPE dblp SYSTEM \"" + dtd + "\">\n");

        //    String line = dtdInput.readLine();
        //    while (line != null) {
        //      dtdBuffer.append(line);
        //      line = dtdInput.readLine();
        //    }

        dtdBuffer.append("\n\n");

        return new XMLFileStreamIterator(input, dtdBuffer, delimiterLine, preffix, suffix);
    }
}
