package uk.ac.mdx.RBornat.Saeedgenerator;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

import org.apache.commons.lang3.RandomStringUtils;

public class Generator {
    final static boolean onMacOSX = System.getProperty("os.name").equals("Mac OS X");    
    final static boolean runningBlind = onMacOSX && (System.getProperty("os.version").startsWith("10.11") ||
                                                     System.getProperty("os.version").startsWith("10.12")); 

    static Value v0 = new Value(0);
    static final String version = "1.2";
    
    static AssignMechanism[] assignMechanisms = new AssignMechanism[] { 
        new AssignMechanism(AssignModel.RightToLeftMove, AssignDirection.Left) { // M1: a:=b; b:=0
            public ValuePair assign(Value v1, Value v2) {
                return new ValuePair(v2, v0);
            }
        },
        new AssignMechanism(AssignModel.RightToLeftCopy, AssignDirection.Left) { // M2: a:=b
            public ValuePair assign(Value v1, Value v2) {
                return new ValuePair(v2, v2);
            }
        }, 
        new AssignMechanism(AssignModel.LeftToRightMove, AssignDirection.Right) { // M3: b:=a; a:=0
            public ValuePair assign(Value v1, Value v2) {
                return new ValuePair(v0, v1);
            }
        },
        new AssignMechanism(AssignModel.LeftToRightCopy, AssignDirection.Right) { // M4: b:=a
            public ValuePair assign(Value v1, Value v2) {
                return new ValuePair(v1, v1);
            }
        },
        new AssignMechanism(AssignModel.RightToLeftCopyAdd, AssignDirection.Left) { // M5: a:=a+b
            public ValuePair assign(Value v1, Value v2) {
                return new ValuePair(v1.plus(v2), v2);
            }
        },
        new AssignMechanism(AssignModel.RightToLeftMoveAdd, AssignDirection.Left) { // M6: a:=a+b; b:=0
            public ValuePair assign(Value v1, Value v2) {
                return new ValuePair(v1.plus(v2), v0);
            }
        },
        new AssignMechanism(AssignModel.LeftToRightCopyAdd, AssignDirection.Right) { // M7: b:=b+a
            public ValuePair assign(Value v1, Value v2) {
                return new ValuePair(v1, v2.plus(v1));
            }
        },
        new AssignMechanism(AssignModel.LeftToRightMoveAdd, AssignDirection.Right) { // M8: b:=b+a; a:=0
            public ValuePair assign(Value v1, Value v2) {
                return new ValuePair(v0, v2.plus(v1));
            }
        }
    };

    // M9 is no change
    static Answer NoChangeAnswer(State s) {
        return new Answer(s, new BiModel(AssignModel.NoChange, SequenceModel.Isolated));
    }

    // M10 is equality (multiple answers)
    static Answer EqualityAnswer(State s, Assign[] commands, SequenceModel sm) {
        EquivalenceGraph<Variable> cs = new EquivalenceGraph<Variable>();
        for (Assign command : commands) {
            cs.add(command.left); cs.add(command.right);
            cs.makeEquiv(command.left, command.right);
        }
        SimpleSet<State> states = new SimpleSet<State>();
        states.add(s);
        Vector<Vector<Variable>> vvs = cs.classes();
        for (Enumeration<Vector<Variable>> evvs = vvs.elements(); evvs.hasMoreElements(); ) {
            Vector<Variable> vs = evvs.nextElement();
            if (vs.size()>1) { // we have a non-trivial class, multiply the states
                SimpleSet<State> newstates = new SimpleSet<State>();
                // for every existing state, for every variable, generate a new state
                for (Iterator<State> statesItr = states.iterator(); statesItr.hasNext(); ) { 
                    State s1 = statesItr.next();
                   for (Enumeration<Variable> evs = vs.elements(); evs.hasMoreElements(); ) {
                        Variable var = evs.nextElement();
                        Value val;
                        try { val = s1.get(var); } catch (State.StateLookup e) {
                            Utils.showErrorAlert("StateLookup m10answer var "+var+"; state "+s+"; assignments "+commands);
                            System.exit(1);
                            val = null; // oh dear
                        } 
                        State s2 = s1;
                        for (Enumeration<Variable> eevs = vs.elements(); eevs.hasMoreElements(); ) {
                            Variable v1 = eevs.nextElement();
                            try { s2 = s2.set(v1,val);  } catch (State.StateLookup e) {
                                Utils.showErrorAlert("StateLookup m10answer var "+v1+"; state "+s+"; assignments "+commands);
                                System.exit(1);
                            } 
                        }
                        // generated s2 from s1 for var
                        newstates.add(s2);
                    }
                   // multiplied s1
                }
                // done all the states
                states = newstates;
            }
        }
        
        // we now have all the final states. Add them to make an answer
        return new Answer(states, new BiModel(AssignModel.Equality, sm));
    }

