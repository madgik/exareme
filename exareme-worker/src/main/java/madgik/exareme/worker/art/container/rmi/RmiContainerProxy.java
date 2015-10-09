/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.rmi;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.utils.properties.AdpProperties;
import madgik.exareme.worker.art.container.Container;
import madgik.exareme.worker.art.container.ContainerProxy;
import madgik.exareme.worker.art.container.ContainerSession;
import madgik.exareme.worker.art.registry.PolicyFactory;
import madgik.exareme.worker.art.registry.RegisterPolicy;
import madgik.exareme.worker.art.remote.RmiObjectProxy;

import java.rmi.RemoteException;
import java.rmi.ServerException;

/**
 * University of Athens /
 * Department of Informatics and Telecommunications.
 *
 * @since 1.0
 */
public class RmiContainerProxy extends RmiObjectProxy<Container> implements ContainerProxy {

    private static final long serialVersionUID = 1L;
    private String ip = null;
    private String regEntryName = null;
    private EntityName entityName = null;
    private RegisterPolicy registerPolicy = null;


    public RmiContainerProxy(String ip, String regEntryName, EntityName regEntityName)
        throws RemoteException {
        super(regEntryName, regEntityName);
        this.ip = ip;
        if (ip == null) {
            throw new ServerException("IP is null");
        }
        this.regEntryName = regEntryName;
        this.entityName = new EntityName(regEntryName, ip, regEntityName.getPort(),
            regEntityName.getDataTransferPort());
        long lifeTime =
            AdpProperties.getArtProps().getLong("art.container.rmi.RmiContainer.lifetime");
        this.registerPolicy = PolicyFactory.generateTimeExpirationDeletePolicy(lifeTime);
    }

    @Override public EntityName getEntityName() {
        return entityName;
    }

    @Override public RegisterPolicy getRegisterPolicy() {
        return registerPolicy;
    }

    @Override public Type getType() {
        return Type.container;
    }

    @Override public ContainerSession createSession(ContainerSessionID containerSessionID,
        PlanSessionID sessionID) throws RemoteException {
        return new ContainerSession(this, containerSessionID, sessionID);
    }

    @Override public void destroySessions(PlanSessionID sessionID) throws RemoteException {
        //log.debug("edooo");

        getRemoteObject().destroySessions(sessionID);
    }

    @Override public void destroyAllSessions() throws RemoteException {
        getRemoteObject().destroyAllSessions();
    }

    @Override public void destroyContainerSession(ContainerSessionID containerSessionID,
        PlanSessionID sessionID) throws RemoteException {
        getRemoteObject().destroyContainerSession(containerSessionID, sessionID);
    }
}
