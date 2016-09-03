package uk.ac.mdx.RBornat.Saeedgenerator;

import java.awt.FileDialog;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import uk.ac.mdx.RBornat.Saeedgenerator.Questionnaire.QuestionnaireType;

public class Test {
    
    public final String title;
    public final TestQuestion[] progQuestions;
    public final AnswerPage[] answerPages;
    public final int nQuestionPages, questionPageNumbers[];
    public final int shortCount, longCount, singleCount, multipleCount;
    private boolean legacy = false; 

    boolean isLegacyTest() { return legacy; }
    
    static String showJavaItems(String[] items, int m, int n) {
        String[] is = new String[n-m];
        for (int i=m; i<n; i++)
            is[i-m] = items[i];
        return TextUtils.interpolateStrings(is, " ");
    }

    static void checkMapped(State s, Variable var, boolean assigned, String line) {
        if (!s.maps(var))
            Utils.fail((assigned ? "assignment to" : "use of")+" "+var+
                    ", but no declaration of "+var+" in line "+line);
    }

    static String[] getJavaItems(String line, boolean doubledash) {
        int si = 0;
        Vector<String> items = new Vector<String>();
        while (si!=line.length()) {
            int c = line.charAt(si);
            if (c == ' ')
                si++;
            else 
            if (TextUtils.isAlpha(c)) {
                int sj = si;
                while (sj!=line.length() && (TextUtils.isAlpha(line.charAt(sj))||TextUtils.isDigit(line.charAt(sj)))) {
                    sj=sj+1; 
                }
                items.add(line.substring(si,sj));
                si = sj;
            }
            else
            if (TextUtils.isDigit(c)) {
                int sj = si;
                while (sj!=line.length() && TextUtils.isDigit(line.charAt(sj))) {
                    sj = sj+1;
                }
                items.add(line.substring(si,sj));
                si = sj;
            }
            else 
            if (doubledash && c=='-') {
                int sj = si;
                while (sj!=line.length() && line.charAt(sj)=='-') {
                    sj = sj+1;
                }
                items.add(line.substring(si,sj));
                si = sj;
            }
            else {
                items.add(line.substring(si,si+1));
                si = si+1;
            }
        }
        return items.toArray(new String[items.size()]);
    }

    static String[] getJavaItems(String line) { return getJavaItems(line, false); }
    
    static int skipSep(String[] items, int i) {
        if (i==items.length)
            return i;
        else
        if (items[i].equals(",") || items[i].equals(":") || items[i].equals(";") ||
                items[i].equals("/") || items[i].equals("\\"))
            return i+1;
        else
            return i;
    }
    
    static SimpleSet<State> parseOtherString(State s, String other) {
        String[] items = getJavaItems(other);
        // System.err.println("parsing other string \""+other+"\"; items.length="+items.length);
        SimpleSet<State> ss = new SimpleSet<State>();
        
        boolean allqueries = true;
        for (int i = 0; i<other.length(); i++)
            if (other.charAt(i)!='?')
                allqueries = false;
        if (allqueries || other.equals("I don't know"))
            return ss; // i.e. empty

        for (int ii = 0; ii<items.length; ) {
            // System.err.println("parseOtherString "+ii);
            // is there something like a=20 first?
            try {
                State s1 = new State();
                int jj = ii;
                for (int j=0; j<s.size(); j++) {
                    if (jj+2<items.length && 
                            TextUtils.isName(items[jj]) && items[jj+1].equals("=") && TextUtils.isNumber(items[jj+2])) {
                        Variable var = new Variable(items[jj]);
                        if (!s.maps(var)) {
                            // try again ignoring case
                            var = s.mapsIgnoreCase(var);
                            if (var==null)
                                throw (new ParseException("unknown variable", jj));
                        }
                        if (s1.maps(var))
                            throw (new ParseException("already mentioned", jj));
                        s1 = s1.add(var, new Value(Integer.parseInt(items[jj+2]))); 
                        jj = skipSep(items, jj+3);
                    }
                    else throw (new ParseException("no var=val", jj));
                }
                ii = jj;
                ss.add(s1);
            } catch (ParseException e) {
                // System.err.println(e.getMessage()+" \""+other+"\" -- item "+e.getErrorOffset());
                try {
                    State s1 = new State();
                    int jj = ii;
                    for (int j=0; j<s.size(); j++) {
                        if (jj<items.length && TextUtils.isNumber(items[jj]))
                            s1 = s1.add(s.item(j).var, new Value(Integer.parseInt(items[jj])));
                        else
                            throw (new ParseException("no number", jj));
                        jj = skipSep(items, jj+1);
                    }
                    ii = jj;
                    ss.add(s1);
               } catch (ParseException e1) {
                   // System.err.println(e1.getMessage()+" \""+other+"\" -- item "+e1.getErrorOffset());
                   return null;
               }
            }
        }
        return ss;
    }

