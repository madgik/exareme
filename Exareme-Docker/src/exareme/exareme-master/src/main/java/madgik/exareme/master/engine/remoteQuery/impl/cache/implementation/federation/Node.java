/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.engine.remoteQuery.impl.cache.implementation.federation;

import madgik.exareme.master.engine.remoteQuery.impl.cache.CachedDataInfo;

/**
 * @author Christos Mallios <br>
 * University of Athens / Department of Informatics and Telecommunications.
 */
class Node {

    public CachedDataInfo info;
    public int numberOfPins;

    public Node(CachedDataInfo info) {
        this.info = info;
        this.numberOfPins = 0;
    }
}
