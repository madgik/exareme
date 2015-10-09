/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.worker.arm.compute.cloud;

import madgik.exareme.worker.arm.compute.session.ActiveContainer;

/**
 * @author Christos
 */
public interface ContainerManagementInterface {

    ActiveContainer createVM();

    void deleteVM(ActiveContainer container);
}
