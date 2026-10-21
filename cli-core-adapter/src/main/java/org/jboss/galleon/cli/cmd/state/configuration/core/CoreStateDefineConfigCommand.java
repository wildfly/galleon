/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.configuration.core;

import java.io.IOException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.state.configuration.StateDefineConfigCommand;
import org.jboss.galleon.cli.cmd.state.core.CoreAbstractStateCommand;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.config.ConfigId;

/**
 *
 * @author jdenise@redhat.com
 */
public class CoreStateDefineConfigCommand extends CoreAbstractStateCommand<StateDefineConfigCommand> {

    @Override
    protected void runCommand(ProvisioningSession invoc, State state, StateDefineConfigCommand command) throws IOException, ProvisioningException, CommandExecutionException {
        try {
            state.defineConfiguration(invoc, new ConfigId(command.getModel(), command.getName()));
        } catch (Exception ex) {
            throw new CommandExecutionException(invoc.getPmSession(), CliErrors.defineConfigFailed(), ex);
        }
    }
}
