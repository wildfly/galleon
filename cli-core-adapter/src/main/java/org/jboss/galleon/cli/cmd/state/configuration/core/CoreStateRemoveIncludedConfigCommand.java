/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.configuration.core;

import java.io.IOException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.state.configuration.StateRemoveIncludedConfigCommand;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.config.FeaturePackConfig;

/**
 *
 * @author jdenise@redhat.com
 */
public class CoreStateRemoveIncludedConfigCommand extends CoreAbstractProvisionedDefaultConfigCommand<StateRemoveIncludedConfigCommand> {

    @Override
    protected void runCommand(ProvisioningSession session, State state, FeaturePackConfig config, StateRemoveIncludedConfigCommand command) throws IOException, ProvisioningException, CommandExecutionException {
        try {
            state.removeIncludedConfiguration(session, ConfigurationUtil.getIncludedConfigurations(session, config, command.getConfiguration()));
        } catch (Exception ex) {
            throw new CommandExecutionException(session.getPmSession(), CliErrors.removeFailed(), ex);
        }
    }

}
