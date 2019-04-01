/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.query;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;

import java.util.ArrayList;
import java.util.List;

import static madgik.exareme.master.queryProcessor.decomposer.util.Util.operandsAreEqual;


/**
 * @author dimitris
 */
public class Output {

    private String outputName;
    private Operand object;

    public Output(String name, Operand o) {
        this.outputName = name;
        this.object = o;
    }

    public Operand getObject() {
        return object;
    }

    public String getOutputName() {
        return outputName;
    }

    @Override
    public String toString() {
        if (outputName.startsWith("\"")) {
            return object.toString() + " as " + outputName;
        } else {
            return object.toString() + " as \"" + outputName + "\"";
        }
    }

    public void setOutputName(String o) {
        this.outputName = o;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + (this.outputName != null ? this.outputName.hashCode() : 0);
        hash = 23 * hash + (this.object != null ? this.object.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Output other = (Output) obj;
        if ((this.outputName == null) ?
                (other.outputName != null) :
                !this.outputName.equals(other.outputName)) {
            return false;
        }
        return !(this.object != other.object && (this.object == null || !operandsAreEqual(
                this.object, other.object)));
    }

    public void setObject(Operand op) {
        this.object = op;
    }

    @Override
    public Output clone() throws CloneNotSupportedException {
        Output cloned = new Output(this.outputName, this.object.clone());
        return cloned;
    }

    public HashCode getHashID() {
        List<HashCode> codes = new ArrayList<HashCode>();
        codes.add(object.getHashID());
        codes.add(Hashing.sha1().hashBytes(outputName.toUpperCase().getBytes()));

        return Hashing.combineOrdered(codes);
    }
}
