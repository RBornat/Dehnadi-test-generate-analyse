package uk.ac.mdx.RBornat.Saeedgenerator;

public class BiModel {
    public final AssignModel assignModel;
    public final SequenceModel sequenceModel;
    
    BiModel(AssignModel assignModel, SequenceModel sequenceModel) { 
        this.assignModel = assignModel; this.sequenceModel = sequenceModel; 
    }
    
    BiModel(int assignIdx, int sequenceIdx) { 
        this.assignModel = AssignModel.values()[assignIdx]; 
        this.sequenceModel = SequenceModel.values()[sequenceIdx]; 
    }
    
    public String toString() {
        String suffix = sequenceModel.toString();
        return assignModel.toString()+(suffix.equals("") ? "" : "+"+suffix);
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof BiModel))
            return false;
        else {
            BiModel m = (BiModel) o;
            return m.assignModel==assignModel && m.sequenceModel==sequenceModel; 
        }
    }
}

