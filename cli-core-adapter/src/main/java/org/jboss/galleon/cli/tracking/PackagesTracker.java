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
 */
public class PackagesTracker extends CliProgressTracker<PackageRuntime> {

    public PackagesTracker(ProvisioningSession session) {
        super(session, "Installing packages", "Packages installed.");
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
