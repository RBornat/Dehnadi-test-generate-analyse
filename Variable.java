package uk.ac.mdx.RBornat.Saeedgenerator;

public class Variable implements Comparable<Variable>{
    final String name;
    Variable(String name) {
        this.name = name;
    }
    
    public String toString() {
        return name;
    }
    
    public int compareTo(Variable var) {
        return name.compareTo(var.name);
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof Variable))
            return false;
        Variable v = (Variable) o;
        return v.name.equals(name);
    }

    public boolean equalsIgnoreCase(Object o) {
        if (!(o instanceof Variable))
            return false;
        Variable v = (Variable) o;
        return v.name.equalsIgnoreCase(name);
    }
}
