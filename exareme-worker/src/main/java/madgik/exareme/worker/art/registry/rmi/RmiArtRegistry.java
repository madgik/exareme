/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.registry.rmi;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.managementBean.RegistryManagment;
import madgik.exareme.worker.art.registry.*;
import madgik.exareme.worker.art.registry.Registerable.Type;
import madgik.exareme.worker.art.registry.policy.DeleteOnExpirationActionPolicy;
import madgik.exareme.worker.art.registry.policy.NotifyOnExpirationActionPolicy;
import madgik.exareme.worker.art.registry.resourceStorage.MemoryResourceStorage;
import madgik.exareme.worker.art.remote.RmiRemoteObject;
import org.apache.log4j.Logger;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.Collection;
import java.util.Iterator;

/**
 * University of Athens /
 * Department of Informatics and Telecommunications.
 *
 * @since 1.0
 */
public class RmiArtRegistry extends RmiRemoteObject<ArtRegistryProxy> implements ArtRegistry {

    private static String BEAN_NAME = "ArtRegistry";
    private static Logger log = Logger.getLogger(RmiArtRegistry.class);
    private MemoryResourceStorage storage;
    private EntityName regEntityName = null;

    public RmiArtRegistry(RegistryResourceStorage registryResourceStorage, EntityName regEntityName)
            throws Exception {

        super(BEAN_NAME);
        this.regEntityName = regEntityName;

        storage = (MemoryResourceStorage) registryResourceStorage;
        super.register();

        RegistryManagment artRegistryManagment = new RegistryManagment(this);
        madgik.exareme.utils.managementBean.ManagementUtil
                .registerMBean(artRegistryManagment, BEAN_NAME);
    }

    public ArtRegistryProxy createProxy() throws RemoteException {
        return new RmiArtRegistryProxy(super.getRegEntryName(), regEntityName);
    }

    public void registerEntry(Registerable r) throws RemoteException {
        try {
            r.getRegisterPolicy().getExpirationPolicy().init();
            storage.store(r);
        } catch (Exception e) {
            throw new RemoteException("Invalid object.", e);
        }
    }

    public Registerable lookupEntry(EntityName epr) throws RemoteException {
        try {
            Registerable r = storage.retrieve(epr);

            if (r.getRegisterPolicy().getExpirationPolicy().hasExpired()) {

                ExpirationActionPolicy eap = r.getRegisterPolicy().getExpirationActionPolicy();

                if (eap instanceof DeleteOnExpirationActionPolicy) {
                    storage.delete(epr);
                    throw new NoSuchObjectException("Object expired.");
                } else if (eap instanceof NotifyOnExpirationActionPolicy) {
                    throw new UnsupportedOperationException(
                            "Notification of expiration not supported yet.");
                }
            }

            return r;
        } catch (Exception e) {
            throw new RemoteException("Object not found.", e);
        }
    }

    public void removeEntry(EntityName epr) throws RemoteException {
        try {
            storage.delete(epr);
        } catch (Exception e) {
            throw new RemoteException("Object not found.", e);
        }
    }

    public ArtRegistryStatus getStatus() throws RemoteException {
        return new ArtRegistryStatus(storage.getResourceStorageStatus());
    }

    public Collection<Registerable> list(Type type) throws RemoteException {
        return storage.retrieveAll(type);
    }

    public String getStorageInfo() throws RemoteException {
        return storage.getInfo();
    }

    public String[] getStoredObjectsNames() throws RemoteException {

        Collection<Registerable> registered = storage.retrieveAll();

        int length = registered.size();

        String names[] = new String[length];

        Iterator<Registerable> it = registered.iterator();

        int i = 0;

        while (it.hasNext()) {
            names[i++] = it.next().getEntityName().getName();
        }

        return names;
    }

    public void stopArtRegistry() throws RemoteException {
        try {
            super.unregister();
            madgik.exareme.utils.managementBean.ManagementUtil.unregisterMBean(BEAN_NAME);
        } catch (Exception e) {
            throw new ServerException("Cannot stop art registry", e);
        }

        log.info("Art registry stopped!");
    }
}
