package uk.ac.mdx.RBornat.Saeedgenerator;

import static uk.ac.mdx.RBornat.Saeedgenerator.Localizer.__;

import java.awt.FileDialog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.javatuples.Pair;

public class PaperQuestionnaire {
    final Questionnaire questionnaire;
    final MaybeFileWriter questionsout;
    final MaybeFileWriter answersout;
    final MaybeFileWriter csvout;
    
    PaperQuestionnaire(Questionnaire questionnaire, MaybeFileWriter questionsout, MaybeFileWriter answersout, MaybeFileWriter csvout) {
        this.questionnaire = questionnaire;
        this.questionsout = questionsout;
        this.answersout = answersout;
        this.csvout = csvout;
    }
    
    static final String 
    answerPreamble =
        "The new values of";

    void laTeXPreambles(String title, String[] welcome) throws IOException {
        String commonheader = "\\documentclass[11pt,a4wide]{article}"+TextUtils.LineSep+
        __("paper.localPreamble")+TextUtils.LineSep+
        "\\usepackage{varwidth}"+TextUtils.LineSep+
        "\\usepackage{alltt}"+TextUtils.LineSep+
        "\\usepackage{multirow}"+TextUtils.LineSep+
        "\\usepackage{array}"+TextUtils.LineSep+
        "\\usepackage{boxedminipage}"+TextUtils.LineSep+
        TextUtils.LineSep+
        "\\newcommand{\\hstrut}[1]{\\rule{#1}{0pt}}"+TextUtils.LineSep+
        "\\newcommand{\\vstrut}[1]{\\rule{0pt}{#1}}"+TextUtils.LineSep+
        "\\newcommand{\\hsep}[1]{\\begin{center}\\rule{#1}{2pt}\\end{center}}"+TextUtils.LineSep+
        TextUtils.LineSep+
        "\\hoffset = 0mm"+TextUtils.LineSep+
        "\\voffset = 0mm"+TextUtils.LineSep+
        "\\textwidth = 170mm"+TextUtils.LineSep+
        "\\textheight = 229mm"+TextUtils.LineSep+
        "\\oddsidemargin = 0.0 in"+TextUtils.LineSep+
        "\\evensidemargin = 0.0 in"+TextUtils.LineSep+
        "\\topmargin = 0.0 in"+TextUtils.LineSep+
        "\\headheight = 0.0 in"+TextUtils.LineSep+
        "\\headsep = 0.0 in"+TextUtils.LineSep+
        "\\parskip = 3pt"+TextUtils.LineSep+
        "\\parindent = 0.0in"+TextUtils.LineSep+
        TextUtils.LineSep;

        questionsout.write(commonheader);
        
        questionsout.write(
                "\\newcommand{\\questionpreamble}{\\vstrut{10pt}" +
                __("paper.progQuestionPreamble")+" in the next column.}" + TextUtils.LineSep+
                "\\newcommand{\\answerpreamble}{"+answerPreamble+"}"+TextUtils.LineSep+
                "\\newcommand{\\answermidamble}{\\vstrut{20pt}Any other values for}"+TextUtils.LineSep+
                "\\newcommand{\\notespreamble}{\\vstrut{10pt}Use this column for your rough notes please}"+TextUtils.LineSep+
                "\\newcommand{\\tickbox}{\\raisebox{0mm}{\\fbox{\\hstrut{1.5mm}\\vstrut{1.5mm}}}}"+TextUtils.LineSep);
        
        questionsout.write("\\title{"+title+"}"+TextUtils.LineSep+
                "\\date{}"+TextUtils.LineSep);
        
        questionsout.write(
                "\\newcommand{\\qsep}{\\rule{100pt}{2pt}}" + TextUtils.LineSep +
                "\\setlength{\\unitlength}{1pt}" + TextUtils.LineSep +
                "\\newcommand{\\answerbox}{\\begin{tabular}{|c|}%" + TextUtils.LineSep +
                "\\hline%" + TextUtils.LineSep +
                "\\vstrut{20pt}\\hstrut{80mm} \\\\%" + TextUtils.LineSep +
                "\\hline%" + TextUtils.LineSep +
                "\\end{tabular}}" + TextUtils.LineSep +
                "\\newcommand{\\mycircle}{\\raisebox{-13pt}" +
                "{\\begin{picture}(32,32)\\put(16,16){\\circle{16}}" +
                "\\end{picture}}}" + TextUtils.LineSep +
                "\\newcommand{\\mysquare}{\\raisebox{-13pt}" +
                "{\\begin{picture}(32,32)" +
                "\\multiput(0,4)(0,20){2}{\\line(1,0){20}}" +
                "\\multiput(0,4)(20,0){2}{\\line(0,1){20}}" +
                "\\end{picture}}}" + TextUtils.LineSep);
        
       questionsout.write("\\begin{document}"+TextUtils.LineSep+"\\maketitle{}"+TextUtils.LineSep);
        
       questionsout.write(TextUtils.enParaLaTeX(welcome)+TextUtils.LineSep);

       answersout.write(commonheader+"\\begin{document}"+TextUtils.LineSep);
    }

