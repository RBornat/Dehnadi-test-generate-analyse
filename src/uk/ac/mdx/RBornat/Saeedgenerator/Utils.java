package uk.ac.mdx.RBornat.Saeedgenerator;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class Utils {
    
    // oh the ceaseless dance of interface conversions ..
    public static final int Plain    = JOptionPane.PLAIN_MESSAGE;
    public static final int Info     = JOptionPane.INFORMATION_MESSAGE;
    public static final int Warning  = JOptionPane.WARNING_MESSAGE;
    public static final int Error    = JOptionPane.ERROR_MESSAGE;
    public static final int Question = JOptionPane.QUESTION_MESSAGE;
    
    private static final Frame frame = new Frame();
    
    public static void showErrorAlert(String message) {
        JOptionPane.showMessageDialog(frame, makeMessage(message),
                                                 "Test generator", Error);
    }

    public static void showInfoAlert(String title, String message) {
        JOptionPane.showMessageDialog(frame, makeMessage(message),
                                                 title, Info);
    }

    public static void showInfoAlert(String message) {
        showInfoAlert("Generator / Analyser", message);
    }

    public static void fail(String s) {
        showErrorAlert(s);
        System.exit(1);
    }
    
    public static void failWithTextOutput(String s) {
        System.err.println(s);
        fail(s);
    }
    
    public static void crash(String s) {
        System.err.println("Crash: "+s);
        fail(s);
    }
    
    private static Object makeMessage(Object o) {
        if (o instanceof String) {
            String s = (String)o;
            int nli;
            JLabel[] result;

            if ((nli=s.indexOf(TextUtils.LineSep))!=-1) {
                JLabel[] first = (JLabel[])makeMessage(s.substring(0,nli));
                JLabel[] second = (JLabel[])makeMessage(s.substring(nli+TextUtils.LineSep.length()));
                result = new JLabel[first.length+second.length];
                for (int i=0; i<first.length; i++)
                    result[i]=first[i];
                for (int i=0; i<second.length; i++)
                    result[i+first.length]=second[i];
            }
            else 
                result = new JLabel[] { makeLabel(s) };

            return result;
        }
        else
            return o;
    }

    public static JLabel makeLabel(String s) {
        return new JLabel(s.length()==0 ? " " : s);
    }

    /* does a1 equal a2[m..n]? return the number of elements which match */
    public static int checkSubSequence(Object[] a1, Object[] a2, int m, int n) {
        if (a1==null || a2==null || m<0 || a2.length<m+a1.length) {
            System.err.println("isSubsequence parameter mismatch: "+a1.length+", "+a2.length+", "+m);
            return 0;
        }
        for (int i=0; i<n; i++) 
            if (!(a1[i]==null ||                // null means don't care
                    a1[i].equals(a2[i+m])))
                return i;
        return a1.length;
    }
    
    public static int checkSubSequence(Object[] a1, Object[] a2, int m) {
        return checkSubSequence(a1, a2, m, a1.length);
    }
    
    public static boolean isSubSequence(Object[] a1, Object[] a2) {
        return checkSubSequence(a1, a2, 0)==a1.length;
    }
    
    // Questions, and assignment models, are numbered from 0.
    // As mental hygiene I've tried to say 'idx' for such numbers.
    // and use this function to generate the ordinal number.
    static int ordinal(int idx) {
        return idx+1;
    }
    
    public static <T> T[] concatArrays(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
      }

    public static File askFile(int loadorsave, String title, String directoryprefid, String fileprefid, 
            String filedefault) {
        if (Generator.runningBlind && loadorsave==FileDialog.LOAD) {
            Utils.showInfoAlert(title, "Java for OS X 10.11/12 has a bug. It doesn't show the title bar "+
                                       "on file dialogs.\n\n The file dialog you are about to see "+
                                       "should be titled \""+title+"\"");
        }
        FileDialog fd = new FileDialog(Generator.frame, title, loadorsave); 
        fd.setDirectory(Prefs.getProp(directoryprefid, ""));
        fd.setFile(fileprefid.equals("") ? filedefault : Prefs.getProp(fileprefid, filedefault));
        fd.setVisible(true);
        
        String directoryname = fd.getDirectory(), filename = fd.getFile();
        if (filename==null)
            return null;
        else 
            return new File(directoryname, filename);
    }

    public static MaybeFileWriter openWrite(String title, String directoryprefid, String fileprefid, String filedefault) {
        while (true) {
            File f = Utils.askFile(FileDialog.SAVE, title, directoryprefid, fileprefid, filedefault); 

            if (f==null) 
                return new MaybeFileWriter(null,null);
            else {
                try {
                    MaybeFileWriter m = new MaybeFileWriter(f,new TabbedFileWriter(f)); 
                    Prefs.putProp(directoryprefid, f.getParent());
                    if (!fileprefid.equals("")) Prefs.putProp(fileprefid, f.getName());
                    return m;
               }
                catch (IOException e) {
                    showErrorAlert("can't create "+f);
                    directoryprefid = f.getParent();
                    if (!fileprefid.equals(""))
                        fileprefid = f.getName();
                }
            } // f!=null
        } // while true
    }


    public static FBufferedReader openRead(String title, String directoryprefid, String fileprefid, String filedefault) {
        while (true) {
            File f = Utils.askFile(FileDialog.LOAD, title, directoryprefid, fileprefid, filedefault); 
    
            if (f==null) 
                return null;
            else {
                try {
                    FBufferedReader fb = new FBufferedReader(f);
                    Prefs.putProp(directoryprefid, f.getParent());
                    if (!fileprefid.equals("")) Prefs.putProp(fileprefid, f.getName());
                    return fb;
              }
                catch (IOException e) {
                    showErrorAlert("can't open "+f);
                    directoryprefid = f.getParent();
                    if (!fileprefid.equals(""))
                        fileprefid = f.getName();
                }
            } // f!=null
        } // while true
    }

    public static boolean member(Object item, Object[] set) {
        if (set==null)
            return false; // maybe this happens anyway, but belt and braces ...
        for (Object s : set)
            if (item.equals(s))
                return true;
        return false;
    }

}
