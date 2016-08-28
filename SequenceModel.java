package uk.ac.mdx.RBornat.Saeedgenerator;

public enum SequenceModel {
    Isolated, Sequence, ParallelMultiple, ParallelDestinationOnly;
    
    /* 
     * enum -> int
     * sm.ordinal()
     * 
     * int -> enum
     * SequenceModel.values()[someInt]
     * 
     * enum -> String
     * sm.name()    
     * 
     * String -> enum
     * SequenceModel.valueOf(yourString)
     * 
     * size of type
     * values().length
     * 
     */
    
   public String toString() {
        return this==Isolated ? "" : "S"+ordinal();
    }
}
