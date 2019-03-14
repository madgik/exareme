package madgik.exareme.worker.art.executionPlan.parser.expression;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author herald
 */
public class Pragma implements Serializable {

    private static final long serialVersionUID = 1L;

    public final String pragmaName;
    public final String pragmaValue;

    public Pragma(String pragmaName, String pragmaValue) {
        this.pragmaName = pragmaName;
        this.pragmaValue = pragmaValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        Pragma guest = (Pragma) obj;
        return pragmaName.equals(guest.pragmaName) && pragmaValue.equals(guest.pragmaValue);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.pragmaName);
        hash = 89 * hash + Objects.hashCode(this.pragmaValue);
        return hash;
    }

}
