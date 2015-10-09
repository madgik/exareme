/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.operatorMgr.thread;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.common.art.entity.OperatorImplementationEntity;
import madgik.exareme.worker.art.concreteOperator.AbstractOperatorImpl;
import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.container.adaptor.CombinedReadAdaptorProxy;
import madgik.exareme.worker.art.container.adaptor.CombinedWriteAdaptorProxy;
import madgik.exareme.worker.art.container.jobQueue.JobQueueInterface;
import madgik.exareme.worker.art.executionEngine.session.PlanSessionReportID;
import madgik.exareme.worker.art.parameter.Parameters;
import org.apache.log4j.Logger;

import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author herald
 */
public class TCOMContainerSession {

    private static Logger log = Logger.getLogger(TCOMContainerSession.class);
    JobQueueInterface jobQueueInterface;
    private long operatorCount = 0;
    private HashMap<ConcreteOperatorID, AbstractOperatorImpl> operatorMap =
        new HashMap<ConcreteOperatorID, AbstractOperatorImpl>();
    private HashMap<ConcreteOperatorID, OperatorExecutionThread> operatorExecutionMap =
        new HashMap<ConcreteOperatorID, OperatorExecutionThread>();
    private HashMap<ConcreteOperatorID, OperatorImplementationEntity> operatorEntityMap =
        new HashMap<ConcreteOperatorID, OperatorImplementationEntity>();
    private HashMap<ConcreteOperatorID, PlanSessionReportID> sessionReportMap =
        new HashMap<ConcreteOperatorID, PlanSessionReportID>();
    private HashMap<ConcreteOperatorID, Future<?>> futureMap =
        new HashMap<ConcreteOperatorID, Future<?>>();
    // Executor service
    private ExecutorService execService = null;
    //  Used for dynamic class loading
    private ClassLoader classLoader = null;
    private HashMap<URL, ClassLoader> classLoaderURLMap = new HashMap<URL, ClassLoader>();
    private HashMap<OperatorImplementationEntity, Class<?>> loadedClassMap =
        new HashMap<OperatorImplementationEntity, Class<?>>();
    private ContainerSessionID containerSessionID = null;
    private PlanSessionID sessionID = null;

    public TCOMContainerSession(ContainerSessionID containerSessionID, PlanSessionID sessionID,
        JobQueueInterface jobQueueInterface) {
        this.containerSessionID = containerSessionID;
        this.sessionID = sessionID;
        this.operatorCount = 0;
        this.classLoader = ClassLoader.getSystemClassLoader();
        this.execService = Executors.newCachedThreadPool();
        this.jobQueueInterface = jobQueueInterface;
    }

    public long getNextOperatorID() {
        operatorCount++;
        return operatorCount;
    }

    public Class<?> getOperatorClass(OperatorImplementationEntity entity) throws RemoteException {
        try {
            Class<?> opClass = loadedClassMap.get(entity);

            if (opClass == null) {
                ClassLoader loader = createClassLoader(entity);
                opClass = loader.loadClass(entity.getClassName());
                loadedClassMap.put(entity, opClass);
            }

            return opClass;
        } catch (Exception e) {
            throw new ServerException("Cannot load operator", e);
        }
    }

