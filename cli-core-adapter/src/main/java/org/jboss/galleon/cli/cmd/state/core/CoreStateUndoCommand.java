/*
 * Copyright 2016-2023 Red Hat, Inc. and/or its affiliates
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
