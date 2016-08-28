package uk.ac.mdx.RBornat.Saeedgenerator;

public enum AssignModel {
    RightToLeftMove,
    RightToLeftCopy,
    LeftToRightMove,
    LeftToRightCopy,
    RightToLeftCopyAdd,
    RightToLeftMoveAdd,
    LeftToRightCopyAdd,
    LeftToRightMoveAdd,
    NoChange,
    Equality,
    Swap;
    
    /* 
     * enum -> int
     * am.ordinal()
     * 
     * int -> enum
     * AssignModel.values()[someInt]
     * 
     * enum -> String
     * am.name()    
     * 
     * String -> enum
     * AssignModel.valueOf(yourString)
     * 
     * size of type
     * values().length
     * 
     */
    
    public String toString() {
        return "M"+Utils.ordinal(ordinal());
    }
}
