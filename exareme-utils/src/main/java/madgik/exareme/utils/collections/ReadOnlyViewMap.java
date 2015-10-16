/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.collections;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * @param <K>
 * @param <V>
 * @author herald
 */
public class ReadOnlyViewMap<K, V> implements Serializable {
    private static final long serialVersionUID = 1L;

    private Map<K, V> map = null;
    private Map<K, V> readOnlyView = null;

    public ReadOnlyViewMap(Map<K, V> map) {
        this.map = map;
        this.readOnlyView = Collections.unmodifiableMap(this.map);
    }

    public Map<K, V> getMap() {
        return map;
    }

    public Map<K, V> getReadOnlyView() {
        return readOnlyView;
    }
}
