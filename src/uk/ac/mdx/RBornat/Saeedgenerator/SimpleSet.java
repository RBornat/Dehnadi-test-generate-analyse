package uk.ac.mdx.RBornat.Saeedgenerator;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;


public class SimpleSet<E> extends AbstractSet<E> implements Collection<E> {

    private final Vector<E> vs;

    SimpleSet() { vs = new Vector<E>(); }

    SimpleSet(E e) { this(); this.add(e); }

    SimpleSet(Collection<E> c) { this(); this.addAll(c); }
    
    /* because Java's Set works like this, so must we. SimpleSet is not immutable. Sigh. */
    public boolean add(E e) {
        if (vs.contains(e)) 
            return false;
        else {
            vs.add(e); return true;
        }
    }

    public Iterator<E> iterator() {
        return vs.iterator(); 
    }

    public int size() {
        return vs.size();
    }
    
    public E item(int i) {
        return vs.elementAt(i); // must work
    }
    
    public int indexOf(E e) {
        return vs.indexOf(e);
    }
    
    public E elementAt(int i) {
        return vs.elementAt(i);
    }
    
    public String toString() {
        String r = "";
        for (Iterator<E> ie = iterator(); ie.hasNext(); ) {
            if (r.length()!=0)
                r = r+"; ";
            r = r+ie.next();
        }
        return "{"+r+"}";
    }
}
