package uk.ac.mdx.RBornat.Saeedgenerator;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Vector;

import org.javatuples.Triplet;

public class Marker {
    final Test qq;
    final AnswerPage[] answerPages;
    final String[][] headers;
    final int quStart, quEnd, tiStart;
    final Questionnaire.QuestionnaireType questionnaireType;
    final int[] cstart;
    final int nCols;
    
    Marker(Test qq, AnswerPage[] answerPages, String [][] headers, 
            int quStart, int quEnd, int tiStart, Questionnaire.QuestionnaireType questionnaireType) {
        this.qq = qq;
        this.answerPages = answerPages;
        this.headers = headers;
        this.quStart = quStart; 
        this.quEnd = quEnd;
        this.tiStart = tiStart;
        this.questionnaireType = questionnaireType;
        
        this.cstart = new int[qq.progQuestions.length+1]; /* +1 because it gives the end position of the 'next' question,
                                                         even when it's the last question
                                                       */
        
        int c = quStart;
        for (int i=0; i<qq.progQuestions.length; i++) {
            cstart[i] = c;
            c += answerPages[i].choices.size()+1;
        }
        if (c!=quEnd)
            Utils.fail("Marker calculated "+(c-quStart)+" question columns; " +
            		"Generator calculated "+(quEnd-quStart)+" "+quStart+" "+quEnd);
        cstart[qq.progQuestions.length] = c;
        
        this.nCols = headers[0].length;
    }

    public String[] getRow(Itemiser itemiser) throws IOException {
        String[] row = itemiser.itemise(nCols);
        int rowidx = itemiser.rowidx();
        
        if (rowidx<headers.length) {
            if (row==null)
                Utils.fail("marks file is empty!");
            
            for (int i=0; i<row.length; i++) { 
                if (!(headers[rowidx][i].equalsIgnoreCase(row[i]) ||
                        (questionnaireType==Questionnaire.QuestionnaireType.SurveyMonkey && i>=quStart && i<=quEnd && 
                                /* SurveyMonkey progQuestion headers: ignore spaces */
                                headers[rowidx][i].replaceAll(" ","").equalsIgnoreCase(row[i].replaceAll(" ",""))) ||
                        (questionnaireType==Questionnaire.QuestionnaireType.LimeSurvey && i>=quEnd && 
                            /* LimeSurvey timing headers */
                            headers[rowidx][i].equals("groupTime") && row[i].startsWith("groupTime"))))
                Utils.failWithTextOutput("The marks file doesn't seem to be data from the test: " +
                        "expected \""+headers[itemiser.rowidx()][i]+"\" in column "+(i+1)+
                        " of row "+Utils.ordinal(rowidx)+" -- found \""+row[i]+"\"");
            }
        
            if (row.length!=nCols)
                Utils.fail("row "+Utils.ordinal(rowidx)+" has "+row.length+" column headers: expected "+nCols);
        }
        else
        if (row!=null && row.length!=nCols)
            Utils.fail("irregular marks file: row "+Utils.ordinal(rowidx)+" has "+row.length+
                    " columns; row 1 had "+nCols+" columns");

        return row;
    }

    SimpleSet<State>[] findTicks(MaybeFileWriter diagout, String[] row) throws IOException {
        @SuppressWarnings("unchecked")
        SimpleSet<State> [] ticks = 
            (SimpleSet<State> [])java.lang.reflect.Array.newInstance(new SimpleSet<State>().getClass(), qq.progQuestions.length);
        for (int qidx = 0; qidx<qq.progQuestions.length; qidx++) {
            AnswerPage ap = answerPages[qidx];
            ticks[qidx] = new SimpleSet<State>();
            for (int c=cstart[qidx]; c<cstart[qidx+1]-1; c++) { // doesn't look at the 'any other' column
                String a = row[c];
                if (!a.equals("")) { 
                    int choice = c-cstart[qidx];
                    ticks[qidx].add(ap.choices.item(choice));
                }
            }
            String other = row[cstart[qidx+1]-1];
            if (!other.equals("")) {
                SimpleSet<State> ss = 
                    Test.parseOtherString(qq.progQuestions[qidx].state, other);
                if (ss==null) {
                    ss = Decoder.showDecoder(PaperQuestionnaire.stringOfOther(qq.progQuestions[qidx]), 
                            other, qq.progQuestions[qidx].state);
                }
                if (ss==null) {
                    diagout.tabwriteln("(didn't decode \""+other+"\")");
                    row[cstart[qidx+1]-1] = ""; // so later passes don't come this way
                }
                else 
                if (ss.size()!=0) {
                    diagout.tabwriteln("(decoded \""+other+"\" as "+ss+")");
                    for (State tick : ss) 
                        ticks[qidx].add(tick);
                }
            }
        }
        return (SimpleSet<State> [])ticks;
    }
    
    // this thing can be given null arguments by the lazy
    void recordMultiModels(TestQuestion q, Answer a, 
                            int[] shortAssignScores,    int[][] shortMultiScores, 
                            int[] multipleAssignScores, int[][] multipleMultiScores, 
                            int[] allAssignScores,      int[][] allMultiScores) {
        if (a!=null) {
            int[] amUsed = new int[AssignModel.values().length]; // Java initialises to zero, I believe
            int assignCount = q.commands.length;
            for (BiModel mm : a.models) {
                int assignIdx = mm.assignModel.ordinal();
                amUsed[assignIdx] = 1;
                if (assignCount==1 && mm.assignModel!=AssignModel.NoChange 
                                   && mm.assignModel!=AssignModel.Equality) { // stripe it across
                    for (SequenceModel sm : SequenceModel.values()) {
                        if (shortMultiScores!=null)
                            shortMultiScores[assignIdx][sm.ordinal()]++;
                        if (allMultiScores!=null)
                            allMultiScores[assignIdx][sm.ordinal()]++;
                    }
                }
                else {
                    if (assignCount<=2 && shortMultiScores!=null)
                        shortMultiScores[assignIdx][mm.sequenceModel.ordinal()]++;
                    if (assignCount>=2 && multipleMultiScores!=null)
                        multipleMultiScores[assignIdx][mm.sequenceModel.ordinal()]++;
                    if (allMultiScores!=null)
                        allMultiScores[assignIdx][mm.sequenceModel.ordinal()]++;
                }
            }
            for (int i=0; i<AssignModel.values().length; i++) {
                if (assignCount<=2 && shortAssignScores!=null)
                    shortAssignScores[i] += amUsed[i];
                if (assignCount>=2 && multipleAssignScores!=null)
                    multipleAssignScores[i] += amUsed[i];
                if (allAssignScores!=null)
                    allAssignScores[i] += amUsed[i];
            }
        }
    }
 
