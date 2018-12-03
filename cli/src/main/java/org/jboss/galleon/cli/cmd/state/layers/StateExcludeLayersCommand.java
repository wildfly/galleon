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
package org.jboss.galleon.cli.cmd.state.layers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Option;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.cmd.AbstractCommaSeparatedCompleter;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.state.StateActivators.ConfigDependentCommandActivator;
import org.jboss.galleon.cli.model.ConfigInfo;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.config.ConfigId;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "exclude-layers", description = HelpDescriptions.EXCLUDE_LAYERS, activator = ConfigDependentCommandActivator.class)
public class StateExcludeLayersCommand extends AbstractLayersCommand {

    public static class LayersCompleter extends AbstractCommaSeparatedCompleter {

        @Override
        protected List<String> getItems(PmCompleterInvocation completerInvocation) {
            AbstractLayersCommand cmd = (AbstractLayersCommand) completerInvocation.getCommand();
            ConfigId config = cmd.getConfig();
            if (config == null) {
                return Collections.emptyList();
            }
            List<ConfigInfo> configs = completerInvocation.getPmSession().getState().getContainer().getFinalConfigs().get(config.getModel());
            ConfigInfo targetConfig = null;
            if (configs != null) {
                for (ConfigInfo ci : configs) {
                    if (ci.getId().equals(config)) {
                        targetConfig = ci;
                        break;
                    }
                }
            }
            if (targetConfig == null) {
                return Collections.emptyList();
            }
            List<String> layers = new ArrayList<>();
            for (ConfigId layer : completerInvocation.getPmSession().getState().getContainer().getLayers()) {
                if (layer.getModel().equals(config.getModel()) && targetConfig.getlayers().contains(layer)) {
                    layers.add(layer.getName());
                }
            }
            return layers;
        }

    }

    @Option(required = true, description = HelpDescriptions.INSTALL_LAYERS, completer = LayersCompleter.class)
    private String layers;

    @Override
    protected void runCommand(PmCommandInvocation invoc, State state) throws IOException, ProvisioningException, CommandExecutionException {
        try {
            state.excludeLayersConfiguration(invoc.getPmSession(), getConfiguration(state), layers.split(",+"));
        } catch (Exception ex) {
            throw new CommandExecutionException(invoc.getPmSession(), CliErrors.excludeFailed(), ex);
        }
    }
}
