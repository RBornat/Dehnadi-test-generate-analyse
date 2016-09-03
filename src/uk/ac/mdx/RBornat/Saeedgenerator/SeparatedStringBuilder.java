package uk.ac.mdx.RBornat.Saeedgenerator;

public class SeparatedStringBuilder { // can't extend StringBuilder, rats.
    final StringBuilder sb = new StringBuilder();
    final String sep;
    private boolean separated;
    
    SeparatedStringBuilder(String sep) {
        this.sep = sep; this.separated = false;
    }
    
    public void append(String s) {
        if (s!=null) {
            if (separated) sb.append(sep);
            sb.append(s);
            separated = true;
        }
    }

    public void append(String[] ss) {
        if (ss!=null)
            for (String s : ss)
                append(s);
    }

    public String toString() {
        return sb.toString();
    }
}
