package uk.ac.mdx.RBornat.Saeedgenerator;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileWriter;
import java.io.IOException;

public class TabbedFileWriter extends FileWriter {

    // all the FileWriter constructors, just in case
    public TabbedFileWriter(String fileName) throws IOException {
        super(fileName);
    }
    public TabbedFileWriter(String fileName, boolean append) throws IOException {
        super(fileName, append);
    }
    public TabbedFileWriter(File file) throws IOException {
        super(file);
    }
    public TabbedFileWriter(File file, boolean append) throws IOException {
        super(file, append);
    }
    public TabbedFileWriter(FileDescriptor fd) {
        super(fd);
    }

    private int cursor = 0;
    
    public void write(String s) throws IOException {
        if (s!=null) {
           int length = s.length();
           if (length!=0) {
               char[] cs = new char[length];
               s.getChars(0, length, cs, 0);
               write(cs, 0, length);
           }
        }
    }
    
    private int find(char[] cs, char c, int cBegin, int cEnd) {
        for (int i=cBegin; i<cEnd; i++)
            if (cs[i]==c)
                return i;
        return -1;
    }
    
    // implement 4-character tabs, for simple layout
    public void write(char[] cs, final int cBegin, final int cEnd) throws IOException {
        if (cs!=null && cBegin<cEnd) {
            int idx;
            if ((idx = find(cs, '\r', cBegin, cEnd))!=-1) {
                write(cs, cBegin, idx);                         // recursive
                super.write('\r'); cursor = 0;
                write(cs, idx+1, cEnd);                         // recursive
            }
            else 
            if ((idx = find(cs, '\n', cBegin, cEnd))!=-1) {
                write(cs, cBegin, idx);                         // recursive
                super.write('\n'); cursor = 0;
                write(cs, idx+1, cEnd);                         // recursive
            }
            else
            if ((idx = find(cs, '\t', cBegin, cEnd))!=-1) {
                write(cs, cBegin, idx);                         // recursive
                int newcursor = ((cursor+4)/4)*4;
                for (int i=cursor; i<newcursor; i++)
                    super.write(' ');
                cursor = newcursor;
                write(cs, idx+1, cEnd);                         // recursive
            }
            else {
                for (int i=cBegin; i<cEnd; i++) {
                    super.write(cs[i]); cursor++;
                }
            }
        }
    }
}
