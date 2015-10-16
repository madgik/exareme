/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.job;

import madgik.exareme.utils.association.Pair;
import madgik.exareme.worker.art.container.ContainerJobResult;
import madgik.exareme.worker.art.container.ContainerJobType;
import madgik.exareme.worker.art.container.buffer.BufferID;

import java.util.HashMap;

/**
 * @author heraldkllapi
 */
public class CreateBufferJobResult extends ContainerJobResult {

    public final BufferID bufferId;
    public HashMap<String, Pair<String, BufferID>> operatorIDToBufferId;
    public String operatorName;

    public CreateBufferJobResult(BufferID bufferId) {
        this.bufferId = bufferId;
        this.operatorName = null;
        this.operatorIDToBufferId = null;
    }

    @Override public ContainerJobType getType() {
        return ContainerJobType.createBuffer;
    }

    public void setOpToBuffer(HashMap<String, Pair<String, BufferID>> operatorIDToBufferId) {
        this.operatorIDToBufferId = operatorIDToBufferId;
    }

    public void setOp(String operatorName) {
        this.operatorName = operatorName;
    }

}
