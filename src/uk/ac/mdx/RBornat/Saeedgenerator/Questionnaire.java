package uk.ac.mdx.RBornat.Saeedgenerator;

import java.io.IOException;
import java.util.Vector;
import org.javatuples.Pair;

public class Questionnaire {
    final FBufferedReader qrein, testin;
    String title=null, welcome[]=null, goodbye[]=null;
    Test test=null;
    final Question[] questions;
    final boolean checkrepetitions;
    final QuestionnaireType questionnaireType;
    
    Questionnaire(FBufferedReader qrein, FBufferedReader testin, boolean checkrepetitions, 
                      Questionnaire.QuestionnaireType questionnaireType
                 ) {
        this.qrein = qrein;
        this.testin = testin;
        this.checkrepetitions = checkrepetitions;
        this.questionnaireType = questionnaireType;
        
        questions = readQuestions(null);
        if (title==null)
            Utils.fail("questionnaire description has no **Title");
        if (welcome==null && welcomeNeeded())
            Utils.fail("questionnaire description has no **Welcome");
        if (goodbye==null && goodbyeNeeded())
            Utils.fail("questionnaire description has no **Goodbye");
        if (test==null)
            Utils.fail("questionnaire description has no **Test");
    }
    
    boolean welcomeNeeded() { return questionnaireType==QuestionnaireType.LimeSurvey; }
    
    boolean goodbyeNeeded() { return questionnaireType==QuestionnaireType.LimeSurvey; }
    
    
    static void badControl(String line) {
        Utils.fail("bad control line: "+line);
    }
    
    static void badControl(String message, String line) {
        Utils.fail("bad control line ("+message+"): "+line);
    }
    
    static void badControl(String [] items) {
        badControl(TextUtils.interpolateStrings(items, " "));
    }
    
    static void badControl(String message, String [] items) {
        badControl(message, TextUtils.interpolateStrings(items, " "));
    }
    
    Question[] readQuestions(String[] followedBys) {
        Vector<Question> qs = new Vector<Question>(8);
        String line;
        try {
            while ((line=TextUtils.markedReadLine(qrein))!=null) {
                // System.err.println("line is "+line);
                if (TextUtils.dataLine(line))
                    Utils.fail("control line expected (starting with '**'); found " +line);
                String [] items = TextUtils.tokenise(line);
                /*  System.err.print("tokenised as ");
                    for (String item: items)
                        System.err.println(item+" ");
                    System.err.println("");
                 */
                if (items[0].equals("**Title")) {
                    if (title==null)
                        title = TextUtils.stripControlWord(line);
                    else
                        Utils.fail("two **Title lines");
                }
                else
                if (line.equals("**Welcome")) {
                    if (welcome==null) {
                        welcome = properTextBlock("**Welcome");
                        //System.out.println(TextUtils.enLine(welcome));
                        //System.out.println("Welcome is\n\n"+TextUtils.enParaHTML(welcome));
                    }
                    else
                        Utils.fail("two **Welcome sections");
                }
                else
                if (line.equals("**Goodbye")) {
                    if (goodbye==null)
                        goodbye = TextUtils.inputTextBlock(qrein);
                    else
                        Utils.fail("two **Goodbye sections");
                }
                else
                if (items[0].equals("**Authorisation") || items[0].equals("**Authorization")) 
                    qs.add(new AuthQuestion(items));
                else
                if (items[0].equals("**Text")) 
                    qs.add(new TextQuestion(items));
                else 
                if (items[0].equals("**MultiText")) 
                    qs.add(new MultiTextQuestion(items));
                else 
                if (items[0].equals("**Number")) 
                    qs.add(new NumberQuestion(items));
                else 
                if (items[0].equals("**Choice")) 
                    qs.add(new ChoiceQuestion(items));
                else 
                if (items[0].equals("**MultiChoice")) 
                    qs.add(new MultiChoiceQuestion(items));
                else 
                if (items[0].equals("**ArrayChoice")) 
                    qs.add(new ArrayChoiceQuestion(items));
                else
                if (items[0].equals("**Final"))
                    qs.add(new FinalQuestion(items));
                else 
                if (items[0].equals("**If"))
                    qs.add(new ConditionalSection(items));
                else
                if (items[0].equals("**Group")) 
                    qs.add(new GroupStarter(items));
                else 
                if (items[0].equals("**Questionnaire")) {
                    if (test==null) {
                        test = readTest(testin);
                        if (title==null)
                            title = test.title;
                        else
                            title = title+" ("+test.title+")";
                        qs.add(new TestSection(items, test));
                    }
                    else
                        Utils.fail("two **Questionnaire entries");
                }
                else 
                if (Utils.member(items[0], followedBys)) {
                    qrein.reset();
                    break;
                }
                else
                if (Utils.member(items[0], conditionalTerminators))
                    Utils.fail("conditional closing bracket ("+line+" outside conditional");
                else
                    Utils.fail("Questionnaire.readQuestions cannot recognise control line "+line);
                // System.err.println("next line is "+TextUtils.markedReadLine(introin));
                // introin.reset();
            }
        } catch (IOException e) {
            Utils.crash("How can there be an IO error ("+e+") while reading lines of the introductory questions?");
        }
        
        return qs.toArray(new Question[qs.size()]);
    }
    
