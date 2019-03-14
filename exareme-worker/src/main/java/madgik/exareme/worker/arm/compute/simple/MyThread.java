/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.worker.arm.compute.simple;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Semaphore;

/**
 * @author Χρήστος
 */
public class MyThread implements Runnable {

    private ContainerManager manager;
    private int number;
    //static private Semaphore semaphores;

    public MyThread(Semaphore sem, ContainerManager manager, int number) {
        this.number = number;
        this.manager = manager;
        //semaphores = sem;
    }

    public boolean release_containers() {
        Random random = new Random();
        int maximum = 2;
        int minimum = 1;
        int n = maximum - minimum + 1;
        int i = random.nextInt() % n;
        int randomNum = minimum + i;

        return randomNum != 0;
    }

    @Override
    public void run() {
        ArrayList<String> containers;
        ArrayList<String> released = new ArrayList();
        int i;

        //    Session session = new Session(number, manager);
        //    containers = session.get(number);
        //    for (i = 0; i < containers.size(); i++) {
        //      released.add(containers.get(i));
        //    }
        //    try {
        //      /*
        //       if(session.tryGet(number) == true){
        //       containers = session.get(number);
        //       for(i=0; i<containers.size(); i++){
        //       released.add(containers.get(i));
        //       }
        //       }
        //       else{
        //       containers = session.getAtMost(number);
        //       for(i=0; i<containers.size(); i++){
        //       released.add(containers.get(i));
        //       }
        //       }
        //       */
        //      Thread.sleep(1000);
        //      if (number > 1) {
        //        session.release(released);
        //      }
        //    } catch (InterruptedException ex) {
        //      Logger.getLogger(MyThread.class.getName()).log(Level.SEVERE, null, ex);
        //    }
    }
}
