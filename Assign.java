package uk.ac.mdx.RBornat.Saeedgenerator;

public class Assign {
    public final Variable left, right;
    Assign(Variable left, Variable right) {
        this.left = left; this.right = right;
    }
    public String toString() {
        return left+"="+right+";";
    }
}
