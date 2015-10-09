/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.histogram.partitionRule;

/**
 * This indicates if there are any restrictions on the buckets. Of great importance is the serial
 * class, which requires that buckets are non-overlapping with respect to some parameter (the
 * next characteristic), and its subclass end-biased, which requires at most one non-singleton
 * bucket. [The History of Histograms Yannis Ioannidis]
 *
 * @author herald
 */
public enum PartitionClass {

    serial,
    end_biased
}