    // AssignModel index is one too large: a design error. Sob.
    AssignModel readAssignModel(String item) {
        if (item.startsWith("M") && TextUtils.isNumber(item.substring(1))) {
            // it looks like a model
            int m = Integer.parseInt(item.substring(1));
            return (m==0 || m-1>=AssignModel.values().length ? null : AssignModel.values()[m-1]);
        }
        else
            return null;
    }
    
    SequenceModel readSequenceModel(String item) {
        if (item.startsWith("S") && TextUtils.isNumber(item.substring(1))) {
            // it looks like a model
            int m = Integer.parseInt(item.substring(1));
            return (m==0 || m>=SequenceModel.values().length ? null : SequenceModel.values()[m]);
        }
        else
            return null;
    }
    
    Answer readAnswer(FBufferedReader testin,  SimpleMap<State,Integer> indexMap, SimpleSet<State> states, 
                        String line, String [] items, int ii, TestQuestion q, int qnum) 
          throws IOException {
        
        /* each answer is a semicolon-separated list of states, 
         * followed by --, , followed by semicolon,
         * followed by a /-separated list of models.
         * 
         * The argument 'states' contains the states in the answer so far.
         * 
         * The answer may spread over several lines, but states and model lists can't be split.
         * 
         * Each state is a comma-separated list of <var>=num.
         * 
         * Each model is M<int>+S<int> 
         *  -- unless there is only a single assignment in the question, in which
         *     case only M<int>, and only one model in the answer.
         */

        State s = new State();
        int index = 0;
        
        if (TextUtils.isNumber(items[ii])) {
            index = Integer.parseInt(items[ii]);
            ii++;
        }
        
        while (ii+2<items.length && 
                TextUtils.isName(items[ii]) && 
                items[ii+1].equals("=") &&
                TextUtils.isNumber(items[ii+2])) {
            Variable var = new Variable(items[ii]);
            Value val = new Value(Integer.parseInt(items[ii+2]));
            if (!q.state.maps(var))
                Utils.fail (line+": "+var+" is not a variable of the question");
            if (s.maps(var))
                Utils.fail (line+": variable "+var+" is mentioned twice");
            s = s.add(var,val);
            if (ii+3<items.length && items[ii+3].equals(","))
                ii += 4; 
            else {
                ii += 3; break;
            }
        }
        
        if (s.size()==0)
            Utils.fail (line+": no var=val detected; ii="+ii+"; items[ii]="+items[ii]);
        
        if (ii==items.length)
            Utils.fail (line+": no semicolon or double-dash after state");
        
        if (index>0) {
            if (!indexMap.containsKey(s))
                indexMap.put(s,index);
            else {
                Integer oldindex = indexMap.get(s);
                if (index!=oldindex)
                    Utils.fail ("question "+qnum+": choice "+s+" has two indices -- "+oldindex+" and "+index);
            }
        }

        states.add(s);

        if (items[ii].equals(";")) {
            // another state coming
            if (ii+1==items.length) {
                String line2 = TextUtils.markedReadLine(testin);
                if (!TextUtils.dataLine(line2))
                    Utils.fail ("question "+qnum+"answer line missing after "+line);
                return readAnswer(testin, indexMap, states, line2, getJavaItems(line2, true), 0, q, qnum);
            }
            else
                return readAnswer(testin, indexMap, states, line, items, ii+1, q, qnum);
        }
        else
        if (items[ii].equals("--")) {
            // models coming
            if (ii+1==items.length) {
                String line2 = TextUtils.markedReadLine(testin);
                if (!TextUtils.dataLine(line2))
                    Utils.fail ("question "+qnum+"answer line missing after "+line);
                line = line2;
                items = getJavaItems(line2, true);
                ii=0;
            }
            else
                ii++;
            
            if (q.commands.length==1) {
                // only one command, only one model, and no sequence model
                if (ii+1==items.length && readAssignModel(items[ii])!=null) {
                    AssignModel am = readAssignModel(items[ii]);
                    BiModel m = new BiModel(am,SequenceModel.Isolated);
                    return new Answer(states, m);
                }
                else 
                    Utils.fail (line+": single-assignment question must have single assign model and no sequence model");
            }
            else {
                Answer a = new Answer(states, new SimpleSet<BiModel>());
                
                while (ii+2<items.length &&
                        readAssignModel(items[ii])!=null &&
                        items[ii+1].equals("+") &&
                        readSequenceModel(items[ii+2])!=null) {
                    AssignModel am = readAssignModel(items[ii]);
                    SequenceModel sm = readSequenceModel(items[ii+2]);
                    a.accept(states, new BiModel(am,sm));
                    if (ii+4<items.length && items[ii+3].equals("/"))
                        ii +=4;
                    else {
                        ii += 3; break;
                    }
                }
                
                if (ii==items.length)
                    return a;
                else
                    Utils.fail (line+": end of models reached, saw "+items[ii]);
            }
        }
        else
            Utils.fail(line+": neither semicolon or double-dash after state; saw "+items[ii]);
        
        return new Answer(states, new BiModel(AssignModel.Swap, SequenceModel.Isolated)); // shut up compiler
    }
    
