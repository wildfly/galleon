/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.core;

import java.io.IOException;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.state.StateUndoCommand;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.model.state.State;

/**
 * Dual command, applies in case of edit mode or not.
 *
 * @author jdenise@redhat.com
 */
public class CoreStateUndoCommand extends CoreAbstractStateCommand<StateUndoCommand> {

    @Override
    protected void runCommand(ProvisioningSession session, State state, StateUndoCommand command) throws IOException, ProvisioningException, CommandExecutionException {
        try {
            if (!state.hasActions()) {
                throw new ProvisioningException(Errors.historyIsEmpty());
            }
            state.pop(session);
        } catch (ProvisioningException | IOException ex) {
            throw new CommandExecutionException(session.getCommandInvocation().getPmSession(), CliErrors.undoFailed(), ex);
        }
    }

}
