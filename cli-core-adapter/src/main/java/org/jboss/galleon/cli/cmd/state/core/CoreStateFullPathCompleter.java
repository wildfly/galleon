/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.core;

import java.util.List;

import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.model.FeatureContainer;
import org.jboss.galleon.cli.path.FeatureContainerPathConsumer;

/**
 *
 * @author jdenise@redhat.com
 */
public class CoreStateFullPathCompleter extends CoreAbstractPathCompleter {

    @Override
    protected void filterCandidates(FeatureContainerPathConsumer consumer, List<String> candidates) {
        // No OP.
    }

    @Override
    protected String getCurrentPath(PmCompleterInvocation completerInvocation, ProvisioningSession session) throws Exception {
        return session.getCurrentPath();
    }

    @Override
    protected FeatureContainer getContainer(PmCompleterInvocation completerInvocation, ProvisioningSession session) throws Exception {
        FeatureContainer container = session.getContainer();
        return container;
    }
}
