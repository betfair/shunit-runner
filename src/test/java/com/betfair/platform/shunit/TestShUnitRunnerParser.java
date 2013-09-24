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

import com.betfair.platform.shunit.ShUnitRunner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.Failure;
import org.junit.runner.Description;
import org.junit.Before;
import org.junit.Test;
import org.hamcrest.BaseMatcher;
import org.mockito.InOrder;
import static org.mockito.Mockito.*;
import static org.mockito.Matchers.argThat;
import org.apache.commons.lang.StringUtils;

import java.io.InputStream;

/**
 */
public class TestShUnitRunnerParser {

    private RunNotifier notifier;
    private InOrder inorder;

    @Before
    public void setup() {
        notifier = mock(RunNotifier.class);
        inorder = inOrder(notifier);
    }

    @Test
    public void singleTestSuccess() {
        invokeParser("singleTestSuccess.txt");

        verifyTestSuccess("testSomething");
    }

    @Test
    public void multiTestSuccess() {
        invokeParser("multiTestSuccess.txt");

        verifyTestSuccess("testHelpPage");
        verifyTestSuccess("testTestModeOutput");
        verifyTestSuccess("testSimpleOk");
        verifyTestSuccess("testSimpleWarn");
        verifyTestSuccess("testSimpleFail");
    }

    @Test
    public void singleTestFailure() {
        invokeParser("singleTestFailure.txt");

        verifyTestFailure("testSomething", "Some text expected:<string1> but was:<string2>");
    }

    @Test
    public void singleTestMultipleAssertionFailure() {
        invokeParser("singleTestMultipleAssertionFailure.txt");

        verifyTestFailure("testSomething", "Some text expected:<string1> but was:<string2>");
        verifyNoMoreFailures();
    }

    @Test
    public void multiTestSingleFailure() {
        invokeParser("multiTestSingleFailure.txt");

        verifyTestSuccess("testHelpPage");
        verifyTestFailure("testTestModeOutput", "Script should print warning when in test mode expected:<Warning: Running in test moe> but was:<Warning: Running in test mode>");
        verifyTestSuccess("testSimpleOk");
        verifyTestSuccess("testSimpleWarn");
        verifyTestSuccess("testSimpleFail");
    }

    @Test
    public void multiTestMultiFailure() {
        invokeParser("multiTestMultiFailure.txt");

        verifyTestSuccess("testHelpPage");
        verifyTestFailure("testTestModeOutput", "Script should print warning when in test mode expected:<Warning: Running in test moe> but was:<Warning: Running in test mode>");
        verifyTestFailure("testSimpleOk", "Some text expected:<3> but was:<4>");
        verifyTestSuccess("testSimpleWarn");
        verifyTestSuccess("testSimpleFail");
    }

    private void invokeParser(String fileName) {
        ShUnitRunner.OutputParser parser = new ShUnitRunner.OutputParser(getClass());
        String className = getClass().getName();
        String packageName = "";
        if (className.contains(".")) {
            packageName = className.substring(0, className.lastIndexOf("."));
        }
        String resourcePath = "/" + StringUtils.replace(packageName, ".", "/") + "/" + fileName;
        InputStream is = getClass().getResourceAsStream(resourcePath);
        parser.parseOutput(is, notifier);
    }

    private void verifyTestSuccess(String testName) {
        inorder.verify(notifier).fireTestStarted(desc(testName));
        inorder.verify(notifier).fireTestFinished(desc(testName));
    }

    private void verifyTestFailure(String testName, String message) {
        inorder.verify(notifier).fireTestStarted(desc(testName));
        inorder.verify(notifier).fireTestFailure(fail(testName, message));
    }

    private void verifyNoMoreFailures() {
        inorder.verify(notifier, never()).fireTestFailure(any(Failure.class));
    }

    private Description desc(String s) {
        return argThat(new DescriptionMatcher(s));
    }

    private Failure fail(String s, String message) {
        return argThat(new FailureMatcher(s, message));
    }

    private class DescriptionMatcher extends BaseMatcher<Description> {
        private String string;

        public DescriptionMatcher(String s) {
            string = s;
        }

        public boolean matches(Object o) {
            Description d = (Description) o;
            return d.toString().equals(string+"("+TestShUnitRunnerParser.class.getName()+")");
        }

        public void describeTo(org.hamcrest.Description description) {
            description.appendText("Description(\""+Description.createTestDescription(TestShUnitRunnerParser.class, string)+"\")");
        }
    }

    private class FailureMatcher extends BaseMatcher<Failure> {
        private String testName;
        private String message;

        public FailureMatcher(String testName, String message) {
            this.testName = testName;
            this.message = message;
        }

        public boolean matches(Object o) {
            Failure f = (Failure) o;
            if (!f.getDescription().toString().equals(testName +"("+TestShUnitRunnerParser.class.getName()+")")) {
                return false;
            }
            String assertionMessage = f.getException().getMessage();
            return assertionMessage.equals(message);
        }

        public void describeTo(org.hamcrest.Description description) {
            description.appendText("Failure(\n");
            description.appendText("Description(\""+Description.createTestDescription(TestShUnitRunnerParser.class, testName)+"\"),\n");
            description.appendText("Exception message = \""+message+"\"");
            description.appendText(")");
        }
    }
}

