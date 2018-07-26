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
import java.util.HashMap;
import static org.jboss.galleon.cli.path.FeatureContainerPathConsumer.CONFIGS;
import static org.jboss.galleon.cli.path.FeatureContainerPathConsumer.DEPENDENCIES;

import java.util.List;
import java.util.Map;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Option;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.AbstractStateCommand;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.cmd.CliErrors;
import static org.jboss.galleon.cli.cmd.state.InfoTypeCompleter.ALL;
import org.jboss.galleon.cli.model.FeatureContainer;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.layout.ProvisioningLayout;
import org.jboss.galleon.layout.ProvisioningLayout.FeaturePackLayout;
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
            if (!config.hasFeaturePackDeps()) {
                return;
            }
            if (type == null) {
                displayFeaturePacks(invoc, config);
            } else {
                try (ProvisioningLayout<FeaturePackLayout> layout = invoc.getPmSession().getLayoutFactory().newConfigLayout(config)) {
                    switch (type) {
                        case ALL: {
                            FeatureContainer container = getFeatureContainer(invoc.getPmSession(), layout);
                            displayFeaturePacks(invoc, config);
                            displayDependencies(invoc, layout);
                            displayConfigs(invoc, container);
                            break;
                        }
                        case CONFIGS: {
                            FeatureContainer container = getFeatureContainer(invoc.getPmSession(), layout);
                            displayFeaturePacks(invoc, config);
                            displayConfigs(invoc, container);
                            break;
                        }
                        case DEPENDENCIES: {
                            displayFeaturePacks(invoc, config);
                            displayDependencies(invoc, layout);
                            break;
                        }
                        default: {
                            throw new CommandExecutionException(CliErrors.invalidInfoType());
                        }
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

    private void displayFeaturePacks(PmCommandInvocation invoc, ProvisioningConfig config) {
        StateInfoUtil.printFeaturePacks(invoc, config.getFeaturePackDeps());
    }

    private void displayDependencies(PmCommandInvocation invoc, ProvisioningLayout<FeaturePackLayout> layout) throws ProvisioningException {
        Map<FPID, FeaturePackConfig> configs = new HashMap<>();
        List<FeaturePackLocation> dependencies = new ArrayList<>();
        for (FeaturePackLayout fpLayout : layout.getOrderedFeaturePacks()) {
            boolean isProduct = true;
            for (FeaturePackLayout fpLayout2 : layout.getOrderedFeaturePacks()) {
                if (fpLayout2.getSpec().hasTransitiveDep(fpLayout.getFPID().getProducer())
                        || fpLayout2.getSpec().getFeaturePackDep(fpLayout.getFPID().getProducer()) != null) {
                    isProduct = false;
                    break;
                }
            }
            if (!isProduct) {
                FeaturePackLocation loc = invoc.getPmSession().getExposedLocation(fpLayout.getFPID().getLocation());
                dependencies.add(loc);
                FeaturePackConfig transitiveConfig = layout.getConfig().getTransitiveDep(fpLayout.getFPID().getProducer());
                configs.put(loc.getFPID(), transitiveConfig);
            }
        }
        String str = StateInfoUtil.buildDependencies(dependencies, configs);
        if (str != null) {
            invoc.println(str);
        }
    }
}