    void laTeXPostambles(String[] goodbye) throws IOException {
        questionsout.write(TextUtils.enParaLaTeX(goodbye)+TextUtils.LineSep);
        questionsout.write("\\end{document}"+TextUtils.LineSep);
        answersout.write("\\end{document}"+TextUtils.LineSep);
    }

    static void showLaTeXMultiple(MaybeFileWriter f, Object[] os, String before, String after) throws IOException {
        for (int i=0; i<os.length; i++)
            f.write(before+os[i]+after);
    }

    static void defineTabularColumns(MaybeFileWriter f, int numvars, int width) throws IOException {
        for (int i=1; i<numvars; i++)
            f.write("m{"+width/numvars+"mm}");
        f.write("m{"+(width-width/numvars*(numvars-1))+"mm}");
    }
    
    static void showTeXQuestion(MaybeFileWriter f, State s, Assign[] assigns) throws IOException {
        showLaTeXMultiple(f, s.pairs, "int ", "; \\\\ "+TextUtils.LineSep);
        f.write("\\\\ "+TextUtils.LineSep);
        showLaTeXMultiple(f, assigns, "", " \\\\ "+TextUtils.LineSep);
    }
    
    static void showTxtQuestion(MaybeFileWriter f, State s, Assign[] assigns) throws IOException {
        for (VarVal vv : s.pairs) 
            f.write("int "+vv+";"+TextUtils.LineSep);
        f.write(TextUtils.LineSep);
        for (Assign a : assigns) 
            f.write(a.toString()+TextUtils.LineSep); // yes, assignment in Java includes a semicolon ..
        f.write(TextUtils.LineSep);
    }
    
    static void showVariables(MaybeFileWriter f, State s) throws IOException {
        for (int i=0; i<s.pairs.length-2; i++)
            f.write(s.pairs[i].var+", ");
        f.write(s.pairs[s.pairs.length-2].var+" and "+s.pairs[s.pairs.length-1].var);
    }
    
