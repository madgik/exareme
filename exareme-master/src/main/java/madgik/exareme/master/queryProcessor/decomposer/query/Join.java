/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.decomposer.query;

import java.util.Objects;

/**
 * @author heraldkllapi
 */
public class Join {

    public String leftTableAlias = null;
    public String leftColumnName = null;
    public String rightTableAlias = null;
    public String rightColumnName = null;

    @Override public String toString() {
        return leftTableAlias + "." + leftColumnName + " = " + rightTableAlias + "."
            + rightColumnName;
    }

    @Override public int hashCode() {
        int hash = 3;
        hash = 47 * hash + Objects.hashCode(this.leftTableAlias);
        hash = 47 * hash + Objects.hashCode(this.leftColumnName);
        hash = 47 * hash + Objects.hashCode(this.rightTableAlias);
        hash = 47 * hash + Objects.hashCode(this.rightColumnName);
        return hash;
    }

    @Override public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Join other = (Join) obj;
        if (!Objects.equals(this.leftTableAlias, other.leftTableAlias)) {
            return false;
        }
        if (!Objects.equals(this.leftColumnName, other.leftColumnName)) {
            return false;
        }
        if (!Objects.equals(this.rightTableAlias, other.rightTableAlias)) {
            return false;
        }
        if (!Objects.equals(this.rightColumnName, other.rightColumnName)) {
            return false;
        }
        return true;
    }


}