    public ClassLoader createClassLoader(OperatorImplementationEntity entity)
        throws RemoteException {
        if (entity.getLocations() != null) {
            for (URL url : entity.getLocations()) {
                if (!classLoaderURLMap.containsKey(url)) {
                    ClassLoader newLoader = new URLClassLoader(new URL[] {url}, classLoader);
                    classLoaderURLMap.put(url, newLoader);
                    classLoader = newLoader;
                }
            }
        }

        return classLoader;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public long getOperatorCount() {
        return operatorCount;
    }

    public void addInstance(ConcreteOperatorID opID, PlanSessionReportID sessionReportID,
        OperatorImplementationEntity operator, AbstractOperatorImpl op,
        OperatorExecutionThread executionThread) throws RemoteException {
        operatorExecutionMap.put(opID, executionThread);
        operatorMap.put(opID, op);
        operatorEntityMap.put(opID, operator);
        sessionReportMap.put(opID, sessionReportID);
    }

    public void startInstance(ConcreteOperatorID opID) throws RemoteException {
        try {
            checkNotRunning(opID);
            OperatorExecutionThread executionThread = operatorExecutionMap.get(opID);
            //JC na vro pou ftiaxnontai ta thread


            if (executionThread == null) {
                throw new NoSuchObjectException("Operator not found");
            }

            Future<?> future = execService.submit(executionThread);
            futureMap.put(opID, future);
        } catch (Exception e) {
            throw new ServerException("Cannot stop operator", e);
        }
    }

    public void stopInstance(ConcreteOperatorID opID) throws RemoteException {
        try {
            log.debug("Stopping instance : " + opID.uniqueID + " " + opID.operatorName);
            OperatorExecutionThread exec = operatorExecutionMap.get(opID);
            Future<?> future = futureMap.remove(opID);
            if (future == null) {
                throw new NoSuchObjectException("Operator not running");
            }
            exec.shutdown();
            if (future.cancel(true) == false) {
                throw new ServerException("Cannot stop operator.");
            }

            AbstractOperatorImpl op = operatorMap.get(opID);
            op.cleanResources();

        } catch (Exception e) {
            throw new ServerException("Cannot stop operator: " + opID.operatorName, e);
        }
    }

    public void destroyInstance(ConcreteOperatorID opID) throws RemoteException {
        try {
            try {
                stopInstance(opID);
            } catch (Exception e) {
                log.debug("Operator not running");
            }

            jobQueueInterface.freeResources(opID);
            // TODO(DSD): free resources?

            operatorExecutionMap.remove(opID);
            operatorMap.remove(opID);
            operatorEntityMap.remove(opID);
            sessionReportMap.remove(opID);
        } catch (Exception e) {
            throw new ServerException("Cannot destroy instance", e);
        }
    }

    public void addReadAdaptor(ConcreteOperatorID opID, CombinedReadAdaptorProxy adaptor,
        String adaptorName, String portName, Parameters parameters, boolean remote)
        throws RemoteException {
        try {
            checkNotRunning(opID);

            AbstractOperatorImpl op = operatorMap.get(opID);
            if (op == null) {
                throw new NoSuchObjectException("Operator not found");
            }

            op.getAdaptorManager()
                .addReadAdaptor(adaptor, adaptorName, portName, parameters, remote);
        } catch (Exception e) {
            throw new ServerException("Cannot ses input", e);
        }
    }

    public void addWriteAdaptor(ConcreteOperatorID opID, CombinedWriteAdaptorProxy adaptor,
        String adaptorName, String portName, Parameters parameters, boolean remote)
        throws RemoteException {
        try {
            checkNotRunning(opID);

            AbstractOperatorImpl op = operatorMap.get(opID);
            if (op == null) {
                throw new NoSuchObjectException("Operator not found");
            }

            op.getAdaptorManager()
                .addWriteAdaptor(adaptor, adaptorName, portName, parameters, remote);
        } catch (Exception e) {
            throw new ServerException("Cannot se output", e);
        }
    }

    private void checkNotRunning(ConcreteOperatorID opID) throws RemoteException {
        if (futureMap.containsKey(opID)) {
            throw new ServerException("Cannot manipulate running thread");
        }
    }

    public int destroySession() throws RemoteException {

        try {
            log.debug("Destroying instances ...");
            ArrayList<ConcreteOperatorID> opIDs =
                new ArrayList<ConcreteOperatorID>(operatorMap.keySet());
            for (ConcreteOperatorID id : opIDs) {
                destroyInstance(id);
                jobQueueInterface.freeResources(id); // TODO(DSD): free resources?
            }

            execService.shutdownNow();

            int count = operatorExecutionMap.size();
            operatorMap.clear();
            operatorEntityMap.clear();
            sessionReportMap.clear();
            operatorExecutionMap.clear();
            classLoaderURLMap.clear();
            loadedClassMap.clear();

            return count;
        } catch (Exception e) {
            throw new ServerException("Cannot destroy session", e);
        }
    }
}
