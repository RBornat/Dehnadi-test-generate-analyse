package uk.ac.mdx.RBornat.Saeedgenerator;

import java.io.IOException;
import java.util.Vector;

import org.javatuples.Pair;

public class SurveyMonkeyQuestionnaire {
    final Questionnaire questionnaire;
    final MaybeFileWriter textout;
    final Test test;
    final TestQuestion[] progQuestions;
    final AnswerPage[] answerPages;
    
    SurveyMonkeyQuestionnaire(Questionnaire questionnaire, MaybeFileWriter textout) {
        this.questionnaire = questionnaire;
        this.textout = textout;
        this.test = questionnaire.test;
        this.progQuestions = test.progQuestions;
        this.answerPages = test.answerPages;
    }
    
    boolean finalised = false;
    int questionNum = 0;
    
    void processQuestionnaire() throws IOException {
        processQuestions(questionnaire.questions);
    }
    
    void processQuestions(Questionnaire.Question[] questions) throws IOException {
        if (questions!=null) {
            for (Questionnaire.Question question : questions) {
                if (question!=null) {
                    if (finalised)
                        Utils.fail("no questions can follow **Final -- " +
                        		"SurveyMonkeyQuestionnaire.processQuestionnaire sees "+question);                
    
                    if (question instanceof Questionnaire.GroupStarter ||
                            question instanceof Questionnaire.FinalQuestion ||
                            question instanceof Questionnaire.AuthQuestion ||
                            question instanceof Questionnaire.ArrayChoiceQuestion) {
                        Utils.showInfoAlert("SurveyMonkey generator",
                                "The questionnaire generator can't deal with " +
                                question.controlString() +
                                " questions for SurveyMonkey questionnaires");
                        /* newPage(questionsout);
                        questionsout.write("\\begin{center}{\\LARGE{}"+
                                ((Survey.GroupStarter)question).groupMessage+"}\\end{center}"+
                                TextUtils.LaTeXParaSep);
                        groupId = ((Survey.GroupStarter)question).id;
                        questionNum = 0; */
                    }
                    else
                    if (question instanceof Questionnaire.TestSection) {
                        for (int qIdx=0; qIdx<progQuestions.length; qIdx++) {
                            progQuestionOut(qIdx);
                        }
                    }
                    else
                    if (question instanceof Questionnaire.ConditionalSection) {
                        Utils.showInfoAlert("SurveyMonkey generator",
                                "The questionnaire generator can't deal with " +
                                question.controlString() +
                                " questions for SurveyMonkey, " +
                                "\nbut it will try to deal with the questions inside the " +
                                question.controlString());
                        processQuestions(((Questionnaire.ConditionalSection)question).thens);
                        processQuestions(((Questionnaire.ConditionalSection)question).elses);     
                    }
                    else {
                        String paraSep = TextUtils.LineSep+TextUtils.LineSep;
                        textout.write(paraSep);
                        
                        textout.write(""+(++questionNum)+": "+paraSep);
                        
                        if (question instanceof Questionnaire.TextQuestion) {
                            Questionnaire.TextQuestion q = (Questionnaire.TextQuestion)question;
                            textout.write(TextUtils.enPara(q.questionText, paraSep));
                        }
                        else
                        if (question instanceof Questionnaire.MultiTextQuestion) {
                            Questionnaire.MultiTextQuestion q = (Questionnaire.MultiTextQuestion)question;
                            textout.write(TextUtils.enPara(q.questionText, paraSep)+paraSep);
                            for (Pair<String,String[]> option : q.options)
                                textout.write(TextUtils.enLine(option.getValue1())+TextUtils.LineSep);
                            textout.write(TextUtils.LineSep);
                        }
                        else
                        if (question instanceof Questionnaire.NumberQuestion) {
                            Questionnaire.NumberQuestion q = (Questionnaire.NumberQuestion)question;
                            textout.write(TextUtils.enPara(q.questionText, paraSep)+paraSep);
                        }
                        else
                        if (question instanceof Questionnaire.ChoiceQuestion) {
                            Questionnaire.ChoiceQuestion q = (Questionnaire.ChoiceQuestion)question;
                            textout.write(TextUtils.enPara(q.questionText, paraSep)+paraSep);
                            for (Pair<String,String[]> option : q.options)
                                textout.write(TextUtils.enLine(option.getValue1())+TextUtils.LineSep);
                        }
                        else
                        if (question instanceof Questionnaire.MultiChoiceQuestion){
                            Questionnaire.MultiChoiceQuestion q = (Questionnaire.MultiChoiceQuestion)question;
                            textout.write(TextUtils.enPara(q.questionText, paraSep)+paraSep);
                            for (Pair<String,String[]> option : q.options)
                                textout.write(TextUtils.enLine(option.getValue1())+TextUtils.LineSep);
                        }
                        else
                          Utils.fail ("missing question type "+question.controlString()+
                                  " in SurveyMonkeyQuestionnaire.processQuestionnaire");
                    }
                }
            }
        }
    }

    void progQuestionOut(int qIdx) throws IOException {
        textout.write(Integer.toString(++questionNum)+"."+TextUtils.LineSep+TextUtils.LineSep);
        textout.write(PaperQuestionnaire.stringOfQuestion(progQuestions[qIdx]));
        textout.write(TextUtils.LineSep);

        // list the answers
        for (State b : answerPages[qIdx].choices) {
            textout.write(b.toString(" "));
            textout.write(TextUtils.LineSep);
        }

        textout.write(TextUtils.LineSep+"any other values for ");
        PaperQuestionnaire.showVariables(textout, test.progQuestions[qIdx].state);
        textout.write(":"+TextUtils.LineSep+TextUtils.LineSep);
    }
    
