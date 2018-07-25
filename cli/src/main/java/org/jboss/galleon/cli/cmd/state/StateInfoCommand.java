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
package org.jboss.galleon.cli.cmd.state;

import java.util.ArrayList;
import static org.jboss.galleon.cli.path.FeatureContainerPathConsumer.CONFIGS;
import static org.jboss.galleon.cli.path.FeatureContainerPathConsumer.DEPENDENCIES;

import java.util.List;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Option;
import org.jboss.galleon.cli.AbstractStateCommand;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.cmd.CliErrors;
import static org.jboss.galleon.cli.cmd.state.InfoTypeCompleter.ALL;
import org.jboss.galleon.cli.model.FeatureContainer;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "info", description = "Display information for an installation directory or editing state")
public class StateInfoCommand extends AbstractStateCommand {

    @Option(completer = InfoTypeCompleter.class)
    private String type;

    @Override
    protected void runCommand(PmCommandInvocation invoc) throws CommandExecutionException {
        try {
            ProvisioningConfig config = getProvisioningConfig(invoc.getPmSession());
            if (type == null) {
                displayFeaturePacks(invoc, config);
            } else {
                switch (type) {
                    case ALL: {
                        FeatureContainer container = getFeatureContainer(invoc.getPmSession());
                        displayFeaturePacks(invoc, config);
                        displayDependencies(invoc, container);
                        displayConfigs(invoc, container);
                        break;
                    }
                    case CONFIGS: {
                        FeatureContainer container = getFeatureContainer(invoc.getPmSession());
                        displayFeaturePacks(invoc, config);
                        displayConfigs(invoc, container);
                        break;
                    }
                    case DEPENDENCIES: {
                        FeatureContainer container = getFeatureContainer(invoc.getPmSession());
                        displayFeaturePacks(invoc, config);
                        displayDependencies(invoc, container);
                        break;
                    }
                    default: {
                        throw new CommandExecutionException(CliErrors.invalidInfoType());
                    }
                }
            }
        } catch (Exception ex) {
            throw new CommandExecutionException(invoc.getPmSession(), CliErrors.infoFailed(), ex);
        }
    }

    private void displayConfigs(PmCommandInvocation invoc, FeatureContainer container) {
        String str = StateInfoUtil.buildConfigs(container.getFinalConfigs());
        if (str != null) {
            invoc.println(str);
        }
    }

    private void displayDependencies(PmCommandInvocation invoc, FeatureContainer container) {
        List<FeaturePackLocation> locs = new ArrayList<>();
        ProvisioningConfig config = container.getProvisioningConfig();
        if (container.getFullDependencies().isEmpty()) {
            for (FPID id : container.getDependencies()) {
                if (containsDeps(config, id)) {
                    continue;
                }
                locs.add(invoc.getPmSession().
                        getExposedLocation(id.getLocation()));
            }
        } else { // A State in memory.
            for (FeatureContainer c : container.getFullDependencies().values()) {
                if (containsDeps(config, c.getFPID())) {
                    continue;
                }
                locs.add(invoc.getPmSession().
                        getExposedLocation(c.getFPID().getLocation()));
            }
        }
        String str = StateInfoUtil.buildDependencies(locs, container.getConfigs());
        if (str != null) {
            invoc.println(str);
        }
    }

    private static boolean containsDeps(ProvisioningConfig config, FPID id) {
        for (FeaturePackConfig c : config.getFeaturePackDeps()) {
            if (c.getLocation().getFPID().equals(id)) {
                return true;
            }
        }
        return false;
    }

    private void displayFeaturePacks(PmCommandInvocation invoc, ProvisioningConfig config) {
        StateInfoUtil.printFeaturePacks(invoc, config.getFeaturePackDeps());
    }
}
