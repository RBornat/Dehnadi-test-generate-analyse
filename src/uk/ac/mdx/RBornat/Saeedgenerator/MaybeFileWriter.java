package uk.ac.mdx.RBornat.Saeedgenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MaybeFileWriter {
    final File file;
    final FileWriter fw;
    final static String LineSep = System.getProperty("line.separator");
    
    MaybeFileWriter(File f, FileWriter fw) {
        this.file = f;
        this.fw = fw;
    }
    public void write(String s) throws IOException {
        if (fw!=null)
            fw.write(s);
    }
    public void writeln(String s) throws IOException {
        write(s);
        writeln();
}
    public void writeln() throws IOException {
        write(LineSep);
}
    public void tabwrite(String s) throws IOException {
        tabwrite();
        write(s);
    }
    public void tabwrite() throws IOException {
        write("\t");
    }
    public void tabwriteln(String s) throws IOException {
        tabwrite();
        write(s);
        writeln();
    }
    public void close() throws IOException {
        if (fw!=null)
            fw.close();
    }
}
