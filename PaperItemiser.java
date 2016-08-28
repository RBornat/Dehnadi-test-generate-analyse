package uk.ac.mdx.RBornat.Saeedgenerator;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PaperItemiser extends Itemiser {
    
    final int quStart, quEnd;
    final int[] ordering;

    PaperItemiser(BufferedReader datain, String[] actualHeaders, SpreadsheetHeaders unrandomisedHeaders) {
        super(datain);
        
        if (actualHeaders.length!=unrandomisedHeaders.size())
            Utils.fail(
                   "The responses spreadsheet doesn't seem to be data from the questionnaire whose description you provided." +
            	"\n" +
            	"\nThe questionnaire description generates "+actualHeaders.length+" spreadsheet columns;" +
		"\nthe responses file has "+unrandomisedHeaders.size()+" columns.");
        
        // find the first test question. Fool the 'final' check
        String testRegex = "Q\\d+: ([a-z]\\w*=\\d+(, [a-z]\\w*=\\d+)* \\[\\d+]|Other)";
        int start = 0;
        boolean found = false;
        for (int i = 0; i<actualHeaders.length; i++) {
            if (actualHeaders[i].matches(testRegex)) {
                // System.out.println(actualHeaders[i]+" matches");
                found = true; start = i;
                break;
            }
            /* else
                System.out.println(actualHeaders[i]+" doesn't match"); */
        }
        if (!found)
            Utils.fail("The responses spreadsheet doesn't seem to contain any test responses");
        quStart = start;
        
        int end = actualHeaders.length;
        for (int i = quStart; i<actualHeaders.length; i++) {
            if (!actualHeaders[i].matches(testRegex)) {
                end = i;
                break;
            }
        }
        quEnd = end; 
        
        if (quStart!=unrandomisedHeaders.quStart||quEnd!=unrandomisedHeaders.quEnd)
            Utils.fail(
                    "The responses spreadsheet doesn't seem to be data from the questionnaire whose description you provided." +
                 "\n" +
                 "\nThe questionnaire description generates test responses from column "+unrandomisedHeaders.quStart+ 
                    " to column "+unrandomisedHeaders.quEnd+";" +
                 "\nthe test responses in the responses file run from column "+quStart+ 
                    " to column "+quEnd);
        
        ordering = new int[quEnd-quStart];
        int idx = quStart;
        int qIdx = idx;
        int qNum = 1;
        while (idx<quEnd) {
            Pattern qAnswerPat = Pattern.compile("Q"+qNum+": ([a-z]\\w*=\\d+(, [a-z]\\w*=\\d+)*) \\[(\\d+)]");
            Pattern qOtherPat  = Pattern.compile("Q"+qNum+": Other");
            Matcher qAnswerMatch = qAnswerPat.matcher(actualHeaders[idx]);
            Matcher qOtherMatch  = qOtherPat.matcher(actualHeaders[idx]);
            if (qAnswerMatch.matches()) {
                int sub_idx = Integer.parseInt(qAnswerMatch.group(3))-1;
                System.out.println("we have Q"+qNum+"."+(sub_idx+1)+" "+qAnswerMatch.group(1)+
                        " at "+TextUtils.excelIdxOfIdx(idx));
                System.out.println("original corresponding is "+unrandomisedHeaders.elementAt(qIdx+sub_idx));
                ordering[idx-quStart] = qIdx+sub_idx;
            }
            else
            if (qOtherMatch.matches()) {
                System.out.println("we have an Other at "+TextUtils.excelIdxOfIdx(idx));
                System.out.println("original corresponding is "+unrandomisedHeaders.elementAt(idx));
                ordering[idx-quStart] = idx;
                ++qNum;
                qIdx = idx+1;
            }
            else
                Utils.crash("headers column " + TextUtils.excelIdxOfIdx(idx) + " " + actualHeaders[idx] + " is neither a question nor an Other");
            
            ++idx;
        }
        
        /*
            System.out.println("quStart = "+quStart);
            System.out.println("quEnd = "+quEnd);
            for (int i = quStart; i<quEnd; i++)
                System.out.println(""+i+": "+ordering[i-quStart]);
            Utils.fail("PaperItemiser goodbye");
         */
    }

    public String[] itemise(int nCols) throws IOException {
        String[] cols = super.itemise(nCols);
        if (rowIndex==0 || cols==null) 
            return cols;
        else {
            String[] newCols = cols.clone();
            for (int i = quStart; i<quEnd; i++)
                newCols[ordering[i-quStart]] = cols[i];
            return newCols;
        }
    }
}
