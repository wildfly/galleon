/*
 * Copyright 2016-2019 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.cli.cmd.plugin;

import java.util.HashSet;
import java.util.Set;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningOption;
import org.jboss.galleon.cli.PmCommandActivator;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.layout.FeaturePackPluginVisitor;
import org.jboss.galleon.plugin.InstallPlugin;
import org.jboss.galleon.runtime.ProvisioningRuntime;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class AbstractProvisionCommand extends AbstractProvisionWithPlugins {

    public AbstractProvisionCommand(PmSession pmSession) {
        super(pmSession);
    }

    @Override
    protected String getName() {
        return "provision";
    }

    protected Set<ProvisioningOption> getPluginOptions(ProvisioningRuntime runtime) throws ProvisioningException {
        Set<ProvisioningOption> pluginOptions = new HashSet<>(ProvisioningOption.getStandardList());
        FeaturePackPluginVisitor<InstallPlugin> visitor = new FeaturePackPluginVisitor<InstallPlugin>() {
            @Override
            public void visitPlugin(InstallPlugin plugin) throws ProvisioningException {
                pluginOptions.addAll(plugin.getOptions().values());
            }
        };
        runtime.getLayout().visitPlugins(visitor, InstallPlugin.class);
        return pluginOptions;
    }

    @Override
    protected PmCommandActivator getActivator() {
        return null;
    }
}
