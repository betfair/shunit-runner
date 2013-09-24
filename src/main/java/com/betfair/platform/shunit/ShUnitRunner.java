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

package com.betfair.platform.shunit;

import org.junit.runner.Runner;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.Failure;
import org.junit.internal.runners.InitializationError;

import java.io.*;

import junit.framework.AssertionFailedError;

/**
 * Runner enabling junit to run shunit tests.
 * @see ShUnitWrapper
 */
public class ShUnitRunner extends Runner {
    private Class<?> clazz;

    public ShUnitRunner(Class<?> clazz) throws InitializationError {
		this.clazz = clazz;
	}

    public Description getDescription() {
        return Description.createSuiteDescription(clazz);
    }

    @Override
    public int testCount() {
        ShUnitWrapper wrapper = getWrapper(null);
        // default to a crap number, but assume that developer intended there to be at least one test
        // the test running will fail with a meaningful message
        if (wrapper == null) {
            return 1;
        }
        try {
            int count = 0;
            BufferedReader br = new BufferedReader(new FileReader(wrapper.getScript()));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.matches("^function test.*\\(\\).*") || line.matches("^test.*\\(\\).*")) {
                    count ++;
                }
            }
            return count;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ShUnitWrapper getWrapper(RunNotifier notifier) {
        Object o = null;
        try {
            o = clazz.newInstance();
        } catch (InstantiationException e) {
            if (notifier != null) {
                notifier.testAborted(Description.createSuiteDescription(clazz), e);
            }
        } catch (IllegalAccessException e) {
            if (notifier != null) {
                notifier.testAborted(Description.createSuiteDescription(clazz), e);
            }
        }
        if (o instanceof ShUnitWrapper) {
            return (ShUnitWrapper) o;
        }
        return null;
    }

    public void run(RunNotifier notifier) {
        ShUnitWrapper wrapper = getWrapper(notifier);
        if (wrapper != null) {
            File script = wrapper.getScript();
            File parentFile = null;

            try {
                ProcessBuilder pb = new ProcessBuilder();
                parentFile = script.getCanonicalFile().getParentFile();
                pb.directory(parentFile);
                pb.command("sh", "./"+script.getName());
                Process p = pb.start();
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                String line;
                while ((line = br.readLine()) != null) {
                    System.err.println(line);
                }
                new OutputParser(clazz).parseOutput(p.getInputStream(), notifier);
            } catch (Exception e) {
                Exception ex = e;
                if (parentFile != null) {
                    StringBuilder buffer = new StringBuilder();
                    buffer.append("Directory: ").append(parentFile).append("\n");
                    buffer.append("Script exists? ").append(script.exists()).append("\n");
                    buffer.append("Script is readable? ").append(script.canRead()).append("\n");
                    buffer.append("Script is a file? ").append(script.isFile()).append("\n");
                    if (script.exists() && script.canRead() && script.isFile()) {
                        // lets have a look at the perms
                        try {
                            ProcessBuilder pb = new ProcessBuilder();
                            pb.directory(parentFile);
                            pb.command("ls", "-l", script.getName());
                            Process p = pb.start();
                            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                            buffer.append("ls -l output:\n");
                            String line;
                            while ((line = br.readLine()) != null) {
                                buffer.append(line).append("\n");
                            }
                        } catch (IOException e1) {
                            buffer.append("Couldn't execute ls: ").append(e1.getMessage()).append("\n");
                        }
                    }
                    ex = new RuntimeException(buffer.toString(), e);
                }
                notifier.testAborted(Description.createSuiteDescription(clazz), ex);
            }
        }
        else {
			notifier.testAborted(Description.createSuiteDescription(clazz), new ClassCastException("This runner requires test classes that implement ShUnitWrapper"));
        }
    }

    public static class OutputParser {
        private Class clazz;

        public OutputParser(Class clazz) {
            this.clazz = clazz;
        }

        public void parseOutput(InputStream is, RunNotifier notifier) {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            try {
                String currentTestName = null;
                boolean currentTestFailed = false;
                while ((line = br.readLine()) != null) {
                    // first line is the name of a test
                    if (line.startsWith("test")) {
                        // finish previous test
                        if (currentTestName != null && !currentTestFailed) {
                            notifier.fireTestFinished(Description.createTestDescription(clazz, currentTestName));
                        }
                        // reset flag
                        currentTestFailed = false;
                        currentTestName = line;
                        // start new test
                        notifier.fireTestStarted(Description.createTestDescription(clazz, currentTestName));
                        continue;
                    }
                    // only single failure per test supported by junit
                    if (line.startsWith("ASSERT:") && !currentTestFailed) {
                        // assertion failure
                        notifier.fireTestFailure(new Failure(Description.createTestDescription(clazz, currentTestName), new AssertionFailedError(line.substring(7))));
                        currentTestFailed = true;
                        continue;
                    }
                }
                // finish last test
                if (!currentTestFailed) {
                    notifier.fireTestFinished(Description.createTestDescription(clazz, currentTestName));
                }
            } catch (IOException e) {
                notifier.testAborted(Description.createSuiteDescription(clazz), e);
            }
        }
    }
}

