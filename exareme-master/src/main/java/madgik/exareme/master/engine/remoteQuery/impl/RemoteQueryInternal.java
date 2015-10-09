/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.engine.remoteQuery.impl;

import madgik.exareme.master.engine.remoteQuery.ServerInfo;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @author Christos Mallios <br>
 *         University of Athens / Department of Informatics and Telecommunications.
 */
public interface RemoteQueryInternal {

    public void queryCompletion(ServerInfo server, String query, String directoryOfStorage,
        String cacheDatabase, String cacheTable, int responseTime, int size)
        throws IOException, SQLException;

}
