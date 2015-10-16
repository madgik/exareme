/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.collections;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * @param <T>
 * @author herald
 */
public class ReadOnlyViewList<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<T> list = null;
    private List<T> readOnlyView = null;

    public ReadOnlyViewList(List<T> list) {
        this.list = list;
        this.readOnlyView = Collections.unmodifiableList(this.list);
    }

    public List<T> getList() {
        return list;
    }

    public List<T> getReadOnlyView() {
        return readOnlyView;
    }
}
