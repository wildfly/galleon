/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.configuration.core;

import java.io.IOException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.state.configuration.StateIncludeConfigCommand;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.config.FeaturePackConfig;

/**
 *
 * @author jdenise@redhat.com
 */
public class CoreStateIncludeConfigCommand extends CoreAbstractDefaultConfigCommand<StateIncludeConfigCommand> {

    @Override
    protected void runCommand(ProvisioningSession session, State state, FeaturePackConfig config, StateIncludeConfigCommand command) throws IOException, ProvisioningException, CommandExecutionException {
        try {
            state.includeConfiguration(session, ConfigurationUtil.getConfigurations(session,
                    config, command.getConfiguration()));
        } catch (Exception ex) {
            throw new CommandExecutionException(session.getPmSession(), CliErrors.includeFailed(), ex);
        }
    }
}
