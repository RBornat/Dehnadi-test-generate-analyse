package uk.ac.mdx.RBornat.Saeedgenerator;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

public class SimpleMap<K, V> extends AbstractMap<K, V> {

    final Set<Map.Entry<K, V>> set;
    
    SimpleMap() {
        this.set = new SimpleSet<Map.Entry<K, V>>();
    }
    
    
    @Override
    public V put(K k, V v) {
        set.add(new Entry(k, v));
        return v; // why?
    }
    
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return set;
    }

    class Entry implements Map.Entry<K,V> {
        final K key; V value;
        Entry(K key, V value) { 
            this.key = key; this.value = value; 
        }
        @Override
        public K getKey() {
            return key;
        }
        @Override
        public V getValue() {
            return value;
        }
        @Override
        public V setValue(V value) {
            this.value = value;
            return value; // why?
        }
        public String toString() {
            return "<<"+this.key+"|->"+this.value+">>";
        }
    }
}
