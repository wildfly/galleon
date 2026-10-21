/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.userchanges.test.diff;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.jboss.galleon.Constants;
import org.jboss.galleon.diff.FsDiff;

public class UserChangesOnlyConflictsDiffTestCase extends UserChangesFullDiffTestCaseTestCase {

    @Override
    protected HashMap<String, String> getPMOptions() {
        final HashMap<String, String> options = new HashMap<>();
        options.put(Constants.PRINT_ONLY_CONFLICTS, "true");
        return options;
    }

    @Override
    protected List<String> expectedDiff() {
        return Arrays.asList(
           FsDiff.formatMessage(FsDiff.CONFLICT, "prod1/p4.txt", FsDiff.CONFLICTS_WITH_THE_UPDATED_VERSION),
           FsDiff.formatMessage(FsDiff.ADDED, "prod1/p1.txt", FsDiff.HAS_BEEN_REMOVED_FROM_THE_UPDATED_VERSION),
           FsDiff.formatMessage(FsDiff.CONFLICT, "prod1/p3.txt", FsDiff.HAS_CHANGED_IN_THE_UPDATED_VERSION)
        );
    }
}
