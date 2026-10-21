/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.tracking;

import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.progresstracking.ProgressTracker;
import org.jboss.galleon.state.ProvisionedConfig;

/**
 *
 * @author jdenise@redhat.com
 */
public class ConfigsTracker extends CliProgressTracker<ProvisionedConfig> {

    static final String DELAYED_EXECUTION_MSG = "Delayed generation, waiting...";

    public ConfigsTracker(ProvisioningSession session) {
        super(session, "Generating configuration", "Configurations generated.");
    }

    @Override
    protected String processingContent(ProgressTracker<ProvisionedConfig> tracker) {
        if(tracker.getItem() == null) {
            // Delayed use case
            return DELAYED_EXECUTION_MSG;
        }
        return tracker.getItem().getModel() + "/" + tracker.getItem().getName();
    }

    @Override
    protected String completeContent(ProgressTracker<ProvisionedConfig> tracker) {
        return "";
    }

}
