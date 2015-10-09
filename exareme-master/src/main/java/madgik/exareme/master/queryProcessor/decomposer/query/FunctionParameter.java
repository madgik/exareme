/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.query;

/**
 * @author dimitris
 */
public class FunctionParameter {

    public static int CONSTANT_VALUE = 0;
    public static int COLUMN = 1;
    private int type;
    private Column c;
    private Object constantValue;

    public FunctionParameter(Column c) {
        this.type = COLUMN;
        this.c = c;
        this.constantValue = null;
    }

    public FunctionParameter(Object c) {
        if (c.getClass() == Column.class) {
            this.type = COLUMN;
            this.c = (Column) c;
            this.constantValue = null;
        } else {
            this.type = CONSTANT_VALUE;
            this.constantValue = c;
            this.c = null;
        }
    }

    public int getType() {
        return this.type;
    }

    public Column getColumn() {
        return this.c;
    }

    Object getConstantValue() {
        return this.constantValue;
    }
}