    void outputLaTeXQuestionAndAnswers(int qidx, TestQuestion question, AnswerPage answers,
            Vector<String> randomisedTestHeaders) throws IOException {
        String qID = Integer.toString(Utils.ordinal(qidx));
        int qleftwidth = 45, qboxwidth = 8, qmidwidth = 50, qrightwidth = 40; // all millimetres
        questionsout.write("\\begin{tabular}{|m{"+qleftwidth+"mm}|m{"+qboxwidth+"mm}");
        defineTabularColumns(questionsout, question.state.pairs.length, qmidwidth);
        questionsout.write("|m{"+qrightwidth+"mm}|}"+TextUtils.LineSep);
        questionsout.write("\\hline "+TextUtils.LineSep+
                "\\multirow{10}{*}{\\begin{varwidth}{"+qleftwidth+"mm} "+TextUtils.LineSep+
                "\\textbf{\\footnotesize{"+qID+". \\questionpreamble}}"+TextUtils.LineSep+
                "\\texttt{\\\\ \\\\ "+TextUtils.LineSep);
        showTeXQuestion(questionsout, question.state, question.commands);
        questionsout.write("\\\\ }"+TextUtils.LineSep+"\\end{varwidth}}"+TextUtils.LineSep+
                "& \\multicolumn{"+(question.state.pairs.length+1)+"}{l|}{\\textbf{\\footnotesize{\\answerpreamble\\ ");
        showVariables(questionsout, question.state);
        questionsout.write("}}} "+
                "& \\multirow{1}{*}{\\begin{varwidth}{"+qrightwidth+"mm}"+TextUtils.LineSep+
                "\\textbf{\\footnotesize{\\notespreamble}}\\\\ "+TextUtils.LineSep+
                "\\end{varwidth}}\\\\ "+TextUtils.LineSep);
        // a spacer before the answers
        for (int i=0; i<question.state.pairs.length+2; i++)
            questionsout.write("& ");
        questionsout.write("\\\\ "+TextUtils.LineSep);

        // list the answers
        // in the order given in the test description (legacy tests); in random order otherwise
        Integer[] indices = answers.getIndices();

        if (indices==null) {
            List<Integer> indexlist = new ArrayList<Integer>();
            for (int i = 0; i<answers.choices.size(); i++) 
                indexlist.add(i);
            if (!questionnaire.test.isLegacyTest())
                Collections.shuffle(indexlist);
            indices = indexlist.toArray(new Integer[answers.choices.size()]);
        }
        
        for (int idx : indices) {
            State b = answers.choices.item(idx);
            randomisedTestHeaders.add(SpreadsheetHeaders.standardTestSubQuestionHeader(qidx, idx, b));
            questionsout.write("& \\tickbox ");
            for (VarVal v : b.pairs) {
                questionsout.write("& "+v);
            }
            questionsout.write("& \\\\ "+TextUtils.LineSep);
        }
      
        // another spacer
        questionsout.write("& \\multicolumn{"+(question.state.pairs.length+1) + 
                "}{l|}{\\textbf{\\footnotesize{\\answermidamble\\ ");
        showVariables(questionsout, question.state);
        questionsout.write("}}}  & \\\\");
        
        // and some blank answers
        randomisedTestHeaders.add(SpreadsheetHeaders.standardTestOtherQuestionHeader(qidx));
        for (int i=0; i<question.state.pairs.length+1; i++) {
            questionsout.write("&  \\vstrut{10pt} ");
            for (VarVal v : question.state.pairs)
                questionsout.write("& "+v.var+"= ");
            questionsout.write("& \\\\ "+TextUtils.LineSep);
        }
        questionsout.write("\\hline "+TextUtils.LineSep);
        questionsout.write("\\end{tabular}\\vspace{10pt} \\\\ "+TextUtils.LaTeXParaSep);
                
        // answer sheet
        int aleftwidth = 40, amidwidth = 60, arightwidth = 50; // all millimetres
        String csep = "\\cline{2-"+(question.state.pairs.length+2)+"}";
        answersout.write("\\begin{tabular}{|m{"+aleftwidth+"mm}|");
        defineTabularColumns(answersout, question.state.pairs.length, amidwidth);
        answersout.write("|m{"+arightwidth+"mm}|}"+TextUtils.LineSep);
        answersout.write("\\hline "+TextUtils.LineSep+
                "\\multirow{10}{*}{\\begin{varwidth}{"+aleftwidth+"mm} "+TextUtils.LineSep+
                "\\texttt{\\\\ \\\\ "+qID+". \\\\ \\\\ "+TextUtils.LineSep);
        showTeXQuestion(answersout, question.state, question.commands);
        answersout.write("\\\\ }"+TextUtils.LineSep+"\\end{varwidth}}"+TextUtils.LineSep);

        // take out the singles
        for (Answer a: answers.answers) {
            if (a.ticks.size()==1) {
                for (State s: a.ticks)
                    showLaTeXMultiple(answersout, s.pairs, "& ", " "); 
                answersout.write("& "+a.models+" \\\\ "+csep+TextUtils.LineSep);
            }
        }

        // there is always at least one multiple answer
        answersout.write("&\\multicolumn{"+(question.state.pairs.length+1)+"}{l|}{\\vstrut{5pt}} \\\\ ");

        for (Answer a : answers.answers) {
            if (a.ticks.size()>1) {
                boolean first = true;
                answersout.write(csep+" "+TextUtils.LineSep);
                for (State s : a.ticks) {
                    showLaTeXMultiple(answersout, s.pairs, "& ", " "); 
                    if (first) {
                        answersout.write("& \\multirow{"+a.ticks.size()+"}{"+qrightwidth+"mm}{"+a.models+"}");
                        first = false;
                    }
                    else
                        answersout.write("& \\hstrut{1pt}");
                    answersout.write(" \\\\  "+TextUtils.LineSep);
                }
            }
        }            
        answersout.write("\\hline "+TextUtils.LineSep);
        answersout.write("\\end{tabular} \\\\ "+TextUtils.LineSep);
    }
    
