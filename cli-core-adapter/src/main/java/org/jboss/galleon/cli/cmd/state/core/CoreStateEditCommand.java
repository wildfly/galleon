/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.core;

import java.io.IOException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.CliErrors;
import static org.jboss.galleon.cli.cmd.state.StateCommand.WELCOME_STATE_MSG;
import org.jboss.galleon.cli.cmd.state.StateEditCommand;
import org.jboss.galleon.cli.core.GalleonCoreExecution;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.model.state.State;

/**
 *
 * @author jdenise@redhat.com
 */
public class CoreStateEditCommand implements GalleonCoreExecution<ProvisioningSession, StateEditCommand>  {

    @Override
    public void execute(ProvisioningSession session, StateEditCommand command) throws CommandExecutionException {
        State state;
        try {
            state = new State(session,
                    command.getInstallationHome(session.getPmSession().getAeshContext()));
        } catch (ProvisioningException | IOException ex) {
            throw new CommandExecutionException(session.getPmSession(), CliErrors.readContentFailed(), ex);
        }
        session.setState(state);
        session.getCommandInvocation().setPrompt(session.getPmSession().buildPrompt(state.getPath()));
        session.getCommandInvocation().println(WELCOME_STATE_MSG);
    }

}