    String[] properTextBlock(String before) throws IOException {
        String[] block = TextUtils.inputTextBlock(qrein);
        if (block.length==0)
            Utils.fail("text block expected after "+before);
        // System.err.println("properTextBlock sees "+TextUtils.enPara(block));
        // System.err.println("next line is "+TextUtils.markedReadLine(introin));
        // introin.reset();
        return block;
    }
    
    /* this should check for uniqueness of codes */
    @SuppressWarnings("unchecked") // applies to return statement
    Pair<String,String[]>[] readOptions(String[] items, String control, int maxlen) throws IOException {
        Vector<Pair<String,String[]>> optV = new Vector<Pair<String,String[]>>(3);
        String line;
        while (true) {
            line = TextUtils.markedReadLine(qrein);
            if (line.startsWith(control)) {
                String[] tokens = TextUtils.tokenise(line);
                if (tokens.length!=2)
                   badControl(tokens);
                if (maxlen>0 && tokens[1].length()>maxlen)
                    Utils.fail("At present LimeSurvey doesn't allow **Option ids to be\n" +
                    		"more than " + maxlen + " characters: \"" + tokens[1] +
                    		"\" is " + tokens[1].length() +
                    		" characters");
                String[] opt = properTextBlock(tokens[0]);
                for (int i=0; i<optV.size(); i++)
                    if (tokens[1].equals(optV.elementAt(i).getValue0()))
                        Utils.fail("repeated **Option id \"" + tokens[1] + "\"");
                optV.add(Pair.with(tokens[1], opt));
            }
            else {
                if (optV.size()==0)
                    Utils.fail("no "+control+" options follow "+TextUtils.interpolateStrings(items," "));
                qrein.reset(); 
                // must use reflection because Java 'cannot' create the array. Stupid Java.
                return optV.toArray(
                        (Pair<String,String[]>[])java.lang.reflect.Array.newInstance(new Pair<String,String[]>(null,null).getClass(), 
                                                                                     optV.size()));
            }
        }
    }
    
    Pair<String,String[]>[] readOptions(String[] items, int maxlen) throws IOException {
        return readOptions(items, "**Option", maxlen);
    }
    
    /* String[] readItem(String[] items, String control) throws IOException {
        String line = TextUtils.markedReadLine(introin);
        if (line.startsWith(control)) {
            String[] tokens = TextUtils.tokenise(TextUtils.stripControlWord(line));
            if (tokens.length==0)
               badControl(line);
            return tokens;
        }
        else {
            Utils.fail("no "+control+" line after "+TextUtils.interpolateStrings(items," "));
            return null; // shut up compiler
        }
   } */
        
    class Question {
        final String[] items;
        final String id; // "" if not a namable question
        final boolean compulsory;
        Question(String[] items, int length, int idIndex, boolean compulsory) { // which should start with **word(space)
            if ((length==0 && items.length<idIndex) || 
                (length!=0 && items.length!=length))
                badControl(items);
            this.items = items;
            this.id = items[idIndex];
            this.compulsory = compulsory;
        }
        
        Question(String[] items, int length, int idIndex) { // which should start with **word(space)
            this(items, length, idIndex, false);
        }
        
        Question(String[] items, int length, int idIndex, int compulsoryIndex) { // which should start with **word(space)
            if (items.length!=length || !(items[compulsoryIndex].equals("true") || items[compulsoryIndex].equals("false")))
                badControl(items);
            this.items = items;
            this.id = items[idIndex];
            this.compulsory = items[compulsoryIndex].equals("true");
        }
        
        Question(String[] items, String id) {
            this.items = items;
            this.id = id;
            this.compulsory = true;
        }
        
        public String controlString() {
            return items[0];
        }
    }
    
    class AuthQuestion extends Question {
        // an AuthQuestion is some text, option OK, option exit
        final String authText[], OKtext, NotOKtext, NotOKpara[];
        AuthQuestion(String[] items) throws IOException {
            super(items, 2, 1);
            String line;
            authText = properTextBlock(id);
            line = TextUtils.markedReadLine(qrein);
            if (line==null || !line.startsWith("**OK"))
                Utils.fail("Authorisation text must be followed by **OK description: found "+line);
            OKtext = TextUtils.stripControlWord(line);
            if (OKtext.length()==0)
                Utils.fail("In authorisation, OK text is empty");
            line = TextUtils.markedReadLine(qrein);
            if (line==null || !line.startsWith("**NotOK"))
                Utils.fail("Authorisation text, **OK, then there should be **NotOK: found "+line);
            NotOKtext = TextUtils.stripControlWord(line);
            if (NotOKtext.length()==0)
                Utils.fail("In authorisation, NotOK text is empty");
            NotOKpara = properTextBlock("**NotOK");
        }
    }
    
