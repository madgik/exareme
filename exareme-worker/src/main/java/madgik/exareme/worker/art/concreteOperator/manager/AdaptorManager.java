/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.concreteOperator.manager;

import madgik.exareme.common.art.AdaptorStatistics;
import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorInfo;
import madgik.exareme.worker.art.container.adaptor.CombinedReadAdaptorProxy;
import madgik.exareme.worker.art.container.adaptor.CombinedWriteAdaptorProxy;
import madgik.exareme.worker.art.container.adaptor.ReadAdaptorWrapper;
import madgik.exareme.worker.art.container.adaptor.WriteAdaptorWrapper;
import madgik.exareme.worker.art.container.adaptor.monitor.ReadRmiStreamAdaptorProxyMonitor;
import madgik.exareme.worker.art.container.adaptor.monitor.ReadSocketStreamAdaptorProxyMonitor;
import madgik.exareme.worker.art.container.adaptor.monitor.WriteRmiStreamAdaptorProxyMonitor;
import madgik.exareme.worker.art.container.adaptor.monitor.WriteSocketStreamAdaptorProxyMonitor;
import madgik.exareme.worker.art.container.netMgr.session.NetSessionSimple;
import madgik.exareme.worker.art.parameter.Parameter;
import madgik.exareme.worker.art.parameter.Parameters;
import org.apache.log4j.Logger;

import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.util.*;

