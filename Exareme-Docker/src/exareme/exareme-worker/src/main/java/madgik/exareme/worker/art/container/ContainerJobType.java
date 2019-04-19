/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container;

/**
 * @author heraldkllapi
 */
public enum ContainerJobType {
    // Operators
    createOperator,
    startOperator,
    stopOperator,
    destroyOperator,
    // Buffer
    createBuffer,
    destroyBuffer,
    // Adaptor
    createReadAdaptor,
    createWriteAdaptor,
    // Stats
    getStatistics,
    // Table transfer
    dataTransfer,
    createOperatorLink,
    dataTransferRegister,
    distributedJobCreateDataflow
}
