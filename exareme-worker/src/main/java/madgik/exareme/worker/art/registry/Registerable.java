/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.registry;

import madgik.exareme.common.art.entity.EntityName;

import java.io.Serializable;

/**
 * This is the Registerable interface.
 *
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 *         {herald,paparas,evas}@di.uoa.gr<br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public interface Registerable extends Serializable {

    EntityName getEntityName();

    RegisterPolicy getRegisterPolicy();

    Type getType();

    enum Type {
        container,
        executionEngine,
        logger,
        computeMediator,
        storageMediator
    }
}
