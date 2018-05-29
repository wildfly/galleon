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
package org.jboss.galleon.cli.cmd.state.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.aesh.command.option.Argument;
import org.aesh.command.option.Option;
import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.cli.AbstractCompleter;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.state.AbstractFPProvisionedCommand;
import org.jboss.galleon.cli.model.Identity;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.cli.path.PathParser;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.universe.FeaturePackLocation.ChannelSpec;
import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class AbstractProvisionedDefaultConfigCommand extends AbstractFPProvisionedCommand {

    public static class ProvisionedConfigCompleter extends AbstractCompleter {

        @Override
        protected List<String> getItems(PmCompleterInvocation completerInvocation) {
            try {
                List<String> configs = new ArrayList<>();
                AbstractProvisionedDefaultConfigCommand cmd = (AbstractProvisionedDefaultConfigCommand) completerInvocation.getCommand();
                FeaturePackConfig fp = cmd.getProvisionedFP(completerInvocation.getPmSession());
                if (fp == null) {
                    // We want them all from all FP
                    for (FeaturePackConfig fc : completerInvocation.getPmSession().getState().getConfig().getFeaturePackDeps()) {
                        for (ConfigId cid : cmd.getTargetedConfigs(fc)) {
                            String name = cid.getModel() + PathParser.PATH_SEPARATOR + cid.getName();
                            if (!configs.contains(name)) {
                                configs.add(name);
                            }
                        }
                    }
                } else {
                    for (ConfigId cid : cmd.getTargetedConfigs(fp)) {
                        String name = cid.getModel() + PathParser.PATH_SEPARATOR + cid.getName();
                        if (!configs.contains(name)) {
                            configs.add(name);
                        }
                    }
                }
                return configs;
            } catch (Exception ex) {
                return Collections.emptyList();
            }
        }

    }

    public static class TargetedFPCompleter extends AbstractCompleter {

        @Override
        protected List<String> getItems(PmCompleterInvocation completerInvocation) {
            try {
                State session = completerInvocation.getPmSession().getState();
                List<String> lst = new ArrayList<>();
                if (session != null) {
                    AbstractProvisionedDefaultConfigCommand cmd = (AbstractProvisionedDefaultConfigCommand) completerInvocation.getCommand();
                    String config = cmd.getConfiguration();
                    for (FeaturePackConfig fc : session.getConfig().getFeaturePackDeps()) {
                        String[] split = config.split("" + PathParser.PATH_SEPARATOR);
                        String model = split[0];
                        String name = split[1];
                        ConfigId cid = new ConfigId(model, name);
                        if (cmd.getTargetedConfigs(fc).contains(cid)) {
                            lst.add(Identity.buildOrigin(fc.getLocation().getChannel()));
                        }
                    }
                }
                return lst;
            } catch (Exception ex) {
                return Collections.emptyList();
            }
        }

    }
    @Argument(required = false, description = "Configuration name",
            completer = ProvisionedConfigCompleter.class)
    private String configuration;

    @Option(completer = TargetedFPCompleter.class)
    protected String origin;

    @Override
    public ChannelSpec getChannel(PmSession session) throws CommandExecutionException {
        if (origin == null) {
            return null;
        }
        return LegacyGalleon1Universe.toFpl(ArtifactCoords.newGav(origin)).getChannel();
    }

    protected String getConfiguration() {
        return configuration;
    }

    protected abstract Set<ConfigId> getTargetedConfigs(FeaturePackConfig cf);

}
