/*
 * This class has been created just for
 * experience's purposes
 */
package madgik.exareme.worker.arm.compute.cloud;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.arm.compute.session.ActiveContainer;

/**
 * @author Christos
 */
public class ContainerManager implements ContainerManagementInterface {

    private static int i = 0;

    @Override public ActiveContainer createVM() {
        EntityName entity;
        ActiveContainer container;
        entity = new EntityName("Container" + i, "192.168.2.3");
        i++;
        return (new ActiveContainer(i, entity, i));
    }

    @Override public void deleteVM(ActiveContainer container) {
    }

}
