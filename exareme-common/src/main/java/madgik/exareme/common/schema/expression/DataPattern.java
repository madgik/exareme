/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.schema.expression;

/**
 * @author herald
 */
public enum DataPattern {

    // Input
    direct_product,
    cartesian_product,
    external,
    virtual,
    tree,
    remote,

    // Output
    same,
    many,
}
