/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.rmi;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.utils.properties.AdpProperties;
import madgik.exareme.worker.art.executionEngine.ExecutionEngine;
import madgik.exareme.worker.art.executionEngine.ExecutionEngineProxy;
import madgik.exareme.worker.art.executionEngine.session.ExecutionEngineSession;
import madgik.exareme.worker.art.registry.PolicyFactory;
import madgik.exareme.worker.art.registry.RegisterPolicy;
import madgik.exareme.worker.art.remote.RmiObjectProxy;

import java.rmi.RemoteException;
import java.rmi.ServerException;

/**
 * @author Herald Kllapi <br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class RmiExecutionEngineProxy extends RmiObjectProxy<ExecutionEngine>
    implements ExecutionEngineProxy {

    private static final long serialVersionUID = 1L;
    private String ip = null;
    private String regEntryName = null;
    private EntityName entityName = null;
    private RegisterPolicy registerPolicy = null;

    public RmiExecutionEngineProxy(String ip, String regEntryName, EntityName regEntityName)
        throws RemoteException {
        super(regEntryName, regEntityName);

        if (ip == null) {
            throw new ServerException("IP is null");
        }

        this.ip = ip;
        this.regEntryName = regEntryName;
        this.entityName = new EntityName(regEntryName, ip);
        long lifeTime = AdpProperties.getArtProps()
            .getLong("art.executionEngine.rmi.RmiExecutionEngine.lifetime");
        this.registerPolicy = PolicyFactory.generateTimeExpirationDeletePolicy(lifeTime);
    }

    @Override public EntityName getEntityName() {
        return entityName;
    }

    @Override public RegisterPolicy getRegisterPolicy() {
        return registerPolicy;
    }

    @Override public Type getType() {
        return Type.executionEngine;
    }

    @Override public ExecutionEngineSession createSession() throws RemoteException {
        return new ExecutionEngineSession(this.getRemoteObject());
    }
}
