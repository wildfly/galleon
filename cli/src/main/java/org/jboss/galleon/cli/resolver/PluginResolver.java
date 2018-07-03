/*
 * Copyright 2016-2018 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.cli.resolver;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.resolver.ResourceResolver.Resolver;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.layout.FeaturePackPluginVisitor;
import org.jboss.galleon.layout.ProvisioningLayout;
import org.jboss.galleon.plugin.DiffPlugin;
import org.jboss.galleon.plugin.InstallPlugin;
import org.jboss.galleon.plugin.PluginOption;
import org.jboss.galleon.universe.FeaturePackLocation;

/**
 *
 * @author jdenise@redhat.com
 */
public class PluginResolver implements Resolver<ResolvedPlugins> {

    private final ProvisioningConfig config;
    private final PmSession session;

    private PluginResolver(PmSession session, ProvisioningConfig config) {
        this.session = session;
        this.config = config;
    }

    public static PluginResolver newResolver(PmSession session, ProvisioningConfig config) {
        return new PluginResolver(session, config);
    }

    public static PluginResolver newResolver(PmSession session, FeaturePackLocation loc) throws ProvisioningDescriptionException {
        ProvisioningConfig config = ProvisioningConfig.builder().addFeaturePackDep(loc).build();
        return new PluginResolver(session, config);
    }

    @Override
    public ResolvedPlugins resolve() throws ResolutionException {
        ResolvedPlugins plugins = null;
        try {
            try (ProvisioningLayout layout = session.getLayoutFactory().newConfigLayout(config)) {
                if (layout.hasPlugins()) {
                    Set<PluginOption> installOptions = new HashSet<>();
                    FeaturePackPluginVisitor<InstallPlugin> visitor = new FeaturePackPluginVisitor<InstallPlugin>() {
                        @Override
                        public void visitPlugin(InstallPlugin plugin) throws ProvisioningException {
                            installOptions.addAll(plugin.getOptions().values());
                        }
                    };
                    layout.visitPlugins(visitor, InstallPlugin.class);
                    Set<PluginOption> diffOptions = new HashSet<>();
                    FeaturePackPluginVisitor<DiffPlugin> diffVisitor = new FeaturePackPluginVisitor<DiffPlugin>() {
                        @Override
                        public void visitPlugin(DiffPlugin plugin) throws ProvisioningException {
                            diffOptions.addAll(plugin.getOptions().values());
                        }
                    };
                    layout.visitPlugins(diffVisitor, DiffPlugin.class);
                    plugins = new ResolvedPlugins(installOptions, diffOptions);
                }
            }
        } catch (Exception ex) {
            throw new ResolutionException(ex.getLocalizedMessage(), ex);
        }
        return plugins == null ? new ResolvedPlugins(Collections.emptySet(), Collections.emptySet()) : plugins;
    }

}
