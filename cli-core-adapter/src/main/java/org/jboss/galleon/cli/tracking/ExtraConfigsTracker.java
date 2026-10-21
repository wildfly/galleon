/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.tracking;

import java.nio.file.Path;
import java.util.List;
import org.jboss.galleon.cli.core.ProvisioningSession;
import static org.jboss.galleon.cli.tracking.ConfigsTracker.DELAYED_EXECUTION_MSG;
import org.jboss.galleon.layout.ProvisioningLayoutFactory;
import org.jboss.galleon.progresstracking.ProgressTracker;
import org.jboss.galleon.state.ProvisionedConfig;

/**
 * The List index 0 contains the String phase, index 1 the content.
 * @author jdenise@redhat.com
 */
public class ExtraConfigsTracker extends CliProgressTracker<List<Object>> {

    public ExtraConfigsTracker(ProvisioningSession session) {
        super(session, "Additional configurations:", "Additional configurations generated.");
    }

    @Override
    protected String processingContent(ProgressTracker<List<Object>> tracker) {
        String content = "Preparing...";
        String phase = (String) tracker.getItem().get(0);
        if(tracker.getItem().get(1) instanceof ProvisionedConfig) {
            content = "Generating " + ((ProvisionedConfig) tracker.getItem().get(1)).getName();
        } else if (tracker.getItem().get(1) instanceof Path) {
            content = "Installing config "+ ((Path) tracker.getItem().get(1)).getFileName();
        } else if (tracker.getItem().get(1) == null&& ProvisioningLayoutFactory.TRACK_CONFIGS.equals(phase)) {
            content = DELAYED_EXECUTION_MSG;
        }
        return content;
    }

    @Override
    protected String completeContent(ProgressTracker<List<Object>> tracker) {
        return "";
    }

}
