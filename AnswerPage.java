package uk.ac.mdx.RBornat.Saeedgenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

// Seems to be the entry for one question. answers is all the answers we recognise; choices is those that we list
// in the test (I think). RB 21/06/2016

// We deliberately do not include choices that only appear in +S3. This reduces the size of the questionnaire considerably.
// But at what cost?

public class AnswerPage {
    public final SimpleSet<Answer> answers;
    public final SimpleSet<State> choices;
    Integer[] indices = null; // Only in legacy tests. See PaperQuestionnaire for uses
    
    Integer[] getIndices() { return indices; }
    
    void setIndices(Integer[] indices) {
        if (this.indices==null)
            this.indices = indices;
        else
            Utils.fail ("setting AnswerPage indices when it's already set");
    }
    
    AnswerPage() { this.answers = new SimpleSet<Answer>(); this.choices = new SimpleSet<State>(); }
    
    public void add(SimpleSet<State> ticks, BiModel m) {
        if (m.sequenceModel!=SequenceModel.ParallelMultiple) // why not S3? See above ...
            choices.addAll(ticks);
        for (Answer a : answers) 
            if (a.accept(ticks, m)) 
                return;
        answers.add(new Answer(ticks, m));
    }

    public void add(State s, BiModel m) {
        add(new SimpleSet<State>(s), m);
    }

    public void add(Answer a) {
        for (Iterator<BiModel> mi = a.models.iterator(); mi.hasNext(); )
            add(a.ticks, mi.next());
    }
    
    public void add(Collection<Answer> as) {
        for (Answer a: as)
            add(a);
    }
    
    public String toString() {
        String r = "";
        
        for (Answer a : answers)
            r += a+TextUtils.LineSep;
        return r;
    }
    
    public Answer lookupAnswer(SimpleSet<State> choices) {
        for (Answer a : answers) {
            if (a.ticks.equals(choices))
                return a;
        }
        return null;
    }    
    
    public Answer lookupAnswer(State choice) {
        return lookupAnswer(new SimpleSet<State>(choice));

    }    
    
    /*
    public Answer lookupAnswer(int choicenum) { // 0..n, please
        SimpleSet<State> s = new SimpleSet<State>(choices.lookup(choicenum)); // exits if can't find
        for (Iterator<Answer> ai = answers.iterator(); ai.hasNext(); ) {
            Answer a = ai.next();
            if (a.ticks.equals(s))
                return a;
        }
        Utils.showErrorAlert("can't find "+s+" in "+this);
        System.exit(1);
        return null;
    }
    */
}
