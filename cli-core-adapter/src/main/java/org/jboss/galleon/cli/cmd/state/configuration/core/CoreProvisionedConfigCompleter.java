/*
 * Copyright 2016-2023 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.cli.cmd.state.configuration.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.jboss.galleon.cli.CliLogging;
import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.cmd.state.configuration.AbstractProvisionedDefaultConfigCommand;
import org.jboss.galleon.cli.cmd.state.core.CoreAbstractFPProvisionedCommand;
import org.jboss.galleon.cli.core.GalleonCoreContentCompleter;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.path.PathParser;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.universe.FeaturePackLocation;

/**
 *
 * @author jdenise@redhat.com
 */
public class CoreProvisionedConfigCompleter implements GalleonCoreContentCompleter<ProvisioningSession> {

    @Override
    public List<String> complete(PmCompleterInvocation invoc, ProvisioningSession context) {
        try {
            List<String> configs = new ArrayList<>();
            AbstractProvisionedDefaultConfigCommand cmd = (AbstractProvisionedDefaultConfigCommand) invoc.getCommand();
            FeaturePackLocation.ProducerSpec spec = CoreAbstractProvisionedDefaultConfigCommand.getProducerSpec(context, cmd);
            FeaturePackConfig fp = CoreAbstractFPProvisionedCommand.getProvisionedFPConfig(spec, context, cmd);
            if (fp == null) {
                // We want them all from all FP
                for (FeaturePackConfig fc : context.getState().getConfig().getFeaturePackDeps()) {
                    Collection<ConfigId> configList = cmd.isIncludedConfigs() ? fc.getIncludedConfigs() : fc.getExcludedConfigs();
                    for (ConfigId cid : configList) {
                        String name = cid.getModel() + PathParser.PATH_SEPARATOR + cid.getName();
                        if (!configs.contains(name)) {
                            configs.add(name);
                        }
                    }
                }
            } else {
                Collection<ConfigId> configList = cmd.isIncludedConfigs() ? fp.getIncludedConfigs() : fp.getExcludedConfigs();
                for (ConfigId cid : configList) {
                    String name = cid.getModel() + PathParser.PATH_SEPARATOR + cid.getName();
                    if (!configs.contains(name)) {
                        configs.add(name);
                    }
                }
            }
            return configs;
        } catch (Exception ex) {
            CliLogging.completionException(ex);
            return Collections.emptyList();
        }
    }

}