    void recordSeparateMultiModels(SimpleSet<State> ticks, TestQuestion q, AnswerPage ap, 
                                        int[] assignScores, int[][] multiScores) {
        int newAssignScores[] = new int[AssignModel.values().length],
        newMultiScores[][] = new int[AssignModel.values().length][SequenceModel.values().length];
        for (State tick : ticks) {
            Answer a = ap.lookupAnswer(new SimpleSet<State>(tick));
            if (a!=null) {
                for (BiModel mm : a.models) {
                    AssignModel am = mm.assignModel;
                    newAssignScores[am.ordinal()] = 1;
                    if (q.commands.length==1 && mm.assignModel!=AssignModel.NoChange 
                                             && mm.assignModel!=AssignModel.Equality) // stripe it across
                        for (SequenceModel sm : SequenceModel.values())
                            newMultiScores[am.ordinal()][sm.ordinal()] = 1;
                    else
                        newMultiScores[am.ordinal()][mm.sequenceModel.ordinal()] = 1;
                }
            }
        }
        for (AssignModel am : AssignModel.values()) {
            assignScores[am.ordinal()] += newAssignScores[am.ordinal()];
            for (SequenceModel sm : SequenceModel.values())
                multiScores[am.ordinal()][sm.ordinal()] +=
                    newMultiScores[am.ordinal()][sm.ordinal()];
        }
    }
    
    class Assessment {
        final int maxAssign, maxAssignIdx, maxMulti, maxMultiAssignIdx, maxMultiSequenceIdx;
        Assessment(int maxAssign, int maxAssignIdx, 
                   int maxMulti, int maxMultiAssignIdx, int maxMultiSequenceIdx) {
            this.maxAssign=maxAssign;
            this.maxAssignIdx=maxAssignIdx;
            this.maxMulti=maxMulti;
            this.maxMultiAssignIdx=maxMultiAssignIdx;
            this.maxMultiSequenceIdx=maxMultiSequenceIdx;
        }
    }
    
    Assessment assessResponses(int[] assignScores, int[][] multiScores) {
        int maxMulti = -1, maxAssign = -1, // so we always get something ...
        maxMultiAssignIdx = 0, maxMultiSequenceIdx = 0, maxAssignIdx = 0;

        // assess the  scores
        for (AssignModel am : AssignModel.values()) {
            if (assignScores[am.ordinal()]>maxAssign) {
                if (am!=AssignModel.NoChange) {
                    maxAssign = assignScores[am.ordinal()];
                    maxAssignIdx = am.ordinal();
                }
            }
            for (SequenceModel sm : SequenceModel.values()) // not interested in S0
                if (sm!=SequenceModel.Isolated && multiScores[am.ordinal()][sm.ordinal()]>maxMulti) {
                        maxMulti = multiScores[am.ordinal()][sm.ordinal()];
                        maxMultiAssignIdx = am.ordinal();
                        maxMultiSequenceIdx = sm.ordinal();
                }
        }
        return new Assessment(maxAssign, maxAssignIdx, maxMulti, maxMultiAssignIdx, maxMultiSequenceIdx);
    }
    
    String describeModel(int amIdx, int smIdx, int value) {
        AssignModel am = AssignModel.values()[amIdx];
        StringBuilder sb = new StringBuilder();
        sb.append(am.toString());
        sb.append("("); sb.append(am.name()); sb.append(")");
        if (smIdx!=SequenceModel.Isolated.ordinal()) {
            if (smIdx<0)
                sb.append("+(more than one sequence model)");
            else {
                SequenceModel sm = SequenceModel.values()[smIdx];
                sb.append("+"); sb.append(sm.toString());
                sb.append("("); sb.append(sm.name()); sb.append(")");
            }
        }
        sb.append("="); sb.append(Integer.toString(value));
        return sb.toString();
    }
    
    static void showTableTitle(MaybeFileWriter diagout, String title) throws IOException {
        diagout.write("\t\t"); diagout.write(title); 
        for (int i=0; i<5-title.length()/4; i++)
            diagout.tabwrite();
    }
    
    static void showTable(MaybeFileWriter diagout,
                            String title, int[][] multiScores,  int[] assignScores) throws IOException {
        showTableTitle(diagout, title); 
        diagout.writeln();
        for (SequenceModel sm : SequenceModel.values())
            diagout.tabwrite(sm.toString());
        diagout.tabwrite(); diagout.tabwrite("S*");
        diagout.writeln();
        for (AssignModel am : AssignModel.values()) {
            diagout.write(am.toString());
            for (SequenceModel sm : SequenceModel.values())
                diagout.tabwrite(Integer.toString(multiScores[am.ordinal()][sm.ordinal()]));
            diagout.tabwrite(";");
            diagout.tabwrite(Integer.toString(assignScores[am.ordinal()]));
            diagout.writeln();
        }
    }

