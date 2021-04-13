/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.registry.resourceStorage;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.registry.Registerable;
import madgik.exareme.worker.art.registry.Registerable.Type;
import madgik.exareme.worker.art.registry.RegistryResourceStorage;
import org.apache.log4j.Logger;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * @author Dimitris Paparas<br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class MemoryResourceStorage implements RegistryResourceStorage {
    private static Logger log = Logger.getLogger(MemoryResourceStorage.class);

    private Semaphore semaphore = null;
    private RegistryResourceStorageStatus registryResourceStorageStatus;
    private Map<String, Registerable> objectMap = null;
    private Map<Registerable.Type, List<Registerable>> typeMap;

    public MemoryResourceStorage() {
        registryResourceStorageStatus = new RegistryResourceStorageStatus();
        objectMap = new HashMap<String, Registerable>();
        typeMap = new EnumMap<Registerable.Type, List<Registerable>>(Registerable.Type.class);
        semaphore = new Semaphore(1);
    }

    @Override
    public void store(Registerable r) throws RemoteException {
        try {
            semaphore.acquire();
            Registerable old = objectMap.put(r.getEntityName().getName(), r);
            List<Registerable> l = typeMap.get(r.getType());
            if (l == null) {
                l = Collections.synchronizedList(new LinkedList<Registerable>());
                typeMap.put(r.getType(), l);
            }

            if (old == null) {
                registryResourceStorageStatus.increaseStoredObjects();
            } else {
                l.remove(old);
            }
            l.add(r);

        } catch (Exception e) {
            throw new RemoteException(
                    "Cannot store object: '" + r.getEntityName().getName() + "' at " + r.getEntityName()
                            .getIP() + ":" + r.getEntityName().getPort(), e);
        } finally {
            semaphore.release();
        }
    }

    @Override
    public Registerable retrieve(EntityName epr) throws RemoteException {
        try {
            semaphore.acquire();
            Registerable r = objectMap.get(epr.getName());

            if (r == null) {
                throw new NoSuchObjectException(
                        "Object was not found: '" + epr.getName() + "' at " + epr.getIP() + ":" + epr
                                .getPort());
            }
            return r;
        } catch (Exception e) {
            throw new RemoteException("Cannot retrieve object.", e);
        } finally {
            semaphore.release();
        }
    }

    @Override
    public void delete(EntityName epr) throws RemoteException {
        try {
            semaphore.acquire();
            Registerable r = objectMap.remove(epr.getName());

            if (r == null) {
                throw new NoSuchObjectException(
                        "Object was not found: '" + epr.getName() + "' at " + epr.getIP() + ":" + epr
                                .getPort());
            }
            registryResourceStorageStatus.decreaseStoredObjects();
            List<Registerable> l = typeMap.get(r.getType());
            l.remove(r);

        } catch (Exception e) {
            throw new RemoteException("Cannot delete object.", e);
        } finally {
            semaphore.release();
        }
    }

    @Override
    public Collection<Registerable> retrieveAll(Type type) throws RemoteException {
        Collection<Registerable> col = null;
        try {
            semaphore.acquire();
            col = typeMap.get(type);
        } catch (InterruptedException ex) {
            throw new ServerException("Cannot retrieve all objects of type: " + type, ex);
        } finally {
            semaphore.release();
        }
        return col;
    }

    @Override
    public RegistryResourceStorageStatus getResourceStorageStatus()
            throws RemoteException {
        return registryResourceStorageStatus;
    }

    @Override
    public String getInfo() throws RemoteException {
        return "Memory Storage";
    }

    @Override
    public Collection<Registerable> retrieveAll() throws RemoteException {
        Collection<Registerable> col = new ArrayList<Registerable>();
        try {
            semaphore.acquire();
            Iterator<List<Registerable>> it = typeMap.values().iterator();
            while (it.hasNext()) {
                col.addAll(it.next());
            }
        } catch (InterruptedException ex) {
            throw new ServerException("Cannot retrieve all objects", ex);
        } finally {
            semaphore.release();
        }
        return col;
    }
}
