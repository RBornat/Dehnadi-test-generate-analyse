package uk.ac.mdx.RBornat.Saeedgenerator;

public abstract class AssignMechanism {
    public final AssignModel model;
    public final AssignDirection direction;
   
    AssignMechanism(AssignModel model, AssignDirection direction) { 
        this.model = model; this.direction = direction; 
    }
    
    public abstract ValuePair assign(Value v1, Value v2);
}