    static void showTables(MaybeFileWriter diagout,
            String leftTitle, 
            int[][]leftMultiScores,  int[]leftAssignScores, 
            String rightTitle,
            int[][]rightMultiScores, int[]rightAssignScores) throws IOException {
        showTableTitle(diagout, leftTitle); diagout.write("||");
        showTableTitle(diagout, rightTitle); 
        diagout.writeln();
        showTableTitle(diagout, ""); diagout.write("||");
        showTableTitle(diagout, ""); 
        diagout.writeln();
        
        for (SequenceModel sm : SequenceModel.values())
            diagout.tabwrite(sm.toString());
        diagout.tabwrite(); diagout.tabwrite("S*");
        diagout.tabwrite("||");
        for (SequenceModel sm : SequenceModel.values())
            diagout.tabwrite(sm.toString());
        diagout.tabwrite(); diagout.tabwrite("S*");
        diagout.writeln();
        for (AssignModel am : AssignModel.values()) {
            diagout.write(am.toString());
            for (SequenceModel sm : SequenceModel.values())
                diagout.tabwrite(Integer.toString(leftMultiScores[am.ordinal()][sm.ordinal()]));
            diagout.tabwrite(";");
            diagout.tabwrite(Integer.toString(leftAssignScores[am.ordinal()]));
            /* if (separateMinds>1) { */
            diagout.tabwrite("||");
            for (SequenceModel sm : SequenceModel.values())
                diagout.tabwrite(Integer.toString(rightMultiScores[am.ordinal()][sm.ordinal()]));
            diagout.tabwrite(";"); 
            diagout.tabwrite(Integer.toString(rightAssignScores[am.ordinal()]));
            diagout.writeln();
        }
}

    // a threshold function. Two-thirds, rounded up, but at most n-1 so small numbers
    // don't demand perfection
    static int thresholdF(int qCount) {
        return (qCount*2+2)/3;
    }
    
    // this is map lookupAnswer ticks. Oh dear
    static Answer[] findCombinedResponses(SimpleSet<State>[] ticks, AnswerPage[] answerPages, int qlength) {
        Answer[] result = new Answer[qlength];
        for (int qidx = 0; qidx<qlength; qidx++) {
            AnswerPage ap = answerPages[qidx];
            if (ticks[qidx].size()==0)
                result[qidx] = null;
            else 
                result[qidx] = ap.lookupAnswer(ticks[qidx]);
        }
        return result;
    }

    static void processCombinedResponses(MaybeFileWriter diagout, SimpleSet<State>[] ticks, Answer[] answers, 
                                            String[] judgements, int qlength) throws IOException {
        boolean blankLinePending = false;
        for (int qidx = 0; qidx<qlength; qidx++) {
            if (ticks[qidx].size()==0) {
                judgements[qidx]="";
                blankLinePending = true;
            }
            else {
                StringBuilder judgement = new StringBuilder();
                judgement.append(ticks[qidx].toString());
                judgement.append(":");
                if (answers[qidx]==null) {
                    judgement.append("not modelled");
                }
                else                        
                    judgement.append(answers[qidx].models.toString());

                if (blankLinePending) {
                    diagout.writeln(); blankLinePending = false; 
                }
                diagout.tabwrite(Integer.toString(Utils.ordinal(qidx)));
                diagout.write(": ");
                diagout.writeln((judgements[qidx]=judgement.toString()));
            }
        }
    }

    
    Assessment combinedAssessment(SimpleSet<State>[] ticks) {
        int assignScores[] = new int[AssignModel.values().length],
        multiScores[][] = new int[AssignModel.values().length][SequenceModel.values().length];

        for (int qidx=0; qidx<ticks.length; qidx++)
            recordMultiModels(qq.progQuestions[qidx], answerPages[qidx].lookupAnswer(ticks[qidx]),
                                null, null, null, null, assignScores, multiScores);

       return assessResponses(assignScores, multiScores); 
    }
    
    Assessment separateAssessment(SimpleSet<State>[] ticks) {
        int assignScores[] = new int[AssignModel.values().length],
        multiScores[][] = new int[AssignModel.values().length][SequenceModel.values().length];

        for (int qidx=0; qidx<ticks.length; qidx++)
            recordSeparateMultiModels(ticks[qidx], qq.progQuestions[qidx], answerPages[qidx],
                    assignScores, multiScores);

       return assessResponses(assignScores, multiScores); 
    }
    
    // oh for local methods in Java
    Triplet<Integer,Integer,SimpleSet<State>[]> subtractBestModel(Assessment a, SimpleSet<State>[] ticks) {
        SimpleSet<State>[] sticks = Arrays.copyOf(ticks, ticks.length);
        
        // find the start index, end index and count of the best model,
        // and subtract its ticks from sticks
        AssignModel am = AssignModel.values()[a.maxMultiAssignIdx];
        SequenceModel sm = SequenceModel.values()[a.maxMultiSequenceIdx];
        BiModel mm = new BiModel(am, sm);
        BiModel mmSingle = new BiModel(am, SequenceModel.Isolated);
        int start = -1, end = -1;
        
        for (int qidx=0; qidx<qq.progQuestions.length; qidx++) {
            Answer ans = answerPages[qidx].lookupAnswer(sticks[qidx]); 
            if (ans!=null && ans.models.contains(qq.progQuestions[qidx].commands.length==1 ? mmSingle : mm)) {
                if (start<0)
                    start = qidx;                               // locate start
                end = qidx;                                     // locate end
                sticks[qidx] = new SimpleSet<State>();          // subtract it
            }
        }
        
        return Triplet.with(new Integer(start), new Integer(end), sticks);
    }