    static String[] questionLines(TestQuestion q) {
        Vector<String> lines = new Vector<String>(10);
        lines.add(__("paper.progQuestionPreamble")+" below.");
        lines.add(""); // blank line
        VarVal[] pairs = q.state.pairs;
        for (VarVal vv : pairs) 
           lines.add("int "+vv+";");
        lines.add("");
        for (Assign a : q.commands) 
            lines.add(a.toString());
        lines.add("");
        lines.add(answerPreamble+" "+stringOfStateVars(q.state)+":");
        return lines.toArray(new String[lines.size()]);
    }
    
    // this now puts LineSep between the lines, and doesn't do whatever enPara does.
    static String stringOfQuestion(TestQuestion q) {
        SeparatedStringBuilder question = new SeparatedStringBuilder(TextUtils.LineSep);
        question.append(PaperQuestionnaire.questionLines(q));
        question.append("");
        return question.toString();
    }
    
    static String stringOfStateVars(State s) {
        String r = "";
        for (int i=0; i<s.pairs.length-2; i++)
            r = r+s.pairs[i].var+", ";
        r = r + s.pairs[s.pairs.length-2].var+" and "+s.pairs[s.pairs.length-1].var;
        return r;
    }
    
    static String stringOfOther(TestQuestion q) {
        return "any other values for "+stringOfStateVars(q.state)+":";
    }
        
    void outputLaTeXandHeaders() {
        /* Test test = questionnaire.test;
           TestQuestion[] questions = test.progQuestions;
           AnswerPage[] answerPages = test.answerPages;
         */
        
        try {
            laTeXPreambles(questionnaire.title, questionnaire.welcome);
            Vector<String> randomisedTestHeaders = processQuestions(questionnaire.questions, null);
            laTeXPostambles(questionnaire.goodbye);
            
            SpreadsheetHeaders headers = new SpreadsheetHeaders(questionnaire.questions.length);
            headers.addColumns(questionnaire.questions);
            String[] headerStrings = headers.toArray(new String[headers.size()]);
            SeparatedStringBuilder csvb = new SeparatedStringBuilder(",");
            for (int i=0; i<headers.quStart; i++) {
                csvb.append(headerStrings[i]);
            }
            csvb.append(randomisedTestHeaders.toArray(new String[randomisedTestHeaders.size()]));
            for (int i=headers.quEnd; i<headerStrings.length; i++) {
                csvb.append(headerStrings[i]);
            }
            csvout.writeln(csvb.toString());
            
        } catch (IOException e) {
            Utils.crash("IOException "+e+" whilst writing questionnaire and answer sheet (TeX)");
        }
    }
    
    boolean finalised = false, questioned = false;
    String groupId = "";
    int questionNum = 0;
    
    private HashMap<String, Pair<String, HashMap<String,String[]>>> questionMap = 
            new HashMap<String, Pair<String, HashMap<String,String[]>>>(20);

