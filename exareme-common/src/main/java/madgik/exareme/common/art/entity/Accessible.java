/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.art.entity;

import java.io.Serializable;

/**
 * This is the Accessible interface. It indicates that an entity
 * is accessible locally or remotely.
 *
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 * {herald,paparas,evas}@di.uoa.gr<br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public interface Accessible extends Serializable {

    /**
     * Get the end point reference.
     *
     * @return the end point reference.
     */
    EntityName getEntityName();
}
