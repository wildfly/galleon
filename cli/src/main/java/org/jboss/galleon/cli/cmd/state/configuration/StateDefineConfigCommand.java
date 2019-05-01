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
package org.jboss.galleon.cli.cmd.state.configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Option;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.AbstractCompleter;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.CommandDomain;
import org.jboss.galleon.cli.cmd.state.AbstractStateCommand;
import org.jboss.galleon.cli.cmd.state.StateActivators.FPDependentCommandActivator;
import org.jboss.galleon.cli.model.ConfigInfo;
import org.jboss.galleon.cli.model.FeatureContainer;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.config.ConfigId;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "define-config", description = HelpDescriptions.DEFINE_CONFIG, activator = FPDependentCommandActivator.class)
public class StateDefineConfigCommand extends AbstractStateCommand {

    public static class ModelCompleter extends AbstractCompleter {

        @Override
        protected List<String> getItems(PmCompleterInvocation completerInvocation) {
            Set<String> models = new HashSet<>();
            // expect the dependencies to have some default configs to retrieve models from.
            for (FeatureContainer fc : completerInvocation.getPmSession().getState().getContainer().getFullDependencies().values()) {
                Map<String, List<ConfigInfo>> configs = fc.getFinalConfigs();
                if (configs != null) {
                    models.addAll(configs.keySet());
                }
            }
            List<String> result = new ArrayList<>(models);
            return result;
        }

    }

    @Option(required = true, description = HelpDescriptions.CONFIGURATION_MODEL, completer = ModelCompleter.class)
    private String model;

    @Option(required = true, description = HelpDescriptions.CONFIGURATION_NAME)
    private String name;

    @Override
    public CommandDomain getDomain() {
        return CommandDomain.EDITING;
    }

    @Override
    protected void runCommand(PmCommandInvocation invoc, State state) throws IOException, ProvisioningException, CommandExecutionException {
        try {
            state.defineConfiguration(invoc.getPmSession(), new ConfigId(model, name));
        } catch (Exception ex) {
            throw new CommandExecutionException(invoc.getPmSession(), CliErrors.defineConfigFailed(), ex);
        }
    }
}
