/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.jdbc.federated;

/**
 * @author dimitris
 */
public class AdpRecord extends java.util.ArrayList {
    private static final long serialVersionUID = 1;

    public AdpRecord() {
        super();
    }

    public AdpRecord(String[] s) {
        super();
        for (int i = 0; i < s.length; i++) {
            this.add(s[i]);
        }
    }
}
