/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.proc;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author heraldkllapi
 */
public class ProcessDefn implements Serializable {

    private final List<String> args = new LinkedList<String>();
    private String process = null;
    private String directory = "/tmp/";
    private String stdin = "";

    public ProcessDefn() {
    }

    public String getProcess() {
        if (process == null) {
            throw new IllegalStateException("Process not set");
        }
        return process;
    }

    public ProcessDefn setProcess(String proc) {
        this.process = proc;
        return this;
    }

    public String getDirectory() {
        return directory;
    }

    public ProcessDefn setDirectory(String dir) {
        this.directory = dir;
        return this;
    }

    public List<String> getArgs() {
        return args;
    }

    public ProcessDefn addArg(String arg) {
        args.add(arg);
        return this;
    }

    public ProcessDefn addArgs(String... args) {
        this.args.addAll(Arrays.asList(args));
        return this;
    }

    public String getStdin() {
        return stdin;
    }

    public ProcessDefn setStdin(String stdin) {
        this.stdin = stdin;
        return this;
    }
}
