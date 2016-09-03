package uk.ac.mdx.RBornat.Saeedgenerator;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Vector;

public class TextUtils {
    
    final static String LineSep = System.getProperty("line.separator");
    final static String HTMLParaSep = "<p>&nbsp;</p>";
    final static String LaTeXParaSep = LineSep+LineSep;

    public static boolean dataLine(String line) {
        return line!=null && (line.length()>=2 ? !line.substring(0,2).equals("**") : true);
    }

    public static String markedReadLine(BufferedReader in) throws IOException {
        in.mark(4000);
        return stripTrailingSpaces(in.readLine());
    }

    public static String stripControlWord(String line) { // which must start **word(spaces)
        if (dataLine(line) || line.length()<=2 || !isAlpha(line.charAt(2)))
            Utils.fail("stripControlWord given "+line);
        int i=2; 
        while (i<line.length() && isAlpha(line.charAt(i))) {
            ++i;
        }
        while (i<line.length() && line.charAt(i)==' ') {
           ++i;
        }
        return line.substring(i);
    }
    
    public static String[] inputTextBlock(BufferedReader testin) throws IOException {
        Vector<String> ss = new Vector<String>();
        String line;

        while (dataLine((line=markedReadLine(testin)))) {
            ss.add(line);
            // System.err.println("adding "+line);
        }

        /* if (line==null)
            System.err.println("ended at null");
        else
            System.err.println("ended at \""+line+"\""); */

        testin.reset();
        return ss.toArray(new String[ss.size()]);
    }

    public static boolean isAlpha(int c) {
        return ('a'<=c && c<='z') || ('A'<=c && c<='Z');
    }

    public static boolean isDigit(int c) {
        return '0'<=c && c<='9';
    }

    public static boolean isName(String item) {
        return item!=null && item.length()!=0 && isAlpha(item.charAt(0)) && allAlphaNum(item, 1, item.length());
    }

    public static boolean isNumber(String item) {
        return item!=null && item.length()!=0 && allDigit(item, 0, item.length());
    }

    public static boolean allAlpha(String s, int m, int n) {
        for (int i=m; i<n; i++)
            if (!isAlpha(s.charAt(i)))
                return false;
        return true;
    }

    public static boolean allDigit(String s, int m, int n) {
        for (int i=m; i<n; i++)
            if (!isDigit(s.charAt(i)))
                return false;
        return true;
    }

    public static boolean allAlphaNum(String s, int m, int n) {
        for (int i=m; i<n; i++)
            if (!isAlpha(s.charAt(i)) && !isDigit(s.charAt(i)))
                return false;
        return true;
    }

    public static String stripTrailingSpaces(String s) { 
        if (s==null)
            return s;

        int lim = s.length();
        while (lim!=0 && (s.charAt(lim-1)==' ' || s.charAt(lim-1)=='\t'))
            lim--;
        if (lim==s.length()) 
            return s;
        else 
            return s.substring(0,lim);
    }

    public static String enLine(String[] lines) {
        SeparatedStringBuilder sb = new SeparatedStringBuilder(" ");
        for (String line : lines) {
            if (line!=null)
                sb.append(stripTrailingSpaces(line));
        }
        return sb.toString();
    }

    public static String enPara(String[] lines, String paraSep) {
        SeparatedStringBuilder sb = new SeparatedStringBuilder(paraSep);
        SeparatedStringBuilder para = new SeparatedStringBuilder(LineSep);
        boolean paraLoaded = false;

        for (String line : lines) {
            if (line!=null) {
                line = stripTrailingSpaces(line);
                if (line.length()>0) {
                    para.append(line);
                    paraLoaded = true;
                }
                else
                if (paraLoaded) {
                    sb.append(para.toString());
                    para = new SeparatedStringBuilder(LineSep);
                    paraLoaded = false;
                }
            }
        }

        if (paraLoaded)
            sb.append(para.toString());

        return sb.toString();
    }
    
    public static String enParaHTML(String[] lines) {
        String paraSep = "</p>"+LineSep+HTMLParaSep+LineSep+"<p>";
        String para = enPara(lines, paraSep);
        StringBuilder sp = new StringBuilder();
        sp.append("<p>"); sp.append(para); sp.append("</p>");
        return sp.toString();
    }
    
    public static String enParaLaTeX(String[] lines) {
        return enPara(lines, LaTeXParaSep);
    }
    
    static String[] tokenise(String line) { 
        Vector<String> tokens = new Vector<String>(10);
        int c;
        int i = 0;
        
        while (i<line.length() && line.charAt(i)==' ') i++;

        // invariant: we are not pointing at a space
        while (i<line.length()) {
            StringBuilder sb = new StringBuilder();

            while (i<line.length() && line.charAt(i)!=' ') {
                sb.append(line.charAt(i));
                i++;
            }
            
            tokens.add(sb.toString());
            
            while (i<line.length() && line.charAt(i)==' ') i++;
        }
         
        return tokens.toArray(new String[tokens.size()]);
    }

    public static String interpolateStrings(String[] ss, int startidx, String sep) {
        SeparatedStringBuilder sb = new SeparatedStringBuilder(sep);
        for (int i = startidx; i<ss.length; i++) 
            sb.append(ss[i]);
        return sb.toString();
    }

    public static String interpolateStrings(String[] ss, String sep) {
        return interpolateStrings(ss, 0, sep);
    }

    public static String enQuote(Object o) {
        if (o==null)
            return "null";
        else {
            StringBuffer b = new StringBuffer();
            b.append('"');
            String s = o.toString();
            for (int i=0; i<s.length(); i++) {
                char c = s.charAt(i);
                if (c=='"')
                    b.append("\\\"");
                else
                    b.append(c);
            }
            b.append('"');
            return b.toString();
        }
    }

    static String csvItem(String item) {
        if (item==null)
            return "";
        else
        if (item.indexOf(',')==-1)
            return item;
        else
            return "\""+item+"\"";
    }
    
    static String excelIdxOfIdx(int idx) { // runs from zero, of course
        if (idx<0)
            return "";
        else
        if (idx<26)
            return String.valueOf((char)(idx+'A'));
        else
            return excelIdxOfIdx(idx / 26-1).concat(excelIdxOfIdx(idx % 26));
    }
}
