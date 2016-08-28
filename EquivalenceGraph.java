package uk.ac.mdx.RBornat.Saeedgenerator;

import java.util.Collection;
import java.util.Vector;

public class EquivalenceGraph<E extends Comparable<E>> {
    
    class Thing {
        final E e;
        Thing up;
        Thing(E e) { this.e = e; this.up = null; } 
        Thing representative() {
            return up==null ? this : up.representative();
        }
    }

    final Vector<Thing> things = new Vector<Thing>();
    
    Thing find(E e) {
        for (Thing t : things)
            if (t.e.equals(e))
                return t;
        return null;
    }
    
    EquivalenceGraph() { }
    
    EquivalenceGraph(E e) {
        add(e);
    }
    
    EquivalenceGraph(Collection<E> c) {
        for (E e : c)
            add(e);
    }
    
    public boolean add(E e) {
        if (find(e)==null) {
            things.add(new Thing(e));
            return true;
        }
        else
            return false;
    }
    
    public boolean makeEquiv(E e1, E e2) {
        Thing t1 = find(e1), t2 = find(e2);
        if (t1==null || t2==null)
            return false;
        else {
            Thing p1 = t1.representative(), p2 = t2.representative();
            if (p1!=p2) {
                // alphabetical order: root is least
                if (p1.e.compareTo(p2.e)<=0)
                    p2.up = p1;
                else
                p1.up = p2;
            }
            return true;
        }        
    }
    
    public Vector<Vector<E>> classes(){
        Vector<Vector<E>> cs = new Vector<Vector<E>>();
        for (Thing t : things) {
            if (t.up == null) { // it's a root
                Vector<E> c = new Vector<E>();
                cs.add(c);
                c.add(t.e);
                for (Thing t1 : things) {
                    if (t1!=t && t1.representative()==t)
                        c.add(t1.e);
                }
            }
        }
        return cs;
    }
}
