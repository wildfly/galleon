/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.core;

import java.io.IOException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.state.StatePwdCommand;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.model.state.State;

public class CoreStatePwdCommand extends CoreAbstractStateCommand<StatePwdCommand> {

    @Override
    protected void runCommand(ProvisioningSession invoc, State state, StatePwdCommand command) throws IOException, ProvisioningException, CommandExecutionException {
        String p = invoc.getCurrentPath();
        invoc.getCommandInvocation().println(p);
    }
}
