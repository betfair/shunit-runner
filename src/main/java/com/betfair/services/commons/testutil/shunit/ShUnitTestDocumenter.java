package com.betfair.services.commons.testutil.shunit;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: langfords
 * Date: 31-Jul-2009
 * Time: 15:50:21
 * To change this template use File | Settings | File Templates.
 */
public class ShUnitTestDocumenter {


    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: ShUnitTestDocumenter <target-dir> <source-file>+");
        }

        File targetDir = new File(args[0]);
        File[] sourceFiles = new File[args.length-1];
        for (int i=1; i<args.length; i++) {
            sourceFiles[i-1] = new File(args[i]);
        }

        for (File f : sourceFiles) {
            String fileName = f.getName();
            if (fileName.endsWith(".sh")) {
                fileName = fileName.substring(0, fileName.length() - 3);
            }
            File targetFile = new File(targetDir, fileName);
            documentTestSuite(f, targetFile);
        }
    }

    private static final int MODE_NONE = 0;
    private static final int READING_COMMENT = 1;

    private static void documentTestSuite(File source, File target) throws IOException {
        StringBuilder output = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(source));
        String line;
        int mode = MODE_NONE;
        output.append("h2. ");
        while (br.readLine() != null) {
        }
    }
}
