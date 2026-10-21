/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.configuration.core;

import java.util.List;

import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.cmd.state.core.CoreStateFullPathCompleter;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.path.FeatureContainerPathConsumer;

/**
 *
 * @author jdenise@redhat.com
 */
public class CoreProvisionedConfigurationCompleter extends CoreStateFullPathCompleter {

    @Override
    protected void filterCandidates(FeatureContainerPathConsumer consumer, List<String> candidates) {
        ConfigurationUtil.filterCandidates(consumer, candidates);
    }

    @Override
    protected String getCurrentPath(PmCompleterInvocation completerInvocation, ProvisioningSession session) throws Exception {
        return ConfigurationUtil.getCurrentPath(session.getPmSession());
    }

}
