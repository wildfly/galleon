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

import java.io.IOException;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Option;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.CommandDomain;
import org.jboss.galleon.cli.cmd.state.AbstractStateCommand;
import org.jboss.galleon.cli.model.ConfigInfo;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.cli.path.FeatureContainerPathConsumer;
import org.jboss.galleon.cli.path.PathConsumerException;
import org.jboss.galleon.cli.path.PathParser;
import org.jboss.galleon.cli.path.PathParserException;

/**
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "reset-config", description = HelpDescriptions.RESET_CONFIG, activator = ResetConfigCommandActivator.class)
public class StateResetConfigCommand extends AbstractStateCommand {
    @Option(required = true, description = HelpDescriptions.CONFIGURATION_NAME,
            completer = ProvisionedConfigurationCompleter.class)
    private String configuration;

    @Override
    protected void runCommand(PmCommandInvocation invoc, State state) throws IOException, ProvisioningException, CommandExecutionException {
        try {
            state.resetConfiguration(invoc.getPmSession(), getConfiguration(state));
        } catch (Exception ex) {
            throw new CommandExecutionException(invoc.getPmSession(), CliErrors.resetConfigFailed(), ex);
        }
    }

    protected ConfigInfo getConfiguration(State state) throws PathParserException, PathConsumerException, ProvisioningException, Exception {
        String path = FeatureContainerPathConsumer.FINAL_CONFIGS_PATH + configuration + PathParser.PATH_SEPARATOR;
        FeatureContainerPathConsumer consumer = new FeatureContainerPathConsumer(state.getContainer(), false);
        PathParser.parse(path, consumer);
        ConfigInfo ci = consumer.getConfig();
        if (ci == null) {
            throw new ProvisioningException("Not a valid config " + configuration);
        }
        return ci;
    }

    @Override
    public CommandDomain getDomain() {
        return CommandDomain.EDITING;
    }
}
