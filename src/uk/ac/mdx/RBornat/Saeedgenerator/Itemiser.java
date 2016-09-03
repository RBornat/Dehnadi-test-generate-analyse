package uk.ac.mdx.RBornat.Saeedgenerator;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Vector;

/* just to read items from the a CSV file, and keep track of line numbers, character numbers, row numbers.
 * 
 * needed because clever LimeSurvey allows newlines in strings in CSV files (and Excel cooperates)
 */
public class Itemiser {
    final BufferedReader datain;
    int rowIndex, linenum, charidx;
    char[] line;

    Itemiser(BufferedReader datain) {
        this.datain = datain;
        this.rowIndex = -1;
        this.linenum = this.charidx = 0; 
        this.line = null;
    }
    
    public int rowidx() {
        return rowIndex;
    }
    
    // normalises end-of-line to \n
    int getchar() throws IOException {
        if (line==null) {
            String s = datain.readLine();
            if (s==null)
                return -1;
            else {
                line = s.toCharArray();
                linenum++;
                charidx = 0;
            }
        }
        if (charidx==line.length) {
            line = null;
            return '\n';
        }
        else
            return line[charidx++];
    }

    void putback(int c) {
        // make sure the next thing getchar() returns is c
        if (c=='\n') { 
            line = new char[0]; 
            charidx = 0;
        }
        else
        if (c!=-1)
            charidx--;
    }
    
    String getItem() throws IOException {
        StringBuilder sb = new StringBuilder();
        int c=getchar();
        
        if (c==-1)
            return null;
        else
        if (c=='\n') { // end of line, baby
            putback(c); 
            return sb.toString();
        }
        else
        if (c=='"') {
            int startlinenum = linenum, startcolidx=charidx;
            while (true) {
                c=getchar();
                if (c=='"') {
                    int c1 = getchar();
                    if (c1=='"')
                        // two quotes is a quate, says IBM
                        sb.append((char)c);
                    else {
                        putback(c1);
                        break;
                    }
                }
                else
                if (c=='\\') {
                    // what can we escape? Backslash and quote, I hope
                    int c1 = getchar();
                    if (c1=='\\')
                        sb.append((char)c);
                    else
                    if (c1=='"') {
                        // \"" is a quote, says LimeSurvey, and Excel cooperates
                        int c2 = getchar();
                        if (c2=='"')
                            sb.append((char)c2);
                        else {
                            putback(c2);
                            sb.append((char)c1);
                        }
                    }
                    else
                        sb.append((char)c1); // why not?
                }
                else
                if (c==-1)
                    Utils.fail("unterminated string (starts character "+Utils.ordinal(startcolidx)+
                                    " of line "+startlinenum+")");
                else
                    sb.append((char)c);
            }
        }
        else {
            while (c!=',' && c!='\n' && c!=-1) {
                sb.append((char)c);
                c = getchar();
            }
            putback(c); 
        }
        
        return sb.toString();
    }
    
    /* has to handle newlines in strings (thanks, LimeSurvey!) */
    public String[] itemise(int nCols) throws IOException { 
        rowIndex++;
        Vector<String> row = new Vector<String>(nCols);
        int c;
        
        do {
            String item = getItem();

            if (item==null) {
                if (row.size()==0)
                        return null;
                else
                    Utils.fail("marks file ends with a comma!");
            }
        
            row.add(item);
            c = getchar();
        } while (c==',');
        
        if (c!='\n' && c!=-1)
            Utils.fail("comma expected after item \""+row.lastElement()+"\" on line "+linenum+" character "+charidx);
        
        String [] cols = row.toArray(new String[row.size()]);

        return cols;
    }
}