    // M11 is swap
    static AssignMechanism swapModel = 
        new AssignMechanism(AssignModel.Swap, AssignDirection.Both) { // M11: swap
        public ValuePair assign(Value v1, Value v2) {
            return new ValuePair(v2, v1);
        }
    };

    static AssignMechanism[] assignModelsPlusSwap = 
        Utils.concatArrays(assignMechanisms, new AssignMechanism[]{swapModel}); 
    
    static State doSingleStateAssign(State s, Assign a, AssignMechanism m) {
        try {
            ValuePair result = m.assign(s.get(a.left), s.get(a.right));
            return s.set(a.left, result.left).set(a.right, result.right);
        }
        catch (State.StateLookup e) {
            Utils.showErrorAlert("StateLookup doSingleAssign state "+s+"; assignment "+a+"; model+"+m);
            System.exit(1);
            return s; /* oh dear it demands this */
        }
    }

    static VarVal doDestinationOnlyAssign(State s, Assign a, AssignMechanism m, AssignDirection dir) {
        try {
            ValuePair result = m.assign(s.get(a.left), s.get(a.right));
            if (dir==AssignDirection.Left)
                return new VarVal(a.left, result.left); 
            else
            if (dir==AssignDirection.Right)
                return new VarVal(a.right, result.right);
            else {
                Utils.showErrorAlert("doDestinationOnlySingleStateAssign sees destination "+m.direction);
                return new VarVal(a.left, result.right);        // to be wrong
            }
        }
        catch (State.StateLookup e) {
            Utils.showErrorAlert("StateLookup doStateToStateAssign state s="+s+"; " +
            		"; assignment "+a+"; model+"+m);
            System.exit(1);
            return null;
        }
    }

    static Answer destinationOnlyAssigns(State s, Assign[] assigns, AssignMechanism m, AssignDirection dest) {
        SimpleSet<VarVal> vvs = new SimpleSet<VarVal>();
        for (Assign a: assigns)
            vvs.add(doDestinationOnlyAssign(s, a, m, dest));
        for (Iterator<VarVal> vvsItr = vvs.iterator(); vvsItr.hasNext(); ) {
            VarVal vv = vvsItr.next(); 
            try { s=s.set(vv); } catch (State.StateLookup e) {            
                Utils.showErrorAlert("StateLookup destinationOnlyAssigns vv="+vv+"; state s="+s);
                System.exit(1);
                return null;
            }
        }

        return new Answer(s,new BiModel(m.model, SequenceModel.ParallelDestinationOnly));
    }

    static boolean uniqueAssignDestination(AssignDirection dest, Assign[] assigns) {
        for (Assign a : assigns) {
            Variable v = dest==AssignDirection.Left ? a.left : a.right;
            for (Assign b: assigns)
                if (a!=b && v==(dest==AssignDirection.Left ? b.left : b.right))
                    return false;
        }
        return true;
    }
    
    static State doSequenceAssign(State s, Assign[] assigns, AssignMechanism m) {
        for (int i=0; i<assigns.length; i++)
            s = doSingleStateAssign(s, assigns[i], m);
        return s;
    }

    static AnswerPage elaborate(TestQuestion question) {
        AnswerPage answers = new AnswerPage();

        // the sequence (S1) model
        {       SequenceModel mm = question.commands.length==1 ? SequenceModel.Isolated : SequenceModel.Sequence;
                for (AssignMechanism am: assignMechanisms) 
                    answers.add(doSequenceAssign(question.state, question.commands, am), 
                            new BiModel(am.model, mm));
                answers.add(NoChangeAnswer(question.state));
                answers.add(EqualityAnswer(question.state, question.commands, SequenceModel.Isolated));
                answers.add(doSequenceAssign(question.state, question.commands, swapModel), new BiModel(swapModel.model, mm));
        }

        if (question.commands.length>1) {
            // parallel, multiple (S2)
            for (AssignMechanism m: assignModelsPlusSwap) {
                BiModel mm = new BiModel(m.model, SequenceModel.ParallelMultiple);
                SimpleSet<State> ticks = new SimpleSet<State>();
                for (int i=0; i<question.commands.length; i++)
                    ticks.add(doSingleStateAssign(question.state, question.commands[i], m));
                answers.add(new Answer(ticks, mm));
            }
            
            // still in S2: this didn't appear in earlier versions. Doesn't add any choices, amazingly.
            for (Assign command : question.commands)
                answers.add(EqualityAnswer(question.state, new Assign[]{command}, SequenceModel.ParallelMultiple)); 
                // I think this is valid: parallel, multiple

            // parallel, destination only (S3)
            // check that each name is assigned at most once on its destination side
            for (AssignMechanism m: assignMechanisms) {
                if (uniqueAssignDestination(m.direction, question.commands)) 
                    answers.add(destinationOnlyAssigns(question.state, question.commands, m, m.direction));
            }
            if (uniqueAssignDestination(AssignDirection.Left, question.commands))
                answers.add(destinationOnlyAssigns(question.state, question.commands, swapModel, AssignDirection.Left));
            if (uniqueAssignDestination(AssignDirection.Right, question.commands))
                answers.add(destinationOnlyAssigns(question.state, question.commands, swapModel, AssignDirection.Right));
        }
        
        return answers;
    }