    static class Headers {
        // we build two components, because some things go in the first row, 
        // and some things go in the second.
        final Vector<String> h0, h1;

        int quStart=0, quEnd=0;
        
        Headers(int size) {
            h0 = new Vector<String>(size);
            h1 = new Vector<String>(size);
        }

        void addColumn(String c0, String c1) {
            // System.out.println("adding \""+c0+"\", \""+c1+"\"");
            h0.add(c0);
            h1.add(c1);
        }
        
        void addColumn(String c) {
            addColumn(c,"");
        }
        
        void addColumn(String[] c0, String c1) {
            addColumn(TextUtils.enLine(c0), c1);
        }
        
        void addColumn(String c0, String[] c1) {
            addColumn(c0, TextUtils.enLine(c1));
        }
        
        void addColumn(String[] c0, String[] c1) {
            addColumn(TextUtils.enLine(c0), TextUtils.enLine(c1));
        }
        
        void addColumns(String[] cols) {
            if (cols!=null)
                for (String c : cols)
                    addColumn(c);
        }
        
        void addMultiColumn(String[] qtext, String[] options) {
            // System.out.println("should now add "+options.length+" columns");
            // actually adds many columns
            addColumn(qtext, options[0]);
            for (int i=1; i<options.length; i++)
                addColumn("", options[i]);
        }
        
        void addMultiColumn(String[] qtext, Pair<String,String[]>[] options) {
            String[] cols = new String[options.length];
            for (int idx = 0; idx<cols.length; idx++)
                cols[idx] = TextUtils.enLine(options[idx].getValue1());
            addMultiColumn(qtext, cols);
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
                    if (q instanceof Questionnaire.AuthQuestion) 
                        addColumn(((Questionnaire.AuthQuestion)q).authText, 
                                "Open-Ended Response");
                    else
                    if (q instanceof Questionnaire.ChoiceQuestion) 
                        addColumn(((Questionnaire.ChoiceQuestion)q).questionText, 
                                "Response");
                    else
                    if (q instanceof Questionnaire.TextQuestion)
                        addColumn(((Questionnaire.TextQuestion)q).questionText, 
                                "Open-Ended Response");
                    else
                    if (q instanceof Questionnaire.NumberQuestion ||
                            q instanceof Questionnaire.MultiTextQuestion ||
                            q instanceof Questionnaire.ArrayChoiceQuestion ||
                            q instanceof Questionnaire.FinalQuestion)
                        Utils.showInfoAlert("Marking SurveyMonkey questionnaire",
                                "Can't do SurveyMonkey headers for " +
                                   q.controlString());
                        /* addColumn(((Survey.NumberQuestion)q).questionText, 
                                "Open-Ended Response"); */
                    else
                    if (q instanceof Questionnaire.MultiChoiceQuestion) {
                        addMultiColumn(((Questionnaire.MultiChoiceQuestion)q).questionText, 
                                ((Questionnaire.MultiTextQuestion)q).options); /* by analogy with program questions */
                    }
                    else
                    if (q instanceof Questionnaire.TestSection)
                        addColumns(((Questionnaire.TestSection)q).test);
                    else
                    if (q instanceof Questionnaire.ConditionalSection) {
                        Utils.showInfoAlert("Marking SurveyMonkey questionnaire",
                                "Can't do SurveyMonkey headers for " +
                                   q.controlString() +
                                   " but will do headers for questions inside the " +
                                           q.controlString());
                        addColumns(((Questionnaire.ConditionalSection)q).thens);
                        addColumns(((Questionnaire.ConditionalSection)q).elses);
                    }
                    else
                    if (q instanceof Questionnaire.GroupStarter)
                        ; // which had better be skip
                    else
                        Utils.crash("missing question type " +
                                q.controlString() +
                                " in SMQuestionnaireHeaders.addColumns");
        }

        void addColumns(Test qu) {
           quStart = h0.size();
           for (int qIdx = 0; qIdx<qu.progQuestions.length; qIdx++) {
               String[] qLines = PaperQuestionnaire.questionLines(qu.progQuestions[qIdx]);
               Vector<String> choices = 
                       new Vector<String>(qu.answerPages[qIdx].choices.size());
                String qid = Integer.toString(Utils.ordinal(qIdx));
                for (int cIdx = 0; cIdx<qu.answerPages[qIdx].choices.size(); cIdx++)
                    choices.add(qu.answerPages[qIdx].choices.item(cIdx).toString(" "));
                choices.add(PaperQuestionnaire.stringOfOther(qu.progQuestions[qIdx]));
                addMultiColumn(qLines, choices.toArray(new String[choices.size()]));
            }
           quEnd = h0.size();
        }
        
        String [][] toArrays() {
            String[] s0 = h0.toArray(new String[h0.size()]);
            String[] s1 = h1.toArray(new String[h1.size()]);
            if (s0.length!=s1.length)
                Utils.crash("SMQuestionnaireHeaders.toArrays: h0 "+s0.length+" h1 "+s1.length);
            return new String[][]{s0, s1};
        }
        
    }

}