    class GroupStarter extends Question {
        final String groupMessage; 
        GroupStarter(String[] items) throws IOException {
            super(items, items[1]);
            if (items.length<=2)
                badControl(items);
            groupMessage = TextUtils.interpolateStrings(items, 2, " ");
        }
    }

    class TextQuestion extends Question {
        final String questionText[]; 
        TextQuestion(String[] items) throws IOException {
            super(items, 3, 1, 2);
            questionText = properTextBlock(id);
        }
    }

    class MultiTextQuestion extends Question {
        final String[] questionText; 
        final Pair<String, String[]> options[];
        MultiTextQuestion(String[] items) throws IOException {
            super(items, 3, 1, 2);
            questionText = properTextBlock(id);
            options = readOptions(items, 0);
        }
    }

    class NumberQuestion extends Question {
        final String questionText[]; 
        NumberQuestion(String[] items) throws IOException {
            super(items, 3, 1, 2);
            questionText = properTextBlock(id);
        }
    }

    class ChoiceQuestion extends Question {
        final String[] questionText; 
        final int columns;
        final Pair<String, String[]> options[];
        ChoiceQuestion(String[] items) throws IOException {
            super(items, 4, 1, 3);
            questionText = properTextBlock(id);
            options = readOptions(items, 5);
            columns = Integer.parseInt(items[2]);
        }
    }

    class MultiChoiceQuestion extends Question {
        final String[] questionText; 
        final int columns;
        final Pair<String, String[]> options[];
        MultiChoiceQuestion(String[] items) throws IOException {
            super(items, 4, 1, 3);
            questionText = properTextBlock(id);
            options = readOptions(items, 0);
            columns = Integer.parseInt(items[2]);
        }
    }

    class ArrayChoiceQuestion extends Question {
        final String[] questionText; 
        final Pair<String, String[]> vertics[], horizs[];
        ArrayChoiceQuestion(String[] items) throws IOException {
            super(items, 3, 1, 2);
            questionText = properTextBlock(id);
            vertics = readOptions(items, "**Vertical", 5);
            horizs = readOptions(items, "**Horizontal", 5);
        }
    }

    class FinalQuestion extends Question {
        // an AuthQuestion is some text, option OK, option exit
        final String finalText[];
        FinalQuestion(String[] items) throws IOException {
            super(items, 2, 1);
            finalText = properTextBlock(id);
        }
    }
    
    final String[] conditionalTerminators = new String[]{"**Else", "**Elsf", "**Fi" };
    final String[] elseTerminators = new String[]{"**Fi" };

    class ConditionalSection extends Question {
        final Pair<String,String> condition;
        final Question thens[], elses[];
        ConditionalSection(String[] items) throws IOException {
            super(items, "");
            if (items.length!=3)
                badControl(items);
            condition = Pair.with(items[1],items[2 ]);
            thens = readQuestions(conditionalTerminators);
            String command = TextUtils.markedReadLine(qrein);
            if (command!=null && command.startsWith("**Else"))
                elses = readQuestions(elseTerminators);
            else
            if (command!=null && command.startsWith("**Elsf"))
                elses = new Question[] { new ConditionalSection(TextUtils.tokenise(command)) };
            else {
                elses = null; // fool the compiler's 'final' check
                if (command==null)
                    Utils.fail("**Else, **Elsf or **Fi expected to terminate "+
                                TextUtils.interpolateStrings(items, " ")+"; found end-of-file");
                else
                if (!command.startsWith("**Fi"))
                   Utils.fail("**Else, **Elsf or **Fi expected to terminate "+
                           TextUtils.interpolateStrings(items, " ")+"; found "+command);
            }
        }
    }
    
    class TestSection extends Question {
        final Test test;
        TestSection(String[] items, Test test) {
            super(items, "Questionnaire");
            this.test = test;
        }
    }
   
    static enum QuestionnaireType { Paper, SurveyMonkey, LimeSurvey;
        static EnumUtils<QuestionnaireType> utils = new EnumUtils<QuestionnaireType>(QuestionnaireType.class);
    }

    Test readTest(FBufferedReader testin) throws IOException {
        /*
         * We don't check the test Id against the questionnaire any more
        
            String line = TextUtils.markedReadLine(testin);
            if (line==null) 
                Utils.fail("test description is empty");
    
            String [] items = TextUtils.tokenise(line);
            if (!items[0].equals("**Id"))
                Utils.fail("expected **Id line in test description; found " +line);
            
             
            String foundId = TextUtils.stripControlWord(line);
            if (!testId.equals(foundId))
                Utils.fail("questionnaire description specifies test "+testId+";\n" +
                		"test description file contains **Id "+foundId);
            		
         */
        
        return new Test(testin, checkrepetitions);
    }
}
