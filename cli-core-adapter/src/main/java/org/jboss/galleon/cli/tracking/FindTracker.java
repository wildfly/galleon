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
public class FindTracker extends CliProgressTracker<FPID> {
    public static final String TRACK_FIND = "CliFindTracker";
    private final ProvisioningSession session;

    public FindTracker(ProvisioningSession session) {
        super(session, "Searching in", "Search done.");
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
