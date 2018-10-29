/*
 * Copyright 2016-2018 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.cli.tracking;

import org.jboss.galleon.progresstracking.ProgressTracker;
import org.jboss.galleon.runtime.PackageRuntime;

/**
 *
 * @author jdenise@redhat.com
 * @author alexey.loubyansky@redhat.com
 */
public class JBossModulesTracker extends CliProgressTracker<PackageRuntime> {

    public JBossModulesTracker() {
        super("Installing JBoss modules", "JBoss modules installed.");
    }

    @Override
    public String processingContent(ProgressTracker<PackageRuntime> tracker) {
        return String.format("%s of %s (%s%%)",
                tracker.getProcessedVolume(), tracker.getTotalVolume(), ((double) Math.round(tracker.getProgress() * 10)) / 10);
    }

    @Override
    protected String completeContent(ProgressTracker<PackageRuntime> tracker) {
        return "";
    }
}
