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
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.config.ConfigModel;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "remove-excluded-layers", description = HelpDescriptions.REMOVE_EXCLUDED_LAYERS, activator = ConfigDependentCommandActivator.class)
public class StateRemoveExcludedLayersCommand extends AbstractLayersCommand {

    public static class LayersCompleter extends AbstractCommaSeparatedCompleter {

        @Override
        protected List<String> getItems(PmCompleterInvocation completerInvocation) {
            AbstractLayersCommand cmd = (AbstractLayersCommand) completerInvocation.getCommand();
            ConfigId config = cmd.getConfig();
            if (config == null) {
                return Collections.emptyList();
            }
            ConfigModel cModel = completerInvocation.getPmSession().getState().getConfig().getDefinedConfig(config);
            if (cModel == null) {
                return Collections.emptyList();
            }
            return new ArrayList<>(cModel.getExcludedLayers());
        }

    }

    @Option(required = true, description = HelpDescriptions.INSTALL_LAYERS, completer = LayersCompleter.class)
    private String layers;

    @Override
    protected void runCommand(PmCommandInvocation invoc, State state) throws IOException, ProvisioningException, CommandExecutionException {
        try {
            state.removeExcludedLayersConfiguration(invoc.getPmSession(), getConfiguration(state), layers.split(",+"));
        } catch (Exception ex) {
            throw new CommandExecutionException(invoc.getPmSession(), CliErrors.removeFailed(), ex);
        }
    }
}