/**
 * @author Herald Kllapi <br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class AdaptorManager {

    private static Logger log = Logger.getLogger(AdaptorManager.class);
    private List<CombinedWriteAdaptorProxy> outputList = new ArrayList<CombinedWriteAdaptorProxy>();
    private List<CombinedReadAdaptorProxy> inputList = new ArrayList<CombinedReadAdaptorProxy>();
    private Map<String, CombinedWriteAdaptorProxy> outputMap =
        new HashMap<String, CombinedWriteAdaptorProxy>();
    private Map<String, CombinedReadAdaptorProxy> inputMap =
        new HashMap<String, CombinedReadAdaptorProxy>();
    private Map<String, List<String>> inputParamMap = new HashMap<String, List<String>>();
    private Map<String, List<String>> outputParamMap = new HashMap<String, List<String>>();
    private HashMap<String, Parameters> inputParameters = new HashMap<String, Parameters>();
    private HashMap<String, Parameters> outputParameters = new HashMap<String, Parameters>();
    private ArrayList<Boolean> closedInputs = new ArrayList<Boolean>();
    private int closedInputCount = 0;
    private ArrayList<Boolean> closedOutputs = new ArrayList<Boolean>();
    private int closedOuputCount = 0;
    private int outputCount = 0;
    private int inputCount = 0;
    private SessionManager sessionManager = null;
    private List<ConcreteOperatorInfo> inputInfo = new ArrayList<ConcreteOperatorInfo>();
    private List<ConcreteOperatorInfo> outputInfo = new ArrayList<ConcreteOperatorInfo>();

    public AdaptorManager(int outputCount, int inputCount, SessionManager sessionManager) {
        this.outputCount = outputCount;
        this.inputCount = inputCount;
        this.sessionManager = sessionManager;
    }

    public int getInputCount() {
        return inputList.size();
    }

    public int getOutputCount() {
        return outputList.size();
    }

    public void addWriteAdaptor(CombinedWriteAdaptorProxy output, String adaptorName,
        String portName, Parameters params, boolean remote) throws RemoteException {
        output.writeSocketStreamAdaptorProxy.setNetSession(new NetSessionSimple());
        AdaptorStatistics stats = sessionManager.getSessionStatistics()
            .createAdaptorStatistics(adaptorName, sessionManager.getOperatorName(), portName);
        if (remote) {
            stats.setRemote();
        } else {
            stats.setLocal();
        }
        // Create monitors for the adaptors
        output.writeRmiStreamAdaptorProxy =
            new WriteRmiStreamAdaptorProxyMonitor(output.writeRmiStreamAdaptorProxy, stats,
                sessionManager.getOperatorStatistics());
        output.writeSocketStreamAdaptorProxy =
            new WriteSocketStreamAdaptorProxyMonitor(output.writeSocketStreamAdaptorProxy, stats,
                sessionManager.getOperatorStatistics());
        this.outputList.add(output);
        this.outputMap.put(portName, output);
        this.closedOutputs.add(false);
        // Add the parameter indexes
        for (Parameter param : params) {
            String id = param.getName() + ":" + param.getValue();
            List<String> outputs = outputParamMap.get(id);
            if (outputs == null) {
                outputs = new ArrayList<String>();
                outputParamMap.put(id, outputs);
            }
            outputs.add(portName);
        }
        outputParameters.put(portName, params);
        outputInfo.add(new ConcreteOperatorInfo(sessionManager.getOperatorName()));
    }

    public void addReadAdaptor(CombinedReadAdaptorProxy input, String adaptorName, String portName,
        Parameters params, boolean remote) throws RemoteException {
        input.readSocketStreamAdaptorProxy.setNetSession(new NetSessionSimple());
        AdaptorStatistics stats = sessionManager.getSessionStatistics()
            .createAdaptorStatistics(adaptorName, portName, sessionManager.getOperatorName());
        if (remote) {
            stats.setRemote();
        } else {
            stats.setLocal();
        }
        input.readRmiStreamAdaptorProxy =
            new ReadRmiStreamAdaptorProxyMonitor(input.readRmiStreamAdaptorProxy, stats,
                sessionManager.getOperatorStatistics());
        input.readSocketStreamAdaptorProxy =
            new ReadSocketStreamAdaptorProxyMonitor(input.readSocketStreamAdaptorProxy, stats,
                sessionManager.getOperatorStatistics());
        this.inputList.add(input);
        this.inputMap.put(portName, input);
        this.closedInputs.add(false);
        // Add the parameter indexes
        for (Parameter param : params) {
            String id = param.getName() + ":" + param.getValue();
            List<String> inputs = inputParamMap.get(id);
            if (inputs == null) {
                inputs = new ArrayList<String>();
                inputParamMap.put(id, inputs);
            }
            inputs.add(portName);
        }
        inputParameters.put(portName, params);
        inputInfo.add(new ConcreteOperatorInfo(sessionManager.getOperatorName()));
    }

    public WriteAdaptorWrapper getWriteStreamAdaptor(int num) {
        return new WriteAdaptorWrapper(outputList.get(num));
    }

    public ReadAdaptorWrapper getReadStreamAdaptor(int num) {
        return new ReadAdaptorWrapper(inputList.get(num));
    }

    public List<String> getReadStreamAdaptorNamesByParam(String param, String value) {
        return inputParamMap.get(param + ":" + value);
    }

    public WriteAdaptorWrapper getWriteStreamAdaptor(String name) {
        return new WriteAdaptorWrapper(outputMap.get(name));
    }

    public List<String> getWriteStreamAdaptorNamesByParam(String param, String value) {
        return outputParamMap.get(param + ":" + value);
    }

    public ReadAdaptorWrapper getReadStreamAdaptor(String name) {
        return new ReadAdaptorWrapper(inputMap.get(name));
    }

    public Set<String> getInputNames() {
        return inputMap.keySet();
    }

    public Parameters getInputParams(String name) {
        return inputParameters.get(name);
    }

    public Parameters getOutputParams(String name) {
        return outputParameters.get(name);
    }

    public Set<String> getOutputNames() {
        return outputMap.keySet();
    }

    public void closeOutput(int num) throws RemoteException {
        log.debug("Closing output : " + num);
        try {
            outputList.get(num).close();
            closedOuputCount++;
            closedOutputs.set(num, Boolean.TRUE);
        } catch (Exception e) {
            throw new AccessException("Cannot close output: " + num, e);
        }
    }

    public void closeInput(int num) throws RemoteException {
        log.debug("Closing input : " + num);
        try {
            inputList.get(num).close();
            closedInputCount++;
            closedInputs.set(num, Boolean.TRUE);
        } catch (Exception e) {
            throw new AccessException("Cannot close input: " + num, e);
        }
    }

    /**
     * Close the output streams.
     *
     * @throws RemoteException
     */
    public void closeAllOutputs() throws RemoteException {
        for (int i = 0; i < getOutputCount(); i++) {
            if (isOutputClosed(i) == false) {
                this.closeOutput(i);
            }
        }
    }

    /**
     * Close the inputs streams.
     *
     * @throws RemoteException
     */
    public void closeAllInputs() throws RemoteException {
        for (int i = 0; i < getInputCount(); i++) {
            if (isInputClosed(i) == false) {
                this.closeInput(i);
            }
        }
    }

    public boolean isOutputClosed(int num) {
        return closedOutputs.get(num);
    }

    public boolean isInputClosed(int num) {
        return closedInputs.get(num);
    }

    public ConcreteOperatorInfo getInputInfo(int num) {
        return inputInfo.get(num);
    }

    public ConcreteOperatorInfo getOutputInfo(int num) {
        return outputInfo.get(num);
    }
}
