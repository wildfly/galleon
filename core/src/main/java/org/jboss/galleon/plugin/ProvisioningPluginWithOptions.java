/*
 * Copyright ${license.git.copyrightYears} Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.galleon.plugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class ProvisioningPluginWithOptions implements ProvisioningPlugin {

    private Map<String, PluginOption> pluginOptions;

    @Override
    public Map<String, PluginOption> getOptions() {
        if(pluginOptions == null) {
            final List<PluginOption> options= initPluginOptions();
            if(options.isEmpty()) {
                pluginOptions = Collections.emptyMap();
            } else if(options.size() == 1) {
                final PluginOption option = options.get(0);
                pluginOptions = Collections.singletonMap(option.getName(), option);
            } else {
                pluginOptions = new HashMap<>(options.size());
                for(int i = 0; i < options.size(); ++i) {
                    final PluginOption option = options.get(i);
                    pluginOptions.put(option.getName(), option);
                }
            }
        }
        return pluginOptions;
    }

    protected abstract List<PluginOption> initPluginOptions();
}