    Triplet<Integer,Integer,SimpleSet<State>[]> subtractBestStrand(MaybeFileWriter diagout, int step, 
                                                            Assessment a, SimpleSet<State>[] ticks,
                                                            boolean concurrent) throws IOException {
        SimpleSet<State>[] newticks = Arrays.copyOf(ticks, ticks.length);

        AssignModel am = AssignModel.values()[a.maxMultiAssignIdx];
        SequenceModel sm = SequenceModel.values()[a.maxMultiSequenceIdx];
        BiModel mm = new BiModel(am, sm);
        BiModel mmSingle = new BiModel(am, SequenceModel.Isolated);
        int start = -1, end = -1;
        
        diagout.write("separate "); diagout.write(concurrent ? "concurrent" : "sequential");
        diagout.write(" strand "); diagout.write(Integer.toString(step)); 
        diagout.write(": "); diagout.write(mm.toString()); diagout.write(TextUtils.LineSep);

        for (int qidx=0; qidx<ticks.length; qidx++) {
            newticks[qidx] = new SimpleSet<State>();
            for (State tick : ticks[qidx]) {
                Answer ans = answerPages[qidx].lookupAnswer(tick);
                int comlength = qq.progQuestions[qidx].commands.length;
                boolean keep = ans==null || !ans.models.contains(concurrent || comlength!=1 ? mm : mmSingle);

                if (keep)
                    newticks[qidx].add(tick); // keep this tick

                if (ans!=null && ans.models.contains(comlength==1 ? mmSingle : mm)) {
                    if (start<0)
                        start = qidx;                               // locate start
                    end = qidx;                                     // locate end
                    diagout.write("\t"); 
                    if (keep)
                        diagout.write("(");

                    diagout.write(Integer.toString(Utils.ordinal(qidx))); diagout.write(": ");
                    diagout.write(tick.toString()); diagout.write(":");
                    diagout.write(ans.models.toString());
                    if (keep)
                        diagout.write(")"); 
                    diagout.write(TextUtils.LineSep);
                }
            }
        }
        diagout.write(TextUtils.LineSep);
        return Triplet.with(new Integer(start), new Integer(end), newticks);
    }
    
    String checkAndWarn(SimpleMap<Answer,Integer[]> answerReps, int answerCount, BiModel mm, QuestionType qtype, 
                            String id) {
        for (Entry<Answer, Integer[]> e : answerReps.entrySet()) {
            Answer a = e.getKey();
            if (a.models.contains(mm)) {
                Integer[] es = e.getValue();
                int qCount = 0;
                switch (qtype) {
                    case ALL:
                        qCount = es.length;
                        break;
                        
                    case MULTIPLE:
                        for (int aidx=0; aidx<es.length; aidx++)
                            if (qq.progQuestions[es[aidx].intValue()].commands.length>1)
                                qCount++;
                        break;
                       
                    case SHORT:
                        for (int aidx=0; aidx<es.length; aidx++)
                            if (qq.progQuestions[es[aidx].intValue()].commands.length==1)
                                qCount++;
                        break;
                        
                    default:
                        Utils.crash("Marker.checkAndWarn overlooked "+qtype.name());
                }
                if (qCount>=answerCount/2)
                return Integer.toString(qCount)+" of "+(id==null ? mm.toString() : id)+" answers were repeated";
            }
        }
        return null;
    }
    
    String checkAndWarnSeparate(SimpleSet<State>[] ticks, int answerCount, BiModel mm) {
        // check for same-answer questionnaire bias
        int qlength = qq.progQuestions.length;
        
        SimpleMap<Answer,Integer[]> answerReps = new SimpleMap<Answer,Integer[]>();
        for (int qidx=0; qidx<qlength; qidx++) 
            for (State tick : ticks[qidx]) {
                Answer a = answerPages[qidx].lookupAnswer(tick);
                if (a!=null && !answerReps.containsKey(a)) {
                    Vector<Integer> reps = new Vector<Integer>();
                    for (int repidx=qidx; repidx<qlength; repidx++) {
                        for (State reptick : ticks[repidx]) {
                            if (a.equals(answerPages[repidx].lookupAnswer(reptick))) {
                                reps.add(new Integer(repidx));
                                break; // don't get two of same repidx
                            }
                        }
                    }
                    if (reps.size()>1) {
                        answerReps.put(a, reps.toArray(new Integer[reps.size()]));
                    }
                }
        }
        
       for (Entry<Answer, Integer[]> e : answerReps.entrySet()) {
            Answer a = e.getKey();
            if (a.models.contains(mm) && e.getValue().length>=answerCount/2)
                    return Integer.toString(e.getValue().length)+" of "+mm.toString()+" answers were repeated";
        }
        return null;
}


    
    /* 
     * Make a judgement. 
     * 
     * From Saeed's thesis:
     * 
     * Some answers in Q4-Q12 are ambiguous ... 
     *  We want to maximize judgment of consistency: put pencil ticks in each of the relevant columns. 
     *  Then, when all the questions are marked, look for the column with the most ticks; 
     *  and ink pencilled ticks in that column
     *  
     * 1. A response with six inked ticks in the same column 
     *    for Q1-Q6 (single and double assignment) is judged consistent.
     *    
     * 2. Otherwise, a response with 8 or more inked ticks in the same column is judged consistent.
     * 
     * 3. Otherwise, a response with fewer than 8 inked ticks in total (two-thirds of questions) is judged blank.
     * 
     * 4. Otherwise, the response is judged inconsistent.
     * 
     * -----------------------------------------
     * 
     * What the program does is this:
     * 
     * 1. It sums each model used in an assignmodel x sequencemodel matrix, 
     *    plus a vector of assignmodels. In each question an assignmodel can only
     *    score once in the vector.
     * 
     * 2. The first three questions don't have a sequence model, so answers score
     *    once for each sequencemodel (and once in the assignmodel vector).
     *    
     * 3. It does all this for all the single- or double-assignment questions, 
     *    and separately for longer questions: the first is the 'short' matrix/vector,
     *    and shortCount is the number of those questions; the other is the 'long' 
     *    matrix/vector and longCount is the number of those questions.
     * 
     * 4. It uses Algorithmic/Unrecognised rather than Consistent/Inconsistent.
     * 
     * 5. If any position in the short+long matrices scores (shortCount+longCount) * 2/3 
     *    then Algorithmic, else
     * 
     * 6. If any position in the short matrix scores shortCount then Algorithmic, else
     * 
     * 6a. Here we can spot consecutive sequences (see below).
     * 
     * 7. If any position in the short+long vectors scores (shortCount+longCount) * 2/3 
     *    then Possibly algorithmic, else
     * 
     * 8. If any position in the short vector scores shortCount then Possibly algorithmic, else
     * 
     * 8a. Here we can spot concurrent models (see below).
     * 
     * 9. If the number of answers is fewer than (shortCount+longCount) * 2/3 then Blank, else
     * 
     * 10. Unrecognised.
     * 
     */

