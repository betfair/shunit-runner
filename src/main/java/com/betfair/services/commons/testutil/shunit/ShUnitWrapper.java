package com.betfair.services.commons.testutil.shunit;

import java.io.File;

/**
 * A wrapper around shunit test scripts. shunit test scripts are self contained test suites, so any implementer of this
 * interface will represent a suite. This wrapper allows ease of integration into all places where junit works.
 * @see ShUnitRunner
 * @author Simon Matic Langford
 */
public interface ShUnitWrapper {
    File getScript();
}