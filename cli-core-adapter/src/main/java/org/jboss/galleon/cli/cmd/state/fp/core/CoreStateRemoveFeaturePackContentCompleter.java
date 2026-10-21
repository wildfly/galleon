/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.fp.core;

import java.util.ArrayList;
import java.util.List;
import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.core.GalleonCoreContentCompleter;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.config.FeaturePackConfig;

/**
 *
 * @author jdenise@redhat.com
 */
public class CoreStateRemoveFeaturePackContentCompleter implements GalleonCoreContentCompleter<ProvisioningSession> {

    @Override
    public List<String> complete(PmCompleterInvocation invoc, ProvisioningSession context) {
        State session = context.getState();
        List<String> lst = new ArrayList<>();
        if (session != null) {
            for (FeaturePackConfig fp : session.getConfig().getFeaturePackDeps()) {
                String loc = context.
                        getExposedLocation(null, fp.getLocation()).toString();
                lst.add(loc);
            }
        }
        return lst;
    }

}
