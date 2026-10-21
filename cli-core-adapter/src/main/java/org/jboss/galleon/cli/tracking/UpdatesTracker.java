/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.tracking;

import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.progresstracking.ProgressTracker;
import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;

/**
 *
 * @author jdenise@redhat.com
 */
public class UpdatesTracker extends CliProgressTracker<ProducerSpec> {

    private final ProvisioningSession session;

    public UpdatesTracker(ProvisioningSession session) {
        super(session, "Looking for update for", null);
        this.session = session;
    }

    @Override
    protected String processingContent(ProgressTracker<ProducerSpec> tracker) {
        return session.getExposedLocation(null, tracker.getItem().getLocation()).toString();
    }

    @Override
    protected String completeContent(ProgressTracker<ProducerSpec> tracker) {
        // No complete message, the command itself adds one.
        return null;
    }

}
