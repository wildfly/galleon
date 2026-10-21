/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.tracking;

import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.progresstracking.ProgressTracker;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;

/**
 *
 * @author jdenise@redhat.com
 */
public class BuildLayoutTracker extends CliProgressTracker<FPID> {

    private final ProvisioningSession session;

    public BuildLayoutTracker(ProvisioningSession session) {
        super(session, "Resolving feature-pack", "Feature-packs resolved.");
        this.session = session;
    }

    @Override
    protected String processingContent(ProgressTracker<FPID> tracker) {
        return session.getExposedLocation(null, tracker.getItem().getLocation()).toString();
    }

    @Override
    protected String completeContent(ProgressTracker<FPID> tracker) {
        return "";
    }

}
