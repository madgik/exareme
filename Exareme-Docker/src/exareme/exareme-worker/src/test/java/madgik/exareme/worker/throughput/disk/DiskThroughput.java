package madgik.exareme.worker.throughput.disk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * @author herald
 */
public class DiskThroughput {

    public static void main(String[] args) throws Exception {
        String path = args[0];
        int bufferSize = Integer.parseInt(args[1]);
        int times = Integer.parseInt(args[2]);

        ArrayList<Thread> workers = new ArrayList<Thread>();
        for (int i = 3; i < args.length; ++i) {
            if (args[i].equals("w")) {
                workers.add(new WriteThread(new File(path + "/file" + i), bufferSize, times));
            } else {
                workers.add(new ReadThread(new File(path + "/file" + i), bufferSize, times));
            }
        }

        System.out.println("Number of workers is : " + workers.size());

        for (Thread t : workers) {
            t.start();
        }

        for (Thread t : workers) {
            t.join();
        }
    }
}


class WriteThread extends Thread {

    File file = null;
    int bufferSize;
    int times;
    FileOutputStream fos = null;

    public WriteThread(File file, int bufferSize, int times) throws FileNotFoundException {
        this.file = file;
        this.bufferSize = bufferSize;
        this.times = times;
        this.fos = new FileOutputStream(file);
    }

    @Override
    public void run() {
        try {
            byte[] buffer = new byte[bufferSize];
            for (int i = 0; i < times; ++i) {
                fos.write(buffer);
            }

            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


class ReadThread extends Thread {

    File file = null;
    int bufferSize;
    int times;
    FileInputStream fis = null;

    public ReadThread(File file, int bufferSize, int times) throws FileNotFoundException {
        this.file = file;
        this.bufferSize = bufferSize;
        this.times = times;
        this.fis = new FileInputStream(file);
    }

    @Override
    public void run() {
        try {
            byte[] buffer = new byte[bufferSize];
            for (int i = 0; i < times; ++i) {
                fis.read(buffer);
            }

            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
