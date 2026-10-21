/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.tracking;

import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.progresstracking.ProgressTracker;
import org.jboss.galleon.runtime.PackageRuntime;

/**
 *
 * @author jdenise@redhat.com
 * @author alexey.loubyansky@redhat.com
 */
public class JBossModulesTracker extends CliProgressTracker<PackageRuntime> {

    public JBossModulesTracker(ProvisioningSession session) {
        super(session, "Installing JBoss modules", "JBoss modules installed.");
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
