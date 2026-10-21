/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.feature.core;

import java.util.List;
import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.cmd.state.core.CoreAbstractPathCompleter;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.model.FeatureContainer;
import org.jboss.galleon.cli.path.FeatureContainerPathConsumer;

/**
 *
 * @author jdenise@redhat.com
 */
public class CoreFeatureSpecIdCompleter extends CoreAbstractPathCompleter {

    @Override
    protected String getCurrentPath(PmCompleterInvocation completerInvocation, ProvisioningSession session) throws Exception {
        return FeatureContainerPathConsumer.FEATURES_PATH;
    }

    @Override
    protected void filterCandidates(FeatureContainerPathConsumer consumer, List<String> candidates) {
        // NO-OP.
    }

    @Override
    protected FeatureContainer getContainer(PmCompleterInvocation completerInvocation, ProvisioningSession session) throws Exception {
        return new CoreStateAddFeatureCommand.AllFeaturesContainer(session.getState().getContainer());
    }
}