    public static final Frame frame = new Frame();
    
    static String[] getColumns(BufferedReader datain, int ncols, int rownum) throws IOException { 
        String line = datain.readLine();
        if (line==null)
            return null;
        else 
            line = line.concat(","); // thus do we banish the parsing demon
        String[] row = new String[ncols];
        int si = 0;
        // check line 1
        for (int i = 0; i<ncols; i++) {
            if (si==line.length()) {
                Utils.showErrorAlert("only "+(i==1 ? "one column " : Integer.toString(i)+" columns ")+" on row "+rownum+": "+ncols+" expected"); 
                return null;
            }
            if (line.charAt(si)=='"') {
                int sj = line.indexOf('"', si+1);
                row[i] = line.substring(si+1, sj);
                si = sj+1;
                if (si>=line.length() || line.charAt(si)!=',') {
                    Utils.showErrorAlert("no comma after \""+row[i]+"\" in row "+rownum); 
                    return null;
                }
                si = si+1;
            }
            else {
                int sj = line.indexOf(',', si);
                row[i] = line.substring(si, sj);
                si = sj+1;
            }
        }
        if (si!=line.length()) {
            Utils.showErrorAlert("Row "+rownum+" too long: we still have "+line.substring(si, line.length())); 
            return null;
        }
        return row;
    }
    
    static String stringOfStatePairs(State s) {
        SeparatedStringBuilder ssb = new SeparatedStringBuilder(" ");

        for (VarVal vv : s.pairs) {
            ssb.append(vv.toString());
        }
        return ssb.toString();
    }
    
    // strip leading and trailing spaces (and tabs) from an admin header 
    // this is inefficient, but for goodness sake who cares?
    static String getAdminHeader(String s, int lineNumber) {
        if (s==null || s.equals(""))
            Utils.fail("null header on line"+lineNumber);

        if (s.startsWith(" ") || s.startsWith("\t"))
            return getAdminHeader(s.substring(1), lineNumber);
        else
        if (s.endsWith(" ") || s.endsWith("\t"))
            return getAdminHeader(s.substring(0, s.length()-1), lineNumber);
        else
            return s;
    }
    
    final static int questionnaireIDlength = 8;
    
    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                                
                Utils.showInfoAlert(null, "Dehnadi test generator / analyser v"+version);
                Generator.ActionType actionType =
                    Generator.ActionType.utils.showOptionDialog("Action", 
                        "Generate a new questionnaire, or Mark the results of a questionnaire?", Generator.ActionType.Mark);
                if (actionType==null)
                    Utils.fail("neither Generate nor Mark chosen: generator / analyser quits");
                
                switch (actionType) {
                    case Generate:
                       generate();
                       Utils.showInfoAlert("questionnaire generated successfully");
                       break;
                        
                    case Mark:
                        mark();
                        Utils.showInfoAlert("responses analysed successfully");
                       break;
                        
                    default:
                        Utils.crash("missing case "+actionType.name()+" in Generator.main");
                        break;
                }
                
