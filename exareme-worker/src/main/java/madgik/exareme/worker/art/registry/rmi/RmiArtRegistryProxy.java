/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.registry.rmi;

//import madgik.exareme.db.arm.compute.ArmComputeProxy;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.utils.properties.AdpProperties;
import madgik.exareme.worker.arm.compute.ArmComputeProxy;
import madgik.exareme.worker.art.container.ContainerProxy;
import madgik.exareme.worker.art.executionEngine.ExecutionEngineProxy;
import madgik.exareme.worker.art.registry.ArtRegistry;
import madgik.exareme.worker.art.registry.ArtRegistryProxy;
import madgik.exareme.worker.art.registry.ArtRegistryProxyCache;
import madgik.exareme.worker.art.registry.Registerable;
import madgik.exareme.worker.art.remote.RmiObjectProxy;

import java.rmi.RemoteException;

/**
 * University of Athens /
 * Department of Informatics and Telecommunications.
 *
 * @since 1.0
 */
public class RmiArtRegistryProxy extends RmiObjectProxy<ArtRegistry> implements ArtRegistryProxy {
    private static final long serialVersionUID = 1L;

    private boolean caching;
    private int cacheSize;
    private ArtRegistryProxyCache cache;

    public RmiArtRegistryProxy(String regEntryName, EntityName regEntityName) {
        super(regEntryName, regEntityName);

        caching =
            AdpProperties.getArtProps().getBoolean("art.registry.rmi.RmiArtRegistryProxy.caching");

        if (caching) {
            cacheSize = AdpProperties.getArtProps()
                .getInt("art.registry.rmi.RmiArtRegistryProxy.cacheSize");

            cache = new ArtRegistryProxyCache(cacheSize);
        }
    }

    public void registerContainer(ContainerProxy containerProxy) throws RemoteException {
        ArtRegistry reg = super.getRemoteObject();

        reg.registerEntry(containerProxy);

        if (caching && containerProxy != null) {
            cache.put(containerProxy);
        }
    }

    public ContainerProxy lookupContainer(EntityName epr) throws RemoteException {
        ContainerProxy cp;

        if (caching) {
            cp = (ContainerProxy) cache.get(epr);

            if (cp != null) {
                return cp;
            }
        }

        cp = (ContainerProxy) super.getRemoteObject().lookupEntry(epr);

        if (cp != null && caching) {
            cache.put(cp);
        }

        return cp;
    }

    public ContainerProxy[] getContainers() throws RemoteException {

        ContainerProxy containers[] = super.getRemoteObject().
            list(Registerable.Type.container).toArray(new ContainerProxy[] {});

        if (caching) {
            for (ContainerProxy c : containers) {
                cache.get(c.getEntityName());
            }
        }

        return containers;
    }

    public void removeContainer(EntityName epr) throws RemoteException {
        ArtRegistry reg = super.getRemoteObject();

        reg.removeEntry(epr);

        if (caching) {
            cache.delete(epr);
        }
    }

    public void removeLogger(EntityName epr) throws RemoteException {
        ArtRegistry reg = super.getRemoteObject();

        reg.removeEntry(epr);

        if (caching) {
            cache.delete(epr);
        }
    }

    public void registerExecutionEngine(ExecutionEngineProxy executionEngineProxy)
        throws RemoteException {
        ArtRegistry reg = super.getRemoteObject();

        reg.registerEntry(executionEngineProxy);

        if (caching) {
            cache.put(executionEngineProxy);
        }
    }

    public ExecutionEngineProxy lookupExecutionEngine(EntityName epr) throws RemoteException {

        ExecutionEngineProxy eep;

        if (caching) {
            eep = (ExecutionEngineProxy) cache.get(epr);

            if (eep != null) {
                return eep;
            }
        }

        eep = (ExecutionEngineProxy) super.getRemoteObject().lookupEntry(epr);

        if (eep != null && caching) {
            cache.put(eep);
        }

        return eep;
    }

    public ExecutionEngineProxy[] getExecutionEngines() throws RemoteException {
        ExecutionEngineProxy executionEngines[] = super.getRemoteObject().
            list(Registerable.Type.executionEngine).toArray(new ExecutionEngineProxy[] {});

        if (caching) {
            for (ExecutionEngineProxy eep : executionEngines) {
                cache.get(eep.getEntityName());
            }
        }

        return executionEngines;
    }

    public void removeExecutionEngine(EntityName epr) throws RemoteException {
        ArtRegistry reg = super.getRemoteObject();

        reg.removeEntry(epr);

        if (caching) {
            cache.delete(epr);
        }
    }

    public void registerComputeMediator(ArmComputeProxy resourceMediatorProxy)
        throws RemoteException {
        ArtRegistry reg = super.getRemoteObject();

        reg.registerEntry(resourceMediatorProxy);

        if (caching) {
            cache.put(resourceMediatorProxy);
        }
    }

    public ArmComputeProxy lookupComputeMediator(EntityName name) throws RemoteException {
        ArmComputeProxy rmp;

        if (caching) {
            rmp = (ArmComputeProxy) cache.get(name);

            if (rmp != null) {
                return rmp;
            }
        }

        rmp = (ArmComputeProxy) super.getRemoteObject().lookupEntry(name);

        if (rmp != null && caching) {
            cache.put(rmp);
        }

        return rmp;
    }

    public ArmComputeProxy[] getComputeMediators() throws RemoteException {
        ArmComputeProxy mediatorProxies[] = super.getRemoteObject().
            list(Registerable.Type.computeMediator).toArray(new ArmComputeProxy[] {});

        if (caching) {
            for (ArmComputeProxy eep : mediatorProxies) {
                cache.get(eep.getEntityName());
            }
        }

        return mediatorProxies;
    }

    public void removeComputeMediator(EntityName name) throws RemoteException {
        ArtRegistry reg = super.getRemoteObject();

        reg.removeEntry(name);

        if (caching) {
            cache.delete(name);
        }
    }
}
