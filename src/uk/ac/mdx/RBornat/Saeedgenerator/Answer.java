package uk.ac.mdx.RBornat.Saeedgenerator;

// should be immutable, because it goes in SimpleSet. But in AnswerPage, the only place we deploy a SimpleSet<Answer>,
// we make sure that there are never two entries with the same ticks.
public class Answer {
    public final SimpleSet<State> ticks;
    public SimpleSet<BiModel> models;
    
    // a new use case: set of states, set of models (which may even be empty ...)
    
    Answer(SimpleSet<State> ts, SimpleSet<BiModel> ms) { 
        this.ticks = new SimpleSet<State>(ts); 
        this.models = new SimpleSet<BiModel>(ms) {
            public String toString(){
                String s = "";
                for (BiModel m: this) {
                    if (s.length()!=0)
                        s += " / ";
                    s += m;
                }
                return s;
            }
        };
    }
    
    Answer(SimpleSet<State> ts, BiModel m) { 
        this(ts, new SimpleSet<BiModel>(m));
    }
    
    Answer(State s, BiModel m) { this(new SimpleSet<State>(s), m); }
        
    /* public void add(State s) {
        ticks.add(s);
    } */
    
    public boolean accept(SimpleSet<State> ticks, BiModel model) {
        if (ticks.equals(this.ticks)) {
            // System.out.print(ticks+" -> "+models+" accepting "+model);
            models.add(model);
            // System.out.print(" => "+models+Generator.NL+Generator.NL);
            return true;
        }
        else
            return false;
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof Answer))
            return false;
        Answer a = (Answer)o;
        return ticks.equals(a.ticks) && models.equals(a.models);
    }
    
    public String toString() {
        String r = "";
        for (State s: ticks) {
            if (r.length()!=0)
                r += "; ";
            r +=s;
        }
        if (ticks.size()!=1)
            r = "{"+r+"}";
        return r+" -- " +models;
    }
}
