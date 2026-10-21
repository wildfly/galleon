/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.feature.core;

import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.cmd.state.core.CoreStateFullPathCompleter;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.path.FeatureContainerPathConsumer;

/**
 *
 * @author jdenise@redhat.com
 */
public class CoreFeatureCompleter extends CoreStateFullPathCompleter {

    @Override
    protected String getCurrentPath(PmCompleterInvocation completerInvocation, ProvisioningSession session) throws Exception {
        return FeatureContainerPathConsumer.FINAL_CONFIGS_PATH;
    }
}
