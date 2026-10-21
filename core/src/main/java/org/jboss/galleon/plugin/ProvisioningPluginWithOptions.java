/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.plugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.galleon.ProvisioningOption;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class ProvisioningPluginWithOptions implements ProvisioningPlugin {

    private Map<String, ProvisioningOption> pluginOptions;

    @Override
    public Map<String, ProvisioningOption> getOptions() {
        if(pluginOptions == null) {
            final List<ProvisioningOption> options= initPluginOptions();
            if(options.isEmpty()) {
                pluginOptions = Collections.emptyMap();
            } else if(options.size() == 1) {
                final ProvisioningOption option = options.get(0);
                pluginOptions = Collections.singletonMap(option.getName(), option);
            } else {
                pluginOptions = new HashMap<>(options.size());
                for(int i = 0; i < options.size(); ++i) {
                    final ProvisioningOption option = options.get(i);
                    pluginOptions.put(option.getName(), option);
                }
            }
        }
        return pluginOptions;
    }

    protected abstract List<ProvisioningOption> initPluginOptions();
}
