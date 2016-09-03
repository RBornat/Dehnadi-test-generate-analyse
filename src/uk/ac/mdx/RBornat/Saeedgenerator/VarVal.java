package uk.ac.mdx.RBornat.Saeedgenerator;

// these pairs are immutable. Hooray for functional programming.
public class VarVal {
    public final Variable var;
    public final Value val;
    
    VarVal(Variable var, Value val) { this.var = var; this.val = val; }
    
    VarVal(VarVal v) { this(v.var, v.val); }
    
    public boolean equals(Object o) {
        if (!(o instanceof VarVal))
            return false;
        VarVal v = (VarVal)o;
        return v.var.equals(var) && v.val.equals(val);
    }
    
    public String toString(){
        return var+"="+val;
    }
}