    /* 
     * Consecutive sequences:
     * 
     * At Trinity High School, Redditch, Saeed interviewed one person who seemed to change horses
     * in mid-stream.
     * 
     * So if we can't judge somebody conventionally Algorithmic, we look to see if we can split
     * their answers into two halves, each at least one-third of the questionnaire, and 
     * spot one model in the first half, another in the second. If so, that is judged Algorithmic.
     * (probably should be less confident in that judgement).
     * 
     */
    
    /* Concurrent models:
     * 
     * At Trinity High School, Redditch, Richard interviewed one person who seemed to be in two
     * minds how to answer, and had given two answers to each multi-assignment question.
     * 
     * So if we can't judge somebody Possibly algorithmic, we count up all the models we see if
     * we take their answers not as a set of ticks but as separate ticks, and see if we can 
     * spot a high-scoring model in there. 
     * 
     * Once it has spotted one, it subtracts those ticks and tries again. If it finds another 
     * model, and the max number of ticks per question is 2, then bingo. If there are max 3 ticks
     * per question, try again.
     * 
     * It's rare, but it happens.
     * 
     */
    
    enum QuestionType {SHORT, MULTIPLE, ALL};

    void processResponses(Itemiser itemiser, MaybeFileWriter dataout, MaybeFileWriter diagout) throws IOException {
        String[] row;
        final int qlength = qq.progQuestions.length, amCount = AssignModel.values().length, 
        smCount = SequenceModel.values().length;

        String [] judgements = new String[qlength] /*, separateJudgements = new String[qlength] */;
        while ((row = getRow(itemiser))!=null) {

            int answerCount = 0;
            int shortAssignScores[] = new int[amCount], 
                multipleAssignScores[] = new int[amCount], 
                allAssignScores[] = new int[amCount],
                shortMultiScores[][] = new int[amCount][smCount], 
                multipleMultiScores[][] = new int[amCount][smCount], 
                allMultiScores[][] = new int[amCount][smCount];
            /* int notModelled = 0; */
            diagout.writeln(row[0]+": ");
            SimpleSet<State> [] ticks = findTicks(diagout, row); // analyse the data, decoding where necessary

            Answer[] answers = findCombinedResponses(ticks, answerPages, qlength); // sort out the answers
            processCombinedResponses(diagout, ticks, answers, judgements, qlength); 
            // show the ticks and answers, and fill in the judgements
            diagout.writeln(); diagout.writeln();

            // now fill in the tables
            for (int qidx = 0; qidx<qlength; qidx++)
                if (ticks[qidx].size()>0) {
                    answerCount++;
                    recordMultiModels(qq.progQuestions[qidx], answers[qidx], 
                            shortAssignScores, shortMultiScores, 
                            multipleAssignScores, multipleMultiScores, 
                            allAssignScores, allMultiScores);
                }

            // and print them out
            showTables(diagout, "Short", shortMultiScores, shortAssignScores, "All", allMultiScores, allAssignScores);
            diagout.writeln();

            Assessment shortAssessment = assessResponses(shortAssignScores, shortMultiScores),
                                         multipleAssessment = assessResponses(multipleAssignScores, multipleMultiScores),
                                         allAssessment = assessResponses(allAssignScores, allMultiScores);
            final int threshold = thresholdF(qq.progQuestions.length); // 8 out of 12 in Saeed's formulation

            // check for same-answer questionnaire bias
            SimpleMap<Answer,Integer[]> answerReps = new SimpleMap<Answer,Integer[]>();
            for (int qidx=0; qidx<qlength; qidx++) {
                Answer a = answerPages[qidx].lookupAnswer(ticks[qidx]);
                if (a!=null && !answerReps.containsKey(a)) {
                    Vector<Integer> reps = new Vector<Integer>();
                    for (int repidx=qidx; repidx<qlength; repidx++) {
                        if (a.equals(answerPages[repidx].lookupAnswer(ticks[repidx])))
                            reps.add(new Integer(repidx));
                    }
                    if (reps.size()>1) {
                        answerReps.put(a, reps.toArray(new Integer[reps.size()]));
                    }
                }
            }
            
            String judgement, reason, warning;
            
            if (allAssessment.maxMulti>=threshold) {
                judgement = "Algorithmic (overall)";
                reason = describeModel(allAssessment.maxMultiAssignIdx,allAssessment.maxMultiSequenceIdx,
                        allAssessment.maxMulti);
                warning = checkAndWarn(answerReps, allAssessment.maxMulti,
                            new BiModel(allAssessment.maxMultiAssignIdx, allAssessment.maxMultiSequenceIdx), 
                            QuestionType.ALL, "those");
            }
            else
            if (shortAssessment.maxMulti==qq.shortCount) {
                judgement = "Algorithmic (first "+qq.shortCount+")";
                reason = describeModel(shortAssessment.maxMultiAssignIdx,shortAssessment.maxMultiSequenceIdx,
                        shortAssessment.maxMulti);
                warning = checkAndWarn(answerReps, shortAssessment.maxMulti,
                            new BiModel(shortAssessment.maxMultiAssignIdx, shortAssessment.maxMultiSequenceIdx),
                            QuestionType.SHORT, "those");
            }
            else
            if (multipleAssessment.maxMulti>=thresholdF(qq.multipleCount)) {
                judgement = "Algorithmic (last "+qq.multipleCount+")";
                reason = describeModel(multipleAssessment.maxMultiAssignIdx,multipleAssessment.maxMultiSequenceIdx,
                        multipleAssessment.maxMulti);
                warning = checkAndWarn(answerReps, multipleAssessment.maxMulti,
                                            new BiModel(multipleAssessment.maxMultiAssignIdx, 
                                                            multipleAssessment.maxMultiSequenceIdx),
                                            QuestionType.MULTIPLE, "those");
            }
            else
            if (allAssignScores[AssignModel.Equality.ordinal()]>=threshold ||
                    allAssignScores[AssignModel.NoChange.ordinal()]>=threshold) {
                // sequentiality, concurrency are irrelevant in these cases
                if (allAssignScores[AssignModel.Equality.ordinal()]>=threshold) {
                    judgement = "Possibly algorithmic (overall)";
                    reason = describeModel(allAssessment.maxAssignIdx, SequenceModel.Isolated.ordinal(), 
                            allAssignScores[AssignModel.Equality.ordinal()]);
                    warning = null;
                }
                else {
                    judgement = "No change";
                    reason = describeModel(AssignModel.NoChange.ordinal(), SequenceModel.Isolated.ordinal(), 
                            allAssignScores[AssignModel.NoChange.ordinal()]);
                    warning = null;
                }
            }
            else { // count 'wide' models (those which have ticked a lot of choices)
                int width = 0, wideCount = 0;
                for (int qidx = 0; qidx<qlength; qidx++) {
                    int w = ticks[qidx].size();
                    if (w>=answerPages[qidx].choices.size()/2)
                        wideCount++;
                    width = Math.max(width, w);
                }

                if (wideCount>=threshold) {
                    judgement = "Ticked everything";
                    reason = "";
                    warning = null;
                }
                else {
                    /*
                     * Look for sequential use of two models
                     * 
                     */

                    String splitJudgement=null, splitReason=null, splitWarning=null;

                    // no need to look if they haven't scored half
                    if (allAssessment.maxMulti>=thresholdF(qlength/2)) {
                        Assessment a1 = allAssessment;
                        Triplet<Integer,Integer,SimpleSet<State>[]> t1 = subtractBestModel(a1, ticks);
                        Assessment a2 = combinedAssessment(t1.getValue2());
                        Triplet<Integer,Integer,SimpleSet<State>[]> t2 = subtractBestModel(a2, t1.getValue2());

                        if (a1.maxMulti+a2.maxMulti>=threshold) {
                            // there is some hope ...
                            // compute the warning while we still know where we are
                            SeparatedStringBuilder sb = new SeparatedStringBuilder(" and ");
                            sb.append(checkAndWarn(answerReps, a1.maxMulti, 
                                                    new BiModel(a1.maxMultiAssignIdx, a1.maxMultiSequenceIdx),
                                                    QuestionType.ALL, null));
                            sb.append(checkAndWarn(answerReps, a2.maxMulti, 
                                    new BiModel(a2.maxMultiAssignIdx, a2.maxMultiSequenceIdx),
                                    QuestionType.ALL, null));
                            splitWarning = sb.toString();

                            if (t1.getValue0().intValue()>t2.getValue0().intValue()) {
                                // swap them round, sigh. Oh for Java swap ...
                                Triplet<Integer,Integer,SimpleSet<State>[]> tmpT = t1;
                                t1 = t2; t2 = tmpT;
                                Assessment tmpA = a1;
                                a1 = a2; a2 = tmpA;
                            }

                            if (t1.getValue1().intValue()-t2.getValue0().intValue()<=(qlength+3)/4) { // a bit of overlap
                                // blimey they're sequential
                                splitJudgement = "Sequentially algorithmic";
                                splitReason = 
                                    describeModel(a1.maxMultiAssignIdx,a1.maxMultiSequenceIdx,a1.maxMulti) + 
                                    "(Q"+Utils.ordinal(t1.getValue0().intValue()) +
                                    "-Q"+Utils.ordinal(t1.getValue1().intValue())+"); " +
                                    describeModel(a2.maxMultiAssignIdx,a2.maxMultiSequenceIdx,a2.maxMulti) + 
                                    "(Q"+Utils.ordinal(t2.getValue0().intValue())+
                                    "-Q"+Utils.ordinal(t2.getValue1().intValue())+")";
                            }
                        }
                    }

                    /* This is the 'old' way of doing it, iteratively splitting the questions
                     * 
                     */
                    /*
                     * Look for sequential use of two models, splitting the assessment
                     * so that each half is at least 4 questions or one-third of the questionnaire.
                     * 
                     * The threshold for each section is
                     *      4: 4
                     *      5: 4
                     *      6 and above: two-thirds
                     *      -- i.e. (n+2)*2/3;
                     *      
                     * To avoid undermining existing judgements, the models must be different
                     *    (that's probably unnecessary, but it's a trivial test).
                     * 
                     */
                    /*
                        // no need to look if they haven't scored half
                        if (allAssessment.maxMulti>=thresholdF(qlength/2)) {
                            int margin = Math.max(4, qlength/3);
                            for (int split = margin; split<qlength-margin; split++) {
                                int splitAssignScores[][] = new int[2][amCount],
                                    splitMultiScores[][][] = new int[2][amCount][smCount];
                                for (int qidx = 0; qidx<split; qidx++)
                                    recordMultiModels(qq.questions[qidx], answerPages[qidx].lookupAnswer(ticks[qidx]), null, null, 
                                            splitAssignScores[0], splitMultiScores[0]);
                                for (int qidx = split; qidx<qlength; qidx++)
                                    recordMultiModels(qq.questions[qidx], answerPages[qidx].lookupAnswer(ticks[qidx]), null, null, 
                                            splitAssignScores[1], splitMultiScores[1]);
                                Assessment left  = assessResponses(splitAssignScores[0], splitMultiScores[0]),
                                right = assessResponses(splitAssignScores[1], splitMultiScores[1]);
                                int leftThreshold = thresholdF(split), rightThreshold = thresholdF(qlength-split);
            
                                if (left.maxMulti>=leftThreshold && right.maxMulti>=rightThreshold &&
                                        left.maxMultiAssignIdx!=right.maxMultiAssignIdx &&
                                        left.maxMultiSequenceIdx!=right.maxMultiSequenceIdx) {
                                    diagout.writeln();
                                    showTables(diagout, "Q1-Q"+split, 
                                            splitMultiScores[0], splitAssignScores[0], 
                                            "Q"+(split+1)+"-Q"+qlength, 
                                            splitMultiScores[1], splitAssignScores[1]);
                                    diagout.writeln();                        
                                    splitJudgement = "Algorithmic (two consecutive models)";
                                    splitReason = describeModel(left.maxMultiAssignIdx,left.maxMultiSequenceIdx,
                                            left.maxMulti) + " then "+
                                            describeModel(right.maxMultiAssignIdx,right.maxMultiSequenceIdx,
                                                    right.maxMulti);
                                    break;
                                }
                            }
                        }
                     */

                    if (splitJudgement!=null) { // this to shut up Java's "may not be assigned" moan
                        judgement = splitJudgement;
                        reason = splitReason;
                        warning = splitWarning;
                    }
                    else {
                        diagout.writeln("Can't see evidence of two combined models in sequence"); 

                        SimpleSet<State>[] septicks = Arrays.copyOf(ticks, qlength);
                        int sepwidth = 0;

                        // we trace separate strands. This can be confused by people who tick lots
                        // of answers. Crudely, throw away answers wider than 3
                        for (int qidx = 0; qidx<qlength; qidx++) {
                            int cw = septicks[qidx].size();
                            if (cw>3) {
                                diagout.writeln("separation ignores Q"+Utils.ordinal(qidx)+" ("+septicks[qidx].size()+" ticks)");
                                septicks[qidx] = new SimpleSet<State>();
                            }
                            else
                                sepwidth = Math.max(sepwidth, cw);
                        }

                        // check for separate models
                        String consecutiveSeparatedJudgement = null, consecutiveSeparatedReason = null,
                                consecutiveSeparatedWarning = null;

                        if (sepwidth>1) { // we would have seen it otherwise
                            Assessment consecA1 = separateAssessment(septicks);
                            if (consecA1.maxMulti>=3) { // less is ridiculous
                                diagout.writeln();
                                Triplet<Integer,Integer,SimpleSet<State>[]> consecT1 = 
                                    subtractBestStrand(diagout, 1, consecA1, septicks, false);
                                Assessment consecA2 = separateAssessment(consecT1.getValue2());
                                if (consecA2.maxMulti>=3) { // ditto
                                    Triplet<Integer,Integer,SimpleSet<State>[]> consecT2 = 
                                        subtractBestStrand(diagout, 2, consecA2, consecT1.getValue2(), false);

                                    // compute the warning while we still know where we are
                                    SeparatedStringBuilder sb = new SeparatedStringBuilder(" and ");
                                    sb.append(checkAndWarnSeparate(septicks, consecA1.maxMulti, 
                                            new BiModel(consecA1.maxMultiAssignIdx, consecA1.maxMultiSequenceIdx)));
                                    sb.append(checkAndWarnSeparate(consecT1.getValue2(), consecA2.maxMulti, 
                                            new BiModel(consecA2.maxMultiAssignIdx, consecA2.maxMultiSequenceIdx)));
                                    consecutiveSeparatedWarning = sb.toString();

                                    if (consecT1.getValue0().intValue()>consecT2.getValue0().intValue()) {
                                        // swap them round, sigh. Oh for Java swap ...
                                        Triplet<Integer,Integer,SimpleSet<State>[]> tmpT = consecT1;
                                        consecT1 = consecT2;
                                        consecT2 = tmpT;
                                        Assessment tmpA = consecA1;
                                        consecA1 = consecA2;
                                        consecA2 = tmpA;
                                    }

                                    if (consecA1.maxMulti+consecA2.maxMulti>=threshold) {
                                        // there is some hope ...
                                        if (consecT1.getValue1().intValue()-consecT2.getValue0().intValue()<=(qlength+3)/4) { // a bit of overlap
                                            // blimey they're sequential
                                            consecutiveSeparatedJudgement = "Sequentially algorithmic";
                                            consecutiveSeparatedReason = 
                                                describeModel(consecA1.maxMultiAssignIdx,consecA1.maxMultiSequenceIdx,consecA1.maxMulti) + 
                                                "(Q"+Utils.ordinal(consecT1.getValue0().intValue()) +
                                                "-Q"+Utils.ordinal(consecT1.getValue1().intValue())+"); " +
                                                describeModel(consecA2.maxMultiAssignIdx,consecA2.maxMultiSequenceIdx,consecA2.maxMulti) + 
                                                "(Q"+Utils.ordinal(consecT2.getValue0().intValue())+
                                                "-Q"+Utils.ordinal(consecT2.getValue1().intValue())+")";
                                        }
                                    }
                                }
                            }
                        }

                        if (consecutiveSeparatedJudgement!=null) {
                            judgement = consecutiveSeparatedJudgement;
                            reason = consecutiveSeparatedReason;
                            warning = consecutiveSeparatedWarning;
                        }
                        else { 
                            diagout.writeln("not enough evidence for separate-model sequentiality");

                            // look for separated concurrent models
                            String concurrentJudgement = null, concurrentReason = null, concurrentWarning = null;

                            if (sepwidth>1) { // else there is no concurrency
                                Assessment sepA1 = separateAssessment(septicks);
                                if (sepA1.maxMulti>=threshold) { // else don't bother
                                    diagout.writeln();
                                    Triplet<Integer,Integer,SimpleSet<State>[]> sepT1 = 
                                        subtractBestStrand(diagout, 1, sepA1, septicks, true);
                                    Assessment sepA2 = separateAssessment(sepT1.getValue2());
                                    if (sepA2.maxMulti>=threshold) { // else don't bother
                                        /* Triplet<Integer,Integer,SimpleSet<State>[]> sepT2 = 
                                            subtractBestStrand(diagout, 2, sepA2, sepT1.getValue2(), true); */

                                        concurrentJudgement = "Concurrently algorithmic";
                                        concurrentReason = 
                                            describeModel(sepA1.maxMultiAssignIdx,sepA1.maxMultiSequenceIdx,sepA1.maxMulti) + 
                                            "||" +
                                            describeModel(sepA2.maxMultiAssignIdx,sepA2.maxMultiSequenceIdx,sepA2.maxMulti);
                                        // compute the warning while we still know where we are
                                        SeparatedStringBuilder sb = new SeparatedStringBuilder(" and ");
                                        sb.append(checkAndWarnSeparate(septicks, sepA1.maxMulti, 
                                                new BiModel(sepA1.maxMultiAssignIdx, sepA1.maxMultiSequenceIdx)));
                                        sb.append(checkAndWarnSeparate(sepT1.getValue2(), sepA2.maxMulti, 
                                                new BiModel(sepA2.maxMultiAssignIdx, sepA2.maxMultiSequenceIdx)));
                                        concurrentWarning = sb.toString();
                                    }
                                }
                            }

                            if (concurrentJudgement!=null) {
                                judgement = concurrentJudgement;
                                reason = concurrentReason;
                                warning = concurrentWarning;
                            }
                            else {
                                diagout.writeln("not enough evidence for separate-model concurrency");
                                
                                if (allAssessment.maxAssign>=threshold) {
                                    judgement = "Possibly algorithmic (overall)";
                                    reason = describeModel(allAssessment.maxAssignIdx, noModel(allAssessment.maxAssignIdx), 
                                                            allAssessment.maxAssign);
                                    warning = "??";
                                }
                                else 
                                if (shortAssessment.maxAssign==qq.shortCount) {
                                    judgement = "Possibly algorithmic (first "+qq.shortCount+")";
                                    reason = describeModel(shortAssessment.maxAssignIdx, noModel(shortAssessment.maxAssignIdx), 
                                                            shortAssessment.maxAssign);
                                    warning = "??";
                                }
                                else
                                if (multipleAssessment.maxAssign>=thresholdF(qq.multipleCount)) {
                                    judgement = "Possibly algorithmic (last "+qq.multipleCount+")";
                                    reason = describeModel(multipleAssessment.maxAssignIdx, noModel(multipleAssessment.maxAssignIdx), 
                                                                multipleAssessment.maxAssign);
                                    warning = "??";
                                }
                                else
                                if (answerCount>=threshold) {
                                    judgement = "Unrecognised";
                                    reason = allAssessment.maxMulti>=allAssessment.maxAssign ?
                                            describeModel(allAssessment.maxMultiAssignIdx,allAssessment.maxMultiSequenceIdx,
                                                    allAssessment.maxMulti) :
                                            describeModel(allAssessment.maxAssignIdx, noModel(allAssessment.maxAssignIdx), 
                                                    allAssessment.maxAssign);
                                    warning = null;
                                }
                                else {
                                    judgement = "Blank";
                                    reason = Integer.toString(answerCount)+" answers";
                                    warning = null;
                                }
                            }
                        }
                    }
                }
            }

            diagout.writeln(judgement+(reason.equals("") ? "" : (": "+reason)));
            if (warning!=null && !(warning.equals("")))
                diagout.writeln("BUT! "+warning);
            diagout.writeln();
            diagout.writeln("-----------------------------------");
            diagout.writeln();

            showMarkedRow(dataout, row, judgement, reason, judgements);
        }
    }
    
