/*
 * Copyright 2016-2022 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
           FsDiff.formatMessage(FsDiff.MODIFIED, "prod1/p4.txt", FsDiff.CONFLICTS_WITH_THE_UPDATED_VERSION),
           FsDiff.formatMessage(FsDiff.ADDED, "prod1/p1.txt", FsDiff.HAS_BEEN_REMOVED_FROM_THE_UPDATED_VERSION),
           FsDiff.formatMessage(FsDiff.MODIFIED, "prod1/p3.txt", FsDiff.HAS_CHANGED_IN_THE_UPDATED_VERSION)
        );
    }
}
