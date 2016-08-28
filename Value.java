package uk.ac.mdx.RBornat.Saeedgenerator;

public class Value {
    final int value;
    
    Value(int value) {
        this.value = value;
    }
    
    public Value plus(Value v) {
        return new Value(value+v.value);
    }
    
    public String toString() {
        return Integer.toString(value);
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof Value))
            return false;
        Value v = (Value) o;
        return v.value==value;
    }
}
