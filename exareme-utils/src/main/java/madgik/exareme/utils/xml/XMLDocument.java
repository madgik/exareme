/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.xml;

import org.w3c.dom.Document;

/**
 * @author herald
 */
public class XMLDocument {

    private Document dom = null;
    private StringBuilder source = null;

    private Exception exception = null;

    public XMLDocument(Document dom, StringBuilder source) {
        this.dom = dom;
        this.source = source;
    }

    public XMLDocument(Exception exception, StringBuilder source) {
        this.exception = exception;
        this.source = source;
    }

    public boolean hasError() {
        return (exception != null);
    }

    public Document getDom() {
        return dom;
    }

    public StringBuilder getSource() {
        return source;
    }

    public Exception getException() {
        return exception;
    }
}