    private Vector<String> processQuestions(Questionnaire.Question questions[], Pair<String,String> condition) 
                throws IOException {
        if (questions!=null) {
            Vector<String> randomisedTestHeaders = new Vector<String>(questions.length);

            for (Questionnaire.Question question : questions) {
                if (question!=null) {
                    if (finalised)
                        Utils.fail("no questions can follow **Final -- PaperQuestionnaire.processQuestions sees "+question);                
    
                    if (question instanceof Questionnaire.GroupStarter) {
                        newPage(questionsout);
                        questionsout.write("\\begin{center}{\\LARGE{}"+
                                ((Questionnaire.GroupStarter)question).groupMessage+"}\\end{center}"+
                                TextUtils.LaTeXParaSep);
                        groupId = ((Questionnaire.GroupStarter)question).id;
                        questionNum = 0;
                    }
                    else
                    if (question instanceof Questionnaire.TestSection) {
                        questionsout.write(TextUtils.LineSep+"\\newpage"+TextUtils.LineSep+TextUtils.LineSep);
                        for (int qIdx=0; qIdx<questionnaire.test.progQuestions.length; qIdx++) {
                            outputLaTeXQuestionAndAnswers(qIdx, 
                                    questionnaire.test.progQuestions[qIdx], questionnaire.test.answerPages[qIdx],
                                    randomisedTestHeaders); 
                                // responds properly if either or both files is/are null
                            answersout.write("\\newpage "+TextUtils.LineSep);
                        }
                    }
                    else
                    if (question instanceof Questionnaire.ConditionalSection)
                        processConditionalSection((Questionnaire.ConditionalSection)question, condition);
                    else
                    if (question instanceof Questionnaire.FinalQuestion) {
                        processFinalQuestion((Questionnaire.FinalQuestion)question);
                        finalised = true;
                    }
                    else
                    if (question instanceof Questionnaire.AuthQuestion)
                        processAuthorisationQuestion((Questionnaire.AuthQuestion)question);
                    else {
                        /* if (separatorNeeded)
                            questionsout.write(TextUtils.LaTeXParaSep+"\\qsep"+TextUtils.LaTeXParaSep);
                        
                        separatorNeeded = true; */
                        
                        questionsout.write(TextUtils.LaTeXParaSep);
                        
                        questionsout.write("\\textbf{"+groupId+(++questionNum)+":} ");
                        
                        if (question instanceof Questionnaire.TextQuestion)
                            processTextQuestion((Questionnaire.TextQuestion)question);
                        else
                        if (question instanceof Questionnaire.MultiTextQuestion)
                            processMultiTextQuestion((Questionnaire.MultiTextQuestion)question);
                        else
                        if (question instanceof Questionnaire.NumberQuestion)
                            processNumberQuestion((Questionnaire.NumberQuestion)question);
                        else
                        if (question instanceof Questionnaire.ChoiceQuestion)
                            processChoiceQuestion((Questionnaire.ChoiceQuestion)question);
                        else
                        if (question instanceof Questionnaire.MultiChoiceQuestion)
                            processMultiChoiceQuestion((Questionnaire.MultiChoiceQuestion)question);
                        else
                        if (question instanceof Questionnaire.ArrayChoiceQuestion)
                            processArrayChoiceQuestion((Questionnaire.ArrayChoiceQuestion)question);
                        else
                          Utils.fail ("PaperQuestionnaire.processQuestions cannot handle "+question);
                    }
                }
            }
            return randomisedTestHeaders;
        }
        else
            return null;
    }

    void processAuthorisationQuestion(Questionnaire.AuthQuestion question) throws IOException {
        questionsout.write(TextUtils.LaTeXParaSep+"\\hsep{100pt}"+TextUtils.LaTeXParaSep);
        questionsout.write(TextUtils.enParaLaTeX(question.authText)+TextUtils.LaTeXParaSep);
        questionsout.write("If you are happy to allow your answers to be used in this way, " +
        		"please sign here: \\\\" +
        		"\\vstrut{35pt} \\\\" +
        		"and then fill in the rest of the questionnaire."+TextUtils.LaTeXParaSep);

	questionsout.write("If you do not consent to use of your answers, " +
			"we're sorry you weren't able to participate. " +
			"Please ignore the rest of the questionnaire." +TextUtils.LineSep);
    }
    
    void newPage(MaybeFileWriter f) throws IOException {
        f.write(TextUtils.LineSep+"\\newpage" +TextUtils.LineSep);
    }
    
