/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.configuration.core;

import java.io.IOException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.state.configuration.StateResetConfigCommand;
import org.jboss.galleon.cli.cmd.state.core.CoreAbstractStateCommand;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.model.ConfigInfo;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.cli.path.FeatureContainerPathConsumer;
import org.jboss.galleon.cli.path.PathConsumerException;
import org.jboss.galleon.cli.path.PathParser;
import org.jboss.galleon.cli.path.PathParserException;

/**
 * @author jdenise@redhat.com
 */
public class CoreStateResetConfigCommand extends CoreAbstractStateCommand<StateResetConfigCommand> {

    protected ConfigInfo getConfiguration(State state, StateResetConfigCommand command) throws PathParserException, PathConsumerException, ProvisioningException, Exception {
        String path = FeatureContainerPathConsumer.FINAL_CONFIGS_PATH + command.getConfiguration() + PathParser.PATH_SEPARATOR;
        FeatureContainerPathConsumer consumer = new FeatureContainerPathConsumer(state.getContainer(), false);
        PathParser.parse(path, consumer);
        ConfigInfo ci = consumer.getConfig();
        if (ci == null) {
            throw new ProvisioningException("Not a valid config " + command.getConfiguration());
        }
        return ci;
    }

    @Override
    protected void runCommand(ProvisioningSession invoc, State state, StateResetConfigCommand command) throws IOException, ProvisioningException, CommandExecutionException {
        try {
            state.resetConfiguration(invoc, getConfiguration(state, command));
        } catch (Exception ex) {
            throw new CommandExecutionException(invoc.getPmSession(), CliErrors.resetConfigFailed(), ex);
        }
    }
}