    AnswerPage readAnswers(FBufferedReader testin, TestQuestion q, int qnum) {
        // Only used when legacy is true. Builds indexMap.
        String line;
        AnswerPage ap = new AnswerPage();
        SimpleMap<State,Integer>indexMap = new SimpleMap<State,Integer>();
        
        try {
            
            while ((line=TextUtils.markedReadLine(testin))!=null &&
                   line.startsWith("**Answer")) {
                line = TextUtils.stripControlWord(line);
                String [] items = getJavaItems(line, true);
                
                ap.add (readAnswer(testin, indexMap, new SimpleSet<State>(), line, items, 0, q, qnum));
            }
            
            testin.reset(); /* let others read the line */
            
        } catch (IOException e) {
            Utils.fail("IO error ("+e+") whilst reading answers to question "+qnum); 
        }
        
        // we have a map from states to Integers in indexMap. We want an array (we use an ArrayList)
        // of Integers. First check that the map size and choices size are the same.
        Integer[] indices = null;
        int mapsize = indexMap.size();
        int choicesize = ap.choices.size();
        
        if (mapsize!=choicesize) {
            // if you have any indices then then you must have them all
            if (mapsize!=0)
                Utils.fail ("question "+qnum+(mapsize>choicesize ? " has too many choices: "
                                                                 : " doesn't have enough choices: "
                                             )
                                +choicesize+" required, "+mapsize+" provided");
        }
        else { // transform into index array
            indices = new Integer[mapsize];
            for (Map.Entry<State, Integer> pair : indexMap.entrySet()) {
                State s = pair.getKey();
                int idx = pair.getValue();
                // we don't forget that idx is index+1
                int choice = ap.choices.indexOf(s);
                if (choice==-1)
                    Utils.fail ("readAnswers: no match in choices for "+pair);
                else
                if (idx-1<0 || idx-1>=indices.length)
                    Utils.fail ("question "+qnum+" readAnswers: invalid Integer "+idx+" in "+pair);
                if (indices[idx-1]==null)
                    indices[idx-1] = choice;
                else
                    Utils.fail("question "+qnum+": choice "+idx+" multiply defined: "
                                    +ap.choices.elementAt(indices[idx-1])+ " and "+s);
            }
        }
        
        ap.setIndices(indices);
        return ap;
    }
    
    enum BiasResponse { Abandon, TryAgain, Save, Ignore };