    String boxWidth = "100mm";
    
    void boxOpen() throws IOException {
        questionsout.write("\\begin{tabular}{r|m{"+boxWidth+"}|}" +
                TextUtils.LineSep);
    }
    
    void boxLine(String text) throws IOException {
        questionsout.write("\\cline{2-2}" +
                TextUtils.LineSep+
                text+
                " & \\vstrut{20pt} \\\\" +
                TextUtils.LineSep);      
    }
    void boxClose() throws IOException {
        questionsout.write("\\cline{2-2}"+
                        TextUtils.LineSep+
                        "\\end{tabular}\\vspace{10pt}"+
                        TextUtils.LineSep);
    }
    
    void processTextQuestion(Questionnaire.TextQuestion question) throws IOException {
        questionsout.write(TextUtils.enParaLaTeX(question.questionText)+TextUtils.LaTeXParaSep+
                "\\answerbox\\vspace{10pt}"+TextUtils.LineSep);
    }
    
    void processNumberQuestion(Questionnaire.NumberQuestion question) throws IOException {
        questionsout.write(TextUtils.enParaLaTeX(question.questionText)+TextUtils.LaTeXParaSep+
                "\\answerbox\\vspace{10pt}"+TextUtils.LineSep);
    }
    
    void processMultiTextQuestion(Questionnaire.MultiTextQuestion question) throws IOException {
        questionsout.write(TextUtils.enParaLaTeX(question.questionText)+TextUtils.LaTeXParaSep);
        boxOpen();
        for (Pair <String,String[]> opt : question.options)
            boxLine(TextUtils.enLine(opt.getValue1()));
        boxClose();
    }
    
    void processChoiceQuestion(Questionnaire.ChoiceQuestion question) throws IOException {
        HashMap<String,String[]> subqs = new HashMap<String,String[]>();
        for (Pair<String,String[]> option : question.options)
            subqs.put(option.getValue0(), option.getValue1());
        questionMap.put(question.id, Pair.with(groupId+questionNum, subqs));
      
        questionsout.write(TextUtils.enParaLaTeX(question.questionText)+TextUtils.LaTeXParaSep);
        
        generateMultiTable(question.columns, question.options, "\\mycircle");
    }
    
    void processMultiChoiceQuestion(Questionnaire.MultiChoiceQuestion question) throws IOException {
        questionsout.write(TextUtils.enParaLaTeX(question.questionText)+TextUtils.LaTeXParaSep);
        
        generateMultiTable(question.columns, question.options, "\\mysquare");
    }

    int maxTableColumns = 4;
    
    void generateMultiTable(int columns, Pair <String,String[]>[] options, String icon) throws IOException {
        questionsout.write("\\begin{tabular}{");
        int realColumns = Math.min(maxTableColumns, Math.min(columns, options.length));
        for (int i=0; i<realColumns; i++)
            questionsout.write("|rc");
        questionsout.write("|}"+TextUtils.LineSep+"\\hline"+TextUtils.LineSep);
        SeparatedStringBuilder sb = new SeparatedStringBuilder(" & ");
        int colNum = 0;
        for (Pair <String,String[]> opt : options) {
            if (colNum==realColumns) {
                questionsout.write(sb.toString()+TextUtils.LineSep);
                questionsout.write("  \\\\"+TextUtils.LineSep+"\\hline"+TextUtils.LineSep);
                sb = new SeparatedStringBuilder(" & ");
                colNum = 0;
            }
            sb.append(TextUtils.enLine(opt.getValue1()));
            sb.append(icon);
            ++colNum;
        }
        questionsout.write(sb.toString()+TextUtils.LineSep);
        questionsout.write("  \\\\"+TextUtils.LineSep+"\\cline{1-"+(colNum*2)+"}"+TextUtils.LineSep+"\\end{tabular}\\vspace{10pt}"+TextUtils.LineSep);
    }
   