    void showMarkedRow(MaybeFileWriter dataout, String[] responses, 
            String judgement, String reason, String[] judgements) {
        SeparatedStringBuilder csvb = new SeparatedStringBuilder(",");
        try {
            for (int i = 0; i<quStart; i++)
                csvb.append(TextUtils.csvItem(responses[i]));
            for (int i = quEnd; i<tiStart; i++)
                csvb.append(TextUtils.csvItem(responses[i]));
            csvb.append(TextUtils.csvItem(judgement));
            csvb.append(TextUtils.csvItem(reason));
            for (String qjudgement : judgements)
                csvb.append(TextUtils.csvItem(qjudgement));
            for (int i = quStart; i<quEnd; i++) 
                csvb.append(TextUtils.csvItem(responses[i]));
            for (int i = tiStart; i<responses.length; i++) 
                csvb.append(TextUtils.csvItem(responses[i]));
            dataout.write(csvb.toString());
            dataout.write(TextUtils.LineSep);
        } catch (IOException e) {
            Utils.crash("IOException "+e+": couldn't write a row of the marks file");
        }
    }


    int noModel(int assignIdx) {
        return assignIdx==AssignModel.NoChange.ordinal() ||
               assignIdx==AssignModel.Equality.ordinal() ?
                    SequenceModel.Isolated.ordinal() : -1;
    }

}
