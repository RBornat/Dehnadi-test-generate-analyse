package uk.ac.mdx.RBornat.Saeedgenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class FBufferedReader extends BufferedReader {
    public final File file;
    FBufferedReader(File f) throws FileNotFoundException {
        super(new FileReader(f));
        file = f;
    }
}
