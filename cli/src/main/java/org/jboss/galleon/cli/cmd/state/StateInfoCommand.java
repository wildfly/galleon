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

import static org.jboss.galleon.cli.path.FeatureContainerPathConsumer.CONFIGS;
import static org.jboss.galleon.cli.path.FeatureContainerPathConsumer.DEPENDENCIES;

import java.util.List;
import java.util.Map.Entry;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Option;
import org.jboss.galleon.ArtifactCoords.Gav;
import org.jboss.galleon.cli.AbstractFeaturePackCommand;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.model.ConfigInfo;
import org.jboss.galleon.cli.model.FeatureContainer;
import org.jboss.galleon.cli.model.FeaturePackInfo;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "info", description = "Display information for a "
        + "feature-pack or installation directory or editing state")
public class StateInfoCommand extends AbstractFeaturePackCommand {

    @Option(completer = StateInfoTypeCompleter.class)
    private String type;

    @Override
    protected void runCommand(PmCommandInvocation invoc) throws CommandExecutionException {
        try {
            FeatureContainer container = getFeatureContainer(invoc.getPmSession(), invoc.getAeshContext());
            if (type == null) {
                displayDependencies(invoc, container);
                displayConfigs(invoc, container);
            } else {
                switch (type) {
                    case CONFIGS: {
                        displayConfigs(invoc, container);
                        break;
                    }
                    case DEPENDENCIES: {
                        displayDependencies(invoc, container);
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            throw new CommandExecutionException(ex);
        }
    }

    private void displayConfigs(PmCommandInvocation invoc, FeatureContainer container) {
        invoc.println("configurations");
        if (container.getFinalConfigs().isEmpty()) {
            invoc.println("  NONE");
        } else {
            for (Entry<String, List<ConfigInfo>> entry : container.getFinalConfigs().entrySet()) {
                invoc.println("  " + entry.getKey());
                for (ConfigInfo info : entry.getValue()) {
                    invoc.println("    " + info.getName());
                }
            }
        }
    }

    private void displayDependencies(PmCommandInvocation invoc, FeatureContainer container) {
        if (container instanceof FeaturePackInfo) {
            invoc.println("feature-pack " + container.getGav());
        }
        invoc.println("dependencies");
        if (container.getDependencies().isEmpty()) {
            invoc.println("  NONE");
        } else {
            for (Gav dep : container.getDependencies()) {
                invoc.println("  " + dep.toString());
            }
        }
    }
}
