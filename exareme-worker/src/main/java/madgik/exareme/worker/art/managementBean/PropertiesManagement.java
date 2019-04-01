/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.managementBean;

import madgik.exareme.utils.properties.AdpProperties;

import java.rmi.RemoteException;

/**
 * The implementation of the bean that manages ART properties.
 *
 * @author Dimitris Paparas <br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class PropertiesManagement implements PropertiesManagementMBean {

    public boolean getCachingStatus() throws RemoteException {
        return AdpProperties.getArtProps()
                .getBoolean("art.registry.rmi.RmiArtRegistryProxy.caching");
    }

    public int getRegisterCacheSize() throws RemoteException {
        return AdpProperties.getArtProps().getInt("art.registry.rmi.RmiArtRegistryProxy.cacheSize");
    }

    public int getMessageEventDumpPeriod() throws RemoteException {
        return AdpProperties.getArtProps()
                .getInt("art.logger.database.sqlite.SqliteMessageEventStorage.dumpPeriod");
    }

    public long getContainerLifetime() throws RemoteException {
        return AdpProperties.getArtProps().getLong("art.container.rmi.RmiContainer.lifetime");
    }

    public long getRmiExecutionEngineLifetime() throws RemoteException {
        return AdpProperties.getArtProps()
                .getLong("art.executionEngine.rmi.RmiExecutionEngine.lifetime");
    }
}
