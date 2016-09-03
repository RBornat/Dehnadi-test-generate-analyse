package uk.ac.mdx.RBornat.Saeedgenerator;

public class StatePlusModels {
    public final State state;
    public final SimpleSet<BiModel> models;
    
    StatePlusModels(State state, SimpleSet<BiModel> models) { this.state = state; this.models = models; }
        
    public boolean equals(Object o) {
        if (!(o instanceof StatePlusModels))
            return false;
        StatePlusModels spm = (StatePlusModels)o;
        return spm.state.equals(state) && 
                ((spm.models!=null && models!=null && spm.models.equals(models)) || 
                 spm.models==models);
    }
    
    public String toString(){
        return state.toString()+":"+(models==null || models.size()==0 ? "not modelled" : models.toString());
    }

}