                System.exit(0);
            }
       });
    }
    
    static void generate() {
        
        Utils.showInfoAlert("Generating a questionnaire", 
                "To generate a questionnaire, the generator needs:" +
             "\n" +
             "\n    - a questionnaire description, which gives the personal information questions," +
             "\n        and says where to put the test questions amongst them;" +
             "\n    - a test description, essentially a list of assignment and sequence questions;" +
             "\n    - to know what kind of questionnaire to generate from these descriptions" +
             "\n        (currently Paper, LimeSurvey or SurveyMonkey)" +
             "\n" +
             "\nThere are standard versions of the questionnaire and test description, or you can " +
             "create your own.");
         
        FBufferedReader questionnairein = 
             Utils.openRead("questionnaire description", "questionnairedescription_generate_directory", 
                     "questionnairedescription_generate_filename", "StandardQuestionnaire.txt");
         if (questionnairein==null)
             Utils.fail("no questionnaire description provided: Generator quits");
         
         FBufferedReader testin = 
             Utils.openRead("test description", "testdescription_generate_directory", 
                     "testdescription_generate_filename", "StandardTest.txt");
         if (testin==null)
             Utils.fail("no questionnaire description provided: Generator quits");
         
         
         Questionnaire.QuestionnaireType questionnaireType = 
             Questionnaire.QuestionnaireType.utils.showOptionDialog(
                     "Questionnaire type", 
                     "What kind of questionnaire?", 
                     Questionnaire.QuestionnaireType.LimeSurvey);
         if (questionnaireType==null)
             Utils.fail("no questionnaire type specified: generator quits");
         
         Questionnaire questionnaire = new Questionnaire(questionnairein, testin, true, questionnaireType);

         // Test test = questionnaire.test;
         // TestQuestion[] questions = test.progQuestions;
         // AnswerPage[] answerPages = test.answerPages;
         File targetDirFile = null;
        
         String questionnaireID = RandomStringUtils.randomAlphanumeric(questionnaireIDlength);
         try {
             switch (questionnaireType) {
                 case Paper:
                     MaybeFileWriter 
                     questionsout = 
                     Utils.openWrite(
                             "TeX question sheet", "Questionsheetdirectory", 
                             "", "questionnaire_"+questionnaireID+".tex"),
                             answersout = 
                             Utils.openWrite(
                                     "answer sheet", "Answersheetdirectory", 
                                     "", "answersheet_"+questionnaireID+".tex"),
                                     csvout = 
                                     Utils.openWrite(
                                             "csv responses file", "csvresponsesdirectory", 
                                             "", "responses_"+questionnaireType.name()+"_"+questionnaireID+".csv"); 
                     PaperQuestionnaire paperQuestionnaire = new PaperQuestionnaire(questionnaire, questionsout, answersout, csvout);
                     paperQuestionnaire.outputLaTeXandHeaders();
                     questionsout.close(); 
                     answersout.close();
                     csvout.close();
                     targetDirFile = csvout.file; 
                     break;

                 case LimeSurvey: 
                     try {
                         DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                         DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                         Document doc = docBuilder.newDocument();
                         LimeSurveyQuestionnaire lsQuestionnaire = new LimeSurveyQuestionnaire(doc, questionnaire);

                         File lssout = Utils.askFile(FileDialog.SAVE, 
                                 "LimeSurvey questionnaire", "LimeSurveyDirectory", 
                                 "", "questionnaire_"+questionnaireID+".lss");

                         if (lssout!=null) {
                             Prefs.putProp("LimeSurveyDirectory", lssout.getParent());
                             Prefs.putProp("LimeSurveyFile", lssout.getName());
                             lsQuestionnaire.writeToFile(lssout);
                         }

                         targetDirFile = lssout;   

                     } catch(ParserConfigurationException pce) {
                         System.err.println("Crash: (Configuration Exception)");
                         pce.printStackTrace();
                         Utils.crash("ConfigurationException "+pce+" setting up LimeSurvey XML document");
                     }
                     break;

                 case SurveyMonkey: 
                     MaybeFileWriter monkeyout = 
                     Utils.openWrite(
                             "Text to paste into SurveyMonkey", "Questionnairedirectory", 
                             "", "questionnaire_"+questionnaireID+".txt");
                     SurveyMonkeyQuestionnaire smQuestionnaire = new SurveyMonkeyQuestionnaire(questionnaire, monkeyout);
                     smQuestionnaire.processQuestionnaire(); 
                     monkeyout.close();
                     targetDirFile = monkeyout.file; 
                     break;

                 default:
                     Utils.crash("missing case "+questionnaireType.name()+" in Generator.generate");
             }
             if (targetDirFile!=null) {
                 File qdcopy = new File(targetDirFile.getParentFile(), "QD_"+questionnaireType.name()+"_"+questionnaireID+".txt");
                 File tdcopy = new File(targetDirFile.getParentFile(), "TD_"+questionnaireType.name()+"_"+questionnaireID+".txt");
                 copyFile(questionnaire.qrein.file, qdcopy);
                 copyFile(testin.file, tdcopy);
             }
         } catch (IOException e) {
             Utils.showErrorAlert("Error whilst generating questionnaire"+TextUtils.LineSep+TextUtils.LineSep+e);
         }

    }
    
    /* I used to use org.apache.commons.io.FileUtils.copyFile, but that doesn't work with Ant: 
     * for some reason the class isn't in the jar. But other things seem to be (also from commons),
     * so I'm confused. After experiments with one-jar (which worked, but gave me a multi-megabyte
     * jar file), I decided to write my own copyFile.
     * 
     * Not whiteroom, but as white as I can make it.
     */
    
    static void copyFile(File source, File copy) throws IOException {
        FileInputStream in = new FileInputStream(source);
        FileOutputStream out = new FileOutputStream(copy);
        
        int count;
        byte buffer[] = new byte[4096]; // big enough for my text files
        while ((count = in.read(buffer))!=-1) {
            out.write(buffer,0,count);
        }
        in.close();
        out.close();
    }
    enum FileOptions {TryAgain, TakeIt, StartAgain, Quit;
        @Override
        public String toString() {
            switch (this) {
                case TryAgain   : return "Try Again";
                case TakeIt     : return "Take It";
                case StartAgain : return "Start Again";
                default         : return super.toString();
            }
        }
        static EnumUtils<FileOptions> utils = new EnumUtils<FileOptions>(FileOptions.class);
    }
     
    
    // enums in showOptionDialog come out in reverse order
    static enum ActionType { Mark, Generate;
        static EnumUtils<ActionType> utils = new EnumUtils<ActionType>(ActionType.class);
    }


    static FBufferedReader findMarkDescriptionFile(String title, String directoryprefid, String fName) {
        FBufferedReader datain = 
                Utils.openRead(title, directoryprefid, "", fName);
        if (datain==null)
            Utils.fail("no "+title+" provided: generator/analyser quits");
        
        if (!datain.file.getName().equals(fName)) {
            FileOptions option = FileOptions.utils.showOptionDialog(
                    "wrong file?",
                    "expected "+fName+"; chose "+datain.file.getName(),
                    FileOptions.TryAgain);
            if (option==null)
                option=FileOptions.Quit;
            switch (option) {
                case TryAgain   : return findMarkDescriptionFile(title, directoryprefid, fName);
                case TakeIt     : return datain;
                case Quit       : System.exit(0); return null;
                case StartAgain : mark(); System.exit(0); 
                                  return null;
                default         : Utils.crash("missing case "+option+" in Generator.findNamedDescriptionFile");
                                  return null;
            }
        }
        else
            return datain;
    }
    
    static void mark() {
        Utils.showInfoAlert("Marking a questionnaire", 
                "To analyse the responses to a questionnaire, the analyser needs:" +
             "\n" +
             "\n    - the responses file, a spreadsheet in `csv' form (text, one line per spreadsheet row," +
             "\n        with entries separated by commas), which was either generated by the online" +
             "\n        LimeSurvey/SurveyMonkey tool, or filled in by you if it was a paper questionnaire;" +
             "\n    - the questionnaire description used to generate the questionnaire (in a file called" +
             "\n        QD_<questionnairetype>_<questionnaireid>.txt, created when the questionnaire" +
             "\n        was generated);" +
             "\n    - the test description used to generate the questionnaire (in a file called" +
             "\n        TD_<questionnairetype>_<questionnaireid>.txt, also created when the " +
             "\n        questionnaire was generated)." +
             "\n" +
             "\nFrom the names of the files that you choose, the analyser can tell what kind of survey you" +
             "\nwant to mark, and how to read the responses file.");
        
        /*  +
             "\n" +
             "\nThe QD and TD filenames must match -- have the same type and id -- and if you are analysing " +
             "\na Paper questionaire, the responses filename must be " +
             "\n" +
             "\n       responses_<questionnairetype>_<questionnaireid>.csv" +
             "\n" +
             "\nIf you are analysing a LimeSurvey or SurveyMonkey questionnaire, the header row in the responses " +
             "\nfile must match the questionnaire description. " +
             "\n" +
             "\nIf the three files don't match properly, the analyser will tell you about it, but it may not then " +
             "\nbe able to process the marks." */
        
        FBufferedReader datain = 
            Utils.openRead(
                    "response data", "Responsedata_analysis_directory", 
                    "Responsedata_analysis_filename", "responses.csv");
        if (datain==null)
            Utils.fail("No response data file provided: analyser quits");
        
        FBufferedReader questionnairein, testin;
        String fType=null, fId=null;
        
        Matcher rfPaperMatcher = Pattern.compile("responses_Paper_(\\w{"+questionnaireIDlength+"})\\.csv")
                .matcher(datain.file.getName());
        
        if (rfPaperMatcher.matches()) {
            // System.err.println("it is a paper test");
            fType = "Paper"; fId = rfPaperMatcher.group(1);
            String questionnareFName = "QD_Paper_"+fId+".txt",
                    testFName = "TD_Paper_"+fId+".txt";
            
            Utils.showInfoAlert("Paper questionnaire",
                       "It appears you are analysing the responses to a Paper questionnaire. " +
                    "\n" +
                    "\nPlease now choose questionnaire and test descriptions" +
                    "\n" +
                    "\n       "+questionnareFName+
                    "\n       "+testFName);
            
            
            questionnairein = 
                    findMarkDescriptionFile(
                        "questionnaire description", "questionnairedescription_analysis_directory", 
                        "QD_Paper_"+fId+".txt");
            
            testin = 
                    findMarkDescriptionFile(
                        "test description", "testdescription_analysis_directory", 
                        "TD_Paper_"+fId+".txt");
        }
        else { // not obviously a paper questionnaire
            // System.err.println("not apparently a paper test");
            while (true) {
                questionnairein = 
                    Utils.openRead(
                            "questionnaire description", "questionnairedescription_analysis_directory", 
                            "", "QD_<questionnairetype>_<questionnaireid>.txt");
                if (questionnairein==null)
                    Utils.fail("no questionnaire description provided: analyser quits");
                
                // check that the filename gives us actionType and questionnaireID
                Matcher sdMatcher = 
                        Pattern.compile("QD_(Paper|SurveyMonkey|LimeSurvey)_(\\w{"+questionnaireIDlength+"})\\.txt")
                                            .matcher(questionnairein.file.getName());
                if (sdMatcher.matches()) {
                    fType = sdMatcher.group(1); // QuestionnaireType.valueOf(sdMatcher.group(1));
                    fId = sdMatcher.group(2);
                    // "TD_"+sdMatcher.group(1)+"_"+sdMatcher.group(2)+".txt";
                    if (fType.equals("Paper")) {
                        FileOptions option = FileOptions.utils.showOptionDialog(
                                "wrong file?",
                                  "The questionnaire description file you chose, "+questionnairein.file.getName()+"," +
                		"\nlooks like it goes with a Paper survey, but the responses file "+questionnairein.file.getName()+
                		" doesn't. " +
                		"\n" +
                		"\nWould you like to stick with this file, or try again?",
                                FileOptions.TryAgain);
                        if (option==null)
                            option=FileOptions.Quit;
                        switch (option) {
                            case TryAgain   : continue;
                            case TakeIt     : break;
                            case Quit       : System.exit(0); 
                            case StartAgain : mark(); System.exit(0); 
                            default         : Utils.crash("missing case "+option+" in Generator.mark, reading questionnairein");
                        }
                        break;
                    }
                    
                    break;
                }
                else {
                    FileOptions option = FileOptions.utils.showOptionDialog(
                            "wrong file?",
                               "When analysing responses to a questionnaire, the questionnaire description should be in a file named " +
                            "\n" +
                            "\n        QD_<questionnairetype>_<questionnaireID>.txt" +
                            "\n" +
                            "\nwhere <questionnairetype> is the type of questionnaire (Paper, SurveyMonkey, LimeSurvey), " +
                            "\nand <questionnaireid> is the questionnaire identifier. " +
                            "\n\nThe file you chose was named " +
                            "\n" +
                            "\n        "+questionnairein.file.getName(),
                            FileOptions.TryAgain);
                    if (option==null)
                        option=FileOptions.Quit;
                    switch (option) {
                        case TryAgain   : continue;
                        case TakeIt     : break;
                        case Quit       : System.exit(0); 
                        case StartAgain : mark(); System.exit(0); 
                        default         : Utils.crash("missing case "+option+" in Generator.mark, reading questionnairein");
                    }
                    break;
                }
            }

            String tdFileName = fType==null ? "TestDescription.txt" : "TD_"+fType+"_"+fId+".txt";
            while (true) {
                testin = 
                    Utils.openRead("test description", "testdescription_analysis_directory", 
                            "", tdFileName);
                if (testin==null)
                    Utils.fail("no test description provided: generator/analyser quits");
                
                Matcher tdMatcher = 
                        Pattern.compile("TD_(Paper|SurveyMonkey|LimeSurvey)_(\\w{"+questionnaireIDlength+"})\\.txt")
                                            .matcher(questionnairein.file.getName());

                if (fType==null) {
                    if (tdMatcher.matches()) {
                        fType = tdMatcher.group(1); // QuestionnaireType.valueOf(sdMatcher.group(1));
                        fId = tdMatcher.group(2);
                        break;
                    }
                    else {
                        Questionnaire.QuestionnaireType questionnaireType = Questionnaire.QuestionnaireType.utils.showOptionDialog(
                                "questionnaire type",
                                "What kind of questionnaire was it?",
                                Questionnaire.QuestionnaireType.LimeSurvey);
                        if (questionnaireType==null)
                            Utils.fail("no questionnaire type specified: analyser quits");
                        fType = questionnaireType.toString();
                        break;
                    }
                }
                else {
                    if (tdFileName.equals(testin.file.getName())) 
                        break;
                    if (tdMatcher.matches()) { // different surveys? different qids?
                        if (!fType.equals(tdMatcher.group(1))) {
                            Utils.showErrorAlert(
                                      "Your questionnaire description is called " + questionnairein.file.getName() +
                                    "\nbut your test description is called " + testin.file.getName() +
                                    "\n" +
                                    "\nFrom the questionnaire description it looks as if you are marking a " + fType +
                                    "\nquestionnaire; from the test description it looks like " + tdMatcher.group(1) +
                                    "\n" +
                                    "\nPlease try again");
                            continue;
                        }
                        else {
                            Utils.showErrorAlert(
                                    "Your questionnaire description is called " + questionnairein.file.getName() +
                                  "\nbut your test description is called " + testin.file.getName() +
                                  "\n" +
                                  "\nThese files have different questionnaire ids." +
                                  "\n" +
                                  "\nPlease try again");
                            continue;
                        }
                    }
                    else {
                        FileOptions option = FileOptions.utils.showOptionDialog(
                                "wrong file?",
                                "Your questionnaire description is called " + questionnairein.file.getName() +
                                "\nbut your test description is called " + testin.file.getName(),
                                FileOptions.TryAgain);
                        if (option==null)
                            option=FileOptions.Quit;
                        switch (option) {
                            case TryAgain   : continue;
                            case TakeIt     : break;
                            case Quit       : System.exit(0); 
                            case StartAgain : mark(); System.exit(0); 
                            default         : Utils.crash("missing case "+option+" in Generator.mark, reading testin");
                        }
                        break;
                    }
                }
            }
        }

        final String[][] headers;
        final int quStart, quEnd, tiStart;
        final Itemiser itemiser;

        /* Analyse the response file:
         * We expect on the first line admin columns labelled as in headers array 
         *                  (differs between LimeSurvey and SurveyMonkey);
         * then question sequences of N+1 columns per question (N ticks, 1 other)
         * then maybe timing stuff (LimeSurvey and SurveyMonkey differ). 
         * 
         * SurveyMonkey has a second line giving question info
         * 
         * data lines have admin values,  
         * then sequences of N answer columns (non-blank if not ticked) and an any-other-values column.
         */
/*
                    final String[] preHeaders, postHeaders;
                    final int extraCols;
*/
        // QuestionnaireType questionnaireType = readQuestionnaireType();
        Questionnaire.QuestionnaireType questionnaireType = Questionnaire.QuestionnaireType.valueOf(fType);
        if (questionnaireType==null)
            Utils.crash("unrecognised fType "+fType+" in Generator.mark");
        Questionnaire questionnaire = new Questionnaire(questionnairein, testin, false, questionnaireType);
        Test test = questionnaire.test;
        // TestQuestion[] questions = test.progQuestions;
        AnswerPage[] answerPages = test.answerPages;
        
        try {
            switch (questionnaireType) {
                case SurveyMonkey: 
                    SurveyMonkeyQuestionnaire.Headers monkeyHeaders = 
                        new SurveyMonkeyQuestionnaire.Headers(100);
                    /* standard headers */
                    monkeyHeaders.addColumns(new String [] {
                            "RespondentID", "CollectorID", "StartDate", "EndDate", 
                            "IP Address", "Email Address", "First Name", "LastName", 
                            "Custom Data"
                        });
                    /* headers for our questions. */
                    monkeyHeaders.addColumns(questionnaire.questions);
                    // and that's it
                    headers = monkeyHeaders.toArrays();
                    quStart = monkeyHeaders.quStart;
                    quEnd = monkeyHeaders.quEnd;
                    tiStart = quEnd;
                    itemiser = new Itemiser(datain);
    
                    
                    /*// System.out.println(""+headers[0].length+" columns");
                    for (int row=0; row<2; row++) {
                        for (int col=0; col<headers[row].length; col++)
                            System.out.print(headers[row][col]+", ");
                        System.out.println();
                    }*/
                    break;
    
                case LimeSurvey: 
                    LimeSurveyQuestionnaire.Headers limeHeaders = new LimeSurveyQuestionnaire.Headers(100);
                    /* LimeSurvey has some standard headers, but they differ between versions */
                    datain.mark(4000);
                    String[] items = new Itemiser(datain).itemise(500);
                    datain.reset(); // as if we didn't read it
                    if (items==null)
                        Utils.fail("marks file is empty");
                    String[] v2point00headers = 
                            new String[] {"id", "Completed", "Last page", "Start language"};
                    String[] v1point92headers = 
                            new String[] {"id", "Completed", "Last page seen", "Start language", "Token"};
                    if (Utils.isSubSequence(v2point00headers, items)) {
                        limeHeaders.addColumns(v2point00headers);
                        // System.err.println("v2.00 survey");
                    }
                    else
                    if (Utils.isSubSequence(v1point92headers, items)) {
                        limeHeaders.addColumns(v1point92headers);
                        // System.err.println("v1,92 survey");
                    }
                    else
                        Utils.fail("marks file doesn't start with recognised standard headers: \n" +
                                    "expected either " +
                                        TextUtils.interpolateStrings(v2point00headers, "/") + " \n" +
                                    "or " +
                                        TextUtils.interpolateStrings(v1point92headers, "/"));
    
                    /* and then headers for our questions. */
                    limeHeaders.addColumns(questionnaire.questions);
                    /* "auth", 
                            "Name", "Age", "Gender", 
                            "Qualifications [QualA]", "Qualifications [QualG]",  "Qualifications [QualO]",  
                            "Programmer", "Language [Basic]", "Language [C]", "Language [Java]", 
                            "Language [Cplus]", "Language [VB]", "Language [Fort]", "Language [Other]", 
                            "Course", "Previous [C1]", "Previous [C2]", "Previous [C3]", 
                            "PassFail [C1]", "PassFail [C2]", "PassFail [C3]"}; */
                    
                    /* and then the timing stuff */
                    limeHeaders.startTimingHeaders();
                    limeHeaders.addColumn("interviewtime");
                    /* and then timing headers: one per group, one for each question within the group */
                    limeHeaders.addTimingHeaders(questionnaire.questions);
                    /* postHeaders = new String [] {
                            "finished", "interviewtime", 
                            null, "authTime", 
                            null, "NameTime", "AgeTime", "GenderTime", 
                            "QualificationsTime", "ProgrammerTime", "LanguageTime", 
                            "CourseTime", "PreviousTime", "PassFailTime"}; */
    
                    // and that's it
                    headers = new String[][]{limeHeaders.toArray(new String[limeHeaders.size()])};
                    quStart = limeHeaders.quStart;
                    quEnd = limeHeaders.quEnd;
                    tiStart = limeHeaders.tiStart;
                    itemiser = new Itemiser(datain);
                    
                    /* System.out.println(TextUtils.interpolateStrings(headers[0], ", "));
                    System.out.println(""+surveyHeaders.quStart+", "+questionnaireHeaders.quEnd);
                    System.exit(0); */
                    break;
    
                case Paper: 
                    /* Find out what the headers are, and build an itemiser for them */
                    datain.mark(4000);
                    String[] actualHeaders = new Itemiser(datain).itemise(500);
                    datain.reset(); // as if we didn't read it
                    SpreadsheetHeaders unrandomisedHeaders = new SpreadsheetHeaders(actualHeaders.length);
                    unrandomisedHeaders.addColumns(questionnaire.questions);
                    PaperItemiser myItemiser = 
                            new PaperItemiser(datain, actualHeaders, unrandomisedHeaders);
                    headers = new String[][]{actualHeaders};
                    quStart = myItemiser.quStart;
                    quEnd = myItemiser.quEnd;
                    tiStart = actualHeaders.length;
                    itemiser = myItemiser;
                    break;
                    
                default:
                    Utils.crash("missing case "+questionnaireType.name()+" in Generator.mark");
                    headers = null; // shut up compiler
                    quStart = 0;
                    quEnd = 0;
                    tiStart = 0;
                    itemiser = new Itemiser(datain);
            }

            Marker mm = new Marker(test, answerPages, headers, quStart, quEnd, tiStart, questionnaireType);
    
            /*StringBuilder rowdiag = new StringBuilder();
            rowdiag.append("before reading headers: rowidx="+itemiser.rowidx()+"\n");*/
            String [] row1 = mm.getRow(itemiser); // check data integrity
            String dataFileName = datain.file.getName();
            Matcher dfMatcher = Pattern.compile("(.*?)\\.csv").matcher(dataFileName);
            String dataoutPrefix = dfMatcher.matches() ? dfMatcher.group(1) : dataFileName;
            MaybeFileWriter dataout = 
                Utils.openWrite(
                        "marked responses", "Responsedatadirectory", 
                        "", dataoutPrefix+"_analysis.csv");
            String[] titles = new String[test.progQuestions.length];
            for (int i=0; i<test.progQuestions.length; i++)
                titles[i] = "Q"+(i+1);
            
            mm.showMarkedRow(dataout, row1, "judgement", "reason", titles);
    
            /*rowdiag.append("after reading header 1: rowidx="+itemiser.rowidx()+"\n");*/
            for (int rowIdx = 1; rowIdx<headers.length; rowIdx++) {
                String [] row2 = mm.getRow(itemiser); // reads row, checks data integrity
    
                if (row2==null)
                    Utils.crash("The marks file doesn't seem to be data from the questionnaire " +
                    "(line " + (rowIdx+1) +" is empty)");
    
                for (int i=0; i<test.progQuestions.length; i++)
                    titles[i] = "";
    
                mm.showMarkedRow(dataout, row2, "", "", titles);
                /*rowdiag.append("after reading header "+(rowIdx+1)+": rowidx="+itemiser.rowidx()+"\n");*/
            }
    
            MaybeFileWriter diagout = 
                Utils.openWrite( 
                        "detailed diagnosis", "Responsedatadirectory", "", 
                        dataoutPrefix+"_diagnosis.txt");
    
            int adminrows = itemiser.rowidx()+1;
            
            mm.processResponses(itemiser, dataout, diagout);
    
            diagout.write(Integer.toString(itemiser.rowidx()-adminrows)+" responses\n");
            /*diagout.write("\n"+rowdiag.toString()+"\nrowidx="+itemiser.rowidx()+", adminrows="+adminrows);*/
            datain.close();
            dataout.close();
            diagout.close();
        } catch (IOException e) {
            Utils.showErrorAlert("Error whilst reading response data / writing marks file"+TextUtils.LineSep+TextUtils.LineSep+e);
        }
    }
    
    static Questionnaire.QuestionnaireType bad_fType(String fType) {
        Utils.fail("unknown <questionnaire type> "+fType+" in Generator.mark()");
        return Questionnaire.QuestionnaireType.Paper; // shut up compiler
    }

}
