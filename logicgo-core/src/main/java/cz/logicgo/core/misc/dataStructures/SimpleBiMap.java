package cz.logicgo.core.misc.dataStructures;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SimpleBiMap<K, V> {
    private final Map<K, V> forward = new HashMap<>();
    private final Map<V, K> backward = new HashMap<>();

    public void put(K key, V value) {
        if (forward.containsKey(key)) {
            V oldValue = forward.get(key);
            backward.remove(oldValue);
        }
        forward.put(key, value);
        backward.put(value, key);
    }

    public V getForward(K key) {
        return forward.get(key);
    }

    public K getBackward(V value) {
        return backward.get(value);
    }

    public void removeByKey(K key) {
        V value = forward.remove(key);
        if (value != null) {
            backward.remove(value);
        }
    }

    public void removeByValue(V value) {
        K key = backward.remove(value);
        if (key != null) {
            forward.remove(key);
        }
    }

    public Set<K> keySet() {
        return forward.keySet();
    }

    public Set<V> valueSet() {
        return backward.keySet();
    }

    public Set<Map.Entry<K, V>> entrySet() {
        return forward.entrySet();
    }

    public boolean containsKey(K key) {
        return forward.containsKey(key);
    }

    public boolean containsValue(V value) {
        return backward.containsKey(value);
    }
}
