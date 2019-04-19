/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.arm.compute.rmi;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.utils.properties.AdpProperties;
import madgik.exareme.worker.arm.compute.ArmCompute;
import madgik.exareme.worker.arm.compute.ArmComputeProxy;
import madgik.exareme.worker.arm.compute.session.ArmComputeSession;
import madgik.exareme.worker.arm.compute.session.ArmComputeSessionID;
import madgik.exareme.worker.art.registry.PolicyFactory;
import madgik.exareme.worker.art.registry.RegisterPolicy;
import madgik.exareme.worker.art.remote.RmiObjectProxy;

import java.rmi.RemoteException;

/**
 * @author Herald Kllapi <br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class RmiArmComputeProxy extends RmiObjectProxy<ArmCompute> implements ArmComputeProxy {

    private static final long serialVersionUID = 1L;
    private String regEntryName = null;
    private EntityName entityName = null;
    private RegisterPolicy registerPolicy = null;

    public RmiArmComputeProxy(String regEntryName, EntityName regEntityName) {
        super(regEntryName, regEntityName);
        this.regEntryName = regEntryName;
        this.entityName = new EntityName(regEntryName, null);

        long lifeTime =
                AdpProperties.getArmProps().getLong("arm.compute.rmi.RmiArmCompute.lifetime");
        this.registerPolicy = PolicyFactory.generateTimeExpirationDeletePolicy(lifeTime);
    }

    @Override
    public EntityName getEntityName() {
        return entityName;
    }

    @Override
    public RegisterPolicy getRegisterPolicy() {
        return registerPolicy;
    }

    @Override
    public Type getType() {
        return Type.computeMediator;
    }

    @Override
    public ArmComputeSession createSession() throws RemoteException {
        ArmComputeSessionID sessionID = this.getRemoteObject().createNewSession();
        return new ArmComputeSession(sessionID, this.getRemoteObject());
    }
}
