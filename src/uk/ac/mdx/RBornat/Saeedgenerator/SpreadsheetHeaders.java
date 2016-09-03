package uk.ac.mdx.RBornat.Saeedgenerator;

import java.util.Vector;

import org.javatuples.Pair;

class SpreadsheetHeaders extends Vector<String> {
    int quStart=0, quEnd=0;
    
    SpreadsheetHeaders(int size) {
        super(size);
    }

    void addColumn(String c) {
        this.add(c);
    }
    
    void addColumns(String[] cols) {
        if (cols!=null)
            for (String c : cols)
                this.add(c);
    }
    
    /* Choice questions (and Authorisation) get one column.
     * Text and number questions get one column.
     * MultiText questions get N columns, one for each option.
     * Programming questions are like multitext.
     * Final looks like a choice question.
     * Obviously (?) conditionality doesn't affect the number of columns.
     */

    void addColumns(Questionnaire.Question[] qs) {
        if (qs!=null)
            for (Questionnaire.Question q : qs)
                if (q instanceof Questionnaire.AuthQuestion || 
                    q instanceof Questionnaire.ChoiceQuestion)
                    this.add(q.id);
                else
                if (q instanceof Questionnaire.TextQuestion || 
                    q instanceof Questionnaire.NumberQuestion)
                    this.add(q.id);
                else
                if (q instanceof Questionnaire.MultiTextQuestion) {
                    for (Pair<String, String[]> opt : 
                            ((Questionnaire.MultiTextQuestion)q).options)
                        this.addColumn(q.id, opt.getValue0());
                }
                else
                if (q instanceof Questionnaire.MultiChoiceQuestion) {
                    for (Pair<String, String[]> opt : 
                        ((Questionnaire.MultiChoiceQuestion)q).options)
                        this.addColumn(q.id, opt.getValue0());
                }
                else
                if (q instanceof Questionnaire.ArrayChoiceQuestion) {
                    for (Pair<String, String[]> h : 
                        ((Questionnaire.ArrayChoiceQuestion)q).horizs)
                        this.addColumn(q.id, h.getValue0());
                }
                else
                if (q instanceof Questionnaire.TestSection)
                    addColumns(((Questionnaire.TestSection)q).test);
                else
                if (q instanceof Questionnaire.FinalQuestion)
                    this.add(q.id);
                else
                if (q instanceof Questionnaire.ConditionalSection) {
                    addColumns(((Questionnaire.ConditionalSection)q).thens);
                    addColumns(((Questionnaire.ConditionalSection)q).elses);
                }
                else
                if (q instanceof Questionnaire.GroupStarter)
                    ; // which had better be skip
                else
                    Utils.crash("PaperQuestionnaire.Headers.addColumns cannot handle "+q);
    }

    void addColumns(Test qu) {
        quStart = this.size();
        for (int i = 0; i<qu.progQuestions.length; i++) {
            for (int j = 0; j<qu.answerPages[i].choices.size(); j++) {
                addTestSubQuestionColumn(i, j, qu.answerPages[i].choices.item(j));
            }
            addTestOtherColumn(i);
        }
        quEnd = this.size();
    }
    
    void addColumn(String qid, String optid) {
        this.add(qid+" ["+optid+"]");
    }
    
    // this is the basic treatment: Q<qnum>: <answer> [<ans_idx>] | Q<qnum>: Other
    void addTestSubQuestionColumn(int qidx, int subidx, State b) {
        addColumn(standardTestSubQuestionHeader(qidx, subidx, b));
    }
    
    void addTestOtherColumn(int qidx) {
       addColumn(standardTestOtherQuestionHeader(qidx));
    }

    public static String standardTestSubQuestionHeader(int qidx, int subidx, State b) {
        return "\"Q"+Utils.ordinal(qidx)+": "+b+" ["+Utils.ordinal(subidx)+"]\"";
    }
    
    public static String standardTestOtherQuestionHeader(int qidx) {
        return "\"Q"+Utils.ordinal(qidx)+": Other\"";
    }
}