/**
 * Copyright MaDgIK Group 2010 - 2013.
 */
package madgik.exareme.utils.embedded.process;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author heraldkllapi
 * @author Christoforos Svingos
 */
public class QueryResultStream {
  private static final Logger log = Logger.getLogger(QueryResultStream.class);
  private final BufferedReader input;
  private final InputStreamConsumerThread stderr;
  private final MadisProcess proc;
  int numberOfQueries = 1;
  private String schema = null;
  private boolean end = false;

  BufferedWriter out = null;

  public QueryResultStream(BufferedReader input, InputStreamConsumerThread stderr,
                           MadisProcess proc, int numberOfQueries) {
    this.input = input;
    this.stderr = stderr;
    this.proc = proc;
    this.numberOfQueries = numberOfQueries;
  }

  public Exception getException() {
    return stderr.getException();
  }

  public String getSchema() throws IOException {
    if (schema == null) {
      readSchema();
    }
    return schema;
  }

  public String getNextRecord() throws IOException {
    if (schema == null) {
      readSchema();
    }
    if (end) {
      return null;
    }
    String next = input.readLine();

    if (next == null || next.isEmpty()) {
      end = true;
      proc.queryFinished();
      return null;
    }
    return next;
  }

  public void close() throws IOException {
    while (getNextRecord() != null) {
    }
    proc.queryFinished();
  }

  private void readSchema() throws IOException {
    for (int i = 0; i < this.numberOfQueries - 1; ++i) {
      schema = input.readLine();
      schema = input.readLine();
    }

    schema = input.readLine();
  }
}