    Test(FBufferedReader testin, boolean checkRepetitions) {
        String id = null;
        String title=null;
        TestQuestion[] questions=null;
        AnswerPage [] includedAnswers=null;
        String line;
        
        try {
            while ((line=TextUtils.markedReadLine(testin))!=null) {
                if (line.startsWith("**Id"))
                    if (id==null)
                        id = TextUtils.stripControlWord(line);
                    else
                        Utils.fail("two **Id lines");
                else
                if (line.startsWith("**Title"))
                    if (title==null)
                        title = TextUtils.stripControlWord(line);
                    else
                        Utils.fail("two **Title lines");
                else
                if (line.equals("**Legacy"))
                    legacy = true;
                else
                if (line.equals("**Questions")) 
                    if (questions==null ){
                        Vector<TestQuestion> qs = new Vector<TestQuestion>();
                        Vector<AnswerPage> aps = new Vector<AnswerPage>();

                        while (TextUtils.dataLine((line=TextUtils.markedReadLine(testin)))) {
                            String[] items;
                            // skip blank lines
                            if ((items = getJavaItems(line)).length!=0) {
                                // each line is a sequence of declarations "int var = num;"
                                // followed by a sequence of assignments "var = var';"
                                State s = new State();
                                int ii = 0;
                                // declarations
                                while (ii<=items.length-5 && items[ii].equals("int")) {
                                    if (TextUtils.isName(items[ii+1]) && items[ii+2].equals("=") &&
                                            TextUtils.isNumber(items[ii+3]) && items[ii+4].equals(";")) {
                                        Variable var = new Variable(items[ii+1]);
                                        Value val = new Value(Integer.parseInt(items[ii+3]));
                                        if (s.maps(var))
                                            Utils.fail("more than one declaration of "+var+" in line "+line);
                                        s = s.add(var, val);
                                        // System.err.println("found int "+vvs.elementAt(vvs.size()-1)+";");
                                        ii = ii+5;
                                    }
                                    else
                                        Utils.fail("malformed declaration "+showJavaItems(items, ii, ii+5)+" in line "+line);
                                }

                                if (s.size()==0)
                                    Utils.fail("no declarations in line "+line);
                                
                                // assignments
                                Vector<Assign> as = new Vector<Assign>();
                                while (ii<=items.length-4) {
                                    if (TextUtils.isName(items[ii]) && items[ii+1].equals("=") &&
                                            TextUtils.isName(items[ii+2]) && items[ii+3].equals(";")) {
                                        Variable left = new Variable(items[ii]), right = new Variable(items[ii+2]);
                                        checkMapped(s, left, true, line);
                                        checkMapped(s, right, false, line);
                                        as.add(new Assign(left, right));
                                        // System.err.println("found "+vvs.elementAt(vvs.size()-1)+";");
                                        ii = ii+4;
                                    }
                                    else
                                        Utils.fail("malformed assignment "+showJavaItems(items, ii, ii+4)+" in line "+line);
                                }

                                if (ii!=items.length)
                                    Utils.fail("malformed assignment "+showJavaItems(items, ii, items.length)+" in line "+line);

                                if (as.size()==0)
                                    Utils.fail("no assignments in line "+line);

                                TestQuestion q = new TestQuestion(s, as.toArray(new Assign[as.size()]));
                                qs.add(q);
                                
                                if (legacy) {
                                    AnswerPage ap = readAnswers(testin, q, qs.size());
                                    aps.add(ap);
                                }
                            }
                        }
                        questions = qs.toArray(new TestQuestion[qs.size()]);
                        includedAnswers = aps.toArray(new AnswerPage[aps.size()]);
                    } 
                    else
                        Utils.fail("two **Questions sections");
                else
                    Utils.fail("Test cannot recognise control line"+line);
            } // while line!=null
        } catch (IOException e) {
            Utils.fail("How can there be an IO error ("+e+") while reading lines of the test?");
        }
        
        if (title!=null)
            this.title = title;
        else {
            this.title = null; // fool 'final' check
            Utils.fail("No test title: we must have one");
        }
       if (questions==null)
           Utils.fail("no **Questions section: we must have them");
       
       // process the questions
       this.progQuestions = questions; // can't happen if questions==null
       int qlength = questions.length;
       
       // calculate answerPages
       if (legacy)
           answerPages = includedAnswers;
       else {
           answerPages = new AnswerPage[qlength];
           for (int qidx=0; qidx<qlength; qidx++) {
               AnswerPage ap = Generator.elaborate(questions[qidx]);
               answerPages[qidx] = ap;
           }
       }

       // calculate page layout
       questionPageNumbers = new int[qlength];
       int pageLength = 35, linesSoFar = 0, pageNum = 0;
       for (int i=0; i<qlength; i++) {
           int decs = questions[i].state.size(),
           assigns = questions[i].commands.length,
           answers = answerPages[i].choices.size(),
           questionLines = 1+ //question number
           1+ // Read the following ...
           1+ // blank
           decs +
           1+ // blank
           assigns +
           1+ // blank
           1+ // the new values of ...
           1+ // blank
           (answers +
                   2+ // other is two rows at present
                   1) // to make /2 work
                   /2 // in two columns
                   ;
           if (linesSoFar!=0 && linesSoFar+questionLines>pageLength) {
               pageNum++; linesSoFar = 0;
           }
           questionPageNumbers[i] = pageNum;
           linesSoFar += questionLines;
       }
       nQuestionPages = pageNum+1;

       // calculate singleCount, multipleCount, shortCount, longCount
       int sic=0, muc=0, shc=0, loc = 0;
       
       for (int qidx=0; qidx<qlength; qidx++) {
           int commandCount = questions[qidx].commands.length;
           if (commandCount==1)
               sic++;
           else
               muc++;
           if (commandCount<=2)
               shc++;
           else
               loc++;
       }
       
       singleCount = sic;
       multipleCount = muc;
       shortCount = shc;
       longCount = loc;

       // check for questionnaire bias
       if (checkRepetitions) {
           // check for same-answer questionnaire bias
           SimpleMap<Answer,Integer[]> repetitions = new SimpleMap<Answer,Integer[]>();
           for (int qidx=0; qidx<qlength; qidx++) {
               for (Answer a : answerPages[qidx].answers)
                   if (!repetitions.containsKey(a)) {
                       Vector<Integer> reps = new Vector<Integer>();
                       for (int repidx=qidx; repidx<qlength; repidx++) {
                           if (answerPages[repidx].answers.contains(a))
                               reps.add(new Integer(repidx));
                       }
                       if (reps.size()>1) {
                           repetitions.put(a, reps.toArray(new Integer[reps.size()]));
                       }
                   }
           }
           
           if (repetitions.size()>0) {
               StringBuilder warnings = new StringBuilder();
               for (Answer a : repetitions.keySet()) {
                   SeparatedStringBuilder qb = new SeparatedStringBuilder(",");
                   Integer [] reps = repetitions.get(a);
                   for (int i=0; i<reps.length; i++)
                       qb.append(Integer.toString(Utils.ordinal(reps[i].intValue())));
                   warnings.append("["); warnings.append(qb.toString()); warnings.append("]: ");
                   warnings.append(a.toString()); warnings.append("\n");
               }
               int biasResponseCount = BiasResponse.values().length;
               String[] biasResponseNames = new String[biasResponseCount];
               for (int i=0; i<biasResponseCount; i++)
                   biasResponseNames[i] = 
                           BiasResponse.values()[i]==BiasResponse.TryAgain ? "Try Again" : 
                           BiasResponse.values()[i].name();
               int biasResponseIdx = 
                   JOptionPane.showOptionDialog(
                           null, 
                            "The test you selected has some 'questionnaire bias': by selecting the same answer" +
                           "\nin more than one question, a participant can appear to be using a particular pair" +
                           "\nof models of assignment and sequence." +
                           "\n" +
                           "\nThe repetitive answers are:" +
                           "\n" +
                           "\n" + warnings.toString() +
                           "\nDo you want to ignore this problem, save this warning to a file, try again with" +
                           "\nanother test description, or abandon the attempt to generate a questionnaire?",
                           "Questionnaire bias",
                           JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                           biasResponseNames, "Try Again");
               BiasResponse biasResponse = 
                       biasResponseIdx==JOptionPane.CLOSED_OPTION ? BiasResponse.Abandon
                                                                  : BiasResponse.values()[biasResponseIdx];

               switch (biasResponse) {
                   case Ignore:
                       break;
                   
                   case Save:
                       try {
                           File td = testin.file;
                           String tdName = td.getName();
                           Matcher tdMatch = Pattern.compile("(.+)\\.\\w+").matcher(tdName);
                           String warningFileName = 
                               tdMatch.matches() ? tdMatch.group(1)+"_qbias_warnings.txt"
                                                 : "questionnairebias_warnings.txt";
                           MaybeFileWriter warningsout = 
                               Utils.openWrite("repetition warnings", "testdescriptiondirectory", 
                                       "", warningFileName);                            
                           warningsout.writeln("Questionnaire bias due to repetitions"); 
                           warningsout.writeln();
                           warningsout.write(warnings.toString());
                           warningsout.close();
                       } catch (IOException e) {
                           Utils.crash("Exception "+e+" while writing repetition-warnings");
                       }
                       break;
                       
                   case TryAgain:
                       Generator.generate();
                       System.exit(0);
                       break;
                       
                   case Abandon:
                       System.exit(0);
                       break;
                   
                   default:
                       Utils.crash("Missing case "+biasResponse.name()+" in biasResponse analysis in Test");
               }
               
           }
        }    
    }
}
