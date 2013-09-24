/*
 * Copyright 2013, The Sporting Exchange Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.betfair.services.commons.testutil.shunit;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

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
