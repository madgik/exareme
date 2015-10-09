/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.proc;

import madgik.exareme.utils.file.InputStreamConsumerThread;

import java.io.File;
import java.util.LinkedList;

/**
 * @author heraldkllapi
 */
public class ProcessUtil {

    public static ProcessResult Exec(ProcessDefn defn) throws Exception {
        ProcessResult result = new ProcessResult();

        LinkedList<String> args = new LinkedList<String>();
        args.add(defn.getProcess());
        args.addAll(defn.getArgs());

        ProcessBuilder pb = new ProcessBuilder(args);
        pb.directory(new File(defn.getDirectory()));
        Process p = pb.start();

        p.getOutputStream().write(defn.getStdin().getBytes());
        p.getOutputStream().flush();
        p.getOutputStream().close();

        InputStreamConsumerThread stdout = new InputStreamConsumerThread(p.getInputStream(), false);
        stdout.start();

        InputStreamConsumerThread stderr = new InputStreamConsumerThread(p.getErrorStream(), false);
        stderr.start();

        result.exitCode = p.waitFor();
        stdout.join();
        stderr.join();

        result.stdout = stdout.getOutput();
        result.stderr = stderr.getOutput();

        return result;
    }
}
