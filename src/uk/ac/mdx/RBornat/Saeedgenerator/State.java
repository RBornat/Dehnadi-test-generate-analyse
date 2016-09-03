package uk.ac.mdx.RBornat.Saeedgenerator;

// this just so it can be cloned.
// what does that comment mean? It isn't cloned, so far as I can see
// this is now an immutable state.
public class State {
    @SuppressWarnings("serial")
    public class StateLookup extends Exception { }

    public final VarVal [] pairs;        // should be const, but can't say that
    
    State() {
        this.pairs = new VarVal[0];
    }
    
    State(VarVal[] vs) { 
        this.pairs = vs.clone(); // doesn't clone the pairs, though ...
    }
    
    State(Variable var1, Value val1, Variable v2, Value n2) { 
        this.pairs = new VarVal[]{ new VarVal(var1,val1), new VarVal(v2,n2)};
    }
    
    State(Variable v1, Value n1, Variable v2, Value n2, Variable v3, Value n3) { 
        this.pairs = new VarVal[]{ new VarVal(v1,n1), new VarVal(v2,n2), new VarVal(v3,n3)};
    }
    
    State (State s) {
        this(s.pairs);
    }
    
    public int size() {
        return pairs.length;
    }
    
    public VarVal item(int i) {
        return pairs[i];
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof State))
            return false;
        State s = (State)o;
        // System.out.println("State comparing "+this+" with "+s);
        if (s.pairs.length!=this.pairs.length) {
            // System.out.print(" => false"+Generator.NL+Generator.NL); System.out.flush();
            return false; 
        }
        // we have to do ContainsAll for ourselves. Sigh.
        for (VarVal vv : pairs)
            if (!s.contains(vv)) {
                // System.out.print(" => false"+Generator.NL+Generator.NL); System.out.flush();
                return false;
            }
        // System.out.print(" => true"+Generator.NL+Generator.NL); System.out.flush();
        return true;
    }
    
    public boolean contains(VarVal vv) {
        for (VarVal vv1 : pairs)
            if (vv.equals(vv1))
                return true;
        return false;
    }
    
    public boolean maps(Variable var) {
        for (VarVal vv : pairs)
            if (vv.var.equals(var))
                return true;
        return false;
    }
    
    public Variable mapsIgnoreCase(Variable var) {
        // return the unique Variable which matches var
        Variable result = null;
        for (VarVal vv : pairs)
            if (vv.var.equalsIgnoreCase(var))
                if (result==null)
                    result = vv.var;
                else
                    return null; // it's not unique
        return result;
   }
    public State add(Variable var, Value val) {
        try {
            State s = set(var, val);
            return s;
        } catch (StateLookup e) {
            VarVal newpairs[] = new VarVal[pairs.length+1];
            for (int i = 0; i<pairs.length; i++)
                newpairs[i] = pairs[i];
            newpairs[pairs.length] = new VarVal(var, val);
            return new State(newpairs);
        }
    }
    

    public State add(VarVal vv) {
        return add(vv.var, vv.val);
    }
    
    public State set(Variable var, Value val) throws StateLookup {
        for (int i=0; i<pairs.length; i++) { 
            if (pairs[i].var.equals(var)) {
                if (pairs[i].val.equals(val))
                    return this;
                else {
                    VarVal[] newpairs = pairs.clone();
                    newpairs[i] = new VarVal(var,val);
                    return new State(newpairs);
                }
            }
        }
        throw new StateLookup();
    }
    
    public State set(VarVal vv) throws StateLookup {
        return set(vv.var, vv.val);
    }
    
    public Value get(Variable var) throws StateLookup {
        for (VarVal v : pairs) 
            if (v.var.equals(var))
                return v.val;
        throw new StateLookup();
    }
    
    public String toString(String separator) {
        SeparatedStringBuilder sb = new SeparatedStringBuilder(separator);
        for (VarVal v: pairs) 
            sb.append(v.toString());
        return sb.toString();
    }

    public String toString() {
        return this.toString(", ");
    }
}
