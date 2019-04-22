/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.worker.arm.storage.policy.cluster;

import madgik.exareme.utils.properties.AdpProperties;

/**
 * @author alex
 */
public class ArmClusterStoragePolicy {


    public boolean isValid() {
        String defaultFS = AdpProperties.getArmProps().getString("arm.storage.client.defaultFS");
        return defaultFS.contains("hdfs://");
    }
}
