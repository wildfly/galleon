/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.configuration.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.core.GalleonCoreContentCompleter;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.model.ConfigInfo;
import org.jboss.galleon.cli.model.FeatureContainer;

/**
 *
 * @author jdenise@redhat.com
 */
public class CoreStateModelCompleter implements GalleonCoreContentCompleter<ProvisioningSession> {

    @Override
    public List<String> complete(PmCompleterInvocation invoc, ProvisioningSession context) {
        Set<String> models = new HashSet<>();
        // expect the dependencies to have some default configs to retrieve models from.
        for (FeatureContainer fc : context.getState().getContainer().getFullDependencies().values()) {
            Map<String, List<ConfigInfo>> configs = fc.getFinalConfigs();
            if (configs != null) {
                models.addAll(configs.keySet());
            }
        }
        List<String> result = new ArrayList<>(models);
        return result;
    }

}