    void processArrayChoiceQuestion(Questionnaire.ArrayChoiceQuestion question) throws IOException {
        questionsout.write(TextUtils.enParaLaTeX(question.questionText)+TextUtils.LaTeXParaSep);
        
        questionsout.write("\\begin{tabular}{r|");
        for (Pair <String, String[]> vertic : question.vertics)
            questionsout.write("c|");
        questionsout.write("}"+TextUtils.LineSep);
        questionsout.write("\\multicolumn{1}{r}{}");
        for (int vidx=0; vidx<question.vertics.length; vidx++)
            questionsout.write(" & "+"\\multicolumn{1}{c}{"+
                    TextUtils.enLine(question.vertics[vidx].getValue1())+
                    "}");
        questionsout.write("\\\\"+TextUtils.LineSep);
        for (Pair <String, String[]>horiz : question.horizs) {
            questionsout.write("\\cline{2-"+(question.vertics.length+1)+"}"+TextUtils.LineSep);
            questionsout.write(TextUtils.enLine(horiz.getValue1()));
            for (Pair <String, String[]> vertic : question.vertics)
                questionsout.write(" & \\mycircle");
            questionsout.write("\\\\"+TextUtils.LineSep);
        }
        questionsout.write("\\cline{2-"+(question.vertics.length+1)+"}"+TextUtils.LineSep);
        questionsout.write("\\end{tabular}\\vspace{10pt}");
    }
    
    void processConditionalSection(Questionnaire.ConditionalSection section, Pair<String,String> previouscondition) throws IOException {
        if (previouscondition!=null)
            Utils.fail("PaperTestQuestionnaire cannot handle nested **If sections");
        Pair<String,String> newcondition = section.condition;
        Pair<String, HashMap<String,String[]>> qinfo = questionMap.get(newcondition.getValue0());
        if (qinfo==null)
            Utils.fail("Can't find question with id "+newcondition.getValue0()+
                    " (for condition "+newcondition.getValue0()+" "+newcondition.getValue1()+")");
        String [] subq = qinfo.getValue1().get(newcondition.getValue1());
        if (subq==null)
            Utils.fail("Can't find answer with id "+newcondition.getValue1()+
                    " (for condition "+newcondition.getValue0()+" "+newcondition.getValue1()+")");

        questionsout.write(
                "\\vstrut{10pt}" +TextUtils.LaTeXParaSep+ 
                "\\begin{tabular}{ll}" +TextUtils.LineSep+
                "\\begin{minipage}{30mm}" +TextUtils.LineSep+
                "If your answer to " +
                qinfo.getValue0() +
                " was " +
                TextUtils.enLine(subq) + TextUtils.LineSep+
                "\\end{minipage}" +TextUtils.LineSep+
                "&" +TextUtils.LineSep+
                "{\\setlength{\\fboxrule}{2pt}" +TextUtils.LineSep+
                "\\setlength{\\fboxsep}{10pt}" +TextUtils.LineSep+
                "\\begin{boxedminipage}{130mm}" +TextUtils.LineSep
                );
        boxWidth = "80mm";
        maxTableColumns = 3;
        Vector<String> thensRTHs = processQuestions(section.thens, newcondition);
        if (thensRTHs!=null && thensRTHs.size()!=0)
            Utils.fail("PaperQuestionnaire cannot handle **Questionnaire inside **If");
        maxTableColumns = 4;
        boxWidth = "100mm";
        if (section.elses!=null)
            Utils.fail("PaperQuestionnaire cannot handle **Else or **Elsef");
        
        questionsout.write(
                "\\end{boxedminipage}}" +TextUtils.LineSep+
                "\\end{tabular}\\vspace{10pt}"+TextUtils.LaTeXParaSep
                );
    }
    
    void processFinalQuestion(Questionnaire.FinalQuestion question) throws IOException {
        questionsout.write(TextUtils.LineSep+"\\newpage"+TextUtils.LineSep+TextUtils.LineSep);
        questionsout.write(TextUtils.enParaLaTeX(question.finalText)+TextUtils.LaTeXParaSep);
        questionsout.write(
                "\\begin{boxedminipage}{170mm}\\vstrut{170mm}\\end{boxedminipage}" +
                "\\vspace{10pt}"+TextUtils.LaTeXParaSep);
       
    }
    
 }
